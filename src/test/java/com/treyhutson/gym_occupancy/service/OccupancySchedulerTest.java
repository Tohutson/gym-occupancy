package com.treyhutson.gym_occupancy.service;

import com.treyhutson.gym_occupancy.config.OccupancyProperties;
import com.treyhutson.gym_occupancy.repository.FacilityCountRepository;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class OccupancySchedulerTest {
    private static final Instant NOW = Instant.parse("2026-08-24T20:00:00Z");

    @Test
    void containsCollectionFailuresSoTheSchedulerCanRunAgain() {
        OccupancyCollectionService collectionService = mock(OccupancyCollectionService.class);
        FacilityCountRepository repository = mock(FacilityCountRepository.class);
        when(collectionService.collect()).thenThrow(new RuntimeException("temporary failure"));
        OccupancyScheduler scheduler = scheduler(collectionService, repository);

        assertDoesNotThrow(scheduler::fetchAndStoreOccupancy);
    }

    @Test
    void skipsTheUpstreamCallWhenARecentCollectionExistsAfterRestart() {
        OccupancyCollectionService collectionService = mock(OccupancyCollectionService.class);
        FacilityCountRepository repository = mock(FacilityCountRepository.class);
        when(repository.findLatestCollectedAt()).thenReturn(NOW.minus(Duration.ofMinutes(2)));

        scheduler(collectionService, repository).fetchAndStoreOccupancy();

        verify(collectionService, never()).collect();
    }

    private OccupancyScheduler scheduler(OccupancyCollectionService collectionService,
                                         FacilityCountRepository repository) {
        OccupancyProperties properties = new OccupancyProperties();
        properties.setPollDelay(Duration.ofMinutes(5));
        return new OccupancyScheduler(collectionService, repository, properties,
                Clock.fixed(NOW, ZoneOffset.UTC));
    }
}
