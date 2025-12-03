package org.example.qlttngoaingu.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Response DTO cho Admin Dashboard Statistics
 * Mapping từ view vw_Dashboard_TongQuan
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardStatsResponse {
    
    // Tổng số học viên
    private Integer tongHocVien;
    
    // Tổng số giảng viên
    private Integer tongGiangVien;
    
    // Tổng số lớp học
    private Integer tongLop;
    
    // Số lớp đang dạy (InProgress)
    private Integer lopDangDay;
    
    // Doanh thu tháng hiện tại
    private BigDecimal doanhThuThang;
    
    // Tổng số khóa học
    private Integer tongKhoaHoc;
    
    // Số phòng trống
    private Integer soPhongTrong;
    
    // Số đăng ký trong ngày hôm nay
    private Integer dangKyHomNay;
    
    // === Aliases cho Frontend mapping ===
    
    public Integer getActiveStudents() {
        return tongHocVien;
    }
    
    public Integer getTotalTeachers() {
        return tongGiangVien;
    }
    
    public Integer getOngoingClasses() {
        return lopDangDay;
    }
    
    public BigDecimal getMonthlyRevenue() {
        return doanhThuThang;
    }
    
    public Integer getTotalCourses() {
        return tongKhoaHoc;
    }
    
    public Integer getTodayRegistrations() {
        return dangKyHomNay;
    }
}
