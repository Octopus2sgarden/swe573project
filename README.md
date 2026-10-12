# Indigenous Crafts Platform – SWE 573 Project

An informative web application where users document Indigenous and traditional crafts: their stories, materials, production techniques, colors and dimensions, and the places where they originate or can be seen. Users can explore crafts on a map, search and compare them, and interact with other contributors.

This repository is developed for the SWE 573 course at Boğaziçi University by **Seda Güneş** (MSc Software Engineering; BSc Mathematics, Boğaziçi University).

- 📖 [Wiki](https://github.com/Octopus2sgarden/swe573project/wiki): requirements, user stories, use cases, technology stack, roadmap
- ✅ [Issues](https://github.com/Octopus2sgarden/swe573project/issues) and [Project Board](https://github.com/users/Octopus2sgarden/projects/3)

## Technology Stack

| Part | Technology |
|---|---|
| Backend | Java 25, Spring Boot 4.1, Maven |
| Database | PostgreSQL 17 with PostGIS, Flyway migrations |
| Frontend | Static HTML, Alpine.js, Pico CSS (from a CDN, no Node.js), served by Nginx |
| Infrastructure | Docker Compose |

The reasons for these choices are on the [Technology Stack](https://github.com/Octopus2sgarden/swe573project/wiki/Technology-Stack) wiki page.

## Project Structure

```
swe573project/
├── backend/                 # Spring Boot REST API
│   ├── src/main/java/...    # application code
│   ├── src/main/resources/
│   │   ├── application.yml
│   │   └── db/migration/    # Flyway SQL migrations (V1__..., V2__...)
│   ├── src/test/java/...    # tests
│   └── Dockerfile
├── frontend/                # static pages served by Nginx
│   ├── index.html
│   ├── pages/               # other pages
│   ├── js/                  # config.js (API URL), api.js, layout.js
│   ├── css/
│   ├── nginx.conf           # serves pages and forwards /api to the backend
│   └── Dockerfile
├── docker-compose.yml       # db + backend + frontend
└── .env.example             # environment variables
```

## Running Locally

The only requirement is **Docker** (Docker Desktop on Windows and macOS).

```bash
# 1. Create your environment file (optional, the defaults also work)
cp .env.example .env

# 2. Build and start the database, backend and frontend
docker compose up --build
```

The first build downloads dependencies and takes a few minutes. Then open:

| What | URL |
|---|---|
| Frontend | http://localhost:8000 |
| Backend health check | http://localhost:8080/health |
| Health check through the frontend | http://localhost:8000/api/health |

`/health` returns `200` with `{"status":"UP","database":"UP"}` when the backend is connected to the database, and `503` when it is not. The home page shows the same status.

Useful commands:

```bash
docker compose down        # stop everything
docker compose down -v     # stop and delete the database data
docker compose logs -f backend
```

### Running the backend without Docker

Requires Java 25 and Maven, and a running database (for example `docker compose up db`):

```bash
cd backend
mvn spring-boot:run        # starts on http://localhost:8080
mvn test                   # runs the tests
```

## Database Migrations

The schema is managed by Flyway. Add a new file to `backend/src/main/resources/db/migration` named `V<number>__<description>.sql` (for example `V2__create_users.sql`). It runs automatically when the backend starts. Never edit a migration that has already been applied; add a new one instead.
