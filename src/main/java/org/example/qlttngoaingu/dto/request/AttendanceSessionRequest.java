package org.example.qlttngoaingu.dto.request;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AttendanceSessionRequest {
    private Integer sessionId;
    private List<AttendanceEntryRequest> entries;
}
