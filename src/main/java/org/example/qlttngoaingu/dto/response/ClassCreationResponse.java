package org.example.qlttngoaingu.dto.response;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Data
public class ClassCreationResponse {
    private Integer classId;
    private String className;
    private String courseName;
    private String schedulePattern; // "2-4-6", "3-5-7", etc.
    private LocalTime startTime;
    private LocalTime endTime;
    private LocalDate startDate;
    private LocalDate endDate;
    private String roomName;
    private String instructorName;
    private Integer totalSessions;
    private List<SessionInfo> sessions;

    @Data
    public static class SessionInfo {
        private Integer sessionId;
        private LocalDate date;
    }

}