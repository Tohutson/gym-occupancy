package com.treyhutson.gym_occupancy.service;

import com.treyhutson.gym_occupancy.model.FacilityCount;
import com.treyhutson.gym_occupancy.repository.FacilityCountRepository;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class FacilityDataService {
    private final FacilityCountRepository repository;

    public FacilityDataService(FacilityCountRepository repository) {
        this.repository = repository;
    }

    public List<FacilityCount> getFiltered(String locationName, LocalDateTime start, LocalDateTime end) {
        if (start == null && end == null && locationName == null) {
            return repository.findAll();
        }

        if (start == null) start = LocalDateTime.MIN;
        if (end == null) end = LocalDateTime.MAX;

        if (locationName == null) {
            return repository.findBetweenDates(start, end);
        }

        return repository.findByLocationAndDateRange(locationName, start, end);
    }
}
