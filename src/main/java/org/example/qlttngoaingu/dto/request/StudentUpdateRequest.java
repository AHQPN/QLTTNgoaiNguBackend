package org.example.qlttngoaingu.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO cho cập nhật thông tin học viên từ Admin
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StudentUpdateRequest {
    
    // Họ tên
    private String fullName;
    
    // Email
    private String email;
    
    // Số điện thoại
    private String phoneNumber;
    
    // Ngày sinh (format: yyyy-MM-dd)
    private String dateOfBirth;
    
    // Địa chỉ
    private String address;
    
    // Nghề nghiệp
    private String occupation;
    
    // Trình độ học vấn
    private String educationLevel;
    
    // Avatar URL
    private String avatarUrl;
    
    // Trạng thái hoạt động
    private Boolean isActive;
}
