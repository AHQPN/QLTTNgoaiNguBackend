package org.example.qlttngoaingu.controller;


import org.example.qlttngoaingu.dto.request.CourseRegistrationRequest;
import org.example.qlttngoaingu.dto.response.ApiResponse;
import org.example.qlttngoaingu.dto.response.InvoiceResponse;
import org.example.qlttngoaingu.dto.response.VNPayCreatePaymentResponse;
import org.example.qlttngoaingu.service.CourseRegistrationService;
import org.example.qlttngoaingu.service.VNPayService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrderController {
    private final CourseRegistrationService  courseRegistrationService;
    private final VNPayService vnPayService;

    @PostMapping
    public ResponseEntity<ApiResponse> RegisterClass(@RequestBody  CourseRegistrationRequest courseRegistrationRequest) {

        return ResponseEntity.ok().body(ApiResponse.builder().data(courseRegistrationService.registerCourses(courseRegistrationRequest)).build());
    }

    /**
     * Create order (Invoice) and return VNPay payUrl in one step to avoid user forgetting invoiceId mapping
     */
//    @PostMapping("/register-and-pay")
//    public ResponseEntity<ApiResponse<VNPayCreatePaymentResponse>> registerAndPay(@RequestBody CourseRegistrationRequest courseRegistrationRequest, HttpServletRequest request) {
//        InvoiceResponse invoiceResp = courseRegistrationService.registerCourses(courseRegistrationRequest);
//        // Create VNPay payment with invoiceId
//        VNPayCreatePaymentResponse payResp = vnPayService.createPaymentRequest(String.valueOf(invoiceResp.getTotalAmount().longValue()), "Invoice " + invoiceResp.getInvoiceId(), invoiceResp.getInvoiceId(), request);
//        return ResponseEntity.ok().body(ApiResponse.<VNPayCreatePaymentResponse>builder().data(payResp).build());
//    }


}
