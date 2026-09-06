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