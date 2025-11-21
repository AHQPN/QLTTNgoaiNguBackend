package org.example.qlttngoaingu.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.hibernate.validator.constraints.URL;

import java.util.List;

@Data
public class CourseUpdateRequest {

    @Size(min = 3, message = "COURSE_NAME_TOO_SHORT")
    private String courseName;

    @DecimalMin(value = "0.0", inclusive = false, message = "COURSE_TUITION_FEE_INVALID")
    private Double tuitionFee;

    private Integer categoryId;

    @URL(message = "COURSE_VIDEO_INVALID")
    private String video;

    private String description;

    private String entryLevel;

    private String targetLevel;

    private String image;

    private Integer studyHours;


    private List<Integer> skillIdsToAdd;
    private List<Integer> skillIdsToRemove;
}