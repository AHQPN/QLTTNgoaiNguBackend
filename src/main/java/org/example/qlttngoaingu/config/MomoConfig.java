package org.example.qlttngoaingu.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
@Getter
public class MomoConfig {

    @Value("${momo.partner-code:MOMO}")
    private String partnerCode;

    @Value("${momo.access-key:F8BBA842ECF85}")
    private String accessKey;

    @Value("${momo.secret-key:K951B6PE1waDMi640xX08PD3vg6EkVlz}")
    private String secretKey;

    @Value("${momo.redirect-url:https://momo.vn/return}")
    private String redirectUrl;

    @Value("${momo.ipn-url:https://callback.url/notify}")
    private String ipnUrl;

    @Value("${momo.request-type:payWithMethod}")
    private String requestType;

    @Value("${momo.create-payment-url:https://test-payment.momo.vn/v2/gateway/api/create}")
    private String createPaymentUrl;

    @Value("${momo.check-status-url:https://test-payment.momo.vn/v2/gateway/api/query}")
    private String checkStatusUrl;

    @Value("${momo.lang:vi}")
    private String lang;
}