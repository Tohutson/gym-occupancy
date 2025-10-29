package com.treyhutson.gym_occupancy.repository;

import com.treyhutson.gym_occupancy.model.FacilityCount;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.sql.Time;
import java.time.LocalDateTime;
import java.time.LocalTime;
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

    // Get all records between two dates
    @Query("SELECT fc FROM FacilityCount fc WHERE fc.lastUpdatedDateAndTime >= :start AND fc.lastUpdatedDateAndTime <= :end ORDER BY fc.lastUpdatedDateAndTime")
    List<FacilityCount> findBetweenDates(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );

    // Get all records for a location between two dates
    @Query("SELECT fc FROM FacilityCount fc WHERE fc.locationName = :locationName AND fc.lastUpdatedDateAndTime >= :start AND fc.lastUpdatedDateAndTime <= :end ORDER BY fc.lastUpdatedDateAndTime")
    List<FacilityCount> findByLocationAndDateRange(
            @Param("locationName") String locationName,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );

    @Query(value = """
SELECT AVG(last_count)
FROM facility_counts
WHERE location_name = :locationName
  AND (last_updated_date_and_time::time BETWEEN (:start)::time AND (:end)::time)
""", nativeQuery = true)
    Double findAverageLastCountByLocationAndTime(
            @Param("locationName") String locationName,
            @Param("start") LocalTime start,
            @Param("end") LocalTime end
    );
}
