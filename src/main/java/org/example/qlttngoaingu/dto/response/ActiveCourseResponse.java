package org.example.qlttngoaingu.dto.response;

import lombok.Getter;
import lombok.Setter;
import org.example.qlttngoaingu.dto.request.ObjectiveRequest;
import org.example.qlttngoaingu.entity.Objective;
import org.example.qlttngoaingu.utils.CustomSchedulePattern;

import java.util.List;

@Getter @Setter
public class ActiveCourseResponse {
    private Integer courseId;
    private String courseName;
    private Double tuitionFee;
    private String entryLevel;
    private String targetLevel;
    private String description;
    private Integer studyHours;
    private ClassScheduleResponse classScheduleResponse;
    private String image;
    private List<Objective> objectives;


}
