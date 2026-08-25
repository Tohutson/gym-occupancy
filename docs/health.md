# Application Health

Spring Boot Actuator provides the health endpoints.

## Liveness

Send this request:

```text
GET /actuator/health/liveness
```

An `UP` response shows that the web application is running. Liveness does not depend on PostgreSQL or recent occupancy data.

## Readiness

Send this request:

```text
GET /actuator/health/readiness
```

Readiness internally checks these components, while production responses omit component details:

- `db` checks the PostgreSQL connection.
- `occupancyData` checks the age of the latest successful measurement.

The endpoint returns HTTP `503` when PostgreSQL is unavailable, no successful measurement exists, or the latest measurement is stale.

Set `OCCUPANCY_STALE_AFTER` to control the data age limit. The default value is 30 minutes.

The dashboard uses the same data age limit. It shows a stale-data warning when the latest collection is older than the limit.
