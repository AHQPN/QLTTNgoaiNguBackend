package org.example.qlttngoaingu.dto.response;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class WeeklyScheduleResponse {
    private LocalDate weekStart;
    private LocalDate weekEnd;
    private List<DaySchedule> days;

    @Data
    public static class DaySchedule {
        private LocalDate date; // ngày thực tế
        private String dayName; // Thứ 2, Thứ 3...
        private List<PeriodSchedule> periods; // Sáng, Chiều, Tối
    }

    @Data
    public static class PeriodSchedule {
        private String period; // Sáng / Chiều / Tối
        private List<SessionInfo> sessions; // buổi học trong ca
    }

    @Data
    public static class SessionInfo {
        private Integer sessionId;
        private String className;
        private String courseName;
        private String roomName;
        private String instructorName;
        private String status;
        private String note;
        private String schedulePattern;
        private LocalDate sessionDate;
    }
}
