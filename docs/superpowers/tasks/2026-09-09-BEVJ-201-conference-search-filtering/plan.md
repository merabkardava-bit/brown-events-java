# BEVJ-201: Conference Search and Filtering — Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use `superpowers:subagent-driven-development` (recommended) or `superpowers:executing-plans` to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add keyword, date-range, and status filtering to `GET /api/conferences`, composing with existing pagination, and expose matching filter controls in the conference list UI.

**Architecture:** A new `findAllFiltered` JPQL method on `ConferenceRepository` uses `(:param IS NULL OR ...)` predicates to treat absent params as "no filter". `ConferenceService.getAllConferences` gains four optional params, formats the search term, and delegates to that method. The controller exposes four new `@RequestParam(required = false)` fields and parses `from`/`to` as `LocalDate`. The frontend adds filter state and a filter bar to `ConferenceListPage`, forwards params through `api.js`, and resets to page 0 on any filter change.

**Tech Stack:** Java 11, Spring Boot 2.7.14, Spring Data JPA / JPQL `@Query`, JUnit 5 + Mockito, React 18 / plain JSX.

## Global Constraints

- No new Maven or npm dependencies.
- All Maven commands run from `backend/`.
- JPQL `@Query` pattern only — no `JpaSpecificationExecutor` (no codebase precedent).
- Paginated `@Query` must include a separate `countQuery` (established pattern from `SessionRepository`).
- Response envelope shape (`data`, `page`, `size`, `totalElements`, `totalPages`) must not change.
- Do not fix `BASE_URL` hardcoding (FE-001) or the seeded `"ACTIVE"` status record (TD-011).
- Commit per task using the repository's existing convention.

---

### Task 1: ConferenceRepository — add `findAllFiltered`

**Files:**
- Modify: `backend/src/main/java/com/brownevents/app/repository/ConferenceRepository.java:1-9`

**Interfaces:**
- Produces: `Page<Conference> findAllFiltered(String search, LocalDate from, LocalDate to, String status, Pageable pageable)`

**Test-first: no** — the repository interface is mocked in unit tests; no repository-level tests exist or are required.

- [ ] Add imports (`Query`, `Param`, `Page`, `Pageable`, `LocalDate`) and the new method to `ConferenceRepository`:

```java
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDate;

@Query(value = "SELECT c FROM Conference c WHERE " +
    "(:search IS NULL OR LOWER(c.title) LIKE :search OR LOWER(c.description) LIKE :search) AND " +
    "(:from IS NULL OR c.startDate >= :from) AND " +
    "(:to IS NULL OR c.startDate <= :to) AND " +
    "(:status IS NULL OR UPPER(c.status) = UPPER(:status))",
    countQuery = "SELECT COUNT(c) FROM Conference c WHERE " +
    "(:search IS NULL OR LOWER(c.title) LIKE :search OR LOWER(c.description) LIKE :search) AND " +
    "(:from IS NULL OR c.startDate >= :from) AND " +
    "(:to IS NULL OR c.startDate <= :to) AND " +
    "(:status IS NULL OR UPPER(c.status) = UPPER(:status))")
Page<Conference> findAllFiltered(
    @Param("search") String search,
    @Param("from") LocalDate from,
    @Param("to") LocalDate to,
    @Param("status") String status,
    Pageable pageable);
```

- [ ] Compile: `mvn compile` — must succeed with no errors.

---

### Task 2: ConferenceService — update `getAllConferences` + unit tests

**Files:**
- Modify: `backend/src/main/java/com/brownevents/app/service/ConferenceService.java:23-25`
- Modify: `backend/src/test/java/com/brownevents/app/service/ConferenceServiceTest.java`

**Interfaces:**
- Consumes: `findAllFiltered(String, LocalDate, LocalDate, String, Pageable)` from Task 1.
- Produces: `Page<Conference> getAllConferences(Pageable pageable, String search, LocalDate from, LocalDate to, String status)`

**Test-first: yes** — write two failing tests before changing the service.

- [ ] Add imports to `ConferenceServiceTest.java`: `import java.time.LocalDate;`, `import java.util.List;`, `import static org.mockito.ArgumentMatchers.eq;`, `import static org.mockito.ArgumentMatchers.isNull;`.

- [ ] Add these two test methods to `ConferenceServiceTest` (before `createConference_shouldCallSaveAndReturnResult`):

```java
@Test
public void getAllConferences_shouldPassFormattedSearchTermToRepository() {
    Conference conf = new Conference();
    conf.setId(1L);
    conf.setTitle("Spring Tech Summit");
    Pageable pageable = PageRequest.of(0, 12);
    Page<Conference> expected = new PageImpl<>(List.of(conf), pageable, 1);
    when(conferenceRepository.findAllFiltered(
            eq("%spring%"), isNull(), isNull(), isNull(), any(Pageable.class)))
        .thenReturn(expected);

    Page<Conference> result = conferenceService.getAllConferences(pageable, "spring", null, null, null);

    assertNotNull(result);
    assertEquals(1, result.getTotalElements());
    assertEquals("Spring Tech Summit", result.getContent().get(0).getTitle());
    verify(conferenceRepository, times(1))
        .findAllFiltered(eq("%spring%"), isNull(), isNull(), isNull(), any(Pageable.class));
}

@Test
public void getAllConferences_shouldPassDateRangeToRepository() {
    Conference conf = new Conference();
    conf.setId(2L);
    conf.setTitle("Java Developer Days");
    Pageable pageable = PageRequest.of(0, 12);
    LocalDate from = LocalDate.of(2026, 1, 1);
    LocalDate to = LocalDate.of(2026, 6, 30);
    Page<Conference> expected = new PageImpl<>(List.of(conf), pageable, 1);
    when(conferenceRepository.findAllFiltered(
            isNull(), eq(from), eq(to), isNull(), any(Pageable.class)))
        .thenReturn(expected);

    Page<Conference> result = conferenceService.getAllConferences(pageable, null, from, to, null);

    assertNotNull(result);
    assertEquals(1, result.getTotalElements());
    verify(conferenceRepository, times(1))
        .findAllFiltered(isNull(), eq(from), eq(to), isNull(), any(Pageable.class));
}
```

