# PosterPilot AI — Generative AI Content Policy (v0.1)

> Required reading before submitting to Play Console. Google's
> *Generative AI app policy* requires every app that produces AI-generated
> output to: (1) prevent restricted content, (2) provide an in-app reporting
> path, and (3) document its safeguards.

## What the AI does in v0.1

The app exposes one AI surface: a **Generate AI copy** button in the editor
that produces three short strings — `headline`, `caption`, and `cta` — which
are then rendered onto a poster template.

The model is **not** invoked from the home feed, the preview screen, history,
or any background context. It cannot generate images, audio, or video. It
cannot generate code. It cannot generate URLs. It cannot generate child sexual
abuse material, terrorist content, or content depicting violent extremism — the
backend prompt scaffolding only asks for marketing copy bound to a four-value
poster category enum (`FESTIVAL | SALE_OFFER | NEW_ARRIVAL | THANK_YOU`).

## Backend

`POST /ai/copy` is operated by the publisher (OpenGraph Labs) when an
`AI_BASE_URL` is configured at build time. The request body sent from the app
is documented in `README.md` and contains only the business profile fields
necessary to write on-brand copy. The response is a strict three-field JSON
object; the app does not render any other field. If the backend returns
malformed JSON or HTTP error, the editor shows an error state and falls back
to the user's existing draft.

When `AI_BASE_URL` is empty (the v0.1 internal-testing default), the app uses
**static, hand-written sample copy** — no model is invoked at all.

## In-app safeguards

1. **Human review before publication.** The user always sees the suggestion in
   the editor and can edit any line before tapping **Export PNG**. There is no
   "auto-publish" path.
2. **Reporting affordance.** After the user taps **Generate AI copy**, a
   *Report* link appears under the AI button. Tapping it opens a dialog with
   four reasons (*Inappropriate or unsafe*, *Inaccurate or misleading*,
   *Off-brand or off-tone*, *Other concern*) and a *Submit report* button.
   Submitting fires an `ai_copy_reported` analytics event with the chosen
   reason and a short snippet of the offending copy.
3. **Editing is required.** The exported PNG always reflects the strings
   present in the editor at the moment of export, not the original AI output —
   so any user-side correction takes effect.
4. **Local fallback.** If the AI backend is unreachable or unconfigured, the
   editor still works with manual or sample copy — there is no flow that
   blocks the user behind an AI call.

## Restricted content posture

PosterPilot AI's prompt scope is constrained to four marketing categories for
small businesses. The publisher commits to:

- Refusing model output that contains hate speech, harassment, sexual content,
  violent or terrorist content, dangerous or illegal activity, child-related
  content, or impersonation of real people, by enforcing these refusals in the
  backend system prompt and post-generation filters before returning the
  response to the app.
- Logging and acting on reports submitted via the in-app reporting flow.
- Disabling the AI route at the backend if a pattern of abuse is observed,
  while leaving the manual editor fully functional.

## Provenance and labelling

- Inside the editor, the AI button is clearly labelled **Generate AI copy** and
  the helper text after generation reads *"AI suggestions can miss the mark.
  Edit anything before exporting."*
- The exported PNG itself does **not** currently embed an AI provenance flag.
  Users own the resulting poster and may share it without disclosure on their
  own channels; if Play, regional law, or platform partners require explicit
  AI-content labelling in a future release, a corner watermark or EXIF tag
  will be added before that release.

## Reporting workflow (for the publisher)

For v0.1 the *report* event is recorded only via Logcat (analytics has no
remote sink yet). When wiring up a remote analytics backend in a later
release, the team must:

1. Route `ai_copy_reported` events to a dedicated dashboard with the reason +
   copy snippet visible.
2. Triage reports daily during the closed-testing phase, weekly thereafter.
3. Re-train or re-prompt the backend model when a category of failure
   accounts for >5% of reports.

## Children and sensitive audiences

The app is targeted at adult business operators. The model is not asked to
produce content addressed to or describing children, minors, or vulnerable
groups. The backend will refuse such requests.

## Updates to this policy

Bump the document version date at the top of this file and link the change
from the next release notes whenever the AI surface, prompt scaffolding, or
reporting flow changes.
