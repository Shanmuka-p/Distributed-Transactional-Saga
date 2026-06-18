package com.example.orderservice.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public class CreateOrderRequest {

    private String orderId;

    @NotNull(message = "customerId cannot be null")
    private Long customerId;

    @NotNull(message = "productId cannot be null")
    private Long productId;

    @NotNull(message = "quantity cannot be null")
    @Min(value = 1, message = "quantity must be at least 1")
    private Integer quantity;

    @NotNull(message = "unitPrice cannot be null")
    @DecimalMin(value = "0.01", message = "unitPrice must be greater than 0")
    private BigDecimal unitPrice;

    public CreateOrderRequest() {
    }

    public CreateOrderRequest(String orderId, Long customerId, Long productId, Integer quantity, BigDecimal unitPrice) {
        this.orderId = orderId;
        this.customerId = customerId;
        this.productId = productId;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public Long getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Long customerId) {
        this.customerId = customerId;
    }

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(BigDecimal unitPrice) {
        this.unitPrice = unitPrice;
    }
}
