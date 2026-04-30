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

## Brand

Product: PosterPilot AI  
Company: OpenGraph Labs  
Audience: India-first small businesses and local merchants
