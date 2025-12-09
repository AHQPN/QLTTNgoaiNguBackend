package org.example.qlttngoaingu.utils;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class VNPayUtil {

    /**
     * Tính HMAC SHA512 cho VNPay (trả về hex lowercase)
     */
    public static String hmacSHA512(String key, String data) {
        try {
            Mac hmac512 = Mac.getInstance("HmacSHA512");
            SecretKeySpec secretKey = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA512");
            hmac512.init(secretKey);
            byte[] result = hmac512.doFinal(data.getBytes(StandardCharsets.UTF_8));
            return bytesToHex(result);
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate HMAC", e);
        }
    }

    private static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    /**
     * Hash các fields để tạo chữ ký (dùng cho tạo URL - có URL encode)
     */
    public static String hashAllFields(Map<String, String> fields, String hashSecret) {
        return hashAllFields(fields, hashSecret, true);
    }

    /**
     * Hash các fields để verify chữ ký (dùng cho verify callback - không URL encode)
     */
    public static String hashAllFieldsForVerify(Map<String, String> fields, String hashSecret) {
        return hashAllFields(fields, hashSecret, false);
    }

    /**
     * Hash các fields
     * @param urlEncode true = URL encode giá trị (khi tạo URL), false = không encode (khi verify callback)
     */
    public static String hashAllFields(Map<String, String> fields, String hashSecret, boolean urlEncode) {
        List<String> fieldNames = new ArrayList<>(fields.keySet());
        Collections.sort(fieldNames);
        StringBuilder sb = new StringBuilder();
        boolean first = true;
        for (String fieldName : fieldNames) {
            String fieldValue = fields.get(fieldName);
            if ((fieldValue != null) && (!fieldValue.isEmpty())) {
                if (!first) {
                    sb.append('&');
                }
                sb.append(fieldName);
                sb.append('=');
                if (urlEncode) {
                    sb.append(URLEncoder.encode(fieldValue, StandardCharsets.UTF_8));
                } else {
                    sb.append(fieldValue);
                }
                first = false;
            }
        }
        String hashData = sb.toString();
        log.debug("VNPay hash data: {}", hashData);
        return hmacSHA512(hashSecret, hashData);
    }

    public static String getPaymentURL(Map<String, String> params, String baseUrl) {
        List<String> fieldNames = new ArrayList<>(params.keySet());
        Collections.sort(fieldNames);
        StringBuilder query = new StringBuilder();
        boolean first = true;
        for (String fieldName : fieldNames) {
            String fieldValue = params.get(fieldName);
            if ((fieldValue != null) && (!fieldValue.isEmpty())) {
                if (!first) {
                    query.append('&');
                }
                query.append(URLEncoder.encode(fieldName, StandardCharsets.UTF_8));
                query.append('=');
                query.append(URLEncoder.encode(fieldValue, StandardCharsets.UTF_8));
                first = false;
            }
        }
        return baseUrl + "?" + query.toString();
    }

    public static String getRandomNumber(int len) {
        Random rnd = new Random();
        String chars = "0123456789";
        StringBuilder sb = new StringBuilder(len);
        for (int i = 0; i < len; i++) {
            sb.append(chars.charAt(rnd.nextInt(chars.length())));
        }
        return sb.toString();
    }
}
