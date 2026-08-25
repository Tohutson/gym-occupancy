package com.treyhutson.gym_occupancy.api;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(DashboardController.class)
class DashboardControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private DashboardService dashboardService;

    @Test
    void rejectsUnsupportedRanges() throws Exception {
        mockMvc.perform(get("/api/dashboard")
                        .param("facilityId", "RWC Floor 2")
                        .param("range", "30d"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void rejectsInvalidFacilityIdentifiers() throws Exception {
        mockMvc.perform(get("/api/dashboard")
                        .param("facilityId", "x".repeat(256))
                        .param("range", "today"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void acceptsAValidBoundedRangeQuery() throws Exception {
        when(dashboardService.dashboard(eq("RWC Floor 2"), eq(DashboardRange.DAYS_7)))
                .thenReturn(response());

        mockMvc.perform(get("/api/dashboard")
                        .param("facilityId", "RWC Floor 2")
                        .param("range", "7d"))
                .andExpect(status().isOk());
    }

    private DashboardResponse response() {
        return new DashboardResponse(
                Instant.EPOCH, "UTC", "DAYS_7", null, null, null, null, null,
                List.of(), List.of(), List.of());
    }
}
