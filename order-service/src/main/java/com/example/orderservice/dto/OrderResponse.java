package com.example.orderservice.dto;

import java.math.BigDecimal;

public class OrderResponse {

    private String orderId;
    private Long customerId;
    private Long productId;
    private Integer quantity;
    private BigDecimal unitPrice;
    private BigDecimal amount;
    private String status;

    public OrderResponse() {
    }

    public OrderResponse(String orderId, Long customerId, Long productId, Integer quantity, BigDecimal unitPrice, BigDecimal amount, String status) {
        this.orderId = orderId;
        this.customerId = customerId;
        this.productId = productId;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.amount = amount;
        this.status = status;
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

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public static OrderResponseBuilder builder() {
        return new OrderResponseBuilder();
    }

    public static class OrderResponseBuilder {
        private String orderId;
        private Long customerId;
        private Long productId;
        private Integer quantity;
        private BigDecimal unitPrice;
        private BigDecimal amount;
        private String status;

        public OrderResponseBuilder orderId(String orderId) {
            this.orderId = orderId;
            return this;
        }

        public OrderResponseBuilder customerId(Long customerId) {
            this.customerId = customerId;
            return this;
        }

        public OrderResponseBuilder productId(Long productId) {
            this.productId = productId;
            return this;
        }

        public OrderResponseBuilder quantity(Integer quantity) {
            this.quantity = quantity;
            return this;
        }

        public OrderResponseBuilder unitPrice(BigDecimal unitPrice) {
            this.unitPrice = unitPrice;
            return this;
        }

        public OrderResponseBuilder amount(BigDecimal amount) {
            this.amount = amount;
            return this;
        }

        public OrderResponseBuilder status(String status) {
            this.status = status;
            return this;
        }

        public OrderResponse build() {
            return new OrderResponse(orderId, customerId, productId, quantity, unitPrice, amount, status);
        }
    }
}
