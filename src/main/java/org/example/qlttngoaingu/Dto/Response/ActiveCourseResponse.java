package org.example.qlttngoaingu.Dto.Response;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class ActiveCourseResponse {
    private Integer courseId;
    private String courseName;
    private Double tuitionFee;
    private String entryLevel;
    private String targetLevel;
    private String description;
    private String image;

}
