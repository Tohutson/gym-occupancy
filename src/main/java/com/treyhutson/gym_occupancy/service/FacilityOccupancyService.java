package com.treyhutson.gym_occupancy.service;

import com.treyhutson.gym_occupancy.model.Facility;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.ResponseEntity;

import java.util.Arrays;
import java.util.List;

@Service
public class FacilityOccupancyService {

    private static final String API_URL =
            "https://goboardapi.azurewebsites.net/api/FacilityCount/GetCountsByAccount?AccountAPIKey=17c2cbcb-ec92-4178-a5f5-c4860330aea0";

    public List<Facility> fetchOccupancyData() {
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<Facility[]> response = restTemplate.getForEntity(API_URL, Facility[].class);
        Facility[] facilities = response.getBody();
        return Arrays.asList(facilities);
    }
}
