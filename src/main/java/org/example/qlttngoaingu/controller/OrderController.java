package org.example.qlttngoaingu.controller;


import lombok.RequiredArgsConstructor;
import org.example.qlttngoaingu.dto.request.CourseRegistrationRequest;
import org.example.qlttngoaingu.dto.response.ApiResponse;
import org.example.qlttngoaingu.service.CourseRegistrationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrderController {
    private final CourseRegistrationService  courseRegistrationService;

//    @PostMapping
//    public ResponseEntity<ApiResponse> RegisterClass(@RequestBody  CourseRegistrationRequest courseRegistrationRequest) {
//
//        return ResponseEntity.ok().body(ApiResponse.builder().data(courseRegistrationService.registerCourses(courseRegistrationRequest)).build());
//    }


}
