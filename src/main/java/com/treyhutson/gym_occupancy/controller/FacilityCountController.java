package com.treyhutson.gym_occupancy.controller;

import com.treyhutson.gym_occupancy.model.FacilityCount;
import com.treyhutson.gym_occupancy.repository.FacilityCountRepository;
import com.treyhutson.gym_occupancy.service.FacilityDataService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@CrossOrigin(origins = "http://localhost:3000")
@RestController
@RequestMapping("/api/facilities")
public class FacilityCountController {
    private final FacilityCountRepository repository;
    private final FacilityDataService facilityDataService;

    public FacilityCountController(FacilityCountRepository repository, FacilityDataService facilityDataService) {
        this.repository = repository;
        this.facilityDataService = facilityDataService;
    }

    // GET all records or filter by locationName, start and end date if provided
    @GetMapping
    public List<FacilityCount> getFiltered(
            @RequestParam(required = false) String locationName,
            @RequestParam(required = false) LocalDateTime start,
            @RequestParam(required = false) LocalDateTime end) {

        return facilityDataService.getFiltered(locationName, start, end);
    }

    // GET the latest record for each facility
    @GetMapping("/latest")
    public List<FacilityCount> getLatest() {
        return repository.findLatestPerFacility();
    }


}
