package com.example.orderservice.service;

import com.example.orderservice.config.OrderStateChangeInterceptor;
import com.example.orderservice.entity.Order;
import com.example.orderservice.entity.OrderEvent;
import com.example.orderservice.entity.OrderState;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.config.StateMachineFactory;
import org.springframework.statemachine.persist.StateMachinePersister;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class OrderStateMachineManager {

    private final StateMachineFactory<OrderState, OrderEvent> stateMachineFactory;
    private final StateMachinePersister<OrderState, OrderEvent, String> persister;
    private final OrderStateChangeInterceptor interceptor;

    public OrderStateMachineManager(StateMachineFactory<OrderState, OrderEvent> stateMachineFactory,
                                    StateMachinePersister<OrderState, OrderEvent, String> persister,
                                    OrderStateChangeInterceptor interceptor) {
        this.stateMachineFactory = stateMachineFactory;
        this.persister = persister;
        this.interceptor = interceptor;
    }

    public StateMachine<OrderState, OrderEvent> build(Order order) throws Exception {
        StateMachine<OrderState, OrderEvent> sm = stateMachineFactory.getStateMachine(order.getOrderId());
        sm.stopReactively().block();
        sm.getStateMachineAccessor().doWithAllRegions(accessor -> accessor.addStateMachineInterceptor(interceptor));
        sm.getExtendedState().getVariables().put("orderId", order.getOrderId());
        sm.getExtendedState().getVariables().put("order", order);
        return sm;
    }

    public StateMachine<OrderState, OrderEvent> restore(String orderId) throws Exception {
        StateMachine<OrderState, OrderEvent> sm = stateMachineFactory.getStateMachine(orderId);
        sm.stopReactively().block();
        sm.getStateMachineAccessor().doWithAllRegions(accessor -> accessor.addStateMachineInterceptor(interceptor));
        sm = persister.restore(sm, orderId);
        return sm;
    }

    public void sendEvent(StateMachine<OrderState, OrderEvent> sm, OrderEvent event, String orderId) {
        Message<OrderEvent> message = MessageBuilder.withPayload(event)
                .setHeader("orderId", orderId)
                .build();
        sm.sendEvent(Mono.just(message)).blockLast();
    }
}
