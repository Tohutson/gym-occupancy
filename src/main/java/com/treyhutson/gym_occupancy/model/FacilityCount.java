package com.treyhutson.gym_occupancy.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "facility_counts",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"location_name", "last_updated_date_and_time"})
        }
)
public class FacilityCount {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String facilityName;
    private String locationName;
    private int totalCapacity;
    private int lastCount;

    private boolean isClosed;

    private LocalDateTime lastUpdatedDateAndTime;

    // timestamp for when *we* stored the record (important for history)
    private LocalDateTime recordedAt;

    // Getters and setters
    public Long getId() { return id; }

    public void setId(Long id) { this.id = id; }

    public String getFacilityName() { return facilityName; }

    public void setFacilityName(String facilityName) { this.facilityName = facilityName; }

    public String getLocationName() { return locationName; }

    public void setLocationName(String locationName) { this.locationName = locationName; }

    public int getTotalCapacity() { return totalCapacity; }

    public void setTotalCapacity(int totalCapacity) { this.totalCapacity = totalCapacity; }

    public int getLastCount() { return lastCount; }

    public void setLastCount(int lastCount) { this.lastCount = lastCount; }

    public boolean isClosed() { return isClosed; }

    public void setClosed(boolean closed) { isClosed = closed; }

    public LocalDateTime getLastUpdatedDateAndTime() { return lastUpdatedDateAndTime; }

    public void setLastUpdatedDateAndTime(LocalDateTime lastUpdatedDateAndTime) { this.lastUpdatedDateAndTime = lastUpdatedDateAndTime; }

    public LocalDateTime getRecordedAt() { return recordedAt; }

    public void setRecordedAt(LocalDateTime recordedAt) { this.recordedAt = recordedAt; }
}
