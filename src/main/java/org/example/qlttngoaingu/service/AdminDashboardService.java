package org.example.qlttngoaingu.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.example.qlttngoaingu.dto.response.ActivityResponse;
import org.example.qlttngoaingu.dto.response.CourseProgressResponse;
import org.example.qlttngoaingu.dto.response.DashboardStatsResponse;
import org.example.qlttngoaingu.dto.response.EndingClassResponse;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Service để lấy dữ liệu thống kê Dashboard cho Admin
 * Sử dụng các view có sẵn trong database
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AdminDashboardService {

    private final JdbcTemplate jdbcTemplate;

    /**
     * Lấy thống kê tổng quan Dashboard từ view vw_Dashboard_TongQuan
     */
    public DashboardStatsResponse getDashboardStats() {
        String sql = "SELECT * FROM vw_Dashboard_TongQuan";
        
        try {
            Map<String, Object> result = jdbcTemplate.queryForMap(sql);
            
            // Đếm số đăng ký hôm nay
            int todayRegistrations = countTodayRegistrations();
            
            return DashboardStatsResponse.builder()
                    .tongHocVien(getIntValue(result, "tong_hocvien"))
                    .tongGiangVien(getIntValue(result, "tong_giangvien"))
                    .tongKhoaHoc(getIntValue(result, "tong_khoahoc_active"))
                    .tongLop(getIntValue(result, "tong_lop_dangmo"))
                    .lopDangDay(getIntValue(result, "tong_lop_dangmo"))
                    .doanhThuThang(getBigDecimalValue(result, "doanhthu_thangnay"))
                    .soPhongTrong(getIntValue(result, "so_phong_trong"))
                    .dangKyHomNay(todayRegistrations)
                    .build();
        } catch (Exception e) {
            log.error("Lỗi khi lấy thống kê Dashboard: {}", e.getMessage());
            return getDefaultStats();
        }
    }
    
    /**
     * Đếm số đăng ký hôm nay từ bảng hoadon
     */
    private int countTodayRegistrations() {
        String sql = "SELECT COUNT(*) FROM hoadon WHERE CAST(ngaytao AS DATE) = CAST(GETDATE() AS DATE)";
        try {
            Integer count = jdbcTemplate.queryForObject(sql, Integer.class);
            return count != null ? count : 0;
        } catch (Exception e) {
            log.warn("Không thể đếm đăng ký hôm nay: {}", e.getMessage());
            return 0;
        }
    }
    
    /**
     * Lấy các hoạt động gần đây
     */
    public List<ActivityResponse> getRecentActivities(int limit) {
        List<ActivityResponse> activities = new ArrayList<>();
        
        // Lấy các đăng ký mới nhất
        try {
            String registrationSql = """
                SELECT TOP (?) 
                    hd.mahoadon, 
                    hv.hoten, 
                    nd.manguoidung,
                    hd.ngaytao,
                    l.tenlop
                FROM hoadon hd
                INNER JOIN hocvien hv ON hd.mahocvien = hv.mahocvien
                INNER JOIN nguoidung nd ON hv.manguoidung = nd.manguoidung
                INNER JOIN chitiethoadon cthd ON hd.mahoadon = cthd.hoadon_id
                INNER JOIN lop l ON cthd.malophoc = l.malop
                ORDER BY hd.ngaytao DESC
                """;
            
            List<Map<String, Object>> registrations = jdbcTemplate.queryForList(registrationSql, limit);
            
            for (Map<String, Object> reg : registrations) {
                activities.add(ActivityResponse.builder()
                        .id("reg_" + reg.get("mahoadon"))
                        .type("registration")
                        .title("Đăng ký lớp học mới")
                        .description(reg.get("hoten") + " đã đăng ký lớp " + reg.get("tenlop"))
                        .timestamp(getLocalDateTime(reg, "ngaytao"))
                        .userId(String.valueOf(reg.get("manguoidung")))
                        .userName(String.valueOf(reg.get("hoten")))
                        .build());
            }
        } catch (Exception e) {
            log.warn("Không thể lấy hoạt động đăng ký: {}", e.getMessage());
        }
        
        // Lấy các thanh toán mới nhất
        try {
            String paymentSql = """
                SELECT TOP (?)
                    hd.mahoadon,
                    hv.hoten,
                    nd.manguoidung,
                    hd.ngaythanhtoan,
                    hd.tongtien
                FROM hoadon hd
                INNER JOIN hocvien hv ON hd.mahocvien = hv.mahocvien
                INNER JOIN nguoidung nd ON hv.manguoidung = nd.manguoidung
                WHERE hd.trangthai = 1 AND hd.ngaythanhtoan IS NOT NULL
                ORDER BY hd.ngaythanhtoan DESC
                """;
            
            List<Map<String, Object>> payments = jdbcTemplate.queryForList(paymentSql, limit);
            
            for (Map<String, Object> payment : payments) {
                BigDecimal amount = getBigDecimalValue(payment, "tongtien");
                activities.add(ActivityResponse.builder()
                        .id("pay_" + payment.get("mahoadon"))
                        .type("payment")
                        .title("Thanh toán thành công")
                        .description(payment.get("hoten") + " đã thanh toán " + formatCurrency(amount))
                        .timestamp(getLocalDateTime(payment, "ngaythanhtoan"))
                        .userId(String.valueOf(payment.get("manguoidung")))
                        .userName(String.valueOf(payment.get("hoten")))
                        .build());
            }
        } catch (Exception e) {
            log.warn("Không thể lấy hoạt động thanh toán: {}", e.getMessage());
        }
        
        // Sắp xếp theo thời gian giảm dần
        activities.sort((a, b) -> {
            if (a.getTimestamp() == null) return 1;
            if (b.getTimestamp() == null) return -1;
            return b.getTimestamp().compareTo(a.getTimestamp());
        });
        
        // Giới hạn số lượng
        return activities.stream().limit(limit).toList();
    }
    
    // Helper methods
    private int getIntValue(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value == null) return 0;
        if (value instanceof Number) return ((Number) value).intValue();
        return Integer.parseInt(value.toString());
    }
    
    private BigDecimal getBigDecimalValue(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value == null) return BigDecimal.ZERO;
        if (value instanceof BigDecimal) return (BigDecimal) value;
        if (value instanceof Number) return BigDecimal.valueOf(((Number) value).doubleValue());
        return new BigDecimal(value.toString());
    }
    
    private LocalDateTime getLocalDateTime(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value == null) return null;
        if (value instanceof LocalDateTime) return (LocalDateTime) value;
        if (value instanceof java.sql.Timestamp) return ((java.sql.Timestamp) value).toLocalDateTime();
        return null;
    }
    
    private String formatCurrency(BigDecimal amount) {
        if (amount == null) return "0 VNĐ";
        return String.format("%,.0f VNĐ", amount);
    }
    
    private DashboardStatsResponse getDefaultStats() {
        return DashboardStatsResponse.builder()
                .tongHocVien(0)
                .tongGiangVien(0)
                .tongKhoaHoc(0)
                .tongLop(0)
                .lopDangDay(0)
                .doanhThuThang(BigDecimal.ZERO)
                .soPhongTrong(0)
                .dangKyHomNay(0)
                .build();
    }
    
    /**
     * Lấy tiến độ khóa học (Course Progress)
     * Theo dõi tiến độ hoàn thành các buổi học của từng lớp đang hoạt động
     */
    public List<CourseProgressResponse> getCourseProgress() {
        String sql = """
            SELECT TOP 10
                l.malop AS classId,
                l.tenlop AS className,
                k.tenkh AS courseName,
                COUNT(CASE WHEN b.trangthai = 'Completed' THEN 1 END) AS completedSessions,
                k.sotiet AS totalSessions,
                CAST(COUNT(CASE WHEN b.trangthai = 'Completed' THEN 1 END) * 100.0 / 
                    NULLIF(k.sotiet, 0) AS DECIMAL(5,2)) AS progressRate
            FROM lop l
            INNER JOIN khoahoc k ON l.makhoahoc = k.makhoahoc
            LEFT JOIN buoihoc b ON b.malop = l.malop
            WHERE l.trangthai IN ('InProgress', 'Pending')
            GROUP BY l.malop, l.tenlop, k.tenkh, k.sotiet
            ORDER BY progressRate DESC
            """;
        
        try {
            List<Map<String, Object>> results = jdbcTemplate.queryForList(sql);
            List<CourseProgressResponse> progressList = new ArrayList<>();
            
            for (Map<String, Object> row : results) {
                CourseProgressResponse progress = CourseProgressResponse.builder()
                        .classId(getIntValue(row, "classId"))
                        .className(String.valueOf(row.get("className")))
                        .courseName(String.valueOf(row.get("courseName")))
                        .completedSessions(getIntValue(row, "completedSessions"))
                        .totalSessions(getIntValue(row, "totalSessions"))
                        .progressRate(getDoubleValue(row, "progressRate"))
                        .build();
                progressList.add(progress);
            }
            
            return progressList;
        } catch (Exception e) {
            log.error("Lỗi khi lấy tiến độ khóa học: {}", e.getMessage());
            return new ArrayList<>();
        }
    }
    
    /**
     * Lấy danh sách lớp học sắp kết thúc
     * Cảnh báo các lớp học sắp kết thúc để admin chuẩn bị các công việc kế tiếp
     */
    public List<EndingClassResponse> getEndingClasses() {
        String sql = """
            WITH ClassProgress AS (
                SELECT 
                    l.malop AS classId,
                    l.tenlop AS className,
                    k.tenkh AS courseName,
                    k.sotiet AS totalSessions,
                    COUNT(CASE WHEN b.trangthai = 'Completed' THEN 1 END) AS completedSessions,
                    (k.sotiet - COUNT(CASE WHEN b.trangthai = 'Completed' THEN 1 END)) AS remainingSessions,
                    l.ngaybatdau AS startDate,
                    l.lich AS schedule
                FROM lop l
                INNER JOIN khoahoc k ON l.makhoahoc = k.makhoahoc
                LEFT JOIN buoihoc b ON b.malop = l.malop
                WHERE l.trangthai = 'InProgress'
                GROUP BY l.malop, l.tenlop, k.tenkh, k.sotiet, l.ngaybatdau, l.lich
            )
            SELECT TOP 5
                classId,
                className,
                courseName,
                remainingSessions,
                DATEADD(DAY, 
                    CASE 
                        WHEN remainingSessions <= 0 THEN 0
                        ELSE remainingSessions * 3
                    END, 
                    startDate
                ) AS endDate
            FROM ClassProgress
            WHERE remainingSessions <= 5
            ORDER BY remainingSessions ASC, endDate ASC
            """;
        
        try {
            List<Map<String, Object>> results = jdbcTemplate.queryForList(sql);
            List<EndingClassResponse> endingClasses = new ArrayList<>();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            
            for (Map<String, Object> row : results) {
                // Format endDate
                String endDate = "";
                Object endDateObj = row.get("endDate");
                if (endDateObj != null) {
                    if (endDateObj instanceof java.sql.Timestamp) {
                        endDate = ((java.sql.Timestamp) endDateObj).toLocalDateTime().toLocalDate().format(formatter);
                    } else if (endDateObj instanceof LocalDateTime) {
                        endDate = ((LocalDateTime) endDateObj).toLocalDate().format(formatter);
                    } else if (endDateObj instanceof LocalDate) {
                        endDate = ((LocalDate) endDateObj).format(formatter);
                    }
                }
                
                EndingClassResponse endingClass = EndingClassResponse.builder()
                        .classId(getIntValue(row, "classId"))
                        .className(String.valueOf(row.get("className")))
                        .courseName(String.valueOf(row.get("courseName")))
                        .remainingSessions(getIntValue(row, "remainingSessions"))
                        .endDate(endDate)
                        .build();
                endingClasses.add(endingClass);
            }
            
            return endingClasses;
        } catch (Exception e) {
            log.error("Lỗi khi lấy danh sách lớp sắp kết thúc: {}", e.getMessage());
            return new ArrayList<>();
        }
    }
    
    /**
     * Helper method để lấy giá trị Double từ Map
     */
    private Double getDoubleValue(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value == null) return 0.0;
        if (value instanceof Double) return (Double) value;
        if (value instanceof BigDecimal) return ((BigDecimal) value).doubleValue();
        if (value instanceof Integer) return ((Integer) value).doubleValue();
        if (value instanceof Long) return ((Long) value).doubleValue();
        try {
            return Double.parseDouble(value.toString());
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }
}
