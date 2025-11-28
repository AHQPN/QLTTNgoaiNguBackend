package org.example.qlttngoaingu.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AttendanceEntryResponse {
    private Integer studentId;
    private String studentName;
    private Boolean absent;
    private String note;
}
