package org.example.qlttngoaingu.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;
import org.example.qlttngoaingu.entity.CourseCategory;
import org.hibernate.validator.constraints.URL;

import java.util.List;
@Getter @Setter
public class CourseCreateRequest {

    @NotBlank(message = "COURSE_NAME_NOT_BLANK")
    @Size(min = 3, message = "COURSE_NAME_TOO_SHORT")
    private String courseName;

    @DecimalMin(value = "0.0", inclusive = false, message = "COURSE_TUITION_FEE_INVALID")
    private Double tuitionFee;

    @URL(message = "COURSE_VIDEO_INVALID")
    private String video;

    @NotBlank(message = "FIELD_NOT_BLANK")
    private String description;

    @NotNull
    private Integer studyHours;

    @NotNull
    private Integer courseCategoryId;

    @NotBlank(message = "FIELD_NOT_BLANK")
    private String entryLevel;

    @NotBlank(message = "FIELD_NOT_BLANK")
    private String targetLevel;

    private String image;

    @NotEmpty(message = "COURSE_OBJECTIVES_EMPTY")
    @Valid
    private List<ObjectiveRequest> objectives;

    @NotEmpty(message = "COURSE_MODULES_EMPTY")
    @Valid
    private List<ModuleRequest> modules;

    @NotEmpty(message = "COURSE_SKILLS_EMPTY")
    private List<Integer> skillIds;
}
