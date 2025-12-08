package org.example.qlttngoaingu.controller;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;

import org.example.qlttngoaingu.dto.request.CourseRegistrationRequest;
import org.example.qlttngoaingu.dto.request.VNPayCreatePaymentRequest;
import org.example.qlttngoaingu.dto.response.ApiResponse;
import org.example.qlttngoaingu.dto.response.InvoiceResponse;
import org.example.qlttngoaingu.dto.response.VNPayCreatePaymentResponse;
import org.example.qlttngoaingu.entity.Invoice;
import org.example.qlttngoaingu.exception.AppException;
import org.example.qlttngoaingu.exception.ErrorCode;
import org.example.qlttngoaingu.repository.InvoiceRepository;
import org.example.qlttngoaingu.service.CourseRegistrationService;
import org.example.qlttngoaingu.service.VNPayService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
@Slf4j
public class OrderController {
    private final CourseRegistrationService courseRegistrationService;
    private final VNPayService vnPayService;
    private final InvoiceRepository invoiceRepository;
    private final org.example.qlttngoaingu.service.InvoiceEmailService invoiceEmailService;

    private static final int PAYMENT_TIMEOUT_MINUTES = 15; // Thời gian cho phép thanh toán: 15 phút

    /**
     * Đăng ký khóa học - Tạo hóa đơn
     */
    @PostMapping
    public ResponseEntity<ApiResponse<InvoiceResponse>> registerClass(
            @RequestBody CourseRegistrationRequest courseRegistrationRequest) {

        InvoiceResponse invoice = courseRegistrationService.registerCourses(courseRegistrationRequest);

        log.info("Created invoice {} with amount {}, payment deadline: {} minutes",
                invoice.getInvoiceId(), invoice.getTotalAmount(), PAYMENT_TIMEOUT_MINUTES);

        return ResponseEntity.ok().body(ApiResponse.<InvoiceResponse>builder()
                .data(invoice)
                .message("Đăng ký thành công. Vui lòng thanh toán trong vòng " + PAYMENT_TIMEOUT_MINUTES + " phút")
                .build());
    }

    /**
     * Tạo URL thanh toán VNPay cho hóa đơn
     * Kiểm tra hóa đơn còn trong thời hạn thanh toán không
     */
    @PostMapping("/payment/create")
    public ResponseEntity<ApiResponse<VNPayCreatePaymentResponse>> createPayment(
            @RequestBody @Valid VNPayCreatePaymentRequest request,
            HttpServletRequest httpRequest) {

        // Kiểm tra hóa đơn tồn tại
        Invoice invoice = invoiceRepository.findById(request.getInvoiceId())
                .orElseThrow(() -> new AppException(ErrorCode.INVOICE_NOT_FOUND));

        // Kiểm tra hóa đơn đã thanh toán chưa
        if (Boolean.TRUE.equals(invoice.getStatus())) {
            throw new AppException(ErrorCode.INVOICE_ALREADY_PAID);
        }

        // Kiểm tra hóa đơn đã hết hạn chưa (15 phút)
        LocalDateTime expiryTime = invoice.getDateCreated().plusMinutes(PAYMENT_TIMEOUT_MINUTES);
        if (LocalDateTime.now().isAfter(expiryTime)) {
            log.warn("Invoice {} expired. Created at: {}, Expiry: {}",
                    invoice.getInvoiceId(), invoice.getDateCreated(), expiryTime);
            throw new AppException(ErrorCode.INVOICE_EXPIRED);
        }

        // Lấy IP address của client
        String ipAddress = getClientIpAddress(httpRequest);

        // Parse amount
        long amount = Long.parseLong(request.getAmount());

        // Tạo payment URL với invoiceId
        VNPayCreatePaymentResponse paymentResponse = vnPayService.createPayment(
                amount,
                request.getOrderInfo(),
                request.getInvoiceId(),
                ipAddress);

        log.info("Created VNPay payment URL for invoice: {}, amount: {}", request.getInvoiceId(), amount);

        return ResponseEntity.ok(ApiResponse.<VNPayCreatePaymentResponse>builder()
                .data(paymentResponse)
                .build());
    }

