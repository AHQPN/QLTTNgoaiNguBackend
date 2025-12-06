package org.example.qlttngoaingu.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class ResetPasswordRequest {
    
    @NotBlank(message = "FIELD_REQUIRED")
    private String code;
    
    @NotBlank(message = "FIELD_REQUIRED")
    @Size(min = 6, max = 20, message = "INVALID_PASSWORD")
    private String newPassword;
    
    @NotBlank(message = "FIELD_REQUIRED")
    @Size(min = 6, max = 20, message = "INVALID_PASSWORD")
    private String confirmPassword;
}
