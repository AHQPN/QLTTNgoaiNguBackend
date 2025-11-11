package org.example.qlttngoaingu.service;

import lombok.RequiredArgsConstructor;
import org.example.qlttngoaingu.dto.request.CourseCategoryRequest;
import org.example.qlttngoaingu.dto.response.ActiveCourseResponse;
import org.example.qlttngoaingu.dto.response.CourseCategoryResponse;
import org.example.qlttngoaingu.dto.response.CourseGroupResponse;
import org.example.qlttngoaingu.entity.Course;
import org.example.qlttngoaingu.entity.CourseCategory;
import org.example.qlttngoaingu.repository.CourseCategoryRepository;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CourseCategoryService {

    private final CourseCategoryRepository categoryRepository;

    // ✅ Create
    public CourseCategoryResponse create(CourseCategoryRequest request) {
        CourseCategory category = new CourseCategory();
        category.setName(request.getName());

        category = categoryRepository.save(category);
        return toResponse(category);
    }

    // ✅ Update
    public CourseCategoryResponse update(Integer id, CourseCategoryRequest request) {
        CourseCategory category = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Category not found with id = " + id));

        category.setName(request.getName());
        category = categoryRepository.save(category);
        return toResponse(category);
    }

    // ✅ Get all
    public List<CourseCategoryResponse> getAll() {
        return categoryRepository.findAll()
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    // ✅ Get by ID
    public CourseGroupResponse getById(Integer id) {
        CourseCategory category = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Category not found with id = " + id));
        return toResponseNew(category);
    }

    // ✅ Delete
    public void delete(Integer id) {
        if (!categoryRepository.existsById(id)) {
            throw new RuntimeException("Category not found with id = " + id);
        }
        categoryRepository.deleteById(id);
    }

    // ✅ Private helper để map entity -> response
    private CourseCategoryResponse toResponse(CourseCategory category) {
        CourseCategoryResponse response = new CourseCategoryResponse();
        response.setId(category.getId());
        response.setName(category.getName());
        return response;
    }

    private CourseGroupResponse toResponseNew(CourseCategory category) {
        CourseGroupResponse response = new CourseGroupResponse();

        // Map các trường của Category
        response.setCategoryId(category.getId());
        response.setCategoryName(category.getName());
        response.setCategoryLevel(category.getLevel()); // Giả định getter
        response.setCategoryDescription(category.getDescription()); // Giả định getter

        // Map danh sách các khóa học con
        List<ActiveCourseResponse> courseDTOs;

        // Kiểm tra null để tránh lỗi
        if (category.getCourses() != null) {
            // Sử dụng stream để lặp và map từng Course entity sang DTO
            courseDTOs = category.getCourses().stream()
                    .map(this::toActiveCourseResponse) // Gọi hàm phụ
                    .collect(Collectors.toList());
        } else {
            // Trả về danh sách rỗng nếu không có khóa học
            courseDTOs = Collections.emptyList();
        }

        response.setCourses(courseDTOs);

        return response;
    }

    private ActiveCourseResponse toActiveCourseResponse(Course course) {
        ActiveCourseResponse dto = new ActiveCourseResponse();

        dto.setCourseId(course.getCourseId());
        dto.setCourseName(course.getCourseName());
        dto.setTuitionFee(course.getTuitionFee());
        dto.setEntryLevel(course.getEntryLevel());
        dto.setTargetLevel(course.getTargetLevel());
        dto.setDescription(course.getDescription());
        dto.setImage(course.getImage());

        return dto;
    }
}
