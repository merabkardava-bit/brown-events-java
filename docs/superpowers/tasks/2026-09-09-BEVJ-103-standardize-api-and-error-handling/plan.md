# BEVJ-103: Standardize API Responses and Error Handling — Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Normalize the BrownEvents Spring Boot backend so every non-204 endpoint returns a `{"data": <payload>}` envelope, POST endpoints return 201, and all error paths return structured `{"status","error","message"}` JSON with correct HTTP codes — never a stack trace.

**Architecture:** A new generic `ApiResponse<T>` wrapper replaces ad-hoc `HashMap` constructions in controllers. A new `GlobalExceptionHandler` (`@ControllerAdvice`) intercepts `ResourceNotFoundException` (→ 404), `RegistrationMismatchException` (→ 400), and all other exceptions (→ 500 with a safe, fixed message). Seven bare `.get()` calls and one `IllegalArgumentException` in the service layer are replaced with `.orElseThrow()` using the typed custom exceptions.

**Tech Stack:** Java 11, Spring Boot 2.7.14, JUnit 5 + Mockito, `@WebMvcTest` + MockMvc.

**Spec:** `docs/superpowers/tasks/2026-09-09-BEVJ-103-standardize-api-and-error-handling/spec.md`

## Global Constraints

- Java 11 — no records, no `var` in lambdas, no text blocks
- Spring Boot 2.7.14 — new tests use `@WebMvcTest` slices + Mockito; no `@SpringBootTest`
- No new Maven dependencies
- `application.properties` unchanged
- Frontend `api.js` not in scope (follow-up ticket)
- Paginated `GET` responses `{data:[…], page, size, totalElements, totalPages}` keep their existing `HashMap` construction — they already satisfy the `data` key contract
- `@CrossOrigin` duplication cleanup out of scope
- No new endpoints or domain model changes

---

### Task 1: Create exception types and response DTOs

**Files:**
- Create: `backend/src/main/java/com/brownevents/app/exception/ResourceNotFoundException.java`
- Create: `backend/src/main/java/com/brownevents/app/exception/RegistrationMismatchException.java`
- Create: `backend/src/main/java/com/brownevents/app/ApiResponse.java`
- Create: `backend/src/main/java/com/brownevents/app/ErrorResponse.java`

**Interfaces:**
- Produces: `ResourceNotFoundException(String message)` and `RegistrationMismatchException(String message)` in package `com.brownevents.app.exception`; `ApiResponse<T>(T data)` with `T getData()` and `ErrorResponse(int status, String error, String message)` with three getters in package `com.brownevents.app`

**Test-first: no** — simple value types with no logic.

- [ ] **Step 1: Create `ResourceNotFoundException`**

```java
package com.brownevents.app.exception;

public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) {
        super(message);
    }
}
```

- [ ] **Step 2: Create `RegistrationMismatchException`**

```java
package com.brownevents.app.exception;

public class RegistrationMismatchException extends RuntimeException {
    public RegistrationMismatchException(String message) {
        super(message);
    }
}
```

- [ ] **Step 3: Create `ApiResponse<T>`**

```java
package com.brownevents.app;

public class ApiResponse<T> {
    private final T data;

    public ApiResponse(T data) {
        this.data = data;
    }

    public T getData() {
        return data;
    }
}
```

- [ ] **Step 4: Create `ErrorResponse`**

```java
package com.brownevents.app;

public class ErrorResponse {
    private final int status;
    private final String error;
    private final String message;

    public ErrorResponse(int status, String error, String message) {
        this.status = status;
        this.error = error;
        this.message = message;
    }

    public int getStatus()    { return status; }
    public String getError()   { return error; }
    public String getMessage() { return message; }
}
```

- [ ] **Step 5: Compile**

Run from `backend/`: `mvn compile -q`
Expected: BUILD SUCCESS with no errors.

- [ ] **Step 6: Commit**

Commit the four new files.

---

### Task 2: Create `GlobalExceptionHandler`

**Files:**
- Create: `backend/src/test/java/com/brownevents/app/GlobalExceptionHandlerTest.java`
- Create: `backend/src/main/java/com/brownevents/app/GlobalExceptionHandler.java`

**Interfaces:**
- Consumes: `ResourceNotFoundException`, `RegistrationMismatchException`, `ErrorResponse` from Task 1
- Produces: `GlobalExceptionHandler` `@ControllerAdvice` — Spring Boot auto-applies it to all `@WebMvcTest` slices once it exists in the component scan path

**Test-first: yes** — write the test class first, confirm it fails to compile, then implement.

