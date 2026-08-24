package com.treyhutson.gym_occupancy.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;
import java.time.ZoneId;

@ConfigurationProperties("occupancy")
public class OccupancyProperties {
    private String apiUrl = "";
    private ZoneId sourceZone = ZoneId.of("America/New_York");
    private Duration connectTimeout = Duration.ofSeconds(5);
    private Duration readTimeout = Duration.ofSeconds(10);
    private int retryAttempts = 3;
    private Duration retryDelay = Duration.ofSeconds(1);
    private Duration retryMaxDelay = Duration.ofSeconds(10);
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
    public int getRetryAttempts() { return retryAttempts; }
    public void setRetryAttempts(int retryAttempts) { this.retryAttempts = retryAttempts; }
    public Duration getRetryDelay() { return retryDelay; }
    public void setRetryDelay(Duration retryDelay) { this.retryDelay = retryDelay; }
    public Duration getRetryMaxDelay() { return retryMaxDelay; }
    public void setRetryMaxDelay(Duration retryMaxDelay) { this.retryMaxDelay = retryMaxDelay; }
    public Duration getRetention() { return retention; }
    public void setRetention(Duration retention) { this.retention = retention; }
    public Duration getBaselineLookback() { return baselineLookback; }
    public void setBaselineLookback(Duration baselineLookback) { this.baselineLookback = baselineLookback; }
    public int getBaselineMinimumDays() { return baselineMinimumDays; }
    public void setBaselineMinimumDays(int baselineMinimumDays) { this.baselineMinimumDays = baselineMinimumDays; }
    public Duration getStaleAfter() { return staleAfter; }
    public void setStaleAfter(Duration staleAfter) { this.staleAfter = staleAfter; }
}
