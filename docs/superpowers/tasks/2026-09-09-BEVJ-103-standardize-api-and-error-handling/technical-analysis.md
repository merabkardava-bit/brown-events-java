# Technical Research

**Task**: api response error handling http status controller
**Generated**: 2026-09-09T00:00:00Z
**Research path**: filesystem

---

## 1. Original Context

BEVJ-103: Standardize API Responses and Error Handling. Fix inconsistent API responses and error handling across the BrownEvents Spring Boot backend. Requirements: (1) All endpoints return responses in the same structure — no surprises depending on which endpoint you call. (2) POST endpoints return 201 Created, not 200 OK. (3) Error responses are structured, human-readable, and never expose a stack trace. (4) Common failure scenarios (not found, bad input, unexpected errors) return appropriate HTTP status codes. (5) At least one existing unit test updated to reflect the new response shape.

---

## 2. Codebase Findings

### Existing Implementations

All four controllers live under `backend/src/main/java/com/brownevents/app/controller/`:

- `ConferenceController.java` — 6 handler methods: `GET /api/conferences` (paginated), `GET /api/conferences/{id}`, `POST /api/conferences`, `PUT /api/conferences/{id}`, `GET /api/conferences/{id}/sessions` (paginated), `POST /api/conferences/{id}/sessions`. Each method is fully annotated with springdoc `@Operation` / `@ApiResponses`.
- `SessionController.java` — 2 handler methods: `GET /api/sessions/{id}`, `PUT /api/sessions/{id}`.
- `SpeakerController.java` — 2 handler methods: `GET /api/speakers`, `POST /api/speakers`.
- `RegistrationController.java` — 3 handler methods: `POST /api/conferences/{id}/register`, `GET /api/conferences/{id}/registrations`, `DELETE /api/conferences/{id}/registrations/{registrationId}`.

All four controllers carry `@CrossOrigin(origins = "*")` in addition to the global `WebConfig` CORS setup.

Service layer under `backend/src/main/java/com/brownevents/app/service/`:

- `ConferenceService.java` — `getConference()` line 34, `updateConference()` line 42, `createSession()` line 57 each call `Optional.get()` without a prior presence check.
- `SessionService.java` — `createSession()` line 21, `getSession()` line 27, `updateSession()` line 31 each call `Optional.get()` without a prior presence check.
- `RegistrationService.java` — `registerAttendee()` line 32 calls `Optional.get()`. `deleteRegistration()` lines 46–48 throws `IllegalArgumentException` when a registration does not belong to the given conference.

Current response shape inventory across the 13 endpoints:

| Endpoint | Current shape | Current status |
|---|---|---|
| `GET /api/conferences` | `{data:[…], page, size, totalElements, totalPages}` | 200 |
| `GET /api/conferences/{id}` | raw `Conference` | 200 |
| `POST /api/conferences` | raw `Conference` | 200 |
| `PUT /api/conferences/{id}` | raw `Conference` | 200 |
| `GET /api/conferences/{id}/sessions` | `{data:[…], page, size, totalElements, totalPages}` | 200 |
| `POST /api/conferences/{id}/sessions` | `{"data": Session}` | 200 |
| `POST /api/conferences/{id}/register` | `{"data": Registration}` | 200 |
| `GET /api/conferences/{id}/registrations` | raw `List<Registration>` | 200 |
| `DELETE /api/conferences/{id}/registrations/{rid}` | empty body | 204 |
| `GET /api/sessions/{id}` | raw `Session` | 200 |
| `PUT /api/sessions/{id}` | raw `Session` | 200 |
| `GET /api/speakers` | raw `List<Speaker>` | 200 |
| `POST /api/speakers` | `{"data": Speaker}` | 200 |

No `@ControllerAdvice`, `@ExceptionHandler`, or any global exception handler class exists anywhere in the codebase. Spring Boot's default `/error` endpoint is in effect, which can surface stack traces in JSON error bodies.

### Architecture and Layers Affected

- **Controller layer** — all 4 controller files: response shape normalization, POST status code correction, Swagger `@ApiResponse` annotation updates.
- **Service layer** — `ConferenceService`, `SessionService`, `RegistrationService`: 7 bare `.get()` calls replaced; 1 `IllegalArgumentException` replaced with a typed exception.
- **Exception handling layer** — net-new: a `@ControllerAdvice` global exception handler class does not exist and must be introduced.

### Integration Points

