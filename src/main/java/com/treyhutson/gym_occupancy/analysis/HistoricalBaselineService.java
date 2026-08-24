package com.treyhutson.gym_occupancy.analysis;

import com.treyhutson.gym_occupancy.analysis.HistoricalAnalysis.BaselineBucket;
import com.treyhutson.gym_occupancy.analysis.HistoricalAnalysis.OccupancyComparison;
import com.treyhutson.gym_occupancy.config.OccupancyProperties;
import com.treyhutson.gym_occupancy.model.FacilityCount;
import com.treyhutson.gym_occupancy.repository.FacilityCountRepository;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class HistoricalBaselineService {
    private static final int BUCKET_MINUTES = 30;

    private final FacilityCountRepository repository;
    private final OccupancyProperties properties;

    public HistoricalBaselineService(FacilityCountRepository repository, OccupancyProperties properties) {
        this.repository = repository;
        this.properties = properties;
    }

    public HistoricalAnalysis analyze(FacilityCount current) {
        ZonedDateTime localCurrent = current.getLastUpdatedDateAndTime().atZone(properties.getSourceZone());
        Instant currentDayStart = localCurrent.toLocalDate().atStartOfDay(properties.getSourceZone()).toInstant();
        Instant historyStart = currentDayStart.minus(properties.getBaselineLookback());
        List<FacilityCount> history = repository
                .findHistoryForFacility(
                        current.getFacilityId(), historyStart, currentDayStart.minusNanos(1));

        List<BaselineBucket> baseline = buildBaseline(history, localCurrent.getDayOfWeek());
        LocalTime currentBucket = bucketStart(localCurrent.toLocalTime());
        BaselineBucket matchingBucket = baseline.stream()
                .filter(bucket -> bucket.start().equals(currentBucket))
                .findFirst()
                .orElse(null);

        return new HistoricalAnalysis(
                localCurrent.getDayOfWeek(),
                baseline,
                compare(current.getLastCount(), matchingBucket));
    }

    List<BaselineBucket> buildBaseline(List<FacilityCount> history, DayOfWeek weekday) {
        Map<LocalTime, BucketSamples> groups = new HashMap<>();
        for (FacilityCount measurement : history) {
            ZonedDateTime localTime = measurement.getLastUpdatedDateAndTime().atZone(properties.getSourceZone());
            if (localTime.getDayOfWeek() != weekday || measurement.isClosed()) {
                continue;
            }
            LocalTime bucket = bucketStart(localTime.toLocalTime());
            groups.computeIfAbsent(bucket, ignored -> new BucketSamples())
                    .add(measurement.getLastCount(), localTime.toLocalDate());
        }

        List<BaselineBucket> result = new ArrayList<>();
        groups.forEach((start, samples) -> {
            if (samples.days.size() >= properties.getBaselineMinimumDays()) {
                samples.counts.sort(Comparator.naturalOrder());
                result.add(new BaselineBucket(
                        start,
                        samples.counts.stream().mapToInt(Integer::intValue).average().orElse(0),
                        percentile(samples.counts, 0.25),
                        percentile(samples.counts, 0.75),
                        samples.counts.size(),
                        samples.days.size()));
            }
        });
        result.sort(Comparator.comparing(BaselineBucket::start));
        return List.copyOf(result);
    }

    OccupancyComparison compare(int currentCount, BaselineBucket baseline) {
        if (baseline == null || baseline.sampleDays() < properties.getBaselineMinimumDays()) {
            return OccupancyComparison.insufficient(baseline == null ? 0 : baseline.sampleDays());
        }

        double expected = baseline.average();
        double difference = currentCount - expected;
        double typicalSpread = Math.max(4.0, Math.max(expected * 0.15, baseline.upperQuartile() - baseline.lowerQuartile()));
        double magnitude = Math.abs(difference);
        ComparisonLevel level;
        if (magnitude <= typicalSpread) {
            level = ComparisonLevel.NORMAL;
        } else if (difference < 0) {
            level = magnitude >= typicalSpread * 2 ? ComparisonLevel.MUCH_QUIETER : ComparisonLevel.QUIETER;
        } else {
            level = magnitude >= typicalSpread * 2 ? ComparisonLevel.MUCH_BUSIER : ComparisonLevel.BUSIER;
        }

        Double differencePercent = expected < 1 ? null : difference / expected * 100;
        return new OccupancyComparison(level, level.getLabel(), expected, difference, differencePercent, baseline.sampleDays());
    }

    private LocalTime bucketStart(LocalTime time) {
        return time.withMinute(time.getMinute() < BUCKET_MINUTES ? 0 : BUCKET_MINUTES).withSecond(0).withNano(0);
    }

    private double percentile(List<Integer> sortedValues, double percentile) {
        if (sortedValues.size() == 1) {
            return sortedValues.get(0);
        }
        double index = percentile * (sortedValues.size() - 1);
        int lower = (int) Math.floor(index);
        int upper = (int) Math.ceil(index);
        if (lower == upper) {
            return sortedValues.get(lower);
        }
        double fraction = index - lower;
        return sortedValues.get(lower) + (sortedValues.get(upper) - sortedValues.get(lower)) * fraction;
    }

    private static class BucketSamples {
        private final List<Integer> counts = new ArrayList<>();
        private final Set<LocalDate> days = new HashSet<>();

        private void add(int count, LocalDate date) {
            counts.add(count);
            days.add(date);
        }
    }
}
