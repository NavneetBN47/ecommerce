package com.ecommerce.controller;

import com.ecommerce.dto.ApiResponseDTO;
import com.ecommerce.dto.CreateOrderRequestDTO;
import com.ecommerce.dto.OrderDTO;
import com.ecommerce.entity.Order;
import com.ecommerce.service.OrderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * JUnit 5 test class for OrderController
 * Tests order management operations including create, retrieve, update status, and cancel
 * 
 * @author QA Automation Agent
 * @version 1.0.0
 */
@ExtendWith(MockitoExtension.class)
class test_OrderController {

    @Mock
    private OrderService orderService;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private OrderController orderController;

    private Long userId;
    private Long orderId;
    private String orderNumber;
    private OrderDTO orderDTO;

    @BeforeEach
    void setUp() {
        userId = 1L;
        orderId = 100L;
        orderNumber = "ORD-20240101-1234";
        
        orderDTO = OrderDTO.builder()
            .id(orderId)
            .orderNumber(orderNumber)
            .userId(userId)
            .totalAmount(BigDecimal.valueOf(250.00))
            .status(Order.OrderStatus.PENDING)
            .build();
    }

    /**
     * Test creating order successfully
     * Verifies HTTP 201 status and order data
     */
    @Test
    void createOrderShouldReturnCreatedStatus() {
        // Given
        CreateOrderRequestDTO request = new CreateOrderRequestDTO();
        request.setShippingAddress("123 Main St");
        request.setPaymentMethod("CREDIT_CARD");

        when(authentication.getName()).thenReturn(userId.toString());
        when(orderService.createOrder(eq(userId), any(CreateOrderRequestDTO.class)))
            .thenReturn(orderDTO);

        // When
        ResponseEntity<ApiResponseDTO<OrderDTO>> response = authController.createOrder(request, authentication);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Order created successfully", response.getBody().getMessage());
        assertEquals(orderDTO, response.getBody().getData());
        verify(orderService, times(1)).createOrder(eq(userId), any(CreateOrderRequestDTO.class));
    }

    /**
     * Test creating order from empty cart
     * Verifies proper exception handling
     */
    @Test
    void createOrderShouldHandleEmptyCart() {
        // Given
        CreateOrderRequestDTO request = new CreateOrderRequestDTO();
        request.setShippingAddress("123 Main St");

        when(authentication.getName()).thenReturn(userId.toString());
        when(orderService.createOrder(eq(userId), any(CreateOrderRequestDTO.class)))
            .thenThrow(new RuntimeException("Cannot create order from empty cart"));

        // When/Then
        assertThrows(RuntimeException.class, () -> {
            orderController.createOrder(request, authentication);
        });
    }

    /**
     * Test getting order by ID successfully
     * Verifies HTTP 200 status and order data
     */
    @Test
    void getOrderByIdShouldReturnOkStatus() {
        // Given
        when(orderService.getOrderById(orderId)).thenReturn(orderDTO);

        // When
        ResponseEntity<ApiResponseDTO<OrderDTO>> response = orderController.getOrderById(orderId);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(orderDTO, response.getBody().getData());
        verify(orderService, times(1)).getOrderById(orderId);
    }

    /**
     * Test getting non-existent order by ID
     * Verifies exception handling
     */
    @Test
    void getOrderByIdShouldHandleNonExistentOrder() {
        // Given
        Long nonExistentId = 999L;
        when(orderService.getOrderById(nonExistentId))
            .thenThrow(new RuntimeException("Order not found"));

        // When/Then
        assertThrows(RuntimeException.class, () -> {
            orderController.getOrderById(nonExistentId);
        });
    }

    /**
     * Test getting order by order number successfully
     * Verifies HTTP 200 status and order data
     */
    @Test
    void getOrderByOrderNumberShouldReturnOkStatus() {
        // Given
        when(orderService.getOrderByOrderNumber(orderNumber)).thenReturn(orderDTO);

        // When
        ResponseEntity<ApiResponseDTO<OrderDTO>> response = orderController.getOrderByOrderNumber(orderNumber);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(orderDTO, response.getBody().getData());
        verify(orderService, times(1)).getOrderByOrderNumber(orderNumber);
    }

    /**
     * Test getting order by invalid order number
     * Verifies exception handling
     */
    @Test
    void getOrderByOrderNumberShouldHandleInvalidOrderNumber() {
        // Given
        String invalidOrderNumber = "INVALID";
        when(orderService.getOrderByOrderNumber(invalidOrderNumber))
            .thenThrow(new RuntimeException("Order not found"));

        // When/Then
        assertThrows(RuntimeException.class, () -> {
            orderController.getOrderByOrderNumber(invalidOrderNumber);
        });
    }

