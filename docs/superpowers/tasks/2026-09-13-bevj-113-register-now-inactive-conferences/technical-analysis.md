# Technical Research

**Task**: conference registration status ui
**Generated**: 2026-09-13
**Research path**: filesystem

---

## 1. Original Context

BEVJ-113 — Register Now Button Shown for Inactive Conferences
Role: Developer
The conference detail page shows a "Register Now" button regardless of whether the conference is still accepting registrations. Users on completed or cancelled conferences see the button, click it, and get an error — or register for something that is no longer active.

Definition of Done:
- [ ] Register Now button is not visible on completed conferences
- [ ] Register Now button is not visible on cancelled conferences
- [ ] Button remains visible and functional for upcoming and active conferences
- [ ] No regression in the registration flow for active conferences

---

## 2. Codebase Findings

### Existing Implementations

**The guard already exists and is already broken by a case mismatch.** This is not a greenfield addition; it is a defect in a live conditional.

- `frontend/src/pages/ConferenceDetailPage.jsx:369-375` — the only "Register Now" button in the app. Exact JSX as it stands today:

  ```jsx
  <button
    className="btn btn--primary"
    onClick={handleRegisterClick}
    disabled={conference?.status === 'cancelled' || conference?.status === 'completed'}
  >
    Register Now
  </button>
  ```

  It lives inside the `Registrations` `detail-section`, in a flex row next to the `<h2>Registrations</h2>` heading (`:365-376`).

- `frontend/src/pages/ConferenceDetailPage.jsx:377-381` — a sibling cancelled-notice with the same lowercase comparison, therefore also dead:

  ```jsx
  {conference?.status === 'cancelled' && (
    <p style={{ color: '#6E4A2A', ... }}>
      This conference has been cancelled.
    </p>
  )}
  ```

- `frontend/src/pages/ConferenceDetailPage.jsx:107-120` — `handleRegisterClick()` opens the modal and resets `formData`; no status check inside.
- `frontend/src/pages/ConferenceDetailPage.jsx:133-150` — `handleRegistrationSubmit()` calls `registerAttendee(id, {...formData, sessionId})`; no status check.
- `frontend/src/pages/ConferenceDetailPage.jsx:484-637` — the inline registration modal (`showModal`), collecting firstName, lastName, email, phone, company, dietaryRequirements.

**`Conference.status` exact type and value set** (`backend/src/main/java/com/brownevents/app/entity/Conference.java:35-37`):

```java
@Schema(description = "Current lifecycle status of the conference.", example = "UPCOMING",
        allowableValues = {"UPCOMING", "ONGOING", "COMPLETED", "CANCELLED"})
private String status;
```

- Type: plain `String` — **not** a Java enum. No `@Column(nullable = false)`, no `@Enumerated`, no bean-validation annotation, no default value.
- Documented set (Swagger only, not enforced): `UPCOMING`, `ONGOING`, `COMPLETED`, `CANCELLED` — all uppercase.
- **Actually seeded values** (`backend/src/main/java/com/brownevents/app/DataInitializer.java`):
  - `:97` `springTech.setStatus("UPCOMING")`
  - `:106` `javaDays.setStatus("ACTIVE")` — **`ACTIVE` is not in the documented set**; it is a fourth de-facto value.
  - `:115` `cloudDevOps.setStatus("COMPLETED")`
  - No seeded `CANCELLED` and no seeded `ONGOING` conference exists.
- Frontend-offered values on create (`frontend/src/pages/ConferenceListPage.jsx:252-257`): `UPCOMING`, `ACTIVE`, `COMPLETED`, `CANCELLED` — offers `ACTIVE`, omits `ONGOING`.
- Frontend filter dropdown (`frontend/src/pages/ConferenceListPage.jsx:210-216`): `UPCOMING`, `ONGOING`, `COMPLETED`, `CANCELLED` — the *other* set. The two dropdowns in the same file disagree.
- So the complete set of conference status strings appearing anywhere: **`UPCOMING`, `ONGOING`, `ACTIVE`, `COMPLETED`, `CANCELLED`**, all uppercase at the source. Lowercase forms appear only in the two broken equality checks and inside `.toLowerCase()`-normalised badge switches.

