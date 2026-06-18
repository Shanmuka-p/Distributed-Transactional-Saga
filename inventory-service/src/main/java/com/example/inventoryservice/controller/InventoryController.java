package com.example.inventoryservice.controller;

import com.example.inventoryservice.config.SubmissionConfig;
import com.example.inventoryservice.dto.InventoryRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@RestController
@RequestMapping("/api/inventory")
public class InventoryController {

    private static final Logger log = LoggerFactory.getLogger(InventoryController.class);

    private final SubmissionConfig submissionConfig;
    private final Set<String> reservedInventory = ConcurrentHashMap.newKeySet();

    public InventoryController(SubmissionConfig submissionConfig) {
        this.submissionConfig = submissionConfig;
    }

    @PostMapping("/reserve")
    public ResponseEntity<Void> reserveInventory(@RequestBody InventoryRequest request) {
        log.info("Inventory Service - Reserve request: {}", request);
        String orderId = request.getOrderId();

        if (orderId == null || orderId.trim().isEmpty()) {
            log.warn("Inventory Service - Bad request: orderId is null/empty");
            return ResponseEntity.badRequest().build();
        }

        if (orderId.equals(submissionConfig.getFailingInventoryOrderId())) {
            log.error("Inventory Service - Simulated failure for orderId: {}", orderId);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }

        reservedInventory.add(orderId);
        log.info("Inventory Service - Reserve successful for orderId: {}", orderId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/release")
    public ResponseEntity<Void> releaseInventory(@RequestBody InventoryRequest request) {
        log.info("Inventory Service - Release/Compensate request: {}", request);
        String orderId = request.getOrderId();

        if (orderId == null || orderId.trim().isEmpty()) {
            log.warn("Inventory Service - Bad request: orderId is null/empty");
            return ResponseEntity.badRequest().build();
        }

        reservedInventory.remove(orderId);
        log.info("Inventory Service - Release/Compensate successful for orderId: {}", orderId);
        return ResponseEntity.ok().build();
    }
}
