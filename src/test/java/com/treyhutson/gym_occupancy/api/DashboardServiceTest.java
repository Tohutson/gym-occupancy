package com.treyhutson.gym_occupancy.api;

import com.treyhutson.gym_occupancy.analysis.ComparisonLevel;
import com.treyhutson.gym_occupancy.analysis.HistoricalAnalysis;
import com.treyhutson.gym_occupancy.analysis.HistoricalBaselineService;
import com.treyhutson.gym_occupancy.config.OccupancyProperties;
import com.treyhutson.gym_occupancy.model.FacilityCount;
import com.treyhutson.gym_occupancy.repository.FacilityCountRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Pageable;

import java.time.Clock;
import java.time.DayOfWeek;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class DashboardServiceTest {
    private static final Instant NOW = Instant.parse("2026-08-24T20:00:00Z");
    private FacilityCountRepository repository;
    private HistoricalBaselineService baselineService;
    private DashboardService service;

    @BeforeEach
    void setUp() {
        repository = mock(FacilityCountRepository.class);
        baselineService = mock(HistoricalBaselineService.class);
        OccupancyProperties properties = new OccupancyProperties();
        properties.setSourceZone(ZoneId.of("America/New_York"));
        properties.setStaleAfter(Duration.ofMinutes(30));
        service = new DashboardService(repository, baselineService, properties, Clock.fixed(NOW, ZoneOffset.UTC));
    }

    @Test
    void returnsOneBoundedDashboardPayload() {
        FacilityCount current = measurement("2026-08-24T19:55:00Z", 64);
        current.setRecordedAt(Instant.parse("2026-08-24T19:56:00Z"));
        when(repository.findLatestForFacility(eq("RWC Floor 2"), any(Pageable.class))).thenReturn(List.of(current));
        when(repository.findHistoryForFacility(eq("RWC Floor 2"), any(), any())).thenReturn(List.of(
                measurement("2026-08-24T18:00:00Z", 40), current));
        when(baselineService.analyze(current)).thenReturn(analysis());

        DashboardResponse response = service.dashboard("RWC Floor 2", DashboardRange.TODAY);

        assertEquals(64, response.current().count());
        assertEquals(32.0, response.current().percentOfCapacity(), 0.01);
        assertEquals(40, response.today().minimum());
        assertEquals(64, response.today().maximum());
        assertEquals(2, response.measurements().size());
        assertEquals(4, response.typicalDay().size());
        assertFalse(response.freshness().stale());
        assertEquals(3, response.recommendedVisitWindows().size());
    }

    @Test
    void returnsNotFoundForAnUnknownFacility() {
        when(repository.findLatestForFacility(eq("unknown"), any(Pageable.class))).thenReturn(List.of());

        assertThrows(FacilityNotFoundException.class,
                () -> service.dashboard("unknown", DashboardRange.HOURS_24));
    }

    @Test
    void rejectsAnUnsupportedRange() {
        assertThrows(IllegalArgumentException.class, () -> DashboardRange.fromQuery("30d"));
    }

    private HistoricalAnalysis analysis() {
        List<HistoricalAnalysis.BaselineBucket> buckets = List.of(
                new HistoricalAnalysis.BaselineBucket(LocalTime.of(16, 30), 30, 25, 35, 12, 4),
                new HistoricalAnalysis.BaselineBucket(LocalTime.of(17, 0), 20, 15, 25, 12, 4),
                new HistoricalAnalysis.BaselineBucket(LocalTime.of(18, 0), 25, 20, 30, 12, 4),
                new HistoricalAnalysis.BaselineBucket(LocalTime.of(19, 0), 28, 22, 32, 12, 4));
        var comparison = new HistoricalAnalysis.OccupancyComparison(
                ComparisonLevel.BUSIER, ComparisonLevel.BUSIER.getLabel(), 50.0, 14.0, 28.0, 4);
        return new HistoricalAnalysis(DayOfWeek.MONDAY, buckets, comparison);
    }

    private FacilityCount measurement(String time, int count) {
        FacilityCount value = new FacilityCount();
        value.setFacilityId("RWC Floor 2");
        value.setFacilityName("Operations");
        value.setLocationName("RWC Floor 2");
        value.setTotalCapacity(200);
        value.setLastCount(count);
        value.setLastUpdatedDateAndTime(Instant.parse(time));
        value.setRecordedAt(Instant.parse(time));
        return value;
    }
}
