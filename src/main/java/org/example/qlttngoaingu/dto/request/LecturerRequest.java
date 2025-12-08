package org.example.qlttngoaingu.dto.request;

import java.time.LocalDate;
import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LecturerRequest {
    
    @NotBlank(message = "Họ tên không được để trống")
    private String fullName;
    
    @NotNull(message = "Ngày sinh không được để trống")
    @Past(message = "Ngày sinh phải là ngày trong quá khứ")
    private LocalDate dateOfBirth;
    
    private String imagePath;
    
    // Thông tin liên hệ
    @NotBlank(message = "Email không được để trống")
    @Email(message = "Email không đúng định dạng")
    private String email;
    
    @NotBlank(message = "Số điện thoại không được để trống")
    @Pattern(regexp = "^[0-9]{10,11}$", message = "Số điện thoại phải là 10-11 chữ số")
    private String phoneNumber;
    
    // Mật khẩu cho tài khoản (nếu tạo mới)
    @NotBlank(message = "Mật khẩu không được để trống")
    private String password;
    
    // Danh sách bằng cấp
    @Valid
    private List<CertificateRequest> certificates;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CertificateRequest {
        @NotNull(message = "Loại bằng cấp không được để trống")
        private Integer degreeTypeId;  // ID của loại bằng cấp (bảng loaibangcap: IELTS, TOEIC...)
        
        private String level; // Trình độ cụ thể (VD: "Band 8.0", "950 điểm", "Giỏi")
    }
}
