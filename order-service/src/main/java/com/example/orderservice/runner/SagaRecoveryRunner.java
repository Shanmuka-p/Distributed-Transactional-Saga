package com.example.orderservice.runner;

import com.example.orderservice.entity.Order;
import com.example.orderservice.entity.OrderState;
import com.example.orderservice.repository.OrderRepository;
import com.example.orderservice.service.OrderSagaOrchestrator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class SagaRecoveryRunner implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(SagaRecoveryRunner.class);

    private final OrderRepository orderRepository;
    private final OrderSagaOrchestrator orchestrator;

    public SagaRecoveryRunner(OrderRepository orderRepository, OrderSagaOrchestrator orchestrator) {
        this.orderRepository = orderRepository;
        this.orchestrator = orchestrator;
    }

    @Override
    public void run(String... args) {
        log.info("Starting Saga Orchestrator recovery scanner...");
        try {
            List<Order> activeOrders = orderRepository.findByStatusIn(List.of(
                    OrderState.ORDER_CREATED,
                    OrderState.PAYMENT_PENDING,
                    OrderState.PAYMENT_COMPLETED,
                    OrderState.INVENTORY_RESERVED
            ));

            if (activeOrders.isEmpty()) {
                log.info("No active Sagas found requiring recovery.");
                return;
            }

            log.info("Found {} active Sagas requiring recovery.", activeOrders.size());
            for (Order order : activeOrders) {
                log.info("Recovering active Saga for order {} currently in state {}", order.getOrderId(), order.getStatus());
                try {
                    orchestrator.resumeSaga(order);
                } catch (Exception e) {
                    log.error("Failed to recover order saga for order: {}", order.getOrderId(), e);
                }
            }
            log.info("Saga Orchestrator recovery checks completed.");
        } catch (Exception e) {
            log.error("Saga recovery runner failed", e);
        }
    }
}
