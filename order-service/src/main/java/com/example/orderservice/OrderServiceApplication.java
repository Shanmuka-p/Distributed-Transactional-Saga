package com.example.orderservice;

import com.example.orderservice.entity.Order;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.statemachine.data.jpa.JpaRepositoryStateMachine;

@SpringBootApplication
@EntityScan(basePackageClasses = {
        Order.class,
        JpaRepositoryStateMachine.class
})
public class OrderServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(OrderServiceApplication.class, args);
    }
}
