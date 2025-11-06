package org.example.qlttngoaingu.controller;

import lombok.RequiredArgsConstructor;
import org.example.qlttngoaingu.dto.request.CheckConflictRequest;
import org.example.qlttngoaingu.dto.response.ApiResponse;
import org.example.qlttngoaingu.dto.response.AvailableLecturerResponse;
import org.example.qlttngoaingu.service.LecturerService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/lecturers")
@RequiredArgsConstructor
public class LecturerController {
    private final LecturerService lecturerService;

    @PostMapping("/available")
    public ResponseEntity<List<AvailableLecturerResponse>> getAvailableLecturers(
            @RequestBody CheckConflictRequest request
    ) {
        List<AvailableLecturerResponse> lecturers = lecturerService.getAvailableLecturers(
                request.getSchedulePattern(),
                request.getStartTime(),
                request.getDurationMinutes(),
                request.getStartDate()
        );

        return ResponseEntity.ok(lecturers);
    }

    @GetMapping("lecturer-name")
    public ResponseEntity<ApiResponse> getAll() {
        return ResponseEntity.ok(
                ApiResponse.builder().data(lecturerService.getAllLecturers()).build()
        );
    }


    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse> getById(@PathVariable Integer id) {
        return ResponseEntity.ok(
                ApiResponse.builder().data(lecturerService.getLecturerById(id)).build()
        );
    }



}
