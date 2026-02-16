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
 * JUnit 5 test class for OrderController.
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

    private CreateOrderRequestDTO createOrderRequest;
    private OrderDTO orderDTO;
    private Long userId;
    private Long orderId;
    private String orderNumber;

    /**
     * Set up test data before each test method execution.
     */
    @BeforeEach
    void setUp() {
        userId = 1L;
        orderId = 100L;
        orderNumber = "ORD-2024-001";

        createOrderRequest = new CreateOrderRequestDTO();
        createOrderRequest.setShippingAddress("123 Main St, City, Country");
        createOrderRequest.setPaymentMethod("CREDIT_CARD");

        orderDTO = new OrderDTO();
        orderDTO.setId(orderId);
        orderDTO.setOrderNumber(orderNumber);
        orderDTO.setUserId(userId);
        orderDTO.setTotalAmount(BigDecimal.valueOf(250.00));
        orderDTO.setStatus(Order.OrderStatus.PENDING);
    }

    /**
     * Test successfully creating an order from cart.
     * Verifies that order is created and returns HTTP 201 status.
     */
    @Test
    void testCreateOrder_Success() {
        when(authentication.getName()).thenReturn(userId.toString());
        when(orderService.createOrder(any(Long.class), any(CreateOrderRequestDTO.class)))
            .thenReturn(orderDTO);

        ResponseEntity<ApiResponseDTO<OrderDTO>> response = 
            orderController.createOrder(createOrderRequest, authentication);

        assertNotNull(response);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isSuccess());
        assertEquals("Order created successfully", response.getBody().getMessage());
        assertEquals(orderDTO, response.getBody().getData());
        verify(orderService, times(1)).createOrder(eq(userId), any(CreateOrderRequestDTO.class));
    }

    /**
     * Test creating order with null authentication.
     * Verifies proper handling of unauthenticated request.
     */
    @Test
    void testCreateOrder_NullAuthentication() {
        assertThrows(NullPointerException.class, () -> {
            orderController.createOrder(createOrderRequest, null);
        });
    }

    /**
     * Test creating order with empty cart.
     * Verifies handling when user's cart is empty.
     */
    @Test
    void testCreateOrder_EmptyCart() {
        when(authentication.getName()).thenReturn(userId.toString());
        when(orderService.createOrder(any(Long.class), any(CreateOrderRequestDTO.class)))
            .thenThrow(new RuntimeException("Cart is empty"));

        assertThrows(RuntimeException.class, () -> {
            orderController.createOrder(createOrderRequest, authentication);
        });
        verify(orderService, times(1)).createOrder(eq(userId), any(CreateOrderRequestDTO.class));
    }

    /**
     * Test creating order with invalid payment method.
     * Verifies validation of payment method.
     */
    @Test
    void testCreateOrder_InvalidPaymentMethod() {
        CreateOrderRequestDTO invalidRequest = new CreateOrderRequestDTO();
        invalidRequest.setShippingAddress("123 Main St");
        invalidRequest.setPaymentMethod("");

        when(authentication.getName()).thenReturn(userId.toString());
        when(orderService.createOrder(any(Long.class), any(CreateOrderRequestDTO.class)))
            .thenThrow(new IllegalArgumentException("Invalid payment method"));

        assertThrows(IllegalArgumentException.class, () -> {
            orderController.createOrder(invalidRequest, authentication);
        });
    }

    /**
     * Test successfully retrieving order by ID.
     * Verifies that order details are returned correctly.
     */
    @Test
    void testGetOrderById_Success() {
        when(orderService.getOrderById(any(Long.class))).thenReturn(orderDTO);

        ResponseEntity<ApiResponseDTO<OrderDTO>> response = orderController.getOrderById(orderId);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isSuccess());
        assertEquals(orderDTO, response.getBody().getData());
        verify(orderService, times(1)).getOrderById(eq(orderId));
    }

    /**
     * Test retrieving order with null ID.
     * Verifies proper handling of null order ID.
     */
    @Test
    void testGetOrderById_NullId() {
        when(orderService.getOrderById(null))
            .thenThrow(new IllegalArgumentException("Order ID cannot be null"));

        assertThrows(IllegalArgumentException.class, () -> {
            orderController.getOrderById(null);
        });
    }

    /**
     * Test retrieving non-existent order by ID.
     * Verifies handling of invalid order ID.
     */
    @Test
    void testGetOrderById_NotFound() {
        when(orderService.getOrderById(any(Long.class)))
            .thenThrow(new RuntimeException("Order not found"));

        assertThrows(RuntimeException.class, () -> {
            orderController.getOrderById(orderId);
        });
        verify(orderService, times(1)).getOrderById(eq(orderId));
    }

    /**
     * Test successfully retrieving order by order number.
     * Verifies that order can be retrieved using order number.
     */
    @Test
    void testGetOrderByOrderNumber_Success() {
        when(orderService.getOrderByOrderNumber(any(String.class))).thenReturn(orderDTO);

        ResponseEntity<ApiResponseDTO<OrderDTO>> response = 
            orderController.getOrderByOrderNumber(orderNumber);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isSuccess());
        assertEquals(orderDTO, response.getBody().getData());
        verify(orderService, times(1)).getOrderByOrderNumber(eq(orderNumber));
    }

    /**
     * Test retrieving order with null order number.
     * Verifies proper handling of null order number.
     */
    @Test
    void testGetOrderByOrderNumber_NullOrderNumber() {
        when(orderService.getOrderByOrderNumber(null))
            .thenThrow(new IllegalArgumentException("Order number cannot be null"));

        assertThrows(IllegalArgumentException.class, () -> {
            orderController.getOrderByOrderNumber(null);
        });
    }

    /**
     * Test retrieving order with invalid order number.
     * Verifies handling of non-existent order number.
     */
    @Test
    void testGetOrderByOrderNumber_NotFound() {
        when(orderService.getOrderByOrderNumber(any(String.class)))
            .thenThrow(new RuntimeException("Order not found"));

        assertThrows(RuntimeException.class, () -> {
            orderController.getOrderByOrderNumber(orderNumber);
        });
        verify(orderService, times(1)).getOrderByOrderNumber(eq(orderNumber));
    }

    /**
     * Test successfully retrieving user orders with pagination.
     * Verifies that paginated orders are returned correctly.
     */
    @Test
    void testGetUserOrders_Success() {
        List<OrderDTO> orderList = new ArrayList<>();
        orderList.add(orderDTO);
        Page<OrderDTO> orderPage = new PageImpl<>(orderList, PageRequest.of(0, 10), 1);

        when(authentication.getName()).thenReturn(userId.toString());
        when(orderService.getUserOrders(any(Long.class), any(Pageable.class)))
            .thenReturn(orderPage);

        ResponseEntity<ApiResponseDTO<Page<OrderDTO>>> response = 
            orderController.getUserOrders(0, 10, authentication);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isSuccess());
        assertNotNull(response.getBody().getData());
        assertEquals(1, response.getBody().getData().getTotalElements());
        verify(orderService, times(1)).getUserOrders(eq(userId), any(Pageable.class));
    }

    /**
     * Test retrieving user orders with default pagination.
     * Verifies default page and size parameters.
     */
    @Test
    void testGetUserOrders_DefaultPagination() {
        List<OrderDTO> orderList = new ArrayList<>();
        Page<OrderDTO> orderPage = new PageImpl<>(orderList, PageRequest.of(0, 10), 0);

        when(authentication.getName()).thenReturn(userId.toString());
        when(orderService.getUserOrders(any(Long.class), any(Pageable.class)))
            .thenReturn(orderPage);

        ResponseEntity<ApiResponseDTO<Page<OrderDTO>>> response = 
            orderController.getUserOrders(0, 10, authentication);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().getData().isEmpty());
        verify(orderService, times(1)).getUserOrders(eq(userId), any(Pageable.class));
    }

    /**
     * Test retrieving user orders with custom pagination.
     * Verifies custom page size handling.
     */
    @Test
    void testGetUserOrders_CustomPagination() {
        List<OrderDTO> orderList = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            orderList.add(orderDTO);
        }
        Page<OrderDTO> orderPage = new PageImpl<>(orderList, PageRequest.of(1, 5), 10);

        when(authentication.getName()).thenReturn(userId.toString());
        when(orderService.getUserOrders(any(Long.class), any(Pageable.class)))
            .thenReturn(orderPage);

        ResponseEntity<ApiResponseDTO<Page<OrderDTO>>> response = 
            orderController.getUserOrders(1, 5, authentication);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(5, response.getBody().getData().getContent().size());
        verify(orderService, times(1)).getUserOrders(eq(userId), any(Pageable.class));
    }

    /**
     * Test successfully updating order status.
     * Verifies that order status is updated correctly.
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
        assertTrue(response.getBody().isSuccess());
        assertEquals("Order status updated successfully", response.getBody().getMessage());
        assertEquals(newStatus, response.getBody().getData().getStatus());
        verify(orderService, times(1)).updateOrderStatus(eq(orderId), eq(newStatus));
    }

    /**
     * Test updating order status with null status.
     * Verifies proper handling of null status.
     */
    @Test
    void testUpdateOrderStatus_NullStatus() {
        when(orderService.updateOrderStatus(any(Long.class), eq(null)))
            .thenThrow(new IllegalArgumentException("Status cannot be null"));

        assertThrows(IllegalArgumentException.class, () -> {
            orderController.updateOrderStatus(orderId, null);
        });
    }

    /**
     * Test updating status of non-existent order.
     * Verifies handling of invalid order ID.
     */
    @Test
    void testUpdateOrderStatus_OrderNotFound() {
        when(orderService.updateOrderStatus(any(Long.class), any(Order.OrderStatus.class)))
            .thenThrow(new RuntimeException("Order not found"));

        assertThrows(RuntimeException.class, () -> {
            orderController.updateOrderStatus(orderId, Order.OrderStatus.SHIPPED);
        });
        verify(orderService, times(1)).updateOrderStatus(eq(orderId), any(Order.OrderStatus.class));
    }

    /**
     * Test updating order status with invalid transition.
     * Verifies validation of status transitions.
     */
    @Test
    void testUpdateOrderStatus_InvalidTransition() {
        when(orderService.updateOrderStatus(any(Long.class), any(Order.OrderStatus.class)))
            .thenThrow(new IllegalStateException("Invalid status transition"));

        assertThrows(IllegalStateException.class, () -> {
            orderController.updateOrderStatus(orderId, Order.OrderStatus.DELIVERED);
        });
        verify(orderService, times(1)).updateOrderStatus(eq(orderId), any(Order.OrderStatus.class));
    }

    /**
     * Test successfully cancelling an order.
     * Verifies that order is cancelled and status updated.
     */
    @Test
    void testCancelOrder_Success() {
        orderDTO.setStatus(Order.OrderStatus.CANCELLED);
        when(orderService.cancelOrder(any(Long.class))).thenReturn(orderDTO);

        ResponseEntity<ApiResponseDTO<OrderDTO>> response = orderController.cancelOrder(orderId);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isSuccess());
        assertEquals("Order cancelled successfully", response.getBody().getMessage());
        assertEquals(Order.OrderStatus.CANCELLED, response.getBody().getData().getStatus());
        verify(orderService, times(1)).cancelOrder(eq(orderId));
    }

    /**
     * Test cancelling non-existent order.
     * Verifies handling of invalid order ID.
     */
    @Test
    void testCancelOrder_OrderNotFound() {
        when(orderService.cancelOrder(any(Long.class)))
            .thenThrow(new RuntimeException("Order not found"));

        assertThrows(RuntimeException.class, () -> {
            orderController.cancelOrder(orderId);
        });
        verify(orderService, times(1)).cancelOrder(eq(orderId));
    }

    /**
     * Test cancelling already cancelled order.
     * Verifies handling of duplicate cancellation.
     */
    @Test
    void testCancelOrder_AlreadyCancelled() {
        when(orderService.cancelOrder(any(Long.class)))
            .thenThrow(new IllegalStateException("Order already cancelled"));

        assertThrows(IllegalStateException.class, () -> {
            orderController.cancelOrder(orderId);
        });
        verify(orderService, times(1)).cancelOrder(eq(orderId));
    }

    /**
     * Test cancelling delivered order.
     * Verifies that delivered orders cannot be cancelled.
     */
    @Test
    void testCancelOrder_DeliveredOrder() {
        when(orderService.cancelOrder(any(Long.class)))
            .thenThrow(new IllegalStateException("Cannot cancel delivered order"));

        assertThrows(IllegalStateException.class, () -> {
            orderController.cancelOrder(orderId);
        });
        verify(orderService, times(1)).cancelOrder(eq(orderId));
    }
}