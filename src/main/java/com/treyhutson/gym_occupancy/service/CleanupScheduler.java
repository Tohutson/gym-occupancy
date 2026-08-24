package com.treyhutson.gym_occupancy.service;

import com.treyhutson.gym_occupancy.config.OccupancyProperties;
import com.treyhutson.gym_occupancy.repository.FacilityCountRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.Instant;

@Component
public class CleanupScheduler {
    private static final Logger log = LoggerFactory.getLogger(CleanupScheduler.class);

    private final FacilityCountRepository repository;
    private final OccupancyProperties properties;
    private final Clock clock;

    public CleanupScheduler(FacilityCountRepository repository, OccupancyProperties properties, Clock clock) {
        this.repository = repository;
        this.properties = properties;
        this.clock = clock;
    }

    @Scheduled(cron = "${occupancy.cleanup-cron:0 0 3 * * *}", zone = "UTC")
    public void cleanOldRecords() {
        Instant cutoff = clock.instant().minus(properties.getRetention());
        int deleted = repository.deleteByRecordedAtBefore(cutoff);
        if (deleted > 0) {
            log.info("Deleted {} measurements older than {}", deleted, cutoff);
        }
    }
}
