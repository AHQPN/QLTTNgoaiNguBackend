package org.example.qlttngoaingu.dto.response;

import lombok.Getter;
import lombok.Setter;
import org.example.qlttngoaingu.entity.Objective;
import org.example.qlttngoaingu.entity.Module;
import java.util.List;
@Getter @Setter
public class CourseDetailResponse {
    private Integer courseId;
    private String courseName;
    private Integer studyHours;
    private Double tuitionFee;
    private String video;
    private Boolean status;
    private String description;
    private String targetLevel;
    private String image;
    private String category;
    private String level;
    private List<Objective> objectives;
    private List<Module> modules;
}
