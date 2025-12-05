package org.example.qlttngoaingu.controller;

import org.example.qlttngoaingu.dto.request.PromotionRequest;
import org.example.qlttngoaingu.dto.response.ApiResponse;
import org.example.qlttngoaingu.dto.response.PromotionResponse;
import org.example.qlttngoaingu.service.PromotionService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import java.util.List;

@RestController
@RequestMapping("/promotions")
@RequiredArgsConstructor
public class PromotionController {

    private final PromotionService promotionService;

    /**
     * Tạo chương trình khuyến mãi mới
     */
    @PostMapping
    public ResponseEntity<ApiResponse<PromotionResponse>> createPromotion(
            @RequestBody @Valid PromotionRequest request) {
        PromotionResponse promotion = promotionService.createPromotion(request);
        return ResponseEntity.ok(ApiResponse.<PromotionResponse>builder()
                .data(promotion)
                .message("Promotion created successfully")
                .build());
    }

    /**
     * Cập nhật chương trình khuyến mãi
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<PromotionResponse>> updatePromotion(
            @PathVariable Integer id,
            @RequestBody @Valid PromotionRequest request) {
        PromotionResponse promotion = promotionService.updatePromotion(id, request);
        return ResponseEntity.ok(ApiResponse.<PromotionResponse>builder()
                .data(promotion)
                .message("Promotion updated successfully")
                .build());
    }

    /**
     * Lấy chi tiết chương trình khuyến mãi
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<PromotionResponse>> getPromotion(@PathVariable Integer id) {
        PromotionResponse promotion = promotionService.getPromotion(id);
        return ResponseEntity.ok(ApiResponse.<PromotionResponse>builder()
                .data(promotion)
                .build());
    }

    /**
     * Lấy danh sách tất cả promotions với phân trang
     */
    @GetMapping
    public ResponseEntity<ApiResponse<Page<PromotionResponse>>> getAllPromotions(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDirection) {
        
        Sort.Direction direction = sortDirection.equalsIgnoreCase("ASC") ? Sort.Direction.ASC : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        
        Page<PromotionResponse> promotions = promotionService.getAllPromotions(pageable);
        return ResponseEntity.ok(ApiResponse.<Page<PromotionResponse>>builder()
                .data(promotions)
                .build());
    }

    /**
     * Lấy danh sách promotions đang active
     */
    @GetMapping("/active")
    public ResponseEntity<ApiResponse<List<PromotionResponse>>> getActivePromotions() {
        List<PromotionResponse> promotions = promotionService.getActivePromotions();
        return ResponseEntity.ok(ApiResponse.<List<PromotionResponse>>builder()
                .data(promotions)
                .build());
    }

    /**
     * Bật/tắt trạng thái promotion (soft delete)
     */
    @PatchMapping("/{id}/toggle-status")
    public ResponseEntity<ApiResponse<PromotionResponse>> togglePromotionStatus(@PathVariable Integer id) {
        PromotionResponse promotion = promotionService.togglePromotionStatus(id);
        return ResponseEntity.ok(ApiResponse.<PromotionResponse>builder()
                .data(promotion)
                .message("Promotion status toggled successfully")
                .build());
    }
}
