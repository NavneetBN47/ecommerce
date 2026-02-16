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
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Unit test class for OrderController
 * Tests order operations including creation, retrieval, status updates, and cancellation
 */
@ExtendWith(MockitoExtension.class)
class test_OrderController {

    @Mock
    private OrderService orderService;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private OrderController orderController;

    private CreateOrderRequestDTO createOrderRequestDTO;
    private OrderDTO orderDTO;
    private Long userId;
    private Long orderId;
    private String orderNumber;

    @BeforeEach
    void setUp() {
        userId = 1L;
        orderId = 100L;
        orderNumber = "ORD-2024-001";

        createOrderRequestDTO = new CreateOrderRequestDTO();
        createOrderRequestDTO.setShippingAddress("123 Main St");
        createOrderRequestDTO.setPaymentMethod("CREDIT_CARD");

        orderDTO = new OrderDTO();
        orderDTO.setId(orderId);
        orderDTO.setOrderNumber(orderNumber);
        orderDTO.setUserId(userId);
        orderDTO.setTotalAmount(BigDecimal.valueOf(250.00));
        orderDTO.setStatus(Order.OrderStatus.PENDING);
    }

    /**
     * Test creating order successfully
     * Verifies that an order can be created from cart
     */
    @Test
    void testCreateOrder_Success() {
        when(authentication.getName()).thenReturn(userId.toString());
        when(orderService.createOrder(any(Long.class), any(CreateOrderRequestDTO.class)))
            .thenReturn(orderDTO);

        ResponseEntity<ApiResponseDTO<OrderDTO>> response = 
            orderController.createOrder(createOrderRequestDTO, authentication);

        assertNotNull(response);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Order created successfully", response.getBody().getMessage());
        assertEquals(orderDTO, response.getBody().getData());
        verify(orderService, times(1)).createOrder(eq(userId), any(CreateOrderRequestDTO.class));
    }

    /**
     * Test creating order with null request
     * Verifies proper handling of null input
     */
    @Test
    void testCreateOrder_NullRequest() {
        when(authentication.getName()).thenReturn(userId.toString());
        when(orderService.createOrder(any(Long.class), eq(null)))
            .thenThrow(new IllegalArgumentException("Order request cannot be null"));

        assertThrows(IllegalArgumentException.class, () -> {
            orderController.createOrder(null, authentication);
        });
    }

    /**
     * Test creating order with empty cart
     * Verifies proper error handling for empty cart
     */
    @Test
    void testCreateOrder_EmptyCart() {
        when(authentication.getName()).thenReturn(userId.toString());
        when(orderService.createOrder(any(Long.class), any(CreateOrderRequestDTO.class)))
            .thenThrow(new RuntimeException("Cart is empty"));

        assertThrows(RuntimeException.class, () -> {
            orderController.createOrder(createOrderRequestDTO, authentication);
        });
    }

    /**
     * Test getting order by ID successfully
     * Verifies that an order can be retrieved by its ID
     */
    @Test
    void testGetOrderById_Success() {
        when(orderService.getOrderById(any(Long.class))).thenReturn(orderDTO);

        ResponseEntity<ApiResponseDTO<OrderDTO>> response = orderController.getOrderById(orderId);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(orderDTO, response.getBody().getData());
        verify(orderService, times(1)).getOrderById(eq(orderId));
    }

    /**
     * Test getting order by non-existent ID
     * Verifies proper error handling for non-existent order
     */
    @Test
    void testGetOrderById_NotFound() {
        when(orderService.getOrderById(any(Long.class)))
            .thenThrow(new RuntimeException("Order not found"));

        assertThrows(RuntimeException.class, () -> {
            orderController.getOrderById(999L);
        });
    }

    /**
     * Test getting order by order number successfully
     * Verifies that an order can be retrieved by its order number
     */
    @Test
    void testGetOrderByOrderNumber_Success() {
        when(orderService.getOrderByOrderNumber(any(String.class))).thenReturn(orderDTO);

        ResponseEntity<ApiResponseDTO<OrderDTO>> response = 
            orderController.getOrderByOrderNumber(orderNumber);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(orderDTO, response.getBody().getData());
        verify(orderService, times(1)).getOrderByOrderNumber(eq(orderNumber));
    }

    /**
     * Test getting order by invalid order number
     * Verifies proper error handling for invalid order number
     */
    @Test
    void testGetOrderByOrderNumber_InvalidNumber() {
        when(orderService.getOrderByOrderNumber(any(String.class)))
            .thenThrow(new RuntimeException("Invalid order number"));

        assertThrows(RuntimeException.class, () -> {
            orderController.getOrderByOrderNumber("INVALID");
        });
    }

