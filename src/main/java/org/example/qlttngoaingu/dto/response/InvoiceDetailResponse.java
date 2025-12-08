package org.example.qlttngoaingu.dto.response;

import java.math.BigDecimal;
import java.util.List;

import lombok.Data;

@Data
public class InvoiceDetailResponse {
    private Integer detailId;
    private String courseName;
    private String className;
    
    // Thông tin giá
    private BigDecimal originalPrice;        // Giá gốc của lớp học
    private BigDecimal finalAmount;          // Giá sau tất cả khuyến mãi (= giá phải trả)
    
    // Danh sách khuyến mãi được áp dụng (có thể nhiều loại)
    private List<PromotionAppliedResponse> promotionsApplied;
    
    @Data
    public static class PromotionAppliedResponse {
        private Integer promotionId;
        private String promotionName;
        private String promotionType;        // "Khóa học đơn", "Combo", "Học viên cũ"
        private Integer discountPercent;
        private BigDecimal discountAmount;
    }
}
