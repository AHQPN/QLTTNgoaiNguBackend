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
    
    // Thống kê điểm danh
    private Integer totalStudents;   // Tổng số học viên
    private Integer presentCount;    // Số học viên có mặt
    private Integer absentCount;     // Số học viên vắng
}
