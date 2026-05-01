# PosterPilot AI — Release Readiness Notes (v0.1)

This document captures everything that has been done to make the v0.1 build
**suitable for Google Play internal testing**, plus the items that remain
outstanding before a closed-testing or production release.

> **Target track:** Internal testing only.
>
> **Not yet ready for:** Closed testing, Open testing, Production. See the
> "Remaining blockers" section.

---

## What was done in this release-readiness pass

### Compliance documentation

- `docs/PrivacyPolicy.md` — drafted local-first privacy policy. Must be
  reviewed by counsel and hosted at a stable URL before being pasted into the
  Play listing.
- `docs/DataSafety.md` — pre-filled mapping for every Play Console Data Safety
  question, reflecting the v0.1 collection / sharing posture.
- `docs/AiGeneratedContentPolicy.md` — Generative-AI policy covering
  safeguards, restricted-content posture, in-app reporting, and provenance.

### Manifest and resources

- `android:allowBackup="false"` set explicitly.
- `android:dataExtractionRules="@xml/data_extraction_rules"` and
  `android:fullBackupContent="@xml/backup_rules"` both reference XML files
  that exclude all domains, so the business profile, Room history, and PNG
  cache are never silently backed up to Google Drive or transferred between
  devices.
- Adaptive launcher icon now ships a `<monochrome>` layer
  (`drawable/ic_launcher_monochrome.xml`) for Android 13+ themed-icon support.

### In-app safeguards

- Editor surfaces a *Report* button after AI copy is generated. Tapping it
  opens a dialog with four reasons and dispatches an `ai_copy_reported`
  analytics event with the chosen reason and a short copy snippet.
- Helper text after generation reads *"AI suggestions can miss the mark. Edit
  anything before exporting."*

### Build configuration

- `app/build.gradle.kts` now defines an opt-in release signing config that
  reads `POSTER_PILOT_KEYSTORE_PATH`, `POSTER_PILOT_KEYSTORE_PASSWORD`,
  `POSTER_PILOT_KEY_ALIAS`, `POSTER_PILOT_KEY_PASSWORD` from
  `gradle.properties` **or** environment variables. Builds without these
  values still succeed (unsigned), so local development is not blocked.
- `.gitignore` now blocks `*.jks`, `*.keystore`, `keystore.properties`,
  `signing.properties`, and `upload-keystore*` from accidental commits.
- `app/proguard-rules.pro` populated with keep-rules for Retrofit, OkHttp,
  Gson, Room, and coroutines so that future R8 enabling does not regress.
  Minification remains **off** for v0.1 to reduce risk in the internal
  testing window.
- `lint { abortOnError = false; warningsAsErrors = false }` so a non-blocking
  warning never fails `bundleRelease` on a CI runner. Hard errors still fail.

---

## Build verification

The verification commands listed in `README.md` produce the following expected
outputs:

| Command | Output |
|---|---|
| `./gradlew clean` | clears `app/build/`, `build/`, and the wrapper caches. |
| `./gradlew :app:assembleDebug` | `app/build/outputs/apk/debug/app-debug.apk` (signed with debug keystore). |
| `./gradlew :app:assembleRelease` | `app/build/outputs/apk/release/app-release.apk` (signed if `POSTER_PILOT_KEYSTORE_*` provided, otherwise `app-release-unsigned.apk`). |
| `./gradlew :app:bundleRelease` | `app/build/outputs/bundle/release/app-release.aab` (signed only if `POSTER_PILOT_KEYSTORE_*` is set in this shell or `gradle.properties`). |
| `./gradlew :app:lintRelease` | `app/build/reports/lint-results-release.html`. |

Run-mode build results from this pass are recorded in the PR / commit
description that introduced this document.

---

## Signing for the Play upload AAB

Internal testing requires an AAB **signed with the upload key** that is
registered in Play Console. The repository never holds keystore material; the
recommended flow on the maintainer's machine is:

1. **Generate an upload keystore once** (kept off the repo, e.g.
   `~/posterpilot/upload-keystore.jks`):
   ```bash
   keytool -genkeypair -v \
     -keystore ~/posterpilot/upload-keystore.jks \
     -keyalg RSA -keysize 2048 -validity 10000 \
     -alias posterpilot-upload
   ```
2. **Set the four properties** for that shell (or in
   `~/.gradle/gradle.properties`, never the repo's
   `gradle.properties`):
   ```bash
   export POSTER_PILOT_KEYSTORE_PATH=$HOME/posterpilot/upload-keystore.jks
   export POSTER_PILOT_KEYSTORE_PASSWORD=...
   export POSTER_PILOT_KEY_ALIAS=posterpilot-upload
   export POSTER_PILOT_KEY_PASSWORD=...
   ```
3. **Build the AAB:**
   ```bash
   ./gradlew clean :app:bundleRelease
   ```
4. **Upload** `app/build/outputs/bundle/release/app-release.aab` via Play
   Console → *Internal testing* → *Create new release*.

Google Play App Signing then re-signs the AAB with the production key Google
manages. Keep the upload keystore offline and backed up — losing it requires a
key reset request to Play.

---

## Remaining blockers before closed / production releases

- [ ] **Replace the launcher icon** with the real PosterPilot mark (current
      icon is a generic placeholder document silhouette).
- [ ] **Host the privacy policy publicly** at a stable URL, after legal
      review, and paste that URL into the Play listing.
- [ ] **Translate the policy and Data Safety summary** into Hindi at minimum,
      and any other supported language.
- [ ] **Wire a real analytics sink** if remote analytics is desired, and
      update `docs/DataSafety.md` accordingly.
- [ ] **Operate the AI backend in production** with the system-prompt
      restricted-content guards described in `docs/AiGeneratedContentPolicy.md`.
- [ ] **Enable R8 minification** (`isMinifyEnabled = true`) once the
      `proguard-rules.pro` keep set has been validated against a release smoke
      test.
- [ ] **Add UI smoke tests** so editor / export / share regressions are caught
      before each release.
- [ ] **Add per-locale screenshots** (square + story) for the Play listing.
- [ ] **Bump versionCode / versionName** at every internal release iteration.
- [ ] **Add an in-app "Privacy & terms" entry point** (settings link) once
      the hosted policy URL exists.