**Backend normalisation is partial:**
- `backend/src/main/java/com/brownevents/app/service/ConferenceService.java:49-51` — `updateConference` uppercases: `if (conf.getStatus() != null) existing.setStatus(conf.getStatus().toUpperCase());`
- `ConferenceService.java:38-40` — `createConference` does **no** normalisation and no validation; any arbitrary string (including lowercase `"completed"`) can be persisted via `POST /api/conferences`.
- `backend/src/main/java/com/brownevents/app/repository/ConferenceRepository.java:20,25` — status filtering is case-insensitive: `(:status IS NULL OR UPPER(c.status) = UPPER(:status))`.

**Backend registration path — no status guard anywhere:**
- `backend/src/main/java/com/brownevents/app/controller/RegistrationController.java:74-79` — `@PostMapping("/{id}/register")` → `registrationService.registerAttendee(id, attendee)`, wrapped in `ApiResponse`, returns 201.
- `backend/src/main/java/com/brownevents/app/service/RegistrationService.java:31-41` — the full method body: upsert attendee by email, `conferenceRepository.findById(conferenceId).orElseThrow(ResourceNotFoundException)`, build `Registration`, `setStatus("CONFIRMED")`, save. **It never reads `conference.getStatus()`.** There is no COMPLETED/CANCELLED rejection, no 409, no `IllegalStateException`. Documented `@ApiResponses` list only 201 and 404.
- Consequence: the DoD's "get an error" is not a backend rejection — a registration against a COMPLETED or CANCELLED conference currently **succeeds with 201**.

### Architecture and Layers Affected

- **Frontend presentation layer** — `frontend/src/pages/ConferenceDetailPage.jsx` (button + notice + modal gating). This is where the DoD lives.
- **Frontend API layer** — `frontend/src/api.js` (`getConference`, `registerAttendee`); envelope unwrapping happens in the pages, not here.
- **Frontend routing** — `frontend/src/App.jsx:14` registers `/conferences/:id/register` → `RegistrationPage`.
- **Frontend components** — `frontend/src/components/ConferenceCard.jsx` (status badge only; no register affordance).
- **Backend controller/service** — `RegistrationController`, `RegistrationService` (currently unguarded; touching them is a design choice, not a DoD requirement).
- **Backend entity** — `Conference.status` as an untyped `String` is the root enabler of the case mismatch.

### Integration Points

- `getConference(id)` → `GET /api/conferences/{id}` → `ConferenceController.getConference` (`ConferenceController.java:104-108`) returns `new ApiResponse<>(conference)`, i.e. **`{ "data": { id, title, description, location, startDate, endDate, status } }`**. `sessions` and `registrations` are `@JsonIgnore`d (`Conference.java:39-45`), so the conference payload carries `status` and nothing nested.
- Envelope handling (post-BEVJ-103): `frontend/src/api.js:16-19` `getConference` returns the raw parsed JSON *including* the envelope; both consumers unwrap defensively themselves — `ConferenceDetailPage.jsx:49` `const conf = data.data || data` and `RegistrationPage.jsx:27` `const conf = data.data || data`. So `conference.status` **is already available** on the detail page today; no new fetch is required.
- List endpoint is a *different* shape: `ConferenceController` list returns a raw `Map` with keys `data`, `page`, `size`, `totalElements`, `totalPages` (`ConferenceController.java:80-84`) — not `ApiResponse`.
- `getConferenceRegistrations` (`api.js:48-52`) unwraps with `res.data ?? res`.
- `registerAttendee` (`api.js:60-68`) → `POST /api/conferences/{id}/register`; returns `response.json()` with **no `response.ok` check**, so a 4xx/5xx body is resolved as success (tech-debt FE-002). A future backend 409 would therefore not surface as a thrown error in the current client.
- External services: none. No HTTP clients, no cloud SDKs, no auth provider in this domain.

### Patterns and Conventions

