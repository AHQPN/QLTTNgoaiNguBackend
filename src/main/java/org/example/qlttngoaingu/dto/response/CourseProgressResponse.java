package org.example.qlttngoaingu.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response DTO cho Tiến độ khóa học (Course Progress)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CourseProgressResponse {
    
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
     * Số buổi đã hoàn thành
     */
    private Integer completedSessions;
    
    /**
     * Tổng số buổi
     */
    private Integer totalSessions;
    
    /**
     * Tỷ lệ % (completedSessions/totalSessions * 100)
     */
    private Double progressRate;
}