- **springdoc-openapi-ui 1.7.0** — all controllers carry `@ApiResponses` annotations documenting response codes. Changes to actual response codes (e.g., 200 → 201) and response shapes will need matching Swagger annotation updates.
- **Frontend `api.js`** — multiple functions use `data.data || data` guards to cope with the current mixed envelope pattern (documented in `docs/architecture.md` §5.4 and `docs/tech-debt-audit.md` TD-003). Backend shape changes are not automatically reflected in the frontend.
- **`WebConfig.java`** — global CORS config; no changes required for this task.
- **`OpenApiConfig.java`** — Swagger UI base config; no changes required.

### Patterns and Conventions

- **Constructor injection** throughout all services and controllers — no field injection or `@Autowired`.
- **No common base class** for controllers or services; each is a standalone `@RestController` / `@Service` bean.
- **Inline response building** — `POST`/`GET` wrappers currently use `new HashMap<>()` with `response.put("data", ...)` directly inside controller methods. There is no shared wrapper class or generic record today.
- **`ResponseEntity`** is used consistently as the return type for all controller methods.
- **`@Tag` + `@Operation` + `@ApiResponses`** springdoc annotations are present on every endpoint; any status code change requires updating the `responseCode` string in the matching `@ApiResponse`.

---

## 3. Documentation Findings

### Guides and Architecture Docs

No `.ai-run/guides/` directory exists — conventions derived from code exploration.

`docs/architecture.md` — comprehensive reference covering ER diagram, package-to-responsibility table, all 6 controller→service→repository call chains, and the full REST API surface (13 endpoints). Section 5.4 explicitly notes: "Several functions do `data.data || data` to handle inconsistent response shapes from the backend."

`docs/tech-debt-audit.md` — authoritative audit dated 2026-09-06. Items TD-001 through TD-004 map directly to this task's four requirements:

- **TD-001** (High): 7 unchecked `Optional.get()` calls → 500 instead of 404. Lists exact file and line numbers.
- **TD-002** (High): `IllegalArgumentException` → 500 instead of 400. `RegistrationService.java:47`, `RegistrationController.java` Swagger annotation says 400.
- **TD-003** (Medium): Mixed response envelope — half endpoints wrap in `{"data":…}`, half return raw objects. Lists exact controller file and line numbers.
- **TD-004** (Low): All 4 creation endpoints return `200 OK`, should be `201 Created`. Lists exact controller file and line numbers.

### Architectural Decisions

No ADRs or inline architectural markers (`NOTE:`, `HACK:`, `ADR:`, `DECISION:`) were found in any source file. The tech-debt audit is the closest recorded decision document for this domain.

### Derived Conventions

- All new exception types should extend or use `ResponseStatusException` or be handled by a `@ControllerAdvice` that maps to `ResponseEntity` — the audit's suggested fix for both TD-001 and TD-002 proposes `ResponseStatusException` directly.
- No enum or constant class exists for HTTP error message keys; any new error response DTO would be the first such type.
- No profile-based configuration exists (`application-dev.properties`, `application-prod.properties`); `application.properties` is the single config file.

---

## 4. Testing Landscape

### Existing Coverage

- `backend/src/test/java/com/brownevents/app/service/ConferenceServiceTest.java` — 5 tests covering `getAllConferences` filtering variants and `createConference`. Uses pure Mockito, no Spring context. Does not assert response status codes or response shapes.
- `backend/src/test/java/com/brownevents/app/service/SessionServiceTest.java` — 2 tests covering `getSession` and `createSession`. Pure Mockito. No error path tested.
- `backend/src/test/java/com/brownevents/app/controller/ConferenceControllerTest.java` — 1 test using `@WebMvcTest` + `MockMvc`. Tests that `GET /api/conferences` with ISO date params returns `status().isOk()`. Does not assert response body shape or 201 status for POST.

No test files exist for `SessionController`, `SpeakerController`, or `RegistrationController`.

### Testing Framework and Patterns

- **Framework**: JUnit 5 (`@ExtendWith(MockitoExtension.class)`) for service tests; `@WebMvcTest` with `MockMvc` for the one controller test.
- **Mocking**: `@Mock` / `@InjectMocks` (Mockito) for service tests; `@MockBean` for controller tests.
- **No integration tests**, no `@SpringBootTest`, no database fixture setup.
- **No test utilities** or shared factories.

### Coverage Gaps

- No test covers any 404 path (missing entity); the 7 `.get()` error paths are entirely untested.
- No test covers the `IllegalArgumentException` / 400 path in `deleteRegistration`.
- No test asserts response body shape for any endpoint.
- No test asserts 201 status for any POST endpoint.
- `SessionController`, `SpeakerController`, and `RegistrationController` have zero test coverage.
- No test exists for the error response structure that the new `@ControllerAdvice` will produce.

