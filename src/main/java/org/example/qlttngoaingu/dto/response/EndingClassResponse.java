package org.example.qlttngoaingu.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response DTO cho Lớp học sắp kết thúc (Ending Classes)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EndingClassResponse {
    
    /**
     * ID lớp học
     */
    private Integer classId;
    
    /**
     * Tên lớp (mã lớp)
     */
    private String className;
    
    /**
     * Tên khóa học
     */
    private String courseName;
    
    /**
     * Số buổi còn lại
     */
    private Integer remainingSessions;
    
    /**
     * Ngày dự kiến kết thúc (YYYY-MM-DD)
     */
    private String endDate;
}
