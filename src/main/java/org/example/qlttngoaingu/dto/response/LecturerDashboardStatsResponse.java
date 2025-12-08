package org.example.qlttngoaingu.dto.response;

import java.time.LocalDate;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LecturerDashboardStatsResponse {
    private Overview overview;
    private List<WeeklyScheduleItem> weeklySchedule;
    private List<ActiveClass> activeClasses;
    private List<Reminder> reminders;
    private List<AttendanceStats> attendanceStats;
    
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Overview {
        private int classesInCharge;    // Tổng số lớp phụ trách
        private int todayClasses;       // Số lớp dạy hôm nay
        private int totalStudents;      // Tổng số học viên
        private int hoursTaught;        // Số giờ đã dạy trong tháng
    }
    
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class WeeklyScheduleItem {
        private Integer id;              // Session ID
        private String className;
        private String room;
        private String time;             // Format: "HH:mm - HH:mm"
        private LocalDate date;
        private String status;           // Upcoming, Completed, Canceled
    }
    
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ActiveClass {
        private Integer id;              // Class ID
        private String className;
        private String course;
        private int students;
        private String progress;         // Format: "5/24"
        private int progressPercent;     // 0-100
    }
    
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Reminder {
        private Integer id;
        private String message;
        private String type;             // warning, info
    }
    
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class AttendanceStats {
        private String className;
        private int attendancePercent;   // 0-100
        private int absentPercent;       // 0-100
    }
}
