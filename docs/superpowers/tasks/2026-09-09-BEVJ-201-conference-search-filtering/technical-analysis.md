# Technical Research

**Task**: conference search filter pagination
**Generated**: 2026-09-09T00:00:00Z
**Research path**: filesystem

---

## 1. Original Context

BEVJ-201: Conference Search and Filtering — A user can search conferences by keyword (title or description) and filter by date range and status. The search is available from the conference list page. Results update as filters change. An empty result set shows a helpful message. Filtering works together with pagination — a filtered result set is also paginated. Definition of Done: GET /api/conferences accepts ?search=, ?from=, ?to=, ?status= query params; absent params apply no filter; frontend conference list has a search input and date range pickers; filters and pagination work together correctly; empty state message shown when no results match; at least two unit tests: one for keyword match, one for date range filter.

---

## 2. Codebase Findings

### Existing Implementations

- `backend/src/main/java/com/brownevents/app/entity/Conference.java` — JPA entity mapping to table `conferences`. Relevant fields: `title` (String), `description` (String), `startDate` (LocalDate), `endDate` (LocalDate), `status` (String, raw — no enum, documented values UPCOMING/ONGOING/COMPLETED/CANCELLED). No-arg constructor plus full-arg constructor exist. Getters/setters for all fields.
- `backend/src/main/java/com/brownevents/app/repository/ConferenceRepository.java` — extends `JpaRepository<Conference, Long>` only. Currently has **zero custom methods**. No `JpaSpecificationExecutor`. The only query used is the inherited `findAll(Pageable)`.
- `backend/src/main/java/com/brownevents/app/service/ConferenceService.java` — `getAllConferences(Pageable pageable)` delegates directly to `conferenceRepository.findAll(pageable)` and returns `Page<Conference>`. This is the single method that needs to accept additional filter parameters.
- `backend/src/main/java/com/brownevents/app/controller/ConferenceController.java` — `GET /api/conferences` accepts `?page=` (default 0) and `?size=` (default 12) via `@RequestParam`. Constructs `PageRequest.of(page, size)`, calls `conferenceService.getAllConferences(pageable)`, and returns the response as a `HashMap` envelope `{ data, page, size, totalElements, totalPages }`.
- `backend/src/main/java/com/brownevents/app/repository/SessionRepository.java` — contains the established `@Query` JPQL pattern for paginated queries, including a required separate `countQuery`. This is the direct template for any paginated custom query on `ConferenceRepository`.
- `frontend/src/api.js` — `getConferences(page = 0, size = 12)` builds `GET /api/conferences?page=&size=`. No filter params are forwarded. `BASE_URL` is hardcoded to `http://localhost:8080` (known debt FE-001).
- `frontend/src/pages/ConferenceListPage.jsx` — manages `page`, `totalPages`, `totalElements`, and `refreshKey` state via `useState`. `useEffect` with `[page, refreshKey]` dependency array triggers `getConferences(page)`. Renders a conference grid; shows a "No conferences found." empty state when `conferences.length === 0`. Contains no search input, date pickers, or status filter.

### Architecture and Layers Affected

| Layer | Component | Current State |
|---|---|---|
| Persistence | `ConferenceRepository` | Extends `JpaRepository` only; needs a new `@Query` JPQL method |
| Business Logic | `ConferenceService.getAllConferences` | Accepts `Pageable` only; needs four optional filter params |
| API | `ConferenceController.getAllConferences` | Accepts `page` and `size` only; needs `?search=`, `?from=`, `?to=`, `?status=` |
| Frontend API | `api.js getConferences` | Appends `page` and `size` only; needs to forward filter params |
| Frontend UI | `ConferenceListPage.jsx` | No filter controls; needs search input, date pickers, status selector |

### Integration Points

- `ConferenceController` → `ConferenceService` → `ConferenceRepository` — all three must be updated in concert.
- `ConferenceListPage.jsx` → `api.js getConferences` → `GET /api/conferences` — both must be updated.
- `PageRequest.of(page, size)` is constructed in the controller and passed through the service to the repository. The filter params must travel the same path.
- `spring.jpa.hibernate.ddl-auto=update` governs schema — no migration file is needed because no schema change is required (all filter fields already exist on the `conferences` table).

### Patterns and Conventions

