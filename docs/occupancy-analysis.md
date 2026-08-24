# Occupancy Analysis

The application compares a current measurement with measurements from the same weekday and time.

## Historical baseline

The analysis uses these rules:

1. Use the configured source timezone.
2. Divide each day into 30-minute buckets.
3. Select measurements from the same weekday.
4. Exclude the current day.
5. Use the previous 56 days by default.
6. Exclude measurements that show a closed facility.
7. Require measurements from at least three different dates for each bucket.

The result contains the mean count, the lower quartile, and the upper quartile for each bucket. The quartile range shows historical variability. The method does not fill a bucket that has insufficient data.

Set `OCCUPANCY_BASELINE_LOOKBACK` to change the lookback period. Set `OCCUPANCY_BASELINE_MINIMUM_DAYS` to change the minimum number of historical dates.

## Current comparison

The comparison uses a typical spread. The typical spread is the largest of these values:

- Four people.
- 15 percent of the historical mean.
- The difference between the upper and lower quartiles.

The application uses these labels:

| Condition | Label |
| --- | --- |
| The difference is inside one typical spread. | About normal |
| The count is below the mean by more than one spread. | Quieter than usual |
| The count is below the mean by at least two spreads. | Much quieter than usual |
| The count is above the mean by more than one spread. | Busier than usual |
| The count is above the mean by at least two spreads. | Much busier than usual |

The minimum spread prevents a small count difference from producing a strong label. The variability range prevents a common fluctuation from producing a strong label.

The application returns `Not enough historical data` when the matching bucket has fewer than the configured number of historical dates. The application does not calculate an expected count in this condition.
