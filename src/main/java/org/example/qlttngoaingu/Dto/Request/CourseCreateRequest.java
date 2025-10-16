package org.example.qlttngoaingu.Dto.Request;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;
import org.example.qlttngoaingu.entity.Goal;
import org.example.qlttngoaingu.entity.Module;
import org.hibernate.validator.constraints.URL;

import java.util.List;
@Getter @Setter
public class CourseCreateRequest {

    @NotBlank(message = "COURSE_NAME_NOT_BLANK")
    @Size(min = 3, message = "COURSE_NAME_TOO_SHORT")
    private String courseName;

    @Min(value = 1, message = "COURSE_STUDY_HOURS_INVALID")
    private Integer studyHours;

    @DecimalMin(value = "0.0", inclusive = false, message = "COURSE_TUITION_FEE_INVALID")
    private Double tuitionFee;

    @Min(value = 1, message = "COURSE_NUMBER_OF_SESSIONS_INVALID")
    private Integer numberOfSessions;

    @URL(message = "COURSE_VIDEO_INVALID")
    private String video;

    @NotEmpty(message = "COURSE_OBJECTIVES_EMPTY")
    private List<Goal> objectives;

    @NotEmpty(message = "COURSE_MODULES_EMPTY")
    private List<Module> modules;
}
