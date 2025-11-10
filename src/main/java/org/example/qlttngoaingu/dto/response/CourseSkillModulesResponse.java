package org.example.qlttngoaingu.dto.response;
import lombok.Data;
import org.example.qlttngoaingu.dto.request.ModuleRequest;

import java.util.List;

@Data
public class CourseSkillModulesResponse {
    private Integer courseSkillId;
    private String skillName;
    private List<ModuleRequest> modules;
}