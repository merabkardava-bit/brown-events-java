# BEVJ-113 Register Now Button for Inactive Conferences — Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Close registration for COMPLETED and CANCELLED conferences — hide the Register Now button, reject the API call with 409, and surface that rejection to the user.

**Architecture:** One case-insensitive **denylist** (`completed`, `cancelled`) applied twice: a new shared frontend helper consumed by both register affordances, and a guard at the top of `RegistrationService.registerAttendee` throwing a new exception mapped to 409 by the existing `GlobalExceptionHandler`.

**Tech Stack:** Java 11 / Spring Boot 2.7.14, JUnit 5 + Mockito; React 18 + Vite (plain `.jsx`, no test runner).

**Spec:** `docs/superpowers/tasks/2026-09-13-bevj-113-register-now-inactive-conferences/spec.md`

## Global Constraints

- Denylist, never an allowlist: closed only when the normalised status is `completed` or `cancelled`. Every other value — `UPCOMING`, `ACTIVE`, `ONGOING`, `null`, unrecognised — is open, matching the `getStatusBadgeClass` null precedent (`ConferenceDetailPage.jsx:152-161`).
- Always normalise case before comparing. Raw lowercase equality is the original defect (FE-003).
- Rejection is **409 Conflict**, body `{ "status": 409, "error": "Conflict", "message": "..." }` via the existing immutable `ErrorResponse`. 404 still precedes 409, so the conference lookup runs before the status check.
- Do not settle `ACTIVE` vs `ONGOING`; both keep the button.
- `GlobalExceptionHandler` and `ErrorResponse` are in package `com.brownevents.app` (root), **not** `.exception`.
- Commit per task using the repository's existing convention.

---

### Task 1: Reject closed-conference registrations with 409 and surface it in the client

**Files:**
- Create: `backend/src/main/java/com/brownevents/app/exception/RegistrationClosedException.java`
- Modify: `RegistrationService.java:31-41`; `GlobalExceptionHandler.java:28-32` (new handler after `handleMismatch`); `RegistrationController.java:74-79` (`@ApiResponses`); `frontend/src/api.js:60-68`
- Test: `backend/src/test/java/com/brownevents/app/service/RegistrationServiceTest.java`

**Interfaces:** produces `RegistrationClosedException(String)`; `registerAttendee(Long, Attendee)` signature unchanged; `api.js` `registerAttendee` now rejects with `Error(message)` on non-2xx.

**Test-first: yes** — five new `RegistrationServiceTest` methods fail because `registerAttendee` today returns a saved `Registration` (201) for COMPLETED/CANCELLED conferences instead of throwing.

The `api.js` fix ships in this same commit deliberately: without it a 409 body resolves as success and renders "Registration Successful!" (AC10). Both call sites already surface `err.message` (`ConferenceDetailPage.jsx:138-149`, `RegistrationPage.jsx:47-55`), so no call-site changes.

- [ ] **Step 1: Create the exception, then write the failing tests**

```java
package com.brownevents.app.exception;

public class RegistrationClosedException extends RuntimeException {
    public RegistrationClosedException(String message) {
        super(message);
    }
}
```

Add to the existing `RegistrationServiceTest` (`@ExtendWith(MockitoExtension.class)`, `@Mock` repositories, `@InjectMocks`): `registerAttendee_completedConference_shouldThrowRegistrationClosedException`, `..._cancelledConference_...`, `..._lowercaseCompletedStatus_...`, `registerAttendee_nullStatus_shouldSaveRegistration`, `registerAttendee_activeConference_shouldSaveRegistration`.

Rejection tests stub `conferenceRepository.findById(1L)` with a `Conference` carrying the status under test, then `assertThrows(RegistrationClosedException.class, () -> service.registerAttendee(1L, attendee))` plus `verify(registrationRepository, never()).save(any())` **and** `verify(attendeeRepository, never()).save(any())`. Open-path tests stub `attendeeRepository.findByEmail` to return the attendee and assert `verify(registrationRepository, times(1)).save(any())`.

- [ ] **Step 2: Run them and confirm failure** — from `backend/`: `mvn test -Dtest=RegistrationServiceTest`. Expected: the three rejection tests FAIL (no exception). The `attendeeRepository` `never()` assertion also fails today, because the upsert at `:32-33` runs *before* the conference lookup.

