package com.treyhutson.gym_occupancy.repository;

import com.treyhutson.gym_occupancy.model.FacilityCount;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.time.LocalTime;
import java.util.List;

@Repository
public interface FacilityCountRepository extends JpaRepository<FacilityCount, Long> {
    @Query("""
        SELECT fc FROM FacilityCount fc
        WHERE fc.recordedAt = (
            SELECT MAX(fc2.recordedAt)
            FROM FacilityCount fc2
            WHERE fc2.facilityId = fc.facilityId
        )
        """)
    List<FacilityCount> findLatestPerFacility();

    @Transactional
    @Modifying
    @Query(value = """
        INSERT INTO facility_counts 
            (facility_id, facility_name, location_name, total_capacity, last_count, is_closed, last_updated_date_and_time, recorded_at)
        VALUES 
            (:facilityId, :facilityName, :locationName, :totalCapacity, :lastCount, :isClosed, :lastUpdatedDateAndTime, :recordedAt)
        ON CONFLICT (facility_id, last_updated_date_and_time) DO NOTHING
        """, nativeQuery = true)
    int insertIgnoreDuplicates(
            @Param("facilityId") String facilityId,
            @Param("facilityName") String facilityName,
            @Param("locationName") String locationName,
            @Param("totalCapacity") int totalCapacity,
            @Param("lastCount") int lastCount,
            @Param("isClosed") boolean isClosed,
            @Param("lastUpdatedDateAndTime") Instant lastUpdatedDateAndTime,
            @Param("recordedAt") Instant recordedAt
    );

    // Get all records between two dates
    @Query("SELECT fc FROM FacilityCount fc WHERE fc.lastUpdatedDateAndTime >= :start AND fc.lastUpdatedDateAndTime <= :end ORDER BY fc.lastUpdatedDateAndTime")
    List<FacilityCount> findBetweenDates(
            @Param("start") Instant start,
            @Param("end") Instant end
    );

    // Get all records for a location between two dates
    @Query("SELECT fc FROM FacilityCount fc WHERE fc.locationName = :locationName AND fc.lastUpdatedDateAndTime >= :start AND fc.lastUpdatedDateAndTime <= :end ORDER BY fc.lastUpdatedDateAndTime")
    List<FacilityCount> findByLocationAndDateRange(
            @Param("locationName") String locationName,
            @Param("start") Instant start,
            @Param("end") Instant end
    );

    @Query("""
            SELECT fc FROM FacilityCount fc
            WHERE fc.facilityId = :facilityId
              AND fc.lastUpdatedDateAndTime >= :start
              AND fc.lastUpdatedDateAndTime < :end
            ORDER BY fc.lastUpdatedDateAndTime
            """)
    List<FacilityCount> findHistoryForFacility(
            @Param("facilityId") String facilityId,
            @Param("start") Instant start,
            @Param("end") Instant end
    );

    @Query("SELECT fc FROM FacilityCount fc WHERE fc.facilityId = :facilityId ORDER BY fc.lastUpdatedDateAndTime DESC")
    List<FacilityCount> findLatestForFacility(@Param("facilityId") String facilityId, Pageable pageable);

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

    @Transactional
    @Modifying
    int deleteByRecordedAtBefore(Instant cutoff);
}