    /**
     * Test getting user orders with pagination
     * Verifies HTTP 200 status and paginated results
     */
    @Test
    void getUserOrdersShouldReturnOkStatusWithPaginatedResults() {
        // Given
        List<OrderDTO> orders = new ArrayList<>();
        orders.add(orderDTO);
        Page<OrderDTO> orderPage = new PageImpl<>(orders, PageRequest.of(0, 10), 1);

        when(authentication.getName()).thenReturn(userId.toString());
        when(orderService.getUserOrders(eq(userId), any(Pageable.class)))
            .thenReturn(orderPage);

        // When
        ResponseEntity<ApiResponseDTO<Page<OrderDTO>>> response = 
            orderController.getUserOrders(0, 10, authentication);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().getData().getTotalElements());
        verify(orderService, times(1)).getUserOrders(eq(userId), any(Pageable.class));
    }

    /**
     * Test getting user orders with custom page size
     * Verifies pagination parameters are respected
     */
    @Test
    void getUserOrdersShouldRespectPaginationParameters() {
        // Given
        int page = 2;
        int size = 5;
        Page<OrderDTO> orderPage = new PageImpl<>(new ArrayList<>(), PageRequest.of(page, size), 0);

        when(authentication.getName()).thenReturn(userId.toString());
        when(orderService.getUserOrders(eq(userId), any(Pageable.class)))
            .thenReturn(orderPage);

        // When
        ResponseEntity<ApiResponseDTO<Page<OrderDTO>>> response = 
            orderController.getUserOrders(page, size, authentication);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(orderService, times(1)).getUserOrders(eq(userId), any(Pageable.class));
    }

    /**
     * Test updating order status successfully
     * Verifies HTTP 200 status and updated order
     */
    @Test
    void updateOrderStatusShouldReturnOkStatus() {
        // Given
        Order.OrderStatus newStatus = Order.OrderStatus.SHIPPED;
        OrderDTO updatedOrder = OrderDTO.builder()
            .id(orderId)
            .orderNumber(orderNumber)
            .status(newStatus)
            .build();

        when(orderService.updateOrderStatus(orderId, newStatus))
            .thenReturn(updatedOrder);

        // When
        ResponseEntity<ApiResponseDTO<OrderDTO>> response = 
            orderController.updateOrderStatus(orderId, newStatus);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Order status updated successfully", response.getBody().getMessage());
        assertEquals(newStatus, response.getBody().getData().getStatus());
        verify(orderService, times(1)).updateOrderStatus(orderId, newStatus);
    }

    /**
     * Test updating status of non-existent order
     * Verifies exception handling
     */
    @Test
    void updateOrderStatusShouldHandleNonExistentOrder() {
        // Given
        Long nonExistentId = 999L;
        when(orderService.updateOrderStatus(eq(nonExistentId), any(Order.OrderStatus.class)))
            .thenThrow(new RuntimeException("Order not found"));

        // When/Then
        assertThrows(RuntimeException.class, () -> {
            orderController.updateOrderStatus(nonExistentId, Order.OrderStatus.SHIPPED);
        });
    }

    /**
     * Test cancelling order successfully
     * Verifies HTTP 200 status and cancelled order
     */
    @Test
    void cancelOrderShouldReturnOkStatus() {
        // Given
        OrderDTO cancelledOrder = OrderDTO.builder()
            .id(orderId)
            .orderNumber(orderNumber)
            .status(Order.OrderStatus.CANCELLED)
            .build();

        when(orderService.cancelOrder(orderId)).thenReturn(cancelledOrder);

        // When
        ResponseEntity<ApiResponseDTO<OrderDTO>> response = orderController.cancelOrder(orderId);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Order cancelled successfully", response.getBody().getMessage());
        assertEquals(Order.OrderStatus.CANCELLED, response.getBody().getData().getStatus());
        verify(orderService, times(1)).cancelOrder(orderId);
    }

    /**
     * Test cancelling already delivered order
     * Verifies business rule validation
     */
    @Test
    void cancelOrderShouldHandleDeliveredOrder() {
        // Given
        when(orderService.cancelOrder(orderId))
            .thenThrow(new RuntimeException("Cannot cancel delivered order"));

        // When/Then
        assertThrows(RuntimeException.class, () -> {
            orderController.cancelOrder(orderId);
        });
    }

    /**
     * Test cancelling already cancelled order
     * Verifies idempotency handling
     */
    @Test
    void cancelOrderShouldHandleAlreadyCancelledOrder() {
        // Given
        when(orderService.cancelOrder(orderId))
            .thenThrow(new RuntimeException("Order already cancelled"));

        // When/Then
        assertThrows(RuntimeException.class, () -> {
            orderController.cancelOrder(orderId);
        });
    }
}