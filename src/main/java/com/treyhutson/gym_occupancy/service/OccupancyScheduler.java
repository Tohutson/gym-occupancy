package com.treyhutson.gym_occupancy.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class OccupancyScheduler {
    private static final Logger log = LoggerFactory.getLogger(OccupancyScheduler.class);

    private final OccupancyCollectionService collectionService;

    public OccupancyScheduler(OccupancyCollectionService collectionService) {
        this.collectionService = collectionService;
    }

    @Scheduled(fixedDelayString = "${occupancy.poll-delay:10m}", initialDelayString = "${occupancy.initial-delay:5s}")
    public void fetchAndStoreOccupancy() {
        try {
            OccupancyCollectionService.CollectionResult result = collectionService.collect();
            log.info("Occupancy collection succeeded: received={}, inserted={}", result.received(), result.inserted());
        } catch (RuntimeException exception) {
            log.warn("Occupancy collection failed: {}", exception.getMessage());
            log.debug("Occupancy collection failure details", exception);
        }
    }
}
