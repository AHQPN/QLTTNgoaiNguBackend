package org.example.qlttngoaingu.controller;

import lombok.RequiredArgsConstructor;
import org.example.qlttngoaingu.dto.request.CheckConflictRequest;
import org.example.qlttngoaingu.dto.request.RoomRequest;
import org.example.qlttngoaingu.dto.response.ApiResponse;
import org.example.qlttngoaingu.dto.response.AvailableRoomResponse;
import org.example.qlttngoaingu.repository.RoomRepository;
import org.example.qlttngoaingu.service.RoomService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/rooms")
@RequiredArgsConstructor
public class RoomController {
    private final RoomService roomService;

    @PostMapping("available")
    public ResponseEntity<ApiResponse> getAvailableRoom(@RequestBody CheckConflictRequest checkConflictRequest)
    {
        List<AvailableRoomResponse> lst = roomService.getAvailableRooms(
                checkConflictRequest.getSchedulePattern(),
                checkConflictRequest.getStartTime(),
                checkConflictRequest.getDurationMinutes(),
                checkConflictRequest.getStartDate());
        return ResponseEntity.ok().body(ApiResponse.builder().data(lst).build());
    }

    @GetMapping("/room-name")
    public ResponseEntity<ApiResponse> getAllRooms() {
        return ResponseEntity.ok(
                ApiResponse.builder().data(roomService.getAllRooms()).build()
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse> getRoomById(@PathVariable Integer id) {
        return ResponseEntity.ok(
                ApiResponse.builder().data(roomService.getRoomById(id)).build()
        );
    }
    @PostMapping
    public ResponseEntity<ApiResponse> createRoom(@RequestBody RoomRequest request) {
        return ResponseEntity.ok(
                ApiResponse.builder().data(roomService.createRoom(request)).build()
        );
    }

}
