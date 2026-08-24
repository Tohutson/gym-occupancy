package com.treyhutson.gym_occupancy.service;

import com.treyhutson.gym_occupancy.config.OccupancyProperties;
import com.treyhutson.gym_occupancy.config.UpstreamServiceException;
import com.treyhutson.gym_occupancy.model.Facility;
import com.treyhutson.gym_occupancy.model.FacilityCount;
import com.treyhutson.gym_occupancy.repository.FacilityCountRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeParseException;
import java.util.List;

@Service
public class OccupancyCollectionService {
    private final ExternalFacilityService externalFacilityService;
    private final FacilityCountRepository repository;
    private final OccupancyProperties properties;
    private final Clock clock;

    public OccupancyCollectionService(ExternalFacilityService externalFacilityService, FacilityCountRepository repository,
                                      OccupancyProperties properties, Clock clock) {
        this.externalFacilityService = externalFacilityService;
        this.repository = repository;
        this.properties = properties;
        this.clock = clock;
    }

    @Transactional
    public CollectionResult collect() {
        List<Facility> facilities = externalFacilityService.fetchOccupancyData();
        Instant recordedAt = clock.instant();
        int inserted = 0;
        for (Facility facility : facilities) {
            FacilityCount measurement = toEntity(facility, recordedAt);
            inserted += repository.insertIgnoreDuplicates(
                    measurement.getFacilityId(), measurement.getFacilityName(), measurement.getLocationName(),
                    measurement.getTotalCapacity(), measurement.getLastCount(), measurement.isClosed(),
                    measurement.getLastUpdatedDateAndTime(), measurement.getRecordedAt());
        }
        return new CollectionResult(facilities.size(), inserted, recordedAt);
    }

    FacilityCount toEntity(Facility facility, Instant recordedAt) {
        FacilityCount measurement = new FacilityCount();
        measurement.setFacilityId(facility.getLocationName());
        measurement.setFacilityName(facility.getFacilityName());
        measurement.setLocationName(facility.getLocationName());
        measurement.setTotalCapacity(facility.getTotalCapacity());
        measurement.setLastCount(facility.getLastCount());
        measurement.setClosed(facility.isClosed());
        measurement.setLastUpdatedDateAndTime(parseSourceTimestamp(facility.getLastUpdated()));
        measurement.setRecordedAt(recordedAt);
        return measurement;
    }

    private Instant parseSourceTimestamp(String value) {
        try {
            return OffsetDateTime.parse(value).toInstant();
        } catch (DateTimeParseException ignored) {
            try {
                return LocalDateTime.parse(value).atZone(properties.getSourceZone()).toInstant();
            } catch (DateTimeParseException exception) {
                throw new UpstreamServiceException("The occupancy service returned an invalid timestamp", exception);
            }
        }
    }

    public record CollectionResult(int received, int inserted, Instant collectedAt) {}
}
