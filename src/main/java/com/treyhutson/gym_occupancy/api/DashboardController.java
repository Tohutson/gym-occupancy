package com.treyhutson.gym_occupancy.api;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.validation.annotation.Validated;

import java.util.List;

@RestController
@RequestMapping("/api/dashboard")
@Validated
public class DashboardController {
    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping("/facilities")
    public List<FacilityOption> facilities() {
        return dashboardService.facilities();
    }

    @GetMapping
    public DashboardResponse dashboard(
                                       @RequestParam
                                       @NotBlank(message = "facilityId is required")
                                       @Size(max = 255, message = "facilityId must be at most 255 characters")
                                       @Pattern(regexp = "[^\\p{Cntrl}]+", message = "facilityId contains invalid characters")
                                       String facilityId,
                                       @RequestParam(defaultValue = "today") String range) {
        return dashboardService.dashboard(facilityId, DashboardRange.fromQuery(range));
    }
}
