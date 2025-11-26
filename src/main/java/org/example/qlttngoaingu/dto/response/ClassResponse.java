package org.example.qlttngoaingu.dto.response;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Data
public class ClassResponse {
    private int currentPage;
    private int totalPages;
    private long totalItems;
    private List<ClassInfo> classes; // danh sách lớp

    @Data
    public static class ClassInfo {
        private Integer classId;
        private String className;
        private String courseName;
        private String roomName;
        private String instructorName;
        private LocalDate startDate;
        private LocalDate endDate;
        private LocalTime startTime;
        private LocalTime endTime;
        private String schedulePattern;
        private String status;
        private Integer maxCapacity;
        private Integer currentEnrollment;
    }
}
