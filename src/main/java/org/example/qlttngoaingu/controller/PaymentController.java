//package org.example.qlttngoaingu.controller;
//
//import java.io.IOException;
//import java.util.HashMap;
//import java.util.Map;
//
//import org.example.qlttngoaingu.dto.request.VNPayCreatePaymentRequest;
//import org.example.qlttngoaingu.dto.response.ApiResponse;
//import org.example.qlttngoaingu.dto.response.VNPayCreatePaymentResponse;
//import org.example.qlttngoaingu.service.VNPayService;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.*;
//
//import jakarta.servlet.http.HttpServletRequest;
//import jakarta.servlet.http.HttpServletResponse;
//import jakarta.validation.Valid;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//
//
//@RequestMapping("/payment")
//@RestController
//@RequiredArgsConstructor
//@Slf4j
//public class PaymentController {
//
//    private final VNPayService vnPayService;
//
//    @PostMapping("/create")
//    public ResponseEntity<?> createPayment(
//            @RequestParam long amount,
//            @RequestParam String orderInfo,
//            HttpServletRequest request) {Q
//
//        String ipAddress = getIpAddress(request);
//        String paymentUrl = vnPayService.createPaymentUrl(amount, orderInfo, ipAddress);
//
//        Map<String, String> response = new HashMap<>();
//        response.put("status", "success");
//        response.put("message", "Tạo URL thanh toán thành công");
//        response.put("paymentUrl", paymentUrl);
//
//        return ResponseEntity.ok(response);
//    }
//
//    @GetMapping("/vnpay/return")
//    public ResponseEntity<?> paymentReturn(HttpServletRequest request) {
//        int paymentStatus = vnPayService.orderReturn(request);
//
//        Map<String, Object> response = new HashMap<>();
//        response.put("orderInfo", request.getParameter("vnp_OrderInfo"));
//        response.put("paymentTime", request.getParameter("vnp_PayDate"));
//        response.put("transactionId", request.getParameter("vnp_TransactionNo"));
//        response.put("totalPrice", Long.parseLong(request.getParameter("vnp_Amount")) / 100);
//
//        if (paymentStatus == 1) {
//            response.put("status", "success");
//            response.put("message", "Thanh toán thành công");
//        } else if (paymentStatus == 0) {
//            response.put("status", "failed");
//            response.put("message", "Thanh toán thất bại");
//        } else {
//            response.put("status", "invalid");
//            response.put("message", "Chữ ký không hợp lệ");
//        }
//
//        return ResponseEntity.ok(response);
//    }
//
//    private String getIpAddress(HttpServletRequest request) {
//        String ipAddress = request.getHeader("X-FORWARDED-FOR");
//        if (ipAddress == null) {
//            ipAddress = request.getRemoteAddr();
//        }
//        return ipAddress;
//    }
//
//}