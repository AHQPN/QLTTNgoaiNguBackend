package org.example.qlttngoaingu.service;

import lombok.AllArgsConstructor;
import org.example.qlttngoaingu.dto.request.CourseCreateRequest;
import org.example.qlttngoaingu.dto.request.CourseUpdateRequest;
import org.example.qlttngoaingu.dto.request.ModuleRequest;
import org.example.qlttngoaingu.dto.response.*;
import org.example.qlttngoaingu.entity.*;
import org.example.qlttngoaingu.entity.Module;
import org.example.qlttngoaingu.repository.*;

import org.example.qlttngoaingu.exception.AppException;
import org.example.qlttngoaingu.exception.ErrorCode;
import org.example.qlttngoaingu.mapper.CourseMapper;
import org.example.qlttngoaingu.service.enums.ClassStatusEnum;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class CourseService {


    private final CourseRepository courseRepository;

    private final ModuleService moduleService;
    private final CourseCategoryRepository categoryRepository;
    private final CourseMapper courseMapper;
    private final CourseSkillRepository courseSkillRepository;
    private final SkillRepository skillRepository;
    private final CourseClassService courseClassService;
    private final CourseClassRepository courseClassRepository;
    private final SessionRepository sessionRepository;
    private final PromotionDetailRepository  promotionDetailRepository;
    private final PromotionRepository promotionRepository;

    public List<CourseGroupResponse> getCoursesGroupedResponse() {
        return courseRepository.findByStatusTrue()
                .stream()
                .collect(Collectors.groupingBy(course -> course.getCourseCategory().getId()))
                .values()
                .stream()
                .map(courses -> {
                    Course firstCourse = courses.get(0);
                    CourseCategory category = firstCourse.getCourseCategory();

                    // map từng course sang response
                    List<ActiveCourseResponse> courseResponses = courses
                            .stream()
                            .map(course -> {
                                List<Promotion> validPromotions = promotionRepository.findValidPromotionsByCourseAndType1(
                                        course.getCourseId(),
                                        LocalDate.now()
                                );



                                ActiveCourseResponse response = courseMapper.toActiveResponse(course);
                                response.setObjectives(course.getObjectives());
                                if (!validPromotions.isEmpty()) {
                                    Promotion bestPromotion = validPromotions.get(0);
                                    Double promotionPrice =  course.getTuitionFee() / bestPromotion.getDiscountPercent();
                                    response.setPromotionPrice(promotionPrice);
                                }
                                ClassScheduleResponse classScheduleResponse = courseClassService.getScheduleOfAllClassByCourseId(course.getCourseId());
                                response.setClassScheduleResponse(classScheduleResponse);
                                return response;
                            })
                            .collect(Collectors.toList());


                    return new CourseGroupResponse(
                            category.getId(),
                            category.getName(),
                            category.getLevel(),
                            category.getDescription(),
                            courseResponses
                    );
                })
                .collect(Collectors.toList());
    }



    public List<ActiveCourseNameResponse> getAllActiveCourseNames() {
        return courseRepository.findByStatusTrue()
                .stream()
                .map(courseMapper::toActiveNameResponse)
                .collect(Collectors.toList());
    }

    public CoursePageResponse getAllCourses(int page, int size)
    {
        Pageable paging = PageRequest.of(page, size);
        Page<Course> pageTuts;

        pageTuts = courseRepository.findByStatusTrue(paging);
        List<Course> x = pageTuts.getContent();

        List<CourseResponse> courseResponses = pageTuts.getContent().stream().map(course -> {
            CourseResponse dto = new CourseResponse();
            dto.setCourseId(course.getCourseId());
            dto.setCourseName(course.getCourseName());
            dto.setCreatedDate(course.getCreatedDate());
            dto.setTuitionFee(course.getTuitionFee());
            dto.setIsActive(course.getStatus());
            dto.setCourseCategoryId(course.getCourseCategory().getId());

            return dto;
        }).toList();;


        return new CoursePageResponse(courseResponses,pageTuts.getNumber(),pageTuts.getTotalElements(),pageTuts.getTotalPages());
    }

    private ActiveCourseResponse mapToResponse(Course course) {
        ActiveCourseResponse response = new ActiveCourseResponse();
        response.setCourseId(course.getCourseId());
        response.setCourseName(course.getCourseName());
        response.setTuitionFee(course.getTuitionFee());
        response.setDescription(course.getDescription());
        response.setEntryLevel(course.getEntryLevel());
        response.setTargetLevel(course.getTargetLevel());
        response.setImage(course.getImage());
        return response;
    }


    @Transactional
    public Course createCourse(CourseCreateRequest request) {
        // 1. Tạo course cơ bản
        Course course = courseMapper.toNewCourse(request);
        course.setStatus(false);
        course.setCreatedDate(LocalDateTime.now());
        course.setCreatedBy("admin");
        course.setStudyHours(request.getStudyHours());

        // 2. Thêm Objectives
        if (request.getObjectives() != null && !request.getObjectives().isEmpty()) {
            List<Objective> objectives = request.getObjectives().stream()
                    .map(obj -> {
                        Objective objective = new Objective();
                        objective.setObjectiveName(obj.getObjectiveName());
                        objective.setCourse(course);
                        return objective;
                    })
                    .collect(Collectors.toList());
            course.setObjectives(objectives);
        }

        // Lưu course trước để có courseId
        courseRepository.save(course);

        // 3. Xử lý Skills và Modules
        if (request.getModules() != null && !request.getModules().isEmpty()) {
            // Group modules theo skillId
            Map<Integer, List<ModuleRequest>> modulesBySkill = request.getModules().stream()
                    .collect(Collectors.groupingBy(ModuleRequest::getSkillId));

            // Validate: skillIds trong request phải khớp với skillIds trong modules
            Set<Integer> uniqueSkillIds = modulesBySkill.keySet();
            if (request.getSkillIds() != null && !new HashSet<>(request.getSkillIds()).containsAll(uniqueSkillIds)) {
                throw new AppException(ErrorCode.SKILL_MISMATCH);
            }

            // Với mỗi skill, tạo CourseSkill và modules tương ứng
            for (Map.Entry<Integer, List<ModuleRequest>> entry : modulesBySkill.entrySet()) {
                Integer skillId = entry.getKey();
                List<ModuleRequest> moduleRequests = entry.getValue();

                // Tìm skill
                Skill skill = skillRepository.findById(skillId)
                        .orElseThrow(() -> new AppException(ErrorCode.SKILL_NOT_FOUND));

                // Tạo CourseSkill
                CourseSkill courseSkill = new CourseSkill();
                courseSkill.setCourse(course);
                courseSkill.setSkill(skill);
                courseSkillRepository.save(courseSkill);

                // Tạo modules cho skill này
                for (ModuleRequest moduleReq : moduleRequests) {
                    moduleService.addModule(courseSkill.getCourseSkillId(), moduleReq);
                }
            }
        }

        return course;
    }

    // ========== UPDATE COURSE (chỉ thông tin cơ bản và skills) ==========
    @Transactional
    public Course updateCourse(Integer id, CourseUpdateRequest request) {
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.COURSE_NOT_FOUND));

        // 1. Update thông tin cơ bản
        if (request.getCourseName() != null) {
            course.setCourseName(request.getCourseName());
        }
        if (request.getTuitionFee() != null) {
            course.setTuitionFee(request.getTuitionFee());
        }
        if (request.getDescription() != null) {
            course.setDescription(request.getDescription());
        }
        if (request.getEntryLevel() != null) {
            course.setEntryLevel(request.getEntryLevel());
        }
        if (request.getTargetLevel() != null) {
            course.setTargetLevel(request.getTargetLevel());
        }
        if (request.getImage() != null) {
            course.setImage(request.getImage());
        }
        if (request.getVideo() != null) {
            course.setVideo(request.getVideo());
        }
        if (request.getStudyHours() != null) {
            course.setStudyHours(request.getStudyHours());
        }
        if (request.getCategoryId() != null) {
            CourseCategory category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new AppException(ErrorCode.CATEGORY_NOT_FOUND));
            course.setCourseCategory(category);
        }

        courseRepository.save(course);

        // 2. Xử lý Skills
        // Thêm skills mới
        if (request.getSkillIdsToAdd() != null && !request.getSkillIdsToAdd().isEmpty()) {
            for (Integer skillId : request.getSkillIdsToAdd()) {
                // Kiểm tra xem skill đã tồn tại trong course chưa
                if (!courseSkillRepository.existsByCourse_CourseIdAndSkill_SkillId(id, skillId)) {
                    Skill skill = skillRepository.findById(skillId)
                            .orElseThrow(() -> new AppException(ErrorCode.SKILL_NOT_FOUND));

                    CourseSkill courseSkill = new CourseSkill();
                    courseSkill.setCourse(course);
                    courseSkill.setSkill(skill);
                    courseSkillRepository.save(courseSkill);
                }
            }
        }

        // Xóa skills
        if (request.getSkillIdsToRemove() != null && !request.getSkillIdsToRemove().isEmpty()) {
            for (Integer skillId : request.getSkillIdsToRemove()) {
                Optional<CourseSkill> courseSkillOpt = courseSkillRepository
                        .findByCourse_CourseIdAndSkill_SkillId(id, skillId);

                courseSkillOpt.ifPresent(courseSkillRepository::delete);
                // Cascade sẽ tự động xóa các modules liên quan
            }
        }

        return course;
    }


    // Get course by ID (details)
    public CourseDetailResponse getCourseDetailById(Integer id) {
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.COURSE_NOT_FOUND));


        List<Promotion> validPromotions = promotionRepository.findValidPromotionsByCourseAndType1(
                course.getCourseId(),
                LocalDate.now()
        );


        CourseDetailResponse response = courseMapper.toResponse(course);
        if (!validPromotions.isEmpty()) {
            Promotion bestPromotion = validPromotions.get(0);
            Double promotionPrice =  course.getTuitionFee() / bestPromotion.getDiscountPercent();
            response.setPromotionPrice(promotionPrice);
        }

        response.setEntryLevel(course.getEntryLevel());
        response.setStatus(course.getStatus());
        response.setCategory(course.getCourseCategory().getName());
        response.setLevel(course.getCourseCategory().getLevel());
        List<Module> modules = new ArrayList<>();
        List<CourseSkill> courseSkills = courseSkillRepository.findByCourse_CourseId(course.getCourseId());
        courseSkills.forEach(courseSkill -> {
            modules.addAll(courseSkill.getModules());
        });
        response.setModules(modules);
        List<ClassResponse.ClassInfo> classInfos = getClassesForCourse(course.getCourseId());
        response.setClassInfos(classInfos);
        return response;
    }
    private List<ClassResponse.ClassInfo> getClassesForCourse(Integer courseId) {
        Set<CourseClass> classes = courseClassRepository
                .findByCourse_CourseIdAndStatus(courseId, ClassStatusEnum.InProgress.name());

        return classes.stream()
                .map(this::mapToClassInfo)
                .sorted(Comparator
                        .comparing(ClassResponse.ClassInfo::getStatus).reversed() // Active first
                        .thenComparing(ClassResponse.ClassInfo::getStartDate).reversed()) // Then by start date
                .collect(Collectors.toList());
    }
    private ClassResponse.ClassInfo mapToClassInfo(CourseClass cls) {
        ClassResponse.ClassInfo info = new ClassResponse.ClassInfo();

        info.setClassId(cls.getClassId());
        info.setClassName(cls.getClassName());
        info.setStatus(cls.getStatus());

        if (cls.getCourse() != null) {
            info.setCourseName(cls.getCourse().getCourseName());
        }

        if (cls.getRoom() != null) {
            info.setRoomName(cls.getRoom().getRoomName());
        }

        if (cls.getLecturer() != null) {
            info.setInstructorName(cls.getLecturer().getFullName());
        }

        info.setSchedulePattern(cls.getSchedule());
        info.setStartTime(cls.getStartTime());
        info.setStartDate(cls.getStartDate());

        if (cls.getMinutesPerSession() != null && cls.getStartTime() != null) {
            info.setEndTime(cls.getStartTime().plusMinutes(cls.getMinutesPerSession()));
        }

        List<Session> sessions = sessionRepository
                .findByCourseClass_ClassIdOrderBySessionDate(cls.getClassId());

        if (!sessions.isEmpty()) {
            info.setEndDate(sessions.get(sessions.size() - 1).getSessionDate());
        }

        return info;
    }








    // Change status of course
    public void changeStatus(Integer id) {
        Optional<Course> cs = courseRepository.findById(id);
        cs.ifPresent(course -> {
            course.setStatus(!course.getStatus());
            courseRepository.save(course);
        });
    }
    public List<ActiveCourseResponse>  getRecommendCourses(Integer id)
    {
        List<Course> cs = courseRepository.findTop3ByCourseIdNotAndStatusTrue(id);
        return  cs.stream()
            .map(course -> {
                ActiveCourseResponse dto = new ActiveCourseResponse();
                // Ánh xạ các thuộc tính từ Course sang CourseResponse
                dto.setCourseId(course.getCourseId());
                dto.setCourseName(course.getCourseName());
                dto.setImage(course.getImage());
                dto.setTuitionFee(course.getTuitionFee());
                dto.setEntryLevel(course.getEntryLevel());
                dto.setTargetLevel(course.getTargetLevel());
                dto.setDescription(course.getDescription());
                return dto;
            })
            .toList();



    }
    public List<SkillResponse> getSkills() {
        List<Skill> skills = skillRepository.findAll();
        List<SkillResponse> skillResponseList = new ArrayList<>();
        skills.forEach(skill -> {
            SkillResponse skillResponse = new SkillResponse();
            skillResponse.setSkillName(skill.getSkillName());
            skillResponse.setId(skill.getSkillId());
            skillResponseList.add(skillResponse);
        });
        return skillResponseList;

    }

    // Additional method for overview summary (e.g., count of courses)
    public long getCourseCount() {
        return courseRepository.count();
    }

    public CourseGroupResponse getCourseByStudent() {
        return  null;
    }
}
