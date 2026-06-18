package com.example.orderservice.config;

import com.example.orderservice.entity.OrderEvent;
import com.example.orderservice.entity.OrderState;
import com.example.orderservice.repository.OrderRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.Message;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.state.State;
import org.springframework.statemachine.support.StateMachineInterceptorAdapter;
import org.springframework.statemachine.transition.Transition;
import org.springframework.stereotype.Component;

@Component
public class OrderStateChangeInterceptor extends StateMachineInterceptorAdapter<OrderState, OrderEvent> {

    private static final Logger log = LoggerFactory.getLogger(OrderStateChangeInterceptor.class);

    private final OrderRepository orderRepository;

    public OrderStateChangeInterceptor(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    @Override
    public void preStateChange(State<OrderState, OrderEvent> state, Message<OrderEvent> message,
                               Transition<OrderState, OrderEvent> transition, StateMachine<OrderState, OrderEvent> stateMachine,
                               StateMachine<OrderState, OrderEvent> rootStateMachine) {
        
        String orderId = null;
        if (message != null && message.getHeaders().containsKey("orderId")) {
            orderId = message.getHeaders().get("orderId", String.class);
        } else {
            orderId = (String) stateMachine.getExtendedState().getVariables().get("orderId");
        }

        if (orderId != null) {
            OrderState targetState = state.getId();
            orderRepository.updateStatus(orderId, targetState);

            OrderState sourceState = (transition != null && transition.getSource() != null)
                    ? transition.getSource().getId() : null;
            OrderEvent event = (message != null) ? message.getPayload() : null;

            log.info("Saga for order {} transitioning from {} to {} on event {}",
                    orderId, sourceState, targetState, event);
        }
    }
}