- **Status-normalising badge helper, duplicated twice** — `ConferenceDetailPage.jsx:152-161` and `ConferenceCard.jsx:4-13` are byte-identical `getStatusBadgeClass(status)` functions:

  ```js
  if (!status) return 'badge badge--upcoming'
  switch (status.toLowerCase()) {
    case 'active': return 'badge badge--active'
    case 'upcoming': return 'badge badge--upcoming'
    case 'cancelled': return 'badge badge--cancelled'
    case 'completed': return 'badge badge--completed'
    default: return 'badge badge--upcoming'
  }
  ```

  These *work* precisely because they call `.toLowerCase()` before comparing. This is the established in-repo idiom for comparing status: **normalise, then compare**. There is no shared constants module, no `utils/` directory, no `frontend/src/constants.js` — helpers are defined inline per file and copy-pasted.
- **CSS class per status** — `frontend/src/index.css:316-350`: `.badge` base plus `.badge--active`, `.badge--upcoming`, `.badge--cancelled`, `.badge--completed`. No `.badge--ongoing` exists, so an `ONGOING` conference falls through to the `default` and renders an "upcoming"-coloured badge.
- **Status rendered in two places on the detail page** — header badge (`:234-236`) and the Details info table (`:284-287`), both `{conference?.status || 'Upcoming'}`.
- Backend conventions: constructor injection, `@Service`/`@RestController`, `orElseThrow(() -> new ResourceNotFoundException(...))`, responses wrapped in `com.brownevents.app.ApiResponse<>`, custom exceptions in `com.brownevents.app.exception` mapped centrally by `GlobalExceptionHandler`.
- Frontend conventions: plain `.jsx`, `useState`/`useEffect`, raw `fetch`, inline `style={{}}` objects mixed with BEM-ish class names, `console.log` debugging left in place.

---

## 3. Documentation Findings

### Guides and Architecture Docs

- `.ai-run/guides/` — **absent**. No such directory in the repo.
- `docs/architecture.md` — documents the schema (`varchar status` on conferences at `:20` and registrations at `:60`), the create-conference request body including `status` (`:113`), and the registration write path `INSERT INTO registrations (status='CONFIRMED', ...)` (`:146`). It does not document a status lifecycle or any registration-eligibility rule.
- `docs/tech-debt-audit.md` — directly on point, see below.
- `docs/devlog.md`, `PRODUCT.md`, `README.md`, `CLAUDE.md` present.
- Prior task artifacts: `docs/superpowers/tasks/2026-09-09-BEVJ-103-standardize-api-and-error-handling`, `.../2026-09-09-BEVJ-201-conference-search-filtering`, `.../2026-09-09-BEVJ-201-date-filter-exception`.

### Architectural Decisions

`docs/tech-debt-audit.md` already diagnoses this exact ticket:

- **FE-003 — "Status Comparisons Use Wrong Case — Register Button Never Disables"**, severity High. Quotes the two comparisons and states: *"The API returns status values in uppercase (`"CANCELLED"`, `"COMPLETED"`). These comparisons are therefore always `false` — the button is never disabled and the cancellation notice is never shown, regardless of the conference state."* It explicitly notes the badge switches are fine because they `.toLowerCase()` first, and that "Only the direct equality checks ... are broken." Its suggested fix: `disabled={['CANCELLED','COMPLETED'].includes(conference?.status)}` and `conference?.status === 'CANCELLED'`. **The line numbers cited in the audit (341, 346) are stale** — the current lines are 372 and 377 after the BEVJ-103 changes.
- **TD-011 — "`status` Fields Stored as Raw `String` With No Type Safety"**, severity Medium. Names `Conference.java:37`, `Registration.java:23`, `ConferenceService.java:42`; flags that seed data uses the undocumented value `"ACTIVE"`, that `conf.getStatus().toUpperCase()` is an "ad-hoc safeguard", and proposes `enum ConferenceStatus { UPCOMING, ONGOING, COMPLETED, CANCELLED }`.
- **FE-009 — "Wrong Status Option in Create-Conference Form"**, severity Low: the create dropdown offers `ACTIVE`, which is not a documented backend value.
- **FE-002 — "No `response.ok` check on any fetch call"**, severity High: all 8 `api.js` functions call `.json()` unconditionally, so HTTP errors become data.
- **FE-008 — dead `/conferences/:id/register` route**, severity Low: the route is never linked to from anywhere; `useNavigate` is imported unused in two files.
- **TD-009 — no `@Transactional` on `registerAttendee`** (4 DB ops).
- No ADR directory and no `NOTE:`/`HACK:`/`DECISION:` inline markers found in the affected source files.

