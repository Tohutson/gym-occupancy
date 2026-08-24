package com.treyhutson.gym_occupancy.analysis;

import com.treyhutson.gym_occupancy.config.OccupancyProperties;
import com.treyhutson.gym_occupancy.model.FacilityCount;
import com.treyhutson.gym_occupancy.repository.FacilityCountRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class HistoricalBaselineServiceTest {
    private FacilityCountRepository repository;
    private HistoricalBaselineService service;

    @BeforeEach
    void setUp() {
        repository = mock(FacilityCountRepository.class);
        OccupancyProperties properties = new OccupancyProperties();
        properties.setSourceZone(ZoneId.of("America/New_York"));
        properties.setBaselineMinimumDays(3);
        service = new HistoricalBaselineService(repository, properties);
    }

    @Test
    void comparesAgainstTheSameWeekdayAndThirtyMinuteBucket() {
        FacilityCount current = measurement("2026-08-24T14:12:00Z", 60);
        List<FacilityCount> history = List.of(
                measurement("2026-08-03T14:02:00Z", 30),
                measurement("2026-08-03T14:18:00Z", 34),
                measurement("2026-08-10T14:08:00Z", 36),
                measurement("2026-08-10T14:22:00Z", 40),
                measurement("2026-08-17T14:05:00Z", 38),
                measurement("2026-08-17T14:20:00Z", 42),
                measurement("2026-08-18T14:10:00Z", 100));
        when(repository.findHistoryForFacility(
                eq("RWC Floor 2"), any(), any())).thenReturn(history);

        HistoricalAnalysis analysis = service.analyze(current);

        assertEquals(DayOfWeek.MONDAY, analysis.weekday());
        assertEquals(LocalTime.of(10, 0), analysis.baseline().get(0).start());
        assertEquals(36.666, analysis.baseline().get(0).average(), 0.01);
        assertEquals(3, analysis.baseline().get(0).sampleDays());
        assertEquals(ComparisonLevel.MUCH_BUSIER, analysis.comparison().level());
    }

    @Test
    void reportsInsufficientDataWhenTooFewHistoricalDaysExist() {
        FacilityCount current = measurement("2026-08-24T14:12:00Z", 20);
        when(repository.findHistoryForFacility(
                eq("RWC Floor 2"), any(), any())).thenReturn(List.of(
                measurement("2026-08-10T14:08:00Z", 20),
                measurement("2026-08-17T14:05:00Z", 21)));

        HistoricalAnalysis analysis = service.analyze(current);

        assertEquals(ComparisonLevel.INSUFFICIENT_DATA, analysis.comparison().level());
        assertNull(analysis.comparison().expectedCount());
    }

    @Test
    void usesTheVariabilityBandToAvoidFalsePrecision() {
        var bucket = new HistoricalAnalysis.BaselineBucket(LocalTime.NOON, 50, 42, 58, 12, 4);

        assertEquals(ComparisonLevel.NORMAL, service.compare(64, bucket).level());
        assertEquals(ComparisonLevel.BUSIER, service.compare(70, bucket).level());
        assertEquals(ComparisonLevel.MUCH_QUIETER, service.compare(10, bucket).level());
    }

    private FacilityCount measurement(String timestamp, int count) {
        FacilityCount measurement = new FacilityCount();
        measurement.setFacilityId("RWC Floor 2");
        measurement.setLocationName("RWC Floor 2");
        measurement.setLastCount(count);
        measurement.setLastUpdatedDateAndTime(Instant.parse(timestamp));
        measurement.setRecordedAt(Instant.parse(timestamp));
        return measurement;
    }
}
