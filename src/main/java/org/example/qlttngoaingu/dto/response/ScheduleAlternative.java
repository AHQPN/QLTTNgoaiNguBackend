package org.example.qlttngoaingu.dto.response;

import lombok.Data;

import java.util.List;

@Data
public class ScheduleAlternative {
    private String type; // "ALTERNATIVE_TIME", "ALTERNATIVE_START_DATE", etc.
    private String reason; // Giải thích ngắn gọn
    private int priority; // Điểm ưu tiên (cao hơn = tốt hơn)

    // Thông tin lịch mới
    private java.time.LocalDate startDate;
    private java.time.LocalTime startTime;
    private java.time.LocalTime endTime;
    private String schedulePattern;

    // Resources available với lịch này
    private List<ResourceInfo> availableRooms;
    private List<ResourceInfo> availableLecturers;
}
