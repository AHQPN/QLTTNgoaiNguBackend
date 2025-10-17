package org.example.qlttngoaingu.Controller;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.example.qlttngoaingu.Dto.Request.CourseCreateRequest;
import org.example.qlttngoaingu.Dto.Response.ApiResponse;
import org.example.qlttngoaingu.Dto.Response.CourseDetailResponse;
import org.example.qlttngoaingu.Dto.Response.ActiveCourseResponse;
import org.example.qlttngoaingu.Dto.Response.CoursePageResponse;
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
    @GetMapping("/activecourses")
    public ResponseEntity<ApiResponse> getAllActiveCourses() {
        List<ActiveCourseResponse> lstCourses = courseService.getAllActiveCourses();
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
    public ResponseEntity<ApiResponse> createCourse(
                                                     @Valid @RequestBody CourseCreateRequest request) {

        Course createdCourse = courseService.createCourse(request);

        // Nên trả về HTTP 201 Created cho hành động POST/Create
        return ResponseEntity.ok().body(ApiResponse.builder().message("Tạo khóa học thành công").build());
    }
}