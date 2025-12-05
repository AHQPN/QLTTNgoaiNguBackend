package org.example.qlttngoaingu.controller;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import org.example.qlttngoaingu.dto.request.VNPayCreatePaymentRequest;
import org.example.qlttngoaingu.dto.response.ApiResponse;
import org.example.qlttngoaingu.dto.response.VNPayCreatePaymentResponse;
import org.example.qlttngoaingu.entity.Invoice;
import org.example.qlttngoaingu.repository.InvoiceRepository;
import org.example.qlttngoaingu.service.VNPayService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RequestMapping("/payment")
@RestController
@RequiredArgsConstructor
@Slf4j
public class PaymentController {

    private final VNPayService vnPayService;
    private final InvoiceRepository invoiceRepository;

    /**
     * API tạo URL thanh toán VNPay
     * @param request thông tin thanh toán (amount, orderInfo, invoiceId)
     * @param httpRequest HttpServletRequest để lấy IP
     * @return VNPayCreatePaymentResponse chứa payUrl để redirect
     */
    @PostMapping("/create")
    public ResponseEntity<ApiResponse<VNPayCreatePaymentResponse>> createPayment(
            @RequestBody @Valid VNPayCreatePaymentRequest request,
            HttpServletRequest httpRequest) {

        // Lấy IP address của client
        String ipAddress = getClientIpAddress(httpRequest);
        
        // Parse amount
        long amount = Long.parseLong(request.getAmount());
        
        // Tạo payment URL với invoiceId
        VNPayCreatePaymentResponse paymentResponse = vnPayService.createPayment(
                amount,
                request.getOrderInfo(),
                request.getInvoiceId(),
                ipAddress
        );

        log.info("Created VNPay payment URL for invoice: {}, amount: {}", request.getInvoiceId(), amount);

        return ResponseEntity.ok(ApiResponse.<VNPayCreatePaymentResponse>builder()
                .data(paymentResponse)
                .build());
    }

    /**
     * API callback từ VNPay sau khi thanh toán
     * Xác thực chữ ký, cập nhật trạng thái hóa đơn và redirect về frontend
     */
    @GetMapping("/vnpay-return")
    public void vnpayReturn(HttpServletRequest request, HttpServletResponse response) throws IOException {
        int paymentStatus = vnPayService.verifyPayment(request);
        Integer invoiceId = vnPayService.getInvoiceIdFromRequest(request);
        
        String vnpResponseCode = request.getParameter("vnp_ResponseCode");
        String vnpTransactionNo = request.getParameter("vnp_TransactionNo");
        String vnpAmount = request.getParameter("vnp_Amount");
        
        log.info("VNPay return - invoiceId: {}, responseCode: {}, transactionNo: {}, status: {}", 
                invoiceId, vnpResponseCode, vnpTransactionNo, paymentStatus);

        String baseUrl = vnPayService.getFrontendRedirectUrl();
        String redirectUrl;
        
        if (paymentStatus == 1 && invoiceId != null) {
            // Thanh toán thành công - cập nhật trạng thái hóa đơn
            Invoice invoice = invoiceRepository.findById(invoiceId).orElse(null);
            if (invoice != null) {
                invoice.setStatus(true); // true = đã thanh toán
                invoiceRepository.save(invoice);
                log.info("Invoice {} updated to paid status", invoiceId);
            }
            
            // Redirect về frontend với status success
            redirectUrl = baseUrl
                    + "?status=success"
                    + "&invoiceId=" + invoiceId 
                    + "&transactionNo=" + (vnpTransactionNo != null ? vnpTransactionNo : "")
                    + "&amount=" + (vnpAmount != null ? Long.parseLong(vnpAmount) / 100 : 0)
                    + "&responseCode=" + vnpResponseCode;
        } else {
            // Thanh toán thất bại hoặc chữ ký không hợp lệ
            String errorMessage = getErrorMessage(paymentStatus, vnpResponseCode);
            redirectUrl = baseUrl
                    + "?status=failed"
                    + "&invoiceId=" + (invoiceId != null ? invoiceId : "")
                    + "&error=" + URLEncoder.encode(errorMessage, StandardCharsets.UTF_8)
                    + "&responseCode=" + (vnpResponseCode != null ? vnpResponseCode : "");
        }

        response.sendRedirect(redirectUrl);
    }

    /**
     * Lấy IP address của client, hỗ trợ cả trường hợp đứng sau proxy
     */
    private String getClientIpAddress(HttpServletRequest request) {
        String ipAddress = request.getHeader("X-Forwarded-For");
        if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getHeader("Proxy-Client-IP");
        }
        if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getRemoteAddr();
        }
        // Nếu có nhiều IP (qua nhiều proxy), lấy IP đầu tiên
        if (ipAddress != null && ipAddress.contains(",")) {
            ipAddress = ipAddress.split(",")[0].trim();
        }
        return ipAddress;
    }

    /**
     * Lấy thông báo lỗi dựa trên mã response từ VNPay
     */
    private String getErrorMessage(int paymentStatus, String responseCode) {
        if (paymentStatus == -1) {
            return "Chữ ký không hợp lệ";
        }
        
        if (responseCode == null) {
            return "Lỗi không xác định";
        }
        
        return switch (responseCode) {
            case "07" -> "Giao dịch bị nghi ngờ gian lận";
            case "09" -> "Thẻ/Tài khoản chưa đăng ký dịch vụ InternetBanking";
            case "10" -> "Xác thực thông tin thẻ/tài khoản không đúng quá 3 lần";
            case "11" -> "Đã hết hạn chờ thanh toán";
            case "12" -> "Thẻ/Tài khoản bị khóa";
            case "13" -> "Mã OTP không chính xác";
            case "24" -> "Giao dịch đã bị hủy";
            case "51" -> "Tài khoản không đủ số dư";
            case "65" -> "Vượt quá hạn mức giao dịch trong ngày";
            case "75" -> "Ngân hàng thanh toán đang bảo trì";
            case "79" -> "Nhập sai mật khẩu quá số lần quy định";
            case "99" -> "Lỗi không xác định";
            default -> "Thanh toán thất bại (Mã lỗi: " + responseCode + ")";
        };
    }
}
