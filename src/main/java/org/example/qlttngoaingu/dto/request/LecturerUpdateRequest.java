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
    @Past(message = "Ngày sinh phải là ngày trong quá khứ")
    private LocalDate dateOfBirth;

    /**
     * Email (không bắt buộc)
     */
    @Email(message = "Email không hợp lệ")
    private String email;

    /**
     * Số điện thoại
     * - HEAD: (0|+84) + 9–10 số
     * - master: 10–11 số
     * → Hợp nhất theo chuẩn Việt Nam: 0xxxxxxxxx hoặc +84xxxxxxxxx
     */
    @Pattern(
        regexp = "^(0|\\+84)[0-9]{9,10}$",
        message = "Số điện thoại không hợp lệ"
    )
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
     * Chuyên môn giảng dạy
     */
    private String specialization;

    /**
     * Ảnh đại diện
     */
    private String imagePath;

    /**
     * Mật khẩu mới (nếu cần đổi)
     */
    private String password;

    /**
     * Danh sách bằng cấp - REPLACE ALL strategy:
     * - null: Không thay đổi bằng cấp hiện có
     * - []: Xóa tất cả bằng cấp
     * - [{degreeTypeId: 1, level: "Band 8.0"}]: Xóa tất cả cũ, chỉ giữ list này
     * 
     * Frontend cần gửi TOÀN BỘ bằng cấp muốn giữ lại
     */
    @Valid
    private List<CertificateRequest> certificates;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CertificateRequest {
        private Integer degreeTypeId;  // ID của loại bằng cấp (bảng loaibangcap: IELTS, TOEIC...)
        private String level;          // Trình độ cụ thể (Band 8.0, 950 điểm...)
    }
}
