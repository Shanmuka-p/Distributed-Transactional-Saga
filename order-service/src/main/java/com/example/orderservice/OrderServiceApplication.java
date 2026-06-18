package com.example.orderservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repositories.EnableJpaRepositories;

@SpringBootApplication(scanBasePackages = {
        "com.example.orderservice",
        "org.springframework.statemachine.data.jpa"
})
@EntityScan(basePackages = {
        "com.example.orderservice.entity",
        "org.springframework.statemachine.data.jpa"
})
@EnableJpaRepositories(basePackages = {
        "com.example.orderservice.repository",
        "org.springframework.statemachine.data.jpa"
})
public class OrderServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(OrderServiceApplication.class, args);
    }
}
