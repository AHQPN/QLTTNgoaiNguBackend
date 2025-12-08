package org.example.qlttngoaingu.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.Data;

@Data
public class InvoiceListResponse {
    private Integer invoiceId;
    private LocalDateTime dateCreated;
    private Boolean status;
    private String studentName;
    private String studentId;
    private String paymentMethod;
    private BigDecimal totalAmount;
    private Integer totalDiscountPercent;
}
