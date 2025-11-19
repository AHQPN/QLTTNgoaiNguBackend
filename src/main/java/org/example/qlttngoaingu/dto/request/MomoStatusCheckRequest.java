package org.example.qlttngoaingu.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MomoStatusCheckRequest {
    private String partnerCode;
    private String accessKey;
    private String requestId;
    private String orderId;
    private String signature;
    private String lang;
}