package com.treyhutson.gym_occupancy.config;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.web.client.RestTemplate;

import java.time.Clock;

@Configuration
public class HttpClientConfig {

    @Bean
    RestTemplate occupancyRestTemplate(RestTemplateBuilder builder, OccupancyProperties properties) {
        return builder
                .connectTimeout(properties.getConnectTimeout())
                .readTimeout(properties.getReadTimeout())
                .build();
    }

    @Bean
    RetryTemplate occupancyRetryTemplate(OccupancyProperties properties) {
        return RetryTemplate.builder()
                .maxAttempts(properties.getRetryAttempts())
                .exponentialBackoff(
                        properties.getRetryDelay(),
                        2.0,
                        properties.getRetryMaxDelay())
                .retryOn(UpstreamServiceException.class)
                .build();
    }

    @Bean
    Clock clock() {
        return Clock.systemUTC();
    }
}
