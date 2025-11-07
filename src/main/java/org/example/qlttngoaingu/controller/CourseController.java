package org.example.qlttngoaingu.controller;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.example.qlttngoaingu.dto.request.CourseCreateRequest;
import org.example.qlttngoaingu.dto.request.CourseUpdateRequest;
import org.example.qlttngoaingu.dto.request.ObjectiveRequest;
import org.example.qlttngoaingu.dto.response.*;
import org.example.qlttngoaingu.entity.Objective;
import org.example.qlttngoaingu.service.CourseService;
import org.example.qlttngoaingu.entity.Course;
import org.example.qlttngoaingu.service.ObjectiveService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/courses")
@AllArgsConstructor
public class CourseController {

    private CourseService courseService;
    private ObjectiveService objectiveService;
    // Get all courses (overview)
    @GetMapping("/activecourses")
    public ResponseEntity<ApiResponse> getAllActiveCourses() {
        List<CourseGroupResponse> lstCourses = courseService.getCoursesGroupedResponse();
        return ResponseEntity.ok().body(ApiResponse.builder().data(lstCourses).build());
    }
    @GetMapping("/activecourses-name")
    public ResponseEntity<ApiResponse> getAllActiveCoursesName() {
        List<ActiveCourseNameResponse> lstCourses = courseService.getAllActiveCourseNames();
        return ResponseEntity.ok().body(ApiResponse.builder().data(lstCourses).build());
    }
    @GetMapping
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
//    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse> createCourse(
            @Valid @RequestBody CourseCreateRequest request) {
        Course createdCourse = courseService.createCourse(request);
        return ResponseEntity.ok().body(ApiResponse.builder().message("Tạo khóa học thành công").build());
    }
    @PutMapping("/{id}")
//    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse> updateCourse(@PathVariable Integer id,@Valid @RequestBody CourseUpdateRequest request)
    {
        Course cs = courseService.updateCourse(id,request);
        return ResponseEntity.ok().body(ApiResponse.builder().message("Hoàn tất chỉnh sửa").build());
    }

    @PostMapping("/status/{id}")
//    @PreAuthorize("hasRole('ADMIN')")
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

    @PostMapping("/{courseId}/objectives")
    public ResponseEntity<ApiResponse> addObjective(
            @PathVariable Integer courseId,
            @RequestBody @Valid ObjectiveRequest request) {

        Objective objective = objectiveService.addObjective(request,courseId);
        return ResponseEntity.ok(ApiResponse.builder()
                .message("Objective added successfully")
                .data(objective)
                .build());
    }

    // ----------------- Cập nhật mục tiêu -----------------
    @PutMapping("/objectives/{objectiveId}")
    public ResponseEntity<ApiResponse> updateObjective(
            @PathVariable Integer objectiveId,
            @RequestBody @Valid ObjectiveRequest request) {

        objectiveService.updateObjective(objectiveId, request);
        return ResponseEntity.ok(ApiResponse.builder()
                .message("Objective updated successfully")
                .build());
    }

    // ----------------- Xóa mục tiêu -----------------
    @DeleteMapping("/objectives/{objectiveId}")
    public ResponseEntity<ApiResponse> deleteObjective(
            @PathVariable Integer objectiveId) {

        objectiveService.deleteObjective(objectiveId);
        return ResponseEntity.ok(ApiResponse.builder()
                .message("Objective deleted successfully")
                .build());
    }
}