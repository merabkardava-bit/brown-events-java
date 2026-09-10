# BEVJ-103: Standardize API Responses and Error Handling

**Branch**: BEVJ-103-standardize-api-and-error-handling  
**Complexity**: L (21/36) — estimated 4–5 days  
**Generated**: 2026-09-09

---

## Overview

Normalize the BrownEvents Spring Boot backend so every endpoint returns a predictable JSON shape,
POST endpoints return 201, all failure scenarios map to correct HTTP status codes, and error
responses are structured and safe — never exposing a stack trace or internal exception message.

The change spans three layers: controller (4 files, response shape and status codes), service
(3 files, 8 error-throwing call sites), and a net-new `@ControllerAdvice` global exception handler.

---

## Goals

1. Uniform `{"data": <payload>}` envelope for all non-204 responses.
2. All four POST creation endpoints return `201 Created`.
3. A `GlobalExceptionHandler` (`@ControllerAdvice`) replaces Spring Boot's default `/error` behavior.
4. Missing entities → 404, invalid requests → 400, unexpected errors → 500; all structured JSON.
5. `ConferenceControllerTest` updated to assert the new response shape on at least one POST endpoint.

---

## Non-Goals

- **Frontend changes**: `api.js` `data.data || data` guards are a known coupling point (TD-003);
  updating them is a separate follow-up ticket, not part of this task.
- New endpoints or domain model changes.
- Room API controller (no REST controller exists today; remains out of scope).
- Integration tests or test database setup.
- Pagination structure changes: paginated responses already use `{data:[…], page, size,
  totalElements, totalPages}` and remain unchanged structurally.
- `application.properties` `server.error.*` properties — the `@ControllerAdvice` renders these moot.
- `@CrossOrigin(origins = "*")` duplication cleanup — separate concern, separate ticket.

---

## Response Shape Normalization

### Target shapes after normalization

| Endpoint | New shape | New status |
|---|---|---|
| `GET /api/conferences` | `{data:[…], page, size, totalElements, totalPages}` | 200 |
| `GET /api/conferences/{id}` | `{"data": Conference}` | 200 |
| `POST /api/conferences` | `{"data": Conference}` | **201** |
| `PUT /api/conferences/{id}` | `{"data": Conference}` | 200 |
| `GET /api/conferences/{id}/sessions` | `{data:[…], page, size, totalElements, totalPages}` | 200 |
| `POST /api/conferences/{id}/sessions` | `{"data": Session}` | **201** |
| `POST /api/conferences/{id}/register` | `{"data": Registration}` | **201** |
| `GET /api/conferences/{id}/registrations` | `{"data": [Registration…]}` | 200 |
| `DELETE /api/conferences/{id}/registrations/{rid}` | (empty) | 204 — unchanged |
| `GET /api/sessions/{id}` | `{"data": Session}` | 200 |
| `PUT /api/sessions/{id}` | `{"data": Session}` | 200 |
| `GET /api/speakers` | `{"data": [Speaker…]}` | 200 |
| `POST /api/speakers` | `{"data": Speaker}` | **201** |

### ApiResponse wrapper

A shared generic `ApiResponse<T>` class with a single `data` field of type `T` replaces the
inline `new HashMap<>()` pattern currently used in three endpoints
(`controller/ConferenceController.java`, `controller/RegistrationController.java`,
`controller/SpeakerController.java`). The wrapper is minimal — one field only, no timestamp, no
status code, no message — so it is suitable for all payload types including lists.

Paginated endpoints already produce a map with a `data` key alongside pagination metadata. They
may use `ApiResponse` with the page object as the payload, or keep their existing map construction
— both satisfy the `data` field contract. The spec does not mandate which form they use internally.

---

## Error Handling

### Error response shape

A separate `ErrorResponse` DTO with three fields:

```
{
  "status":  <int>,    // HTTP status code, e.g. 404
  "error":   "<str>",  // short reason phrase, e.g. "Not Found"
  "message": "<str>"   // human-readable detail, e.g. "Conference not found"
}
```

No `timestamp`, `path`, `exception` class name, or stack trace. The catch-all handler must not
include `ex.getMessage()` in the response body for unhandled exceptions — the message is logged at
ERROR level, not returned to the caller.

### GlobalExceptionHandler

New class `GlobalExceptionHandler` in `com.brownevents.app` (root package, alongside `WebConfig`),
annotated `@ControllerAdvice`. Three handler methods:

| Exception caught | HTTP status | `message` value |
|---|---|---|
| `ResourceNotFoundException` | 404 | The exception's own message, e.g. `"Conference not found"` |
| `RegistrationMismatchException` | 400 | The exception's own message |
| `Exception` (catch-all) | 500 | `"An unexpected error occurred"` — never `ex.getMessage()` |

### Custom exception types