    /**
     * Callback từ VNPay sau khi thanh toán
     * Xác thực chữ ký, cập nhật trạng thái hóa đơn và redirect về frontend
     */
    @GetMapping("/payment/vnpay-return")
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
            // Thanh toán thành công - kiểm tra hóa đơn còn hợp lệ không
            Invoice invoice = invoiceRepository.findById(invoiceId).orElse(null);

            if (invoice == null) {
                redirectUrl = baseUrl
                        + "?status=failed"
                        + "&error=" + URLEncoder.encode("Không tìm thấy hóa đơn", StandardCharsets.UTF_8);
            } else if (Boolean.TRUE.equals(invoice.getStatus())) {
                // Hóa đơn đã được thanh toán rồi
                redirectUrl = baseUrl
                        + "?status=success"
                        + "&invoiceId=" + invoiceId
                        + "&message="
                        + URLEncoder.encode("Hóa đơn đã được thanh toán trước đó", StandardCharsets.UTF_8);
            } else {
                // Kiểm tra hóa đơn có hết hạn không
                LocalDateTime expiryTime = invoice.getDateCreated().plusMinutes(PAYMENT_TIMEOUT_MINUTES);
                if (LocalDateTime.now().isAfter(expiryTime)) {
                    log.warn("Payment for expired invoice {} rejected", invoiceId);
                    redirectUrl = baseUrl
                            + "?status=failed"
                            + "&error=" + URLEncoder.encode("Hóa đơn đã hết hạn thanh toán", StandardCharsets.UTF_8);
                } else {
                    // Cập nhật trạng thái hóa đơn
                    invoice.setStatus(true); // true = đã thanh toán
                    invoiceRepository.save(invoice);
                    log.info("Invoice {} updated to paid status", invoiceId);

                    // Gửi email hóa đơn cho học viên
                    invoiceEmailService.sendInvoiceEmail(invoice);

                    redirectUrl = baseUrl
                            + "?status=success"
                            + "&invoiceId=" + invoiceId
                            + "&transactionNo=" + (vnpTransactionNo != null ? vnpTransactionNo : "")
                            + "&amount=" + (vnpAmount != null ? Long.parseLong(vnpAmount) / 100 : 0)
                            + "&responseCode=" + vnpResponseCode;
                }
            }
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
     * Xác nhận thanh toán tiền mặt - Cập nhật trạng thái và gửi hóa đơn
     * Dùng cho phương thức thanh toán tiền mặt (paymentMethodId = 1)
     */
    @PostMapping("/payment/confirm-cash")
    public ResponseEntity<ApiResponse> confirmCashPayment(@RequestParam Integer invoiceId) {
        // Kiểm tra hóa đơn tồn tại
        Invoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new AppException(ErrorCode.INVOICE_NOT_FOUND));

        // Kiểm tra hóa đơn đã thanh toán chưa
        if (Boolean.TRUE.equals(invoice.getStatus())) {
            return ResponseEntity.ok(ApiResponse.builder()
                    .message("Hóa đơn đã được thanh toán trước đó")
                    .data(invoiceId)
                    .build());
        }

        // Cập nhật trạng thái hóa đơn thành đã thanh toán
        invoice.setStatus(true);
        invoiceRepository.save(invoice);
        log.info("Cash payment confirmed for invoice {}", invoiceId);

        // Gửi hóa đơn qua email
        try {
            invoiceEmailService.sendInvoiceEmail(invoice);
            log.info("Invoice email sent for invoice {}", invoiceId);
        } catch (Exception e) {
            log.warn("Failed to send invoice email for invoice {}: {}", invoiceId, e.getMessage());
            // Không throw exception - thanh toán đã thành công, email chỉ là optional
        }

        return ResponseEntity.ok(ApiResponse.builder()
                .message("Thanh toán tiền mặt thành công")
                .data(invoiceId)
                .build());
    }

    @GetMapping
    public ResponseEntity<ApiResponse> getOrders(@RequestParam Integer page, @RequestParam Integer size) {
        return ResponseEntity.ok().body(ApiResponse.builder().data(null).build());
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
