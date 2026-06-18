package com.example.inventoryservice.dto;

public class InventoryRequest {
    private String orderId;

    public InventoryRequest() {
    }

    public InventoryRequest(String orderId) {
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
        return "InventoryRequest{" +
                "orderId='" + orderId + '\'' +
                '}';
    }
}
