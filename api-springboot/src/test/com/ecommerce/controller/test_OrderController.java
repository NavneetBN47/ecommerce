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
        orderDTO.setTotalAmount(BigDecimal.valueOf(199.99));
        orderDTO.setStatus(Order.OrderStatus.PENDING);
    }

    /**
     * Test creating order successfully.
     * Verifies that order is created from cart and returns HTTP 201 CREATED status.
     */
    @Test
    void testCreateOrder_Success() {
        when(authentication.getName()).thenReturn(userId.toString());
        when(orderService.createOrder(eq(userId), any(CreateOrderRequestDTO.class))).thenReturn(orderDTO);

        ResponseEntity<ApiResponseDTO<OrderDTO>> response = orderController.createOrder(createOrderRequest, authentication);

        assertNotNull(response);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Order created successfully", response.getBody().getMessage());
        assertEquals(orderDTO, response.getBody().getData());
        assertEquals(orderNumber, response.getBody().getData().getOrderNumber());
        verify(orderService, times(1)).createOrder(eq(userId), any(CreateOrderRequestDTO.class));
    }

    /**
     * Test creating order with null request.
     * Verifies proper exception handling.
     */
    @Test
    void testCreateOrder_NullRequest() {
        when(authentication.getName()).thenReturn(userId.toString());

        assertThrows(Exception.class, () -> {
            orderController.createOrder(null, authentication);
        });
    }

    /**
     * Test creating order with empty cart.
     * Verifies validation logic for empty cart scenario.
     */
    @Test
    void testCreateOrder_EmptyCart() {
        when(authentication.getName()).thenReturn(userId.toString());
        when(orderService.createOrder(eq(userId), any(CreateOrderRequestDTO.class)))
            .thenThrow(new IllegalStateException("Cart is empty"));

        assertThrows(IllegalStateException.class, () -> {
            orderController.createOrder(createOrderRequest, authentication);
        });
    }

    /**
     * Test getting order by ID successfully.
     * Verifies that order details are retrieved with HTTP 200 OK status.
     */
    @Test
    void testGetOrderById_Success() {
        when(orderService.getOrderById(eq(orderId))).thenReturn(orderDTO);

        ResponseEntity<ApiResponseDTO<OrderDTO>> response = orderController.getOrderById(orderId);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(orderDTO, response.getBody().getData());
        assertEquals(orderId, response.getBody().getData().getId());
        verify(orderService, times(1)).getOrderById(eq(orderId));
    }

    /**
     * Test getting order by non-existent ID.
     * Verifies proper error handling for not found scenario.
     */
    @Test
    void testGetOrderById_NotFound() {
        Long nonExistentId = 999L;
        when(orderService.getOrderById(eq(nonExistentId)))
            .thenThrow(new RuntimeException("Order not found"));

        assertThrows(RuntimeException.class, () -> {
            orderController.getOrderById(nonExistentId);
        });
    }

    /**
     * Test getting order by order number successfully.
     * Verifies that order can be retrieved using order number.
     */
    @Test
    void testGetOrderByOrderNumber_Success() {
        when(orderService.getOrderByOrderNumber(eq(orderNumber))).thenReturn(orderDTO);

        ResponseEntity<ApiResponseDTO<OrderDTO>> response = orderController.getOrderByOrderNumber(orderNumber);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(orderDTO, response.getBody().getData());
        assertEquals(orderNumber, response.getBody().getData().getOrderNumber());
        verify(orderService, times(1)).getOrderByOrderNumber(eq(orderNumber));
    }

    /**
     * Test getting order by invalid order number.
     * Verifies proper error handling.
     */
    @Test
    void testGetOrderByOrderNumber_Invalid() {
        String invalidOrderNumber = "INVALID-ORDER";
        when(orderService.getOrderByOrderNumber(eq(invalidOrderNumber)))
            .thenThrow(new RuntimeException("Order not found"));

        assertThrows(RuntimeException.class, () -> {
            orderController.getOrderByOrderNumber(invalidOrderNumber);
        });
    }

    /**
     * Test getting user orders with pagination.
     * Verifies that paginated order list is retrieved successfully.
     */
    @Test
    void testGetUserOrders_Success() {
        when(authentication.getName()).thenReturn(userId.toString());
        List<OrderDTO> orderList = Arrays.asList(orderDTO);
        Page<OrderDTO> orderPage = new PageImpl<>(orderList, PageRequest.of(0, 10), 1);
        when(orderService.getUserOrders(eq(userId), any(Pageable.class))).thenReturn(orderPage);

        ResponseEntity<ApiResponseDTO<Page<OrderDTO>>> response = orderController.getUserOrders(0, 10, authentication);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertNotNull(response.getBody().getData());
        assertEquals(1, response.getBody().getData().getTotalElements());
        assertEquals(orderDTO, response.getBody().getData().getContent().get(0));
        verify(orderService, times(1)).getUserOrders(eq(userId), any(Pageable.class));
    }

    /**
     * Test getting user orders with custom pagination.
     * Verifies that custom page size and number are respected.
     */
    @Test
    void testGetUserOrders_CustomPagination() {
        when(authentication.getName()).thenReturn(userId.toString());
        List<OrderDTO> orderList = Arrays.asList(orderDTO, orderDTO, orderDTO);
        Page<OrderDTO> orderPage = new PageImpl<>(orderList, PageRequest.of(1, 5), 15);
        when(orderService.getUserOrders(eq(userId), any(Pageable.class))).thenReturn(orderPage);

        ResponseEntity<ApiResponseDTO<Page<OrderDTO>>> response = orderController.getUserOrders(1, 5, authentication);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(3, response.getBody().getData().getContent().size());
        verify(orderService, times(1)).getUserOrders(eq(userId), any(Pageable.class));
    }

    /**
     * Test updating order status successfully.
     * Verifies that order status can be updated (admin operation).
     */
    @Test
    void testUpdateOrderStatus_Success() {
        Order.OrderStatus newStatus = Order.OrderStatus.SHIPPED;
        OrderDTO updatedOrder = new OrderDTO();
        updatedOrder.setId(orderId);
        updatedOrder.setStatus(newStatus);
        
        when(orderService.updateOrderStatus(eq(orderId), eq(newStatus))).thenReturn(updatedOrder);

        ResponseEntity<ApiResponseDTO<OrderDTO>> response = orderController.updateOrderStatus(orderId, newStatus);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Order status updated successfully", response.getBody().getMessage());
        assertEquals(newStatus, response.getBody().getData().getStatus());
        verify(orderService, times(1)).updateOrderStatus(eq(orderId), eq(newStatus));
    }

    /**
     * Test updating order status to invalid state.
     * Verifies validation of status transitions.
     */
    @Test
    void testUpdateOrderStatus_InvalidTransition() {
        Order.OrderStatus invalidStatus = Order.OrderStatus.CANCELLED;
        when(orderService.updateOrderStatus(eq(orderId), eq(invalidStatus)))
            .thenThrow(new IllegalStateException("Invalid status transition"));

        assertThrows(IllegalStateException.class, () -> {
            orderController.updateOrderStatus(orderId, invalidStatus);
        });
    }

    /**
     * Test cancelling order successfully.
     * Verifies that order can be cancelled and status is updated.
     */
    @Test
    void testCancelOrder_Success() {
        OrderDTO cancelledOrder = new OrderDTO();
        cancelledOrder.setId(orderId);
        cancelledOrder.setStatus(Order.OrderStatus.CANCELLED);
        
        when(orderService.cancelOrder(eq(orderId))).thenReturn(cancelledOrder);

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
     * Verifies that shipped orders cannot be cancelled.
     */
    @Test
    void testCancelOrder_AlreadyShipped() {
        when(orderService.cancelOrder(eq(orderId)))
            .thenThrow(new IllegalStateException("Cannot cancel shipped order"));

        assertThrows(IllegalStateException.class, () -> {
            orderController.cancelOrder(orderId);
        });
        verify(orderService, times(1)).cancelOrder(eq(orderId));
    }

    /**
     * Test cancelling non-existent order.
     * Verifies proper error handling.
     */
    @Test
    void testCancelOrder_NotFound() {
        Long nonExistentId = 999L;
        when(orderService.cancelOrder(eq(nonExistentId)))
            .thenThrow(new RuntimeException("Order not found"));

        assertThrows(RuntimeException.class, () -> {
            orderController.cancelOrder(nonExistentId);
        });
    }
}