package org.example.qlttngoaingu.controller;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.example.qlttngoaingu.dto.request.AdminCreateStudentRequest;
import org.example.qlttngoaingu.dto.request.StudentUpdateRequest;
import org.example.qlttngoaingu.dto.response.ApiResponse;
import org.example.qlttngoaingu.dto.response.StudentAdminResponse;
import org.example.qlttngoaingu.dto.response.StudentInfo;
import org.example.qlttngoaingu.service.AdminStudentService;
import org.example.qlttngoaingu.service.UserService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * Controller để quản lý học viên cho Admin
 */
@RestController
@RequestMapping("/admin/students")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN', 'ACADEMIC_MANAGER')")
public class AdminStudentController {

    private final AdminStudentService adminStudentService;
    private final UserService userService;

    /**
     * GET /admin/students
     * Lấy danh sách học viên với tìm kiếm và phân trang
     */
    @GetMapping
    public ResponseEntity<ApiResponse> getStudents(
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        Pageable pageable = PageRequest.of(page, size);
        Page<StudentAdminResponse> students = adminStudentService.getStudents(search, pageable);
        
        return ResponseEntity.ok(ApiResponse.builder()
                .data(Map.of(
                        "content", students.getContent(),
                        "totalElements", students.getTotalElements(),
                        "totalPages", students.getTotalPages(),
                        "currentPage", students.getNumber(),
                        "pageSize", students.getSize()
                ))
                .build());
    }

    /**
     * GET /admin/students/{id}
     * Lấy thông tin chi tiết học viên
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse> getStudentById(@PathVariable Integer id) {
        Optional<StudentAdminResponse> student = adminStudentService.getStudentById(id);
        
        if (student.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        return ResponseEntity.ok(ApiResponse.builder()
                .data(student.get())
                .build());
    }

    /**
     * PUT /admin/students/{id}
     * Cập nhật thông tin học viên
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse> updateStudent(
            @PathVariable Integer id,
            @RequestBody StudentUpdateRequest request) {
        
        Optional<StudentAdminResponse> updatedStudent = adminStudentService.updateStudent(id, request);
        
        if (updatedStudent.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        return ResponseEntity.ok(ApiResponse.builder()
                .data(updatedStudent.get())
                .build());
    }

    /**
     * GET /admin/students/{id}/classes
     * Lấy danh sách lớp học của học viên
     */
    @GetMapping("/{id}/classes")
    public ResponseEntity<ApiResponse> getStudentClasses(@PathVariable Integer id) {
        List<Map<String, Object>> classes = adminStudentService.getStudentClasses(id);
        
        return ResponseEntity.ok(ApiResponse.builder()
                .data(classes)
                .build());
    }

    /**
     * POST /admin/students
     * Admin tạo học viên mới (không gửi email xác thực, mật khẩu mặc định)
     * Dùng cho đăng ký nhanh tại quầy
     */
    @PostMapping
    public ResponseEntity<ApiResponse> createStudent(
            @Valid @RequestBody AdminCreateStudentRequest request) {
        
        StudentInfo student = userService.createStudentByAdmin(request);
        
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.builder()
                        .message("Tạo học viên thành công")
                        .data(student)
                        .build());
    }

    /**
     * GET /admin/students/search-by-phone
     * Tìm học viên theo số điện thoại (chính xác)
     */
    @GetMapping("/search-by-phone")
    public ResponseEntity<ApiResponse> searchByPhone(@RequestParam String phone) {
        Optional<StudentAdminResponse> student = adminStudentService.findByPhoneNumber(phone);
        
        if (student.isEmpty()) {
            return ResponseEntity.ok(ApiResponse.builder()
                    .message("Không tìm thấy học viên với SĐT này")
                    .data(null)
                    .build());
        }
        
        return ResponseEntity.ok(ApiResponse.builder()
                .data(student.get())
                .build());
    }
}