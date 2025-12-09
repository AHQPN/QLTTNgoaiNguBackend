package org.example.qlttngoaingu.controller;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.example.qlttngoaingu.dto.request.CourseRegistrationRequest;
import org.example.qlttngoaingu.dto.request.VNPayCreatePaymentRequest;
import org.example.qlttngoaingu.dto.response.ApiResponse;
import org.example.qlttngoaingu.dto.response.InvoiceListResponse;
import org.example.qlttngoaingu.dto.response.InvoicePageResponse;
import org.example.qlttngoaingu.dto.response.InvoiceResponse;
import org.example.qlttngoaingu.dto.response.VNPayCreatePaymentResponse;
import org.example.qlttngoaingu.entity.Invoice;
import org.example.qlttngoaingu.exception.AppException;
import org.example.qlttngoaingu.exception.ErrorCode;
import org.example.qlttngoaingu.mapper.InvoiceMapper;
import org.example.qlttngoaingu.repository.InvoiceRepository;
import org.example.qlttngoaingu.security.model.UserDetailsImpl;
import org.example.qlttngoaingu.service.CourseRegistrationService;
import org.example.qlttngoaingu.service.VNPayService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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
    private final InvoiceMapper invoiceMapper;

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
     * @param platform "mobile" nếu request từ mobile app, để redirect về deep link
     */
    @PostMapping("/payment/create")
    public ResponseEntity<ApiResponse<VNPayCreatePaymentResponse>> createPayment(
            @RequestBody @Valid VNPayCreatePaymentRequest request,
            @RequestParam(required = false) String platform,
            @RequestHeader(value = "X-User-Role", required = false) String userRole,
            HttpServletRequest httpRequest) {

        // Check if request is from mobile app
        boolean isMobile = "mobile".equalsIgnoreCase(platform);
        
        // Default userRole to ADMIN if not provided
        if (userRole == null || userRole.isEmpty()) {
            userRole = "ADMIN";
        }

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

        // Tạo payment URL với invoiceId (với platform=mobile và userRole nếu từ mobile app)
        VNPayCreatePaymentResponse paymentResponse = vnPayService.createPayment(
                amount,
                request.getOrderInfo(),
                request.getInvoiceId(),
                ipAddress,
                isMobile,
                userRole);

        log.info("Created VNPay payment URL for invoice: {}, amount: {}, platform: {}, userRole: {}", 
                request.getInvoiceId(), amount, isMobile ? "mobile" : "web", userRole);

        return ResponseEntity.ok(ApiResponse.<VNPayCreatePaymentResponse>builder()
                .data(paymentResponse)
                .build());
    }

    /**
     * Thanh toán tiền mặt - Xác nhận thanh toán trực tiếp tại quầy
     * Chỉ nhân viên/admin mới có quyền gọi endpoint này
     */
    @PostMapping("/payment/cash")
    public ResponseEntity<ApiResponse<InvoiceResponse>> payByCash(
            @RequestParam Integer invoiceId,
            @RequestParam(required = false) String note) {
        
        // Kiểm tra hóa đơn tồn tại
        Invoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new AppException(ErrorCode.INVOICE_NOT_FOUND));

        // Kiểm tra hóa đơn đã thanh toán chưa
        if (Boolean.TRUE.equals(invoice.getStatus())) {
            throw new AppException(ErrorCode.INVOICE_ALREADY_PAID);
        }

        // Cập nhật trạng thái hóa đơn
        invoice.setStatus(true); // true = đã thanh toán
        invoiceRepository.save(invoice);
        
        log.info("Invoice {} paid by cash. Amount: {}, Note: {}", 
                invoiceId, invoice.getTotalAmount(), note);

        // Map to response
        InvoiceResponse response = new InvoiceResponse();
        response.setInvoiceId(invoice.getInvoiceId());
        response.setTotalAmount(invoice.getTotalAmount());
        response.setPaymentMethod(invoice.getPaymentMethod().getName());
        response.setDateCreated(invoice.getDateCreated());
        response.setStatus(true); // Boolean: true = đã thanh toán

        return ResponseEntity.ok(ApiResponse.<InvoiceResponse>builder()
                .message("Xác nhận thanh toán tiền mặt thành công")
                .data(response)
                .build());
    }

    /**
     * Callback từ VNPay sau khi thanh toán
     * Xác thực chữ ký, cập nhật trạng thái hóa đơn và redirect về frontend hoặc mobile app
     * Sử dụng query param ?platform=mobile để redirect về deep link của mobile app
     */
    @GetMapping("/payment/vnpay-return")
    public void vnpayReturn(HttpServletRequest request, HttpServletResponse response) throws IOException {
        int paymentStatus = vnPayService.verifyPayment(request);
        Integer invoiceId = vnPayService.getInvoiceIdFromRequest(request);

        String vnpResponseCode = request.getParameter("vnp_ResponseCode");
        String vnpTransactionNo = request.getParameter("vnp_TransactionNo");
        String vnpAmount = request.getParameter("vnp_Amount");
        
        // Check if request is from mobile app (via platform param or User-Agent)
        String platform = request.getParameter("platform");
        boolean isMobile = "mobile".equalsIgnoreCase(platform);
        
        // Get user role from query param (passed when creating payment URL)
        String userRole = request.getParameter("userRole");
        if (userRole == null || userRole.isEmpty()) {
            userRole = "ADMIN"; // Default to ADMIN if not specified
        }

        log.info("VNPay return - invoiceId: {}, responseCode: {}, transactionNo: {}, status: {}, platform: {}, userRole: {}",
                invoiceId, vnpResponseCode, vnpTransactionNo, paymentStatus, isMobile ? "mobile" : "web", userRole);

        // Use mobile deep link URL if request is from mobile app
        String baseUrl = isMobile ? vnPayService.getMobileRedirectUrl() : vnPayService.getFrontendRedirectUrl();
        String redirectUrl;

        if (paymentStatus == 1 && invoiceId != null) {
            // Thanh toán thành công - kiểm tra hóa đơn còn hợp lệ không
            Invoice invoice = invoiceRepository.findById(invoiceId).orElse(null);

            if (invoice == null) {
                redirectUrl = baseUrl
                        + "?status=failed"
                        + "&error=" + URLEncoder.encode("Không tìm thấy hóa đơn", StandardCharsets.UTF_8)
                        + "&userRole=" + userRole;
            } else if (Boolean.TRUE.equals(invoice.getStatus())) {
                // Hóa đơn đã được thanh toán rồi
                redirectUrl = baseUrl
                        + "?status=success"
                        + "&invoiceId=" + invoiceId
                        + "&message="
                        + URLEncoder.encode("Hóa đơn đã được thanh toán trước đó", StandardCharsets.UTF_8)
                        + "&userRole=" + userRole;
            } else {
                // Kiểm tra hóa đơn có hết hạn không
                LocalDateTime expiryTime = invoice.getDateCreated().plusMinutes(PAYMENT_TIMEOUT_MINUTES);
                if (LocalDateTime.now().isAfter(expiryTime)) {
                    log.warn("Payment for expired invoice {} rejected", invoiceId);
                    redirectUrl = baseUrl
                            + "?status=failed"
                            + "&error=" + URLEncoder.encode("Hóa đơn đã hết hạn thanh toán", StandardCharsets.UTF_8)
                            + "&userRole=" + userRole;
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
                            + "&responseCode=" + vnpResponseCode
                            + "&userRole=" + userRole;
                }
            }
        } else {
            // Thanh toán thất bại hoặc chữ ký không hợp lệ
            String errorMessage = getErrorMessage(paymentStatus, vnpResponseCode);
            redirectUrl = baseUrl
                    + "?status=failed"
                    + "&invoiceId=" + (invoiceId != null ? invoiceId : "")
                    + "&error=" + URLEncoder.encode(errorMessage, StandardCharsets.UTF_8)
                    + "&responseCode=" + (vnpResponseCode != null ? vnpResponseCode : "")
                    + "&userRole=" + userRole;
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
    public ResponseEntity<ApiResponse<InvoicePageResponse>> getAllInvoices(
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "15") Integer size,
            @RequestParam(required = false) Boolean status,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) LocalDate fromDate,
            @RequestParam(required = false) LocalDate toDate) {
        
        Pageable pageable = PageRequest.of(page, size);
        
        // Convert LocalDate to LocalDateTime for precise date range filtering
        // fromDate: start of day (00:00:00)
        // toDate: use next day start (exclusive) to include all records on toDate
        LocalDateTime fromDateTime = fromDate != null ? fromDate.atStartOfDay() : null;
        LocalDateTime toDateTime = toDate != null ? toDate.plusDays(1).atStartOfDay() : null;
        
        // Use advanced filter query
        Page<Invoice> invoices = invoiceRepository.searchInvoicesWithFilters(
                keyword != null && !keyword.trim().isEmpty() ? keyword.trim() : null,
                status,
                fromDateTime,
                toDateTime,
                pageable
        );
        
        // Map to InvoiceListResponse (minimal fields)
        List<InvoiceListResponse> invoiceList = invoices.getContent().stream()
                .map(invoiceMapper::toInvoiceListResponse)
                .toList();
        
        InvoicePageResponse response = new InvoicePageResponse(
                invoiceList,
                invoices.getNumber(),
                invoices.getTotalElements(),
                invoices.getTotalPages()
        );
        
        return ResponseEntity.ok(ApiResponse.<InvoicePageResponse>builder()
                .data(response)
                .message("Lấy danh sách hóa đơn thành công")
                .build());
    }
    
    /**
     * GET /orders/{id}
     * Xem chi tiết hóa đơn
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<InvoiceResponse>> getInvoiceById(@PathVariable Integer id) {
        Invoice invoice = invoiceRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.INVOICE_NOT_FOUND));
        
        InvoiceResponse response = invoiceMapper.toInvoiceResponse(invoice);
        
        return ResponseEntity.ok(ApiResponse.<InvoiceResponse>builder()
                .data(response)
                .message("Lấy chi tiết hóa đơn thành công")
                .build());
    }
    
    /**
     * GET /orders/student/my-invoices
     * Học viên xem danh sách hóa đơn của mình
     */
    @GetMapping("/student/my-invoices")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<ApiResponse<Page<InvoiceResponse>>> getMyInvoices(
            @AuthenticationPrincipal UserDetailsImpl principal,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        
        // Lấy studentId từ userId
        Integer studentId = principal.getId(); // Assuming this maps to student ID
        
        Pageable pageable = PageRequest.of(page, size);
        Page<Invoice> invoices = invoiceRepository.findByStudent_IdOrderByDateCreatedDesc(studentId, pageable);
        Page<InvoiceResponse> response = invoices.map(invoiceMapper::toInvoiceResponse);
        
        return ResponseEntity.ok(ApiResponse.<Page<InvoiceResponse>>builder()
                .data(response)
                .message("Lấy danh sách hóa đơn thành công")
                .build());
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
            case "00" -> "Thanh toán thành công";
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