### Derived Conventions

With no guides present, the conventions the change must follow are those observed in code: normalise status with `.toLowerCase()` (or compare against uppercase literals) rather than raw lowercase equality; keep helpers local to the file (there is no shared module to extend); express status styling through the existing `badge badge--<status>` classes; and keep the DoD-relevant logic in `ConferenceDetailPage.jsx`, since that is the only file rendering a register affordance.

---

## 4. Testing Landscape

### Existing Coverage

Backend, `backend/src/test/java/com/brownevents/app/`:

- `service/ConferenceServiceTest.java`
- `service/SessionServiceTest.java`
- `service/RegistrationServiceTest.java` — 3 tests, all on `deleteRegistration` and `getRegistrations`. **`registerAttendee` has zero test coverage.**
- `controller/ConferenceControllerTest.java` — `@WebMvcTest(ConferenceController.class)` + `@MockBean ConferenceService` + `MockMvc`, asserting `status().isCreated()` and `jsonPath("$.data.title")`.
- `GlobalExceptionHandlerTest.java`

Frontend: **no test files, no test directory, no test runner.** `frontend/package.json` has scripts `dev`, `build`, `preview` only; dependencies are `react`, `react-dom`, `react-router-dom`; devDependencies are `@vitejs/plugin-react` and `vite`. There is no `vitest`, `jest`, `@testing-library/*`, `playwright`, or `cypress`, and no `*.test.jsx`/`*.spec.jsx` anywhere under `frontend/`.

There is also no E2E harness in the backend: `backend/pom.xml` declares `spring-boot-starter-test` as the sole test-scoped dependency, and no `E2E` package exists under `backend/src/test/java/com/brownevents/app/` (working tree is clean apart from untracked `.temp/` and this run directory).

### Testing Framework and Patterns

- JUnit 5 (`org.junit.jupiter.api.Test`) + Mockito, supplied transitively by `spring-boot-starter-test`.
- Service unit tests: `@ExtendWith(MockitoExtension.class)` on the class, `@Mock` for each repository, `@InjectMocks` for the service, no Spring context, no database.
- Naming convention: `public class <Service>Test`, methods `public void <method>_<condition>_should<Outcome>()` — e.g. `deleteRegistration_registrationNotFound_shouldThrowResourceNotFoundException`, `getRegistrations_conferenceNotFound_shouldThrowResourceNotFoundException`, `createSession_shouldSetConferenceAndSave`.
- Assertion style: `assertThrows(X.class, () -> ...)` followed by `verify(repo, times(1))` / `verify(repo, never())` interaction checks. Static imports `org.mockito.Mockito.*` and `org.junit.jupiter.api.Assertions.assertThrows`.
- Controller tests: `@WebMvcTest(<Controller>.class)`, `@Autowired MockMvc`, `@MockBean` the service, `jsonPath("$.data...")` for the envelope.
- Commands (from `backend/`): `mvn test`, `mvn test -Dtest=RegistrationServiceTest`, `mvn test -Dtest=RegistrationServiceTest#methodName`.

### Coverage Gaps

- `ConferenceDetailPage.jsx` — the file the DoD targets — has no test coverage and **no framework in which to write any**. Every DoD checkbox is about frontend button visibility.
- `RegistrationService.registerAttendee` — untested, and currently unguarded.
- No integration test exercises `POST /api/conferences/{id}/register` end to end; `RegistrationController` has no controller test.
- No test asserts the case of persisted or returned status values, which is why FE-003 survived.

---

## 5. Configuration and Environment

### Environment Variables

- No env var governs conference status or registration. `frontend/src/api.js:1` hardcodes `const BASE_URL = 'http://localhost:8080'`; `VITE_API_URL` is mentioned in `CLAUDE.md` but is not read anywhere in `frontend/src`.
- Backend config is literal in `application.properties`; no `os.getenv`/`${...}` placeholders relevant to this domain.

### Configuration Files

