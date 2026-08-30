#!/usr/bin/env python3
"""Publish an Android App Bundle to a Google Play track.

Written out rather than delegated to a third-party action because this step is
handed both the signing output and the Play credential, and a hand-rolled
hundred lines is easier to audit than a dependency that gets the same secret.

Every failure path deletes the edit it opened. A Play edit is a scratch
workspace: nothing it holds reaches anyone until it is committed, so a run that
dies halfway leaves the listing untouched rather than half-updated.
"""
from __future__ import annotations

import argparse
import base64
import binascii
import json
import os
import sys

import google.auth.transport.requests
from google.oauth2 import service_account

SCOPE = "https://www.googleapis.com/auth/androidpublisher"
API = "https://androidpublisher.googleapis.com/androidpublisher/v3/applications"
UPLOAD = "https://androidpublisher.googleapis.com/upload/androidpublisher/v3/applications"

# Play truncates anything longer, silently. Better to do it here and say so.
MAX_RELEASE_NOTES = 500


def log(message: str) -> None:
    print(message, flush=True)


def fail(message: str) -> None:
    # Renders as an annotation on the workflow run rather than a line in a log
    # nobody opens.
    print(f"::error::{message}", flush=True)
    sys.exit(1)


def load_credentials() -> service_account.Credentials:
    """Accepts the service account key as raw JSON or base64-wrapped JSON.

    Both are in circulation: `gh secret set < key.json` stores it raw, while
    every tutorial says to base64 it first. Guessing wrong is a confusing
    failure, so accept either.
    """
    raw = os.environ.get("GOOGLE_PLAY_SERVICE_ACCOUNT_JSON", "").strip()
    if not raw:
        fail("GOOGLE_PLAY_SERVICE_ACCOUNT_JSON is empty or unset.")

    if not raw.startswith("{"):
        try:
            raw = base64.b64decode(raw, validate=True).decode("utf-8").strip()
        except (binascii.Error, UnicodeDecodeError):
            fail(
                "GOOGLE_PLAY_SERVICE_ACCOUNT_JSON is neither JSON nor valid "
                "base64. Re-add the secret from the key file."
            )

    try:
        info = json.loads(raw)
    except json.JSONDecodeError as exc:
        fail(f"GOOGLE_PLAY_SERVICE_ACCOUNT_JSON did not parse as JSON: {exc}")

    if info.get("type") != "service_account":
        fail(f"Expected a service account key, got type={info.get('type')!r}.")

    log(f"authenticating as {info.get('client_email')}")
    return service_account.Credentials.from_service_account_info(info, scopes=[SCOPE])


def api_error(response) -> str:
    try:
        error = response.json().get("error", {})
        return f"HTTP {response.status_code} {error.get('status', '')}: {error.get('message', '')}"
    except ValueError:
        return f"HTTP {response.status_code}: {response.text[:400]}"


def read_release_notes(args) -> str | None:
    """The GitHub release body, falling back to the fastlane changelog.

    The fallback is what makes a manual re-run work: workflow_dispatch has no
    release body, and fastlane/metadata already carries the same text per
    version code for F-Droid.
    """
    text = ""
    if args.notes_file and os.path.exists(args.notes_file):
        with open(args.notes_file, encoding="utf-8") as handle:
            text = handle.read().strip()

    if not text:
        fallback = f"fastlane/metadata/android/{args.notes_language}/changelogs/{args.version_code}.txt"
        if os.path.exists(fallback):
            with open(fallback, encoding="utf-8") as handle:
                text = handle.read().strip()
            log(f"release body was empty, using {fallback}")

    if not text:
        log("no release notes found; publishing without a What's new entry")
        return None

    if len(text) > MAX_RELEASE_NOTES:
        log(
            f"::warning::Release notes are {len(text)} characters; Play allows "
            f"{MAX_RELEASE_NOTES}. Truncating."
        )
        text = text[: MAX_RELEASE_NOTES - 1].rstrip() + "…"
    return text


def main() -> None:
    parser = argparse.ArgumentParser()
    parser.add_argument("--package", required=True)
    parser.add_argument("--aab", required=True)
    parser.add_argument("--track", required=True)
    parser.add_argument("--version-code", required=True, type=int)
    parser.add_argument("--release-name", required=True)
    parser.add_argument("--notes-file")
    parser.add_argument("--notes-language", default="en-US")
    parser.add_argument(
        "--dry-run",
        action="store_true",
        help="Do everything except commit, then delete the edit.",
    )
    args = parser.parse_args()

    if not os.path.exists(args.aab):
        fail(f"No bundle at {args.aab}")

    session = google.auth.transport.requests.AuthorizedSession(load_credentials())
    base = f"{API}/{args.package}"

    response = session.post(f"{base}/edits", timeout=60)
    if response.status_code // 100 != 2:
        fail(
            "Could not open a Play edit, so the service account cannot publish "
            f"this app. {api_error(response)}"
        )
    edit_id = response.json()["id"]
    log(f"opened edit {edit_id}")

    committed = False
    try:
        # Checked before the upload rather than after, because Play's own error
        # for a duplicate version code arrives at the end of a multi-megabyte
        # upload and does not say which codes are taken.
        response = session.get(f"{base}/edits/{edit_id}/bundles", timeout=60)
        if response.status_code // 100 == 2:
            existing = [b.get("versionCode") for b in response.json().get("bundles", [])]
            log(f"version codes already on Play: {existing or 'none'}")
            if args.version_code in existing:
                fail(
                    f"versionCode {args.version_code} is already uploaded. Bump "
                    "versionCode in app/build.gradle and cut a new release."
                )

        with open(args.aab, "rb") as handle:
            payload = handle.read()
        log(f"uploading {len(payload):,} bytes")
        response = session.post(
            f"{UPLOAD}/{args.package}/edits/{edit_id}/bundles?uploadType=media",
            data=payload,
            headers={"Content-Type": "application/octet-stream"},
            timeout=900,
        )
        if response.status_code // 100 != 2:
            fail(f"Bundle upload rejected. {api_error(response)}")

        uploaded = response.json().get("versionCode")
        log(f"uploaded versionCode {uploaded}")
        if int(uploaded) != args.version_code:
            fail(
                f"Play recorded versionCode {uploaded} but the build declared "
                f"{args.version_code}. Refusing to roll out a mismatch."
            )

        release: dict = {
            "name": args.release_name,
            "status": "completed",
            "versionCodes": [str(uploaded)],
        }
        notes = read_release_notes(args)
        if notes:
            release["releaseNotes"] = [{"language": args.notes_language, "text": notes}]

        response = session.put(
            f"{base}/edits/{edit_id}/tracks/{args.track}",
            json={"track": args.track, "releases": [release]},
            timeout=120,
        )
        if response.status_code // 100 != 2:
            fail(f"Could not assign the bundle to the {args.track} track. {api_error(response)}")
        log(f"assigned versionCode {uploaded} to {args.track}")

        if args.dry_run:
            log("dry run: everything validated, edit will be discarded uncommitted")
            return

        response = session.post(f"{base}/edits/{edit_id}:commit", timeout=300)
        if response.status_code // 100 != 2:
            fail(f"Commit failed. {api_error(response)}")
        committed = True
        log(f"committed. {args.release_name} is live on {args.track}.")
    finally:
        if not committed:
            deleted = session.delete(f"{base}/edits/{edit_id}", timeout=60)
            log(f"discarded edit {edit_id} (HTTP {deleted.status_code}); nothing published")


if __name__ == "__main__":
    main()
