package org.example.qlttngoaingu.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LecturerDashboardStatsResponse {
    private long totalClasses;
    private long totalStudents;
    private long upcomingSessions;
    private double attendanceRate;
}
