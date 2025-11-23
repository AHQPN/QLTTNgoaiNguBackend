package org.example.qlttngoaingu.dto.response;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class InvoiceDetailResponse {
    private Integer detailId;
    private String courseName;
    private String className;
    private BigDecimal finalAmount;
}
