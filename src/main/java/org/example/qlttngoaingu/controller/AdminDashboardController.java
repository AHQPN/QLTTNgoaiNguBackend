package org.example.qlttngoaingu.controller;

import java.util.List;

import org.example.qlttngoaingu.dto.response.ActivityResponse;
import org.example.qlttngoaingu.dto.response.ApiResponse;
import org.example.qlttngoaingu.dto.response.CourseProgressResponse;
import org.example.qlttngoaingu.dto.response.DashboardStatsResponse;
import org.example.qlttngoaingu.dto.response.EndingClassResponse;
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

    /**
     * GET /admin/dashboard/course-progress
     * Lấy tiến độ khóa học (Course Progress)
     * Theo dõi tiến độ hoàn thành các buổi học của từng lớp đang hoạt động
     */
    @GetMapping("/course-progress")
    public ResponseEntity<ApiResponse> getCourseProgress() {
        List<CourseProgressResponse> courseProgress = adminDashboardService.getCourseProgress();
        return ResponseEntity.ok(ApiResponse.builder()
                .data(courseProgress)
                .build());
    }

    /**
     * GET /admin/dashboard/ending-classes
     * Lấy danh sách lớp học sắp kết thúc
     * Cảnh báo các lớp học sắp kết thúc để admin chuẩn bị các công việc kế tiếp
     */
    @GetMapping("/ending-classes")
    public ResponseEntity<ApiResponse> getEndingClasses() {
        List<EndingClassResponse> endingClasses = adminDashboardService.getEndingClasses();
        return ResponseEntity.ok(ApiResponse.builder()
                .data(endingClasses)
                .build());
    }
}