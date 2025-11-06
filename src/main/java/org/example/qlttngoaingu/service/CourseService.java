package org.example.qlttngoaingu.service;

import lombok.AllArgsConstructor;
import org.example.qlttngoaingu.dto.request.CourseCreateRequest;
import org.example.qlttngoaingu.dto.request.CourseUpdateRequest;
import org.example.qlttngoaingu.dto.response.*;
import org.example.qlttngoaingu.entity.Course;
import org.example.qlttngoaingu.entity.Document;
import org.example.qlttngoaingu.entity.Content;
import org.example.qlttngoaingu.repository.CourseRepository;

import org.example.qlttngoaingu.entity.Objective;
import org.example.qlttngoaingu.entity.Module;
import org.example.qlttngoaingu.exception.AppException;
import org.example.qlttngoaingu.exception.ErrorCode;
import org.example.qlttngoaingu.mapper.CourseMapper;
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

    private final ModuleService moduleService;
    private final CourseMapper courseMapper;

    // Get all courses (overview)
    public List<ActiveCourseResponse> getAllActiveCourses() {
        return courseRepository.findByStatusTrue()
                .stream()
                .map(courseMapper::toActiveResponse)
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
        // Map course cơ bản từ mapper
        Course course = courseMapper.toNewCourse(request);
        course.setStatus(false);
        course.setCreatedDate(LocalDateTime.now());
        course.setCreatedBy("admin");
        course.setStudyHours(request.getStudyHours());
        // 1️⃣ Thêm Objectives
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

        // 2️⃣ Thêm Modules, Documents, Contents
        if (request.getModules() != null && !request.getModules().isEmpty()) {
            List<Module> modules = request.getModules().stream().map(mReq -> {
                Module module = new Module();
                module.setModuleName(mReq.getModuleName());
                module.setCourse(course);

                // 2.1️⃣ Documents
                if (mReq.getDocuments() != null && !mReq.getDocuments().isEmpty()) {
                    List<Document> documents = mReq.getDocuments().stream()
                            .map(dReq -> {
                                Document doc = new Document();
                                doc.setFileName(dReq.getFileName());
                                doc.setLink(dReq.getLink());
                                doc.setDescription(dReq.getDescription());
                                doc.setImage(dReq.getImage());
                                doc.setModule(module);
                                return doc;
                            })
                            .collect(Collectors.toList());
                    module.setDocuments(documents);
                }

                // 2.2️⃣ Contents
                if (mReq.getContents() != null && !mReq.getContents().isEmpty()) {
                    List<Content> contents = mReq.getContents().stream()
                            .map(cReq -> {
                                Content content = new Content();
                                content.setContentName(cReq.getContentName());
                                content.setModule(module);
                                return content;
                            })
                            .collect(Collectors.toList());
                    module.setContents(contents);
                }

                return module;
            }).collect(Collectors.toList());

            course.setModules(modules);




        }

        // 4️⃣ Lưu toàn bộ
        courseRepository.save(course);

        return course;
    }


    // Get course by ID (details)
    public CourseDetailResponse getCourseDetailById(Integer id) {
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.COURSE_NOT_FOUND));
        CourseDetailResponse response = courseMapper.toResponse(course);
        response.setEntryLevel(course.getEntryLevel());
        response.setStatus(course.getStatus());
        response.setCategory(course.getCourseCategory().getName());
        response.setLevel(course.getCourseCategory().getLevel());
        return response;
    }




    // Update an existing course
    public Course updateCourse(Integer id, CourseUpdateRequest updatedCourse) {

        Course cs  = courseRepository.findById(id).orElseThrow(() -> new AppException(ErrorCode.COURSE_NOT_FOUND));
        courseMapper.toExistingCourse(cs, updatedCourse);
        return courseRepository.save(cs);
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

    // Additional method for overview summary (e.g., count of courses)
    public long getCourseCount() {
        return courseRepository.count();
    }
}
