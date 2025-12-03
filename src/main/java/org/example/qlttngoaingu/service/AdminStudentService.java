package org.example.qlttngoaingu.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.qlttngoaingu.dto.request.StudentUpdateRequest;
import org.example.qlttngoaingu.dto.response.StudentAdminResponse;
import org.example.qlttngoaingu.entity.Student;
import org.example.qlttngoaingu.entity.User;
import org.example.qlttngoaingu.repository.StudentRepository;
import org.example.qlttngoaingu.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Service để quản lý học viên cho Admin
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AdminStudentService {

    private final StudentRepository studentRepository;
    private final UserRepository userRepository;
    private final JdbcTemplate jdbcTemplate;

    /**
     * Lấy danh sách học viên với tìm kiếm và phân trang
     */
    public Page<StudentAdminResponse> getStudents(String search, Pageable pageable) {
        List<StudentAdminResponse> students = new ArrayList<>();
        int totalElements = 0;
        
        try {
            // Xây dựng query với search
            String countSql;
            String dataSql;
            
            if (search != null && !search.trim().isEmpty()) {
                String searchPattern = "%" + search.trim() + "%";
                
                countSql = """
                    SELECT COUNT(*) FROM hocvien hv
                    INNER JOIN nguoidung nd ON hv.manguoidung = nd.manguoidung
                    WHERE hv.hoten LIKE ? OR nd.email LIKE ? OR nd.sdt LIKE ?
                    """;
                totalElements = jdbcTemplate.queryForObject(countSql, Integer.class, 
                        searchPattern, searchPattern, searchPattern);
                
                dataSql = """
                    SELECT 
                        hv.mahocvien,
                        hv.hoten,
                        nd.email,
                        nd.sdt,
                        hv.anhdaidien,
                        hv.ngaysinh,
                        hv.diachi,
                        hv.nghenghiep,
                        hv.trinhdo,
                        nd.ngaytao AS enrollment_date,
                        (SELECT COUNT(DISTINCT cthd.malophoc) 
                         FROM chitiethoadon cthd 
                         INNER JOIN hoadon hd ON cthd.hoadon_id = hd.mahoadon 
                         WHERE hd.mahocvien = hv.mahocvien AND hd.trangthai = 1) AS total_classes
                    FROM hocvien hv
                    INNER JOIN nguoidung nd ON hv.manguoidung = nd.manguoidung
                    WHERE hv.hoten LIKE ? OR nd.email LIKE ? OR nd.sdt LIKE ?
                    ORDER BY hv.mahocvien DESC
                    OFFSET ? ROWS FETCH NEXT ? ROWS ONLY
                    """;
                
                List<Map<String, Object>> results = jdbcTemplate.queryForList(dataSql,
                        searchPattern, searchPattern, searchPattern,
                        pageable.getOffset(), pageable.getPageSize());
                
                students = mapToStudentResponses(results);
            } else {
                countSql = "SELECT COUNT(*) FROM hocvien";
                totalElements = jdbcTemplate.queryForObject(countSql, Integer.class);
                
                dataSql = """
                    SELECT 
                        hv.mahocvien,
                        hv.hoten,
                        nd.email,
                        nd.sdt,
                        hv.anhdaidien,
                        hv.ngaysinh,
                        hv.diachi,
                        hv.nghenghiep,
                        hv.trinhdo,
                        nd.ngaytao AS enrollment_date,
                        (SELECT COUNT(DISTINCT cthd.malophoc) 
                         FROM chitiethoadon cthd 
                         INNER JOIN hoadon hd ON cthd.hoadon_id = hd.mahoadon 
                         WHERE hd.mahocvien = hv.mahocvien AND hd.trangthai = 1) AS total_classes
                    FROM hocvien hv
                    INNER JOIN nguoidung nd ON hv.manguoidung = nd.manguoidung
                    ORDER BY hv.mahocvien DESC
                    OFFSET ? ROWS FETCH NEXT ? ROWS ONLY
                    """;
                
                List<Map<String, Object>> results = jdbcTemplate.queryForList(dataSql,
                        pageable.getOffset(), pageable.getPageSize());
                
                students = mapToStudentResponses(results);
            }
            
        } catch (Exception e) {
            log.error("Lỗi khi lấy danh sách học viên: {}", e.getMessage());
        }
        
        return new PageImpl<>(students, pageable, totalElements);
    }

    /**
     * Lấy thông tin chi tiết học viên
     */
    public Optional<StudentAdminResponse> getStudentById(Integer id) {
        try {
            String sql = """
                SELECT 
                    hv.mahocvien,
                    hv.hoten,
                    nd.email,
                    nd.sdt,
                    hv.anhdaidien,
                    hv.ngaysinh,
                    hv.diachi,
                    hv.nghenghiep,
                    hv.trinhdo,
                    nd.ngaytao AS enrollment_date,
                    (SELECT COUNT(DISTINCT cthd.malophoc) 
                     FROM chitiethoadon cthd 
                     INNER JOIN hoadon hd ON cthd.hoadon_id = hd.mahoadon 
                     WHERE hd.mahocvien = hv.mahocvien AND hd.trangthai = 1) AS total_classes
                FROM hocvien hv
                INNER JOIN nguoidung nd ON hv.manguoidung = nd.manguoidung
                WHERE hv.mahocvien = ?
                """;
            
            List<Map<String, Object>> results = jdbcTemplate.queryForList(sql, id);
            if (results.isEmpty()) {
                return Optional.empty();
            }
            
            List<Integer> enrolledClassIds = getEnrolledClassIds(id);
            StudentAdminResponse response = mapToStudentResponse(results.get(0));
            response.setEnrolledClassIds(enrolledClassIds);
            
            return Optional.of(response);
        } catch (Exception e) {
            log.error("Lỗi khi lấy chi tiết học viên {}: {}", id, e.getMessage());
            return Optional.empty();
        }
    }

    /**
     * Cập nhật thông tin học viên
     */
    @Transactional
    public Optional<StudentAdminResponse> updateStudent(Integer id, StudentUpdateRequest request) {
        try {
            Optional<Student> optionalStudent = studentRepository.findById(id);
            if (optionalStudent.isEmpty()) {
                return Optional.empty();
            }
            
            Student student = optionalStudent.get();
            User user = student.getAccount();
            
            // Cập nhật Student
            if (request.getFullName() != null) {
                student.setName(request.getFullName());
            }
            if (request.getAddress() != null) {
                student.setAddress(request.getAddress());
            }
            if (request.getOccupation() != null) {
                student.setJob(request.getOccupation());
            }
            if (request.getEducationLevel() != null) {
                student.setLevel(request.getEducationLevel());
            }
            if (request.getAvatarUrl() != null) {
                student.setAvatar(request.getAvatarUrl());
            }
            if (request.getDateOfBirth() != null) {
                student.setNgaySinh(LocalDate.parse(request.getDateOfBirth()));
            }
            
            // Cập nhật User
            if (request.getEmail() != null) {
                user.setEmail(request.getEmail());
            }
            if (request.getPhoneNumber() != null) {
                user.setPhoneNumber(request.getPhoneNumber());
            }
            
            userRepository.save(user);
            studentRepository.save(student);
            
            return getStudentById(id);
        } catch (Exception e) {
            log.error("Lỗi khi cập nhật học viên {}: {}", id, e.getMessage());
            return Optional.empty();
        }
    }

    /**
     * Lấy danh sách lớp học của học viên
     */
    public List<Map<String, Object>> getStudentClasses(Integer studentId) {
        try {
            String sql = """
                SELECT 
                    l.malop AS classId,
                    l.tenlop AS className,
                    kh.tenkhoahoc AS courseName,
                    gv.hoten AS instructorName,
                    l.ngaybatdau AS startDate,
                    (SELECT MAX(bh.ngayhoc) FROM buoihoc bh WHERE bh.malop = l.malop) AS endDate,
                    l.trangthai AS status,
                    hd.trangthai AS paymentStatus,
                    cthd.giaban AS fee
                FROM chitiethoadon cthd
                INNER JOIN hoadon hd ON cthd.hoadon_id = hd.mahoadon
                INNER JOIN lop l ON cthd.malophoc = l.malop
                INNER JOIN khoahoc kh ON l.makhoahoc = kh.makhoahoc
                LEFT JOIN giangvien gv ON l.magiangvien = gv.magv
                WHERE hd.mahocvien = ? AND hd.trangthai = 1
                ORDER BY l.ngaybatdau DESC
                """;
            
            return jdbcTemplate.queryForList(sql, studentId);
        } catch (Exception e) {
            log.error("Lỗi khi lấy lớp học của học viên {}: {}", studentId, e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * Lấy danh sách ID lớp học đã đăng ký của học viên
     */
    private List<Integer> getEnrolledClassIds(Integer studentId) {
        try {
            String sql = """
                SELECT DISTINCT cthd.malophoc
                FROM chitiethoadon cthd
                INNER JOIN hoadon hd ON cthd.hoadon_id = hd.mahoadon
                WHERE hd.mahocvien = ? AND hd.trangthai = 1
                """;
            return jdbcTemplate.queryForList(sql, Integer.class, studentId);
        } catch (Exception e) {
            log.warn("Không thể lấy danh sách lớp học của học viên {}: {}", studentId, e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * Tìm học viên theo số điện thoại (chính xác)
     */
    public Optional<StudentAdminResponse> findByPhoneNumber(String phoneNumber) {
        try {
            String sql = """
                SELECT 
                    hv.mahocvien,
                    hv.hoten,
                    nd.email,
                    nd.sdt,
                    hv.anhdaidien,
                    hv.ngaysinh,
                    hv.diachi,
                    hv.nghenghiep,
                    hv.trinhdo,
                    nd.ngaytao AS enrollment_date,
                    (SELECT COUNT(DISTINCT cthd.malophoc) 
                     FROM chitiethoadon cthd 
                     INNER JOIN hoadon hd ON cthd.hoadon_id = hd.mahoadon 
                     WHERE hd.mahocvien = hv.mahocvien AND hd.trangthai = 1) AS total_classes
                FROM hocvien hv
                INNER JOIN nguoidung nd ON hv.manguoidung = nd.manguoidung
                WHERE nd.sdt = ?
                """;
            
            List<Map<String, Object>> results = jdbcTemplate.queryForList(sql, phoneNumber);
            if (results.isEmpty()) {
                return Optional.empty();
            }
            
            return Optional.of(mapToStudentResponse(results.get(0)));
        } catch (Exception e) {
            log.error("Lỗi khi tìm học viên theo SĐT {}: {}", phoneNumber, e.getMessage());
            return Optional.empty();
        }
    }

    // Helper methods
    private List<StudentAdminResponse> mapToStudentResponses(List<Map<String, Object>> results) {
        List<StudentAdminResponse> responses = new ArrayList<>();
        for (Map<String, Object> row : results) {
            responses.add(mapToStudentResponse(row));
        }
        return responses;
    }
    
    private StudentAdminResponse mapToStudentResponse(Map<String, Object> row) {
        return StudentAdminResponse.builder()
                .id(getIntValue(row, "mahocvien"))
                .fullName(getStringValue(row, "hoten"))
                .email(getStringValue(row, "email"))
                .phoneNumber(getStringValue(row, "sdt"))
                .avatarUrl(getStringValue(row, "anhdaidien"))
                .dateOfBirth(getLocalDate(row, "ngaysinh"))
                .address(getStringValue(row, "diachi"))
                .occupation(getStringValue(row, "nghenghiep"))
                .educationLevel(getStringValue(row, "trinhdo"))
                .enrollmentDate(getLocalDateTime(row, "enrollment_date"))
                .totalClassesEnrolled(getIntValue(row, "total_classes"))
                .build();
    }
    
    private Integer getIntValue(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value == null) return 0;
        if (value instanceof Number) return ((Number) value).intValue();
        return Integer.parseInt(value.toString());
    }
    
    private String getStringValue(Map<String, Object> map, String key) {
        Object value = map.get(key);
        return value != null ? value.toString() : null;
    }
    
    private LocalDate getLocalDate(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value == null) return null;
        if (value instanceof LocalDate) return (LocalDate) value;
        if (value instanceof java.sql.Date) return ((java.sql.Date) value).toLocalDate();
        return null;
    }
    
    private LocalDateTime getLocalDateTime(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value == null) return null;
        if (value instanceof LocalDateTime) return (LocalDateTime) value;
        if (value instanceof java.sql.Timestamp) return ((java.sql.Timestamp) value).toLocalDateTime();
        return null;
    }
}
