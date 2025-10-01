package com.treyhutson.gym_occupancy.model;

import java.time.LocalDateTime;

public class DataPoint {
    private String facilityName;
    private int count;
    private LocalDateTime timestamp;

    public DataPoint(String facilityName, int count, LocalDateTime timestamp) {
        this.facilityName = facilityName;
        this.count = count;
        this.timestamp = timestamp;
    }

    public String getFacilityName() {
        return facilityName;
    }

    public int getCount() {
        return count;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    @Override
    public String toString() {
        return "DataPoint{" +
                "facilityName='" + facilityName + '\'' +
                ", count=" + count +
                ", timestamp=" + timestamp +
                '}';
    }
}