New sub-package `com.brownevents.app.exception` containing two classes:

**`ResourceNotFoundException extends RuntimeException`**  
One constructor: `ResourceNotFoundException(String message)`. Used for all entity-not-found cases.

**`RegistrationMismatchException extends RuntimeException`**  
One constructor: `RegistrationMismatchException(String message)`. Used when a registration does
not belong to the requested conference, replacing the current `IllegalArgumentException` in
`RegistrationService`. A typed exception avoids accidentally mapping unrelated
`IllegalArgumentException` sources to 400.

---

## Service Layer Changes

Eight call sites replaced across three service files (resolves TD-001 and TD-002 from the
tech-debt audit):

**`ConferenceService.java`** — lines 34, 42, 57: three bare `.get()` calls replaced with
`.orElseThrow(() -> new ResourceNotFoundException("Conference not found"))`.

**`SessionService.java`** — lines 21, 27, 31: three bare `.get()` calls replaced with
`.orElseThrow(() -> new ResourceNotFoundException("Session not found"))`.

**`RegistrationService.java`** — line 32: bare `.get()` replaced with
`.orElseThrow(() -> new ResourceNotFoundException("Conference not found"))`. Line 47:
`throw new IllegalArgumentException(...)` replaced with
`throw new RegistrationMismatchException("Registration does not belong to this conference")`.

---

## Controller Layer Changes

For each of the four controller files:

- All single-entity GET and PUT methods: `ResponseEntity.ok(new ApiResponse<>(entity))`.
- All POST methods: `ResponseEntity.status(HttpStatus.CREATED).body(new ApiResponse<>(entity))`.
- List GET methods that currently return raw lists: `ResponseEntity.ok(new ApiResponse<>(list))`.
- DELETE endpoint: unchanged — already `ResponseEntity.noContent().build()` with 204.
- Inline `new HashMap<>()` constructions removed; `ApiResponse` used in their place.
- Swagger `@ApiResponse(responseCode = "200", ...)` annotations on the four POST methods updated
  to `responseCode = "201"`.

---

## Test Changes

**`ConferenceControllerTest`** (the file satisfying requirement 5) is updated with at least one
new test for `POST /api/conferences` that asserts:
- `status().isCreated()` (HTTP 201)
- `jsonPath("$.data.name").value(...)` — confirming the `{"data": ...}` envelope

The existing `GET /api/conferences` test currently only asserts `status().isOk()` with no body
assertions, so it requires no change unless new body matchers are added. The test class already
uses `@WebMvcTest` + `MockMvc` + `@MockBean`; no new testing infrastructure is needed.

A test for the `GlobalExceptionHandler` (asserting the 404 and 400 error shapes) is encouraged
but not required by the task specification. If added, it fits naturally in a new
`GlobalExceptionHandlerTest` using `@WebMvcTest`.

---

## Acceptance Criteria

1. All 12 non-204 endpoints return a JSON body whose top-level structure contains a `data` field.
2. `POST /api/conferences`, `POST /api/conferences/{id}/sessions`,
   `POST /api/conferences/{id}/register`, and `POST /api/speakers` return HTTP 201.
3. Requesting a non-existent conference or session returns HTTP 404 with body
   `{"status":404,"error":"Not Found","message":"<entity> not found"}` — no stack trace.
4. Deleting a registration that does not belong to the given conference returns HTTP 400 with a
   structured error body — no stack trace.
5. An unhandled runtime exception returns HTTP 500 with body
   `{"status":500,"error":"Internal Server Error","message":"An unexpected error occurred"}` —
   the internal exception message is absent from the response.
6. `ConferenceControllerTest` contains at least one test asserting `status().isCreated()` and
   a `$.data` JSON path match for a POST endpoint.
7. All four POST endpoint Swagger `@ApiResponse` annotations show `responseCode = "201"`.
8. `application.properties` is unchanged (no `server.error.*` properties added or modified).

---

## Open Risks

- **Frontend breakage (deploy blocker)**: `api.js` uses `data.data || data` guards in multiple
  functions as a workaround for the current mixed shapes (documented TD-003). After this change,
  previously-bare responses become `{"data": entity}`. `data.data` now correctly resolves where
  it previously fell through to `|| data`. Functions already targeting `{"data": ...}` responses
  are unaffected. A frontend audit and update is required before deploying to production.
- **`ConferenceService.updateConference` null status** (TD-011): `conf.getStatus().toUpperCase()`
  NPEs when `status` is null in the request body. The catch-all handler converts this to a 500
  with a safe message — an improvement over the current raw stack trace — but the root cause
  remains. A separate ticket is recommended.
- **No regression baseline for 404 paths**: The 7 `Optional.get()` replacements produce new
  behavior that has zero existing test coverage. Tests for the new error paths are encouraged
  alongside this task to establish a regression baseline.
