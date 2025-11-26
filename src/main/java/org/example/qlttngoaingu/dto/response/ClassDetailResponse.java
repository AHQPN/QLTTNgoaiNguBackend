package org.example.qlttngoaingu.dto.response;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
@Data
public class ClassDetailResponse {

    private Integer classId;
    private String className;
    private String courseName;
    private String schedulePattern; // "2-4-6", "3-5-7", etc.
    private LocalTime startTime;
    private LocalTime endTime;
    private Integer minutePerSession;
    private LocalDate startDate;
    private LocalDate endDate;
    private String roomName;
    private String instructorName;
    private Integer totalSessions;
    private Integer currentEnrollment;
    private Integer maxCapacity ;
    private List<StudentInClass> students;
    private List<SessionInfoDetail> sessions;

    @Data
    public static class SessionInfoDetail {
        private Integer sessionId;
        private LocalDate date;
        private String note;
        private String status;

    }
    @Data
    public static class StudentInClass {
        private Integer studentId;
        private String fullName;
        private String avatar;
        private Boolean gender;
        private String email;
        private String phone;
    }
}
