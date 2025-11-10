package org.example.qlttngoaingu.controller;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.example.qlttngoaingu.dto.response.ApiResponse;
import org.example.qlttngoaingu.dto.response.SkillResponse;
import org.example.qlttngoaingu.entity.Skill;
import org.example.qlttngoaingu.repository.SkillRepository;
import org.example.qlttngoaingu.service.CourseService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RequestMapping("/skills")
@RestController
@RequiredArgsConstructor
public class SkillController {
    private final SkillRepository skillRepository;
    private final CourseService courseService;
    @GetMapping
    public ResponseEntity<ApiResponse> getAllSkills() {
        List<SkillResponse> skillResponses = courseService.getSkills();
        return ResponseEntity.ok().body(ApiResponse.builder().data(skillResponses).build());
    }
}