- [ ] **Step 3: Add the guard, hoisting the lookup above the attendee upsert.** Move the existing `conferenceRepository.findById(...).orElseThrow(...)` line (`:34`) above the upsert (`:32-33`) so a rejection writes nothing, then insert after it:

```java
private static final List<String> CLOSED_STATUSES = Arrays.asList("COMPLETED", "CANCELLED");
```
```java
if (conference.getStatus() != null
        && CLOSED_STATUSES.contains(conference.getStatus().trim().toUpperCase())) {
    throw new RegistrationClosedException(
            "Registration is closed for this conference (status: " + conference.getStatus() + ")");
}
```

- [ ] **Step 4: Map it to 409 and document it.** In `GlobalExceptionHandler`:

```java
@ExceptionHandler(RegistrationClosedException.class)
public ResponseEntity<ErrorResponse> handleRegistrationClosed(RegistrationClosedException ex) {
    return ResponseEntity.status(HttpStatus.CONFLICT)
            .body(new ErrorResponse(409, "Conflict", ex.getMessage()));
}
```

Add `@ApiResponse(responseCode = "409", description = "Registration is closed for this conference")` beside the existing 201/404 entries on the register endpoint.

- [ ] **Step 5: Honour `response.ok` in `api.js`.** Replace the bare `return response.json()` (`:67`) with:

```js
const body = await response.json().catch(() => ({}))
if (!response.ok) {
  throw new Error(body.message || `Registration failed (${response.status})`)
}
return body
```

- [ ] **Step 6: Re-run `mvn test -Dtest=RegistrationServiceTest`** — expected: all PASS, including the three pre-existing tests.

---

### Task 2: Add the shared registration-eligibility helper

**Files:** Create `frontend/src/registrationEligibility.js`

**Interfaces:** produces `isRegistrationOpen(status): boolean` and `registrationClosedNotice(status): string`; Tasks 3 and 4 import both from `'../registrationEligibility'`.

**Path rationale:** a flat module beside `frontend/src/api.js` — `src/` keeps shared modules at its root and reserves subdirectories for `components/`/`pages/`, so this avoids inventing a `utils/` convention the repo does not have.

**Test-first: no** — the frontend has no test runner, assertion library, or `test` script, and adding one is an explicit spec non-goal.

- [ ] **Step 1: Create the module**

```js
const CLOSED_STATUSES = ['completed', 'cancelled']

function normalize(status) {
  return status ? String(status).trim().toLowerCase() : ''
}

export function isRegistrationOpen(status) {
  const s = normalize(status)
  return s === '' || !CLOSED_STATUSES.includes(s)
}

export function registrationClosedNotice(status) {
  return normalize(status) === 'cancelled'
    ? 'This conference has been cancelled. Registration is closed.'
    : 'This conference has ended. Registration is closed.'
}
```

- [ ] **Step 2: Build** — from `frontend/`: `npm run build`. Expected: success.

---

### Task 3: Hide the Register Now button and repair the notice on the detail page

**Files:** Modify `frontend/src/pages/ConferenceDetailPage.jsx:369-375` (button), `:377-381` (notice), imports `:1-5`

**Interfaces:** consumes both helpers from Task 2.

**Test-first: no** — no frontend test infrastructure exists and adding one is a non-goal; verified by the build, with DOM/visual checks owned by the pipeline's verification stage.

- [ ] **Step 1:** Import the helpers from `'../registrationEligibility'`.

- [ ] **Step 2: Remove the button from the DOM when closed.** Wrap the button block (`:369-375`) in `{isRegistrationOpen(conference?.status) && ( ... )}` and delete its dead `disabled={conference?.status === 'cancelled' || ...}` prop (`:372`) — the helper owns the rule now, and the DoD asks for "not visible", not disabled. Leave the flex row (`:365-376`, `justifyContent: 'space-between'`) as-is; the `<h2>Registrations</h2>` heading reflowing alone is expected.

- [ ] **Step 3: Repair the notice for both closed states.** Change the condition at `:377` to `!isRegistrationOpen(conference?.status)` and render `{registrationClosedNotice(conference?.status)}` instead of the hardcoded sentence, keeping the inline style object at `:378`. This is the affordance replacing the button.

