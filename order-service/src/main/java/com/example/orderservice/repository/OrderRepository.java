package com.example.orderservice.repository;

import com.example.orderservice.entity.Order;
import com.example.orderservice.entity.OrderState;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface OrderRepository extends JpaRepository<Order, String> {

    List<Order> findByStatusIn(List<OrderState> statuses);

    @Modifying
    @Transactional
    @Query("update Order o set o.status = :status where o.orderId = :orderId")
    void updateStatus(String orderId, OrderState status);
}
