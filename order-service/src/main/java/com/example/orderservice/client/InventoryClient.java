package com.example.orderservice.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Component
public class InventoryClient {

    private static final Logger log = LoggerFactory.getLogger(InventoryClient.class);

    private final RestTemplate restTemplate;
    private final String baseUrl;

    public InventoryClient(RestTemplate restTemplate, @Value("${INVENTORY_SERVICE_URL}") String baseUrl) {
        this.restTemplate = restTemplate;
        this.baseUrl = baseUrl;
    }

    public boolean reserve(String orderId) {
        String url = baseUrl + "/api/inventory/reserve";
        log.info("Saga order {} - Requesting inventory reservation at {}", orderId, url);
        try {
            Map<String, String> request = Map.of("orderId", orderId);
            ResponseEntity<Void> response = restTemplate.postForEntity(url, request, Void.class);
            boolean success = response.getStatusCode().is2xxSuccessful();
            log.info("Saga order {} - Inventory reservation result: {}", orderId, success);
            return success;
        } catch (Exception e) {
            log.error("Saga order {} - Inventory reservation failed with exception: {}", orderId, e.getMessage());
            return false;
        }
    }

    public boolean release(String orderId) {
        String url = baseUrl + "/api/inventory/release";
        log.info("Saga order {} - Requesting inventory release at {} (Compensation)", orderId, url);
        try {
            Map<String, String> request = Map.of("orderId", orderId);
            ResponseEntity<Void> response = restTemplate.postForEntity(url, request, Void.class);
            boolean success = response.getStatusCode().is2xxSuccessful();
            log.info("Saga order {} - Inventory release result: {}", orderId, success);
            return success;
        } catch (Exception e) {
            log.error("Saga order {} - Inventory release failed with exception: {}", orderId, e.getMessage());
            return false;
        }
    }
}
