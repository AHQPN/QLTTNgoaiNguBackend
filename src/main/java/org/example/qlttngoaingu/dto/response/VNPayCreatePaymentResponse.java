package org.example.qlttngoaingu.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VNPayCreatePaymentResponse {
    private String txnRef;
    private long amount;
    private String payUrl;
}
