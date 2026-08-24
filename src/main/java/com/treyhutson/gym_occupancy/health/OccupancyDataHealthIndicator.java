package com.treyhutson.gym_occupancy.health;

import com.treyhutson.gym_occupancy.config.OccupancyProperties;
import com.treyhutson.gym_occupancy.repository.FacilityCountRepository;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;

@Component("occupancyData")
public class OccupancyDataHealthIndicator implements HealthIndicator {
    private final FacilityCountRepository repository;
    private final OccupancyProperties properties;
    private final Clock clock;

    public OccupancyDataHealthIndicator(FacilityCountRepository repository, OccupancyProperties properties, Clock clock) {
        this.repository = repository;
        this.properties = properties;
        this.clock = clock;
    }

    @Override
    public Health health() {
        Instant latest = repository.findLatestCollectedAt();
        if (latest == null) {
            return Health.down().withDetail("reason", "No successful measurement exists").build();
        }

        long ageSeconds = Math.max(0, Duration.between(latest, clock.instant()).getSeconds());
        Health.Builder status = ageSeconds > properties.getStaleAfter().toSeconds() ? Health.down() : Health.up();
        return status
                .withDetail("latestCollectedAt", latest)
                .withDetail("ageSeconds", ageSeconds)
                .withDetail("staleAfterSeconds", properties.getStaleAfter().toSeconds())
                .build();
    }
}
