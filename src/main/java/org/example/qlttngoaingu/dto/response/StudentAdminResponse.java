package org.example.qlttngoaingu.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Response DTO cho danh sách học viên (Admin view)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StudentAdminResponse {
    
    // ID học viên
    private Integer id;
    
    // Họ tên
    private String fullName;
    
    // Email (từ bảng nguoidung)
    private String email;
    
    // Số điện thoại (từ bảng nguoidung)
    private String phoneNumber;
    
    // Ảnh đại diện
    private String avatarUrl;
    
    // Ngày sinh
    private LocalDate dateOfBirth;
    
    // Địa chỉ
    private String address;
    
    // Nghề nghiệp
    private String occupation;
    
    // Trình độ học vấn
    private String educationLevel;
    
    // Ngày đăng ký (từ nguoidung.ngaytao)
    private LocalDateTime enrollmentDate;
    
    // Tổng số lớp đã đăng ký
    private Integer totalClassesEnrolled;
    
    // Danh sách ID các lớp đã đăng ký
    private List<Integer> enrolledClassIds;
}
