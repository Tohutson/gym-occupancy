package com.treyhutson.gym_occupancy.api;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/dashboard")
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
    public DashboardResponse dashboard(@RequestParam String facilityId,
                                       @RequestParam(defaultValue = "today") String range) {
        return dashboardService.dashboard(facilityId, DashboardRange.fromQuery(range));
    }
}
