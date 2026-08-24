package com.treyhutson.gym_occupancy.api;

import java.time.Duration;

public enum DashboardRange {
    TODAY(null),
    HOURS_24(Duration.ofHours(24)),
    DAYS_7(Duration.ofDays(7));

    private final Duration duration;

    DashboardRange(Duration duration) {
        this.duration = duration;
    }

    public Duration getDuration() {
        return duration;
    }

    public static DashboardRange fromQuery(String value) {
        return switch (value == null ? "today" : value.toLowerCase()) {
            case "today" -> TODAY;
            case "24h" -> HOURS_24;
            case "7d" -> DAYS_7;
            default -> throw new IllegalArgumentException("range must be one of: today, 24h, 7d");
        };
    }
}
