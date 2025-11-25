package org.example.qlttngoaingu.dto.response;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MomoIPNResponse {
    private String partnerCode;
    private String orderId;
    private String requestId;
    private Integer resultCode;   // 0 = đã nhận và xử lý thành công
    private String message;
    private Long responseTime;
}