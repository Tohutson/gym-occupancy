package com.treyhutson.gym_occupancy.service;

import com.treyhutson.gym_occupancy.model.FacilityCount;
import com.treyhutson.gym_occupancy.repository.FacilityCountRepository;
import com.treyhutson.gym_occupancy.service.FacilityOccupancyService;
import com.treyhutson.gym_occupancy.model.Facility;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Component
public class OccupancyScheduler {

    private final FacilityOccupancyService facilityOccupancyService;
    private final FacilityCountRepository repository;

    public OccupancyScheduler(FacilityOccupancyService facilityOccupancyService,
                              FacilityCountRepository repository) {
        this.facilityOccupancyService = facilityOccupancyService;
        this.repository = repository;
    }

    @Scheduled(fixedRate = 300_000) // every 5 minutes
    public void fetchAndStoreOccupancy() {
        List<Facility> facilities = facilityOccupancyService.fetchOccupancyData();

        facilities.forEach(f -> {
            FacilityCount entity = toEntity(f);
            repository.save(entity); // Save historical record
            System.out.println("[" + f.getLastUpdated() + "] Saved: " + f);
        });
    }

    public FacilityCount toEntity(Facility facility) {
        FacilityCount entity = new FacilityCount();
        entity.setLocationName(facility.getLocationName());
        entity.setTotalCapacity(facility.getTotalCapacity());
        entity.setLastCount(facility.getLastCount());
        entity.setClosed(facility.isClosed());

        DateTimeFormatter formatter = DateTimeFormatter.ISO_DATE_TIME;
        entity.setLastUpdatedDateAndTime(LocalDateTime.parse(facility.getLastUpdated(), formatter));

        entity.setRecordedAt(LocalDateTime.now()); // timestamp for trend analysis
        return entity;
    }
}

