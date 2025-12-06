package org.example.qlttngoaingu.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Response DTO cho thông tin đánh giá khóa học
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReviewResponse {
    
    private Integer reviewId;
    private Integer classId;
    private String className;
    private Integer courseId;
    private String courseName;
    private String courseImage;
    
    // Điểm đánh giá chi tiết (1-5 sao)
    private Integer teacherRating;
    private Integer facilityRating;
    private Integer overallRating;
    
    // Điểm trung bình
    private Double averageRating;
    
    // Nhận xét
    private String comment;
    
    // Thông tin học viên (cho view admin/teacher)
    private Integer studentId;
    private String studentName;
    private String studentAvatar;
    
    // Thời gian
    private LocalDateTime createdAt;
    
    /**
     * Tính điểm trung bình từ các tiêu chí
     */
    public static Double calculateAverageRating(Integer teacher, Integer facility, Integer overall) {
        int count = 0;
        int sum = 0;
        
        if (teacher != null) { sum += teacher; count++; }
        if (facility != null) { sum += facility; count++; }
        if (overall != null) { sum += overall; count++; }
        
        return count > 0 ? (double) sum / count : null;
    }
}
