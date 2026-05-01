# PosterPilot AI — Privacy Policy (Draft v0.1)

> **Status:** Draft for internal testing only. Before any closed or production
> release on Google Play, this draft must be reviewed by a qualified
> data-protection lawyer for the operator's jurisdiction (India: DPDP Act 2023;
> EU users: GDPR; UK users: UK GDPR; US California users: CCPA/CPRA), hosted at
> a stable public URL, and that URL pasted into the Play Console listing.

**App:** PosterPilot AI
**Publisher:** OpenGraph Labs
**Contact:** privacy@opengraphlabs.example *(replace with real address before
publishing)*
**Effective date:** _to be filled before public release_

---

## 1. What this app is

PosterPilot AI is an Android app that lets small-business owners create poster
and "status creative" PNG images from local templates. The app's MVP is
deliberately scoped to local-first behaviour: there is no user account, no
cloud sync, no payments, and no advertising SDKs.

## 2. Data we collect on the device

The app stores the following information **only on the user's device**:

| Item | Where it is stored | Purpose |
|------|-------------------|---------|
| Business profile (business name, category, language, phone number, brand colour) | AndroidX Preferences DataStore (app-private storage) | Render the poster with the user's business details. |
| Onboarding completion flag | AndroidX Preferences DataStore | Skip onboarding on subsequent launches. |
| Poster export history (template id, headline, caption, CTA, thumbnail file path, timestamp) | Room SQLite database (app-private storage) | Show the export history list and re-share previously exported posters. |
| Exported PNG files | App cache directory (app-private storage) | Provide the file to the Android share sheet via FileProvider. |

The app does **not** read the user's contacts, photos, location, microphone,
camera, calendar, SMS, call logs, or device identifiers. The only Android
permission requested is `INTERNET`.

## 3. Data sent off the device

When the user taps **Generate AI copy** in the editor, and only when an AI
backend URL has been configured at build time, the app sends the following
JSON body to the configured backend (`POST /ai/copy`):

```json
{
  "language": "en | hi",
  "posterCategory": "FESTIVAL | SALE_OFFER | NEW_ARRIVAL | THANK_YOU",
  "businessName": "<from profile>",
  "businessCategory": "<from profile>",
  "occasion": "",
  "offerText": "",
  "tone": "promotional"
}
```

The response (`headline`, `caption`, `cta`) is shown in the editor and stored
locally only when the user taps **Export PNG**. If no backend URL is configured
at build time, the app uses on-device sample copy and **no network request is
made**.

The app does not transmit the device IMEI, advertising ID, contacts, location,
or any other personal identifier. The HTTP request includes only the standard
OkHttp `User-Agent` header.

## 4. Sharing posters

When the user taps **Share** or **Export PNG**, the resulting image is handed
to the Android share sheet via a `FileProvider` URI. After that point, the
destination app (WhatsApp, Instagram, Gmail, etc.) is responsible for the
image — its handling is governed by that app's own privacy policy, which is
outside our control.

## 5. Backups and device transfer

`android:allowBackup` is set to **false**, and explicit `<exclude>` rules are
declared in `data_extraction_rules.xml` and `backup_rules.xml`. As a result:

- The business profile and export history are **not** uploaded to Google Drive
  Auto Backup.
- The data **is not** copied during Android device-to-device transfer.
- Uninstalling the app deletes everything; there is no recovery path.

## 6. Children

PosterPilot AI is for business operators and is not directed at children under
13. The app does not knowingly collect data from children.

## 7. AI-generated content

AI-generated marketing copy can be inaccurate, off-tone, or otherwise
unsuitable for the user's brand. The user reviews and may edit every line
before exporting. An in-app **Report** affordance lets users flag a problematic
suggestion; reports are recorded only in the local analytics log for v0.1 and
never include the user's phone number or business name beyond a short copy
snippet. See `docs/AiGeneratedContentPolicy.md` for the full policy.

## 8. Analytics

Analytics events for v0.1 are written **only to Logcat on the user's own
device** through `LogcatAnalyticsTracker`. No analytics data is uploaded to
any server. If a future release wires up a remote analytics backend, this
document and the Play Data Safety form will be updated before that change
ships.

## 9. Your rights

Because the app holds your data only on your device, you can exercise the
following rights at any time without contacting us:

- **Access / portability:** the exported PNG files live in the app's cache
  directory. The headline / caption / CTA fields are visible in the editor and
  history list.
- **Deletion:** uninstalling the app, or clearing app storage from
  *Settings → Apps → PosterPilot AI → Storage → Clear data*, removes the
  business profile, history, and exported PNGs immediately.
- **Correction:** edit the business profile from the onboarding flow at any
  time.

For EU/UK users: because no personal data leaves the device for v0.1, there is
no controller-to-processor transfer to disclose. For Indian users under DPDP
Act 2023: the publisher acts as the *Data Fiduciary* for any data passed to
the optional AI backend; that backend's operator is the *Data Processor*.

## 10. Changes to this policy

If this policy materially changes, the updated version will be published at
the same URL, and the in-app version metadata will be bumped before the next
Play release.

## 11. Contact

Questions, complaints, or rights requests:
**privacy@opengraphlabs.example** *(replace with real, monitored address before
publishing)*.
