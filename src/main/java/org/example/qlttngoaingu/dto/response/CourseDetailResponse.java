package org.example.qlttngoaingu.dto.response;

import java.util.List;

import org.example.qlttngoaingu.entity.Module;
import org.example.qlttngoaingu.entity.Objective;

import lombok.Getter;
import lombok.Setter;
@Getter @Setter
public class CourseDetailResponse {
    private Integer courseId;
    private String courseName;
    private Integer studyHours;
    private Double tuitionFee;
    private Double PromotionPrice;
    private String video;
    private Boolean status;
    private String description;
    private String entryLevel;
    private String targetLevel;
    private String image;
    private Integer courseCategoryId;
    private String category;
    private String level;
    private List<Objective> objectives;
    private List<Module> modules;
    private List<ClassResponse.ClassInfo> classInfos;
    private List<ComboPromotionInfo> comboPromotions;
    
    @Getter @Setter
    public static class ComboPromotionInfo {
        private String comboName;
        private Integer discountPercent;
        private List<String> requiredCourseNames; // Các khóa cần mua kèm
    }
}
