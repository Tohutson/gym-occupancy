package com.treyhutson.gym_occupancy.api;

import com.treyhutson.gym_occupancy.analysis.HistoricalAnalysis.OccupancyComparison;

import java.time.Instant;
import java.util.List;

public record DashboardResponse(
        Instant generatedAt,
        String timezone,
        String range,
        Facility facility,
        CurrentOccupancy current,
        OccupancyComparison comparison,
        Freshness freshness,
        TodaySummary today,
        List<MeasurementPoint> measurements,
        List<BaselinePoint> typicalDay,
        List<VisitWindow> recommendedVisitWindows
) {
    public record Facility(String id, String name, String locationName, int capacity) {}
    public record CurrentOccupancy(int count, double percentOfCapacity, boolean closed, Instant measuredAt) {}
    public record Freshness(Instant collectedAt, long ageSeconds, boolean stale) {}
    public record TodaySummary(Integer minimum, Integer maximum, Double average, Instant peakAt) {}
    public record MeasurementPoint(Instant time, Integer count) {}
    public record BaselinePoint(Instant time, double average, double lowerQuartile, double upperQuartile, int sampleDays) {}
    public record VisitWindow(Instant start, Instant end, double expectedCount) {}
}
