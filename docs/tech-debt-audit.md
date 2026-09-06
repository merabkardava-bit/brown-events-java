# Brown Events — Technical Debt Audit

> **Branch**: BEVJ-003-tech-debt-audit  
> **Date**: 2026-09-06  
> **Scope**: Full-stack audit — services, controllers, entities, config, tests, React frontend  
> **Stack**: Spring Boot 2.7.14 · Java 11 · PostgreSQL · Spring Data JPA

---

## Summary

| ID | Category | Severity | One-liner |
|----|----------|----------|-----------|
| [TD-001](#td-001--unchecked-optionalget-throughout-service-layer) | Error Handling | 🔴 High | Seven unguarded `.get()` calls silently return 500 instead of 404 |
| [TD-002](#td-002--illegalargumentexception-maps-to-500-not-400) | Error Handling | 🔴 High | Business error thrown as `IllegalArgumentException` → HTTP 500, documented as 400 |
| [TD-003](#td-003--mixed-response-envelope-across-endpoints) | Consistency | 🟠 Medium | Some endpoints wrap in `{"data":…}`, others return raw objects |
| [TD-004](#td-004--post-endpoints-return-200-ok-instead-of-201-created) | Consistency | 🟡 Low | All four creation endpoints return `200 OK` instead of `201 Created` |
| [TD-005](#td-005--plain-text-database-credentials-in-source-control) | Configuration | 🔴 High | DB username/password hard-coded and committed to source control |
| [TD-006](#td-006--ddl-autoupdate-is-unsafe-for-production) | Configuration | 🔴 High | `ddl-auto=update` can silently mutate production schema on startup |
| [TD-007](#td-007--sql-logging-enabled-globally) | Configuration | 🟡 Low | `show-sql=true` leaks query structure and degrades performance in all environments |
| [TD-008](#td-008--cors-wildcard-declared-twice) | Configuration | 🟡 Low | `@CrossOrigin(origins = "*")` on every controller duplicates the global `WebConfig` |
| [TD-009](#td-009--no-transactional-boundary-on-multi-step-registration) | Performance | 🟠 Medium | `registerAttendee` does 4 DB ops with no transaction — partial failure leaves orphaned rows |
| [TD-010](#td-010--default-eager-fetch-on-manytoone-associations-in-registration) | Performance | 🟠 Medium | `@ManyToOne` defaults to `EAGER` in `Registration` — risks N+1 on list queries |
| [TD-011](#td-011--status-fields-stored-as-raw-string-with-no-type-safety) | Maintainability | 🟠 Medium | Conference and Registration statuses are unconstrained strings; seed data uses undocumented value `"ACTIVE"` |
| [TD-012](#td-012--no-bean-validation-on-entities-or-controller-request-bodies) | Maintainability | 🟠 Medium | No `@NotBlank` / `@Email` / `@Valid` — invalid input is accepted and persisted silently |
| [FE-001](#fe-001--hardcoded-localhostport-in-apijs-breaks-every-non-local-environment) | Frontend | 🔴 High | `http://localhost:8080` hard-coded in `api.js` — app is broken in Docker and every deployed env |
| [FE-002](#fe-002--no-responsestatus-check-on-any-fetch-call) | Frontend | 🔴 High | All 8 API functions call `.json()` without checking `response.ok` — HTTP errors silently become data |
| [FE-003](#fe-003--status-comparisons-use-wrong-case--register-button-never-disables) | Frontend | 🔴 High | Status guards compare lowercase `'cancelled'` against API's uppercase `'CANCELLED'` — button stays enabled |
| [FE-004](#fe-004--ghost-entity-fields-referenced-throughout-the-ui) | Frontend | 🟠 Medium | 10+ fields referenced in JSX that don't exist on the backend entities — rows silently render blank |
| [FE-005](#fe-005--registration-form-collects-and-sends-fields-the-backend-ignores) | Frontend | 🟠 Medium | Modal sends `phone`, `company`, `dietaryRequirements`, `sessionId` — all silently dropped by the API |
| [FE-006](#fe-006--formattime-is-a-no-op-in-two-places) | Frontend | 🟠 Medium | `formatTime` returns the raw ISO string unchanged — timestamps display as `2024-03-15T09:00:00` |
| [FE-007](#fe-007--speaker-name-never-renders-on-session-cards) | Frontend | 🟠 Medium | `SessionMeta` reads `session.speakerName` (flat string) but the API nests it under `session.speaker.firstName` |
| [FE-008](#fe-008--dead-registrationpage-route-and-usenavvigate-imported-unused) | Frontend | 🟡 Low | `/conferences/:id/register` route is never linked to; `useNavigate` imported in two files but never called |
| [FE-009](#fe-009--wrong-status-option-in-create-conference-form) | Frontend | 🟡 Low | Create-conference dropdown offers `"ACTIVE"` — not a valid backend value; should be `"ONGOING"` |
| [FE-010](#fe-010--debug-consolelog-statements-in-production-code) | Frontend | 🟡 Low | 9 `console.log` calls across `api.js`, `ConferenceDetailPage`, and `RegistrationPage` left in production |

---

## Error Handling

### TD-001 — Unchecked `Optional.get()` Throughout Service Layer

**Severity**: 🔴 High

| File | Lines |
|------|-------|
| `backend/src/main/java/com/brownevents/app/service/ConferenceService.java` | 28, 36, 51 |
| `backend/src/main/java/com/brownevents/app/service/RegistrationService.java` | 32 |
| `backend/src/main/java/com/brownevents/app/service/SessionService.java` | 21, 27, 31 |

**Description**  
Seven calls to `Optional.get()` are made without any prior `isPresent()` check
or use of `orElseThrow()`. When the requested entity does not exist, the `Optional`
is empty and `get()` throws a `java.util.NoSuchElementException`. Spring's default
exception handler has no mapping for this exception, so it converts it to HTTP 500
Internal Server Error. The Swagger annotations on every affected controller endpoint
correctly document a 404 response, but the code can never produce one.

```java
// ConferenceService.java:28 — representative example
public Conference getConference(Long id) {
    return conferenceRepository.findById(id).get(); // throws NoSuchElementException if absent
}
```

**Suggested Fix**  
Replace every bare `.get()` with a `orElseThrow` call that produces a
`ResponseStatusException` with status 404:

```java
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

public Conference getConference(Long id) {
    return conferenceRepository.findById(id)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
            "Conference not found: " + id));
}
```

Apply the same pattern across all seven occurrences in `ConferenceService`,
`RegistrationService`, and `SessionService`.

---

### TD-002 — `IllegalArgumentException` Maps to 500, Not 400

**Severity**: 🔴 High

| File | Line |
|------|------|
| `backend/src/main/java/com/brownevents/app/service/RegistrationService.java` | 47 |
| `backend/src/main/java/com/brownevents/app/controller/RegistrationController.java` | 118 (Swagger `@ApiResponse`) |

**Description**  
`deleteRegistration` throws `IllegalArgumentException` when the registration ID
does not belong to the given conference. Spring's `DefaultHandlerExceptionResolver`
does not map `IllegalArgumentException` to any HTTP status — the request ends with
HTTP 500. The Swagger documentation on the controller promises HTTP 400, which
is the correct semantic for "bad client input", but the runtime never produces it.

```java
// RegistrationService.java:45–48
public void deleteRegistration(Long conferenceId, Long registrationId) {
    if (!registrationRepository.existsByIdAndConferenceId(registrationId, conferenceId)) {
        throw new IllegalArgumentException("Registration not found for this conference"); // → 500
    }
```

**Suggested Fix**  
Replace with `ResponseStatusException` so the HTTP mapping is explicit:

```java
throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
    "Registration " + registrationId + " does not belong to conference " + conferenceId);
```

Alternatively, introduce a global `@ControllerAdvice` / `@ExceptionHandler` that
maps `IllegalArgumentException` → 400 consistently across the whole application.

---

## Consistency

### TD-003 — Mixed Response Envelope Across Endpoints

**Severity**: 🟠 Medium

| File | Lines | Shape returned |
|------|-------|----------------|
| `backend/.../controller/ConferenceController.java` | 52, 77, 118, 162 | raw `Conference` / `List<Conference>` |
| `backend/.../controller/ConferenceController.java` | 194, 226 | `{"data": …}` |
| `backend/.../controller/RegistrationController.java` | 79 | `{"data": Registration}` |
| `backend/.../controller/RegistrationController.java` | 103 | raw `List<Registration>` |
| `backend/.../controller/SpeakerController.java` | 50 | raw `List<Speaker>` |
| `backend/.../controller/SpeakerController.java` | 76 | `{"data": Speaker}` |

**Description**  
Half the endpoints return their payload directly as JSON; the other half wrap it
in a `Map<String, Object>` with a `"data"` key, constructed inline using
`new HashMap<>()`. There is no declared rule about when an envelope is used.
API clients (and the existing Swagger spec) must handle two different shapes
depending on which endpoint is called, which makes client code fragile.

**Suggested Fix**  
Pick one convention and apply it uniformly. The two most common options are:

1. **No envelope** — return bare objects/lists for all endpoints. Simple, less
   boilerplate, easier to document with Swagger `@Schema(implementation = X.class)`.
2. **Typed envelope for all responses** — introduce a generic wrapper once:
   ```java
   public record ApiResponse<T>(T data) {}
   ```
   Return `ResponseEntity<ApiResponse<T>>` from every controller method.

Delete all inline `new HashMap<>()` / `response.put("data", ...)` blocks —
they are not reusable and cannot be described correctly in Swagger.

---

### TD-004 — POST Endpoints Return `200 OK` Instead of `201 Created`

**Severity**: 🟡 Low

| File | Method | Line |
|------|--------|------|
| `backend/.../controller/ConferenceController.java` | `createConference` | 118 |
| `backend/.../controller/ConferenceController.java` | `createSession` | 226 |
| `backend/.../controller/RegistrationController.java` | `registerAttendee` | 79 |
| `backend/.../controller/SpeakerController.java` | `createSpeaker` | 76 |

**Description**  
All four creation endpoints call `ResponseEntity.ok(...)`, which returns
HTTP 200. RFC 7231 §6.3.2 and REST convention specify that a successful
resource creation should return `201 Created`. HTTP 200 is ambiguous — it
cannot be distinguished from a read response. Some clients and API gateways
key on the status code to decide caching and retry behaviour.

**Suggested Fix**  
```java
// Replace
return ResponseEntity.ok(conferenceService.createConference(conference));

// With
return ResponseEntity.status(HttpStatus.CREATED).body(conferenceService.createConference(conference));
```

For `createSession` and `createSpeaker` the same substitution applies. Update
the `@ApiResponse(responseCode = "200", ...)` Swagger annotations to `"201"`.

---

## Configuration

### TD-005 — Plain-text Database Credentials in Source Control

**Severity**: 🔴 High

| File | Lines |
|------|-------|
| `backend/src/main/resources/application.properties` | 2–3 |

**Description**  
The database username and password are hard-coded as literal strings:

```properties
spring.datasource.username=brownevents
spring.datasource.password=brownevents
```

Any developer who clones the repository, or any system that mirrors it, gets
direct database access credentials. In a CI/CD pipeline the values are visible
in build logs; if the repo is ever made public, the credentials are exposed
immediately. This violates the "Credentials must not be stored in source
control" requirement of OWASP and the Twelve-Factor App methodology.

**Suggested Fix**  
Replace with environment-variable placeholders and retain the current value as
a local fallback only (never commit to remote):

```properties
spring.datasource.username=${DB_USERNAME:brownevents}
spring.datasource.password=${DB_PASSWORD:brownevents}
```

Add a git-ignored `.env` file (or `docker-compose.override.yml`) for local
development. Inject real credentials via the deployment environment (Docker
secrets, Kubernetes Secrets, or a secrets manager) in production.

---

### TD-006 — `ddl-auto=update` Is Unsafe for Production

**Severity**: 🔴 High

| File | Line |
|------|------|
| `backend/src/main/resources/application.properties` | 4 |

**Description**  
```properties
spring.jpa.hibernate.ddl-auto=update
```
`update` instructs Hibernate to modify the live schema on every application
startup — adding missing columns and tables but never dropping anything. This
means:
- A typo in an entity field name silently creates a duplicate column.
- Column type changes are not automatically migrated, potentially corrupting
  data or causing startup failure.
- There is no audit trail of schema changes — it is impossible to know what
  the schema was at any point in history.
- Running two instances simultaneously during a rolling deploy can produce a
  race condition on schema mutations.

**Suggested Fix**  
Switch to `validate` (or `none`) and adopt a migration tool with explicit
versioned SQL scripts:

```properties
spring.jpa.hibernate.ddl-auto=validate
```

Add **Flyway** or **Liquibase** to `pom.xml` and create
`src/main/resources/db/migration/V1__initial_schema.sql` to own schema
evolution explicitly.

---

### TD-007 — SQL Logging Enabled Globally

**Severity**: 🟡 Low

| File | Line |
|------|------|
| `backend/src/main/resources/application.properties` | 5 |

**Description**  
```properties
spring.jpa.show-sql=true
```
This setting writes every SQL statement emitted by Hibernate to stdout in every
environment — local, staging, and production. In production this:
- Floods application logs with routine query chatter, increasing cost and
  storage on log aggregation services.
- Leaks schema and query structure to anyone with log access.
- Degrades throughput under load because string formatting and I/O for each
  SQL statement is synchronous.

There is no `application-dev.properties` or `application-prod.properties` to
provide per-environment overrides.

**Suggested Fix**  
Set the base value to `false` and add a dev-only override:

```properties
# application.properties
spring.jpa.show-sql=false

# application-dev.properties  (new file, not committed with sensitive values)
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true
```

Activate the dev profile locally with `--spring.profiles.active=dev` or in
`application-dev.properties`.

---

### TD-008 — CORS Wildcard Declared Twice

**Severity**: 🟡 Low

| File | Lines |
|------|-------|
| `backend/src/main/java/com/brownevents/app/WebConfig.java` | 12–16 |
| `backend/src/main/java/com/brownevents/app/controller/ConferenceController.java` | 27 |
| `backend/src/main/java/com/brownevents/app/controller/RegistrationController.java` | 27 |
| `backend/src/main/java/com/brownevents/app/controller/SessionController.java` | 21 |
| `backend/src/main/java/com/brownevents/app/controller/SpeakerController.java` | 25 |

**Description**  
`WebConfig` registers a global CORS mapping for `/api/**` with
`allowedOriginPatterns("*")` and all HTTP methods. All four controllers also
carry `@CrossOrigin(origins = "*")`. The per-controller annotations are
completely redundant — they add noise and create a maintenance trap: if the
project ever needs to restrict origins (e.g. to `https://app.example.com`), the
change must be made in five places instead of one, and it is easy to miss one.

**Suggested Fix**  
Remove `@CrossOrigin(origins = "*")` from all four controllers. Rely solely on
`WebConfig` as the single authoritative CORS configuration:

```java
// Delete from ConferenceController, RegistrationController,
// SessionController, SpeakerController:
@CrossOrigin(origins = "*")  // ← remove
```

---

## Performance

### TD-009 — No `@Transactional` Boundary on Multi-step Registration

**Severity**: 🟠 Medium

| File | Lines |
|------|-------|
| `backend/src/main/java/com/brownevents/app/service/RegistrationService.java` | 29–38, 45–50 |

**Description**  
`registerAttendee` executes four separate database operations with no enclosing
transaction:

1. `attendeeRepository.findByEmail(email)` — SELECT
2. *(conditional)* `attendeeRepository.save(attendee)` — INSERT
3. `conferenceRepository.findById(conferenceId).get()` — SELECT
4. `registrationRepository.save(registration)` — INSERT

If step 4 fails (e.g. a unique constraint, network timeout, or unexpected
runtime exception), steps 1–3 have already been committed independently. The
result is a persisted `Attendee` row with no corresponding `Registration`,
leaving the database in an inconsistent state that is hard to detect and
correct. `deleteRegistration` has the same issue across its two DB calls.

**Suggested Fix**  
Add `@Transactional` from `org.springframework.transaction.annotation`:

```java
import org.springframework.transaction.annotation.Transactional;

@Transactional
public Registration registerAttendee(Long conferenceId, Attendee attendee) { ... }

@Transactional
public void deleteRegistration(Long conferenceId, Long registrationId) { ... }
```

This ensures that if any step throws, Spring rolls back all prior DB changes in
the same unit of work.

---

### TD-010 — Default EAGER Fetch on `@ManyToOne` Associations in `Registration`

**Severity**: 🟠 Medium

| File | Lines |
|------|-------|
| `backend/src/main/java/com/brownevents/app/entity/Registration.java` | 27–33 |

**Description**  
Both associations in `Registration` are declared without an explicit fetch type:

```java
@ManyToOne
@JoinColumn(name = "conference_id")
private Conference conference;   // default: EAGER

@ManyToOne
@JoinColumn(name = "attendee_id")
private Attendee attendee;       // default: EAGER
```

JPA's default for `@ManyToOne` is `FetchType.EAGER`. When
`GET /api/conferences/{id}/registrations` returns a list of N registrations,
Hibernate may issue additional SELECT queries for `Conference` and `Attendee`
per row (an N+1 pattern), rather than loading the full graph in a single JOIN.
For large conferences this can generate hundreds of unnecessary round-trips to
the database.

**Suggested Fix**  
Mark both associations `LAZY` and fetch the graph explicitly only when needed:

```java
@ManyToOne(fetch = FetchType.LAZY)
@JoinColumn(name = "conference_id")
private Conference conference;

@ManyToOne(fetch = FetchType.LAZY)
@JoinColumn(name = "attendee_id")
private Attendee attendee;
```

Add a custom query to `RegistrationRepository` that uses a JOIN FETCH when the
controller needs to return the full registration with attendee details:

```java
@Query("SELECT r FROM Registration r JOIN FETCH r.attendee JOIN FETCH r.conference WHERE r.conference.id = :id")
List<Registration> findByConferenceIdWithDetails(@Param("id") Long id);
```

---

## Maintainability

### TD-011 — `status` Fields Stored as Raw `String` With No Type Safety

**Severity**: 🟠 Medium

| File | Lines | Note |
|------|-------|------|
| `backend/src/main/java/com/brownevents/app/entity/Conference.java` | 37 | `private String status` |
| `backend/src/main/java/com/brownevents/app/entity/Registration.java` | 23 | `private String status` |
| `backend/src/main/java/com/brownevents/app/service/ConferenceService.java` | 42 | `conf.getStatus().toUpperCase()` |
| `backend/src/main/java/com/brownevents/app/DataInitializer.java` | 106 | `"ACTIVE"` — not a documented value |

**Description**  
Both `Conference.status` and `Registration.status` are plain `String` fields.
The `Conference` entity's `@Schema` annotation documents `UPCOMING / ONGOING /
COMPLETED / CANCELLED` as the valid values, but nothing enforces this. Three
problems result:

1. **Silent invalid data**: Any arbitrary string can be persisted.
   `DataInitializer.java:106` seeds `"ACTIVE"` for `javaDays`, which is not
   in the documented set and would not match any valid filter in client code.
2. **Manual normalization**: `ConferenceService.java:42` calls
   `conf.getStatus().toUpperCase()` as an ad-hoc safeguard — if `conf.getStatus()`
   is `null`, this line throws a `NullPointerException`.
3. **No compile-time safety**: Renaming a status value requires a full-text
   search across the codebase instead of a single enum rename.

**Suggested Fix**  
Define typed enums:

```java
// ConferenceStatus.java
public enum ConferenceStatus { UPCOMING, ONGOING, COMPLETED, CANCELLED }

// RegistrationStatus.java
public enum RegistrationStatus { CONFIRMED, CANCELLED }
```

Annotate the entity fields:

```java
@Enumerated(EnumType.STRING)
private ConferenceStatus status;
```

Remove the `.toUpperCase()` call from `ConferenceService.java:42`. Fix the seed
data in `DataInitializer.java:106`: `javaDays.setStatus(ConferenceStatus.ONGOING)`.

---

### TD-012 — No Bean Validation on Entities or Controller Request Bodies

**Severity**: 🟠 Medium

| File | Scope |
|------|-------|
| `backend/src/main/java/com/brownevents/app/entity/Conference.java` | all fields |
| `backend/src/main/java/com/brownevents/app/entity/Session.java` | all fields |
| `backend/src/main/java/com/brownevents/app/entity/Attendee.java` | all fields |
| `backend/src/main/java/com/brownevents/app/entity/Speaker.java` | all fields |
| All POST/PUT controller methods | `@RequestBody` parameters |

**Description**  
No entity field carries a constraint annotation (`@NotBlank`, `@NotNull`,
`@Email`, `@Future`, `@Min`). No controller method's `@RequestBody` parameter
carries `@Valid`. As a result:

- `POST /api/conferences` with `{"title": null, "status": "INVALID"}` is
  accepted and persisted.
- `POST /api/conferences/{id}/register` with `{"email": "not-an-email"}` is
  accepted and the attendee email is stored without validation.
- `POST /api/conferences/{id}/sessions` with negative `capacity` or
  `endTime` before `startTime` is accepted silently.

Any downstream display, email dispatch, or business logic that assumes valid
data will encounter unexpected `null` values or malformed strings.

**Suggested Fix**  
Add constraints to entities (examples):

```java
// Attendee.java
@NotBlank
private String firstName;
@NotBlank
private String lastName;
@Email @NotBlank
private String email;

// Conference.java
@NotBlank
private String title;
@NotNull
private LocalDate startDate;
@NotNull
private LocalDate endDate;

// Session.java
@Min(1)
private Integer capacity;
```

Add `@Valid` to every `@RequestBody` in the controller layer:

```java
// ConferenceController.java
public ResponseEntity<Conference> createConference(
        @org.springframework.web.bind.annotation.RequestBody @Valid Conference conference) { ... }
```

Spring Boot's default `MethodArgumentNotValidException` handler will
automatically return HTTP 400 with a structured error body listing the violated
constraints.

---

---

## Frontend

### FE-001 — Hardcoded `localhost:8080` in `api.js` Breaks Every Non-local Environment

**Severity**: 🔴 High

| File | Line |
|------|------|
| `frontend/src/api.js` | 1 |

**Description**  
Every API call in the application is constructed from an absolute base URL:

```js
const BASE_URL = 'http://localhost:8080';
```

This breaks in three scenarios that are all present in the repository:

1. **Docker Compose deployment** — `frontend/nginx.conf` proxies `/api/` to
   `http://backend:8080/api/` (the internal Docker hostname). Because `api.js`
   builds absolute `http://localhost:8080/...` URLs, the browser sends those
   requests directly and never touches the nginx proxy. In a containerised
   environment `localhost:8080` is unreachable from the browser.
2. **Any staging or production environment** where the backend is not on
   `localhost:8080`.
3. **Vite dev proxy** — `vite.config.js` defines
   `'/api': 'http://localhost:8080'` to proxy `/api` requests. Because
   `api.js` uses absolute URLs, the proxy is bypassed entirely and that
   configuration is dead code.

**Suggested Fix**  
Use relative URLs everywhere and let the hosting layer (Vite proxy in dev,
nginx in prod) route them:

```js
// api.js — remove BASE_URL entirely
export async function getConferences() {
  const response = await fetch('/api/conferences');
  ...
}
```

Remove `BASE_URL` and the module-level `console.log` on line 3. The Vite dev
proxy in `vite.config.js` then works as intended with no further changes.

---

### FE-002 — No Response-Status Check on Any `fetch` Call

**Severity**: 🔴 High

| File | Lines |
|------|-------|
| `frontend/src/api.js` | 8, 14, 19, 26, 31, 40, 45, 61 |

**Description**  
All eight API functions parse the JSON body immediately without first checking
`response.ok`:

```js
export async function getConference(id) {
  const response = await fetch(`${BASE_URL}/api/conferences/${id}`);
  return response.json();   // called even if status is 404 or 500
}
```

When the backend returns 404, 500, or 400, `response.json()` succeeds and
returns the error body as data. The component's `.catch()` handler only fires
on network failures — it never fires for HTTP errors. The UI either silently
renders an error object as if it were real data, or (if the error body is not
valid JSON) throws an unhandled parse error instead of a clean user-facing message.

`deleteRegistration` is even more silent — it does not call `.json()` at all,
so a failed delete simply goes unnoticed:

```js
export async function deleteRegistration(conferenceId, registrationId) {
  await fetch(`...`, { method: 'DELETE' });   // error status is discarded
}
```

**Suggested Fix**  
Add a `response.ok` guard in every function:

```js
export async function getConference(id) {
  const response = await fetch(`/api/conferences/${id}`);
  if (!response.ok) throw new Error(`Failed to load conference (${response.status})`);
  return response.json();
}

export async function deleteRegistration(conferenceId, registrationId) {
  const response = await fetch(`/api/conferences/${conferenceId}/registrations/${registrationId}`,
    { method: 'DELETE' });
  if (!response.ok) throw new Error(`Failed to delete registration (${response.status})`);
}
```

This lets the component `.catch()` handlers in `ConferenceDetailPage.jsx`
surface a real error message instead of silently swallowing failures.

---

### FE-003 — Status Comparisons Use Wrong Case — Register Button Never Disables

**Severity**: 🔴 High

| File | Lines |
|------|-------|
| `frontend/src/pages/ConferenceDetailPage.jsx` | 341, 346 |

**Description**  
The Register button guard and the cancelled-notice are gated on lowercase
string comparisons:

```jsx
// Line 341
disabled={conference?.status === 'cancelled' || conference?.status === 'completed'}

// Line 346
{conference?.status === 'cancelled' && (
  <p>This conference has been cancelled.</p>
)}
```

The API returns status values in uppercase (`"CANCELLED"`, `"COMPLETED"`).
These comparisons are therefore always `false` — the button is never disabled
and the cancellation notice is never shown, regardless of the conference
state. A cancelled conference will still display an active "Register Now"
button and accept new registrations.

The same bug exists in `ConferenceCard.jsx:6` and `ConferenceDetailPage.jsx:147`
inside `getStatusBadgeClass`, where the `switch` cases are lowercase but the
API values are uppercase — however these calls use `.toLowerCase()` on the
input, so they work correctly. Only the direct equality checks (lines 341 and
346) are broken.

**Suggested Fix**  
Normalise before comparing, or compare uppercase:

```jsx
disabled={['CANCELLED', 'COMPLETED'].includes(conference?.status)}

{conference?.status === 'CANCELLED' && (
  <p>This conference has been cancelled.</p>
)}
```

---

### FE-004 — Ghost Entity Fields Referenced Throughout the UI

**Severity**: 🟠 Medium

| File | Lines | Non-existent field |
|------|-------|--------------------|
| `frontend/src/pages/ConferenceDetailPage.jsx` | 239 | `conference.maxAttendees` |
| `frontend/src/pages/ConferenceDetailPage.jsx` | 242 | `conference.organizerName` |
| `frontend/src/pages/ConferenceDetailPage.jsx` | 264 | `conference.venue` |
| `frontend/src/pages/ConferenceDetailPage.jsx` | 285 | `conference.maxAttendees` (info table) |
| `frontend/src/pages/ConferenceDetailPage.jsx` | 289 | `conference.organizerName` (info table) |
| `frontend/src/pages/ConferenceDetailPage.jsx` | 290 | `conference.website` |
| `frontend/src/pages/SessionDetailPage.jsx` | 101 | `session.conferenceName` |
| `frontend/src/pages/SessionDetailPage.jsx` | 115 | `session.date` |
| `frontend/src/pages/SessionDetailPage.jsx` | 121, 143 | `session.sessionType` / `session.type` |
| `frontend/src/pages/SessionDetailPage.jsx` | 159 | `session.availableSeats` |
| `frontend/src/pages/SessionDetailPage.jsx` | 161 | `session.tags` |
| `frontend/src/pages/SessionDetailPage.jsx` | 198 | `speaker.title`, `speaker.company` |
| `frontend/src/pages/SessionDetailPage.jsx` | 236, 240 | `room.floor`, `room.building` |
| `frontend/src/components/SessionMeta.jsx` | 23, 26 | `session.sessionType`, `session.speakerName` |

**Description**  
Dozens of fields are read from API response objects that have no corresponding
column in any entity. All of them silently evaluate to `undefined` and either
render as `"—"` (in table rows) or are suppressed by the conditional rendering
guard, so rows and meta sections go blank without any indication that the data
is missing by design vs. a bug.

Notable examples:
- The breadcrumb in `SessionDetailPage.jsx:101` always shows the literal text
  `"Conference"` (not the conference title) because `session.conferenceName`
  does not exist — the link text fallback is always used.
- The entire "Available Seats" table row always shows `"—"` because the API
  tracks `capacity` (total) not available seats.
- The Speaker section in `SessionDetailPage` never shows a title/company line
  because `speaker.title` and `speaker.company` are not on the `Speaker` entity.

**Suggested Fix**  
Remove every reference to fields that don't exist in the API. For fields that
represent genuinely missing functionality (available seats, tags, session type,
conference website), either remove the UI rows or file follow-up tickets to add
those columns to the backend. For `session.conferenceName`, read the nested
object instead:

```jsx
// SessionDetailPage.jsx — breadcrumb conference name
<Link to={`/conferences/${id}`}>
  {session?.conference?.title || 'Conference'}
</Link>
```

For speaker name on session cards (`SessionMeta.jsx:26`), read the nested object:

```jsx
// SessionMeta.jsx — was: session.speakerName (doesn't exist)
{session.speaker && (
  <span>🎤 {session.speaker.firstName} {session.speaker.lastName}</span>
)}
```

---

### FE-005 — Registration Form Collects and Sends Fields the Backend Ignores

**Severity**: 🟠 Medium

| File | Lines |
|------|-------|
| `frontend/src/pages/ConferenceDetailPage.jsx` | 30–37, 132–135, 534–583 |

**Description**  
The registration modal collects six fields: `firstName`, `lastName`, `email`,
`phone`, `company`, and `dietaryRequirements`. It also attaches `sessionId`
from the selected session:

```js
// Line 132–135
const result = await registerAttendee(id, {
  ...formData,
  sessionId: selectedSession ? selectedSession.id : null
})
```

The `Attendee` entity has only `firstName`, `lastName`, and `email`. The
backend `RegistrationService.registerAttendee()` maps the incoming JSON to an
`Attendee` object — Jackson will silently ignore unknown fields. The values
the user enters for phone, company, dietary requirements, and session
preference are accepted by the form, submitted to the API, and permanently
discarded without any acknowledgement.

Additionally, the success message on line 470 says "A confirmation will be
sent to `{email}`" — the backend has no email-sending capability, so this
claim is false.

**Suggested Fix**  
Short term: trim the modal form to only the three fields the backend actually
stores (`firstName`, `lastName`, `email`) and remove the `sessionId` field
from the payload. Remove the false confirmation email claim.

Long term: if phone/company/dietary requirements are a genuine product
requirement, add those columns to the `Attendee` entity and backend validation.

---

### FE-006 — `formatTime` Is a No-op in Two Places

**Severity**: 🟠 Medium

| File | Lines |
|------|-------|
| `frontend/src/pages/SessionDetailPage.jsx` | 27–30 |
| `frontend/src/components/SessionMeta.jsx` | 4–7 |

**Description**  
Both `SessionDetailPage` and `SessionMeta` define a `formatTime` function that
is identical and does nothing:

```js
function formatTime(timeString) {
  if (!timeString) return '—'
  return timeString           // raw ISO string returned unchanged
}
```

Session timestamps from the API are ISO 8601 datetimes:
`"2024-03-15T09:00:00"`. Users see exactly that string on every session card
and detail page instead of a human-readable time like "9:00 AM".

**Suggested Fix**  
Parse and format the datetime properly:

```js
function formatTime(timeString) {
  if (!timeString) return '—'
  try {
    return new Date(timeString).toLocaleTimeString('en-US', {
      hour: 'numeric',
      minute: '2-digit',
      hour12: true
    })
  } catch {
    return timeString
  }
}
```

Extract this (and `formatDate`) to a shared `frontend/src/utils/formatting.js`
module — `formatDate` is already duplicated across `ConferenceCard.jsx`,
`ConferenceDetailPage.jsx`, and `SessionDetailPage.jsx`.

---

### FE-007 — Speaker Name Never Renders on Session Cards

**Severity**: 🟠 Medium

| File | Line |
|------|------|
| `frontend/src/components/SessionMeta.jsx` | 26 |

**Description**  
`SessionMeta` attempts to show the speaker name via a flat property:

```jsx
{session.speakerName && (
  <span>🎤 {session.speakerName}</span>
)}
```

The `Session` entity serializes the speaker as a nested object:
`{ "speaker": { "id": 3, "firstName": "Ada", "lastName": "Lovelace", ... } }`.
There is no top-level `speakerName` field. As a result, the speaker chip never
appears on any session card in the sessions list.

**Suggested Fix**  
Read from the nested object:

```jsx
{session.speaker && (
  <span>🎤 {session.speaker.firstName} {session.speaker.lastName}</span>
)}
```

---

### FE-008 — Dead `RegistrationPage` Route and `useNavigate` Imported Unused

**Severity**: 🟡 Low

| File | Lines |
|------|-------|
| `frontend/src/pages/RegistrationPage.jsx` | 2, 7 |
| `frontend/src/App.jsx` | 14 |

**Description**  
`App.jsx:14` registers the route `/conferences/:id/register` pointing to
`RegistrationPage`. No component in the application links to or navigates
to this URL — the only registration flow is the modal inside
`ConferenceDetailPage.jsx`. The standalone page is therefore unreachable by
any user following normal navigation.

In addition, both `RegistrationPage.jsx` and `ConferenceDetailPage.jsx`
import and destructure `useNavigate` but never call `navigate(...)`:

```js
// RegistrationPage.jsx:2,7
import { useParams, Link, useNavigate } from 'react-router-dom'
const navigate = useNavigate()   // never used
```

**Suggested Fix**  
Either remove `RegistrationPage.jsx` and its route from `App.jsx` entirely, or
link to it and retire the modal (pick one registration flow). Remove the
`useNavigate` import and declaration from both files.

---

### FE-009 — Wrong Status Option in Create-Conference Form

**Severity**: 🟡 Low

| File | Line |
|------|------|
| `frontend/src/pages/ConferenceListPage.jsx` | 145 |

**Description**  
The status `<select>` in the create-conference modal includes `"ACTIVE"` as an
option:

```jsx
<option value="ACTIVE">ACTIVE</option>
```

The backend's documented valid statuses are `UPCOMING`, `ONGOING`,
`COMPLETED`, and `CANCELLED`. `"ACTIVE"` is not among them (it also appears as
a seed-data bug in `DataInitializer.java:106` — see TD-011). Selecting
`"ACTIVE"` will persist an undocumented status value with no backend
validation to reject it.

**Suggested Fix**  
Replace `"ACTIVE"` with `"ONGOING"` to match the documented enum set:

```jsx
<option value="ONGOING">ONGOING</option>
```

Align this change with TD-011 — once `ConferenceStatus` is an enum on the
backend, the frontend should pull valid values from the API or a shared
constants module rather than hard-coding them.

---

### FE-010 — Debug `console.log` Statements in Production Code

**Severity**: 🟡 Low

| File | Lines |
|------|-------|
| `frontend/src/api.js` | 3, 6, 20, 55 |
| `frontend/src/pages/ConferenceDetailPage.jsx` | 46, 96, 101, 130, 136 |
| `frontend/src/pages/RegistrationPage.jsx` | 44, 49 |

**Description**  
Nine `console.log` calls are active in production code, logging internal state
on page load, user interactions, and API responses:

```js
console.log('API base URL:', BASE_URL)          // fires on every page load
console.log('fetching conferences')             // fires on every list load
console.log('sessions response:', data)         // logs raw API payload
console.log('submitting registration form data:', formData)  // logs PII (email)
console.log('registration result:', result)     // logs full registration object
```

The `registerAttendee` log on line 55 of `api.js` is particularly notable — it
logs the full form submission including the user's email address to the browser
console. In browsers that forward console output to remote logging services
(some observability setups), this constitutes a PII leak.

**Suggested Fix**  
Remove all `console.log` statements. For development-time diagnostics, gate
logging behind `import.meta.env.DEV`:

```js
if (import.meta.env.DEV) console.log('sessions response:', data)
```

---

*End of audit — 22 issues identified across 6 categories (12 backend · 10 frontend).*
