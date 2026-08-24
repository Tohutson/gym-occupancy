package com.treyhutson.gym_occupancy.service;

import com.treyhutson.gym_occupancy.config.OccupancyProperties;
import com.treyhutson.gym_occupancy.config.UpstreamServiceException;
import com.treyhutson.gym_occupancy.model.Facility;
import org.springframework.http.ResponseEntity;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.List;

@Service
public class ExternalFacilityService {
    private final RestTemplate restTemplate;
    private final RetryTemplate retryTemplate;
    private final OccupancyProperties properties;

    public ExternalFacilityService(RestTemplate restTemplate, RetryTemplate retryTemplate, OccupancyProperties properties) {
        this.restTemplate = restTemplate;
        this.retryTemplate = retryTemplate;
        this.properties = properties;
    }

    public List<Facility> fetchOccupancyData() {
        if (!StringUtils.hasText(properties.getApiUrl())) {
            throw new UpstreamServiceException("OCCUPANCY_API_URL is not configured");
        }
        return retryTemplate.execute(context -> fetchOnce());
    }

    private List<Facility> fetchOnce() {
        try {
            ResponseEntity<Facility[]> response = restTemplate.getForEntity(properties.getApiUrl(), Facility[].class);
            Facility[] body = response.getBody();
            if (body == null || body.length == 0) {
                throw new UpstreamServiceException("The occupancy service returned no facilities");
            }
            List<Facility> facilities = Arrays.asList(body);
            facilities.forEach(this::validate);
            return facilities;
        } catch (RestClientException exception) {
            throw new UpstreamServiceException("The occupancy service request failed", exception);
        }
    }

    private void validate(Facility facility) {
        if (facility == null
                || !StringUtils.hasText(facility.getLocationName())
                || !StringUtils.hasText(facility.getLastUpdated())
                || facility.getLastCount() < 0
                || facility.getTotalCapacity() < 0
                || (facility.getTotalCapacity() > 0 && facility.getLastCount() > facility.getTotalCapacity())) {
            throw new UpstreamServiceException("The occupancy service returned a malformed facility");
        }
    }
}
