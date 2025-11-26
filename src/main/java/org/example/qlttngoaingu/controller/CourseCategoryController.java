package org.example.qlttngoaingu.controller;

import lombok.RequiredArgsConstructor;
import org.example.qlttngoaingu.dto.request.CourseCategoryRequest;
import org.example.qlttngoaingu.dto.response.ApiResponse;
import org.example.qlttngoaingu.dto.response.CourseCategoryResponse;
import org.example.qlttngoaingu.dto.response.CourseGroupResponse;
import org.example.qlttngoaingu.service.CourseCategoryService;
import org.example.qlttngoaingu.service.CourseCategoryService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/categories")
@RequiredArgsConstructor
public class CourseCategoryController {
    private final CourseCategoryService courseCategoryService;

    @PostMapping
    public ResponseEntity<?> create(@RequestBody CourseCategoryRequest request) {
        CourseCategoryResponse response = courseCategoryService.create(request);
        return ResponseEntity.ok().body(ApiResponse.builder().data(response).build());
    }

    //  2. Update category
    @PutMapping("/{id}")
    public ResponseEntity<?> update(
            @PathVariable Integer id,
            @RequestBody CourseCategoryRequest request) {
        CourseCategoryResponse response = courseCategoryService.update(id, request);
        return ResponseEntity.ok().body(ApiResponse.builder().data(response).build());
    }

    //  3. Get all categories
    @GetMapping
    public ResponseEntity<?> getAll() {
        return ResponseEntity.ok().body(ApiResponse.builder().data(courseCategoryService.getAll()).build());
    }

    //  4. Get detail by ID
    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable Integer id) {
        return ResponseEntity.ok().body(ApiResponse.builder().data(courseCategoryService.getById(id)).build());
    }
}
