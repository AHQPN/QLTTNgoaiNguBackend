package org.example.qlttngoaingu.dto.request;

import lombok.Data;
import java.time.LocalDate;
import java.time.LocalTime;

@Data
public class ScheduleCheckRequest {
    private Integer courseId;
    private LocalDate startDate;
    private LocalTime startTime;
    private Integer durationMinutes;
    private String schedulePattern; // "2-4-6", "3-5", etc.
    private Integer excludeClassId; // optional nếu cần check trùng với cả lớp hiện tại

    // Optional: nếu user đã chọn phòng/GV cụ thể
    private Integer preferredRoomId;
    private Integer preferredLecturerId;


    public ScheduleCheckRequest copy() {
        ScheduleCheckRequest copy = new ScheduleCheckRequest();
        copy.setCourseId(this.courseId);
        copy.setStartDate(this.startDate);
        copy.setStartTime(this.startTime);
        copy.setDurationMinutes(this.durationMinutes);
        copy.setSchedulePattern(this.schedulePattern);
        copy.setPreferredRoomId(this.preferredRoomId);
        copy.setPreferredLecturerId(this.preferredLecturerId);
        return copy;
    }
}