- [ ] **Step 1: Write `GlobalExceptionHandlerTest`**

```java
package com.brownevents.app;

import com.brownevents.app.exception.RegistrationMismatchException;
import com.brownevents.app.exception.ResourceNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = GlobalExceptionHandlerTest.TestController.class)
@Import(GlobalExceptionHandler.class)
public class GlobalExceptionHandlerTest {

    @Autowired
    private MockMvc mockMvc;

    @RestController
    static class TestController {
        @GetMapping("/test/not-found")
        public void throwNotFound() {
            throw new ResourceNotFoundException("Conference not found");
        }
        @GetMapping("/test/mismatch")
        public void throwMismatch() {
            throw new RegistrationMismatchException("Registration does not belong to this conference");
        }
        @GetMapping("/test/unexpected")
        public void throwUnexpected() {
            throw new RuntimeException("secret internal details");
        }
    }

    @Test
    public void notFound_shouldReturn404WithErrorShape() throws Exception {
        mockMvc.perform(get("/test/not-found"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("Conference not found"));
    }

    @Test
    public void mismatch_shouldReturn400WithErrorShape() throws Exception {
        mockMvc.perform(get("/test/mismatch"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value("Registration does not belong to this conference"));
    }

    @Test
    public void unexpectedException_shouldReturn500WithSafeMessage() throws Exception {
        mockMvc.perform(get("/test/unexpected"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.error").value("Internal Server Error"))
                .andExpect(jsonPath("$.message").value("An unexpected error occurred"));
    }

    @Test
    public void unexpectedException_shouldNotExposeInternalMessage() throws Exception {
        // "secret internal details" must not appear anywhere in the response
        mockMvc.perform(get("/test/unexpected"))
                .andExpect(content().string(org.hamcrest.Matchers.not(
                        org.hamcrest.Matchers.containsString("secret internal details"))));
    }
}
```

- [ ] **Step 2: Run — expect compile failure**

Run from `backend/`: `mvn test -Dtest=GlobalExceptionHandlerTest -q`
Expected: compilation error — `GlobalExceptionHandler` does not yet exist.

- [ ] **Step 3: Create `GlobalExceptionHandler`**

```java
package com.brownevents.app;

import com.brownevents.app.exception.RegistrationMismatchException;
import com.brownevents.app.exception.ResourceNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(ResourceNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ErrorResponse(404, "Not Found", ex.getMessage()));
    }

    @ExceptionHandler(RegistrationMismatchException.class)
    public ResponseEntity<ErrorResponse> handleMismatch(RegistrationMismatchException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse(400, "Bad Request", ex.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUnexpected(Exception ex) {
        log.error("Unexpected error: {}", ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse(500, "Internal Server Error", "An unexpected error occurred"));
    }
}
```

- [ ] **Step 4: Run — expect PASS**

Run from `backend/`: `mvn test -Dtest=GlobalExceptionHandlerTest -q`
Expected: BUILD SUCCESS, 4 tests pass.

- [ ] **Step 5: Commit**

Commit `GlobalExceptionHandler.java` and `GlobalExceptionHandlerTest.java`.

---

### Task 3: Update service layer (8 call sites)

**Files:**
- Modify: `backend/src/main/java/com/brownevents/app/service/SessionService.java:20-38`
- Modify: `backend/src/main/java/com/brownevents/app/service/ConferenceService.java:33-59`
- Modify: `backend/src/main/java/com/brownevents/app/service/RegistrationService.java:29-49`
- Modify: `backend/src/test/java/com/brownevents/app/service/SessionServiceTest.java`

**Interfaces:**
- Consumes: `ResourceNotFoundException`, `RegistrationMismatchException` from Task 1
- Produces: services throw typed exceptions instead of `NoSuchElementException` / `IllegalArgumentException`

**Test-first: yes** — add the failing test to `SessionServiceTest` before editing the service.

- [ ] **Step 1: Add the failing test to `SessionServiceTest`**

Add this method inside the existing `SessionServiceTest` class (existing imports already cover `Optional` and `times`):

```java
@Test
public void getSession_notFound_shouldThrowResourceNotFoundException() {
    when(sessionRepository.findById(99L)).thenReturn(Optional.empty());

    org.junit.jupiter.api.Assertions.assertThrows(
            com.brownevents.app.exception.ResourceNotFoundException.class,
            () -> sessionService.getSession(99L)
    );
    verify(sessionRepository, times(1)).findById(99L);
}
```

- [ ] **Step 2: Run — expect FAIL**

