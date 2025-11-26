package org.example.qlttngoaingu.dto.response;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;


@Data
public class InvoiceResponse {
    private Integer invoiceId;
    private LocalDateTime dateCreated;
    private Boolean status;

    private String studentName;
    private String studentId;
    private String paymentMethod;

    // Thông tin chi tiết giảm giá (3 loại)
    private BigDecimal totalOriginalPrice;          // Tổng giá gốc

    // Khuyến mãi khóa học lẻ (Type 1)
    private Integer courseDiscountPercent;          // % giảm khóa học lẻ
    private BigDecimal courseDiscountAmount;        // Tiền giảm khóa học lẻ

    // Khuyến mãi Combo (Type 2)
    private Integer comboDiscountPercent;           // % giảm combo
    private BigDecimal comboDiscountAmount;         // Tiền giảm combo

    // Khuyến mãi HV cũ (Type 3)
    private Integer returningDiscountPercent;       // % giảm HV cũ
    private BigDecimal returningDiscountAmount;     // Tiền giảm HV cũ

    // Tổng hợp
    private Integer totalDiscountPercent;           // Tổng % giảm
    private BigDecimal totalDiscountAmount;         // Tổng tiền giảm
    private BigDecimal totalAmount;                 // Tổng thanh toán

    private List<InvoiceDetailResponse> details;
}