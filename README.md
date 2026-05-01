# PosterPilot AI

PosterPilot AI is an Android app by OpenGraph Labs for helping India-first small businesses create poster and status creatives quickly. Users set up a business profile once, choose a local template, edit poster copy, export a PNG, and share it from the Android share sheet.

The repository is intentionally focused on the functional MVP. Premium visual polish, advanced editing, payments, login, and cloud features are out of scope for the current build.

## Current MVP Features

- Kotlin Android app built with Jetpack Compose.
- DataStore-backed onboarding and business profile persistence.
- Business setup for language, business name, category, phone, and brand color.
- Local JSON poster templates loaded from `app/src/main/assets/templates`.
- Home feed grouped by Festival, Sale/Offer, New Arrival, and Thank You.
- Compose template preview renderer for square `1:1` and story `9:16` formats.
- Simple in-memory editor for headline, caption, CTA, theme color, and logo placeholder controls.
- PNG export at fixed output sizes: `1080x1080` for square and `1080x1920` for story.
- Android share sheet support using `FileProvider`.
- Room-backed local export history with thumbnail display and re-share support.
- Retrofit-backed AI copy integration for `POST /ai/copy` with mock fallback when no backend URL is configured.
- Basic Logcat analytics tracking through an `AnalyticsTracker` interface.

## Tech Stack

- Language: Kotlin
- UI: Jetpack Compose and Material 3
- Navigation: Navigation Compose
- Local preferences: AndroidX Preferences DataStore
- Local database: Room
- Networking: Retrofit, OkHttp, Gson converter
- Export/share: Android `Bitmap`, app cache files, and `FileProvider`
- Build: Gradle Kotlin DSL
- Min SDK: 26
- Target SDK: 35

## Project Structure

```text
app/src/main/java/com/opengraphlabs/posterpilot/
  MainActivity.kt
  PosterPilotApp.kt
  core/
    analytics/
    export/
    model/
    renderer/
    share/
    theme/
    ui/
  data/
    local/
    remote/
    templates/
  feature/
    editor/
    history/
    home/
    onboarding/
    templates/
  navigation/
```

## Backend Contract

AI copy generation uses:

```http
POST /ai/copy
Content-Type: application/json
```

```json
{
  "language": "en",
  "posterCategory": "FESTIVAL",
  "businessName": "BizReach",
  "businessCategory": "Restaurant",
  "occasion": "Diwali",
  "offerText": "",
  "tone": "promotional"
}
```

```json
{
  "headline": "Celebrate Diwali with Special Offers",
  "caption": "Make this festive season memorable with fresh deals from BizReach.",
  "cta": "Order Now"
}
```

Set `AI_BASE_URL` through Gradle configuration when a backend is available. If the value is blank, the app uses safe mock copy so the editor flow remains usable.

## Build

From the repository root:

```powershell
.\gradlew.bat :app:assembleDebug
```

Debug APK output:

```text
app/build/outputs/apk/debug/app-debug.apk
```

On macOS/Linux:

```bash
./gradlew :app:assembleDebug
```

## Run

Open the project in Android Studio and run the `app` configuration on an emulator or Android device.

The first launch shows onboarding. After the business profile is completed, subsequent launches go directly to Home. Exported PNGs and history remain local to the device.

## Analytics

Analytics is intentionally local-first for the MVP. `LogcatAnalyticsTracker` logs events with the `PosterPilotAnalytics` tag. The interface is ready for a later Firebase or backend implementation without changing feature screens.

Tracked MVP events include onboarding completion, home/template/editor/history views, AI copy request outcomes, export outcomes, and share sheet opens.

## Deferred Work

The current MVP does not include:

- Logo upload or background removal.
- Payments, login, team accounts, or cloud sync.
- Video export.
- Template marketplace.
- Full Canva-style freeform editor.
- Premium UI redesign.

Known layout and visual polish issues are documented in `docs/KnownLayoutIssues.md` and should be addressed after Phase 9, once the functional MVP is stable.

## Release Readiness

PosterPilot AI v0.1 is targeted at the **Google Play internal testing** track.
For the full status report — what is in place, what is still missing before
closed / production releases, and the Play Console answers to use — read
`docs/ReleaseReadiness.md`.

The supporting compliance documents:

- `docs/PrivacyPolicy.md` — local-first privacy policy draft (must be
  legal-reviewed and hosted at a public URL before submission).
- `docs/DataSafety.md` — pre-filled Play Console *Data safety* form mapping.
- `docs/AiGeneratedContentPolicy.md` — generative-AI safeguards and the
  in-editor *Report* affordance behaviour.

### Verification commands

Run all five before each release upload:

```powershell
.\gradlew.bat clean
.\gradlew.bat :app:assembleDebug
.\gradlew.bat :app:assembleRelease
.\gradlew.bat :app:bundleRelease
.\gradlew.bat :app:lintRelease
```

The release AAB is produced at:

```text
app/build/outputs/bundle/release/app-release.aab
```

### Release signing

Signing material is **never** committed to the repository. The release
`signingConfig` reads four values, in order of preference, from
`gradle.properties` then environment variables:

| Property / env var | Description |
|---|---|
| `POSTER_PILOT_KEYSTORE_PATH` | Absolute path to the upload keystore (`*.jks`). |
| `POSTER_PILOT_KEYSTORE_PASSWORD` | Keystore password. |
| `POSTER_PILOT_KEY_ALIAS` | Key alias inside the keystore. |
| `POSTER_PILOT_KEY_PASSWORD` | Password for that key. |

If any of the four is missing, the release AAB / APK is built **unsigned**;
the build itself still succeeds so local development is not blocked. The full
upload-keystore generation and Play upload flow is documented in
`docs/ReleaseReadiness.md`.

### Internal testing release checklist

- [ ] Privacy policy hosted at a public URL and pasted into the Play listing.
- [ ] Data Safety form completed using `docs/DataSafety.md`.
- [ ] AI content policy disclosed in the listing's GenAI section.
- [ ] Upload keystore configured locally (see above) before
      `:app:bundleRelease`.
- [ ] `versionCode` bumped in `app/build.gradle.kts` for each iteration.
- [ ] `app-release.aab` uploaded via Play Console → *Internal testing* →
      *Create new release*.
- [ ] Release notes written for the internal testers.
- [ ] Tester emails added to the closed list.

## Brand

Product: PosterPilot AI  
Company: OpenGraph Labs  
Audience: India-first small businesses and local merchants
