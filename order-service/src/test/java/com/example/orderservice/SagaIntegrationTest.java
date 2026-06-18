package com.example.orderservice;

import com.example.orderservice.client.InventoryClient;
import com.example.orderservice.client.PaymentClient;
import com.example.orderservice.dto.CreateOrderRequest;
import com.example.orderservice.dto.OrderResponse;
import com.example.orderservice.entity.OrderState;
import com.example.orderservice.service.OrderService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@SpringBootTest
@ActiveProfiles("test")
public class SagaIntegrationTest {

    @Autowired
    private OrderService orderService;

    @MockBean
    private PaymentClient paymentClient;

    @MockBean
    private InventoryClient inventoryClient;

    @Test
    public void testSagaSuccessFlow() {
        String orderId = "100";
        
        when(paymentClient.process(orderId)).thenReturn(true);
        when(inventoryClient.reserve(orderId)).thenReturn(true);

        CreateOrderRequest request = new CreateOrderRequest(orderId, 1L, 1L, 2, BigDecimal.valueOf(50.00));
        OrderResponse response = orderService.createOrder(request);

        assertEquals(OrderState.ORDER_COMPLETED.name(), response.getStatus());
        verify(paymentClient, times(1)).process(orderId);
        verify(inventoryClient, times(1)).reserve(orderId);
        verify(paymentClient, never()).cancel(orderId);
        verify(inventoryClient, never()).release(orderId);
    }

    @Test
    public void testPaymentFailureFlow() {
        String orderId = "201";

        when(paymentClient.process(orderId)).thenReturn(false);

        CreateOrderRequest request = new CreateOrderRequest(orderId, 1L, 1L, 2, BigDecimal.valueOf(50.00));
        OrderResponse response = orderService.createOrder(request);

        assertEquals(OrderState.ORDER_FAILED.name(), response.getStatus());
        verify(paymentClient, times(1)).process(orderId);
        verify(inventoryClient, never()).reserve(orderId);
        verify(paymentClient, never()).cancel(orderId);
    }

    @Test
    public void testInventoryFailureFlow() {
        String orderId = "302";

        when(paymentClient.process(orderId)).thenReturn(true);
        when(inventoryClient.reserve(orderId)).thenReturn(false);
        when(paymentClient.cancel(orderId)).thenReturn(true);

        CreateOrderRequest request = new CreateOrderRequest(orderId, 1L, 1L, 2, BigDecimal.valueOf(50.00));
        OrderResponse response = orderService.createOrder(request);

        assertEquals(OrderState.ORDER_FAILED.name(), response.getStatus());
        verify(paymentClient, times(1)).process(orderId);
        verify(inventoryClient, times(1)).reserve(orderId);
        verify(paymentClient, times(1)).cancel(orderId);
    }
}
