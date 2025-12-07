package org.example.qlttngoaingu.controller;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.example.qlttngoaingu.dto.request.ReviewRequest;
import org.example.qlttngoaingu.dto.response.ApiResponse;
import org.example.qlttngoaingu.dto.response.ClassResponse;
import org.example.qlttngoaingu.dto.response.GradeResponse;
import org.example.qlttngoaingu.dto.response.ReviewResponse;
import org.example.qlttngoaingu.dto.response.StudentDocumentResponse;
import org.example.qlttngoaingu.dto.response.StudentInfo;
import org.example.qlttngoaingu.dto.response.WeeklyScheduleResponse;
import org.example.qlttngoaingu.security.model.UserDetailsImpl;
import org.example.qlttngoaingu.service.CourseClassService;
import org.example.qlttngoaingu.service.CourseService;
import org.example.qlttngoaingu.service.DocumentService;
import org.example.qlttngoaingu.service.GradeService;
import org.example.qlttngoaingu.service.ReviewService;
import org.example.qlttngoaingu.service.UserService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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
@RequestMapping("students")
@RequiredArgsConstructor
public class StudentController {
    private final UserService userService;
    private final CourseService courseService;
    private final CourseClassService courseClassService;
    private final GradeService gradeService;
    private final ReviewService reviewService;
    private final DocumentService documentService;

    @GetMapping
    public ResponseEntity<ApiResponse> getStudentInfo(@AuthenticationPrincipal UserDetailsImpl principal) {
        return ResponseEntity.ok().body(ApiResponse.builder().data(userService.getStudentInfo(principal.getId())).build());
    }

    @PutMapping
    public ResponseEntity<ApiResponse> updateStudentInfo(@AuthenticationPrincipal UserDetailsImpl principal, @RequestBody StudentInfo student) {
        userService.updateStudentInfo(principal.getId(),student);
        return ResponseEntity.ok().body(ApiResponse.builder().message("Cập nhật thành công").build());
    }

    @GetMapping("/schedule-by-week")
    public ResponseEntity<ApiResponse> getScheduleByWeekforStudent(@AuthenticationPrincipal UserDetailsImpl principal, @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        WeeklyScheduleResponse response =  courseClassService.getWeeklyScheduleByUser(principal.getId(),date);
        return  ResponseEntity.ok().body(ApiResponse.builder().data(response).build());
    }

    @GetMapping("/get-classes-enrolled")
    public ResponseEntity<ApiResponse> getClassesEnrolled(
            @AuthenticationPrincipal UserDetailsImpl principal,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size
    ) {

        List<ClassResponse.ClassInfo> fullList =
                courseClassService.getClassByUser(principal.getId());

        int totalItems = fullList.size();
        int totalPages = (int) Math.ceil((double) totalItems / size);

        int fromIndex = (page - 1) * size;
        int toIndex = Math.min(fromIndex + size, totalItems);

        List<ClassResponse.ClassInfo> paginatedList =
                (fromIndex >= totalItems) ?
                        new ArrayList<>() :
                        fullList.subList(fromIndex, toIndex);

        ClassResponse response = new ClassResponse();
        response.setCurrentPage(page);
        response.setTotalPages(totalPages);
        response.setTotalItems(totalItems);
        response.setClasses(paginatedList);

        return ResponseEntity.ok(
                ApiResponse.builder()
                        .data(response)
                        .build()
        );
    }

    // Alias endpoint cho backward compatibility với FE cũ
    @GetMapping("/get-courses_enrolled")
    public ResponseEntity<ApiResponse> getCoursesEnrolled(
            @AuthenticationPrincipal UserDetailsImpl principal,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return getClassesEnrolled(principal, page, size);
    }

    // ==================== GRADES APIs (STU-01, STU-02) ====================

    /**
     * STU-01: GET /students/grades
     * Lấy tất cả điểm của học viên đang đăng nhập
     */
    @GetMapping("/grades")
    public ResponseEntity<ApiResponse> getMyGrades(
            @AuthenticationPrincipal UserDetailsImpl principal
    ) {
        List<GradeResponse> grades = gradeService.getStudentGrades(principal.getId());
        return ResponseEntity.ok(ApiResponse.builder()
                .message("Lấy điểm thành công")
                .data(grades)
                .build());
    }

    /**
     * STU-02: GET /students/grades/class/{classId}
     * Lấy điểm của học viên theo lớp cụ thể
     */
    @GetMapping("/grades/class/{classId}")
    public ResponseEntity<ApiResponse> getGradesByClass(
            @AuthenticationPrincipal UserDetailsImpl principal,
            @PathVariable Integer classId
    ) {
        GradeResponse grades = gradeService.getStudentGradesByClass(principal.getId(), classId);
        return ResponseEntity.ok(ApiResponse.builder()
                .message("Lấy điểm lớp thành công")
                .data(grades)
                .build());
    }

    // ==================== REVIEWS APIs (STU-03, STU-04) ====================

    /**
     * STU-03: POST /students/reviews
     * Học viên gửi đánh giá khóa học
     */
    @PostMapping("/reviews")
    public ResponseEntity<ApiResponse> submitReview(
            @AuthenticationPrincipal UserDetailsImpl principal,
            @Valid @RequestBody ReviewRequest request
    ) {
        ReviewResponse review = reviewService.submitReview(principal.getId(), request);
        return ResponseEntity.ok(ApiResponse.builder()
                .message("Đánh giá đã được gửi thành công")
                .data(review)
                .build());
    }

    /**
     * STU-04: GET /students/reviews
     * Học viên xem lịch sử đánh giá của mình
     */
    @GetMapping("/reviews")
    public ResponseEntity<ApiResponse> getMyReviews(
            @AuthenticationPrincipal UserDetailsImpl principal
    ) {
        List<ReviewResponse> reviews = reviewService.getStudentReviews(principal.getId());
        return ResponseEntity.ok(ApiResponse.builder()
                .message("Lấy lịch sử đánh giá thành công")
                .data(reviews)
                .build());
    }

    // ==================== DOCUMENTS API (STU-05) ====================

    /**
     * STU-05: GET /students/documents
     * Lấy tài liệu của các khóa học mà học viên tham gia, group by khóa học
     */
    @GetMapping("/documents")
    public ResponseEntity<ApiResponse> getMyDocuments(
            @AuthenticationPrincipal UserDetailsImpl principal
    ) {
        List<StudentDocumentResponse> documents = documentService.getStudentDocuments(principal.getId());
        return ResponseEntity.ok(ApiResponse.builder()
                .message("Lấy tài liệu thành công")
                .data(documents)
                .build());
    }
}
