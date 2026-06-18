package com.example.paymentservice.controller;

import com.example.paymentservice.config.SubmissionConfig;
import com.example.paymentservice.dto.PaymentRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private static final Logger log = LoggerFactory.getLogger(PaymentController.class);

    private final SubmissionConfig submissionConfig;
    private final Set<String> processedPayments = ConcurrentHashMap.newKeySet();
    private final Set<String> delayedOrders = ConcurrentHashMap.newKeySet();

    public PaymentController(SubmissionConfig submissionConfig) {
        this.submissionConfig = submissionConfig;
    }

    @PostMapping("/process")
    public ResponseEntity<Void> processPayment(@RequestBody PaymentRequest request) {
        log.info("Payment Service - Process request: {}", request);
        String orderId = request.getOrderId();

        if (orderId == null || orderId.trim().isEmpty()) {
            log.warn("Payment Service - Bad request: orderId is null/empty");
            return ResponseEntity.badRequest().build();
        }

        if (orderId.equals(submissionConfig.getFailingPaymentOrderId())) {
            log.error("Payment Service - Simulated failure for orderId: {}", orderId);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }

        if (orderId.equals(submissionConfig.getSuccessfulOrderId()) && delayedOrders.add(orderId)) {
            log.info("Payment Service - Simulating payment delay (5s) for orderId: {}", orderId);
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.warn("Payment Service - Sleep interrupted");
            }
        }

        processedPayments.add(orderId);
        log.info("Payment Service - Process successful for orderId: {}", orderId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/cancel")
    public ResponseEntity<Void> cancelPayment(@RequestBody PaymentRequest request) {
        log.info("Payment Service - Cancel/Compensate request: {}", request);
        String orderId = request.getOrderId();

        if (orderId == null || orderId.trim().isEmpty()) {
            log.warn("Payment Service - Bad request: orderId is null/empty");
            return ResponseEntity.badRequest().build();
        }

        processedPayments.remove(orderId);
        log.info("Payment Service - Cancel/Compensate successful for orderId: {}", orderId);
        return ResponseEntity.ok().build();
    }
}
