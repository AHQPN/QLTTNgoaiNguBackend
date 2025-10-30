package org.example.qlttngoaingu.service;

import jakarta.validation.constraints.DecimalMax;
import lombok.RequiredArgsConstructor;
import org.example.qlttngoaingu.entity.CourseCategory;
import org.example.qlttngoaingu.repository.CourseCategoryRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CourseCategoryService {
    private final CourseCategoryRepository categoryRepository;


    public List<CourseCategory> findAll() {
        return categoryRepository.findAll();
    }

    public CourseCategory findById(Integer id) {
        return categoryRepository.findById(id).isPresent() ?
                categoryRepository.findById(id).get() : null;
    }

    public CourseCategory save(CourseCategory category) {
        return categoryRepository.save(category);
    }

    public void deleteById(Integer id) {
        categoryRepository.deleteById(id);
    }
}