- `backend/src/main/resources/application.properties` — Postgres JDBC URL (`postgres:5432`), `spring.jpa.hibernate.ddl-auto=update`, `spring.jpa.show-sql=true`, Swagger UI.
- `frontend/vite.config.js` — dev proxy `/api/*` → `localhost:8080` (redundant given the absolute `BASE_URL`).
- `frontend/nginx.conf` — proxies `/api/` → `http://backend:8080/api/`, `try_files` SPA fallback.
- `docker-compose.yml` — Postgres + backend:8080 + frontend:3000.
- `frontend/src/index.css` — the single global stylesheet; holds the `.badge--*` status classes (`:316-350`).
- `backend/pom.xml` — Java 11, Spring Boot 2.7.14, springdoc 1.7.0, `spring-boot-starter-test`.

### Feature Flags and Deployment Concerns

No feature-flag system, no runtime toggles, no secrets management (DB credentials are plaintext in `application.properties`). `DataInitializer` seeds on every boot, so the `ACTIVE` and `COMPLETED` fixtures are what any local or Docker run will display — the `COMPLETED` "Cloud & DevOps World" conference is the natural manual-verification target, and there is **no seeded `CANCELLED` conference to verify against without creating one**.

---

## 6. Risk Indicators

- **`status` is nullable and unvalidated.** `Conference.status` is a bare `String` with no `nullable = false`, no default, and no validation on `POST /api/conferences`. A conference with `status = null` is representable today. The existing badge helpers answer this by treating null as `upcoming` (`ConferenceDetailPage.jsx:153`, `ConferenceCard.jsx:5`) and the display falls back to the literal `'Upcoming'` (`:235`, `:285`). *Speculative:* the change must pick a null policy, and the only precedent in the codebase is "null behaves as upcoming", i.e. show the button; an allow-list of blocked statuses reproduces that behaviour, a deny-list of permitted statuses inverts it.
- **Case sensitivity is the whole defect.** Any fix that compares `conference.status` against a literal without normalising will reintroduce FE-003 the moment a lowercase row exists — and lowercase rows *are* creatable, because `createConference` does not uppercase while `updateConference` does (`ConferenceService.java:38-51`).
- **The value set is contested.** `ACTIVE` (seeded, `DataInitializer.java:106`) vs `ONGOING` (documented, `Conference.java:36`) are both live. The DoD's phrase "upcoming and active conferences" maps to `UPCOMING` plus *both* `ACTIVE` and `ONGOING`. Handling only the documented four values leaves the seeded `ACTIVE` conference unaccounted for.
- **DoD says "not visible", code says `disabled`.** `ConferenceDetailPage.jsx:372` currently disables rather than hides. Satisfying "is not visible" is a change in rendering approach, not just in the predicate — and the surrounding flex row at `:365-376` is laid out with `justifyContent: 'space-between'` around the heading, so removing the button changes that row's layout.
- **Frontend has no test infrastructure at all.** No runner, no assertion library, no component-test harness, no E2E tool, and no npm `test` script. Every DoD checkbox is a frontend-rendering assertion, so a test-first workflow cannot be satisfied for the DoD as written without first standing up a runner (adding devDependencies, a config file, and a `test` script) — that is net-new infrastructure with no in-repo precedent, and `CLAUDE.md` states plainly "There is no lint or test script in the frontend."
- **Backend has no guard, so the UI is the only defence.** `RegistrationService.registerAttendee` (`:31-41`) accepts registrations for COMPLETED and CANCELLED conferences and returns 201. Hiding the button closes the visible path but leaves `POST /api/conferences/{id}/register` and the unlinked `/conferences/:id/register` route open. *Speculative:* a backend guard would be the only part of this ticket that is unit-testable with the existing JUnit+Mockito pattern, and `RegistrationServiceTest` is the natural home for it — but it is additional scope beyond the four DoD checkboxes.
- **Second entry point is unguarded and route-reachable.** `RegistrationPage.jsx` (`/conferences/:id/register`, `App.jsx:14`) already fetches the conference and has `conference.status` in hand (`:25-35`) but performs no status check; its submit button at `:195-201` is gated only on `submitting`. It is not linked from any page (tech-debt FE-008), yet it is directly URL-reachable. The DoD names only "the conference detail page".
- **`api.js` swallows HTTP errors.** `registerAttendee` (`api.js:60-68`) never checks `response.ok`, so if a backend rejection were added, the client would treat the error body as success and show "Registration Successful!". This couples any backend-guard work to fixing FE-002.
- **Helper duplication invites divergence.** `getStatusBadgeClass` exists identically in two files with no shared module; adding a third status helper inline continues the pattern but spreads the eligibility rule further.
- **Missing CSS state.** No `.badge--ongoing` class exists in `index.css`, so `ONGOING` renders with upcoming styling — evidence that the `ACTIVE`/`ONGOING` ambiguity has already leaked into the stylesheet.
- **Stale documentation.** `docs/tech-debt-audit.md` FE-003 cites `ConferenceDetailPage.jsx` lines 341/346; the code is now at 372/377. Anyone navigating by the audit's line numbers will land in the wrong place.
- **No seeded CANCELLED fixture.** Verifying DoD checkbox 2 manually requires creating a cancelled conference via `POST`/`PUT` first; `DataInitializer` provides only `UPCOMING`, `ACTIVE`, and `COMPLETED`.

