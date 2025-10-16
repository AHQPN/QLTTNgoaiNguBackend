package org.example.qlttngoaingu.Dto.Response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter @AllArgsConstructor
public class LoginResponse {
    private String accessToken;
    private String refreshToken;
    private String identifier;
    private String role;
    private Integer userId;


}
