package org.example.qlttngoaingu.controller;

import java.util.List;

import org.example.qlttngoaingu.dto.request.AttendanceSessionRequest;
import org.example.qlttngoaingu.dto.request.CheckConflictRequest;
import org.example.qlttngoaingu.dto.response.ApiResponse;
import org.example.qlttngoaingu.dto.response.AttendanceSessionResponse;
import org.example.qlttngoaingu.dto.response.AvailableLecturerResponse;
import org.example.qlttngoaingu.entity.Lecturer;
import org.example.qlttngoaingu.security.model.UserDetailsImpl;
import org.example.qlttngoaingu.service.AttendanceService;
import org.example.qlttngoaingu.service.LecturerService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/lecturers")
@RequiredArgsConstructor
public class LecturerController {
    private final LecturerService lecturerService;
    private final AttendanceService attendanceService;

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
    public ResponseEntity<ApiResponse> getById(@AuthenticationPrincipal UserDetailsImpl user,@PathVariable Integer id) {
        return ResponseEntity.ok(
                ApiResponse.builder().data(lecturerService.getLecturerById(user.getId(),id)).build()
        );
    }
    @GetMapping("/me")
    public ResponseEntity<ApiResponse> getMyInfo(
            @AuthenticationPrincipal UserDetailsImpl user
    ) {
        return ResponseEntity.ok(
                ApiResponse.builder()
                        .data(lecturerService.getLecturerById(user.getId(), null))
                        .build()
        );
    }


    @PostMapping("/sessions/{sessionId}/attendance")
    public ResponseEntity<ApiResponse<AttendanceSessionResponse>> markAttendance(@AuthenticationPrincipal UserDetailsImpl principal, @PathVariable Integer sessionId, @RequestBody AttendanceSessionRequest request) {
        if (!sessionId.equals(request.getSessionId())) {
            return ResponseEntity.badRequest().body(ApiResponse.<AttendanceSessionResponse>builder().message("sessionId mismatch").build());
        }
        AttendanceSessionResponse resp = attendanceService.markAttendance(principal.getId(), request);
        return ResponseEntity.ok().body(ApiResponse.<AttendanceSessionResponse>builder().data(resp).build());
    }









}
