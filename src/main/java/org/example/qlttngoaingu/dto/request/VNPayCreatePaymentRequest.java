package org.example.qlttngoaingu.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VNPayCreatePaymentRequest {

    @NotBlank(message = "Amount is required")
    @Pattern(regexp = "^[1-9][0-9]*$", message = "Amount must be a positive number")
    private String amount;

    private String orderInfo; // Optional, default "Payment"
    private Integer invoiceId; // Optional: if provided we'll use invoice total as amount and store invoiceId in vnp_OrderInfo
}
