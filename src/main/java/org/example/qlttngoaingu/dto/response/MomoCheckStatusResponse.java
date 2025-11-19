package org.example.qlttngoaingu.dto.response;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MomoCheckStatusResponse {
    private String partnerCode;
    private String orderId;
    private String requestId;
    private long amount;
    private long transId; // Mã giao dịch MoMo
    private int resultCode; // 0: Thành công
    private String message;
    private long responseTime;
    private String signature;
    // ... và các trường khác
}