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
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

/**
 * Test class for OrderController
 * 
 * Tests order operations including create, retrieve, update status, and cancel
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

    private CreateOrderRequestDTO createOrderRequest;
    private OrderDTO orderDTO;
    private Long userId;
    private Long orderId;
    private String orderNumber;

    @BeforeEach
    void setUp() {
        userId = 1L;
        orderId = 100L;
        orderNumber = "ORD-20240101-1234";

        createOrderRequest = new CreateOrderRequestDTO();
        createOrderRequest.setShippingAddress("123 Main St");
        createOrderRequest.setBillingAddress("123 Main St");
        createOrderRequest.setPaymentMethod("CREDIT_CARD");

        orderDTO = OrderDTO.builder()
            .id(orderId)
            .orderNumber(orderNumber)
            .userId(userId)
            .totalAmount(BigDecimal.valueOf(150.00))
            .status(Order.OrderStatus.PENDING)
            .shippingAddress("123 Main St")
            .paymentMethod("CREDIT_CARD")
            .paymentStatus("PENDING")
            .build();
    }

    /**
     * Test creating order successfully
     * 
     * Verifies:
     * - Order is created from cart
     * - Returns CREATED status
     * - OrderService.createOrder is called with correct parameters
     */
    @Test
    void testCreateOrder_Success() {
        // Given
        when(authentication.getName()).thenReturn(userId.toString());
        when(orderService.createOrder(anyLong(), any(CreateOrderRequestDTO.class)))
            .thenReturn(orderDTO);

        // When
        ResponseEntity<ApiResponseDTO<OrderDTO>> response = 
            orderController.createOrder(createOrderRequest, authentication);

        // Then
        assertNotNull(response, "Response should not be null");
        assertEquals(HttpStatus.CREATED, response.getStatusCode(), "Status should be CREATED");
        assertNotNull(response.getBody(), "Response body should not be null");
        assertEquals("Order created successfully", response.getBody().getMessage());
        assertEquals(orderDTO, response.getBody().getData());
        verify(orderService, times(1)).createOrder(userId, createOrderRequest);
    }

    /**
     * Test creating order with empty cart
     * 
     * Verifies:
     * - Appropriate exception is thrown for empty cart
     */
    @Test
    void testCreateOrder_EmptyCart() {
        // Given
        when(authentication.getName()).thenReturn(userId.toString());
        when(orderService.createOrder(anyLong(), any(CreateOrderRequestDTO.class)))
            .thenThrow(new RuntimeException("Cannot create order from empty cart"));

        // When/Then
        assertThrows(RuntimeException.class, () -> {
            orderController.createOrder(createOrderRequest, authentication);
        });
    }

    /**
     * Test getting order by ID successfully
     * 
     * Verifies:
     * - Order is retrieved by ID
     * - Returns OK status
     * - OrderService.getOrderById is called with correct ID
     */
    @Test
    void testGetOrderById_Success() {
        // Given
        when(orderService.getOrderById(anyLong())).thenReturn(orderDTO);

        // When
        ResponseEntity<ApiResponseDTO<OrderDTO>> response = orderController.getOrderById(orderId);

        // Then
        assertNotNull(response, "Response should not be null");
        assertEquals(HttpStatus.OK, response.getStatusCode(), "Status should be OK");
        assertNotNull(response.getBody(), "Response body should not be null");
        assertEquals(orderDTO, response.getBody().getData());
        verify(orderService, times(1)).getOrderById(orderId);
    }

    /**
     * Test getting order by non-existent ID
     * 
     * Verifies:
     * - Appropriate exception is thrown for non-existent order
     */
    @Test
    void testGetOrderById_NotFound() {
        // Given
        when(orderService.getOrderById(anyLong()))
            .thenThrow(new RuntimeException("Order not found"));

        // When/Then
        assertThrows(RuntimeException.class, () -> {
            orderController.getOrderById(999L);
        });
    }

    /**
     * Test getting order by order number successfully
     * 
     * Verifies:
     * - Order is retrieved by order number
     * - Returns OK status
     * - OrderService.getOrderByOrderNumber is called with correct number
     */
    @Test
    void testGetOrderByOrderNumber_Success() {
        // Given
        when(orderService.getOrderByOrderNumber(anyString())).thenReturn(orderDTO);

        // When
        ResponseEntity<ApiResponseDTO<OrderDTO>> response = 
            orderController.getOrderByOrderNumber(orderNumber);

        // Then
        assertNotNull(response, "Response should not be null");
        assertEquals(HttpStatus.OK, response.getStatusCode(), "Status should be OK");
        assertNotNull(response.getBody(), "Response body should not be null");
        assertEquals(orderDTO, response.getBody().getData());
        verify(orderService, times(1)).getOrderByOrderNumber(orderNumber);
    }

    /**
     * Test getting user orders with pagination
     * 
     * Verifies:
     * - User orders are retrieved with pagination
     * - Returns OK status
     * - OrderService.getUserOrders is called with correct parameters
     */
    @Test
    void testGetUserOrders_Success() {
        // Given
        List<OrderDTO> orders = new ArrayList<>();
        orders.add(orderDTO);
        Page<OrderDTO> orderPage = new PageImpl<>(orders, PageRequest.of(0, 10), 1);
        
        when(authentication.getName()).thenReturn(userId.toString());
        when(orderService.getUserOrders(anyLong(), any(Pageable.class)))
            .thenReturn(orderPage);

        // When
        ResponseEntity<ApiResponseDTO<Page<OrderDTO>>> response = 
            orderController.getUserOrders(0, 10, authentication);

        // Then
        assertNotNull(response, "Response should not be null");
        assertEquals(HttpStatus.OK, response.getStatusCode(), "Status should be OK");
        assertNotNull(response.getBody(), "Response body should not be null");
        assertEquals(1, response.getBody().getData().getTotalElements());
        verify(orderService, times(1)).getUserOrders(eq(userId), any(Pageable.class));
    }

    /**
     * Test getting user orders with custom pagination
     * 
     * Verifies:
     * - Custom page size and number are respected
     */
    @Test
    void testGetUserOrders_CustomPagination() {
        // Given
        List<OrderDTO> orders = new ArrayList<>();
        Page<OrderDTO> orderPage = new PageImpl<>(orders, PageRequest.of(2, 5), 0);
        
        when(authentication.getName()).thenReturn(userId.toString());
        when(orderService.getUserOrders(anyLong(), any(Pageable.class)))
            .thenReturn(orderPage);

        // When
        ResponseEntity<ApiResponseDTO<Page<OrderDTO>>> response = 
            orderController.getUserOrders(2, 5, authentication);

        // Then
        assertNotNull(response, "Response should not be null");
        assertEquals(HttpStatus.OK, response.getStatusCode(), "Status should be OK");
        verify(orderService, times(1)).getUserOrders(eq(userId), any(Pageable.class));
    }

    /**
     * Test updating order status successfully
     * 
     * Verifies:
     * - Order status is updated
     * - Returns OK status
     * - OrderService.updateOrderStatus is called with correct parameters
     */
    @Test
    void testUpdateOrderStatus_Success() {
        // Given
        Order.OrderStatus newStatus = Order.OrderStatus.SHIPPED;
        OrderDTO updatedOrder = OrderDTO.builder()
            .id(orderId)
            .orderNumber(orderNumber)
            .status(newStatus)
            .build();
        
        when(orderService.updateOrderStatus(anyLong(), any(Order.OrderStatus.class)))
            .thenReturn(updatedOrder);

        // When
        ResponseEntity<ApiResponseDTO<OrderDTO>> response = 
            orderController.updateOrderStatus(orderId, newStatus);

        // Then
        assertNotNull(response, "Response should not be null");
        assertEquals(HttpStatus.OK, response.getStatusCode(), "Status should be OK");
        assertNotNull(response.getBody(), "Response body should not be null");
        assertEquals("Order status updated successfully", response.getBody().getMessage());
        assertEquals(newStatus, response.getBody().getData().getStatus());
        verify(orderService, times(1)).updateOrderStatus(orderId, newStatus);
    }

    /**
     * Test cancelling order successfully
     * 
     * Verifies:
     * - Order is cancelled
     * - Returns OK status
     * - OrderService.cancelOrder is called with correct ID
     */
    @Test
    void testCancelOrder_Success() {
        // Given
        OrderDTO cancelledOrder = OrderDTO.builder()
            .id(orderId)
            .orderNumber(orderNumber)
            .status(Order.OrderStatus.CANCELLED)
            .build();
        
        when(orderService.cancelOrder(anyLong())).thenReturn(cancelledOrder);

        // When
        ResponseEntity<ApiResponseDTO<OrderDTO>> response = orderController.cancelOrder(orderId);

        // Then
        assertNotNull(response, "Response should not be null");
        assertEquals(HttpStatus.OK, response.getStatusCode(), "Status should be OK");
        assertNotNull(response.getBody(), "Response body should not be null");
        assertEquals("Order cancelled successfully", response.getBody().getMessage());
        assertEquals(Order.OrderStatus.CANCELLED, response.getBody().getData().getStatus());
        verify(orderService, times(1)).cancelOrder(orderId);
    }

    /**
     * Test cancelling already delivered order
     * 
     * Verifies:
     * - Appropriate exception is thrown for delivered orders
     */
    @Test
    void testCancelOrder_AlreadyDelivered() {
        // Given
        when(orderService.cancelOrder(anyLong()))
            .thenThrow(new RuntimeException("Cannot cancel order with status: DELIVERED"));

        // When/Then
        assertThrows(RuntimeException.class, () -> {
            orderController.cancelOrder(orderId);
        });
    }
}
