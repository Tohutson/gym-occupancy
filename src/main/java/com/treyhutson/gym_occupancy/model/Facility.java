package com.treyhutson.gym_occupancy.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Facility {

    @JsonProperty("FacilityName")
    private String facilityName;

    @JsonProperty("LocationName")
    private String locationName;

    @JsonProperty("LastCount")
    private int lastCount;

    @JsonProperty("TotalCapacity")
    private int totalCapacity;

    @JsonProperty("LastUpdatedDateAndTime")
    private String lastUpdated;

    @JsonProperty("IsClosed")
    private boolean isClosed;

    public Facility() {}

    // Getters and setters
    public String getFacilityName() { return facilityName; }
    public void setFacilityName(String facilityName) { this.facilityName = facilityName; }

    public String getLocationName() { return locationName; }
    public void setLocationName(String locationName) { this.locationName = locationName; }

    public int getLastCount() { return lastCount; }
    public void setLastCount(int lastCount) { this.lastCount = lastCount; }

    public int getTotalCapacity() { return totalCapacity; }
    public void setTotalCapacity(int totalCapacity) { this.totalCapacity = totalCapacity; }

    public String getLastUpdated() { return lastUpdated; }
    public void setLastUpdated(String lastUpdated) { this.lastUpdated = lastUpdated; }

    public boolean isClosed() { return isClosed; }
    public void setClosed(boolean closed) { isClosed = closed; }

    @Override
    public String toString() {
        return locationName + "(" + facilityName + ")" + ": " + lastCount + "/" + totalCapacity + (isClosed ? " (Closed)" : "");
    }
}
