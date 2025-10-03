package com.treyhutson.gym_occupancy.service;

import com.treyhutson.gym_occupancy.service.FacilityOccupancyService;
import com.treyhutson.gym_occupancy.model.Facility;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class OccupancyScheduler {

    private final FacilityOccupancyService facilityOccupancyService;

    public OccupancyScheduler(FacilityOccupancyService facilityOccupancyService) {
        this.facilityOccupancyService = facilityOccupancyService;
    }

    // Runs every 5 minutes
    @Scheduled(fixedRate = 300_000)
    public void fetchAndLogOccupancy() {
        List<Facility> facilities = facilityOccupancyService.fetchOccupancyData();
        facilities.forEach(f ->
                System.out.println("[" + f.getLastUpdated() + "] " + f)
        );
    }
}