    /**
     * Test getting user orders successfully
     * Verifies that all orders for a user can be retrieved with pagination
     */
    @Test
    void testGetUserOrders_Success() {
        when(authentication.getName()).thenReturn(userId.toString());
        List<OrderDTO> orders = Arrays.asList(orderDTO);
        Page<OrderDTO> orderPage = new PageImpl<>(orders, PageRequest.of(0, 10), 1);
        when(orderService.getUserOrders(any(Long.class), any(Pageable.class)))
            .thenReturn(orderPage);

        ResponseEntity<ApiResponseDTO<Page<OrderDTO>>> response = 
            orderController.getUserOrders(0, 10, authentication);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().getData().getTotalElements());
        verify(orderService, times(1)).getUserOrders(eq(userId), any(Pageable.class));
    }

    /**
     * Test getting user orders with custom pagination
     * Verifies proper pagination handling
     */
    @Test
    void testGetUserOrders_CustomPagination() {
        when(authentication.getName()).thenReturn(userId.toString());
        List<OrderDTO> orders = Arrays.asList(orderDTO);
        Page<OrderDTO> orderPage = new PageImpl<>(orders, PageRequest.of(1, 5), 10);
        when(orderService.getUserOrders(any(Long.class), any(Pageable.class)))
            .thenReturn(orderPage);

        ResponseEntity<ApiResponseDTO<Page<OrderDTO>>> response = 
            orderController.getUserOrders(1, 5, authentication);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(orderService, times(1)).getUserOrders(eq(userId), any(Pageable.class));
    }

    /**
     * Test updating order status successfully
     * Verifies that order status can be updated
     */
    @Test
    void testUpdateOrderStatus_Success() {
        Order.OrderStatus newStatus = Order.OrderStatus.SHIPPED;
        orderDTO.setStatus(newStatus);
        when(orderService.updateOrderStatus(any(Long.class), any(Order.OrderStatus.class)))
            .thenReturn(orderDTO);

        ResponseEntity<ApiResponseDTO<OrderDTO>> response = 
            orderController.updateOrderStatus(orderId, newStatus);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Order status updated successfully", response.getBody().getMessage());
        assertEquals(newStatus, response.getBody().getData().getStatus());
        verify(orderService, times(1)).updateOrderStatus(eq(orderId), eq(newStatus));
    }

    /**
     * Test updating order status with invalid status
     * Verifies proper validation of order status
     */
    @Test
    void testUpdateOrderStatus_InvalidStatus() {
        when(orderService.updateOrderStatus(any(Long.class), any(Order.OrderStatus.class)))
            .thenThrow(new IllegalArgumentException("Invalid order status"));

        assertThrows(IllegalArgumentException.class, () -> {
            orderController.updateOrderStatus(orderId, Order.OrderStatus.CANCELLED);
        });
    }

    /**
     * Test cancelling order successfully
     * Verifies that an order can be cancelled
     */
    @Test
    void testCancelOrder_Success() {
        orderDTO.setStatus(Order.OrderStatus.CANCELLED);
        when(orderService.cancelOrder(any(Long.class))).thenReturn(orderDTO);

        ResponseEntity<ApiResponseDTO<OrderDTO>> response = orderController.cancelOrder(orderId);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Order cancelled successfully", response.getBody().getMessage());
        assertEquals(Order.OrderStatus.CANCELLED, response.getBody().getData().getStatus());
        verify(orderService, times(1)).cancelOrder(eq(orderId));
    }

    /**
     * Test cancelling already shipped order
     * Verifies proper error handling for invalid cancellation
     */
    @Test
    void testCancelOrder_AlreadyShipped() {
        when(orderService.cancelOrder(any(Long.class)))
            .thenThrow(new RuntimeException("Cannot cancel shipped order"));

        assertThrows(RuntimeException.class, () -> {
            orderController.cancelOrder(orderId);
        });
    }

    /**
     * Test cancelling non-existent order
     * Verifies proper error handling for non-existent order
     */
    @Test
    void testCancelOrder_NotFound() {
        when(orderService.cancelOrder(any(Long.class)))
            .thenThrow(new RuntimeException("Order not found"));

        assertThrows(RuntimeException.class, () -> {
            orderController.cancelOrder(999L);
        });
    }
}