package org.example.qlttngoaingu.Controller;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.example.qlttngoaingu.Dto.Request.CourseCreateRequest;
import org.example.qlttngoaingu.Dto.Response.ApiResponse;
import org.example.qlttngoaingu.Dto.Response.CourseDetailResponse;
import org.example.qlttngoaingu.Dto.Response.CourseResponse;
import org.example.qlttngoaingu.Service.CourseService;
import org.example.qlttngoaingu.entity.Course;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/courses")
@AllArgsConstructor
public class CourseController {

    private CourseService courseService;

    // Get all courses (overview)
    @GetMapping
    public ResponseEntity<ApiResponse> getAllCourses() { // Thay đổi kiểu trả về tường minh
        List<CourseResponse> lstCourses = courseService.getAllCourses();
        return ResponseEntity.ok().body(ApiResponse.builder().data(lstCourses).build());
    }

    // Get course details by ID
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse> getCourseById(@PathVariable Integer id) { // Thay đổi kiểu trả về tường minh
        CourseDetailResponse courseDetailResponse = courseService.getCourseDetailById(id);
        return ResponseEntity.ok().body(ApiResponse.builder().data(courseDetailResponse).build());
    }

    // Create a new course
    @PostMapping
    public ResponseEntity<ApiResponse> createCourse( // Thay đổi kiểu trả về tường minh
                                                     @Valid @RequestBody CourseCreateRequest request) {

        Course createdCourse = courseService.createCourse(request);

        // Nên trả về HTTP 201 Created cho hành động POST/Create
        return ResponseEntity.ok().body(ApiResponse.builder().message("Tạo khóa học thành công").build());
    }
}