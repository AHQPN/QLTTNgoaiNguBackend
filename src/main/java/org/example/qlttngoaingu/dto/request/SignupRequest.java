package org.example.qlttngoaingu.dto.request;


import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class SignupRequest {

    @Size(min = 6, max = 20, message = "INVALID_PASSWORD")
    @NotBlank(message = "FIELD_REQUIRED")
    private String password;

    @NotBlank(message = "FIELD REQUIRED")
    @Pattern(
            regexp = "^(0|\\+84)[0-9]{9}$",
            message = "INVALID_PHONE_NUMBER"
    )
    @NotBlank(message = "FILED_REQUIRED")
    private String phoneNumber;

    @Email(message = "INVALID_EMAIL")
    private String email;

}
