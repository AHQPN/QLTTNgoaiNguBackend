package org.example.qlttngoaingu.dto.response;

import lombok.Data;

@Data
public class AvailableRoomResponse {
    private Integer roomId;
    private String roomName;
    private Integer capacity;
    private String status;
}