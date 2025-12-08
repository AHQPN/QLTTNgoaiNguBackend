package org.example.qlttngoaingu.controller;

import java.util.List;

import org.example.qlttngoaingu.dto.response.ActivityResponse;
import org.example.qlttngoaingu.dto.response.ApiResponse;
import org.example.qlttngoaingu.dto.response.DashboardStatsResponse;
import org.example.qlttngoaingu.service.AdminDashboardService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

/**
 * Controller để xử lý các API Dashboard cho Admin
 */
@RestController
@RequestMapping("/admin/dashboard")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminDashboardController {

    private final AdminDashboardService adminDashboardService;

    /**
     * GET /admin/dashboard/stats
     * Lấy thống kê tổng quan Dashboard
     */
    @GetMapping("/stats")
    public ResponseEntity<ApiResponse> getDashboardStats() {
        DashboardStatsResponse stats = adminDashboardService.getDashboardStats();
        return ResponseEntity.ok(ApiResponse.builder()
                .data(stats)
                .build());
    }

    /**
     * GET /admin/dashboard/activities
     * Lấy các hoạt động gần đây
     */
    @GetMapping("/activities")
    public ResponseEntity<ApiResponse> getRecentActivities(
            @RequestParam(defaultValue = "10") int limit) {
        List<ActivityResponse> activities = adminDashboardService.getRecentActivities(limit);
        return ResponseEntity.ok(ApiResponse.builder()
                .data(activities)
                .build());
    }
}