- **Paginated @Query with separate countQuery**: Established in `SessionRepository` — a JPQL `@Query(value = "...", countQuery = "SELECT COUNT(s) FROM ...")`. The same pattern is required for a paginated conference search query.
- **Optional-param JPQL pattern**: Not yet present in this codebase. The standard Spring Data JPA approach for optional filters in a single `@Query` is `:param IS NULL OR field LIKE :param` for each optional predicate. Alternatively, `JpaSpecificationExecutor<Conference>` can be added, but this departs from the codebase style which avoids that abstraction.
- **Controller response envelope**: All `GET /api/conferences` responses are `HashMap` with keys `data`, `page`, `size`, `totalElements`, `totalPages` — this must be preserved.
- **Service returns `Page<T>`**: `getAllConferences` already returns `Page<Conference>`; the signature change is additive (extra params, same return type).
- **No `@Transactional` on service methods**: Consistent with the rest of the codebase; a read-only query does not require it.
- **Frontend filter → page reset**: When filters change, `page` must be reset to 0. The existing `refreshKey` pattern in `ConferenceListPage.jsx` shows a deliberate mechanism for forcing re-fetch; a simpler `setPage(0)` on filter state change is the in-keeping approach.
- **useEffect dependency array**: Must include all filter state variables alongside `page` and `refreshKey` to trigger re-fetch on filter change.

---

## 3. Documentation Findings

### Guides and Architecture Docs

- `.ai-run/guides/` — directory absent. Only `.ai-run/sdlc-factory/` exists with operational files.
- `docs/architecture.md` — full architecture reference. Documents the `GET /api/conferences` endpoint and its current `page`/`size` params. Documents the `ConferenceRepository` as having no custom methods. Lists `getConferences()` in the frontend API table with no filter params.
- `docs/tech-debt-audit.md` — documents TD-011 (status stored as raw String, seed data uses undocumented `"ACTIVE"`) and FE-001 (hardcoded BASE_URL). Both are relevant risks for this feature.
- `docs/devlog.md` — records BEVJ-102 pagination work: confirms the pagination design (Pageable, Page<T>, response envelope, countQuery rationale) and that `ConferenceServiceTest` was updated alongside the pagination change.

### Architectural Decisions

- **BEVJ-102 (Pagination)**: `ConferenceService.getAllConferences` was deliberately changed to take `Pageable` and return `Page<Conference>`. Any filter parameters added to this method must preserve that signature shape.
- **BEVJ-101 (N+1 fix)**: The `@Query` + `countQuery` separation in `SessionRepository` was explicitly introduced because "JPQL with JOIN FETCH cannot be used as-is for the COUNT(*) that Page metadata needs." A conference search query with pagination must follow the same discipline if it uses JOIN FETCH; for a plain SELECT on `Conference` (no joins needed since sessions/registrations are `@JsonIgnore`), a separate countQuery is still needed but simpler.
- **TD-011**: `status` is a raw `String`. No enum enforcement exists. A status filter must be case-insensitive or normalise to UPPER before comparing.

### Derived Conventions

- All repository custom queries use `@Query` JPQL with `@Param`, not Spring Data derived method names.
- Paginated queries always include a `countQuery` alongside the main query.
- The controller always owns `PageRequest.of(page, size)` construction — the service never constructs `Pageable` itself.
- Frontend: all `fetch` calls are in `api.js`; components never call `fetch` directly.
- Filter state that drives re-fetching must appear in the `useEffect` dependency array.
- No TypeScript; no PropTypes; plain JSX with inline styles where global CSS classes are insufficient.

---

## 4. Testing Landscape

### Existing Coverage

- `backend/src/test/java/com/brownevents/app/service/ConferenceServiceTest.java` — covers `getAllConferences` (mocks `conferenceRepository.findAll(Pageable)` returning a `PageImpl`, asserts total elements and first item title) and `createConference`. No filter-parameter tests exist.
- `backend/src/test/java/com/brownevents/app/service/SessionServiceTest.java` — covers `getSession` and `createSession`; not relevant to conference search.
- Frontend: no test files anywhere in `frontend/`.

### Testing Framework and Patterns

- **Framework**: JUnit 5 + Mockito via `@ExtendWith(MockitoExtension.class)`
- **Class-level setup**: `@Mock ConferenceRepository`, `@Mock SessionRepository`, `@InjectMocks ConferenceService`
- **Stub pattern**: `when(conferenceRepository.findAll(any(Pageable.class))).thenReturn(new PageImpl<>(list, pageable, list.size()))`
- **Assertions**: `assertNotNull`, `assertEquals`, `verify(mock, times(1)).method(arg)`
- **No `@SpringBootTest`**: Tests are pure unit tests with no Spring context, no H2, no DB. This means the new repository method is not integration-tested; only the service logic is exercised.
- The DoD requires at minimum: one test for keyword match (e.g. pass a search term, verify the repository method is called and filtered results are returned) and one for date range.

### Coverage Gaps

- No test for `getAllConferences` with a non-null `search` param — required by DoD.
- No test for `getAllConferences` with `from`/`to` date params — required by DoD.
- No test for `getAllConferences` with a `status` param.
- No test for the "absent params apply no filter" path (null/empty params).
- No integration or repository-level tests — the JPQL query itself is never exercised by the test suite.
- Frontend has zero tests; the DoD does not require any frontend tests.

