package org.example.qlttngoaingu.dto.response;

import java.math.BigDecimal;
import java.util.List;

import lombok.Data;

@Data
public class CartPreviewResponse {
    private List<CartItemResponse> items;
    private CartSummaryResponse summary;
    
    @Data
    public static class CartItemResponse {
        private Integer courseClassId;
        private String courseName;
        private String className;
        private BigDecimal originalPrice; // Giá sau khi áp Type 1
        private BigDecimal finalPrice;    // Giá cuối cùng của item này
    }
    
    @Data
    public static class CartSummaryResponse {
        private BigDecimal totalOriginalPrice; // Tổng giá sau Type 1
        private List<ComboDiscountInfo> appliedCombos;
        private BigDecimal returningDiscountAmount;
        private BigDecimal totalDiscountAmount; // Chỉ combo + returning
        private BigDecimal finalAmount;
    }
    
    @Data
    public static class ComboDiscountInfo {
        private String comboName;
        private Integer discountPercent;
        private BigDecimal discountAmount;
        private List<String> courseNames;
    }
}
