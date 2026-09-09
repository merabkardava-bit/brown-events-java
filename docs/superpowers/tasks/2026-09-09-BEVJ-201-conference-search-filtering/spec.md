# BEVJ-201: Conference Search and Filtering

**Created:** 2026-09-09
**Size:** M (2–3 days)
**Branch:** BEVJ-201-converence-search-filtering

---

## Problem

The conference list page shows all conferences without any way to narrow results. As the dataset grows, users cannot find relevant conferences efficiently. There is no search, no date filtering, and no status filtering — and pagination (BEVJ-102) was added without filter support, so these concerns must be integrated together from the start.

---

## Goal

Allow users to search and filter the conference list by keyword, date range, and status. All filter parameters must compose with existing pagination. Absent parameters apply no filter. An empty result set shows a helpful message rather than a blank page.

---

## Scope

### In scope
- Backend: `GET /api/conferences` accepts four new optional query parameters: `?search=`, `?from=`, `?to=`, `?status=`
- Backend: parameter absence is treated as "no filter" for that dimension; all four parameters are independently optional and composable
- Backend: two new unit tests in `ConferenceServiceTest` — one for keyword match, one for date range filter
- Frontend: `ConferenceListPage` gains a search text input, two date pickers (`from`/`to`), and a status dropdown
- Frontend: filters and pagination work together; changing any filter resets the page to 0
- Frontend: empty state message displayed when the filtered result set is empty

### Non-goals
- Full-text search (no FTS engine, no relevance ranking — keyword match via LIKE is sufficient)
- Fixing the `BASE_URL` hardcoding in `api.js` (FE-001, pre-existing debt, out of scope)
- Fixing the seeded `"ACTIVE"` status record (TD-011, pre-existing debt, out of scope)
- Repository-level or integration-level tests; the DoD requires only service-layer unit tests
- Frontend tests (none exist in this codebase; DoD does not require them)
- Adding enum enforcement to `Conference.status`
- Any changes to sessions, speakers, rooms, registrations, or other entities

---

## Design

### Backend

**Repository** (`ConferenceRepository`): Add one new paginated method. The method accepts four nullable params — `search`, `from`, `to`, `status` — and uses the optional-predicate JPQL pattern `(:param IS NULL OR ...)` established in `SessionRepository`. A separate `countQuery` is required alongside the main query (same discipline as `SessionRepository` BEVJ-101). The keyword predicate covers both `title` and `description` fields using case-insensitive LIKE. The date predicate filters on `startDate` (`:from IS NULL OR c.startDate >= :from` and `:to IS NULL OR c.startDate <= :to`). The status predicate normalises both sides to upper-case (`UPPER(c.status) = UPPER(:status)`).

