package com.treyhutson.gym_occupancy.service;

import com.treyhutson.gym_occupancy.config.OccupancyProperties;
import com.treyhutson.gym_occupancy.model.Facility;
import com.treyhutson.gym_occupancy.repository.FacilityCountRepository;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

class OccupancyCollectionServiceTest {
    private static final Instant COLLECTED_AT = Instant.parse("2026-08-24T19:05:00Z");

    @Test
    void convertsOffsetTimestampToUtc() {
        OccupancyCollectionService service = service(ZoneId.of("America/New_York"));
        Facility facility = facility("2026-08-24T15:00:00-04:00");

        var measurement = service.toEntity(facility, COLLECTED_AT);

        assertEquals("RWC Floor 2", measurement.getFacilityId());
        assertEquals(Instant.parse("2026-08-24T19:00:00Z"), measurement.getLastUpdatedDateAndTime());
        assertEquals(COLLECTED_AT, measurement.getRecordedAt());
    }

    @Test
    void appliesConfiguredZoneWhenSourceTimestampHasNoOffset() {
        OccupancyCollectionService service = service(ZoneId.of("America/New_York"));

        var measurement = service.toEntity(facility("2026-01-12T15:00:00"), COLLECTED_AT);

        assertEquals(Instant.parse("2026-01-12T20:00:00Z"), measurement.getLastUpdatedDateAndTime());
    }

    private OccupancyCollectionService service(ZoneId sourceZone) {
        OccupancyProperties properties = new OccupancyProperties();
        properties.setSourceZone(sourceZone);
        return new OccupancyCollectionService(
                mock(ExternalFacilityService.class),
                mock(FacilityCountRepository.class),
                properties,
                Clock.fixed(COLLECTED_AT, ZoneOffset.UTC));
    }

    private Facility facility(String timestamp) {
        Facility facility = new Facility();
        facility.setFacilityName("Operations");
        facility.setLocationName("RWC Floor 2");
        facility.setLastCount(42);
        facility.setTotalCapacity(200);
        facility.setLastUpdated(timestamp);
        return facility;
    }
}