- [ ] **Step 4: Build** — from `frontend/`: `npm run build`. Expected: success, import resolves.

---

### Task 4: Gate the standalone `/conferences/:id/register` submit button

**Files:** Modify `frontend/src/pages/RegistrationPage.jsx:195-201`, imports `:1-3`

**Interfaces:** consumes both helpers from Task 2; uses the existing `conference` state (`:9`, populated `:26-28`).

**Test-first: no** — same reason as Task 3.

- [ ] **Step 1:** Import the helpers from `'../registrationEligibility'`.

- [ ] **Step 2: Block submission.** Change `disabled={submitting}` (`:198`) to `disabled={submitting || !isRegistrationOpen(conference?.status)}`. This route is unlinked (FE-008) but URL-reachable; the button stays rendered and disabled here — "not visible" applies only to the detail page's Register Now button.

- [ ] **Step 3: Explain why.** Above the button row (`:194`) render `{!isRegistrationOpen(conference?.status) && (<p className="form-hint">{registrationClosedNotice(conference?.status)}</p>)}` so the disabled state is not silent.

- [ ] **Step 4: Build** — from `frontend/`: `npm run build`. Expected: success.

---

### Task 5: Seed a CANCELLED conference fixture

**Files:** Modify `backend/src/main/java/com/brownevents/app/DataInitializer.java:109-116` (add a fourth conference after `cloudDevOps`)

**Test-first: no** — `DataInitializer` has no test and no seed-data test precedent in the repo; verified by compile and boot.

**Seeding constraint (accepted risk — do not solve here):** `DataInitializer.run` seeds only when `conferenceRepository.count() == 0` (`:36-40`), so this fixture appears **only on a fresh database**. Existing volumes keep three conferences and need a DB recreate or a `PUT` to `CANCELLED`. The spec records this as an open risk; add no migration or re-seed logic.

- [ ] **Step 1: Add the fixture** following the exact shape of `cloudDevOps` (`:109-116`) — `new Conference()`, `setTitle`/`setDescription`/`setLocation`/`setStartDate`/`setEndDate` with `LocalDate.of(...)`, `setStatus("CANCELLED")`, `conferenceRepository.save(...)`. Suggested: "Frontend Masters Summit", Seattle WA, 2024-11-05 to 2024-11-07. Leave the three existing conferences, speakers, rooms, and sessions untouched; add no sessions for it. A fourth conference shifts list pagination counts on a fresh DB.

- [ ] **Step 2: Compile** — from `backend/`: `mvn compile`. Expected: success.

---

## Self-Review

**Spec coverage:** eligibility rule → T2; frontend scope → T3 (button + notice), T4 (`RegistrationPage`), T1/Step 5 (`api.js`); backend scope → T1 (exception, guard, 409, `@ApiResponses`, 404-before-409); seed data → T5; automatable criteria 7-9 → T1/Step 1. Criteria 1-6, 10, 11 are manual or suite-level and belong to the pipeline's verification stage, not to plan tasks.

**Negative-constraint pass** (Non-goals plus every "not/without" clause):
- No enum conversion (TD-011) — `Conference.java` untouched; `status` stays a `String`, compared via `toUpperCase()`.
- `ConferenceListPage` dropdowns (FE-009) and `.badge--ongoing` CSS — no task lists `ConferenceListPage.jsx` or `index.css`.
- `response.ok` scoped to `registerAttendee` only (FE-002) — T1/Step 5 names `api.js:60-68`; the other seven functions are untouched.
- Modal field set — T3 touches only `:369-381`, not the modal at `:484-637`.
- No frontend test infrastructure — T2-T5 are `Test-first: no` with the reason; no devDependency, config file, or `test` script is added.
- `ACTIVE` vs `ONGOING` undecided — the denylist names neither; both stay open.
- No `@Transactional` (TD-009) — T1 hoists one existing line and adds a guard, no annotation.
- Route not linked into navigation (FE-008) — T4 only gates the existing button.
- "not visible" honoured, not merely disabled — T3/Step 2 removes the button from the DOM and deletes the `disabled` prop.
