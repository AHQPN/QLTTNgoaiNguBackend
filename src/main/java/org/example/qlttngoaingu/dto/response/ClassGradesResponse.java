package org.example.qlttngoaingu.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Response DTO cho danh sách điểm của lớp học (dành cho giảng viên)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClassGradesResponse {
    
    private Integer classId;
    private String className;
    private Integer courseId;
    private String courseName;
    private Integer lecturerId;
    private String lecturerName;
    
    // Danh sách điểm học viên
    private List<StudentGradeInfo> students;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StudentGradeInfo {
        private Integer studentId;
        private String studentName;
        private String email;
        private String avatar;
        private Integer enrollmentId;  // macthd - cần để nhập điểm
        
        // Điểm số
        private BigDecimal attendanceScore;
        private BigDecimal midtermScore;
        private BigDecimal finalScore;
        private BigDecimal totalScore;
        private String grade;
        private String status;
        
        // Chi tiết điểm với ID
        private GradeDetail attendanceGrade;
        private GradeDetail midtermGrade;
        private GradeDetail finalGrade;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GradeDetail {
        private Integer gradeId;
        private BigDecimal score;
        private String comment;
        private LocalDateTime gradedAt;
        private String gradedByName;
    }
}
