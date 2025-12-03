package org.example.qlttngoaingu.dto.response;

import jakarta.persistence.Column;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class TeacherInfo {

    private Integer lecturerId;
    private String fullName;
    private LocalDate dateOfBirth;
    private String imagePath;
    private String phoneNumber;
    private String email;

    // Thống kê cho Flutter FE
    private Integer totalClasses;
    private Integer totalStudents;
    private Double rating;
    private Integer totalReviews;

    // Thông tin tài khoản (chỉ Admin mới xem được)
    private AccountInfo accountInfo;

    private List<QualificationDTO> qualifications;

    @Data
    public static class QualificationDTO {
        private Integer degreeId;
        private String degreeName;
        private String level;
    }

    @Data
    public static class AccountInfo {
        private Integer userId;
        private String username; // email hoặc sdt
        private String password; // chỉ trả về nếu là admin
        private String role;
        private LocalDateTime createdAt;
        private Boolean isVerified;
    }
}
