# Publishing to Google Play

`.github/workflows/release-play.yaml` ships a release when a GitHub release is
published. It does not touch the version: the release is expected to point at a
commit where `versionCode` and `versionName` were already raised by hand, and the
run fails if the tag and `app/build.gradle` disagree.

## The flow

1. **CI** — debug build, unit tests, lint. Everything below is gated on this.
2. **Guards** — tag matches `versionName`; all five secrets present; the keystore
   opens; the built bundle carries the certificate Play expects; the version code
   is not already uploaded.
3. **Build** — `bundleRelease`, signed with the upload key.
4. **Publish** — upload the bundle, file the GitHub release body as *What's new*,
   assign the track, commit.

Track selection: a normal release goes to `production`, a GitHub **prerelease**
goes to `beta`, and `workflow_dispatch` lets you pick. Change `DEFAULT_TRACK` at
the top of the workflow to stage through `internal` instead.

## The two keys, which are not the same key

Play App Signing means Google holds the **app signing key** and re-signs your
upload before distributing it. It does not mean you can upload without a key.
Every upload must still be signed with the **upload key**, and Play rejects
anything else:

```
The Android App Bundle was signed with the wrong key.
Found: SHA1: … , expected: SHA1: 70:97:AC:82:…:DD:53
```

The service account JSON authenticates the API *call*. It does not sign the
*artifact*. Both are required.

The upload certificate Play expects for `com.mua.unmukto` is
`70:97:AC:82:89:82:2E:9B:EC:5D:33:B6:07:FE:9C:74:EE:0E:DD:53`, which the
workflow checks locally before uploading. A fingerprint is not a secret — it is
in every published APK — and having it in the workflow turns a 403 at the end of
a multi-megabyte upload into a clear failure a step earlier.

If the upload key is ever lost, it is recoverable: Play Console →
*Setup → App integrity → Upload key certificate → Request upload key reset*.
Losing the app signing key, once Google holds it, is not your problem to solve.

## Secrets

Five, all prefixed `GOOGLE_PLAY_`.

| Secret | What it is |
| --- | --- |
| `GOOGLE_PLAY_SERVICE_ACCOUNT_JSON` | The service account key, raw JSON or base64 |
| `GOOGLE_PLAY_UPLOAD_KEYSTORE` | The upload keystore, base64 |
| `GOOGLE_PLAY_UPLOAD_KEYSTORE_PASSWORD` | Store password |
| `GOOGLE_PLAY_UPLOAD_KEY_ALIAS` | Key alias |
| `GOOGLE_PLAY_UPLOAD_KEY_PASSWORD` | Key password |

The JSON is one secret rather than a field per key. The `private_key` is a
multi-line PEM, and splitting it across fields means reassembling newlines in
YAML, which is where these break. The workflow accepts it raw or base64 because
both are in circulation.

```sh
gh secret set GOOGLE_PLAY_SERVICE_ACCOUNT_JSON < /path/to/service-account.json
base64 -w0 /path/to/upload-keystore.jks | gh secret set GOOGLE_PLAY_UPLOAD_KEYSTORE
gh secret set GOOGLE_PLAY_UPLOAD_KEYSTORE_PASSWORD   # prompts, nothing in shell history
gh secret set GOOGLE_PLAY_UPLOAD_KEY_ALIAS
gh secret set GOOGLE_PLAY_UPLOAD_KEY_PASSWORD
```

The keystore stays in `secret/`, which is git-ignored, and CI restores it there
from the secret.

## Release notes

The GitHub release body becomes the *What's new* text, filed under `en-US`.

Play caps release notes at **500 characters** per language and truncates silently;
the workflow truncates deliberately and warns in the log instead.

When the body is empty — a `workflow_dispatch` re-run, say — it falls back to
`fastlane/metadata/android/en-US/changelogs/<versionCode>.txt`, the same file
F-Droid reads.

## Trying it without publishing

Run the workflow manually with **dry run** ticked. It builds, signs, verifies,
uploads and assigns the track, then deletes the edit. A Play edit is a scratch
workspace: nothing in it reaches anyone until it is committed, so a dry run —
or a run that fails halfway — leaves the listing untouched.
