package org.example.qlttngoaingu.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import  jakarta.validation.constraints.NotBlank;
import  jakarta.validation.constraints.Pattern;



@Data
@NoArgsConstructor
@AllArgsConstructor
public class MomoCreatePaymentRequest {

    @NotBlank(message = "Amount is required")
    @Pattern(regexp = "^[1-9][0-9]*$", message = "Amount must be a positive number")
    private String amount;

    private String orderInfo; // Optional, mặc định sẽ là "Payment"
}