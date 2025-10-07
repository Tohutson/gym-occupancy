package com.treyhutson.gym_occupancy.repository;

import com.treyhutson.gym_occupancy.model.FacilityCount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

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
}
