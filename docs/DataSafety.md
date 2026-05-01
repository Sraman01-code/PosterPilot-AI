# PosterPilot AI — Play Console Data Safety mapping (v0.1)

> Use this document when filling in the **Data safety** section of the Play
> Console listing. Each row maps directly to the choices Google asks for. This
> reflects the v0.1 internal-testing build; revisit before promoting to closed
> testing or production.

## Top-level answers

| Play question | Answer for v0.1 |
|---|---|
| Does your app collect or share any of the required user data types? | **Yes** — Personal info (name, phone), App info & performance (analytics events to Logcat only), Messages (poster copy you generate). |
| Is all of the user data collected by your app encrypted in transit? | **Yes** for the optional AI backend call (HTTPS via OkHttp). No other data leaves the device. |
| Do you provide a way for users to request that their data be deleted? | **Yes** — uninstall or *Settings → Apps → PosterPilot AI → Clear data*. There is no server-side store to delete from. |
| Are you committed to following Google Play's Families Policy? | **Not applicable** (app is not in the Designed for Families program). |

## Data type collection / sharing matrix

For each row Google asks: *Collected?*, *Shared with third parties?*,
*Processed ephemerally?*, *Required or optional?*, *Why?*

### Personal info

| Sub-type | Collected | Shared | Ephemeral | Optional? | Purpose |
|---|---|---|---|---|---|
| **Name** (business name) | Yes | No | No | Required | App functionality — printed onto the poster. |
| **Phone number** | Yes | No | No | Optional | App functionality — printed onto the poster only when the user types it in. |
| Email address | No | — | — | — | — |
| User IDs | No | — | — | — | — |
| Address | No | — | — | — | — |
| Race / ethnicity, sexual orientation, political views, religion | No | — | — | — | — |
| Other personal info | No | — | — | — | — |

### Financial info

| Sub-type | Collected | Shared |
|---|---|---|
| All financial sub-types (payment info, purchase history, credit score, financial info) | **No** | — |

### Health and fitness

| Sub-type | Collected | Shared |
|---|---|---|
| All sub-types | **No** | — |

### Messages

| Sub-type | Collected | Shared | Ephemeral | Optional? | Purpose |
|---|---|---|---|---|---|
| **In-app messages** (the poster headline / caption / CTA the user types or generates) | Yes — stored locally only | No | No | Required to use the editor | App functionality — these are the strings rendered onto the poster. |
| Emails / SMS / MMS | No | — | — | — | — |

### Photos and videos

| Sub-type | Collected | Shared |
|---|---|---|
| **Photos** | **No collection or upload** — the app *generates* PNG files on-device and hands them to the OS share sheet. | **No** — sharing happens through the user's explicit Android share intent. |
| Videos | No (no video export) | — |

### Audio files / Files and docs / Calendar / Contacts / Location / Web browsing / Health / Device or other IDs

| Sub-type | Collected | Shared |
|---|---|---|
| All sub-types in these categories | **No** | — |

### App activity

| Sub-type | Collected | Shared | Ephemeral | Optional? | Purpose |
|---|---|---|---|---|---|
| **App interactions** (screens viewed, AI generations requested, exports performed) | Yes — written to **device-local Logcat only** for v0.1 | No | Logcat is rotated by the OS | Cannot disable in v0.1 (no remote SDK collects it; nothing leaves the device) | Analytics — local debugging only. |
| In-app search history, installed apps, other user-generated content (other than poster copy already covered above), other actions | No | — | — | — | — |

### Web browsing

| Sub-type | Collected | Shared |
|---|---|---|
| Web browsing history | **No** | — |

### App info and performance

| Sub-type | Collected | Shared |
|---|---|---|
| Crash logs, diagnostics, other performance data | **No** (no Firebase Crashlytics or similar SDK in v0.1) | — |

### Device or other IDs

| Sub-type | Collected | Shared |
|---|---|---|
| Device or other IDs (advertising ID, IMEI, SSAID, etc.) | **No** | — |

## Optional AI backend disclosure

When the app is built **with** a non-empty `AI_BASE_URL` Gradle property and the
user taps **Generate AI copy**, the app sends the business name, business
category, language, and poster category to the configured backend over HTTPS.
This must be reflected in the *Personal info → Name* row above as **shared
with third parties** for the production listing if a public backend is used.
For v0.1 internal testing, internal testers run with an **empty `AI_BASE_URL`**
so no data leaves the device.

## Security practices

- **In transit:** the only outgoing call (optional AI) uses HTTPS via OkHttp
  defaults.
- **At rest:** business profile and history live in app-private storage
  (DataStore + Room). Android sandboxes app-private storage between apps.
- **Backups:** disabled (`android:allowBackup="false"`, Auto Backup and Device
  Transfer both excluded). See `app/src/main/res/xml/data_extraction_rules.xml`.
- **Account deletion:** there is no account. *Clear data* in Settings deletes
  everything immediately.

## Review checklist before submitting the Data Safety form

- [ ] Confirm `AI_BASE_URL` is empty for the v0.1 internal-testing build OR
      update the *shared with third parties* answers above accordingly.
- [ ] Confirm no analytics SDK has been added since this document was written.
      `grep -ri "firebase" app/src` should return zero results.
- [ ] Confirm `INTERNET` is the only declared permission in
      `app/src/main/AndroidManifest.xml`.
- [ ] Confirm Privacy Policy URL is live and matches `docs/PrivacyPolicy.md`.
