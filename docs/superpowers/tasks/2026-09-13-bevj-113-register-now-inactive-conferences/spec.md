# BEVJ-113 — Register Now Button Shown for Inactive Conferences

**Date**: 2026-09-13 · **Complexity**: S (13/36, ~1 day) · **Status**: approved design

## Problem

The conference detail page offers "Register Now" for every conference, including completed and
cancelled ones. Users click it and either see an error or register for something no longer active.

**The root cause is a case mismatch, not a missing guard.** A guard already exists at
`frontend/src/pages/ConferenceDetailPage.jsx:369-375`: it compares `conference?.status` against the
lowercase literals `'cancelled'` and `'completed'`. The API emits uppercase (`DataInitializer.java:97/106/115`
seeds `UPCOMING`, `ACTIVE`, `COMPLETED`), so both comparisons are permanently false and the guard is
dead code. The sibling cancelled-notice at `:377-381` is dead for the identical reason. Already
catalogued as tech-debt FE-003 (High) — note the audit's cited lines 341/346 are stale.

The fix is therefore to **normalise before comparing**, reusing the idiom the codebase already gets
right in two byte-identical copies of `getStatusBadgeClass` (`ConferenceDetailPage.jsx:152-161`,
`ConferenceCard.jsx:4-13`), both of which work precisely *because* they call `.toLowerCase()` first.

## Eligibility rule

A **denylist**: registration is closed when the normalised status is `completed` or `cancelled`;
it is open for every other value — `UPCOMING`, `ACTIVE`, `ONGOING`, `null`, and anything added later.

**Rationale for the denylist (deliberate, and the point of the choice):** the repo does not agree with
itself about which value means "in progress". `ACTIVE` is seeded but undocumented; `ONGOING` is
documented at `Conference.java:35-37` but never seeded; the two `ConferenceListPage` dropdowns
(`:210-216`, `:252-257`) each offer one and omit the other. A denylist makes that ruling *unnecessary* —
both values, and any future one, keep the button. **This ticket does not settle ACTIVE vs ONGOING.**
Treating null/unrecognised as open also matches the only existing precedent, where
`getStatusBadgeClass` maps a null status to `upcoming`.

## Scope

**Frontend.** Extract one shared eligibility helper into a new module (no `utils/` or `constants.js`
exists today) and use it in both places, so the rule is written once:

- `ConferenceDetailPage.jsx:369-375` — **remove the button from the DOM** when registration is closed,
  rather than disabling it, per the DoD's "not visible".
- `ConferenceDetailPage.jsx:377-381` — repair the dead notice so it fires for **both** completed and
  cancelled and explains that registration is closed, replacing the button in that row.
- `RegistrationPage.jsx:195-201` — apply the same rule to the standalone `/conferences/:id/register`
  submit button, which today is gated only on `submitting`. The route is unlinked from navigation
  (FE-008) but directly URL-reachable.
- `api.js:60-68` — `registerAttendee` must check `response.ok` and throw an `Error` carrying the
  server's `message`, so a rejection is not rendered as "Registration Successful!". Both call sites
  already catch and surface `err.message` (`ConferenceDetailPage.jsx:138-149`, `RegistrationPage.jsx:47-55`),
  so no call-site changes are required.

**Backend (deliberately beyond the literal DoD).** Hiding a button is not access control: the
standalone route and any direct API call bypass the UI entirely, and `RegistrationService.registerAttendee`
(`:31-41`) never reads `conference.getStatus()` — a registration against a COMPLETED or CANCELLED
conference succeeds with 201 today. The guard must reject with **409 Conflict**, using the same
case-insensitive denylist semantics as the frontend so the two cannot drift.

Follow the BEVJ-103 error conventions rather than inventing a shape: a small `RuntimeException` in
`com.brownevents.app.exception` alongside `ResourceNotFoundException`, mapped by one `@ExceptionHandler`
in `GlobalExceptionHandler` returning the existing immutable `ErrorResponse(int status, String error,
String message)`. The new response body, which does not exist yet:

