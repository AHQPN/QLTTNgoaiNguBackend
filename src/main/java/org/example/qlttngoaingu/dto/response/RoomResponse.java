package org.example.qlttngoaingu.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoomResponse {
    private Integer roomId;
    private String roomName;
    private Integer capacity;
    private String status;
    private Integer totalClasses;
    private Integer activeClasses;
}
