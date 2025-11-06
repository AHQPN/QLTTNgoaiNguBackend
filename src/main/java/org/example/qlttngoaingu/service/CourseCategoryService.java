package org.example.qlttngoaingu.service;

import lombok.RequiredArgsConstructor;
import org.example.qlttngoaingu.dto.request.CourseCategoryRequest;
import org.example.qlttngoaingu.dto.response.CourseCategoryResponse;
import org.example.qlttngoaingu.entity.CourseCategory;
import org.example.qlttngoaingu.repository.CourseCategoryRepository;
import org.springframework.stereotype.Service;

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
    public CourseCategoryResponse getById(Integer id) {
        CourseCategory category = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Category not found with id = " + id));
        return toResponse(category);
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
}