---

## 5. Configuration and Environment

### Environment Variables

No environment variables are used for the feature area. `application.properties` uses hardcoded literal values for all configuration. No `JAVA_OPTS`, `SPRING_PROFILES_ACTIVE`, or error-handling-specific properties are set.

`server.error.include-message` and `server.error.include-stacktrace` are not set; Spring Boot 2.7's defaults suppress the `message` field by default but the `/error` endpoint still exposes a structured JSON body that callers can reach without a custom error handler.

### Configuration Files

- `backend/src/main/resources/application.properties` — sole config file. Relevant to this task: no `server.error.*` properties; no profile separation.

### Feature Flags and Deployment Concerns

No feature flags or runtime toggles exist. No deployment manifests reference the error-handling domain. `docker-compose.yml` starts Postgres + backend:8080 + frontend:3000 but has no error-handling-specific configuration.

---

## 6. Risk Indicators

- **Frontend breakage**: `frontend/src/api.js` uses `data.data || data` in multiple functions to cope with the current mixed shapes. If the backend adopts a uniform envelope, those guards change meaning. The task scope appears to be backend-only; frontend is an undocumented dependency of this change.
- **Swagger annotation drift**: 13 endpoints carry `@ApiResponse(responseCode = "200", ...)` for creation endpoints that will become 201. If only the `ResponseEntity` call is updated without updating the Swagger annotations, the API documentation will still show 200 — a silent inconsistency.
- **No existing 404 tests**: The 7 `Optional.get()` → `orElseThrow` replacements produce new behavior (404 with a JSON body) that has zero existing test coverage to run against. The task requires at least one test update, but the untested paths are in 3 service files touching all major entity types.
- **`@ControllerAdvice` is net-new**: No exception handler class or base class exists to model from. The pattern is standard Spring Boot but is the only novel structural file this task introduces.
- **`IllegalArgumentException` in service layer**: `RegistrationService.deleteRegistration` throws `IllegalArgumentException`. If a `@ControllerAdvice` maps this exception type globally to 400, other unanticipated `IllegalArgumentException` sources could accidentally start returning 400 instead of 500.
- **`ConferenceService.updateConference` NullPointerException**: Line 48 calls `conf.getStatus().toUpperCase()` — if `status` is null in the request body, this throws `NullPointerException` before the `Optional.get()` is even reached, surfacing as an unhandled 500. Noted in TD-011 of the audit.
- **Only one controller test class exists**: `ConferenceControllerTest` is `@WebMvcTest` — updating it to assert 201 and response shape will require adding `content()` matchers. The three untested controllers have no test class to update.

---

## 7. Summary for Complexity Assessment

This task touches three distinct layers: the controller layer (4 files, 13 endpoint methods, response shape and status code changes), the service layer (3 files, 8 error-throwing call sites), and introduces one net-new infrastructure class (a `@ControllerAdvice` global exception handler). The change surface is moderately wide — every controller and most service classes are affected — but the changes within each file are mechanical substitutions (`.get()` → `.orElseThrow()`, `ResponseEntity.ok()` → `ResponseEntity.status(CREATED).body()`, inline `HashMap` wrappers removed or standardized). The highest structural novelty is the `@ControllerAdvice` class, which has no predecessor in the codebase to model from but is a well-established Spring Boot pattern.

Test coverage for the affected code paths is thin. The existing `ConferenceControllerTest` provides a single `@WebMvcTest` starting point that satisfies requirement 5's "at least one existing test updated," but it only checks `isOk()` with no body assertions. None of the three remaining controllers have any test class. The 7 `Optional.get()` error paths and the `IllegalArgumentException` error path are completely untested, meaning the new behavior in those paths has no regression baseline. Adding meaningful tests for the new error response structure will require either extending the existing `@WebMvcTest` class or creating new ones for `SessionController`, `SpeakerController`, and `RegistrationController`.

The principal risk factor is the frontend coupling: `api.js` uses `data.data || data` guards that are a direct symptom of the current shape inconsistency. A backend-only normalization without a corresponding frontend update will either break the `data.data` path (if bare objects are now enveloped) or break the `|| data` fallback (if envelopes are now bare). The task requirements do not mention the frontend, so this dependency should be surfaced to the planner for explicit scoping. Within the backend boundary, the task is well-documented (TD-001 through TD-004 in the tech-debt audit provide line-level specifics) and does not require any schema migration, new dependency, or infrastructure change.

---

## 8. External References

None named by the task.
