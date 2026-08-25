package com.treyhutson.gym_occupancy.service;

import com.treyhutson.gym_occupancy.config.OccupancyProperties;
import com.treyhutson.gym_occupancy.config.UpstreamServiceException;
import com.treyhutson.gym_occupancy.repository.FacilityCountRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;

@Component
public class OccupancyScheduler {
    private static final Logger log = LoggerFactory.getLogger(OccupancyScheduler.class);

    private final OccupancyCollectionService collectionService;
    private final FacilityCountRepository repository;
    private final OccupancyProperties properties;
    private final Clock clock;

    public OccupancyScheduler(OccupancyCollectionService collectionService, FacilityCountRepository repository,
                              OccupancyProperties properties, Clock clock) {
        this.collectionService = collectionService;
        this.repository = repository;
        this.properties = properties;
        this.clock = clock;
    }

    @Scheduled(fixedDelayString = "${occupancy.poll-delay:5m}", initialDelayString = "${occupancy.initial-delay:30s}")
    public void fetchAndStoreOccupancy() {
        try {
            if (collectionIsNotDue()) {
                return;
            }
            OccupancyCollectionService.CollectionResult result = collectionService.collect();
            log.info("Occupancy collection succeeded: received={}, inserted={}", result.received(), result.inserted());
        } catch (RuntimeException exception) {
            String reason = exception instanceof UpstreamServiceException
                    ? exception.getMessage()
                    : exception.getClass().getSimpleName();
            log.warn("Occupancy collection failed: {}", reason);
        }
    }

    private boolean collectionIsNotDue() {
        Instant latestCollection = repository.findLatestCollectedAt();
        if (latestCollection == null) {
            return false;
        }
        Duration elapsed = Duration.between(latestCollection, clock.instant());
        return !elapsed.isNegative() && elapsed.compareTo(properties.getPollDelay()) < 0;
    }
}
