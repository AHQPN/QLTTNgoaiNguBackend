package org.example.qlttngoaingu.controller;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.example.qlttngoaingu.dto.request.ClassCreationRequest;
import org.example.qlttngoaingu.dto.request.SessionCreateRequest;
import org.example.qlttngoaingu.dto.request.SessionUpdateRequest;
import org.example.qlttngoaingu.dto.response.ApiResponse;
import org.example.qlttngoaingu.dto.response.AttendanceSessionResponse;
import org.example.qlttngoaingu.dto.response.ClassCreationResponse;
import org.example.qlttngoaingu.dto.response.ClassResponse;
import org.example.qlttngoaingu.dto.response.WeeklyScheduleResponse;
import org.example.qlttngoaingu.security.model.UserDetailsImpl;
import org.example.qlttngoaingu.service.AttendanceService;
import org.example.qlttngoaingu.service.CourseClassService;
import org.example.qlttngoaingu.service.ReviewService;
import org.springframework.format.annotation.DateTimeFormat;
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

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/courseclasses")
@RequiredArgsConstructor
public class CourseClassController {
    private final CourseClassService courseClassService;
    private final AttendanceService attendanceService;
    private final ReviewService reviewService;
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse> getCourseClass(@PathVariable Integer id) {

        return ResponseEntity.ok().body(ApiResponse.builder().data(courseClassService.getClass(id)).build());
    }
    @GetMapping
    public ResponseEntity<ApiResponse> getALlCourseClasses(@RequestParam(defaultValue = "0") int page,
                                                           @RequestParam(defaultValue = "10") int size) {

        return ResponseEntity.ok().body(ApiResponse.builder().data(courseClassService.getAllClasses(page,size)).build());
    }


    @PostMapping()
    public ResponseEntity<ApiResponse> createCourseClass(@RequestBody ClassCreationRequest classCreationRequest)
    {
        ClassCreationResponse classCreationResponse = courseClassService.createClass(classCreationRequest);
        return ResponseEntity.ok().body(ApiResponse.builder()
                .message("Course Class has been created")
                .data(classCreationResponse).build());
    }
//    @PostMapping("/{classId}")
//    public ResponseEntity<ApiResponse> changeStatusCourseClass(@PathVariable Integer classId){
//        ScheduleSuggestionResponse scheduleSuggestionResponse = courseClassService.changeStatus(classId);
//
//        ApiResponse apiResponse = (scheduleSuggestionResponse == null)
//                ? ApiResponse.builder().message("Course Class status has been changed successfully.").build()
//                : ApiResponse.builder().data(scheduleSuggestionResponse).message("Schedule suggestions available.").build();
//
//        return ResponseEntity.ok().body(apiResponse);
//    }

    @GetMapping("/filter")
    public ClassResponse filter(
            @RequestParam(required = false) Integer lecturerId,
            @RequestParam(required = false) Integer roomId,
            @RequestParam(required = false) Integer courseId,
            @RequestParam(required = false) String className,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        List<ClassResponse.ClassInfo> filteredList =
                courseClassService.filterClasses(lecturerId, roomId, courseId, className);

        int totalItems = filteredList.size();
        int totalPages = (int) Math.ceil((double) totalItems / size);

        // index phân trang
        int fromIndex = (page - 1) * size;
        int toIndex = Math.min(fromIndex + size, totalItems);

        // tránh lỗi out of bound
        List<ClassResponse.ClassInfo> paginatedList =
                (fromIndex >= totalItems) ?
                        new ArrayList<>() :
                        filteredList.subList(fromIndex, toIndex);

        // build response
        ClassResponse res = new ClassResponse();
        res.setCurrentPage(page);
        res.setTotalPages(totalPages);
        res.setTotalItems(totalItems);
        res.setClasses(paginatedList);

        return res;
    }

