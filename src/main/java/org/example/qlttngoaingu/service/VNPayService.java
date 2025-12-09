package org.example.qlttngoaingu.service;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

import org.example.qlttngoaingu.config.VNPayConfig;
import org.example.qlttngoaingu.dto.response.VNPayCreatePaymentResponse;
import org.example.qlttngoaingu.utils.VNPayUtil;
import org.springframework.stereotype.Service;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class VNPayService {

    private final VNPayConfig vnPayConfig;

    /**
     * Tạo URL thanh toán VNPay với invoiceId để tracking
     * @param amount số tiền (VND)
     * @param orderInfo thông tin đơn hàng
     * @param invoiceId mã hóa đơn
     * @param ipAddress IP của client
     * @return VNPayCreatePaymentResponse chứa txnRef, amount và payUrl
     */
    public VNPayCreatePaymentResponse createPayment(long amount, String orderInfo, Integer invoiceId, String ipAddress) {
        return createPayment(amount, orderInfo, invoiceId, ipAddress, false, null);
    }

    /**
     * Tạo URL thanh toán VNPay với invoiceId để tracking (có hỗ trợ mobile)
     * @param amount số tiền (VND)
     * @param orderInfo thông tin đơn hàng
     * @param invoiceId mã hóa đơn
     * @param ipAddress IP của client
     * @param isMobile true nếu request từ mobile app
     * @param userRole vai trò người dùng (ADMIN/STUDENT)
     * @return VNPayCreatePaymentResponse chứa txnRef, amount và payUrl
     */
    public VNPayCreatePaymentResponse createPayment(long amount, String orderInfo, Integer invoiceId, String ipAddress, boolean isMobile, String userRole) {
        String txnRef = String.valueOf(invoiceId); // Sử dụng invoiceId làm txnRef để dễ tracking
        
        // Add platform=mobile and userRole to return URL if request is from mobile app
        String returnUrl = vnPayConfig.getReturnUrl();
        if (isMobile) {
            returnUrl += (returnUrl.contains("?") ? "&" : "?") + "platform=mobile";
        }
        // Always add userRole to return URL for proper navigation after payment
        if (userRole != null && !userRole.isEmpty()) {
            returnUrl += (returnUrl.contains("?") ? "&" : "?") + "userRole=" + userRole;
        }
        
        // Chuẩn hóa orderInfo - loại bỏ dấu tiếng Việt và ký tự đặc biệt
        String safeOrderInfo = "Thanh toan hoa don " + invoiceId;
        
        Map<String, String> vnpParams = new HashMap<>();
        vnpParams.put("vnp_Version", vnPayConfig.getVersion());
        vnpParams.put("vnp_Command", vnPayConfig.getCommand());
        vnpParams.put("vnp_TmnCode", vnPayConfig.getTmnCode());
        vnpParams.put("vnp_Amount", String.valueOf(amount * 100)); // VNPay yêu cầu amount * 100
        vnpParams.put("vnp_CurrCode", vnPayConfig.getCurrencyCode());
        vnpParams.put("vnp_TxnRef", txnRef);
        vnpParams.put("vnp_OrderInfo", safeOrderInfo);
        vnpParams.put("vnp_OrderType", vnPayConfig.getOrderType());
        vnpParams.put("vnp_Locale", vnPayConfig.getLocale());
        vnpParams.put("vnp_ReturnUrl", returnUrl);
        vnpParams.put("vnp_IpAddr", ipAddress);

        // Sử dụng timezone Việt Nam
        Calendar cld = Calendar.getInstance(TimeZone.getTimeZone(vnPayConfig.getTimezone()));
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
        formatter.setTimeZone(TimeZone.getTimeZone(vnPayConfig.getTimezone()));
        String vnpCreateDate = formatter.format(cld.getTime());
        vnpParams.put("vnp_CreateDate", vnpCreateDate);

        cld.add(Calendar.MINUTE, 15);
        String vnpExpireDate = formatter.format(cld.getTime());
        vnpParams.put("vnp_ExpireDate", vnpExpireDate);

        String signValue = VNPayUtil.hashAllFields(vnpParams, vnPayConfig.getHashSecret());
        vnpParams.put("vnp_SecureHash", signValue);

        String payUrl = VNPayUtil.getPaymentURL(vnpParams, vnPayConfig.getApiUrl());
        
        return new VNPayCreatePaymentResponse(txnRef, amount, payUrl);
    }

    /**
     * Lấy URL redirect về frontend (web)
     */
    public String getFrontendRedirectUrl() {
        return vnPayConfig.getFrontendRedirectUrl();
    }

    /**
     * Lấy URL redirect về mobile app (deep link)
     */
    public String getMobileRedirectUrl() {
        return vnPayConfig.getMobileRedirectUrl();
    }

    /**
     * Xác thực callback từ VNPay và trả về kết quả
     * @param request HttpServletRequest từ VNPay callback
     * @return 1 = thành công, 0 = thất bại, -1 = chữ ký không hợp lệ
     */
    public int verifyPayment(HttpServletRequest request) {
        Map<String, String> fields = new HashMap<>();
        for (Enumeration<String> params = request.getParameterNames(); params.hasMoreElements();) {
            String fieldName = params.nextElement();
            String fieldValue = request.getParameter(fieldName);
            if ((fieldValue != null) && (fieldValue.length() > 0)) {
                fields.put(fieldName, fieldValue);
            }
        }

        String vnpSecureHash = request.getParameter("vnp_SecureHash");
        fields.remove("vnp_SecureHash");
        fields.remove("vnp_SecureHashType");
        
        // Remove custom params that we added (not part of VNPay signature)
        fields.remove("platform");
        fields.remove("userRole");

        // Hash với URL encode vì VNPay tính hash trên chuỗi đã encode
        String signValue = VNPayUtil.hashAllFields(fields, vnPayConfig.getHashSecret(), true);
        
        log.info("VNPay signature verification - Expected: {}, Received: {}", signValue, vnpSecureHash);

        if (signValue.equalsIgnoreCase(vnpSecureHash)) {
            String responseCode = request.getParameter("vnp_ResponseCode");
            if ("00".equals(responseCode)) {
                return 1; // Thanh toán thành công
            } else {
                return 0; // Thanh toán thất bại
            }
        } else {
            // Thử lại với không encode (một số trường hợp VNPay trả về đã decode)
            String signValueNoEncode = VNPayUtil.hashAllFields(fields, vnPayConfig.getHashSecret(), false);
            log.info("VNPay signature retry without encode - Expected: {}, Received: {}", signValueNoEncode, vnpSecureHash);
            
            if (signValueNoEncode.equalsIgnoreCase(vnpSecureHash)) {
                String responseCode = request.getParameter("vnp_ResponseCode");
                if ("00".equals(responseCode)) {
                    return 1; // Thanh toán thành công
                } else {
                    return 0; // Thanh toán thất bại
                }
            }
            return -1; // Chữ ký không hợp lệ
        }
    }

    /**
     * Lấy invoiceId từ vnp_TxnRef
     */
    public Integer getInvoiceIdFromRequest(HttpServletRequest request) {
        String txnRef = request.getParameter("vnp_TxnRef");
        if (txnRef != null && !txnRef.isEmpty()) {
            try {
                return Integer.parseInt(txnRef);
            } catch (NumberFormatException e) {
                log.error("Invalid txnRef format: {}", txnRef);
                return null;
            }
        }
        return null;
    }
}
