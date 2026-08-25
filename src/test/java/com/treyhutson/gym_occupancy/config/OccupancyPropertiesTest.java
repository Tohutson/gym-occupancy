package com.treyhutson.gym_occupancy.config;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OccupancyPropertiesTest {
    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    void pollDelayCannotBeConfiguredBelowFiveMinutes() {
        OccupancyProperties properties = new OccupancyProperties();
        properties.setPollDelay(Duration.ofMinutes(4));
        assertFalse(validator.validate(properties).isEmpty());

        properties.setPollDelay(Duration.ofMinutes(5));
        assertTrue(validator.validate(properties).isEmpty());
    }
}
