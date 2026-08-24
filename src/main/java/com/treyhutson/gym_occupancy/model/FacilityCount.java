package com.treyhutson.gym_occupancy.model;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(
        name = "facility_counts",
        uniqueConstraints = {
                @UniqueConstraint(name = "uq_facility_measurement", columnNames = {"facility_id", "last_updated_date_and_time"})
        },
        indexes = {
                @Index(name = "idx_facility_recorded_at", columnList = "facility_id, recorded_at"),
                @Index(name = "idx_recorded_at", columnList = "recorded_at")
        }
)
public class FacilityCount {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String facilityId;

    private String facilityName;
    private String locationName;
    private int totalCapacity;
    private int lastCount;

    private boolean isClosed;

    @Column(nullable = false)
    private Instant lastUpdatedDateAndTime;

    // timestamp for when *we* stored the record (important for history)
    @Column(nullable = false)
    private Instant recordedAt;

    // Getters and setters
    public Long getId() { return id; }

    public void setId(Long id) { this.id = id; }

    public String getFacilityId() { return facilityId; }

    public void setFacilityId(String facilityId) { this.facilityId = facilityId; }

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

    public Instant getLastUpdatedDateAndTime() { return lastUpdatedDateAndTime; }

    public void setLastUpdatedDateAndTime(Instant lastUpdatedDateAndTime) { this.lastUpdatedDateAndTime = lastUpdatedDateAndTime; }

    public Instant getRecordedAt() { return recordedAt; }

    public void setRecordedAt(Instant recordedAt) { this.recordedAt = recordedAt; }
}
