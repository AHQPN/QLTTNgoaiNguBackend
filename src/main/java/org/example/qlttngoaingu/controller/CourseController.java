package org.example.qlttngoaingu.controller;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.example.qlttngoaingu.dto.request.CourseCreateRequest;
import org.example.qlttngoaingu.dto.request.CourseUpdateRequest;
import org.example.qlttngoaingu.dto.response.*;
import org.example.qlttngoaingu.service.CourseService;
import org.example.qlttngoaingu.entity.Course;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/courses")
@AllArgsConstructor
public class CourseController {

    private CourseService courseService;

    // Get all courses (overview)
    @GetMapping("/activecourses")
    public ResponseEntity<ApiResponse> getAllActiveCourses() {
        List<ActiveCourseResponse> lstCourses = courseService.getAllActiveCourses();
        return ResponseEntity.ok().body(ApiResponse.builder().data(lstCourses).build());
    }
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getAllCourses(@RequestParam(defaultValue = "0") int page,
                                      @RequestParam(defaultValue = "15") int size)
    {
        CoursePageResponse coursePageResponse = courseService.getAllCourses(page,size);
        return ResponseEntity.ok().body(ApiResponse.builder().data(coursePageResponse).build());
    }


    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse> getCourseById(@PathVariable Integer id) {
        CourseDetailResponse courseDetailResponse = courseService.getCourseDetailById(id);
        return ResponseEntity.ok().body(ApiResponse.builder().data(courseDetailResponse).build());
    }

    // Create a new course
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse> createCourse(
            @Valid @RequestBody CourseCreateRequest request) {
        Course createdCourse = courseService.createCourse(request);
        return ResponseEntity.ok().body(ApiResponse.builder().message("Tạo khóa học thành công").build());
    }
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse> updateCourse(@PathVariable Integer id,@Valid @RequestBody CourseUpdateRequest request)
    {
        Course cs = courseService.updateCourse(id,request);
        return ResponseEntity.ok().body(ApiResponse.builder().message("Hoàn tất chỉnh sửa").build());
    }

    @PostMapping("/status/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse> setCourseStatus(@PathVariable Integer id)
    {
        courseService.changeStatus(id);
        return ResponseEntity.ok().body(ApiResponse.builder().message("Hoàn tất chỉnh sửa").build());
    }

    @GetMapping("/recommedcousres/{id}")
    public ResponseEntity<ApiResponse> recommendCourses(@PathVariable Integer id) {
        List<ActiveCourseResponse> courseResponse = courseService.getRecommendCourses(id);
        return ResponseEntity.ok().body(ApiResponse.builder().data(courseResponse).build());

    }
}