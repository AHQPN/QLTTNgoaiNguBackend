package org.example.qlttngoaingu.Service;

import lombok.AllArgsConstructor;
import org.example.qlttngoaingu.Dto.Request.CourseCreateRequest;
import org.example.qlttngoaingu.Dto.Response.CourseDetailResponse;
import org.example.qlttngoaingu.Dto.Response.ActiveCourseResponse;
import org.example.qlttngoaingu.Dto.Response.CoursePageResponse;
import org.example.qlttngoaingu.Dto.Response.CourseResponse;
import org.example.qlttngoaingu.Repository.ContentRepository;
import org.example.qlttngoaingu.Repository.CourseRepository;
import org.example.qlttngoaingu.Repository.GoalRepository;
import org.example.qlttngoaingu.Repository.ModuleRepository;
import org.example.qlttngoaingu.entity.Course;
import org.example.qlttngoaingu.entity.Objective;
import org.example.qlttngoaingu.entity.Module;
import org.example.qlttngoaingu.exception.AppException;
import org.example.qlttngoaingu.exception.ErrorCode;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class CourseService {


    private final CourseRepository courseRepository;
    private final ModuleRepository moduleRepository;
    private final GoalRepository goalRepository;
    private final ContentRepository contentRepository;

    // Get all courses (overview)
    public List<ActiveCourseResponse> getAllActiveCourses() {
        return courseRepository.findByStatusTrue()
                .stream()
                .map(this::mapToResponse)
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
        Course course = new Course();
        course.setCourseName(request.getCourseName());
        course.setTuitionFee(request.getTuitionFee());

        course.setVideo(request.getVideo());
        course.setStatus(true);
        course.setCreatedDate(LocalDateTime.now());
        course.setDescription(request.getDescription());
        course.setEntryLevel(request.getEntryLevel());
        course.setTargetLevel(request.getTargetLevel());
        course.setImage(request.getImage());
        course.setCreatedBy("admin");

        // ✅ Map danh sách mục tiêu học tập (objectives)
        if (request.getObjectives() != null) {
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

        if (request.getModules() != null) {
            List<Module> modules = request.getModules().stream()
                    .map(m -> {
                        Module module = new Module();
                        module.setModuleName(m.getModuleName());
                        module.setDuration(m.getDuration());
                        module.setCourse(course);
                        return module;
                    })
                    .collect(Collectors.toList());
            course.setModules(modules);
            int totalDuration = modules.stream()
                    .filter(m -> m.getDuration() != null)
                    .mapToInt(Module::getDuration)
                    .sum();
            course.setNumberOfSessions(totalDuration);
            course.setStudyHours(2*totalDuration);
        }
        return courseRepository.save(course);

    }

    // Get course by ID (details)
    public CourseDetailResponse getCourseDetailById(Integer id) {
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.COURSE_NOT_FOUND));

        return mapToDetailResponse(course);
    }

    private CourseDetailResponse mapToDetailResponse(Course course) {
        CourseDetailResponse response = new CourseDetailResponse();
        response.setCourseId(course.getCourseId());
        response.setCourseName(course.getCourseName());
        response.setStudyHours(course.getStudyHours());
        response.setTuitionFee(course.getTuitionFee());
        response.setNumberOfSessions(course.getNumberOfSessions());
        response.setVideo(course.getVideo());
        response.setObjectives(course.getObjectives());
        response.setModules(course.getModules());
        response.setDescription(course.getDescription());
        response.setEntryLevel(course.getEntryLevel());
        response.setTargetLevel(course.getTargetLevel());
        response.setImage(course.getImage());
        return response;
    }


    // Update an existing course
    public Optional<Course> updateCourse(Integer id, Course updatedCourse) {
        return courseRepository.findById(id).map(course -> {
            course.setCourseName(updatedCourse.getCourseName());
            course.setStudyHours(updatedCourse.getStudyHours());
            course.setTuitionFee(updatedCourse.getTuitionFee());
            course.setNumberOfSessions(updatedCourse.getNumberOfSessions());
            course.setVideo(updatedCourse.getVideo());
            course.setStatus(updatedCourse.getStatus());
            course.setDescription(updatedCourse.getDescription());
            course.setEntryLevel(updatedCourse.getEntryLevel());
            course.setTargetLevel(updatedCourse.getTargetLevel());
            return courseRepository.save(course);
        });
    }


    // Disable a course
    public void disableCourse(Integer id) {
        Optional<Course> cs = courseRepository.findById(id);
        cs.ifPresent(course -> {
            course.setStatus(false);
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

    // Additional method for overview summary (e.g., count of courses)
    public long getCourseCount() {
        return courseRepository.count();
    }
}
