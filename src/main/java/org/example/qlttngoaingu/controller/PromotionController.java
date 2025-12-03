package org.example.qlttngoaingu.controller;

import lombok.RequiredArgsConstructor;
import org.example.qlttngoaingu.dto.response.ApiResponse;
import org.example.qlttngoaingu.entity.Promotion;
import org.example.qlttngoaingu.entity.PromotionDetail;
import org.example.qlttngoaingu.repository.PromotionRepository;
import org.example.qlttngoaingu.repository.PromotionDetailRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/promotions")
@RequiredArgsConstructor
public class PromotionController {

    private final PromotionRepository promotionRepository;
    private final PromotionDetailRepository promotionDetailRepository;

    /**
     * Lấy danh sách tất cả khuyến mãi đang hoạt động
     */
    @GetMapping("/active")
    public ResponseEntity<ApiResponse<List<PromotionResponse>>> getActivePromotions() {
        List<Promotion> activePromotions = promotionRepository.findAllActivePromotions(LocalDate.now());
        
        List<PromotionResponse> response = activePromotions.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(ApiResponse.<List<PromotionResponse>>builder()
                .code(1000)
                .message("Lấy danh sách khuyến mãi thành công")
                .data(response)
                .build());
    }

    /**
     * Lấy tất cả khuyến mãi (bao gồm cả đã hết hạn)
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<PromotionResponse>>> getAllPromotions() {
        List<Promotion> promotions = promotionRepository.findAll();
        
        List<PromotionResponse> response = promotions.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(ApiResponse.<List<PromotionResponse>>builder()
                .code(1000)
                .message("Lấy danh sách khuyến mãi thành công")
                .data(response)
                .build());
    }

    /**
     * Lấy chi tiết một khuyến mãi theo ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<PromotionResponse>> getPromotionById(@PathVariable Integer id) {
        Promotion promotion = promotionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy khuyến mãi với ID: " + id));
        
        return ResponseEntity.ok(ApiResponse.<PromotionResponse>builder()
                .code(1000)
                .message("Lấy chi tiết khuyến mãi thành công")
                .data(mapToResponse(promotion))
                .build());
    }

    /**
     * Kiểm tra mã khuyến mãi có hợp lệ không
     */
    @GetMapping("/validate")
    public ResponseEntity<ApiResponse<PromotionResponse>> validatePromotionCode(
            @RequestParam String code) {
        List<Promotion> activePromotions = promotionRepository.findAllActivePromotions(LocalDate.now());
        
        Promotion promotion = activePromotions.stream()
                .filter(p -> p.getName() != null && p.getName().equalsIgnoreCase(code))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Mã khuyến mãi không hợp lệ hoặc đã hết hạn"));
        
        return ResponseEntity.ok(ApiResponse.<PromotionResponse>builder()
                .code(1000)
                .message("Mã khuyến mãi hợp lệ")
                .data(mapToResponse(promotion))
                .build());
    }

    /**
     * Lấy khuyến mãi áp dụng cho một khóa học cụ thể
     */
    @GetMapping("/course/{courseId}")
    public ResponseEntity<ApiResponse<List<PromotionResponse>>> getPromotionsByCourse(
            @PathVariable Integer courseId) {
        List<Promotion> promotions = promotionRepository.findValidPromotionsByCourseAndType1(
                courseId, LocalDate.now());
        
        List<PromotionResponse> response = promotions.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(ApiResponse.<List<PromotionResponse>>builder()
                .code(1000)
                .message("Lấy danh sách khuyến mãi theo khóa học thành công")
                .data(response)
                .build());
    }

    private PromotionResponse mapToResponse(Promotion promotion) {
        LocalDate today = LocalDate.now();
        boolean isExpired = promotion.getEndDate() != null && today.isAfter(promotion.getEndDate());
        boolean isNotStarted = promotion.getStartDate() != null && today.isBefore(promotion.getStartDate());
        
        String status;
        if (!Boolean.TRUE.equals(promotion.getActive())) {
            status = "inactive";
        } else if (isExpired) {
            status = "expired";
        } else if (isNotStarted) {
            status = "upcoming";
        } else {
            status = "active";
        }

        // Lấy danh sách khóa học được áp dụng từ bảng chitietkhuyenmai
        List<PromotionDetail> promotionDetails = promotionDetailRepository.findByPromotion(promotion);
        List<Integer> applicableCourseIds = promotionDetails.stream()
                .filter(pd -> pd.getCourse() != null)
                .map(pd -> pd.getCourse().getCourseId())
                .collect(Collectors.toList());
        
        List<String> applicableCourseNames = promotionDetails.stream()
                .filter(pd -> pd.getCourse() != null)
                .map(pd -> pd.getCourse().getCourseName())
                .collect(Collectors.toList());
        
        return PromotionResponse.builder()
                .id(promotion.getId())
                .code(promotion.getName()) // Dùng name như là code
                .name(promotion.getName())
                .description(promotion.getDescription())
                .discountPercent(promotion.getDiscountPercent())
                .startDate(promotion.getStartDate())
                .endDate(promotion.getEndDate())
                .active(promotion.getActive())
                .status(status)
                .promotionTypeId(promotion.getPromotionType() != null ? promotion.getPromotionType().getId() : null)
                .promotionTypeName(promotion.getPromotionType() != null ? promotion.getPromotionType().getName() : null)
                .applicableCourseIds(applicableCourseIds)
                .applicableCourseNames(applicableCourseNames)
                .build();
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class PromotionResponse {
        private Integer id;
        private String code;
        private String name;
        private String description;
        private Integer discountPercent;
        private LocalDate startDate;
        private LocalDate endDate;
        private Boolean active;
        private String status; // active, expired, upcoming, inactive
        private Integer promotionTypeId;
        private String promotionTypeName;
        private List<Integer> applicableCourseIds;
        private List<String> applicableCourseNames;
    }
}
