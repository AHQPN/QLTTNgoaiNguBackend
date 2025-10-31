package org.example.qlttngoaingu.dto.response;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
@Getter @Setter
public class CourseResponse {
    private Integer courseId;
    private String courseName;
    private Double tuitionFee;
    private LocalDateTime createdDate;
    private Boolean isActive;
    private Integer courseCategoryId;

}
