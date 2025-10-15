package com.treyhutson.gym_occupancy.repository;

import com.treyhutson.gym_occupancy.model.FacilityCount;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface FacilityCountRepository extends JpaRepository<FacilityCount, Long> {
    @Query("""
        SELECT fc FROM FacilityCount fc
        WHERE fc.recordedAt IN (
            SELECT MAX(fc2.recordedAt)
            FROM FacilityCount fc2
            GROUP BY fc2.locationName
        )
        """)
    List<FacilityCount> findLatestPerFacility();

    @Transactional
    @Modifying
    @Query(value = """
        INSERT INTO facility_counts 
            (facility_name, location_name, total_capacity, last_count, is_closed, last_updated_date_and_time, recorded_at)
        VALUES 
            (:facilityName, :locationName, :totalCapacity, :lastCount, :isClosed, :lastUpdatedDateAndTime, :recordedAt)
        ON CONFLICT (location_name, last_updated_date_and_time) DO NOTHING
        """, nativeQuery = true)
    void insertIgnoreDuplicates(
            @Param("facilityName") String facilityName,
            @Param("locationName") String locationName,
            @Param("totalCapacity") int totalCapacity,
            @Param("lastCount") int lastCount,
            @Param("isClosed") boolean isClosed,
            @Param("lastUpdatedDateAndTime") LocalDateTime lastUpdatedDateAndTime,
            @Param("recordedAt") LocalDateTime recordedAt
    );
}
