package org.example.qlttngoaingu.Dto.Response;

import lombok.Getter;
import lombok.Setter;
import org.example.qlttngoaingu.entity.Goal;

import java.util.List;

@Getter @Setter
public class CourseResponse {
    private Integer courseId;
    private String courseName;
    private Double tuitionFee;

}