Run from `backend/`: `mvn test -Dtest=SessionServiceTest#getSession_notFound_shouldThrowResourceNotFoundException -q`
Expected: FAIL — `NoSuchElementException` is thrown, not `ResourceNotFoundException`.

- [ ] **Step 3: Update `SessionService.java`**

Add import `import com.brownevents.app.exception.ResourceNotFoundException;`.

Replace the three bare `.get()` calls:
- Line 21 `conferenceRepository.findById(conferenceId).get()` → `.orElseThrow(() -> new ResourceNotFoundException("Conference not found"))`
- Line 27 `sessionRepository.findById(id).get()` → `.orElseThrow(() -> new ResourceNotFoundException("Session not found"))`
- Line 31 `sessionRepository.findById(id).get()` → `.orElseThrow(() -> new ResourceNotFoundException("Session not found"))`

- [ ] **Step 4: Update `ConferenceService.java`**

Add import `import com.brownevents.app.exception.ResourceNotFoundException;`.

Replace the three bare `.get()` calls:
- Line 34 `conferenceRepository.findById(id).get()` → `.orElseThrow(() -> new ResourceNotFoundException("Conference not found"))`
- Line 42 `conferenceRepository.findById(id).get()` → `.orElseThrow(() -> new ResourceNotFoundException("Conference not found"))`
- Line 57 `conferenceRepository.findById(conferenceId).get()` → `.orElseThrow(() -> new ResourceNotFoundException("Conference not found"))`

- [ ] **Step 5: Update `RegistrationService.java`**

Add imports `import com.brownevents.app.exception.ResourceNotFoundException;` and `import com.brownevents.app.exception.RegistrationMismatchException;`.

- Line 32: `conferenceRepository.findById(conferenceId).get()` → `.orElseThrow(() -> new ResourceNotFoundException("Conference not found"))`
- Line 47: `throw new IllegalArgumentException("Registration not found for this conference")` → `throw new RegistrationMismatchException("Registration does not belong to this conference")`

- [ ] **Step 6: Run all service tests — expect PASS**

Run from `backend/`: `mvn test -Dtest=SessionServiceTest,ConferenceServiceTest -q`
Expected: BUILD SUCCESS, all 8 tests pass (5 in `ConferenceServiceTest`, 3 in `SessionServiceTest`).

- [ ] **Step 7: Commit**

Commit the three modified service files and the updated `SessionServiceTest.java`.

---

### Task 4: Update all four controllers and `ConferenceControllerTest`

**Files:**
- Modify: `backend/src/test/java/com/brownevents/app/controller/ConferenceControllerTest.java`
- Modify: `backend/src/main/java/com/brownevents/app/controller/ConferenceController.java`
- Modify: `backend/src/main/java/com/brownevents/app/controller/SessionController.java`
- Modify: `backend/src/main/java/com/brownevents/app/controller/SpeakerController.java`
- Modify: `backend/src/main/java/com/brownevents/app/controller/RegistrationController.java`

**Interfaces:**
- Consumes: `ApiResponse<T>` from Task 1; `GlobalExceptionHandler` from Task 2 (auto-applied by Spring to all `@WebMvcTest` slices)

**Test-first: yes** — add the failing controller test first, then update `ConferenceController`, then the remaining three.

- [ ] **Step 1: Add the failing test to `ConferenceControllerTest`**

Add these imports to `ConferenceControllerTest`:
```java
import com.brownevents.app.entity.Conference;
import org.springframework.http.MediaType;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
```

Add this test method inside the class:
```java
@Test
public void createConference_shouldReturn201WithDataEnvelope() throws Exception {
    Conference created = new Conference();
    created.setId(1L);
    created.setTitle("Brown Tech Summit 2025");

    when(conferenceService.createConference(any(Conference.class))).thenReturn(created);

    mockMvc.perform(post("/api/conferences")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"title\":\"Brown Tech Summit 2025\",\"status\":\"UPCOMING\"}"))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.data.title").value("Brown Tech Summit 2025"));
}
```

- [ ] **Step 2: Run — expect FAIL**

Run from `backend/`: `mvn test -Dtest=ConferenceControllerTest#createConference_shouldReturn201WithDataEnvelope -q`
Expected: FAIL — controller currently returns 200 with an unwrapped body.

- [ ] **Step 3: Update `ConferenceController.java`**

