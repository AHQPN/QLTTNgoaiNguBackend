package org.example.qlttngoaingu.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.qlttngoaingu.client.MomoHttpClient;
import org.example.qlttngoaingu.config.MomoConfig;
import org.example.qlttngoaingu.dto.request.MomoIPNRequest;
import org.example.qlttngoaingu.dto.request.MomoPaymentRequest;
import org.example.qlttngoaingu.dto.request.MomoStatusCheckRequest;
import org.example.qlttngoaingu.dto.response.MomoCheckStatusResponse;
import org.example.qlttngoaingu.dto.response.MomoCreatePaymentResponse;
import org.example.qlttngoaingu.exception.AppException;
import org.example.qlttngoaingu.exception.ErrorCode;
import org.example.qlttngoaingu.factory.MomoRequestFactory;
import org.example.qlttngoaingu.utils.MomoSignatureUtil;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class MomoService {

    private final MomoConfig momoConfig;
    private final MomoHttpClient momoHttpClient;
    private final MomoRequestFactory momoRequestFactory;

    /**
     * Tạo payment request
     */
    public MomoCreatePaymentResponse createPaymentRequest(String amount) {
        return createPaymentRequest(amount, "SN Mobile Payment");
    }

    /**
     * Tạo payment request với custom order info
     */
    public MomoCreatePaymentResponse createPaymentRequest(String amount, String orderInfo) {
        try {
            log.info("Creating MoMo payment request for amount: {}", amount);

            // Validate amount
            validateAmount(amount);

            // Create request
            MomoPaymentRequest request = momoRequestFactory.createPaymentRequest(amount, orderInfo);

            // Call MoMo API
            MomoCreatePaymentResponse response = momoHttpClient.post(
                    momoConfig.getCreatePaymentUrl(),
                    request,
                    MomoCreatePaymentResponse.class
            );

            // Validate response
            validatePaymentResponse(response);

            log.info("Payment request created successfully. OrderId: {}", response.getOrderId());
            return response;

        } catch (AppException e) {
            // Re-throw AppException
            throw e;
        } catch (Exception e) {
            log.error("Failed to create payment request", e);
            throw new AppException(ErrorCode.MOMO_PAYMENT_CREATE_FAILED);
        }
    }

    /**
     * Kiểm tra trạng thái thanh toán
     */
    public MomoCheckStatusResponse checkPaymentStatus(String orderId) {
        try {
            log.info("Checking payment status for orderId: {}", orderId);

            // Validate orderId
            if (orderId == null || orderId.trim().isEmpty()) {
                throw new AppException(ErrorCode.MOMO_INVALID_ORDER_ID);
            }

            // Create request
            MomoStatusCheckRequest request = momoRequestFactory.createStatusCheckRequest(orderId);

            // Call MoMo API
            MomoCheckStatusResponse response = momoHttpClient.post(
                    momoConfig.getCheckStatusUrl(),
                    request,
                    MomoCheckStatusResponse.class
            );

            // Log và check result code
            logStatusCheckResult(orderId, response);

            return response;

        } catch (AppException e) {
            // Re-throw AppException
            throw e;
        } catch (Exception e) {
            log.error("Failed to check payment status for orderId: {}", orderId, e);
            throw new AppException(ErrorCode.MOMO_STATUS_CHECK_FAILED);
        }
    }

    /**
     * Validate amount
     */
    private void validateAmount(String amount) {
        if (amount == null || amount.trim().isEmpty()) {
            throw new AppException(ErrorCode.MOMO_PAYMENT_INVALID_AMOUNT);
        }

        try {
            long amountValue = Long.parseLong(amount);
            if (amountValue <= 0) {
                throw new AppException(ErrorCode.MOMO_PAYMENT_INVALID_AMOUNT);
            }
        } catch (NumberFormatException e) {
            throw new AppException(ErrorCode.MOMO_PAYMENT_INVALID_AMOUNT);
        }
    }

    /**
     * Validate payment response
     */
    private void validatePaymentResponse(MomoCreatePaymentResponse response) {
        if (response == null) {
            throw new AppException(ErrorCode.MOMO_PAYMENT_NULL_RESPONSE);
        }

        // Check result code from MoMo
        if (response.getResultCode() != 0) {
            ErrorCode errorCode = mapMomoResultCodeToErrorCode(response.getResultCode());
            log.error("MoMo payment creation failed. Code: {}, Message: {}",
                    response.getResultCode(), response.getMessage());
            throw new AppException(errorCode);
        }

        if (response.getPayUrl() == null || response.getPayUrl().trim().isEmpty()) {
            throw new AppException(ErrorCode.MOMO_PAYMENT_NO_PAY_URL);
        }
    }

    /**
     * Log status check result
     */
    private void logStatusCheckResult(String orderId, MomoCheckStatusResponse response) {
        if (response.getResultCode() == 0) {
            log.info("Payment status check successful. OrderId: {}, TransId: {}",
                    orderId, response.getTransId());
        } else {
            log.warn("Payment status check completed with error. OrderId: {}, ResultCode: {}, Message: {}",
                    orderId, response.getResultCode(), response.getMessage());
        }
    }

    /**
     * Map MoMo result code sang ErrorCode của hệ thống
     */
    private ErrorCode mapMomoResultCodeToErrorCode(int momoResultCode) {
        return switch (momoResultCode) {
            case 4001 -> ErrorCode.MOMO_RESULT_INVALID_SIGNATURE;
            case 1001 -> ErrorCode.MOMO_RESULT_INSUFFICIENT_BALANCE;
            case 1004 -> ErrorCode.MOMO_RESULT_TRANSACTION_FAILED;
            case 1005 -> ErrorCode.MOMO_RESULT_ORDER_NOT_FOUND;
            case 1006 -> ErrorCode.MOMO_RESULT_ORDER_ALREADY_PAID;
            case 1007 -> ErrorCode.MOMO_RESULT_TRANSACTION_EXPIRED;
            default -> ErrorCode.MOMO_PAYMENT_API_ERROR;
        };
    }

    /**
     * Xử lý IPN notification từ MoMo
     */
    public void processIPNNotification(MomoIPNRequest request) {
        log.info("Processing IPN for orderId: {}, resultCode: {}",
                request.getOrderId(), request.getResultCode());

        // 1. Verify signature để đảm bảo request từ MoMo
        if (!verifyIPNSignature(request)) {
            log.error("Invalid IPN signature for orderId: {}", request.getOrderId());
            throw new AppException(ErrorCode.MOMO_RESULT_INVALID_SIGNATURE);
        }

        // 2. Kiểm tra result code
        if (request.getResultCode() == 0) {
            // Thanh toán thành công
            handleSuccessfulPayment(request);
        } else {
            // Thanh toán thất bại
            handleFailedPayment(request);
        }
    }

    /**
     * Xử lý khi thanh toán thành công
     */
    private void handleSuccessfulPayment(MomoIPNRequest request) {
        log.info("Payment successful for orderId: {}, transId: {}",
                request.getOrderId(), request.getTransId());

        try {
            // TODO: Cập nhật trạng thái order trong database
            // orderService.updateOrderStatus(request.getOrderId(), OrderStatus.PAID);

            // TODO: Gửi email/notification cho user
            // emailService.sendPaymentSuccessEmail(request.getOrderId());

            // TODO: Kích hoạt khóa học/dịch vụ cho user
            // courseService.activateCourse(request.getOrderId());

            log.info("Order {} has been updated to PAID status", request.getOrderId());

        } catch (Exception e) {
            log.error("Error updating order status for orderId: {}", request.getOrderId(), e);
            // Không throw exception vì đã nhận được tiền rồi
            // Sẽ có job retry sau
        }
    }

    /**
     * Xử lý khi thanh toán thất bại
     */
    private void handleFailedPayment(MomoIPNRequest request) {
        log.warn("Payment failed for orderId: {}, resultCode: {}, message: {}",
                request.getOrderId(), request.getResultCode(), request.getMessage());

        try {
            // TODO: Cập nhật trạng thái order thành FAILED
            // orderService.updateOrderStatus(request.getOrderId(), OrderStatus.FAILED);

            // TODO: Gửi thông báo cho user
            // emailService.sendPaymentFailedEmail(request.getOrderId());

        } catch (Exception e) {
            log.error("Error updating failed order: {}", request.getOrderId(), e);
        }
    }

    /**
     * Verify chữ ký IPN từ MoMo
     */
    private boolean verifyIPNSignature(MomoIPNRequest request) {
        try {
            // Build raw signature theo format của MoMo
            String rawSignature = buildIPNRawSignature(request);

            // Generate signature
            String expectedSignature = MomoSignatureUtil.signHmacSHA256(
                    rawSignature,
                    momoConfig.getSecretKey()
            );

            // So sánh với signature từ MoMo
            boolean isValid = expectedSignature.equals(request.getSignature());

            if (!isValid) {
                log.error("Signature mismatch. Expected: {}, Got: {}",
                        expectedSignature, request.getSignature());
            }

            return isValid;

        } catch (Exception e) {
            log.error("Error verifying IPN signature", e);
            return false;
        }
    }

    /**
     * Build raw signature cho IPN verification
     */
    private String buildIPNRawSignature(MomoIPNRequest request) {
        return String.format(
                "accessKey=%s&amount=%d&extraData=%s&message=%s&orderId=%s&orderInfo=%s&orderType=%s&partnerCode=%s&payType=%s&requestId=%s&responseTime=%d&resultCode=%d&transId=%d",
                momoConfig.getAccessKey(),
                request.getAmount(),
                request.getExtraData() != null ? request.getExtraData() : "",
                request.getMessage(),
                request.getOrderId(),
                request.getOrderInfo(),
                request.getOrderType(),
                request.getPartnerCode(),
                request.getPayType(),
                request.getRequestId(),
                request.getResponseTime(),
                request.getResultCode(),
                request.getTransId()
        );
    }
}