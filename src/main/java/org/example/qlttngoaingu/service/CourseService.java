package org.example.qlttngoaingu.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.example.qlttngoaingu.dto.request.CourseCreateRequest;
import org.example.qlttngoaingu.dto.request.CourseUpdateRequest;
import org.example.qlttngoaingu.dto.request.ModuleRequest;
import org.example.qlttngoaingu.dto.response.ActiveCourseNameResponse;
import org.example.qlttngoaingu.dto.response.ActiveCourseResponse;
import org.example.qlttngoaingu.dto.response.ClassResponse;
import org.example.qlttngoaingu.dto.response.ClassScheduleResponse;
import org.example.qlttngoaingu.dto.response.CourseDetailResponse;
import org.example.qlttngoaingu.dto.response.CourseGroupResponse;
import org.example.qlttngoaingu.dto.response.CoursePageResponse;
import org.example.qlttngoaingu.dto.response.CourseResponse;
import org.example.qlttngoaingu.dto.response.SkillResponse;
import org.example.qlttngoaingu.entity.Course;
import org.example.qlttngoaingu.entity.CourseCategory;
import org.example.qlttngoaingu.entity.CourseClass;
import org.example.qlttngoaingu.entity.CourseSkill;
import org.example.qlttngoaingu.entity.Objective;
import org.example.qlttngoaingu.entity.Promotion;
import org.example.qlttngoaingu.entity.PromotionDetail;
import org.example.qlttngoaingu.entity.Session;
import org.example.qlttngoaingu.entity.Skill;
import org.example.qlttngoaingu.exception.AppException;
import org.example.qlttngoaingu.exception.ErrorCode;
import org.example.qlttngoaingu.mapper.CourseMapper;
import org.example.qlttngoaingu.repository.CourseCategoryRepository;
import org.example.qlttngoaingu.repository.CourseClassRepository;
import org.example.qlttngoaingu.repository.CourseRepository;
import org.example.qlttngoaingu.repository.CourseSkillRepository;
import org.example.qlttngoaingu.repository.InvoiceDetailRepository;
import org.example.qlttngoaingu.repository.PromotionDetailRepository;
import org.example.qlttngoaingu.repository.PromotionRepository;
import org.example.qlttngoaingu.repository.SessionRepository;
import org.example.qlttngoaingu.repository.SkillRepository;
import org.example.qlttngoaingu.service.enums.ClassStatusEnum;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.AllArgsConstructor;

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
    private final InvoiceDetailRepository invoiceDetailRepository;
    private final ReviewService reviewService;

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

        pageTuts = courseRepository.findAll(paging);
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
        
        // Nhóm modules theo skill
        List<CourseSkill> courseSkills = courseSkillRepository.findByCourse_CourseId(course.getCourseId());
        List<CourseDetailResponse.SkillModuleGroup> skillModules = courseSkills.stream()
                .map(courseSkill -> {
                    CourseDetailResponse.SkillModuleGroup group = new CourseDetailResponse.SkillModuleGroup();
                    group.setSkillId(courseSkill.getSkill().getSkillId());
                    group.setSkillName(courseSkill.getSkill().getSkillName());
                    group.setModules(new ArrayList<>(courseSkill.getModules()));
                    return group;
                })
                .collect(Collectors.toList());
        response.setSkillModules(skillModules);
        
        List<ClassResponse.ClassInfo> classInfos = getClassesForCourse(course.getCourseId());
        response.setClassInfos(classInfos);
        
        // Thêm thông tin combo promotions
        List<CourseDetailResponse.ComboPromotionInfo> comboPromotions = getComboPromotionsForCourse(course.getCourseId());
        response.setComboPromotions(comboPromotions);
        
        return response;
    }
    
    /**
     * Lấy danh sách combo promotions có chứa khóa học này
     */
    private List<CourseDetailResponse.ComboPromotionInfo> getComboPromotionsForCourse(Integer courseId) {
        LocalDate today = LocalDate.now();
        List<CourseDetailResponse.ComboPromotionInfo> comboInfos = new ArrayList<>();
        
        // Tìm tất cả promotion Type 2 (combo) đang active
        List<Promotion> activeComboPromotions = promotionRepository.findAll().stream()
                .filter(p -> p.getPromotionType().getId() == 2) // Type 2 = Combo
                .filter(p -> Boolean.TRUE.equals(p.getActive()))
                .filter(p -> !today.isBefore(p.getStartDate()) && !today.isAfter(p.getEndDate()))
                .collect(Collectors.toList());
        
        for (Promotion promo : activeComboPromotions) {
            // Lấy danh sách khóa trong combo
            List<PromotionDetail> details = promotionDetailRepository.findByPromotion(promo);
            List<Integer> comboCourseIds = details.stream()
                    .map(pd -> pd.getCourse().getCourseId())
                    .collect(Collectors.toList());
            
            // Nếu combo chứa khóa học hiện tại
            if (comboCourseIds.contains(courseId)) {
                CourseDetailResponse.ComboPromotionInfo comboInfo = new CourseDetailResponse.ComboPromotionInfo();
                comboInfo.setComboName(promo.getName());
                comboInfo.setDiscountPercent(promo.getDiscountPercent());
                
                // Lấy tên các khóa học còn lại trong combo (không bao gồm khóa hiện tại)
                List<String> requiredCourseNames = details.stream()
                        .filter(pd -> !pd.getCourse().getCourseId().equals(courseId))
                        .map(pd -> pd.getCourse().getCourseName())
                        .collect(Collectors.toList());
                
                comboInfo.setRequiredCourseNames(requiredCourseNames);
                comboInfos.add(comboInfo);
            }
        }
        
        return comboInfos;
    }
    private List<ClassResponse.ClassInfo> getClassesForCourse(Integer courseId) {
        Set<CourseClass> classes = courseClassRepository
                .findByCourse_CourseIdAndStatusAndStartDateAfter(courseId, ClassStatusEnum.InProgress.name(),LocalDate.now());

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
        info.setMaxCapacity(cls.getRoom().getCapacity());
        Integer enrollmentCount = invoiceDetailRepository.countByClassIdAndActiveInvoice(cls.getClassId());

        info.setCurrentEnrollment(enrollmentCount);

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
