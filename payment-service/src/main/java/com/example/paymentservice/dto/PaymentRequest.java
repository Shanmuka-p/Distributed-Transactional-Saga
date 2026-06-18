package com.example.paymentservice.dto;

public class PaymentRequest {
    private String orderId;

    public PaymentRequest() {
    }

    public PaymentRequest(String orderId) {
        this.orderId = orderId;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    @Override
    public String toString() {
        return "PaymentRequest{" +
                "orderId='" + orderId + '\'' +
                '}';
    }
}