Add `import com.brownevents.app.ApiResponse;` and `import org.springframework.http.HttpStatus;`. Remove `HashMap` and `Map` imports (they are used only by `createSession`'s wrapping, which is being replaced).

Four methods to change:

1. `getConference` — return type `ResponseEntity<ApiResponse<Conference>>`, body `ResponseEntity.ok(new ApiResponse<>(conferenceService.getConference(id)))`.

2. `createConference` — return type `ResponseEntity<ApiResponse<Conference>>`, body `ResponseEntity.status(HttpStatus.CREATED).body(new ApiResponse<>(conferenceService.createConference(conference)))`. Change `@ApiResponse(responseCode = "200", ...)` annotation on the POST to `responseCode = "201"`.

3. `updateConference` — return type `ResponseEntity<ApiResponse<Conference>>`, body `ResponseEntity.ok(new ApiResponse<>(conferenceService.updateConference(id, conference)))`.

4. `createSession` — return type `ResponseEntity<ApiResponse<Session>>`, replace `HashMap` block with `ResponseEntity.status(HttpStatus.CREATED).body(new ApiResponse<>(conferenceService.createSession(id, session)))`. Change `@ApiResponse(responseCode = "200", ...)` on the POST to `responseCode = "201"`.

The two paginated `GET` methods (`getAllConferences`, `getConferenceSessions`) are unchanged — they keep their existing `HashMap` construction which already contains a `data` key.

- [ ] **Step 4: Run the controller test — expect PASS**

Run from `backend/`: `mvn test -Dtest=ConferenceControllerTest -q`
Expected: BUILD SUCCESS, both tests pass.

- [ ] **Step 5: Update `SessionController.java`**

Add `import com.brownevents.app.ApiResponse;`.

- `getSession` — return type `ResponseEntity<ApiResponse<Session>>`, body `ResponseEntity.ok(new ApiResponse<>(sessionService.getSession(id)))`.
- `updateSession` — return type `ResponseEntity<ApiResponse<Session>>`, body `ResponseEntity.ok(new ApiResponse<>(sessionService.updateSession(id, session)))`.

No Swagger annotation changes needed (both are GET/PUT returning 200).

- [ ] **Step 6: Update `SpeakerController.java`**

Add `import com.brownevents.app.ApiResponse;` and `import org.springframework.http.HttpStatus;`. Remove `HashMap` and `Map` imports.

- `getAllSpeakers` — return type `ResponseEntity<ApiResponse<List<Speaker>>>`, body `ResponseEntity.ok(new ApiResponse<>(speakerService.getAllSpeakers()))`.
- `createSpeaker` — return type `ResponseEntity<ApiResponse<Speaker>>`, replace `HashMap` block with `ResponseEntity.status(HttpStatus.CREATED).body(new ApiResponse<>(speakerService.createSpeaker(speaker)))`. Change `@ApiResponse(responseCode = "200", ...)` to `responseCode = "201"`.

- [ ] **Step 7: Update `RegistrationController.java`**

Add `import com.brownevents.app.ApiResponse;` and `import org.springframework.http.HttpStatus;`. Remove `HashMap` and `Map` imports.

- `registerAttendee` — return type `ResponseEntity<ApiResponse<Registration>>`, replace `HashMap` block with `ResponseEntity.status(HttpStatus.CREATED).body(new ApiResponse<>(registrationService.registerAttendee(id, attendee)))`. Change `@ApiResponse(responseCode = "200", ...)` to `responseCode = "201"`.
- `getRegistrations` — return type `ResponseEntity<ApiResponse<List<Registration>>>`, body `ResponseEntity.ok(new ApiResponse<>(registrationService.getRegistrations(id)))`.
- `deleteRegistration` — **unchanged** (already returns `ResponseEntity.noContent().build()` with 204).

- [ ] **Step 8: Run all tests — expect PASS**

Run from `backend/`: `mvn test -q`
Expected: BUILD SUCCESS, all tests pass.

- [ ] **Step 9: Commit**

Commit all four controller files and `ConferenceControllerTest.java`.

---

## Negative-constraint audit

| Non-goal | Honored by |
|---|---|
| No frontend changes | No task touches `frontend/` |
| No new endpoints or domain model changes | Tasks add types only; controller signatures change shape not routes |
| Room API controller stays out of scope | No task references Room |
| No integration tests / `@SpringBootTest` | All new tests use `@WebMvcTest` + Mockito |
| Paginated responses structurally unchanged | Task 4 Step 3 explicitly preserves HashMap construction in `getAllConferences` / `getConferenceSessions` |
| `application.properties` unchanged | No task modifies config files |
| `@CrossOrigin` duplication stays | No task removes or changes `@CrossOrigin` |
| Catch-all must never expose `ex.getMessage()` | Task 2 handler logs the message, returns fixed string; Task 2 test Step 1 asserts the safe string and absence of internal details |
