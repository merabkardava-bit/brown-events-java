# BEVJ-201 Date Filter Exception Fix Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Fix `MethodArgumentTypeMismatchException` when a user picks from/to dates in the conference filter UI by adding `@DateTimeFormat(iso = DateTimeFormat.ISO.DATE)` to both `LocalDate` request parameters in `ConferenceController`, and add a `@WebMvcTest` controller test to prevent regression.

**Architecture:** Single annotation fix at the HTTP binding layer in `ConferenceController.getAllConferences`. No service, repository, entity, or frontend changes. A new `ConferenceControllerTest` using `@WebMvcTest` exercises the binding layer directly with `MockMvc`.

**Tech Stack:** Java 11, Spring Boot 2.7.14, Spring Framework 5.3.x, JUnit 5, Mockito, `@WebMvcTest` / `MockMvc`

## Global Constraints

- Fix only the HTTP binding layer — no changes to service, repository, entity, or frontend.
- Use Option A (annotation on parameters), not Option B (global property).
- Import `org.springframework.format.annotation.DateTimeFormat`.
- New test uses `@WebMvcTest(ConferenceController.class)` with `MockMvc`; no `@SpringBootTest`.
- Commit per task using the repository's existing convention.

---

### Task 1: Add `@DateTimeFormat` to `LocalDate` params and cover with `@WebMvcTest`

**Files:**
- Modify: `backend/src/main/java/com/brownevents/app/controller/ConferenceController.java:63-64`
- Create: `backend/src/test/java/com/brownevents/app/controller/ConferenceControllerTest.java`

**Interfaces:**
- Consumes: `ConferenceService.getAllConferences(Pageable, String, LocalDate, LocalDate, String)` — already correct, no changes.
- Produces: `GET /api/conferences?from=<yyyy-MM-dd>&to=<yyyy-MM-dd>` resolves without `MethodArgumentTypeMismatchException`.

**Test-first: yes — `GET /api/conferences?from=2026-09-22&to=2026-12-31` returns HTTP 400 or 500 (binding failure) before the fix, and HTTP 200 after it.**

- [ ] **Step 1: Write the failing test**

Create `backend/src/test/java/com/brownevents/app/controller/ConferenceControllerTest.java`:

```java
package com.brownevents.app.controller;

import com.brownevents.app.service.ConferenceService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ConferenceController.class)
public class ConferenceControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ConferenceService conferenceService;

    @Test
    public void getAllConferences_withIsoDateParams_shouldReturn200() throws Exception {
        when(conferenceService.getAllConferences(
                any(Pageable.class), isNull(), any(LocalDate.class), any(LocalDate.class), isNull()))
                .thenReturn(new PageImpl<>(Collections.emptyList()));

        mockMvc.perform(get("/api/conferences")
                        .param("from", "2026-09-22")
                        .param("to", "2026-12-31"))
                .andExpect(status().isOk());
    }
}
```

- [ ] **Step 2: Run test to verify it fails**

```
cd backend
mvn test -Dtest=ConferenceControllerTest
```

Expected: FAIL — Spring cannot bind `"2026-09-22"` to `LocalDate`, resulting in a 400 or 500 response that does not satisfy `status().isOk()`.

- [ ] **Step 3: Add `@DateTimeFormat` to `ConferenceController`**

In `ConferenceController.java`, add the import after line 20 (`import java.time.LocalDate;`):

```java
import org.springframework.format.annotation.DateTimeFormat;
```

Then at lines 63–64, annotate both parameters:

```java
@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
```

No other lines in this file change.

- [ ] **Step 4: Run test to verify it passes**

```
cd backend
mvn test -Dtest=ConferenceControllerTest
```

Expected: PASS — MockMvc binds the ISO date strings, the stub returns an empty page, and the response is HTTP 200.

- [ ] **Step 5: Run the full test suite to confirm no regression**

```
cd backend
mvn test
```

Expected: all existing tests (`ConferenceServiceTest`, `SessionServiceTest`) plus the new `ConferenceControllerTest` pass.

---

## Negative-constraints pass

Requirements state: "No changes to service, repository, entity, or frontend."

- This plan touches only `ConferenceController.java` (two annotation additions and one import) and the new `ConferenceControllerTest.java`. No service, repository, entity, or frontend file is listed anywhere in the plan.
- No task introduces a global property change (`spring.mvc.format.date=iso`) — Option B is explicitly excluded and is not present.

**negative-constraints: honored — no task violates any stated restriction.**
