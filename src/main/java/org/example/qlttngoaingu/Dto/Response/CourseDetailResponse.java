package org.example.qlttngoaingu.Dto.Response;

import lombok.Getter;
import lombok.Setter;
import org.example.qlttngoaingu.entity.Goal;
import org.example.qlttngoaingu.entity.Module;
import java.util.List;
@Getter @Setter
public class CourseDetailResponse {
    private Integer courseId;
    private String courseName;
    private Integer studyHours;
    private Double tuitionFee;
    private Integer numberOfSessions;
    private String video;
    private List<Goal> objectives;
    private List<Module> modules;
}
