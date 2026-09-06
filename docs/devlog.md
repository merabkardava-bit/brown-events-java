# Dev Log

## 2026-09-05

### EXT-100 — Import PRODUCT.md tasks as GitHub issues

Added a GitHub MCP server with `claude mcp add` and used it to read `PRODUCT.md`,
generating one GitHub issue per task with the title format `<ID> — <task name>`
and the full task description as the issue body.

## 2026-09-06

### BEVJ-001-codebase-mapping

With plan mode on I asked claude code to create a codebase map with an ER diagram, 
a package-to-responsibility table and the controller->service->repository call chain for at least 3 flows.
After that I checked the output and it did a pretty good job.
The only thing I changed from the output is that I got rid of unprompted code analysis and recommendations
like "Add DTOs" and "implement an exception handler".

### BEVJ-002 — API Documentation

I asked claude to add swagger documentation to the API with every endpoint, it's inputs, response schema
and an example request body documented by referencing controller classes and @docs/architecture.md.
The changes were mostly correct after the first prompt, the only issue was that text block literals were used
that aren't supported in java 11.

### BEVN-004 — Technical Debt Audit

I asked claude to read both frontend and backend and audit every issue according to the requirements.
It deployed two explore subagents one for front and one for back. But it started writing the audit document
before the frontend agent finished reporting so the file only included backend issues. 
I had to prompt claude to include issues from frontend agent's findings as well.
I could have explicitly mentioned to wait until they both returned findings 
or not use separate subagents at all, since the project isn't very large yet.

### BEVJ-101 — Slow Sessions Page

I reported that opening a conference page with many sessions was noticeably slow with no errors.
I asked Claude to find the root cause and reference the @tech-debt-audit.md file from the previous task
and it identified the root cause as an N+1 query problem
on the `Session` entity: all three `@ManyToOne` associations (`conference`, `speaker`, `room`)
defaulted to `FetchType.EAGER`, causing Hibernate to fire 1 + 3N database queries for a list of
N sessions. The fix was two changes: marking all three associations `LAZY` in `Session.java`, and
replacing the derived `findByConferenceId` query in `SessionRepository` with an explicit JPQL
query using `JOIN FETCH` / `LEFT JOIN FETCH` to load the full object graph in a single round-trip.
The response data and method signatures stayed identical. I also asked to add comments to the changed parts.