package org.example.qlttngoaingu.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * Request DTO để cập nhật thông tin giảng viên
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LecturerUpdateRequest {
    
    /**
     * Họ tên đầy đủ
     */
    private String fullName;
    
    /**
     * Ngày sinh
     */
    private LocalDate dateOfBirth;
    
    /**
     * Email (không bắt buộc, nhưng nếu có phải đúng format)
     */
    @Email(message = "Email không hợp lệ")
    private String email;
    
    /**
     * Số điện thoại
     */
    @Pattern(regexp = "^(0|\\+84)[0-9]{9,10}$", message = "Số điện thoại không hợp lệ")
    private String phoneNumber;
    
    /**
     * Địa chỉ
     */
    private String address;
    
    /**
     * Giới tính: true = Nam, false = Nữ, null = không đổi
     */
    private Boolean gender;
    
    /**
     * Chuyên môn/Lĩnh vực giảng dạy
     */
    private String specialization;
    
    /**
     * Đường dẫn ảnh đại diện
     */
    private String imagePath;
}
