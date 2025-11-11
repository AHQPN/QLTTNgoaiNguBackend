package org.example.qlttngoaingu.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class CourseGroupResponse {
    private Integer categoryId;
    private String categoryName;
    private String categoryLevel;
    private String categoryDescription;
    private List<ActiveCourseResponse> courses;

    public CourseGroupResponse() {

    }
}