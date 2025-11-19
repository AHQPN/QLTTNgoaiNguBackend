package org.example.qlttngoaingu.utils;


import lombok.extern.slf4j.Slf4j;
import org.example.qlttngoaingu.exception.AppException;
import org.example.qlttngoaingu.exception.ErrorCode;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;

@Slf4j
public class MomoSignatureUtil {

    private static final String HMAC_SHA256 = "HmacSHA256";

    /**
     * Tạo chữ ký HMAC SHA256
     */
    public static String signHmacSHA256(String data, String secretKey) {
        try {
            Mac hmacSHA256 = Mac.getInstance(HMAC_SHA256);
            SecretKeySpec keySpec = new SecretKeySpec(
                    secretKey.getBytes(StandardCharsets.UTF_8),
                    HMAC_SHA256
            );
            hmacSHA256.init(keySpec);

            byte[] hash = hmacSHA256.doFinal(data.getBytes(StandardCharsets.UTF_8));

            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }

            log.debug("Generated signature for data: {}", data);
            return hexString.toString();

        } catch (Exception e) {
            log.error("Error generating HMAC SHA256 signature", e);
            throw new AppException(ErrorCode.MOMO_SIGNATURE_ERROR);
        }
    }

    /**
     * Tạo raw signature cho payment request
     */
    public static String buildPaymentRawSignature(
            String accessKey, String amount, String extraData,
            String ipnUrl, String orderId, String orderInfo,
            String partnerCode, String redirectUrl, String requestId,
            String requestType) {

        return String.format(
                "accessKey=%s&amount=%s&extraData=%s&ipnUrl=%s&orderId=%s&orderInfo=%s&partnerCode=%s&redirectUrl=%s&requestId=%s&requestType=%s",
                accessKey, amount, extraData, ipnUrl, orderId, orderInfo,
                partnerCode, redirectUrl, requestId, requestType
        );
    }

    /**
     * Tạo raw signature cho status check
     */
    public static String buildStatusCheckRawSignature(
            String accessKey, String orderId, String partnerCode, String requestId) {

        return String.format(
                "accessKey=%s&orderId=%s&partnerCode=%s&requestId=%s",
                accessKey, orderId, partnerCode, requestId
        );
    }
}