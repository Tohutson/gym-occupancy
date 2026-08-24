package com.treyhutson.gym_occupancy.api;

import java.time.Instant;

public record FacilityOption(String id, String name, String locationName, int capacity, Instant measuredAt) {}
