package org.example.qlttngoaingu.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO để học viên gửi đánh giá khóa học
 * Các trường chi tiết (teacherRating, materialRating, facilityRating) là optional
 * để tương thích với hệ thống cũ chỉ có overallRating
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReviewRequest {
    
    /**
     * ID lớp học cần đánh giá
     */
    @NotNull(message = "classId là bắt buộc")
    private Integer classId;
    
    /**
     * Điểm đánh giá giảng viên (1-5 sao) - Optional
     */
    @Min(value = 1, message = "Điểm tối thiểu là 1")
    @Max(value = 5, message = "Điểm tối đa là 5")
    private Integer teacherRating;
    
    /**
     * Điểm đánh giá cơ sở vật chất (1-5 sao) - Optional
     */
    @Min(value = 1, message = "Điểm tối thiểu là 1")
    @Max(value = 5, message = "Điểm tối đa là 5")
    private Integer facilityRating;
    
    /**
     * Điểm hài lòng tổng thể (1-5 sao) - Bắt buộc
     */
    @NotNull(message = "overallRating là bắt buộc")
    @Min(value = 1, message = "Điểm tối thiểu là 1")
    @Max(value = 5, message = "Điểm tối đa là 5")
    private Integer overallRating;
    
    /**
     * Nhận xét văn bản (không bắt buộc)
     */
    private String comment;
}
