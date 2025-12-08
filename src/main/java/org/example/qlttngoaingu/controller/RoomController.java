package org.example.qlttngoaingu.controller;

import java.util.List;

import jakarta.validation.Valid;
import org.example.qlttngoaingu.dto.request.CheckConflictRequest;
import org.example.qlttngoaingu.dto.request.RoomRequest;
import org.example.qlttngoaingu.dto.request.RoomUpdateRequest;
import org.example.qlttngoaingu.dto.response.ApiResponse;
import org.example.qlttngoaingu.dto.response.AvailableRoomResponse;
import org.example.qlttngoaingu.dto.response.RoomResponse;
import org.example.qlttngoaingu.service.RoomService;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/rooms")
@RequiredArgsConstructor
public class RoomController {
    private final RoomService roomService;

    // ==================== CRUD APIs với Phân trang ====================

    @GetMapping
    public ResponseEntity<ApiResponse<Page<RoomResponse>>> getAllRoomsPaginated(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "roomId") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDirection
    ) {
        Page<RoomResponse> rooms = roomService.getAllRoomsPaginated(page, size, sortBy, sortDirection);
        return ResponseEntity.ok(ApiResponse.<Page<RoomResponse>>builder()
                .message("Lấy danh sách phòng thành công")
                .data(rooms)
                .build());
    }

    @GetMapping("/detail/{id}")
    public ResponseEntity<ApiResponse<RoomResponse>> getRoomDetail(@PathVariable Integer id) {
        RoomResponse room = roomService.getRoomByIdDetail(id);
        return ResponseEntity.ok(ApiResponse.<RoomResponse>builder()
                .message("Lấy thông tin phòng thành công")
                .data(room)
                .build());
    }

    @PostMapping("/new")
    public ResponseEntity<ApiResponse<RoomResponse>> createNewRoom(
            @Valid @RequestBody RoomRequest request
    ) {
        RoomResponse newRoom = roomService.createRoomFull(request);
        return ResponseEntity.ok(ApiResponse.<RoomResponse>builder()
                .message("Tạo phòng thành công")
                .data(newRoom)
                .build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<RoomResponse>> updateRoom(
            @PathVariable Integer id,
            @Valid @RequestBody RoomUpdateRequest request
    ) {
        RoomResponse updatedRoom = roomService.updateRoomFull(id, request);
        return ResponseEntity.ok(ApiResponse.<RoomResponse>builder()
                .message("Cập nhật phòng thành công")
                .data(updatedRoom)
                .build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteRoom(@PathVariable Integer id) {
        roomService.deleteRoomFull(id);
        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .message("Xóa phòng thành công")
                .build());
    }

    // ==================== Các API khác ====================


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