New interface to pin:
```java
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

The `search` param is passed in pre-formatted as `%<term>%` (lower-cased) by the service layer so the JPQL stays clean.

**Service** (`ConferenceService`): `getAllConferences` gains four optional parameters — `String search`, `LocalDate from`, `LocalDate to`, `String status`. When `search` is non-blank the service lower-cases it and wraps it with `%` wildcards before passing to the repository. When `search` is blank or null it passes `null` to the repository (triggering the "no filter" predicate). The other three params are passed through as-is. The method delegates to the new `findAllFiltered` repository method. The signature of the original `findAll(Pageable)` call is replaced; no other service logic changes.

**Controller** (`ConferenceController`): `GET /api/conferences` gains four new `@RequestParam(required = false)` parameters: `String search`, `String from`, `String to`, `String status`. The controller parses `from` and `to` as `LocalDate` (ISO-8601 `yyyy-MM-dd`) before passing to the service; malformed dates result in a 400 (Spring's default `MethodArgumentTypeMismatchException` handling is sufficient). The response envelope shape (`data`, `page`, `size`, `totalElements`, `totalPages`) is unchanged.

### Frontend

**`api.js`**: `getConferences` gains four optional filter params. Updated signature to pin:
```js
export function getConferences(page = 0, size = 12, search = '', from = '', to = '', status = '') { ... }
```
Non-empty filter values are appended as query params. Empty strings are omitted from the URL so absent params send no filter to the backend.

**`ConferenceListPage.jsx`**: Four new state variables — `search`, `fromDate`, `toDate`, `statusFilter` — each initialised to empty string. The `useEffect` dependency array is expanded to include all four alongside `page` and `refreshKey`. A `useEffect` watching only the four filter variables calls `setPage(0)` when any changes, so a filter change always starts from page 1. The filter bar renders above the conference grid: a text input for keyword search, two `<input type="date">` fields for from/to, and a `<select>` dropdown for status with options `""` (All), `UPCOMING`, `ONGOING`, `COMPLETED`, `CANCELLED`. The existing "No conferences found." empty state already covers the no-results case; its message text may be updated to "No conferences match your search." when any filter is active, but this is a minor UX improvement, not a hard requirement.

### Tests

Two new test methods in `ConferenceServiceTest`:

1. **Keyword match** — stub `conferenceRepository.findAllFiltered` with a `PageImpl` containing one matching conference, call `getAllConferences` with `search = "java"` and null date/status params, assert the returned page contains the expected conference, and verify the repository was called with a `search` param containing `%java%`.

2. **Date range** — stub `findAllFiltered` with a `PageImpl` containing one conference, call `getAllConferences` with `from = 2026-01-01` and `to = 2026-06-30` and null search/status params, assert the returned page and verify the repository received the correct `LocalDate` arguments.

Both tests follow the existing mock pattern: `@Mock ConferenceRepository`, `@InjectMocks ConferenceService`, `when(...).thenReturn(...)`, `assertEquals`, `verify`.

---

## Acceptance Criteria

1. `GET /api/conferences?search=spring` returns only conferences whose title or description contains "spring" (case-insensitive).
2. `GET /api/conferences?from=2026-01-01&to=2026-06-30` returns only conferences whose `startDate` falls within that range.
3. `GET /api/conferences?status=UPCOMING` returns only conferences with status UPCOMING (case-insensitive match).
4. All four params are independently optional; omitting any applies no filter for that dimension.
5. Any combination of the four params returns the intersection of their filter results, paginated.
6. The response envelope shape (`data`, `page`, `size`, `totalElements`, `totalPages`) is identical to the unfiltered response.
7. The conference list page has a search text input, two date pickers, and a status dropdown.
8. Changing any filter resets the displayed page to 1 and re-fetches results.
9. When the filtered result set is empty, a message is shown (not a blank grid).
10. Filters and pagination operate correctly together: the paginated result reflects the filtered set, not the full dataset.
11. `ConferenceServiceTest` includes at least one test for keyword match and one for date range filter, both passing.

---

## Decisions

- JPQL `@Query` over `JpaSpecificationExecutor` — `@Query` is the codebase's only repository pattern; `JpaSpecificationExecutor` has no precedent here.
- Date range filters on `startDate` in `[from, to]` — settled by coordinator; simplest and most intuitive for event browsing.
- `search` pre-formatted as `%term%` in service layer — keeps JPQL clean; consistent with how the service layer shapes data before calling the repository.
- Status filter is case-insensitive via `UPPER()` on both sides — `status` is stored as raw String with no enum enforcement; normalising both sides prevents silent mismatches.
- Status UI is a `<select>` dropdown — status has four documented finite values; a dropdown prevents invalid free-text input and is more usable than a text field.
- Page reset to 0 on filter change — prevents stale pagination offset showing an empty page when filters narrow the result set below the current offset.

---

## Open Risks

- The new `@Query` JPQL is not exercised by any automated test (unit tests mock the repository layer). A malformed query will only fail at runtime or during Docker smoke testing.
- The seeded `"ACTIVE"` record (TD-011) will not be returned by `?status=UPCOMING` or any other documented status filter. This is expected given the pre-existing data inconsistency but may confuse manual testers.
- `BASE_URL` hardcoded to `localhost:8080` (FE-001) means the new filter params, like all existing params, will not work in Docker or production deployments without a separate env-config fix.
