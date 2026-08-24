package com.treyhutson.gym_occupancy.health;

import com.treyhutson.gym_occupancy.config.OccupancyProperties;
import com.treyhutson.gym_occupancy.repository.FacilityCountRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.actuate.health.Status;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class OccupancyDataHealthIndicatorTest {
    private static final Instant NOW = Instant.parse("2026-08-24T20:00:00Z");
    private FacilityCountRepository repository;
    private OccupancyDataHealthIndicator indicator;

    @BeforeEach
    void setUp() {
        repository = mock(FacilityCountRepository.class);
        OccupancyProperties properties = new OccupancyProperties();
        properties.setStaleAfter(Duration.ofMinutes(30));
        indicator = new OccupancyDataHealthIndicator(repository, properties, Clock.fixed(NOW, ZoneOffset.UTC));
    }

    @Test
    void reportsUpWhenARecentCollectionExists() {
        when(repository.findLatestCollectedAt()).thenReturn(NOW.minus(Duration.ofMinutes(10)));

        assertEquals(Status.UP, indicator.health().getStatus());
    }

    @Test
    void reportsDownWhenCollectionDataIsStale() {
        when(repository.findLatestCollectedAt()).thenReturn(NOW.minus(Duration.ofHours(1)));

        assertEquals(Status.DOWN, indicator.health().getStatus());
        assertEquals(3600L, indicator.health().getDetails().get("ageSeconds"));
    }

    @Test
    void reportsDownWhenNoCollectionHasSucceeded() {
        when(repository.findLatestCollectedAt()).thenReturn(null);

        assertEquals(Status.DOWN, indicator.health().getStatus());
    }
}