    @GetMapping("/schedule-by-week")
    public ResponseEntity<ApiResponse> getWeeklySchedule(
            @RequestParam(required = false) Integer lecturerId,
            @RequestParam(required = false) Integer roomId,
            @RequestParam(required = false) Integer courseId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        WeeklyScheduleResponse weeklyScheduleResponse =courseClassService.getWeeklySchedule(lecturerId, roomId, courseId, date);

        return ResponseEntity.ok().body(ApiResponse.builder().data(weeklyScheduleResponse).build());
    }





    @PutMapping("/{classId}")
    public ResponseEntity<ApiResponse> updateCourseClass(@RequestBody ClassCreationRequest classCreationRequest,@PathVariable Integer classId)
    {

        ClassCreationResponse response =  courseClassService.updateClass(classId,classCreationRequest);
        return ResponseEntity.ok().body(ApiResponse.builder().data(response).build());
    }

    @GetMapping("/sessions/{sessionId}/attendance")
    public ResponseEntity<ApiResponse<AttendanceSessionResponse>> getAttendance(@AuthenticationPrincipal UserDetailsImpl principal, @PathVariable Integer sessionId) {
        AttendanceSessionResponse resp = attendanceService.getAttendanceForSession(principal.getId(), sessionId);
        return ResponseEntity.ok().body(ApiResponse.<AttendanceSessionResponse>builder().data(resp).build());
    }

    /**
     * PUT /courseclasses/sessions/{sessionId}
     * Cập nhật trạng thái và ghi chú của buổi học
     */
    @PutMapping("/sessions/{sessionId}")
    public ResponseEntity<ApiResponse> updateSession(
            @PathVariable Integer sessionId,
            @RequestBody SessionUpdateRequest request) {
        var sessionInfo = courseClassService.updateSession(sessionId, request);
        return ResponseEntity.ok().body(
            ApiResponse.builder()
                .message("Cập nhật buổi học thành công")
                .data(sessionInfo)
                .build()
        );
    }

    /**
     * DELETE /courseclasses/sessions/{sessionId}/cancel
     * Hủy buổi học (đổi status thành "Đã hủy")
     */
    @DeleteMapping("/sessions/{sessionId}/cancel")
    public ResponseEntity<ApiResponse> cancelSession(@PathVariable Integer sessionId) {
        var sessionInfo = courseClassService.cancelSession(sessionId);
        return ResponseEntity.ok().body(
            ApiResponse.builder()
                .message("Hủy buổi học thành công")
                .data(sessionInfo)
                .build()
        );
    }

    /**
     * POST /courseclasses/{classId}/sessions
     * Thêm buổi học mới vào lớp
     * Chỉ được thêm số buổi bằng hoặc ít hơn số buổi đã hủy
     */
    @PostMapping("/{classId}/sessions")
    public ResponseEntity<ApiResponse> addSession(
            @PathVariable Integer classId,
            @RequestBody SessionCreateRequest request) {
        var sessionInfo = courseClassService.addSession(classId, request);
        return ResponseEntity.ok().body(
            ApiResponse.builder()
                .message("Thêm buổi học thành công")
                .data(sessionInfo)
                .build()
        );
    }

    /**
     * GET /courseclasses/{classId}/sessions/suggest-dates
     * Gợi ý các ngày phù hợp để thêm buổi học bù (không trùng lịch)
     */
    @GetMapping("/{classId}/sessions/suggest-dates")
    public ResponseEntity<ApiResponse> suggestMakeupDates(
            @PathVariable Integer classId,
            @RequestParam(defaultValue = "7") Integer daysAhead) {
        var suggestions = courseClassService.suggestMakeupDates(classId, daysAhead);
        return ResponseEntity.ok().body(
            ApiResponse.builder()
                .message("Gợi ý ngày học bù")
                .data(suggestions)
                .build()
        );
    }

    /**
     * GET /courseclasses/{classId}/reviews
     * Lấy danh sách đánh giá của lớp học
     */
    @GetMapping("/{classId}/reviews")
    public ResponseEntity<ApiResponse> getClassReviews(@PathVariable Integer classId) {
        return ResponseEntity.ok().body(
            ApiResponse.builder()
                .data(reviewService.getClassReviews(classId))
                .build()
        );
    }











}
