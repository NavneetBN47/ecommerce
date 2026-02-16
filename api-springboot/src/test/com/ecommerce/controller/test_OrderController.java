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
 * JUnit test class for OrderController.
 * Tests order management operations including creation, retrieval, status updates, and cancellation.
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
        orderDTO.setTotalAmount(BigDecimal.valueOf(150.00));
        orderDTO.setStatus(Order.OrderStatus.PENDING);
    }

    /**
     * Test successfully creating an order.
     * Verifies that an order can be created from cart and returns CREATED status.
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
        assertEquals(orderNumber, response.getBody().getData().getOrderNumber());
        verify(orderService, times(1)).createOrder(eq(userId), any(CreateOrderRequestDTO.class));
        verify(authentication, times(1)).getName();
    }

    /**
     * Test creating order with null request.
     * Verifies proper handling of null input.
     */
    @Test
    void testCreateOrder_NullRequest() {
        when(authentication.getName()).thenReturn(userId.toString());
        when(orderService.createOrder(any(Long.class), eq(null)))
            .thenThrow(new IllegalArgumentException("Order request cannot be null"));

        assertThrows(IllegalArgumentException.class, 
            () -> orderController.createOrder(null, authentication));
        verify(authentication, times(1)).getName();
    }

    /**
     * Test creating order with empty cart.
     * Verifies proper exception handling for empty cart.
     */
    @Test
    void testCreateOrder_EmptyCart() {
        when(authentication.getName()).thenReturn(userId.toString());
        when(orderService.createOrder(any(Long.class), any(CreateOrderRequestDTO.class)))
            .thenThrow(new RuntimeException("Cart is empty"));

        assertThrows(RuntimeException.class, 
            () -> orderController.createOrder(createOrderRequestDTO, authentication));
        verify(orderService, times(1)).createOrder(eq(userId), any(CreateOrderRequestDTO.class));
    }

    /**
     * Test successfully retrieving order by ID.
     * Verifies that an order can be fetched by its ID.
     */
    @Test
    void testGetOrderById_Success() {
        when(orderService.getOrderById(any(Long.class))).thenReturn(orderDTO);

        ResponseEntity<ApiResponseDTO<OrderDTO>> response = orderController.getOrderById(orderId);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(orderDTO, response.getBody().getData());
        assertEquals(orderId, response.getBody().getData().getId());
        verify(orderService, times(1)).getOrderById(eq(orderId));
    }

    /**
     * Test retrieving non-existent order by ID.
     * Verifies proper exception handling for missing order.
     */
    @Test
    void testGetOrderById_NotFound() {
        when(orderService.getOrderById(any(Long.class)))
            .thenThrow(new RuntimeException("Order not found"));

        assertThrows(RuntimeException.class, () -> orderController.getOrderById(999L));
        verify(orderService, times(1)).getOrderById(eq(999L));
    }

    /**
     * Test successfully retrieving order by order number.
     * Verifies that an order can be fetched by its order number.
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
        assertEquals(orderNumber, response.getBody().getData().getOrderNumber());
        verify(orderService, times(1)).getOrderByOrderNumber(eq(orderNumber));
    }

    /**
     * Test retrieving order with invalid order number.
     * Verifies proper exception handling for invalid order number.
     */
    @Test
    void testGetOrderByOrderNumber_InvalidNumber() {
        when(orderService.getOrderByOrderNumber(any(String.class)))
            .thenThrow(new RuntimeException("Invalid order number"));

        assertThrows(RuntimeException.class, 
            () -> orderController.getOrderByOrderNumber("INVALID"));
        verify(orderService, times(1)).getOrderByOrderNumber(eq("INVALID"));
    }

    /**
     * Test successfully retrieving user orders with pagination.
     * Verifies that user orders can be fetched with pagination support.
     */
    @Test
    void testGetUserOrders_Success() {
        when(authentication.getName()).thenReturn(userId.toString());
        List<OrderDTO> orderList = Arrays.asList(orderDTO);
        Page<OrderDTO> orderPage = new PageImpl<>(orderList, PageRequest.of(0, 10), 1);
        when(orderService.getUserOrders(any(Long.class), any(Pageable.class)))
            .thenReturn(orderPage);

        ResponseEntity<ApiResponseDTO<Page<OrderDTO>>> response = 
            orderController.getUserOrders(0, 10, authentication);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().getData().getTotalElements());
        assertEquals(orderDTO, response.getBody().getData().getContent().get(0));
        verify(orderService, times(1)).getUserOrders(eq(userId), any(Pageable.class));
        verify(authentication, times(1)).getName();
    }

    /**
     * Test retrieving user orders with custom page size.
     * Verifies pagination with different page sizes.
     */
    @Test
    void testGetUserOrders_CustomPageSize() {
        when(authentication.getName()).thenReturn(userId.toString());
        List<OrderDTO> orderList = Arrays.asList(orderDTO);
        Page<OrderDTO> orderPage = new PageImpl<>(orderList, PageRequest.of(0, 5), 1);
        when(orderService.getUserOrders(any(Long.class), any(Pageable.class)))
            .thenReturn(orderPage);

        ResponseEntity<ApiResponseDTO<Page<OrderDTO>>> response = 
            orderController.getUserOrders(0, 5, authentication);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(5, response.getBody().getData().getPageable().getPageSize());
        verify(orderService, times(1)).getUserOrders(eq(userId), any(Pageable.class));
    }

    /**
     * Test successfully updating order status.
     * Verifies that order status can be updated (admin operation).
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
     * Test updating order status with invalid status.
     * Verifies proper exception handling for invalid status transitions.
     */
    @Test
    void testUpdateOrderStatus_InvalidStatus() {
        when(orderService.updateOrderStatus(any(Long.class), any(Order.OrderStatus.class)))
            .thenThrow(new RuntimeException("Invalid status transition"));

        assertThrows(RuntimeException.class, 
            () -> orderController.updateOrderStatus(orderId, Order.OrderStatus.DELIVERED));
        verify(orderService, times(1)).updateOrderStatus(eq(orderId), eq(Order.OrderStatus.DELIVERED));
    }

    /**
     * Test successfully cancelling an order.
     * Verifies that an order can be cancelled.
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
     * Test cancelling already shipped order.
     * Verifies proper exception handling for invalid cancellation.
     */
    @Test
    void testCancelOrder_AlreadyShipped() {
        when(orderService.cancelOrder(any(Long.class)))
            .thenThrow(new RuntimeException("Cannot cancel shipped order"));

        assertThrows(RuntimeException.class, () -> orderController.cancelOrder(orderId));
        verify(orderService, times(1)).cancelOrder(eq(orderId));
    }

    /**
     * Test cancelling non-existent order.
     * Verifies proper exception handling for missing order.
     */
    @Test
    void testCancelOrder_NotFound() {
        when(orderService.cancelOrder(any(Long.class)))
            .thenThrow(new RuntimeException("Order not found"));

        assertThrows(RuntimeException.class, () -> orderController.cancelOrder(999L));
        verify(orderService, times(1)).cancelOrder(eq(999L));
    }
}