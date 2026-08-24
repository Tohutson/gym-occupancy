package com.treyhutson.gym_occupancy.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class OccupancySchedulerTest {

    @Test
    void containsCollectionFailuresSoTheSchedulerCanRunAgain() {
        OccupancyCollectionService collectionService = mock(OccupancyCollectionService.class);
        when(collectionService.collect()).thenThrow(new RuntimeException("temporary failure"));
        OccupancyScheduler scheduler = new OccupancyScheduler(collectionService);

        assertDoesNotThrow(scheduler::fetchAndStoreOccupancy);
    }
}
