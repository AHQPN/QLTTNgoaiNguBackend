package org.example.qlttngoaingu.controller;

import lombok.RequiredArgsConstructor;
import org.example.qlttngoaingu.dto.request.ClassCreationRequest;
import org.example.qlttngoaingu.dto.response.ApiResponse;
import org.example.qlttngoaingu.dto.response.ClassCreationResponse;
import org.example.qlttngoaingu.entity.CourseClass;
import org.example.qlttngoaingu.exception.AppException;
import org.example.qlttngoaingu.exception.ErrorCode;
import org.example.qlttngoaingu.service.CourseClassService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/courseclasses")
@RequiredArgsConstructor
public class CourseClassController {
    private final CourseClassService courseClassService;

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


    @PutMapping
    public ResponseEntity<ApiResponse> updateCourseClass(@RequestBody ClassCreationRequest classCreationRequest)
    {
        return ResponseEntity.ok().body(ApiResponse.builder().build());
    }





}