---

## 7. Summary for Complexity Assessment

This is a small, well-localised frontend defect with a large surrounding ambiguity. The register affordance exists in exactly one place — `frontend/src/pages/ConferenceDetailPage.jsx:369-375` — and it *already carries a status guard*: `disabled={conference?.status === 'cancelled' || conference?.status === 'completed'}`. The guard never fires because the API emits uppercase (`COMPLETED`, `CANCELLED`, `ACTIVE`, `UPCOMING`, seeded in `DataInitializer.java:97-115`) while the comparison uses lowercase. The same mismatch kills the adjacent cancelled-notice at `:377`. `conference.status` is already fetched and already rendered twice as a badge, so no API, service, entity, or schema work is implied by the DoD; the file change surface is plausibly a single JSX file, with `frontend/src/pages/RegistrationPage.jsx` as an optional second if the unlinked `/conferences/:id/register` route is brought in scope. `docs/tech-debt-audit.md` has already diagnosed the bug as FE-003 (High) and proposed a fix, which sharply reduces investigation cost.

Technical novelty is essentially nil: the codebase already contains the correct idiom in two copies of `getStatusBadgeClass`, which normalise with `.toLowerCase()` before switching, and a `.badge--<status>` CSS class per state. What is *not* settled is the value set and the null policy. `ACTIVE` (seeded) and `ONGOING` (documented in `Conference.java:36`) are both in play; `ConferenceListPage.jsx` contains two dropdowns that disagree with each other about which exists; there is no `.badge--ongoing` class; `status` is a nullable unvalidated `String`; and `createConference` does not uppercase while `updateConference` does, so lowercase rows are creatable. The DoD also asks for "not visible" where the code currently only disables, which touches the layout of the flex row at `:365-376`.

Test coverage posture is the dominant risk. The frontend has **zero** test infrastructure — no runner, no assertion library, no `test` script, no test files — so none of the four DoD checkboxes can be verified by an automated test without first standing up a runner from scratch, which has no precedent in this repo. The backend, by contrast, has a clean JUnit 5 + Mockito pattern (`@ExtendWith(MockitoExtension.class)`, `@Mock`/`@InjectMocks`, `<method>_<condition>_should<Outcome>` naming) plus `@WebMvcTest` controller tests — but `RegistrationService.registerAttendee` is both untested and completely unguarded (it registers attendees for COMPLETED and CANCELLED conferences and returns 201), so the only testable slice of this ticket is scope the DoD does not ask for. Secondary risks: `api.js` never checks `response.ok`, so any future backend rejection would render as success; the unlinked-but-reachable `RegistrationPage` route stays open; and no `CANCELLED` fixture is seeded, so manual verification of checkbox 2 requires creating one.

---

## 8. External References

None named by the task. The research emphasis pointed only at in-repository paths (`backend/src/main/java/com/brownevents/app/entity/Conference.java`, `DataInitializer`, `frontend/src/pages/ConferenceDetailPage.jsx`, `frontend/src/pages/RegistrationPage.jsx`, `frontend/src/pages/ConferenceListPage.jsx`, `frontend/src/api.js`, `backend/src/test/java/com/brownevents/app/service/`), all of which resolved and are reported in Sections 2–5.
