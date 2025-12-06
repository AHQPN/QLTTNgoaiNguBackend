package org.example.qlttngoaingu.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Response DTO cho thông tin điểm số của học viên
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GradeResponse {
    
    private Integer gradeId;
    private Integer classId;
    private String className;
    private Integer courseId;
    private String courseName;
    private String courseImage;
    
    // Điểm theo từng loại
    private BigDecimal attendanceScore;  // Điểm chuyên cần (loaidanhgia = 1)
    private BigDecimal midtermScore;     // Điểm giữa kỳ (loaidanhgia = 2)
    private BigDecimal finalScore;       // Điểm cuối kỳ (loaidanhgia = 3)
    
    // Điểm tổng kết và xếp loại
    private BigDecimal totalScore;       // = 0.1*CC + 0.3*GK + 0.6*CK
    private String grade;                // Xuất sắc/Giỏi/Khá/TB/Yếu
    private String status;               // Hoàn thành / Chưa hoàn thành
    
    // Thông tin bổ sung
    private String comment;
    private LocalDateTime lastGradedAt;
    private String gradedByName;
    
    /**
     * Tính điểm tổng kết từ các điểm thành phần
     * Công thức: 10% Chuyên cần + 30% Giữa kỳ + 60% Cuối kỳ
     */
    public static BigDecimal calculateTotalScore(BigDecimal attendance, BigDecimal midterm, BigDecimal finalScore) {
        BigDecimal cc = attendance != null ? attendance : BigDecimal.ZERO;
        BigDecimal gk = midterm != null ? midterm : BigDecimal.ZERO;
        BigDecimal ck = finalScore != null ? finalScore : BigDecimal.ZERO;
        
        return cc.multiply(new BigDecimal("0.1"))
                .add(gk.multiply(new BigDecimal("0.3")))
                .add(ck.multiply(new BigDecimal("0.6")));
    }
    
    /**
     * Xếp loại dựa trên điểm tổng kết
     */
    public static String calculateGrade(BigDecimal totalScore) {
        if (totalScore == null) return "Chưa có";
        double score = totalScore.doubleValue();
        
        if (score >= 8.5) return "Xuất sắc";
        if (score >= 7.0) return "Giỏi";
        if (score >= 5.5) return "Khá";
        if (score >= 4.0) return "Trung bình";
        return "Yếu";
    }
    
    /**
     * Xác định trạng thái hoàn thành
     */
    public static String calculateStatus(BigDecimal attendance, BigDecimal midterm, BigDecimal finalScore) {
        boolean hasAll = attendance != null && midterm != null && finalScore != null;
        return hasAll ? "Hoàn thành" : "Chưa hoàn thành";
    }
}
