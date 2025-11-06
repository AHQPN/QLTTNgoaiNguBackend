package org.example.qlttngoaingu.controller;

import org.example.qlttngoaingu.dto.request.CheckConflictRequest;
import org.example.qlttngoaingu.dto.response.ApiResponse;
import org.example.qlttngoaingu.dto.response.AvailableRoomResponse;
import org.example.qlttngoaingu.repository.RoomRepository;
import org.example.qlttngoaingu.service.RoomService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/rooms")
public class RoomController {
    private RoomService roomService;

    @GetMapping
    public ResponseEntity<ApiResponse> getAvailableRoom(@RequestBody CheckConflictRequest checkConflictRequest)
    {
        List<AvailableRoomResponse> lst = roomService.getAvailableRooms(
                checkConflictRequest.getSchedulePattern(),
                checkConflictRequest.getStartTime(),
                checkConflictRequest.getDurationMinutes(),
                checkConflictRequest.getStartDate());
        return ResponseEntity.ok().body(ApiResponse.builder().data(lst).build());
    }
}
