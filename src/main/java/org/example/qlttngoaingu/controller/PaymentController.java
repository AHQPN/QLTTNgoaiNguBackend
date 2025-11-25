package org.example.qlttngoaingu.controller;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.qlttngoaingu.dto.request.MomoCreatePaymentRequest;
import org.example.qlttngoaingu.dto.request.MomoIPNRequest;
import org.example.qlttngoaingu.dto.response.MomoIPNResponse;
import org.example.qlttngoaingu.dto.response.ApiResponse;
import org.example.qlttngoaingu.dto.response.MomoCheckStatusResponse;
import org.example.qlttngoaingu.dto.response.MomoCreatePaymentResponse;
import org.springframework.web.bind.annotation.*;
import org.example.qlttngoaingu.service.MomoService;

import java.io.IOException;


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

    @PostMapping("/notify")
    public MomoIPNResponse handleIPNNotification(@RequestBody MomoIPNRequest request) {
        log.info("Received IPN notification from MoMo for orderId: {}", request.getOrderId());

        try {
            // Xử lý thông báo từ MoMo
            momoService.processIPNNotification(request);

            // Trả về response cho MoMo biết đã nhận được
            return MomoIPNResponse.builder()
                    .partnerCode(request.getPartnerCode())
                    .orderId(request.getOrderId())
                    .requestId(request.getRequestId())
                    .resultCode(0)
                    .message("Success")
                    .responseTime(System.currentTimeMillis())
                    .build();

        } catch (Exception e) {
            log.error("Error processing IPN notification", e);

            return MomoIPNResponse.builder()
                    .partnerCode(request.getPartnerCode())
                    .orderId(request.getOrderId())
                    .requestId(request.getRequestId())
                    .resultCode(-1)
                    .message("Error: " + e.getMessage())
                    .responseTime(System.currentTimeMillis())
                    .build();
        }
    }

    /**
     * Return URL - User được redirect về đây sau khi thanh toán
     * Frontend sẽ gọi endpoint này
     */
    @GetMapping("/return")
    public void handleReturnUrl(
            @RequestParam String partnerCode,
            @RequestParam String orderId,
            @RequestParam String requestId,
            @RequestParam String amount,
            @RequestParam String orderInfo,
            @RequestParam String orderType,
            @RequestParam String transId,
            @RequestParam String resultCode,
            @RequestParam String message,
            @RequestParam String payType,
            @RequestParam String responseTime,
            @RequestParam String extraData,
            @RequestParam String signature,
            HttpServletResponse response
    ) throws IOException {
        log.info("User returned from MoMo payment. OrderId: {}, ResultCode: {}", orderId, resultCode);

        // Redirect về frontend với kết quả
        String frontendUrl = "http://localhost:3000/payment/result";
        String redirectUrl = String.format(
                "%s?orderId=%s&resultCode=%s&message=%s&amount=%s&transId=%s",
                frontendUrl, orderId, resultCode, message, amount, transId
        );

        response.sendRedirect(redirectUrl);
    }

}