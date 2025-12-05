package org.example.qlttngoaingu.dto.request;

import java.time.LocalDate;
import java.util.List;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PromotionRequest {

    @NotBlank(message = "Promotion name is required")
    private String name;

    private String description;

    @NotNull(message = "Discount percent is required")
    @Min(value = 1, message = "Discount percent must be at least 1%")
    @Max(value = 100, message = "Discount percent cannot exceed 100%")
    private Integer discountPercent;

    @NotNull(message = "Start date is required")
    private LocalDate startDate;

    @NotNull(message = "End date is required")
    private LocalDate endDate;

    @NotNull(message = "Promotion type ID is required")
    private Integer promotionTypeId;

    // Danh sách courseId cho Type 1 (khóa học đơn) và Type 2 (combo)
    // Bỏ trống cho Type 3 (học viên cũ)
    private List<Integer> courseIds;
}
