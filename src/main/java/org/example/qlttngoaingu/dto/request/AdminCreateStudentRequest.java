package org.example.qlttngoaingu.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

import java.time.LocalDate;

/**
 * DTO cho Admin tạo học viên mới (đăng ký nhanh tại quầy)
 * Không yêu cầu password - sẽ dùng mật khẩu mặc định
 */
@Data
public class AdminCreateStudentRequest {
    
    @NotBlank(message = "Số điện thoại là bắt buộc")
    @Pattern(
            regexp = "^(0|\\+84)[0-9]{9}$",
            message = "Số điện thoại không hợp lệ"
    )
    private String phoneNumber;

    @Email(message = "Email không hợp lệ")
    private String email;

    @NotBlank(message = "Họ tên là bắt buộc")
    private String name;

    private LocalDate dateOfBirth;

    private Boolean gender; // true = Nam, false = Nữ

    private String address;

    private String job;
}
