package org.example.qlttngoaingu.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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
    
    private Integer teacherRating;       // Điểm giảng viên
    private Integer facilityRating;      // Điểm cơ sở vật chất
    private Integer overallRating;       // Điểm hài lòng chung
    private String comment;
    
    // Thông tin học viên (cho view admin/teacher)
    private Integer studentId;
    private String studentName;
    private String studentAvatar;
}
