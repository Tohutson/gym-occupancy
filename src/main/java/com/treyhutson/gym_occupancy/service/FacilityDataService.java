package com.treyhutson.gym_occupancy.service;

import com.treyhutson.gym_occupancy.model.FacilityCount;
import com.treyhutson.gym_occupancy.repository.FacilityCountRepository;
import org.springframework.stereotype.Service;

import java.sql.Time;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Service
public class FacilityDataService {
    private final FacilityCountRepository repository;

    public FacilityDataService(FacilityCountRepository repository) {
        this.repository = repository;
    }

    public List<FacilityCount> getLatestPerFacility() {
        return repository.findLatestPerFacility();
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

    public Double getAverage(String locationName, LocalTime time) {
        LocalTime startTime = time.minusMinutes(30);
        LocalTime endTime = time.plusMinutes(30);
        return repository.findAverageLastCountByLocationAndTime(locationName, startTime, endTime);
    }
}
