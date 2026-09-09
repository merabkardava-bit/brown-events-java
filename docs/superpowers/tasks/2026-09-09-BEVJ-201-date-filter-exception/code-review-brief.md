# Code review — 2026-09-09-BEVJ-201-date-filter-exception (2026-09-09)

**request-changes** · confidence: low · 5 blocking · 0 deferred · 0 filtered as noise
Coverage: blind — n/a (compact profile) · edge-case ✓ · acceptance — n/a (no spec) · verification-gap — n/a (compact profile)  (1/1 applicable lenses ran)

No spec/story artifact for this task — acceptance criteria could not be verified; confidence is low.

## Look here first

- `frontend/src/api.js:13` — [infra] response.json() called unconditionally; any HTTP error with an empty or non-JSON body (e.g., the from>to 400) produces a cryptic SyntaxError across all api.js functions — CR-004
- `backend/src/main/java/com/brownevents/app/repository/ConferenceRepository.java:55` — [other: correctness] date-range filter checks only startDate; multi-day conferences spanning the from-boundary are silently excluded from results — CR-001
- `frontend/src/pages/ConferenceListPage.jsx:60` — [other: UX] early return on invalid date range omits setConferences([]); stale conference cards remain visible alongside the inline error message — CR-005

## Also flagged

- `backend/src/main/java/com/brownevents/app/service/ConferenceService.java:29` — [other: correctness] empty-string ?status= not normalized to null; API consumers (curl, Swagger) receive zero results instead of all conferences — CR-002
- `backend/src/test/java/com/brownevents/app/controller/ConferenceControllerTest.java` — [other: testing] no test for from > to returning 400; the central BEVJ-201 guard can be silently regressed — CR-003

## Checked and clean

commit-format na · code-quality na · security na (standards not in scope for this profile)
