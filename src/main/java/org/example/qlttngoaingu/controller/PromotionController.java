package org.example.qlttngoaingu.controller;

import java.util.List;

import org.example.qlttngoaingu.dto.request.PromotionRequest;
import org.example.qlttngoaingu.dto.response.ApiResponse;
import org.example.qlttngoaingu.dto.response.PromotionResponse;
import org.example.qlttngoaingu.service.PromotionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/promotions")
@RequiredArgsConstructor
public class PromotionController {

    private final PromotionService promotionService;

    /**
     * Lấy danh sách tất cả khuyến mãi đang hoạt động
     */
    @GetMapping("/active")
    public ResponseEntity<ApiResponse<List<PromotionResponse>>> getActivePromotions() {
        List<PromotionResponse> response = promotionService.getActivePromotions();
        
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
        List<PromotionResponse> response = promotionService.getActivePromotions();
        
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
        PromotionResponse response = promotionService.getPromotion(id);
        
        return ResponseEntity.ok(ApiResponse.<PromotionResponse>builder()
                .code(1000)
                .message("Lấy chi tiết khuyến mãi thành công")
                .data(response)
                .build());
    }

    /**
     * Kiểm tra mã khuyến mãi có hợp lệ không
     */
    @GetMapping("/validate")
    public ResponseEntity<ApiResponse<PromotionResponse>> validatePromotionCode(
            @RequestParam String code) {
        PromotionResponse response = promotionService.validatePromotionCode(code);
        
        return ResponseEntity.ok(ApiResponse.<PromotionResponse>builder()
                .code(1000)
                .message("Mã khuyến mãi hợp lệ")
                .data(response)
                .build());
    }

    /**
     * Lấy khuyến mãi áp dụng cho một khóa học cụ thể
     */
    @GetMapping("/course/{courseId}")
    public ResponseEntity<ApiResponse<List<PromotionResponse>>> getPromotionsByCourse(
            @PathVariable Integer courseId) {
        List<PromotionResponse> response = promotionService.getPromotionsByCourse(courseId);
        
        return ResponseEntity.ok(ApiResponse.<List<PromotionResponse>>builder()
                .code(1000)
                .message("Lấy danh sách khuyến mãi theo khóa học thành công")
                .data(response)
                .build());
    }

    /**
     * Tạo khuyến mãi mới
     */
    @PostMapping
    public ResponseEntity<ApiResponse<PromotionResponse>> createPromotion(
            @RequestBody PromotionRequest request) {
        PromotionResponse response = promotionService.createPromotion(request);
        
        return ResponseEntity.ok(ApiResponse.<PromotionResponse>builder()
                .code(1000)
                .message("Tạo khuyến mãi thành công")
                .data(response)
                .build());
    }

    /**
     * Cập nhật khuyến mãi
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<PromotionResponse>> updatePromotion(
            @PathVariable Integer id,
            @RequestBody PromotionRequest request) {
        PromotionResponse response = promotionService.updatePromotion(id, request);
        
        return ResponseEntity.ok(ApiResponse.<PromotionResponse>builder()
                .code(1000)
                .message("Cập nhật khuyến mãi thành công")
                .data(response)
                .build());
    }

    /**
     * Bật/tắt trạng thái khuyến mãi (soft delete)
     */
    @PutMapping("/{id}/toggle")
    public ResponseEntity<ApiResponse<PromotionResponse>> togglePromotionStatus(@PathVariable Integer id) {
        PromotionResponse response = promotionService.togglePromotionStatus(id);
        
        return ResponseEntity.ok(ApiResponse.<PromotionResponse>builder()
                .code(1000)
                .message(response.getActive() ? "Đã bật khuyến mãi" : "Đã tắt khuyến mãi")
                .data(response)
                .build());
    }
}