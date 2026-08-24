package com.treyhutson.gym_occupancy.analysis;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;

public record HistoricalAnalysis(
        DayOfWeek weekday,
        List<BaselineBucket> baseline,
        OccupancyComparison comparison
) {
    public record BaselineBucket(
            LocalTime start,
            double average,
            double lowerQuartile,
            double upperQuartile,
            int sampleCount,
            int sampleDays
    ) {}

    public record OccupancyComparison(
            ComparisonLevel level,
            String label,
            Double expectedCount,
            Double difference,
            Double differencePercent,
            int sampleDays
    ) {
        public static OccupancyComparison insufficient(int sampleDays) {
            ComparisonLevel level = ComparisonLevel.INSUFFICIENT_DATA;
            return new OccupancyComparison(level, level.getLabel(), null, null, null, sampleDays);
        }
    }
}
