package com.example.orderservice.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Component
public class PaymentClient {

    private static final Logger log = LoggerFactory.getLogger(PaymentClient.class);

    private final RestTemplate restTemplate;
    private final String baseUrl;

    public PaymentClient(RestTemplate restTemplate, @Value("${PAYMENT_SERVICE_URL}") String baseUrl) {
        this.restTemplate = restTemplate;
        this.baseUrl = baseUrl;
    }

    public boolean process(String orderId) {
        String url = baseUrl + "/api/payments/process";
        log.info("Saga order {} - Requesting payment processing at {}", orderId, url);
        try {
            Map<String, String> request = Map.of("orderId", orderId);
            ResponseEntity<Void> response = restTemplate.postForEntity(url, request, Void.class);
            boolean success = response.getStatusCode().is2xxSuccessful();
            log.info("Saga order {} - Payment processing result: {}", orderId, success);
            return success;
        } catch (Exception e) {
            log.error("Saga order {} - Payment processing failed with exception: {}", orderId, e.getMessage());
            return false;
        }
    }

    public boolean cancel(String orderId) {
        String url = baseUrl + "/api/payments/cancel";
        log.info("Saga order {} - Requesting payment cancellation at {} (Compensation)", orderId, url);
        try {
            Map<String, String> request = Map.of("orderId", orderId);
            ResponseEntity<Void> response = restTemplate.postForEntity(url, request, Void.class);
            boolean success = response.getStatusCode().is2xxSuccessful();
            log.info("Saga order {} - Payment cancellation result: {}", orderId, success);
            return success;
        } catch (Exception e) {
            log.error("Saga order {} - Payment cancellation failed with exception: {}", orderId, e.getMessage());
            return false;
        }
    }
}
