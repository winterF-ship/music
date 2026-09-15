package com.musicdemo1.controller;

import com.musicdemo1.common.ApiResponse;
import com.musicdemo1.dto.CountItem;
import com.musicdemo1.dto.DashboardSummary;
import com.musicdemo1.service.DashboardService;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/analytics")
public class AdminAnalyticsController {
    private final DashboardService dashboardService;

    public AdminAnalyticsController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping("/summary")
    public ApiResponse<DashboardSummary> summary() {
        return ApiResponse.ok(dashboardService.summary());
    }

    @GetMapping("/songs-by-singer")
    public ApiResponse<List<CountItem>> songsBySinger() {
        return ApiResponse.ok(dashboardService.songsBySinger());
    }
}
