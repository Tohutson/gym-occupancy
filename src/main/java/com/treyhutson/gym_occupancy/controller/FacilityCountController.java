package com.treyhutson.gym_occupancy.controller;

import com.treyhutson.gym_occupancy.model.FacilityCount;
import com.treyhutson.gym_occupancy.repository.FacilityCountRepository;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@CrossOrigin(origins = "http://localhost:3000")
@RestController
@RequestMapping("/api/facilities")
public class FacilityCountController {
    private final FacilityCountRepository repository;

    public FacilityCountController(FacilityCountRepository repository) {
        this.repository = repository;
    }

    // GET all records or filter by locationName if provided
    @GetMapping
    public List<FacilityCount> getFacilities(
            @RequestParam(required = false) String locationName,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate
    ) {
        return repository.findByFilters(locationName, startDate, endDate);
    }

    // GET the latest record for each facility
    @GetMapping("/latest")
    public List<FacilityCount> getLatest() {
        return repository.findLatestPerFacility();
    }


}
