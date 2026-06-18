package com.example.orderservice;

import com.example.orderservice.entity.OrderEvent;
import com.example.orderservice.entity.OrderState;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.config.StateMachineFactory;
import org.springframework.test.context.ActiveProfiles;
import reactor.core.publisher.Mono;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@ActiveProfiles("test")
public class StateMachineTransitionTest {

    @Autowired
    private StateMachineFactory<OrderState, OrderEvent> stateMachineFactory;

    @Test
    public void testTransitions() {
        StateMachine<OrderState, OrderEvent> sm = stateMachineFactory.getStateMachine("sm-test");
        sm.stopReactively().block();
        sm.startReactively().block();

        assertEquals(OrderState.ORDER_CREATED, sm.getState().getId());

        sm.sendEvent(Mono.just(MessageBuilder.withPayload(OrderEvent.CREATE_ORDER).build())).blockLast();
        assertEquals(OrderState.PAYMENT_PENDING, sm.getState().getId());

        sm.sendEvent(Mono.just(MessageBuilder.withPayload(OrderEvent.PAYMENT_SUCCESS).build())).blockLast();
        assertEquals(OrderState.PAYMENT_COMPLETED, sm.getState().getId());

        sm.sendEvent(Mono.just(MessageBuilder.withPayload(OrderEvent.INVENTORY_SUCCESS).build())).blockLast();
        assertEquals(OrderState.ORDER_COMPLETED, sm.getState().getId());
    }

    @Test
    public void testTransitionsFailure() {
        StateMachine<OrderState, OrderEvent> sm = stateMachineFactory.getStateMachine("sm-test-fail");
        sm.stopReactively().block();
        sm.startReactively().block();

        assertEquals(OrderState.ORDER_CREATED, sm.getState().getId());

        sm.sendEvent(Mono.just(MessageBuilder.withPayload(OrderEvent.CREATE_ORDER).build())).blockLast();
        assertEquals(OrderState.PAYMENT_PENDING, sm.getState().getId());

        sm.sendEvent(Mono.just(MessageBuilder.withPayload(OrderEvent.PAYMENT_FAILED).build())).blockLast();
        assertEquals(OrderState.ORDER_FAILED, sm.getState().getId());
    }
}
