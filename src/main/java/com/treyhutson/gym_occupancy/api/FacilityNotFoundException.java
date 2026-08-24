package com.treyhutson.gym_occupancy.api;

public class FacilityNotFoundException extends RuntimeException {
    public FacilityNotFoundException(String facilityId) {
        super("No measurements exist for facility: " + facilityId);
    }
}
