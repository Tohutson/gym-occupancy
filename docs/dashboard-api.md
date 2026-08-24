# Dashboard API

The dashboard API returns bounded data for the single-page dashboard. The API does not return persistence entities.

## List facilities

Send this request:

```text
GET /api/dashboard/facilities
```

The response contains the latest known facility name, capacity, and measurement time. Use the `id` value in a dashboard request.

## Get dashboard data

Send this request:

```text
GET /api/dashboard?facilityId=RWC%20Floor%202&range=today
```

The `facilityId` parameter is required. The `range` parameter accepts these values:

| Value | Measurement period |
| --- | --- |
| `today` | From local midnight to the current time. |
| `24h` | The previous 24 hours. |
| `7d` | The previous seven days. |

The response contains these sections:

- `current` contains the latest count, capacity percentage, closed state, and source measurement time.
- `comparison` contains the historical comparison and its data sufficiency.
- `freshness` contains the database collection time and age.
- `today` contains the current day's minimum, maximum, average, and peak time.
- `measurements` contains the selected bounded time range.
- `typicalDay` contains the weekday baseline and quartile range.
- `recommendedVisitWindows` contains up to three later 30-minute buckets with the lowest expected counts.

The API returns HTTP `400` for an invalid parameter. The API returns HTTP `404` when the facility has no measurements.

Set `OCCUPANCY_STALE_AFTER` to control the `freshness.stale` threshold. The default value is `30m`.
