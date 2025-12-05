package org.example.qlttngoaingu.service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

import org.example.qlttngoaingu.dto.request.PromotionRequest;
import org.example.qlttngoaingu.dto.response.PromotionResponse;
import org.example.qlttngoaingu.entity.Course;
import org.example.qlttngoaingu.entity.Promotion;
import org.example.qlttngoaingu.entity.PromotionDetail;
import org.example.qlttngoaingu.entity.PromotionType;
import org.example.qlttngoaingu.exception.AppException;
import org.example.qlttngoaingu.exception.ErrorCode;
import org.example.qlttngoaingu.repository.CourseRepository;
import org.example.qlttngoaingu.repository.PromotionDetailRepository;
import org.example.qlttngoaingu.repository.PromotionRepository;
import org.example.qlttngoaingu.repository.PromotionTypeRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class PromotionService {

    private final PromotionRepository promotionRepository;
    private final PromotionTypeRepository promotionTypeRepository;
    private final PromotionDetailRepository promotionDetailRepository;
    private final CourseRepository courseRepository;

    /**
     * Tạo mới chương trình khuyến mãi
     */
    @Transactional
    public PromotionResponse createPromotion(PromotionRequest request) {
        // Validate dates
        if (request.getEndDate().isBefore(request.getStartDate())) {
            throw new AppException(ErrorCode.INVALID_DATE_RANGE);
        }

        // Kiểm tra promotion type
        PromotionType promotionType = promotionTypeRepository.findById(request.getPromotionTypeId())
                .orElseThrow(() -> new AppException(ErrorCode.PROMOTION_TYPE_NOT_FOUND));

        // Tạo promotion
        Promotion promotion = new Promotion();
        promotion.setName(request.getName());
        promotion.setDescription(request.getDescription());
        promotion.setDiscountPercent(request.getDiscountPercent());
        promotion.setStartDate(request.getStartDate());
        promotion.setEndDate(request.getEndDate());
        promotion.setPromotionType(promotionType);
        
        // Xác định trạng thái active dựa trên ngày bắt đầu và kết thúc
        LocalDate today = LocalDate.now();
        promotion.setActive(
            !today.isBefore(request.getStartDate()) && !today.isAfter(request.getEndDate())
        );

        Promotion savedPromotion = promotionRepository.save(promotion);

        // Xử lý promotion details (cho Type 1 và Type 2)
        if ((promotionType.getId() == 1 || promotionType.getId() == 2) 
                && request.getCourseIds() != null && !request.getCourseIds().isEmpty()) {
            
            List<Course> courses = courseRepository.findAllById(request.getCourseIds());
            if (courses.size() != request.getCourseIds().size()) {
                throw new AppException(ErrorCode.COURSE_NOT_FOUND);
            }

            // Chỉ kiểm tra cho Type 1 (khuyến mãi lẻ)
            if (promotionType.getId() == 1) {
                validateCourseNotInActivePromotion(request.getCourseIds(), null);
            }

            for (Course course : courses) {
                PromotionDetail detail = new PromotionDetail();
                detail.setPromotion(savedPromotion);
                detail.setCourse(course);
                promotionDetailRepository.save(detail);
            }
        }

        log.info("Created promotion: {} (Type: {})", savedPromotion.getId(), promotionType.getName());
        return toResponse(savedPromotion);
    }

    /**
     * Cập nhật chương trình khuyến mãi
     */
    @Transactional
    public PromotionResponse updatePromotion(Integer id, PromotionRequest request) {
        Promotion promotion = promotionRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.PROMOTION_NOT_FOUND));

        // Validate dates
        if (request.getEndDate().isBefore(request.getStartDate())) {
            throw new AppException(ErrorCode.INVALID_DATE_RANGE);
        }

        // Kiểm tra promotion type
        PromotionType promotionType = promotionTypeRepository.findById(request.getPromotionTypeId())
                .orElseThrow(() -> new AppException(ErrorCode.PROMOTION_TYPE_NOT_FOUND));

        // Cập nhật thông tin
        promotion.setName(request.getName());
        promotion.setDescription(request.getDescription());
        promotion.setDiscountPercent(request.getDiscountPercent());
        promotion.setStartDate(request.getStartDate());
        promotion.setEndDate(request.getEndDate());
        promotion.setPromotionType(promotionType);

        // Cập nhật trạng thái
        updatePromotionStatus(promotion);

        // Xóa promotion details cũ
        List<PromotionDetail> oldDetails = promotionDetailRepository.findByPromotion(promotion);
        promotionDetailRepository.deleteAll(oldDetails);

        // Thêm promotion details mới
        if ((promotionType.getId() == 1 || promotionType.getId() == 2) 
                && request.getCourseIds() != null && !request.getCourseIds().isEmpty()) {
            
            List<Course> courses = courseRepository.findAllById(request.getCourseIds());
            if (courses.size() != request.getCourseIds().size()) {
                throw new AppException(ErrorCode.COURSE_NOT_FOUND);
            }

            // Chỉ kiểm tra cho Type 1 (khuyến mãi lẻ), bỏ qua promotion hiện tại
            if (promotionType.getId() == 1) {
                validateCourseNotInActivePromotion(request.getCourseIds(), id);
            }

            for (Course course : courses) {
                PromotionDetail detail = new PromotionDetail();
                detail.setPromotion(promotion);
                detail.setCourse(course);
                promotionDetailRepository.save(detail);
            }
        }

        Promotion updatedPromotion = promotionRepository.save(promotion);
        log.info("Updated promotion: {}", id);
        return toResponse(updatedPromotion);
    }

    /**
     * Lấy thông tin chi tiết promotion
     */
    public PromotionResponse getPromotion(Integer id) {
        Promotion promotion = promotionRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.PROMOTION_NOT_FOUND));
        
        // Cập nhật trạng thái nếu hết hạn
        updatePromotionStatus(promotion);
        promotionRepository.save(promotion);
        
        return toResponse(promotion);
    }

    /**
     * Lấy danh sách tất cả promotions với phân trang
     */
    public Page<PromotionResponse> getAllPromotions(Pageable pageable) {
        Page<Promotion> promotions = promotionRepository.findAll(pageable);
        
        // Cập nhật trạng thái cho các promotion
        promotions.forEach(this::updatePromotionStatus);
        promotionRepository.saveAll(promotions.getContent());
        
        return promotions.map(this::toResponse);
    }

    /**
     * Lấy danh sách promotions đang active
     */
    public List<PromotionResponse> getActivePromotions() {
        LocalDate today = LocalDate.now();
        List<Promotion> promotions = promotionRepository.findAllActivePromotions(today);
        
        // Cập nhật trạng thái
        promotions.forEach(this::updatePromotionStatus);
        promotionRepository.saveAll(promotions);
        
        return promotions.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Lấy danh sách tất cả promotions (không phân trang)
     */
    public List<PromotionResponse> getAllPromotionsList() {
        List<Promotion> promotions = promotionRepository.findAll();
        
        // Cập nhật trạng thái
        promotions.forEach(this::updatePromotionStatus);
        promotionRepository.saveAll(promotions);
        
        return promotions.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Validate mã khuyến mãi
     */
    public PromotionResponse validatePromotionCode(String code) {
        LocalDate today = LocalDate.now();
        List<Promotion> activePromotions = promotionRepository.findAllActivePromotions(today);
        
        Promotion promotion = activePromotions.stream()
                .filter(p -> p.getName() != null && p.getName().equalsIgnoreCase(code))
                .findFirst()
                .orElseThrow(() -> new AppException(ErrorCode.PROMOTION_NOT_FOUND));
        
        return toResponse(promotion);
    }

    /**
     * Lấy danh sách promotion theo courseId (Type 1)
     */
    public List<PromotionResponse> getPromotionsByCourse(Integer courseId) {
        LocalDate today = LocalDate.now();
        List<Promotion> promotions = promotionRepository.findValidPromotionsByCourseAndType1(courseId, today);
        
        return promotions.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Toggle trạng thái active của promotion (soft delete)
     */
    @Transactional
    public PromotionResponse togglePromotionStatus(Integer id) {
        Promotion promotion = promotionRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.PROMOTION_NOT_FOUND));

        // Đổi trạng thái
        promotion.setActive(!promotion.getActive());
        Promotion updatedPromotion = promotionRepository.save(promotion);
        
        log.info("Toggled promotion {} status to: {}", id, updatedPromotion.getActive());
        return toResponse(updatedPromotion);
    }

    /**
     * Cập nhật trạng thái promotion dựa trên ngày
     * Nếu hết hạn → active = false
     */
    private void updatePromotionStatus(Promotion promotion) {
        LocalDate today = LocalDate.now();
        
        // Nếu chưa đến ngày bắt đầu hoặc đã qua ngày kết thúc → inactive
        if (today.isBefore(promotion.getStartDate()) || today.isAfter(promotion.getEndDate())) {
            if (Boolean.TRUE.equals(promotion.getActive())) {
                promotion.setActive(false);
                log.info("Auto-disabled expired promotion: {}", promotion.getId());
            }
        }
    }

    /**
     * Convert entity sang response DTO
     */
    private PromotionResponse toResponse(Promotion promotion) {
        List<PromotionDetail> details = promotionDetailRepository.findByPromotion(promotion);
        
        List<PromotionResponse.CourseSimpleResponse> courses = details.stream()
                .map(detail -> PromotionResponse.CourseSimpleResponse.builder()
                        .courseId(detail.getCourse().getCourseId())
                        .courseName(detail.getCourse().getCourseName())
                        .build())
                .collect(Collectors.toList());

        return PromotionResponse.builder()
                .id(promotion.getId())
                .name(promotion.getName())
                .description(promotion.getDescription())
                .discountPercent(promotion.getDiscountPercent())
                .startDate(promotion.getStartDate())
                .endDate(promotion.getEndDate())
                .active(promotion.getActive())
                .promotionTypeId(promotion.getPromotionType().getId())
                .promotionTypeName(promotion.getPromotionType().getName())
                .courses(courses)
                .build();
    }

    /**
     * Kiểm tra khóa học đã có trong promotion Type 1 khác còn hiệu lực chưa
     * @param courseIds Danh sách courseId cần kiểm tra
     * @param excludePromotionId PromotionId cần loại trừ (khi update)
     */
    private void validateCourseNotInActivePromotion(List<Integer> courseIds, Integer excludePromotionId) {
        LocalDate today = LocalDate.now();
        
        // Tìm tất cả promotion Type 1 đang active và còn hiệu lực
        List<Promotion> activeType1Promotions = promotionRepository.findAll().stream()
                .filter(p -> p.getPromotionType().getId() == 1) // Type 1
                .filter(p -> Boolean.TRUE.equals(p.getActive())) // Active
                .filter(p -> !today.isBefore(p.getStartDate()) && !today.isAfter(p.getEndDate())) // Còn hạn
                .filter(p -> excludePromotionId == null || !p.getId().equals(excludePromotionId)) // Loại trừ promotion hiện tại
                .collect(Collectors.toList());
        
        // Kiểm tra từng khóa học
        for (Integer courseId : courseIds) {
            for (Promotion existingPromo : activeType1Promotions) {
                List<Integer> existingCourseIds = promotionDetailRepository.findByPromotion(existingPromo)
                        .stream()
                        .map(pd -> pd.getCourse().getCourseId())
                        .collect(Collectors.toList());
                
                if (existingCourseIds.contains(courseId)) {
                    Course course = courseRepository.findById(courseId)
                            .orElseThrow(() -> new AppException(ErrorCode.COURSE_NOT_FOUND));
                    
                    log.error("Course {} '{}' already exists in active promotion '{}' (valid until {})", 
                            courseId, course.getCourseName(), existingPromo.getName(), existingPromo.getEndDate());
                    
                    throw new AppException(ErrorCode.COURSE_ALREADY_IN_ACTIVE_PROMOTION);
                }
            }
        }
    }
}
