# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Commands

All Maven commands must be run from the `backend/` directory.

```bash
cd backend

mvn compile                                          # compile
mvn test                                             # run all tests
mvn test -Dtest=ConferenceServiceTest                # run one test class
mvn test -Dtest=SessionServiceTest#createSession_shouldSetConferenceAndSave  # run one method
mvn package -DskipTests                              # package without tests
mvn spring-boot:run                                  # run locally (requires local Postgres)
```

```bash
docker-compose up --build    # full stack (from project root): Postgres + backend:8080 + frontend:3000
```

Frontend commands (run from `frontend/`):

```bash
cd frontend

npm install       # install dependencies
npm run dev       # dev server on http://localhost:3000 (proxies /api/* to localhost:8080)
npm run build     # production bundle → dist/
npm run preview   # serve dist/ locally
```

There is no lint or test script in the frontend.

## Architecture

BrownEvents is a **conference management app**: a Spring Boot REST API backend + React SPA frontend served by nginx.

### Backend

**Request flow:**

```
HTTP → Controller (@RestController) → Service (@Service) → Repository (JpaRepository) → PostgreSQL
```

**Domain**: Organizers create `Conference` records, add `Session`s (each linked to a `Speaker` and a `Room`), and `Attendee`s register via email (upserted on registration).

**Entry point**: `com.brownevents.app.BrownEventsApplication`

**Package layout** (`com.brownevents.app`):

| Package | Contents |
|---|---|
| `entity/` | 6 JPA entities: `Conference`, `Session`, `Speaker`, `Room`, `Attendee`, `Registration` |
| `repository/` | Spring Data JPA interfaces; `SessionRepository` has a JOIN FETCH JPQL query to avoid N+1 on sessions |
| `service/` | Business logic: `ConferenceService`, `SessionService`, `SpeakerService`, `RegistrationService` |
| `controller/` | REST controllers for conferences, sessions, speakers, registrations |
| Root | `DataInitializer` (seeds DB on boot), `WebConfig` (global CORS), `OpenApiConfig` (Swagger) |

**Key config** (`backend/src/main/resources/application.properties`):
- DB: `jdbc:postgresql://postgres:5432/brownevents` (Docker service name; change to `localhost` for local dev)
- `spring.jpa.hibernate.ddl-auto=update` — Hibernate manages schema
- `spring.jpa.show-sql=true` — all SQL is logged
- Swagger UI: `http://localhost:8080/swagger-ui/index.html`

### Frontend

The frontend lives in `frontend/src/`. Built with Vite 4, React 18, React Router v6. No state management library, no UI component library, no TypeScript — plain `.jsx` with raw `fetch` calls and custom CSS.

**Route table:**

| Path | Page |
|---|---|
| `/` | `ConferenceListPage` — conference grid, inline "New Conference" modal |
| `/conferences/:id` | `ConferenceDetailPage` — sessions list, registrations, inline register modal |
| `/conferences/:id/sessions/:sessionId` | `SessionDetailPage` |
| `/conferences/:id/register` | `RegistrationPage` — standalone register form (lighter than the modal) |

**API layer**: all backend calls are in `frontend/src/api.js`. `BASE_URL` is hardcoded to `http://localhost:8080` — change this or use `VITE_API_URL` for non-local environments. The Vite dev proxy (`vite.config.js`) also forwards `/api/*` to `localhost:8080` but is effectively redundant given the absolute base URL.

**Production**: nginx (`frontend/nginx.conf`) proxies `/api/` to `http://backend:8080/api/` (Docker service name) and serves the static `dist/` bundle with `try_files` fallback for client-side routing.

**Styles**: single global stylesheet at `frontend/src/index.css` using CSS custom properties. Google Fonts: Bebas Neue (headings), Lora (body), Space Mono (labels/metadata).

## Stack

**Backend:** Java 11, Spring Boot 2.7.14, Spring Data JPA / Hibernate, PostgreSQL 14, springdoc-openapi-ui 1.7.0 (Swagger)

**Frontend:** React 18, React Router v6, Vite 4, plain CSS, no TypeScript

**Tests:** JUnit 5 + Mockito (backend only — pure unit tests, no Spring context, no DB; frontend has no tests)

## Tests

Both test classes live in `backend/src/test/java/com/brownevents/app/service/` and use `@ExtendWith(MockitoExtension.class)` with `@Mock` / `@InjectMocks`. They run fast (no `@SpringBootTest`). The frontend has no tests.

## Known Tech Debt

**Backend:**
- Services call `Optional.get()` directly — missing entities throw `NoSuchElementException` instead of a proper 404.
- No `@Transactional` on service methods.
- `@CrossOrigin(origins = "*")` is duplicated on every controller alongside the global `WebConfig` CORS setup.
- `Room` has no API controller; rooms are only accessible via seeded data.

**Frontend:**
- `BASE_URL` in `api.js` is hardcoded to `http://localhost:8080` — not environment-configurable.
- Two registration flows exist: a full modal in `ConferenceDetailPage` (collects phone, company, dietary requirements) and a lighter standalone `RegistrationPage` — they collect different fields.
- Several `api.js` functions do `data.data || data` to work around inconsistent backend response shapes.
