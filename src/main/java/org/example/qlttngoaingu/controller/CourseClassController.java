package org.example.qlttngoaingu.controller;

import lombok.RequiredArgsConstructor;
import org.example.qlttngoaingu.dto.request.ClassCreationRequest;
import org.example.qlttngoaingu.dto.response.*;
import org.example.qlttngoaingu.entity.CourseClass;
import org.example.qlttngoaingu.exception.AppException;
import org.example.qlttngoaingu.exception.ErrorCode;
import org.example.qlttngoaingu.repository.CourseClassRepository;
import org.example.qlttngoaingu.security.model.UserDetailsImpl;
import org.example.qlttngoaingu.service.AttendanceService;
import org.example.qlttngoaingu.service.CourseClassService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.*;

@RestController
@RequestMapping("/courseclasses")
@RequiredArgsConstructor
public class CourseClassController {
    private final CourseClassService courseClassService;
    private final AttendanceService attendanceService;
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










}
