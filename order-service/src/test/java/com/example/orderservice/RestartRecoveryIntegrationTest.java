package com.example.orderservice;

import com.example.orderservice.client.InventoryClient;
import com.example.orderservice.client.PaymentClient;
import com.example.orderservice.entity.Order;
import com.example.orderservice.entity.OrderEvent;
import com.example.orderservice.entity.OrderState;
import com.example.orderservice.repository.OrderRepository;
import com.example.orderservice.service.OrderSagaOrchestrator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.config.StateMachineFactory;
import org.springframework.statemachine.persist.StateMachinePersister;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@SpringBootTest
@ActiveProfiles("test")
public class RestartRecoveryIntegrationTest {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderSagaOrchestrator orchestrator;

    @Autowired
    private StateMachinePersister<OrderState, OrderEvent, String> persister;

    @Autowired
    private StateMachineFactory<OrderState, OrderEvent> stateMachineFactory;

    @MockBean
    private PaymentClient paymentClient;

    @MockBean
    private InventoryClient inventoryClient;

    @Test
    public void testRestartRecovery() throws Exception {
        String orderId = "recovery-test-123";
        
        Order order = Order.builder()
                .orderId(orderId)
                .customerId(1L)
                .productId(1L)
                .quantity(2)
                .unitPrice(BigDecimal.valueOf(50))
                .amount(BigDecimal.valueOf(100))
                .status(OrderState.PAYMENT_PENDING)
                .build();
        order = orderRepository.save(order);

        StateMachine<OrderState, OrderEvent> sm = stateMachineFactory.getStateMachine(orderId);
        sm.stopReactively().block();
        sm.getExtendedState().getVariables().put("orderId", orderId);
        sm.getExtendedState().getVariables().put("order", order);
        persister.persist(sm, orderId);

        when(paymentClient.process(orderId)).thenReturn(true);
        when(inventoryClient.reserve(orderId)).thenReturn(true);

        orchestrator.resumeSaga(order);

        Order updatedOrder = orderRepository.findById(orderId).orElseThrow();
        assertEquals(OrderState.ORDER_COMPLETED, updatedOrder.getStatus());
        
        verify(paymentClient, times(1)).process(orderId);
        verify(inventoryClient, times(1)).reserve(orderId);
    }
}
