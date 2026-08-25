package com.treyhutson.gym_occupancy.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;
import org.hibernate.validator.constraints.time.DurationMin;

import java.time.Duration;
import java.time.ZoneId;

@ConfigurationProperties("occupancy")
@Validated
public class OccupancyProperties {
    private String apiUrl = "";
    private ZoneId sourceZone = ZoneId.of("America/New_York");
    private Duration connectTimeout = Duration.ofSeconds(5);
    private Duration readTimeout = Duration.ofSeconds(10);
    @DurationMin(minutes = 5)
    private Duration pollDelay = Duration.ofMinutes(5);
    private Duration retention = Duration.ofDays(730);
    private Duration baselineLookback = Duration.ofDays(56);
    private int baselineMinimumDays = 3;
    private Duration staleAfter = Duration.ofMinutes(30);

    public String getApiUrl() { return apiUrl; }
    public void setApiUrl(String apiUrl) { this.apiUrl = apiUrl; }
    public ZoneId getSourceZone() { return sourceZone; }
    public void setSourceZone(ZoneId sourceZone) { this.sourceZone = sourceZone; }
    public Duration getConnectTimeout() { return connectTimeout; }
    public void setConnectTimeout(Duration connectTimeout) { this.connectTimeout = connectTimeout; }
    public Duration getReadTimeout() { return readTimeout; }
    public void setReadTimeout(Duration readTimeout) { this.readTimeout = readTimeout; }
    public Duration getPollDelay() { return pollDelay; }
    public void setPollDelay(Duration pollDelay) { this.pollDelay = pollDelay; }
    public Duration getRetention() { return retention; }
    public void setRetention(Duration retention) { this.retention = retention; }
    public Duration getBaselineLookback() { return baselineLookback; }
    public void setBaselineLookback(Duration baselineLookback) { this.baselineLookback = baselineLookback; }
    public int getBaselineMinimumDays() { return baselineMinimumDays; }
    public void setBaselineMinimumDays(int baselineMinimumDays) { this.baselineMinimumDays = baselineMinimumDays; }
    public Duration getStaleAfter() { return staleAfter; }
    public void setStaleAfter(Duration staleAfter) { this.staleAfter = staleAfter; }
}
