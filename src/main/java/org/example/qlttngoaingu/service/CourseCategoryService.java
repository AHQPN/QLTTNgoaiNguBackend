package org.example.qlttngoaingu.service;

import lombok.RequiredArgsConstructor;
import org.example.qlttngoaingu.dto.request.CourseCategoryRequest;
import org.example.qlttngoaingu.dto.response.ActiveCourseResponse;
import org.example.qlttngoaingu.dto.response.ClassScheduleResponse;
import org.example.qlttngoaingu.dto.response.CourseCategoryResponse;
import org.example.qlttngoaingu.dto.response.CourseGroupResponse;
import org.example.qlttngoaingu.entity.Course;
import org.example.qlttngoaingu.entity.CourseCategory;
import org.example.qlttngoaingu.entity.Promotion;
import org.example.qlttngoaingu.exception.AppException;
import org.example.qlttngoaingu.exception.ErrorCode;
import org.example.qlttngoaingu.mapper.CourseMapper;
import org.example.qlttngoaingu.repository.CourseCategoryRepository;
import org.example.qlttngoaingu.repository.CourseRepository;
import org.example.qlttngoaingu.repository.PromotionRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CourseCategoryService {

    private final CourseCategoryRepository categoryRepository;
    private final CourseClassService courseClassService;
    private final CourseRepository courseRepository;
    private final CourseMapper courseMapper;
    private final PromotionRepository promotionRepository;


    public CourseCategoryResponse create(CourseCategoryRequest request) {
        CourseCategory category = new CourseCategory();
        category.setName(request.getName());

        category = categoryRepository.save(category);
        return toResponse(category);
    }


    public CourseCategoryResponse update(Integer id, CourseCategoryRequest request) {
        CourseCategory category = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Category not found with id = " + id));

        category.setName(request.getName());
        category = categoryRepository.save(category);
        return toResponse(category);
    }

    public List<CourseCategoryResponse> getAll() {
        return categoryRepository.findAll()
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public CourseGroupResponse getById(Integer id) {

        CourseCategory category = categoryRepository.getCourseCategoriesById(id)
                .orElseThrow(() -> new AppException(ErrorCode.CATEGORY_NOT_FOUND));

        // chỉ lấy courses đang active của category này
        List<Course> courses = courseRepository.findByCourseCategory_IdAndStatusTrue(id);

        List<ActiveCourseResponse> courseResponses = courses.stream()
                .map(course -> {
                    ActiveCourseResponse response = courseMapper.toActiveResponse(course);
                    response.setObjectives(course.getObjectives());
                    List<Promotion> validPromotions = promotionRepository.findValidPromotionsByCourseAndType1(
                            course.getCourseId(),
                            LocalDate.now()
                    );
                    if (!validPromotions.isEmpty()) {
                        Promotion bestPromotion = validPromotions.get(0);
                        Double promotionPrice =  course.getTuitionFee() / bestPromotion.getDiscountPercent();
                        response.setPromotionPrice(promotionPrice);
                    }
                    ClassScheduleResponse schedule =
                            courseClassService.getScheduleOfAllClassByCourseId(course.getCourseId());
                    response.setClassScheduleResponse(schedule);

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
