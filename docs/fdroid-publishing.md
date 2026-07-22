# Publishing Unmukto on F-Droid

Prepared in response to [issue #5](https://github.com/maifeeulasad/unmukto/issues/5), where
@WPFilmmaker suggested distributing Unmukto through F-Droid alongside the Play Store release.

F-Droid is an app store that only carries free and open source software. Unlike Play, it does
not accept an APK you upload: it builds every app itself, from a public git tag, on its own
infrastructure, and it reads the source while doing so. That has two consequences worth
understanding before starting.

- **The source has to be genuinely buildable by a stranger.** No signing secrets, no private
  dependencies, no steps that only work on your machine.
- **You do not control the release.** F-Droid signs the APK with its own key, so the F-Droid
  build and the Play build are different artifacts and cannot be installed over one another.
  A user has to uninstall one to switch to the other. This is expected and normal; it is not
  something to work around.

---

## 1. Where Unmukto stands

Measured against the [inclusion policy](https://f-droid.org/docs/Inclusion_Policy/).

| Requirement | Status |
|---|---|
| FOSS license file in the repo | **Fixed in this branch** — was missing entirely |
| No proprietary dependencies | Pass — no Play Services, Firebase or Crashlytics |
| No tracking, analytics or ads | Pass — no such library is present |
| No network access | Pass — the manifest declares **zero** permissions |
| Builds from source with a FOSS toolchain | Pass — Gradle + AGP, no proprietary tooling |
| Release commits carry git tags | **Needs a tag** — see step 3 |
| Version literals readable by regex | Pass — plain literals in `app/build.gradle` |
| Unique application ID | Pass — `com.mua.unmukto` |
| Actively maintained | Your call to affirm in the request |
| Store listing metadata | **Added in this branch** — `fastlane/` tree |

The app is an unusually clean candidate. Most rejections come from Firebase, an analytics SDK,
or a missing license; Unmukto only ever had the last of those.

### The license, which was the real blocker

The repository had no `LICENSE` file, and GitHub reported the license as `NONE`. This is worth
being precise about: absent an explicit grant, default copyright law reserves all rights. The
project was *published* but not *licensed* — legally nobody could redistribute or modify it,
which is exactly what an app store does. F-Droid would have refused it outright.

It is now **GPL-3.0-or-later**, matching the other keyboards already in F-Droid
(AnySoftKeyboard, OpenBoard, FlorisBoard). Every commit in this repository's history is
authored by you, so you were free to make that choice unilaterally. That will stop being true
the moment you merge someone else's contribution, so it is a good thing to have settled now.

### Dependencies

All six are Apache-2.0 and resolve from Google's Maven repository or Maven Central, both of
which F-Droid permits:

`androidx.core:core-ktx` · `androidx.appcompat:appcompat` ·
`com.google.android.material:material` · `junit` · `androidx.test.ext:junit` ·
`androidx.test.espresso:espresso-core`

`com.google.android.material` is published *by* Google but is not a proprietary Google service —
it is the open source Material Components library, and it is used throughout F-Droid's catalogue.

---

## 2. What this branch already did

- `LICENSE` — verbatim GPL-3.0 text from gnu.org
- Per-file license notices in all three source files
- `versionCode` 2 → **3**, `versionName` 1.1 → **1.2** in `app/build.gradle`
- `fastlane/metadata/android/{en-US,bn}/` — title, short and full descriptions, changelogs,
  icon; all within F-Droid's character limits
- `docs/fdroid/com.mua.unmukto.yml` — the build recipe, ready to copy into `fdroiddata`

The version bump matters: Play already carries `versionCode 2`, built from the pre-rewrite
source. Shipping a different build under the same version code would mean one version number
describing two different apps.

---

## 3. What you still need to do

### 3.1 Tag the release

F-Droid builds a tag, not a branch. Nothing can be submitted until one exists.

```sh
git checkout main
git pull
git tag -a v1.2 -m "Unmukto 1.2"
git push origin v1.2
```

The tag name must match the `commit:` field in the recipe (`v1.2`) and, because
`UpdateCheckMode` is `Tags`, every future release has to follow the same `v<versionName>`
pattern or automatic update detection will silently stop working.

### 3.2 Take screenshots

Not required, but the F-Droid page looks abandoned without them, and the images currently
linked from the README are from 2021 and show the *old* key rendering — reusing them would
misrepresent the app. Capture on a device running the current build and drop them in
`fastlane/metadata/android/en-US/images/phoneScreenshots/` as `1.png`, `2.png`, …

Suggested set: base layer, shifted layer, setup screen, dark mode. Then delete the
`README.txt` placeholder in that directory.

### 3.3 Verify the build the way F-Droid will

Worth doing before submitting, because a failure here is a failure in their pipeline too:

```sh
git checkout v1.2
./gradlew clean assembleRelease
```

Note this currently fails on your Windows machine for reasons unrelated to the project — the
Gradle daemon cannot accept its own loopback connection. CI on GitHub Actions builds the same
source successfully, so use that as the check until the local environment is sorted.

### 3.4 Submit

Two routes. **You are the developer, so take the second.**

- **RFP** — <https://gitlab.com/fdroid/rfp/-/issues>. This is a request that *someone else*
  package the app. It is what issue #5 linked to, and it is meant for users nominating apps
  they do not maintain. Expect it to sit in a queue.
- **Direct merge request to `fdroiddata`** — faster and the right route for a maintainer:

  1. Sign in at <https://gitlab.com> and fork <https://gitlab.com/fdroid/fdroiddata>
  2. Copy `docs/fdroid/com.mua.unmukto.yml` to `metadata/com.mua.unmukto.yml` in your fork
  3. If you have the F-Droid server tools installed, validate locally:
     ```sh
     fdroid lint com.mua.unmukto
     fdroid build com.mua.unmukto
     ```
     Skip if not — their CI runs the same checks on the merge request.
  4. Commit, push, and open a merge request labelled **New App**

Expect review comments. A maintainer reads the source for license compliance and confirms the
build reproduces. Once merged, the app appears in the main repository within roughly 24–48
hours.

> Submitting is a public action under your own GitLab identity, so it is left for you to do
> rather than automated here.

---

## 4. Releasing future versions

Once the app is in `fdroiddata`, each release is:

1. Bump `versionCode` and `versionName` in `app/build.gradle`
2. Add `fastlane/metadata/android/en-US/changelogs/<versionCode>.txt` — the filename must be
   the version code exactly, no padding, no `v`
3. Tag `v<versionName>` and push the tag

Because the recipe sets `AutoUpdateMode: Version v%v` and `UpdateCheckMode: Tags`, F-Droid's
bot notices the new tag and opens the metadata update itself. You do not normally touch
`fdroiddata` again after the initial submission.

---

## 5. Things that will bite you later

- **`gradle/wrapper/gradle-wrapper.jar` is a prebuilt binary in the repo.** F-Droid's build
  server verifies it against known-good Gradle releases and substitutes its own copy. This is
  handled automatically for standard wrapper versions — but if you ever hand-edit the wrapper
  or pin an unusual Gradle version, the build will be rejected.
- **Do not compute version values.** Anything like `versionCode gitCommitCount()` breaks
  F-Droid, which extracts these with a regex rather than by running Gradle. The current
  literals are correct; keep them literal. There is a comment in `app/build.gradle` saying so.
- **Release signing config.** `app/build.gradle` reads signing material from the environment
  and falls back to an unsigned build when nothing is set. That fallback is what lets F-Droid
  build the release variant without your keystore. Do not make signing mandatory.
- **The Play listing keeps working.** Being on F-Droid does not affect it. The two are signed
  by different keys and are independent installs.
- **`privacy.md` is a genuine asset here.** Zero permissions plus a real no-collection policy
  is a strong story on F-Droid, where users care about exactly that. Consider linking it from
  the full description once the app is listed.
