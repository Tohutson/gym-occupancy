# Architecture

## Components

The application has four runtime components:

1. The collector requests occupancy data and validates the response.
2. PostgreSQL stores valid measurements.
3. The Spring Boot API calculates bounded dashboard responses.
4. The React dashboard presents the response.

The production Docker image includes the compiled React files in the Spring Boot JAR. Spring Boot serves the page and the API on port `8080`.

## Data flow

```text
Upstream occupancy service
        |
        v
ExternalFacilityService
        |
        v
OccupancyCollectionService -> PostgreSQL <- DashboardService
                                      |             |
                                      v             v
                                 health checks   React dashboard
```

`ExternalFacilityService` owns the HTTP timeout, retry, and response validation behavior. `OccupancyCollectionService` owns timestamp conversion and transactional persistence. A failed collection does not create a row.

`HistoricalBaselineService` reads a bounded eight-week period for one facility. `DashboardService` reads at most seven days of chart measurements for one facility. Normal dashboard requests do not load the full measurement table.

## Time handling

The application stores source and collection timestamps as UTC instants. `OCCUPANCY_SOURCE_ZONE` controls these operations:

- Interpret a source timestamp that does not contain a UTC offset.
- Select the local weekday.
- Select the local 30-minute baseline bucket.
- Calculate the start of the local day.

The default source timezone is `America/New_York`.

## Database changes

Flyway owns production schema changes. Hibernate validates the result. Add a numbered migration in `src/main/resources/db/migration` for each schema change.

The main query index starts with `facility_id`. Dashboard and baseline requests select one facility before they select a time range.
