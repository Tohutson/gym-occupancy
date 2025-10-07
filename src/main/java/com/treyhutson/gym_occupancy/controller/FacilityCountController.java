package com.treyhutson.gym_occupancy.controller;

import com.treyhutson.gym_occupancy.model.FacilityCount;
import com.treyhutson.gym_occupancy.repository.FacilityCountRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/facilities")
public class FacilityCountController {
    private final FacilityCountRepository repository;

    public FacilityCountController(FacilityCountRepository repository) {
        this.repository = repository;
    }

    // GET all records
    @GetMapping
    public List<FacilityCount> getAll() {
        return repository.findAll();
    }

    // GET the latest record for each facility
    @GetMapping("/latest")
    public List<FacilityCount> getLatest() {
        return repository.findLatestPerFacility();
    }
}
