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
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReviewRequest {
    
    @NotNull(message = "classId là bắt buộc")
    private Integer classId;
    
    @NotNull(message = "Điểm giảng viên là bắt buộc")
    @Min(value = 1, message = "Điểm tối thiểu là 1")
    @Max(value = 5, message = "Điểm tối đa là 5")
    private Integer teacherRating;
    
    @NotNull(message = "Điểm cơ sở vật chất là bắt buộc")
    @Min(value = 1, message = "Điểm tối thiểu là 1")
    @Max(value = 5, message = "Điểm tối đa là 5")
    private Integer facilityRating;
    
    @NotNull(message = "Điểm hài lòng chung là bắt buộc")
    @Min(value = 1, message = "Điểm tối thiểu là 1")
    @Max(value = 5, message = "Điểm tối đa là 5")
    private Integer overallRating;
    
    /**
     * Nhận xét văn bản (không bắt buộc)
     */
    private String comment;
}
