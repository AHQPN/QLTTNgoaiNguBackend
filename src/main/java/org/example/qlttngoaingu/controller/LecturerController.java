package org.example.qlttngoaingu.controller;

import java.util.List;

import org.example.qlttngoaingu.dto.request.AttendanceSessionRequest;
import org.example.qlttngoaingu.dto.request.CheckConflictRequest;
import org.example.qlttngoaingu.dto.request.GradeRequest;
import org.example.qlttngoaingu.dto.request.LecturerRequest;
import org.example.qlttngoaingu.dto.request.LecturerUpdateRequest;
import org.example.qlttngoaingu.dto.response.ApiResponse;
import org.example.qlttngoaingu.dto.response.AttendanceSessionResponse;
import org.example.qlttngoaingu.dto.response.AvailableLecturerResponse;
import org.example.qlttngoaingu.dto.response.ClassGradesResponse;
import org.example.qlttngoaingu.dto.response.LecturerResponse;
import org.example.qlttngoaingu.entity.GradeSheet;
import org.example.qlttngoaingu.security.model.UserDetailsImpl;
import org.example.qlttngoaingu.service.AttendanceService;
import org.example.qlttngoaingu.service.GradeService;
import org.example.qlttngoaingu.service.LecturerService;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
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

@RestController
@RequestMapping("/lecturers")
@RequiredArgsConstructor
public class LecturerController {
        private final LecturerService lecturerService;
        private final AttendanceService attendanceService;
        private final GradeService gradeService;

        // ==================== CRUD APIs với Phân trang ====================

        /**
         * GET /lecturers - Lấy danh sách giảng viên có phân trang
         * 
         * @param page          Số trang (bắt đầu từ 0)
         * @param size          Số lượng mỗi trang
         * @param sortBy        Trường sắp xếp (mặc định: lecturerId)
         * @param sortDirection Hướng sắp xếp (asc/desc)
         */
        @GetMapping
        public ResponseEntity<ApiResponse<Page<LecturerResponse>>> getAllLecturersPaginated(
                        @RequestParam(defaultValue = "0") int page,
                        @RequestParam(defaultValue = "10") int size,
                        @RequestParam(defaultValue = "lecturerId") String sortBy,
                        @RequestParam(defaultValue = "asc") String sortDirection) {
                Page<LecturerResponse> lecturers = lecturerService.getAllLecturers(page, size, sortBy, sortDirection);
                return ResponseEntity.ok(ApiResponse.<Page<LecturerResponse>>builder()
                                .message("Lấy danh sách giảng viên thành công")
                                .data(lecturers)
                                .build());
        }

        /**
         * GET /lecturers/{id} - Lấy thông tin chi tiết giảng viên
         */
        @GetMapping("/detail/{id}")
        public ResponseEntity<ApiResponse<LecturerResponse>> getLecturerDetail(@PathVariable Integer id) {
                LecturerResponse lecturer = lecturerService.getLecturerByIdForCRUD(id);
                return ResponseEntity.ok(ApiResponse.<LecturerResponse>builder()
                                .message("Lấy thông tin giảng viên thành công")
                                .data(lecturer)
                                .build());
        }

        /**
         * POST /lecturers - Tạo giảng viên mới
         */
        @PostMapping
        public ResponseEntity<ApiResponse<LecturerResponse>> createLecturer(
                        @Valid @RequestBody LecturerRequest request) {
                LecturerResponse newLecturer = lecturerService.createLecturer(request);
                return ResponseEntity.ok(ApiResponse.<LecturerResponse>builder()
                                .message("Tạo giảng viên thành công")
                                .data(newLecturer)
                                .build());
        }

        /**
         * PUT /lecturers/{id} - Cập nhật thông tin giảng viên
         */
        @PutMapping("/{id}")
        public ResponseEntity<ApiResponse<LecturerResponse>> updateLecturer(
                        @PathVariable Integer id,
                        @Valid @RequestBody LecturerUpdateRequest request) {
                LecturerResponse updatedLecturer = lecturerService.updateLecturer(id, request);
                return ResponseEntity.ok(ApiResponse.<LecturerResponse>builder()
                                .message("Cập nhật giảng viên thành công")
                                .data(updatedLecturer)
                                .build());
        }

        /**
         * DELETE /lecturers/{id} - Xóa giảng viên
         */
        @DeleteMapping("/{id}")
        public ResponseEntity<ApiResponse<Void>> deleteLecturer(@PathVariable Integer id) {
                lecturerService.deleteLecturer(id);
                return ResponseEntity.ok(ApiResponse.<Void>builder()
                                .message("Xóa giảng viên thành công")
                                .build());
        }

        // ==================== Các API khác ====================

