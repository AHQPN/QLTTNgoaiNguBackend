package org.example.qlttngoaingu.controller;

import lombok.RequiredArgsConstructor;
import org.example.qlttngoaingu.dto.request.ClassCreationRequest;
import org.example.qlttngoaingu.dto.response.ApiResponse;
import org.example.qlttngoaingu.dto.response.ClassCreationResponse;
import org.example.qlttngoaingu.dto.response.ClassResponse;
import org.example.qlttngoaingu.dto.response.ScheduleSuggestionResponse;
import org.example.qlttngoaingu.entity.CourseClass;
import org.example.qlttngoaingu.exception.AppException;
import org.example.qlttngoaingu.exception.ErrorCode;
import org.example.qlttngoaingu.service.CourseClassService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

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
    @PostMapping("/{classId}")
    public ResponseEntity<ApiResponse> changeStatusCourseClass(@PathVariable Integer classId){
        ScheduleSuggestionResponse scheduleSuggestionResponse = courseClassService.changeStatus(classId);

        ApiResponse apiResponse = (scheduleSuggestionResponse == null)
                ? ApiResponse.builder().message("Course Class status has been changed successfully.").build()
                : ApiResponse.builder().data(scheduleSuggestionResponse).message("Schedule suggestions available.").build();

        return ResponseEntity.ok().body(apiResponse);
    }

    @GetMapping("/filter")
    public ClassResponse filter(
            @RequestParam(required = false) Integer lecturerId,
            @RequestParam(required = false) Integer roomId,
            @RequestParam(required = false) Integer courseId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        List<ClassResponse.ClassInfo> filteredList =
                courseClassService.filterClasses(lecturerId, roomId, courseId);

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



    @PutMapping
    public ResponseEntity<ApiResponse> updateCourseClass(@RequestBody ClassCreationRequest classCreationRequest)
    {
        return ResponseEntity.ok().body(ApiResponse.builder().build());
    }






}
