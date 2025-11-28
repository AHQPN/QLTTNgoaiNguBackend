package org.example.qlttngoaingu.controller;

import lombok.RequiredArgsConstructor;
import org.example.qlttngoaingu.dto.response.*;
import org.example.qlttngoaingu.entity.Student;
import org.example.qlttngoaingu.security.model.UserDetailsImpl;
import org.example.qlttngoaingu.service.CourseClassService;
import org.example.qlttngoaingu.service.CourseService;
import org.example.qlttngoaingu.service.UserService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("students")
@RequiredArgsConstructor
public class StudentController {
    private final UserService userService;
    private final CourseService courseService;
    private final CourseClassService courseClassService;

    @GetMapping
    public ResponseEntity<ApiResponse> getStudentInfo(@AuthenticationPrincipal UserDetailsImpl principal) {
        return ResponseEntity.ok().body(ApiResponse.builder().data(userService.getStudentInfo(principal.getId())).build());
    }

    @PutMapping
    public ResponseEntity<ApiResponse> updateStudentInfo(@AuthenticationPrincipal UserDetailsImpl principal, @RequestBody StudentInfo student) {
        userService.updateStudentInfo(principal.getId(),student);
        return ResponseEntity.ok().body(ApiResponse.builder().message("Cập nhật thành công").build());
    }
//    @GetMapping("/classes")
//    public ResponseEntity<ApiResponse> getClassesByStudent(@AuthenticationPrincipal UserDetailsImpl principal)
//    {
//        return ResponseEntity.ok().body(ApiResponse.builder().data(courseClassService.findByStudent(principal.getId())).build());
//    }

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

        // Chỉ số phân trang
        int fromIndex = (page - 1) * size;
        int toIndex = Math.min(fromIndex + size, totalItems);

        // Tránh lỗi out-of-bound
        List<ClassResponse.ClassInfo> paginatedList =
                (fromIndex >= totalItems) ?
                        new ArrayList<>() :
                        fullList.subList(fromIndex, toIndex);

        // Build response giống API filter
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




}