        @PostMapping("/available")
        public ResponseEntity<List<AvailableLecturerResponse>> getAvailableLecturers(
                        @RequestBody CheckConflictRequest request) {
                List<AvailableLecturerResponse> lecturers = lecturerService.getAvailableLecturers(
                                request.getSchedulePattern(),
                                request.getStartTime(),
                                request.getDurationMinutes(),
                                request.getStartDate());

                return ResponseEntity.ok(lecturers);
        }

        // ==================== Các API khác ====================

        @GetMapping("lecturer-name")
        public ResponseEntity<ApiResponse> getAllLecturerNames() {
                return ResponseEntity.ok(
                                ApiResponse.builder().data(lecturerService.getAllLecturers()).build());
        }

        @GetMapping("/{id}")
        public ResponseEntity<ApiResponse> getLecturerInfo(@AuthenticationPrincipal UserDetailsImpl user,
                        @PathVariable Integer id) {
                return ResponseEntity.ok(
                                ApiResponse.builder().data(lecturerService.getLecturerById(user.getId(), id)).build());
        }

        @GetMapping("/me")
        public ResponseEntity<ApiResponse> getMyInfo(
                        @AuthenticationPrincipal UserDetailsImpl user) {
                return ResponseEntity.ok(
                                ApiResponse.builder()
                                                .data(lecturerService.getLecturerById(user.getId(), null))
                                                .build());
        }

        /**
         * PUT /lecturers/me
         * Giảng viên tự cập nhật thông tin của mình
         */
        @PutMapping("/me")
        public ResponseEntity<ApiResponse> updateMyInfo(
                        @AuthenticationPrincipal UserDetailsImpl user,
                        @Valid @RequestBody LecturerUpdateRequest request) {
                // Lấy lecturerId từ userId của user đang đăng nhập
                var lecturer = lecturerService.getLecturerByUserId(user.getId());
                var updatedInfo = lecturerService.updateLecturer(lecturer.getLecturerId(), request);
                return ResponseEntity.ok(
                                ApiResponse.builder()
                                                .message("Cập nhật thông tin thành công")
                                                .data(updatedInfo)
                                                .build());
        }

        @PostMapping("/sessions/{sessionId}/attendance")
        public ResponseEntity<ApiResponse<AttendanceSessionResponse>> markAttendance(
                        @AuthenticationPrincipal UserDetailsImpl principal, @PathVariable Integer sessionId,
                        @RequestBody AttendanceSessionRequest request) {
                if (!sessionId.equals(request.getSessionId())) {
                        return ResponseEntity.badRequest().body(ApiResponse.<AttendanceSessionResponse>builder()
                                        .message("sessionId mismatch").build());
                }
                AttendanceSessionResponse resp = attendanceService.markAttendance(principal.getId(), request);
                return ResponseEntity.ok().body(ApiResponse.<AttendanceSessionResponse>builder().data(resp).build());
        }

        // ==================== GRADES APIs (TEA-01, TEA-02, TEA-03)
        // ====================

        /**
         * TEA-01: GET /lecturers/grades/class/{classId}
         * Lấy danh sách điểm của tất cả học viên trong lớp
         */
        @GetMapping("/grades/class/{classId}")
        public ResponseEntity<ApiResponse> getClassGrades(
                        @AuthenticationPrincipal UserDetailsImpl principal,
                        @PathVariable Integer classId) {
                ClassGradesResponse grades = gradeService.getClassGrades(principal.getId(), classId);
                return ResponseEntity.ok(ApiResponse.builder()
                                .message("Lấy điểm lớp thành công")
                                .data(grades)
                                .build());
        }

        /**
         * TEA-02: POST /lecturers/grades
         * Nhập điểm cho học viên
         */
        @PostMapping("/grades")
        public ResponseEntity<ApiResponse> submitGrade(
                        @AuthenticationPrincipal UserDetailsImpl principal,
                        @Valid @RequestBody GradeRequest request) {
                GradeSheet gradeSheet = gradeService.submitGrade(principal.getId(), request);
                return ResponseEntity.ok(ApiResponse.builder()
                                .message("Nhập điểm thành công")
                                .data(gradeSheet.getGradeSheetId())
                                .build());
        }

        /**
         * TEA-03: PUT /lecturers/grades/{gradeId}
         * Cập nhật điểm
         */
        @PutMapping("/grades/{gradeId}")
        public ResponseEntity<ApiResponse> updateGrade(
                        @AuthenticationPrincipal UserDetailsImpl principal,
                        @PathVariable Integer gradeId,
                        @Valid @RequestBody GradeRequest request) {
                GradeSheet gradeSheet = gradeService.updateGrade(principal.getId(), gradeId, request);
                return ResponseEntity.ok(ApiResponse.builder()
                                .message("Cập nhật điểm thành công")
                                .data(gradeSheet.getGradeSheetId())
                                .build());
        }
}
