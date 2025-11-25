package org.example.qlttngoaingu.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
@Getter
public class VNPayConfig {

    // VNPay Test/Sandbox credentials
    @Value("${vnpay.tmn-code:CGKD9BN1}")
    private String tmnCode;

    @Value("${vnpay.hash-secret:GJXFAYTALTSLSRLPIPCCBMXBUSHXDRDV}")
    private String hashSecret;

    @Value("${vnpay.api-url:https://sandbox.vnpayment.vn/paymentv2/vpcpay.html}")
    private String apiUrl;

    @Value("${vnpay.return-url:http://localhost:8080/payment/vnpay/return}")
    private String returnUrl;

    @Value("${vnpay.version:2.1.0}")
    private String version;

    @Value("${vnpay.command:pay}")
    private String command;

    @Value("${vnpay.order-type:other}")
    private String orderType;

    @Value("${vnpay.locale:vn}")
    private String locale;

    @Value("${vnpay.currency-code:VND}")
    private String currencyCode;
}