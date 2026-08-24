package com.treyhutson.gym_occupancy.analysis;

public enum ComparisonLevel {
    MUCH_QUIETER("Much quieter than usual"),
    QUIETER("Quieter than usual"),
    NORMAL("About normal"),
    BUSIER("Busier than usual"),
    MUCH_BUSIER("Much busier than usual"),
    INSUFFICIENT_DATA("Not enough historical data");

    private final String label;

    ComparisonLevel(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
