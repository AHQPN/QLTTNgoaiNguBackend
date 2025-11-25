package org.example.qlttngoaingu.dto.request;

import jakarta.persistence.Column;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;
@Data
public class StudentSignupRequest {
    @Size(min = 6, max = 20, message = "INVALID_PASSWORD")
    @NotBlank(message = "FIELD_REQUIRED")
    private String password = "123456";

    @NotBlank(message = "FIELD REQUIRED")
    @Pattern(
            regexp = "^(0|\\+84)[0-9]{9}$",
            message = "INVALID_PHONE_NUMBER"
    )
    @NotBlank(message = "FILED_REQUIRED")
    private String phoneNumber;

    @Email(message = "INVALID_EMAIL")
    private String email;

    private String name;

    private LocalDate ngaySinh;

    private Boolean gender;

    private String address;

    private String job;


}
