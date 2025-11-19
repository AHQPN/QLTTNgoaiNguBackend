package org.example.qlttngoaingu.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.qlttngoaingu.dto.request.MomoCreatePaymentRequest;
import org.example.qlttngoaingu.dto.response.ApiResponse;
import org.example.qlttngoaingu.dto.response.MomoCheckStatusResponse;
import org.example.qlttngoaingu.dto.response.MomoCreatePaymentResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.example.qlttngoaingu.service.MomoService;


@RequestMapping("/payment")
@RestController
@RequiredArgsConstructor
@Slf4j
public class PaymentController {


    private final MomoService momoService;

    /**
     * Tạo payment request
     */
    @PostMapping("/momo/create")
    public ApiResponse<MomoCreatePaymentResponse> createPayment(
            @Valid @RequestBody MomoCreatePaymentRequest request) {

        log.info("Received payment creation request for amount: {}", request.getAmount());

        MomoCreatePaymentResponse response = momoService.createPaymentRequest(
                request.getAmount(),
                request.getOrderInfo() != null ? request.getOrderInfo() : "Payment"
        );

        return ApiResponse.<MomoCreatePaymentResponse>builder()
                .data(response)
                .build();
    }

    /**
     * Kiểm tra trạng thái thanh toán
     */
    @GetMapping("/momo/status/{orderId}")
    public ApiResponse<MomoCheckStatusResponse> checkPaymentStatus(
            @PathVariable String orderId) {

        log.info("Received status check request for orderId: {}", orderId);

        MomoCheckStatusResponse response = momoService.checkPaymentStatus(orderId);

        return ApiResponse.<MomoCheckStatusResponse>builder()
                .data(response)
                .build();
    }

}