```json
{ "status": 409, "error": "Conflict", "message": "<why registration is closed>" }
```

Order matters: the conference must still 404 before it can 409. `RegistrationController.java:74-79`
documents only 201 and 404, so its `@ApiResponses` gains 409.

**Seed data.** Add a CANCELLED conference to `DataInitializer`, keeping all existing seeded data
intact, so the cancelled criterion has a fixture (only `UPCOMING`, `ACTIVE`, `COMPLETED` exist today).

## Acceptance criteria

Verification method is stated per criterion. The frontend has no test runner, so **only the backend
slice is automatable** — see Open risks.

1. Register Now is absent from the DOM on a COMPLETED conference — *manual browser check*
2. Register Now is absent from the DOM on a CANCELLED conference — *manual browser check*
3. A notice explaining registration is closed appears in the button's place for both — *manual*
4. Register Now renders and opens the modal for UPCOMING and ACTIVE — *manual browser check*
5. A conference whose status is lowercase or null behaves as open — *manual, via a created record*
6. The standalone `/conferences/:id/register` submit is blocked for both closed states — *manual*
7. `registerAttendee` rejects a COMPLETED conference with 409 and saves nothing — *automated,
   `RegistrationServiceTest`*
8. `registerAttendee` rejects a CANCELLED conference with 409 and saves nothing — *automated*
9. `registerAttendee` still succeeds for UPCOMING/ACTIVE/null status — *automated*
10. A 409 surfaces to the user as an error, never as success — *manual browser check*
11. Existing registration flow for active conferences is unchanged end to end — *manual, plus
    the existing `mvn test` suite stays green*

New tests follow the established style: `@ExtendWith(MockitoExtension.class)`, `@Mock` repositories,
`@InjectMocks`, `<method>_<condition>_should<Outcome>` naming, `assertThrows` followed by
`verify(registrationRepository, never())`. `registerAttendee` has zero coverage today, so these are
net-new. **Test-first applies to the backend slice only**: criteria 7-9 are the failing-test-first task.

## Non-goals

- Converting `Conference.status` to a Java enum (tech-debt TD-011)
- Reconciling the two disagreeing `ConferenceListPage` status dropdowns (FE-009)
- Adding a `.badge--ongoing` CSS class
- Blanket `response.ok` checks across all of `api.js` (FE-002) — only `registerAttendee` is in scope
- Reworking the registration modal's field set
- Introducing frontend test infrastructure — Vitest, React Testing Library, E2E (separate ticket)
- Deciding whether `ACTIVE` or `ONGOING` is the canonical in-progress value
- Adding `@Transactional` to `registerAttendee` (TD-009)
- Linking the unlinked `/conferences/:id/register` route into navigation (FE-008)

## Open risks

- **The seeded CANCELLED fixture will not appear on existing databases.** `DataInitializer.run` seeds
  only when `conferenceRepository.count() == 0` (`:36-40`), so anyone with an existing volume keeps
  three conferences and must recreate the database, or create a cancelled conference via `PUT`, to
  verify criterion 2. Surfaced by settled decision 8; not decided here.
- **Seven of eleven criteria are manual-only.** No automated test can assert button visibility, so
  regressions in the frontend rule are undetectable by CI. The shared helper limits the blast radius
  but does not substitute for a test.
- **The denylist is duplicated across languages.** Java and JavaScript cannot share the constant, so
  "they cannot drift" is enforced by this spec and by the backend tests, not by the code itself.
- **Removing the button shifts layout.** The row at `ConferenceDetailPage.jsx:365-376` is
  `justifyContent: 'space-between'` beside `<h2>Registrations</h2>`; with the button gone the heading
  reflows. Requires a visual check, not just a DOM assertion.
- **`status` remains an unvalidated nullable String.** `createConference` (`ConferenceService.java:38-40`)
  does not uppercase while `updateConference` (`:49-51`) does, so lowercase rows stay creatable. The
  case-insensitive comparison contains this; the underlying data defect (TD-011) is untouched.
- Adding a fourth seeded conference changes list pagination counts on a fresh database.
