package org.example.qlttngoaingu.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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

    private String orderInfo; // Optional

    @NotNull(message = "InvoiceId is required")
    private Integer invoiceId;
}
