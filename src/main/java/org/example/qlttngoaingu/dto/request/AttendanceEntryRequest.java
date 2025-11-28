package org.example.qlttngoaingu.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AttendanceEntryRequest {
    private Integer studentId;
    private Boolean absent;
    private String note;
}
