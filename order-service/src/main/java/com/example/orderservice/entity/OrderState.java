package com.example.orderservice.entity;

public enum OrderState {
    ORDER_CREATED,
    PAYMENT_PENDING,
    PAYMENT_COMPLETED,
    INVENTORY_RESERVED,
    ORDER_COMPLETED,
    ORDER_FAILED
}
