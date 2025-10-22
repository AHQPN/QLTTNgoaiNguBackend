package org.example.qlttngoaingu.dto.response;

import lombok.Getter;
import lombok.Setter;

import java.util.List;
@Getter @Setter
public class CoursePageResponse {
    private List<CourseResponse> courses;
    private int currentPage;
    private long totalItems;
    private int totalPages;

    public CoursePageResponse(List<CourseResponse> courses, int currentPage, long totalItems, int totalPages) {
        this.courses = courses;
        this.currentPage = currentPage;
        this.totalItems = totalItems;
        this.totalPages = totalPages;
    }
}
