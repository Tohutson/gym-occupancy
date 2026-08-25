package com.treyhutson.gym_occupancy.service;

import com.treyhutson.gym_occupancy.config.OccupancyProperties;
import com.treyhutson.gym_occupancy.config.UpstreamServiceException;
import com.treyhutson.gym_occupancy.model.Facility;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class ExternalFacilityServiceTest {
    private static final String API_URL = "https://example.test/counts";

    @Test
    void returnsValidMeasurementsFromOneRequest() {
        RestTemplate restTemplate = new RestTemplate();
        MockRestServiceServer server = MockRestServiceServer.bindTo(restTemplate).build();
        server.expect(requestTo(API_URL)).andRespond(withSuccess("""
                [{"FacilityName":"Operations","LocationName":"RWC Floor 2","LastCount":42,
                  "TotalCapacity":200,"LastUpdatedDateAndTime":"2026-08-24T15:00:00-04:00","IsClosed":false}]
                """, MediaType.APPLICATION_JSON));

        ExternalFacilityService service = new ExternalFacilityService(restTemplate, properties());
        Facility facility = service.fetchOccupancyData().get(0);

        assertEquals("RWC Floor 2", facility.getLocationName());
        assertEquals(42, facility.getLastCount());
        server.verify();
    }

    @Test
    void rejectsMalformedResponsesInsteadOfRecordingAFalseZero() {
        RestTemplate restTemplate = new RestTemplate();
        MockRestServiceServer server = MockRestServiceServer.bindTo(restTemplate).build();
        server.expect(requestTo(API_URL)).andRespond(withSuccess("""
                [{"FacilityName":"Operations","LocationName":"RWC Floor 2","LastCount":-1,
                  "TotalCapacity":200,"LastUpdatedDateAndTime":"2026-08-24T15:00:00-04:00","IsClosed":false}]
                """, MediaType.APPLICATION_JSON));

        ExternalFacilityService service = new ExternalFacilityService(restTemplate, properties());

        assertThrows(UpstreamServiceException.class, service::fetchOccupancyData);
        server.verify();
    }

    private OccupancyProperties properties() {
        OccupancyProperties properties = new OccupancyProperties();
        properties.setApiUrl(API_URL);
        return properties;
    }

}
