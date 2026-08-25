# Occupancy Collector

The collector requests measurements from one upstream occupancy endpoint. The collector stores valid measurements in PostgreSQL.

## Runtime sequence

1. The scheduler waits for `occupancy.initial-delay`.
2. The HTTP client requests the configured endpoint.
3. The client makes one bounded request. A failure waits for the next scheduled collection.
4. The collector validates each facility response.
5. The collector converts the source timestamp to a UTC instant.
6. The collector writes the response in one database transaction.
7. The unique facility and source timestamp key rejects duplicate measurements.

A failed request does not create a measurement. A valid zero count creates a measurement with `last_count` set to `0`.

The scheduler logs one information message after a successful collection. The scheduler logs one warning after a failed collection. A failed collection does not stop the next scheduled run.

## Required configuration

Set these environment variables before you start the application:

| Variable | Purpose |
| --- | --- |
| `OCCUPANCY_API_URL` | Sets the complete upstream endpoint URL. |
| `DATABASE_URL` | Sets the PostgreSQL JDBC URL. |
| `DB_USERNAME` | Sets the PostgreSQL user. |
| `DB_PASSWORD` | Sets the PostgreSQL password. |

Do not commit the upstream API key or the database password.

## Optional configuration

| Variable | Default | Purpose |
| --- | --- | --- |
| `OCCUPANCY_SOURCE_ZONE` | `America/New_York` | Interprets a source timestamp that has no UTC offset. |
| `OCCUPANCY_CONNECT_TIMEOUT` | `5s` | Limits the TCP connection wait. |
| `OCCUPANCY_READ_TIMEOUT` | `10s` | Limits the response wait. |
| `OCCUPANCY_POLL_DELAY` | `5m` | Sets the delay after one collection completes. Values below five minutes are rejected at startup. |
| `OCCUPANCY_RETENTION` | `730d` | Sets the measurement retention period. |

Use duration values that Spring Boot can parse. Examples are `30s`, `5m`, and `2h`.

The scheduler uses a fixed delay, so executions do not overlap. It also checks the latest successful collection time before calling the upstream service, preventing a quick application restart from producing a duplicate request burst.

## Database migrations

Flyway applies the SQL migrations during application startup. Hibernate validates the mapped schema. Hibernate does not create or update the production schema.

The first migration adds these indexes:

- A unique index on `facility_id` and `last_updated_date_and_time`.
- A query index on `facility_id` and `recorded_at`.
- A retention index on `recorded_at`.

The migration interprets existing timestamps without a UTC offset as `America/New_York` time. Set `OCCUPANCY_SOURCE_ZONE` for new source measurements. Review the migration before you apply it if the existing database used a different timezone.
