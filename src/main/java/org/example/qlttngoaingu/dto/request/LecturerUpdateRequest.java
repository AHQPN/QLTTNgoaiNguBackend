package org.example.qlttngoaingu.dto.request;

import java.time.LocalDate;
import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
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
public class LecturerUpdateRequest {
    
    private String fullName;
    
    @Past(message = "Ngày sinh phải là ngày trong quá khứ")
    private LocalDate dateOfBirth;
    
    private String imagePath;
    
    // Thông tin liên hệ
    @Email(message = "Email không đúng định dạng")
    private String email;
    
    @Pattern(regexp = "^[0-9]{10,11}$", message = "Số điện thoại phải là 10-11 chữ số")
    private String phoneNumber;
    
    // Mật khẩu mới (nếu cần đổi)
    private String password;
    
    // Danh sách bằng cấp (nếu null thì không cập nhật, nếu có thì replace toàn bộ)
    @Valid
    private List<CertificateRequest> certificates;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CertificateRequest {
        private Integer degreeId;
        private String level;
    }
}
