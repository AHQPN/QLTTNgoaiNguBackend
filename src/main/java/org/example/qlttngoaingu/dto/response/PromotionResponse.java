package org.example.qlttngoaingu.dto.response;

import java.time.LocalDate;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PromotionResponse {

    private Integer id;
    private String name;
    private String description;
    private Integer discountPercent;
    private LocalDate startDate;
    private LocalDate endDate;
    private Boolean active;
    private Integer promotionTypeId;
    private String promotionTypeName;
    
    // Danh sách khóa học áp dụng (nếu có)
    private List<CourseSimpleResponse> courses;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CourseSimpleResponse {
        private Integer courseId;
        private String courseName;
    }
}
