package org.example.qlttngoaingu.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.qlttngoaingu.exception.AppException;
import org.example.qlttngoaingu.exception.ErrorCode;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Component
@RequiredArgsConstructor
@Slf4j
public class MomoHttpClient {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    /**
     * Gửi POST request đến MoMo API
     */
    public <T> T post(String url, Object requestBody, Class<T> responseType) {
        try {
            // Convert request body to JSON
            String jsonRequest = objectMapper.writeValueAsString(requestBody);
            log.info("Sending request to MoMo: {}", url);
            log.debug("Request body: {}", jsonRequest);

            // Setup headers
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            // Create HTTP entity
            HttpEntity<String> entity = new HttpEntity<>(jsonRequest, headers);

            // Execute request
            ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);

            String jsonResponse = response.getBody();
            log.info("Received response from MoMo");
            log.debug("Response body: {}", jsonResponse);

            // Parse response
            return objectMapper.readValue(jsonResponse, responseType);

        } catch (RestClientException e) {
            log.error("REST client error when calling MoMo API: {}", url, e);
            throw new AppException(ErrorCode.MOMO_PAYMENT_API_ERROR);
        } catch (Exception e) {
            log.error("Error calling MoMo API: {}", url, e);
            throw new AppException(ErrorCode.MOMO_PAYMENT_PROCESSING_ERROR);
        }
    }
}