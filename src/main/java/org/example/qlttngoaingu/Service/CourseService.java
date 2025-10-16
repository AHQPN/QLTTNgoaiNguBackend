package org.example.qlttngoaingu.Service;

import lombok.AllArgsConstructor;
import org.example.qlttngoaingu.Dto.Request.CourseCreateRequest;
import org.example.qlttngoaingu.Dto.Response.CourseDetailResponse;
import org.example.qlttngoaingu.Dto.Response.CourseResponse;
import org.example.qlttngoaingu.Repository.ContentRepository;
import org.example.qlttngoaingu.Repository.CourseRepository;
import org.example.qlttngoaingu.Repository.GoalRepository;
import org.example.qlttngoaingu.Repository.ModuleRepository;
import org.example.qlttngoaingu.entity.Course;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
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
    public List<CourseResponse> getAllCourses() {
        return courseRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());

    }
    private CourseResponse mapToResponse(Course course) {
        CourseResponse response = new CourseResponse();
        response.setCourseId(course.getCourseId());
        response.setCourseName(course.getCourseName());
        response.setTuitionFee(course.getTuitionFee());
        return response;
    }

    public Course createCourse(CourseCreateRequest request) {
        Course course = new Course();
        course.setCourseName(request.getCourseName());
        course.setStudyHours(request.getStudyHours());
        course.setTuitionFee(request.getTuitionFee());
        course.setNumberOfSessions(request.getNumberOfSessions());
        course.setVideo(request.getVideo());
        course.setObjectives(request.getObjectives());
        course.setModules(request.getModules());

        Course saved = courseRepository.save(course);

        return saved;

    }

    // Get course by ID (details)
    public CourseDetailResponse getCourseDetailById(Integer id) {
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Course not found with id " + id));

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
            // Update nested collections if needed (goals, modules)
            return courseRepository.save(course);
        });
    }

    // Delete a course
    public void deleteCourse(Integer id) {
        courseRepository.deleteById(id);
    }

    // Additional method for overview summary (e.g., count of courses)
    public long getCourseCount() {
        return courseRepository.count();
    }
}
