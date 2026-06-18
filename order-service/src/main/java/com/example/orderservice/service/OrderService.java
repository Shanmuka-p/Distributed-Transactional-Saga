package com.example.orderservice.service;

import com.example.orderservice.dto.CreateOrderRequest;
import com.example.orderservice.dto.OrderResponse;
import com.example.orderservice.entity.Order;
import com.example.orderservice.entity.OrderState;
import com.example.orderservice.repository.OrderRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

@Service
public class OrderService {

    private static final Logger log = LoggerFactory.getLogger(OrderService.class);

    private final OrderRepository orderRepository;
    private final OrderSagaOrchestrator orchestrator;

    public OrderService(OrderRepository orderRepository, OrderSagaOrchestrator orchestrator) {
        this.orderRepository = orderRepository;
        this.orchestrator = orchestrator;
    }

    @Transactional
    public OrderResponse createOrder(CreateOrderRequest request) {
        String orderId = request.getOrderId();
        if (orderId == null || orderId.trim().isEmpty()) {
            orderId = UUID.randomUUID().toString();
        }

        var existingOrderOpt = orderRepository.findById(orderId);
        if (existingOrderOpt.isPresent()) {
            log.info("Idempotency match: Order {} already exists with status {}", orderId, existingOrderOpt.get().getStatus());
            return mapToResponse(existingOrderOpt.get());
        }

        BigDecimal amount = request.getUnitPrice().multiply(BigDecimal.valueOf(request.getQuantity()));

        Order order = Order.builder()
                .orderId(orderId)
                .customerId(request.getCustomerId())
                .productId(request.getProductId())
                .quantity(request.getQuantity())
                .unitPrice(request.getUnitPrice())
                .amount(amount)
                .status(OrderState.ORDER_CREATED)
                .build();

        log.info("Saga order {} - Saving initial order record in DB", orderId);
        order = orderRepository.save(order);

        orchestrator.startSaga(order);

        Order finalOrder = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found after creation: " + orderId));

        return mapToResponse(finalOrder);
    }

    public OrderResponse getOrder(String orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found: " + orderId));
        return mapToResponse(order);
    }

    private OrderResponse mapToResponse(Order order) {
        return OrderResponse.builder()
                .orderId(order.getOrderId())
                .customerId(order.getCustomerId())
                .productId(order.getProductId())
                .quantity(order.getQuantity())
                .unitPrice(order.getUnitPrice())
                .amount(order.getAmount())
                .status(order.getStatus().name())
                .build();
    }
}
