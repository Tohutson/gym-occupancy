package com.treyhutson.gym_occupancy.model;

public class Facility {
    private String facilityName;
    private int currentCount;
    private int maxCapacity;

    // Default constructor (needed for Jackson / RestTemplate)
    public Facility() {}

    // Getters and setters
    public String getFacilityName() {
        return facilityName;
    }

    public void setFacilityName(String facilityName) {
        this.facilityName = facilityName;
    }

    public int getCurrentCount() {
        return currentCount;
    }

    public void setCurrentCount(int currentCount) {
        this.currentCount = currentCount;
    }

    public int getMaxCapacity() {
        return maxCapacity;
    }

    public void setMaxCapacity(int maxCapacity) {
        this.maxCapacity = maxCapacity;
    }

    @Override
    public String toString() {
        return facilityName + ": " + currentCount + "/" + maxCapacity;
    }
}
