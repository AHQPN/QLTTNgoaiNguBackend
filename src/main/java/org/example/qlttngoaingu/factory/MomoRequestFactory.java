package org.example.qlttngoaingu.factory;

import lombok.RequiredArgsConstructor;
import org.example.qlttngoaingu.config.MomoConfig;
import org.example.qlttngoaingu.dto.request.MomoPaymentRequest;
import org.example.qlttngoaingu.dto.request.MomoStatusCheckRequest;
import org.example.qlttngoaingu.utils.MomoSignatureUtil;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class MomoRequestFactory {

    private final MomoConfig momoConfig;

    /**
     * Tạo payment request
     */
    public MomoPaymentRequest createPaymentRequest(String amount, String orderInfo) {
        String requestId = generateRequestId();
        String orderId = generateOrderId();
        String extraData = "";

        // Build raw signature
        String rawSignature = MomoSignatureUtil.buildPaymentRawSignature(
                momoConfig.getAccessKey(),
                amount,
                extraData,
                momoConfig.getIpnUrl(),
                orderId,
                orderInfo,
                momoConfig.getPartnerCode(),
                momoConfig.getRedirectUrl(),
                requestId,
                momoConfig.getRequestType()
        );

        // Generate signature
        String signature = MomoSignatureUtil.signHmacSHA256(rawSignature, momoConfig.getSecretKey());

        // Build request DTO
        return MomoPaymentRequest.builder()
                .partnerCode(momoConfig.getPartnerCode())
                .accessKey(momoConfig.getAccessKey())
                .requestId(requestId)
                .amount(amount)
                .orderId(orderId)
                .orderInfo(orderInfo)
                .redirectUrl(momoConfig.getRedirectUrl())
                .ipnUrl(momoConfig.getIpnUrl())
                .extraData(extraData)
                .requestType(momoConfig.getRequestType())
                .signature(signature)
                .lang(momoConfig.getLang())
                .build();
    }

    /**
     * Tạo status check request
     */
    public MomoStatusCheckRequest createStatusCheckRequest(String orderId) {
        String requestId = generateRequestId();

        // Build raw signature
        String rawSignature = MomoSignatureUtil.buildStatusCheckRawSignature(
                momoConfig.getAccessKey(),
                orderId,
                momoConfig.getPartnerCode(),
                requestId
        );

        // Generate signature
        String signature = MomoSignatureUtil.signHmacSHA256(rawSignature, momoConfig.getSecretKey());

        // Build request DTO
        return MomoStatusCheckRequest.builder()
                .partnerCode(momoConfig.getPartnerCode())
                .accessKey(momoConfig.getAccessKey())
                .requestId(requestId)
                .orderId(orderId)
                .signature(signature)
                .lang(momoConfig.getLang())
                .build();
    }

    /**
     * Generate unique request ID
     */
    private String generateRequestId() {
        return momoConfig.getPartnerCode() + new Date().getTime();
    }

    /**
     * Generate unique order ID
     */
    private String generateOrderId() {
        return UUID.randomUUID().toString();
    }
}