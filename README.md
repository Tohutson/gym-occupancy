# Gym Occupancy Tracker

Gym Occupancy Tracker collects facility occupancy measurements and shows them in a single-page dashboard. The dashboard compares the current count with the same weekday and time in recent history.

## Features

- Collects occupancy data on a configurable schedule.
- Retries temporary upstream failures with bounded exponential backoff.
- Stores UTC timestamps and rejects duplicate measurements.
- Compares the current count with a same-weekday 30-minute baseline.
- Shows measured occupancy, a typical curve, and a historical quartile range.
- Suggests lower-occupancy periods later in the day.
- Reports application, database, and data freshness health.
- Runs as one application container and one PostgreSQL container.

## Architecture

The production application uses these components:

```text
Occupancy endpoint -> Spring collector -> PostgreSQL
                              |
Browser <- React static files <- Spring dashboard API
```

The Docker image builds the React application and includes the result in the Spring Boot JAR. The browser and API use the same origin.

## Requirements

For the container deployment, install these tools:

- Docker Engine.
- Docker Compose v2.

For local development, also install Java 17 and Node.js 22.

## Start with Docker Compose

1. Copy `.env.example` to `.env`.
2. Set `POSTGRES_PASSWORD` to a long random value.
3. Set `OCCUPANCY_API_URL` to the complete upstream endpoint URL.
4. Run `docker compose up --build -d`.
5. Run `docker compose ps`.
6. Open `http://localhost:8080`.
7. Run `curl http://localhost:8080/actuator/health/readiness`.

The readiness endpoint returns `DOWN` until the collector stores its first valid measurement.

## Stop the application

Run this command:

```bash
docker compose down
```

This command keeps the PostgreSQL volume. Do not add `--volumes` unless you want to delete all stored measurements.

## Test and build

Run the backend tests:

```bash
./mvnw test
```

Run the frontend tests and build:

```bash
npm --prefix gym-occupancy-frontend test -- --watchAll=false
npm --prefix gym-occupancy-frontend run build
```

Build the production image:

```bash
docker compose build
```

## Documentation

- [Collector configuration](docs/collector.md)
- [Occupancy analysis](docs/occupancy-analysis.md)
- [Dashboard API](docs/dashboard-api.md)
- [Dashboard behavior](docs/dashboard.md)
- [Health checks](docs/health.md)
- [Linux operations](docs/linux-deployment.md)
- [Cloudflare Tunnel](docs/cloudflare-tunnel.md)
- [Architecture](docs/architecture.md)
