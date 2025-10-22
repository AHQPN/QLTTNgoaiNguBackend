package org.example.qlttngoaingu.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class TokenRefreshRequest {
    @NotBlank
    private String refreshToken;
}
