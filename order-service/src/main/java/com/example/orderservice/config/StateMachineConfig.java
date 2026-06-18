package com.example.orderservice.config;

import com.example.orderservice.entity.OrderEvent;
import com.example.orderservice.entity.OrderState;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.statemachine.config.EnableStateMachineFactory;
import org.springframework.statemachine.config.StateMachineConfigurerAdapter;
import org.springframework.statemachine.config.builders.StateMachineConfigurationConfigurer;
import org.springframework.statemachine.config.builders.StateMachineStateConfigurer;
import org.springframework.statemachine.config.builders.StateMachineTransitionConfigurer;
import org.springframework.statemachine.data.jpa.JpaPersistingStateMachineInterceptor;
import org.springframework.statemachine.data.jpa.JpaStateMachineRepository;
import org.springframework.statemachine.persist.DefaultStateMachinePersister;
import org.springframework.statemachine.persist.StateMachinePersister;
import org.springframework.statemachine.persist.StateMachineRuntimePersister;

import java.util.EnumSet;

@Configuration
@EnableStateMachineFactory
public class StateMachineConfig extends StateMachineConfigurerAdapter<OrderState, OrderEvent> {

    private final JpaStateMachineRepository jpaStateMachineRepository;

    public StateMachineConfig(JpaStateMachineRepository jpaStateMachineRepository) {
        this.jpaStateMachineRepository = jpaStateMachineRepository;
    }

    @Bean
    public StateMachineRuntimePersister<OrderState, OrderEvent, String> stateMachineRuntimePersister() {
        return new JpaPersistingStateMachineInterceptor<>(jpaStateMachineRepository);
    }

    @Bean
    public StateMachinePersister<OrderState, OrderEvent, String> stateMachinePersister(
            StateMachineRuntimePersister<OrderState, OrderEvent, String> stateMachineRuntimePersister) {
        return new DefaultStateMachinePersister<>(stateMachineRuntimePersister);
    }

    @Override
    public void configure(StateMachineConfigurationConfigurer<OrderState, OrderEvent> config) throws Exception {
        config
            .withConfiguration()
                .autoStartup(false)
                .and()
            .withPersistence()
                .runtimePersister(stateMachineRuntimePersister());
    }

    @Override
    public void configure(StateMachineStateConfigurer<OrderState, OrderEvent> states) throws Exception {
        states
            .withStates()
            .initial(OrderState.ORDER_CREATED)
            .states(EnumSet.allOf(OrderState.class));
    }

    @Override
    public void configure(StateMachineTransitionConfigurer<OrderState, OrderEvent> transitions) throws Exception {
        transitions
            .withExternal()
                .source(OrderState.ORDER_CREATED)
                .target(OrderState.PAYMENT_PENDING)
                .event(OrderEvent.CREATE_ORDER)
                .and()
            .withExternal()
                .source(OrderState.PAYMENT_PENDING)
                .target(OrderState.PAYMENT_COMPLETED)
                .event(OrderEvent.PAYMENT_SUCCESS)
                .and()
            .withExternal()
                .source(OrderState.PAYMENT_PENDING)
                .target(OrderState.ORDER_FAILED)
                .event(OrderEvent.PAYMENT_FAILED)
                .and()
            .withExternal()
                .source(OrderState.PAYMENT_COMPLETED)
                .target(OrderState.INVENTORY_RESERVED)
                .event(OrderEvent.INVENTORY_SUCCESS)
                .and()
            .withExternal()
                .source(OrderState.PAYMENT_COMPLETED)
                .target(OrderState.ORDER_FAILED)
                .event(OrderEvent.INVENTORY_FAILED)
                .and()
            .withExternal()
                .source(OrderState.INVENTORY_RESERVED)
                .target(OrderState.ORDER_COMPLETED);
    }
}