---

## 5. Configuration and Environment

### Environment Variables

No environment variables are used for the conference search feature area. DB connection properties are hardcoded in `application.properties` (TD-005). No feature flags exist.

### Configuration Files

- `backend/src/main/resources/application.properties` — `spring.jpa.hibernate.ddl-auto=update` (schema auto-managed; no migration step needed for this feature since all required columns already exist), `spring.jpa.show-sql=true` (query logging active in all environments).

### Feature Flags and Deployment Concerns

No feature flags. No deployment manifest changes required. The `Conference` entity already has all columns needed for filtering (`title`, `description`, `startDate`, `endDate`, `status`) — `ddl-auto=update` will not alter the schema for this change.

---

## 6. Risk Indicators

- **ConferenceRepository has no custom methods** — the JPQL optional-predicate pattern (`:param IS NULL OR ...`) has no existing example in this file. The closest template is in `SessionRepository`, but that query filters by a required `conferenceId`, not optional params. The implementation must introduce this pattern from scratch.
- **Optional multi-param JPQL correctness** — writing a single `@Query` that correctly handles all combinations of null/non-null params (search, from, to, status) is error-prone. If `JpaSpecificationExecutor` is used instead, it introduces a pattern not present anywhere in the codebase, which may surprise future maintainers. Either approach needs careful implementation and the unit tests only mock the repository, so JPQL correctness is never verified by the test suite.
- **Status data inconsistency (TD-011)** — `DataInitializer` seeds one conference with `status = "ACTIVE"` which is not a documented value. A `?status=ONGOING` filter will not return this record even if the intent was `ONGOING`. This is pre-existing tech debt but will surface as unexpected behaviour when filtering by status.
- **Speculative**: Case sensitivity of the status filter — `status` is stored and compared as a raw String. Filtering `?status=upcoming` vs `?status=UPCOMING` will produce different results unless normalisation is applied either in the query or the service.
- **Frontend filter state and page reset** — `ConferenceListPage.jsx` must reset `page` to 0 when any filter changes; failing to do so would return an empty page if filters narrow the result set below the current page offset. The `refreshKey` pattern in the page is an existing safeguard for the create-conference flow but must not be confused with filter-driven resets.
- **No integration tests** — the new `@Query` JPQL is exercised only if a full Spring context or H2 integration test is added. The existing test suite runs pure unit tests that mock the repository, so a malformed JPQL string would only surface at runtime or in a Docker-compose smoke test.
- **api.js hardcoded BASE_URL (FE-001)** — filter query params will be appended to `http://localhost:8080/api/conferences`, which works locally but continues to break in Docker and production deployments. This is pre-existing debt and out of scope for this task, but the implementer should be aware.
- **No `?from=`/`?to=` field spec** — the DoD says "date range" without specifying whether it filters on `startDate`, `endDate`, or spans the conference period (startDate >= from AND endDate <= to vs. startDate >= from AND startDate <= to). The most common interpretation (filter on `startDate`) should be confirmed before implementation.

---

## 7. Summary for Complexity Assessment

This task touches all five architectural layers: `ConferenceRepository` (new JPQL query), `ConferenceService` (new parameters), `ConferenceController` (four new `@RequestParam`s), `frontend/src/api.js` (param forwarding), and `ConferenceListPage.jsx` (filter UI + state). File change surface is moderate — five files plus a new or extended test class. The backend chain is a straight line with no cross-domain dependencies; sessions, registrations, speakers and rooms are not involved.

The technically novel element is the `ConferenceRepository` query. The repository currently extends only `JpaRepository` with no custom methods. Implementing optional multi-param filtering requires either a `@Query` JPQL with nullable-param predicates (`:param IS NULL OR LOWER(c.title) LIKE :param`) or introducing `JpaSpecificationExecutor`. The codebase uses the `@Query` pattern exclusively; a `Specification`-based approach has no precedent here. A paginated `@Query` also requires a separate `countQuery`, a pattern already established in `SessionRepository`. The date range boundary semantics (`from`/`to` against `startDate` vs. a window-overlap query) need a decision before coding.

Test coverage posture is weak for this feature. The existing `ConferenceServiceTest` mocks the repository and does not exercise JPQL. The DoD requires two new unit tests (keyword match, date range), both of which fit naturally into the existing mock-based pattern: stub the repository method with filtered `PageImpl` results and assert the service passes the correct arguments. A JPQL integration test does not exist and is not required by the DoD, but the absence means query correctness relies entirely on manual or smoke testing. Frontend tests do not exist and are not required by the DoD.

---

## 8. External References

None named by the task.
