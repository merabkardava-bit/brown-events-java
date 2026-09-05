# BrownEvents — Brownfield Task Library

> *"All our events are brown as s... stale coffee"*
>
> Read this document before starting any BrownEvents task.
> Set up the project with `docker-compose up` and explore it before picking up a task.

---

## Table of Contents

- [What You're Working With](#what-youre-working-with)
- [Domain Entities](#domain-entities)
- [Phase Summary](#phase-summary)
- [Tasks](#tasks)
  - [Phase 0 — Discovery](#phase-0--discovery)
  - [Phase 1 — Stabilize & Evolve](#phase-1--stabilize--evolve)
  - [Phase 2 — New Features](#phase-2--new-features)

---

## What You're Working With

BrownEvents is a conference management application built two years ago by a team that has since moved on. The app works — conferences can be created, sessions are listed, attendees can register. The codebase, however, has not been maintained. Your job is to explore it, understand it, and improve it.

The stack is **Java 11 + Spring Boot 2.7 + React + Vite + PostgreSQL**. Run `docker-compose up` to get a working environment before starting any task.

> **Important:** Phase 0 tasks are prerequisites. Complete them before Phase 1 so the team has a shared map of the codebase.

---

## Domain Entities

| Entity | Key Fields |
|--------|-----------|
| **Conference** | id, title, description, location, startDate, endDate, status |
| **Session** | id, title, description, startTime, endTime, capacity, conferenceId, speakerId, roomId |
| **Speaker** | id, firstName, lastName, bio, email |
| **Room** | id, name, capacity, location |
| **Attendee** | id, firstName, lastName, email |
| **Registration** | id, conferenceId, attendeeId, registeredAt, status |

---

## Phase Summary

| Phase | Theme | Spec-Driven Flow |
|-------|-------|-----------------|
| Phase 0 | Discovery — map the codebase, document what exists, audit quality | No |
| Phase 1 | Stabilize & Evolve — improve what exists, upgrade stack, add first new features | No — build from plain requirements |
| Phase 2 | New Features — meaningful functionality added through full AI-assisted SDLC | Yes — use `/sdlc:light`, `/sdlc:standard`, or `/sdlc:autonomous` |

Phase 0 is a prerequisite for Phase 1. Stabilization tasks in Phase 1 (BEVJ-101 through BEVJ-106) should be completed before Phase 2.

---

## Tasks

The team consists of 2 backend developers, 1 frontend developer, and 1 QA engineer. Tasks within each phase are designed to be worked in parallel where possible.

---

## Phase 0 — Discovery

> No spec-driven flow. Goal: understand the codebase well enough to work in it safely. Output is documentation, not code.

### BEVJ-001 — Codebase Mapping
**Role:** Developer
Explore the backend codebase and produce a written map of what exists. Document the package structure, the responsibility of each module (conference, session, speaker, registration), and the relationships between entities. Draw an entity-relationship diagram. Identify which controllers call which services, and which services use which repositories. At the end, a new team member should be able to understand the architecture from your document without reading the code.

**Definition of Done:**
- [ ] Entity-relationship diagram created (any format: draw.io, Mermaid, plain ASCII)
- [ ] Module responsibility table written: each package mapped to its role
- [ ] Controller → Service → Repository call chain documented for at least 3 flows
- [ ] Saved as `docs/architecture.md` in the project root

---

### BEVJ-002 — API Documentation
**Role:** Developer
The API has no documentation. A new frontend developer joining the team has no way to know what endpoints exist, what they expect, or what they return without reading the backend source code. Fix this — the API should be self-describing and explorable without touching the code.

**Definition of Done:**
- [ ] All existing endpoints are documented and browsable via a UI
- [ ] Each endpoint shows its expected inputs, possible responses, and at least one example request body
- [ ] A frontend developer can build against the API using only the documentation
- [ ] No endpoint is missing from the documentation

---

### BEVJ-003 — Technical Debt Audit
**Role:** Developer
Read the codebase thoroughly — both backend and frontend — and produce a structured audit document listing every quality issue you find. Each issue should be actionable: a reader should know exactly where to look and what to change. Group issues by category and assign severity. The output of this task feeds directly into the Phase 1 stabilization work.

**Definition of Done:**
- [ ] Both backend and frontend covered in the audit
- [ ] Each issue has: location (file + line where relevant), description, severity (High / Medium / Low), suggested fix
- [ ] Issues grouped by category (e.g. performance, consistency, error handling, maintainability, configuration)
- [ ] At least 8 distinct issues documented
- [ ] Saved as `docs/tech-debt-audit.md` in the project root

---

## Phase 1 — Stabilize & Evolve

> Build from plain requirements — no spec-driven flow. Stabilization tasks first, then new features.

### BEVJ-101 — Sessions Page Is Slow
**Role:** Developer
Users are complaining that opening a conference page takes noticeably longer when the conference has many sessions. No errors, the data loads — it's just slow. Find out why and fix it. The data returned must stay the same.

**Definition of Done:**
- [ ] Root cause identified and documented in a code comment at the fix location
- [ ] Fix applied to all affected endpoints
- [ ] No existing endpoint returns different data than before
- [ ] PR description explains what was happening and what changed

---

### BEVJ-102 — Conference and Session Lists Don't Scale
**Role:** Developer
As the number of conferences grows, the main page is getting slower and the browser starts struggling to render everything at once. Same story on the session list inside a conference. Make these lists work at scale — both on the backend and in the UI.

**Definition of Done:**
- [ ] Conference and session lists load in reasonable time regardless of the total number of records
- [ ] The UI does not render all items at once — users can navigate through results in chunks
- [ ] The backend does not load all records into memory on each request
- [ ] Navigating to a chunk beyond the available data does not cause an error

---

### BEVJ-103 — Standardize API Responses and Error Handling
**Role:** Developer
The frontend team keeps running into surprises when consuming the API — error handling on the client side is a mess because responses look different depending on which endpoint you hit. On top of that, when something breaks on the backend, raw error details leak into the response. Fix this properly so the API behaves predictably for any caller.

**Definition of Done:**
- [ ] All endpoints return responses in the same structure — no surprises depending on which endpoint you call
- [ ] `POST` endpoints return `201 Created`, not `200 OK`
- [ ] Error responses are structured, human-readable, and never expose a stack trace
- [ ] Common failure scenarios (not found, bad input, unexpected errors) return appropriate HTTP status codes
- [ ] At least one existing unit test updated to reflect the new response shape

---

### BEVJ-104 — Upgrade to Spring Boot 3 and Java 21
**Role:** Developer
The project runs on Java 11 and Spring Boot 2.7 — both are out of support. Upgrade to Java 21 (LTS) and Spring Boot 3.x (latest stable). The app must compile, all tests must pass, and `docker-compose up` must bring up a working application after the upgrade. Update the CI pipeline and Docker configuration accordingly.

**Definition of Done:**
- [ ] `pom.xml` updated: Java 21, Spring Boot 3.x (latest stable)
- [ ] Application starts without errors
- [ ] All existing unit tests pass
- [ ] `docker-compose up` produces a running, working application
- [ ] `Dockerfile` in `backend/` updated to use `eclipse-temurin:21-jre` (or equivalent)
- [ ] `.gitlab-ci.yml` build image updated to `maven:3.9-eclipse-temurin-21`

---

### BEVJ-105 — Refactor Frontend: Component Structure
**Role:** Developer
Every time someone needs to touch the conference detail page it turns into a day-long ordeal. New devs get lost, small changes cause unexpected side effects, and adding anything new feels risky. Refactor it so the team can work on it without fear.

**Definition of Done:**
- [ ] A new developer can understand and modify any part of the conference detail area without reading the whole file
- [ ] All four pages work correctly after the refactor
- [ ] No new console errors in the browser during normal navigation
- [ ] The refactor is covered in the PR description: what changed and why

---

### BEVJ-106 — Frontend Cleanup After Code Review
**Role:** Developer
During a routine code review, a senior engineer flagged several issues in the frontend that have been silently sitting in production. Nothing is broken, but the code is not in the shape it should be. Go through the codebase, fix what you find, and document it in the PR.

**Definition of Done:**
- [ ] The app behaves correctly across environments, not just the one it was originally developed on
- [ ] No development artifacts remain in the codebase
- [ ] Users see a meaningful message when something goes wrong, not a broken UI
- [ ] PR description lists every issue found and how it was fixed

---

### BEVJ-107 — Fix Permissive CORS Configuration
**Role:** Developer
Every controller allows requests from any origin — any website on the internet can make requests to this API. In a conference management app that handles attendee registrations, this is a security problem. Replace the wildcard with an explicit allowed-origins list, and move the CORS configuration into a single central place.

**Definition of Done:**
- [ ] Wildcard CORS removed from all controllers
- [ ] CORS configured centrally in one place
- [ ] Allowed origins read from a configurable property (not hardcoded)
- [ ] `docker-compose up` + frontend on `localhost:5173` still works after the change

---

### BEVJ-108 — Add Transaction Boundaries to Multi-Step Writes
**Role:** Developer
The registration flow saves an attendee and then saves a registration in two separate database operations with no transaction wrapping them. If anything fails between the two saves, the database is left in an inconsistent state — an attendee row with no matching registration. Find all multi-step write operations in the service layer and make them atomic.

**Definition of Done:**
- [ ] Multi-step write operations in the service layer are atomic
- [ ] A brief comment explains what state would be corrupted without the transaction
- [ ] Existing unit tests still pass

---

### BEVJ-109 — Add Input Validation to API Endpoints
**Role:** Developer
The API accepts any payload without validation. Submitting a registration with an empty email, creating a conference with no title, or sending a completely empty JSON body all either silently succeed or produce an unhelpful 500. Add proper input validation so the API rejects invalid input with a clear error message before it reaches the service layer.

**Definition of Done:**
- [ ] Invalid input returns `400 Bad Request` with a message listing which fields failed and why
- [ ] At least two unit or integration tests covering validation failure scenarios

---

### BEVJ-110 — Introduce DTOs to Decouple API from Database
**Role:** Developer
The API returns raw JPA entity objects directly from every endpoint. This couples the API contract to the database schema — any column rename or relationship change silently changes the API response shape. It also risks exposing fields that should not be public. Introduce response DTOs to give the API a stable, intentional shape independent of the persistence layer.

**Definition of Done:**
- [ ] Every endpoint returns a DTO, not a JPA entity
- [ ] No JPA entity class appears in any controller return type
- [ ] Removing or renaming an entity field does not automatically change the API response
- [ ] All existing tests pass

---

### BEVJ-111 — Fix Datetime Handling
**Role:** Developer
The codebase uses `LocalDateTime` throughout — in entity fields, service logic, and seed data. `LocalDateTime` carries no timezone information, causing incorrect behavior when the server and clients are in different timezones and errors when mixing with timezone-aware values. Fix all datetime handling to be timezone-aware.

**Definition of Done:**
- [ ] All datetime fields in entities use a timezone-aware type
- [ ] All datetime values produced by the application are timezone-aware
- [ ] No `LocalDateTime` remains in entity classes or service logic
- [ ] A comment explains why timezone-naive datetimes are problematic

---

### BEVJ-112 — Add Read-Only Transaction Hints to Query Methods
**Role:** Developer
Every service method — including all read-only queries — runs in a full read-write transaction. This enables unnecessary change tracking, holds write locks longer than needed, and prevents Hibernate from applying read-only optimizations. Mark all read-only service methods appropriately.

**Definition of Done:**
- [ ] All service methods that only read data are marked as read-only
- [ ] No read-only method holds an unnecessary write transaction
- [ ] All existing tests pass
- [ ] PR explains what Hibernate does differently for read-only transactions

---

### BEVJ-113 — Register Now Button Shown for Inactive Conferences
**Role:** Developer
The conference detail page shows a "Register Now" button regardless of whether the conference is still accepting registrations. Users on completed or cancelled conferences see the button, click it, and get an error — or register for something that is no longer active.

**Definition of Done:**
- [ ] Register Now button is not visible on completed conferences
- [ ] Register Now button is not visible on cancelled conferences
- [ ] Button remains visible and functional for upcoming and active conferences
- [ ] No regression in the registration flow for active conferences

---

### BEVJ-114 — Registration Modal Shows Stale Data After Close
**Role:** Developer
When a user opens the registration modal, partially fills in the form, and then closes it, the fields still contain the old data the next time the modal is opened. After a successful registration, reopening the modal shows the success screen instead of a fresh form — making it impossible to start a new registration without refreshing the page.

**Definition of Done:**
- [ ] Closing the modal resets all form fields to empty
- [ ] Closing the modal clears any validation errors and server error messages
- [ ] After a successful registration, reopening the modal presents a fresh empty form
- [ ] Multiple open/close cycles do not accumulate state

---

### BEVJ-115 — Speakers Directory Page Is Missing
**Role:** Developer
The application has no speakers directory. Users have no way to browse speakers, and the speakers endpoint exposed by the API is not reachable from any part of the frontend.

**Definition of Done:**
- [ ] A speakers directory page lists all speakers
- [ ] Each speaker entry shows at minimum the speaker's name
- [ ] The page is reachable via a navigation link in the main menu
- [ ] The route is registered in the application router
- [ ] Loading and error states are handled

---

### BEVJ-116 — No Way to Create or Edit Conferences from the UI
**Role:** Developer
There are no pages for creating or editing conferences. Administrators must use the raw API to manage conferences, and the frontend surfaces no controls for either action.

**Definition of Done:**
- [ ] A form for creating a new conference is accessible from the conference list page
- [ ] A form for editing an existing conference is accessible from the conference detail page
- [ ] Both forms validate required fields before submission
- [ ] After a successful create or edit, the user is redirected to the conference detail page
- [ ] Both pages handle loading and error states gracefully

---

## Phase 2 — New Features

> Use the spec-driven flow for every task. Choose the entry point based on scope and risk: `/sdlc:autonomous` for end-to-end autonomous execution, `/sdlc:standard` for inline HITL ticket-sized work, `/sdlc:light` for simple well-scoped tasks.

### BEVJ-201 — Conference Search and Filtering
**Role:** Developer

A user can search conferences by keyword (title or description) and filter by date range and status. The search is available from the conference list page. Results update as filters change. An empty result set shows a helpful message. Filtering works together with pagination — a filtered result set is also paginated.

**Definition of Done:**
- [ ] `GET /api/conferences` accepts `?search=`, `?from=`, `?to=`, `?status=` query params
- [ ] Absent params apply no filter — all results returned when no filters active
- [ ] Frontend conference list has a search input and date range pickers
- [ ] Filters and pagination work together correctly
- [ ] Empty state message shown when no results match
- [ ] At least two unit tests: one for keyword match, one for date range filter

---

### BEVJ-202 — Session Waitlist
**Role:** Developer

When a session has reached its capacity, an attendee can join a waitlist. If a registered attendee cancels, the first person on the waitlist is automatically promoted to a confirmed registration. This promotion should be logged. The session detail page shows current registration count, capacity, and waitlist count. Attendees can check their waitlist position.

**Definition of Done:**
- [ ] `POST /api/sessions/{id}/waitlist` adds an attendee to the waitlist
- [ ] Registering for a session at capacity returns 409 — does not auto-waitlist
- [ ] Cancelling a confirmed registration triggers automatic promotion of the next waitlisted attendee
- [ ] Promotion is logged at INFO level with attendee ID and session ID
- [ ] `GET /api/sessions/{id}` response includes `registeredCount`, `capacity`, `waitlistCount`
- [ ] Waitlist position visible in `GET /api/sessions/{id}/waitlist/{attendeeId}`
- [ ] Session detail page shows capacity and waitlist count
- [ ] Unit tests cover: join waitlist, cancel triggers promotion, waitlist ordering

---

### BEVJ-203 — Attendee Registration Dashboard
**Role:** Developer

An attendee can view all their conference registrations in one place. The dashboard shows each registration with the conference name, dates, status (confirmed / cancelled / waitlisted), and a cancel button for active registrations. Cancellation from the dashboard triggers the same waitlist promotion logic as BEVJ-202 if that task is complete. The dashboard uses attendee email as the identifier.

**Definition of Done:**
- [ ] `GET /api/attendees/{email}/registrations` returns all registrations with conference context
- [ ] Dashboard page accessible at `/dashboard` with an email input to look up registrations
- [ ] Each registration shows conference name, dates, status
- [ ] Confirmed registrations have a cancel button; cancelled ones are read-only
- [ ] Cancellation refreshes the list
- [ ] Empty state shown when no registrations found for the email

---

### BEVJ-204 — Speaker Profile Page
**Role:** Developer

Add a speaker profile page to the application. The page shows the speaker's full name, bio, and a list of sessions they are presenting at — with conference name and session time for each. Speaker names in session detail should link to this page.

**Definition of Done:**
- [ ] `GET /api/speakers/{id}` returns speaker details including their sessions with conference context
- [ ] Speaker profile page accessible at `/speakers/:id`
- [ ] Speaker name in session detail is a clickable link to the speaker profile
- [ ] Page shows a loading state while fetching and an error state if the request fails
- [ ] At least one unit test for the speaker service method that builds the session list

---

### BEVJ-205 — End-to-End Test Suite
**Role:** QA

Cover the three critical user flows with end-to-end tests using Playwright. Tests run against the live `docker-compose` stack — no mocked API responses for happy-path scenarios. Each flow covers the success path and at least one failure case. The suite is integrated into the GitLab CI pipeline and blocks merging on failure.

**Definition of Done:**
- [ ] Playwright configured in `frontend/` or a dedicated `e2e/` folder
- [ ] Flow 1: Browse conferences → open detail → view sessions
- [ ] Flow 2: Open session detail → register attendee → verify registration appears
- [ ] Flow 3: Open conference search → apply keyword filter → verify results update
- [ ] Each flow includes at least one failure case (e.g. register with empty email, search with no results)
- [ ] Tests pass against `docker-compose up` stack
- [ ] `.gitlab-ci.yml` has an `e2e` job in the `test` stage that starts the stack and runs Playwright
- [ ] Failed test screenshots saved as GitLab CI artifacts
