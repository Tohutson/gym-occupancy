package com.treyhutson.gym_occupancy.api;

import com.treyhutson.gym_occupancy.analysis.HistoricalAnalysis;
import com.treyhutson.gym_occupancy.analysis.HistoricalAnalysis.BaselineBucket;
import com.treyhutson.gym_occupancy.analysis.HistoricalBaselineService;
import com.treyhutson.gym_occupancy.config.OccupancyProperties;
import com.treyhutson.gym_occupancy.model.FacilityCount;
import com.treyhutson.gym_occupancy.repository.FacilityCountRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
public class DashboardService {
    static final int MAX_DASHBOARD_MEASUREMENTS = 2_500;

    private final FacilityCountRepository repository;
    private final HistoricalBaselineService baselineService;
    private final OccupancyProperties properties;
    private final Clock clock;

    public DashboardService(FacilityCountRepository repository, HistoricalBaselineService baselineService,
                            OccupancyProperties properties, Clock clock) {
        this.repository = repository;
        this.baselineService = baselineService;
        this.properties = properties;
        this.clock = clock;
    }

    public List<FacilityOption> facilities() {
        return repository.findLatestPerFacility().stream()
                .sorted(Comparator.comparing(FacilityCount::getLocationName))
                .map(value -> new FacilityOption(value.getFacilityId(), value.getFacilityName(), value.getLocationName(),
                        value.getTotalCapacity(), value.getLastUpdatedDateAndTime()))
                .toList();
    }

    public DashboardResponse dashboard(String facilityId, DashboardRange range) {
        if (facilityId == null || facilityId.isBlank()) {
            throw new IllegalArgumentException("facilityId is required");
        }
        if (facilityId.length() > 255) {
            throw new IllegalArgumentException("facilityId must be at most 255 characters");
        }
        if (facilityId.codePoints().anyMatch(Character::isISOControl)) {
            throw new IllegalArgumentException("facilityId contains invalid characters");
        }
        if (range == null) {
            throw new IllegalArgumentException("range is required");
        }
        FacilityCount current = repository.findLatestForFacility(facilityId, PageRequest.of(0, 1)).stream()
                .findFirst()
                .orElseThrow(() -> new FacilityNotFoundException(facilityId));

        Instant now = clock.instant();
        ZonedDateTime localNow = now.atZone(properties.getSourceZone());
        Instant todayStart = localNow.toLocalDate().atStartOfDay(properties.getSourceZone()).toInstant();
        Instant rangeStart = range.getDuration() == null ? todayStart : now.minus(range.getDuration());
        List<FacilityCount> measurements = boundedHistory(facilityId, rangeStart, now.plusSeconds(1));
        List<FacilityCount> todayMeasurements = range == DashboardRange.TODAY
                ? measurements
                : boundedHistory(facilityId, todayStart, now.plusSeconds(1));
        HistoricalAnalysis analysis = baselineService.analyze(current);

        long ageSeconds = Math.max(0, Duration.between(current.getRecordedAt(), now).getSeconds());
        double percent = current.getTotalCapacity() <= 0 ? 0 : current.getLastCount() * 100.0 / current.getTotalCapacity();

        return new DashboardResponse(
                now,
                properties.getSourceZone().getId(),
                range.name(),
                new DashboardResponse.Facility(current.getFacilityId(), current.getFacilityName(), current.getLocationName(), current.getTotalCapacity()),
                new DashboardResponse.CurrentOccupancy(current.getLastCount(), percent, current.isClosed(), current.getLastUpdatedDateAndTime()),
                analysis.comparison(),
                new DashboardResponse.Freshness(current.getRecordedAt(), ageSeconds, ageSeconds > properties.getStaleAfter().toSeconds()),
                summarize(todayMeasurements),
                measurements.stream().map(value -> new DashboardResponse.MeasurementPoint(value.getLastUpdatedDateAndTime(), value.getLastCount())).toList(),
                baselinePoints(localNow.toLocalDate(), analysis.baseline()),
                recommendedWindows(localNow, analysis.baseline()));
    }

    private List<FacilityCount> boundedHistory(String facilityId, Instant start, Instant end) {
        List<FacilityCount> newestFirst = repository.findRecentHistoryForFacility(
                facilityId, start, end, PageRequest.of(0, MAX_DASHBOARD_MEASUREMENTS));
        return newestFirst.stream()
                .sorted(Comparator.comparing(FacilityCount::getLastUpdatedDateAndTime))
                .toList();
    }

    private DashboardResponse.TodaySummary summarize(List<FacilityCount> measurements) {
        if (measurements.isEmpty()) {
            return new DashboardResponse.TodaySummary(null, null, null, null);
        }
        FacilityCount peak = measurements.stream().max(Comparator.comparingInt(FacilityCount::getLastCount)).orElseThrow();
        int minimum = measurements.stream().mapToInt(FacilityCount::getLastCount).min().orElse(0);
        double average = measurements.stream().mapToInt(FacilityCount::getLastCount).average().orElse(0);
        return new DashboardResponse.TodaySummary(minimum, peak.getLastCount(), average, peak.getLastUpdatedDateAndTime());
    }

    private List<DashboardResponse.BaselinePoint> baselinePoints(LocalDate date, List<BaselineBucket> baseline) {
        return baseline.stream().map(bucket -> new DashboardResponse.BaselinePoint(
                date.atTime(bucket.start()).atZone(properties.getSourceZone()).toInstant(),
                bucket.average(), bucket.lowerQuartile(), bucket.upperQuartile(), bucket.sampleDays())).toList();
    }

    private List<DashboardResponse.VisitWindow> recommendedWindows(ZonedDateTime now, List<BaselineBucket> baseline) {
        List<BaselineBucket> future = baseline.stream()
                .filter(bucket -> bucket.start().isAfter(now.toLocalTime()))
                .sorted(Comparator.comparingDouble(BaselineBucket::average))
                .limit(3)
                .sorted(Comparator.comparing(BaselineBucket::start))
                .toList();
        List<DashboardResponse.VisitWindow> result = new ArrayList<>();
        for (BaselineBucket bucket : future) {
            Instant start = now.toLocalDate().atTime(bucket.start()).atZone(properties.getSourceZone()).toInstant();
            result.add(new DashboardResponse.VisitWindow(start, start.plus(Duration.ofMinutes(30)), bucket.average()));
        }
        return List.copyOf(result);
    }
}