- [ ] Run: `mvn test -Dtest=ConferenceServiceTest` — both new tests must **FAIL** (method not found). The two existing tests must still pass.

- [ ] Replace `getAllConferences` at `ConferenceService.java:23-25` with the updated signature. Add `import java.time.LocalDate;` to the service. The new method lower-cases and wraps `search` with `%` wildcards when non-blank, passes `null` otherwise, and delegates to `findAllFiltered`:

```java
public Page<Conference> getAllConferences(
        Pageable pageable, String search, LocalDate from, LocalDate to, String status) {
    String searchParam = (search != null && !search.isBlank())
        ? "%" + search.toLowerCase() + "%" : null;
    return conferenceRepository.findAllFiltered(searchParam, from, to, status, pageable);
}
```

- [ ] Update the existing `getAllConferences_shouldReturnAllConferences` test: replace the `findAll(any(Pageable.class))` stub and verify with `findAllFiltered(isNull(), isNull(), isNull(), isNull(), any(Pageable.class))`; update the service call to `conferenceService.getAllConferences(pageable, null, null, null, null)`.

- [ ] Run: `mvn test -Dtest=ConferenceServiceTest` — all four tests must **PASS**.

---

### Task 3: ConferenceController — four new `@RequestParam`

**Files:**
- Modify: `backend/src/main/java/com/brownevents/app/controller/ConferenceController.java:56-69`

**Interfaces:**
- Consumes: `ConferenceService.getAllConferences(Pageable, String, LocalDate, LocalDate, String)` from Task 2.

**Test-first: no** — no controller test class exists in this codebase.

- [ ] Add `import java.time.LocalDate;` to `ConferenceController.java`. In the `getAllConferences` method (lines 56-61), add four new `@RequestParam(required = false)` parameters — `String search`, `LocalDate from`, `LocalDate to`, `String status` — after the existing `size` param. Update the service call on line 61 to `conferenceService.getAllConferences(PageRequest.of(page, size), search, from, to, status)`. Spring's default `MethodArgumentTypeMismatchException` handling will return 400 for malformed date strings; no extra code needed.
- [ ] Compile and run all backend tests: `mvn test` — must pass.

---

### Task 4: api.js — forward filter params

**Files:**
- Modify: `frontend/src/api.js:5-9`

**Test-first: no** — no frontend tests exist.

- [ ] Replace lines 5-9 with the updated `getConferences` function that accepts four optional filter params and appends non-empty values as query parameters:

```js
export async function getConferences(page = 0, size = 12, search = '', from = '', to = '', status = '') {
  console.log('fetching conferences');
  const params = new URLSearchParams({ page, size });
  if (search) params.set('search', search);
  if (from) params.set('from', from);
  if (to) params.set('to', to);
  if (status) params.set('status', status);
  const response = await fetch(`${BASE_URL}/api/conferences?${params.toString()}`);
  return response.json();
}
```

---

### Task 5: ConferenceListPage — filter bar, state, and page reset

**Files:**
- Modify: `frontend/src/pages/ConferenceListPage.jsx`

**Interfaces:**
- Consumes: `getConferences(page, size, search, fromDate, toDate, statusFilter)` from Task 4.

**Test-first: no** — no frontend tests exist.

- [ ] After line 18 (`const [refreshKey, setRefreshKey] = useState(0)`), add four filter state variables — `search`, `fromDate`, `toDate`, `statusFilter` — each initialised to `''`.

- [ ] After the existing `useEffect` block (line 39), add a second `useEffect` that resets `page` to `0` when any filter changes:
  `useEffect(() => { setPage(0) }, [search, fromDate, toDate, statusFilter])`

- [ ] Update the `getConferences` call on line 28 to: `getConferences(page, 12, search, fromDate, toDate, statusFilter)`.

- [ ] Update the `useEffect` dependency array on line 39 to include all filter state: `[page, refreshKey, search, fromDate, toDate, statusFilter]`.

- [ ] After the `<p className="page__subtitle">` line and before `{showModal && ...}`, insert a filter bar `<div>` containing: a text `<input>` bound to `search`, two `<input type="date">` fields bound to `fromDate` and `toDate`, and a `<select>` bound to `statusFilter` with options `""` (All statuses), `UPCOMING`, `ONGOING`, `COMPLETED`, `CANCELLED`.

- [ ] Update the empty state at line 174: when any filter is active (`search || fromDate || toDate || statusFilter`), display `"No conferences match your search."` instead of `"No conferences found."`.

---

<!-- negative-constraints: reviewed; non-goals section has 7 constraints: no FTS engine (LIKE only — satisfied by JPQL LIKE in Task 1), no BASE_URL fix (no task touches api.js BASE_URL), no TD-011 seed fix (no task touches DataInitializer), no enum enforcement (no enum class or @Enumerated added), no integration/repo-level tests (all tests are Mockito unit tests), no frontend tests (all frontend tasks carry Test-first: no), no changes to sessions/speakers/rooms/registrations (no task touches those entities or their files). No task violates any of these. -->
