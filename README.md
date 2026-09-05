# BrownEvents — Conference Management App

BrownEvents is a conference management web application. Organizers can create conferences, add sessions and speakers; attendees can browse conferences and register. The backend is a Spring Boot REST API backed by PostgreSQL; the frontend is a React single-page application built with Vite.

---

## Prerequisites

- Java 11
- Maven 3.8+
- Node.js 18+
- Docker & Docker Compose

---

## Quick Start (Docker)

```bash
docker-compose up --build
```

Once all services are healthy, open http://localhost:3000

> **Note:** In Docker mode the frontend is compiled into a static bundle and served by **nginx** inside the container (port 80), which Docker maps to host port **3000**. This is different from the Vite dev server used in the manual setup below.

---

## Manual Setup (without Docker)

### Backend

```bash
cd backend && mvn spring-boot:run
```

The backend starts on http://localhost:8080. It requires a running PostgreSQL instance reachable at the coordinates in `backend/src/main/resources/application.properties`.

### Frontend

```bash
cd frontend && npm install && npm run dev
```

The frontend dev server starts on http://localhost:5173.


