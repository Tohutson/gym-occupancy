package com.treyhutson.gym_occupancy.service;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

@Component
public class CleanupScheduler {

    @PersistenceContext
    private EntityManager entityManager;

    // Runs every day at 3 AM
    @Scheduled(cron = "0 0 3 * * *")
    @Transactional
    public void cleanOldRecords() {
        int deleted = entityManager.createQuery(
                "DELETE FROM FacilityCount f WHERE f.recordedAt < CURRENT_TIMESTAMP - 90"
        ).executeUpdate();

        System.out.println("🧹 Deleted " + deleted + " old FacilityCount records.");
    }
}
