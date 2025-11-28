package org.example.qlttngoaingu.dto.response;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AttendanceSessionResponse {
    private Integer sessionId;
    private List<AttendanceEntryResponse> entries;
}
