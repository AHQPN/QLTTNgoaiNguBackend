package org.example.qlttngoaingu.controller;

import org.example.qlttngoaingu.dto.request.CartPreviewRequest;
import org.example.qlttngoaingu.dto.response.ApiResponse;
import org.example.qlttngoaingu.dto.response.CartPreviewResponse;
import org.example.qlttngoaingu.security.model.UserDetailsImpl;
import org.example.qlttngoaingu.service.CartService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    /**
     * Preview giỏ hàng với promotion
     */
    @PostMapping("/preview")
    public ApiResponse<CartPreviewResponse> previewCart(
            @RequestBody CartPreviewRequest request,
            @AuthenticationPrincipal UserDetailsImpl authentication) {
        

        CartPreviewResponse response = cartService.previewCart(request, authentication.getId());
        
        return ApiResponse.<CartPreviewResponse>builder()
                .code(1000)
                .data(response)
                .build();
    }
}
