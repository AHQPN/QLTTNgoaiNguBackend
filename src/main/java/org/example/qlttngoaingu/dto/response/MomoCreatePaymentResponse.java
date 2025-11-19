package org.example.qlttngoaingu.dto.response;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MomoCreatePaymentResponse {
    private String partnerCode;
    private String orderId;
    private String requestId;
    private long amount;
    private String payUrl; // URL thanh toán
    private String deeplink;
    private String qrCodeUrl;
    private int resultCode; // 0: Thành công
    private String message;
    private long responseTime;
}