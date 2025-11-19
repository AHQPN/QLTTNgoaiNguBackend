package org.example.qlttngoaingu.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.qlttngoaingu.client.MomoHttpClient;
import org.example.qlttngoaingu.config.MomoConfig;
import org.example.qlttngoaingu.dto.request.MomoPaymentRequest;
import org.example.qlttngoaingu.dto.request.MomoStatusCheckRequest;
import org.example.qlttngoaingu.dto.response.MomoCheckStatusResponse;
import org.example.qlttngoaingu.dto.response.MomoCreatePaymentResponse;
import org.example.qlttngoaingu.exception.AppException;
import org.example.qlttngoaingu.exception.ErrorCode;
import org.example.qlttngoaingu.factory.MomoRequestFactory;
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
}