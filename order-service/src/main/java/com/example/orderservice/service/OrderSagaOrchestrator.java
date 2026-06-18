package com.example.orderservice.service;

import com.example.orderservice.client.InventoryClient;
import com.example.orderservice.client.PaymentClient;
import com.example.orderservice.entity.Order;
import com.example.orderservice.entity.OrderEvent;
import com.example.orderservice.entity.OrderState;
import com.example.orderservice.repository.OrderRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.statemachine.StateMachine;
import org.springframework.stereotype.Service;

@Service
public class OrderSagaOrchestrator {

    private static final Logger log = LoggerFactory.getLogger(OrderSagaOrchestrator.class);

    private final OrderStateMachineManager stateMachineManager;
    private final PaymentClient paymentClient;
    private final InventoryClient inventoryClient;
    private final OrderRepository orderRepository;

    public OrderSagaOrchestrator(OrderStateMachineManager stateMachineManager,
                                 PaymentClient paymentClient,
                                 InventoryClient inventoryClient,
                                 OrderRepository orderRepository) {
        this.stateMachineManager = stateMachineManager;
        this.paymentClient = paymentClient;
        this.inventoryClient = inventoryClient;
        this.orderRepository = orderRepository;
    }

    public void startSaga(Order order) {
        log.info("Saga order {} - Starting orchestration workflow", order.getOrderId());
        try {
            StateMachine<OrderState, OrderEvent> sm = stateMachineManager.build(order);
            sm.startReactively().block();

            log.info("Saga order {} - Sending CREATE_ORDER event", order.getOrderId());
            stateMachineManager.sendEvent(sm, OrderEvent.CREATE_ORDER, order.getOrderId());

            runPaymentStep(order, sm);

        } catch (Exception e) {
            log.error("Saga order {} - Saga startup failed with exception", order.getOrderId(), e);
            failOrder(order.getOrderId());
        }
    }

    public void resumeSaga(Order order) {
        log.info("Saga order {} - Resuming orchestration from state: {}", order.getOrderId(), order.getStatus());
        try {
            StateMachine<OrderState, OrderEvent> sm = stateMachineManager.restore(order.getOrderId());
            sm.startReactively().block();

            switch (order.getStatus()) {
                case ORDER_CREATED:
                    stateMachineManager.sendEvent(sm, OrderEvent.CREATE_ORDER, order.getOrderId());
                    runPaymentStep(order, sm);
                    break;
                case PAYMENT_PENDING:
                    runPaymentStep(order, sm);
                    break;
                case PAYMENT_COMPLETED:
                    runInventoryStep(order, sm);
                    break;
                case INVENTORY_RESERVED:
                    log.info("Saga order {} - Completing order from INVENTORY_RESERVED", order.getOrderId());
                    completeOrder(order.getOrderId());
                    break;
                default:
                    log.info("Saga order {} - Already in terminal state: {}", order.getOrderId(), order.getStatus());
            }
        } catch (Exception e) {
            log.error("Saga order {} - Saga recovery failed with exception", order.getOrderId(), e);
            failOrder(order.getOrderId());
        }
    }

    private void runPaymentStep(Order order, StateMachine<OrderState, OrderEvent> sm) {
        log.info("Saga order {} - Initiating payment request", order.getOrderId());
        boolean success = paymentClient.process(order.getOrderId());
        if (success) {
            log.info("Saga order {} - Payment success, sending PAYMENT_SUCCESS event", order.getOrderId());
            stateMachineManager.sendEvent(sm, OrderEvent.PAYMENT_SUCCESS, order.getOrderId());
            runInventoryStep(order, sm);
        } else {
            log.error("Saga order {} - Payment failed, sending PAYMENT_FAILED event", order.getOrderId());
            stateMachineManager.sendEvent(sm, OrderEvent.PAYMENT_FAILED, order.getOrderId());
            failOrder(order.getOrderId());
        }
    }

    private void runInventoryStep(Order order, StateMachine<OrderState, OrderEvent> sm) {
        log.info("Saga order {} - Initiating inventory reservation request", order.getOrderId());
        boolean success = inventoryClient.reserve(order.getOrderId());
        if (success) {
            log.info("Saga order {} - Inventory reserved, sending INVENTORY_SUCCESS event", order.getOrderId());
            stateMachineManager.sendEvent(sm, OrderEvent.INVENTORY_SUCCESS, order.getOrderId());
            completeOrder(order.getOrderId());
        } else {
            log.error("Saga order {} - Inventory reservation failed, sending INVENTORY_FAILED event", order.getOrderId());
            stateMachineManager.sendEvent(sm, OrderEvent.INVENTORY_FAILED, order.getOrderId());
            compensatePayment(order);
            failOrder(order.getOrderId());
        }
    }

    private void compensatePayment(Order order) {
        log.warn("Saga order {} - Initiating compensation: Cancelling payment", order.getOrderId());
        boolean success = paymentClient.cancel(order.getOrderId());
        if (!success) {
            log.error("Saga order {} - Payment compensation call failed", order.getOrderId());
        } else {
            log.info("Saga order {} - Payment compensation completed", order.getOrderId());
        }
    }

    private void completeOrder(String orderId) {
        log.info("Saga order {} - Order Completed successfully", orderId);
        orderRepository.updateStatus(orderId, OrderState.ORDER_COMPLETED);
    }

    private void failOrder(String orderId) {
        log.error("Saga order {} - Order Failed", orderId);
        orderRepository.updateStatus(orderId, OrderState.ORDER_FAILED);
    }
}
