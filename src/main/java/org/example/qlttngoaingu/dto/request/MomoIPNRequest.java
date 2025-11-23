package org.example.qlttngoaingu.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO nhận từ MoMo IPN (webhook)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MomoIPNRequest {
    private String partnerCode;
    private String orderId;
    private String requestId;
    private Long amount;
    private String orderInfo;
    private String orderType;
    private Long transId;          // Mã giao dịch từ MoMo
    private Integer resultCode;    // 0 = thành công
    private String message;
    private String payType;
    private Long responseTime;
    private String extraData;
    private String signature;      // Chữ ký để verify
}