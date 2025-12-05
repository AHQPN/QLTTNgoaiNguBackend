package org.example.qlttngoaingu.dto.request;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Request DTO để nhập/cập nhật điểm cho học viên
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GradeRequest {
    
    /**
     * ID chi tiết hóa đơn (enrollment) của học viên trong lớp
     */
    @NotNull(message = "enrollmentId là bắt buộc")
    private Integer enrollmentId;
    
    /**
     * Loại đánh giá: 1 = Chuyên cần, 2 = Giữa kỳ, 3 = Cuối kỳ
     */
    @NotNull(message = "gradeTypeId là bắt buộc")
    private Integer gradeTypeId;
    
    /**
     * Điểm số (0-10)
     */
    @NotNull(message = "Điểm số là bắt buộc")
    @DecimalMin(value = "0.0", message = "Điểm tối thiểu là 0")
    @DecimalMax(value = "10.0", message = "Điểm tối đa là 10")
    private BigDecimal score;
    
    /**
     * Nhận xét của giảng viên (không bắt buộc)
     */
    private String comment;
}
