package com.example.inventoryservice.config;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.File;

@Component
public class SubmissionConfig {

    private static final Logger log = LoggerFactory.getLogger(SubmissionConfig.class);

    private String successfulOrderId = "100";
    private String failingPaymentOrderId = "201";
    private String failingInventoryOrderId = "302";

    @PostConstruct
    public void init() {
        try {
            ObjectMapper mapper = new ObjectMapper();
            File file = new File("submission.json");
            if (!file.exists()) {
                file = new File("../submission.json");
            }
            if (!file.exists()) {
                file = new File("/app/submission.json");
            }

            if (file.exists()) {
                log.info("Loading submission config from: {}", file.getAbsolutePath());
                JsonNode root = mapper.readTree(file);
                if (root.has("successfulOrderId")) {
                    successfulOrderId = root.get("successfulOrderId").asText();
                }
                if (root.has("failingPaymentOrderId")) {
                    failingPaymentOrderId = root.get("failingPaymentOrderId").asText();
                }
                if (root.has("failingInventoryOrderId")) {
                    failingInventoryOrderId = root.get("failingInventoryOrderId").asText();
                }
                log.info("Loaded submission config: successfulOrderId={}, failingPaymentOrderId={}, failingInventoryOrderId={}",
                        successfulOrderId, failingPaymentOrderId, failingInventoryOrderId);
            } else {
                log.warn("submission.json not found, using default values.");
            }
        } catch (Exception e) {
            log.error("Failed to load submission.json, using fallback defaults", e);
        }
    }

    public String getSuccessfulOrderId() {
        return successfulOrderId;
    }

    public String getFailingPaymentOrderId() {
        return failingPaymentOrderId;
    }

    public String getFailingInventoryOrderId() {
        return failingInventoryOrderId;
    }
}
