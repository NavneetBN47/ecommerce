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
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Unit test class for OrderController
 * Tests order operations including create, retrieve, update status, and cancel
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
     * Test successfully creating an order
     */
    @Test
    void testCreateOrder_Success() {
        when(authentication.getName()).thenReturn(userId.toString());
        when(orderService.createOrder(anyLong(), any(CreateOrderRequestDTO.class)))
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
     */
    @Test
    void testCreateOrder_NullRequest() {
        when(authentication.getName()).thenReturn(userId.toString());
        when(orderService.createOrder(anyLong(), any()))
            .thenThrow(new IllegalArgumentException("Request cannot be null"));

        assertThrows(IllegalArgumentException.class, () -> {
            orderController.createOrder(null, authentication);
        });
    }

    /**
     * Test creating order with invalid user ID
     */
    @Test
    void testCreateOrder_InvalidUserId() {
        when(authentication.getName()).thenReturn("invalid");

        assertThrows(NumberFormatException.class, () -> {
            orderController.createOrder(createOrderRequestDTO, authentication);
        });
    }

    /**
     * Test successfully retrieving order by ID
     */
    @Test
    void testGetOrderById_Success() {
        when(orderService.getOrderById(anyLong())).thenReturn(orderDTO);

        ResponseEntity<ApiResponseDTO<OrderDTO>> response = orderController.getOrderById(orderId);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(orderDTO, response.getBody().getData());
        verify(orderService, times(1)).getOrderById(eq(orderId));
    }

    /**
     * Test retrieving order by non-existent ID
     */
    @Test
    void testGetOrderById_NotFound() {
        when(orderService.getOrderById(anyLong()))
            .thenThrow(new RuntimeException("Order not found"));

        assertThrows(RuntimeException.class, () -> {
            orderController.getOrderById(999L);
        });
    }

    /**
     * Test successfully retrieving order by order number
     */
    @Test
    void testGetOrderByOrderNumber_Success() {
        when(orderService.getOrderByOrderNumber(anyString())).thenReturn(orderDTO);

        ResponseEntity<ApiResponseDTO<OrderDTO>> response = 
            orderController.getOrderByOrderNumber(orderNumber);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(orderDTO, response.getBody().getData());
        verify(orderService, times(1)).getOrderByOrderNumber(eq(orderNumber));
    }

    /**
     * Test retrieving order by invalid order number
     */
    @Test
    void testGetOrderByOrderNumber_NotFound() {
        when(orderService.getOrderByOrderNumber(anyString()))
            .thenThrow(new RuntimeException("Order not found"));

        assertThrows(RuntimeException.class, () -> {
            orderController.getOrderByOrderNumber("INVALID-ORDER");
        });
    }

    /**
     * Test successfully retrieving user orders with pagination
     */
    @Test
    void testGetUserOrders_Success() {
        List<OrderDTO> orderList = Arrays.asList(orderDTO);
        Page<OrderDTO> orderPage = new PageImpl<>(orderList, PageRequest.of(0, 10), 1);
        
        when(authentication.getName()).thenReturn(userId.toString());
        when(orderService.getUserOrders(anyLong(), any(Pageable.class)))
            .thenReturn(orderPage);

        ResponseEntity<ApiResponseDTO<Page<OrderDTO>>> response = 
            orderController.getUserOrders(0, 10, authentication);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertNotNull(response.getBody().getData());
        assertEquals(1, response.getBody().getData().getTotalElements());
        verify(orderService, times(1)).getUserOrders(eq(userId), any(Pageable.class));
    }

    /**
     * Test retrieving user orders with custom pagination
     */
    @Test
    void testGetUserOrders_CustomPagination() {
        List<OrderDTO> orderList = Arrays.asList(orderDTO);
        Page<OrderDTO> orderPage = new PageImpl<>(orderList, PageRequest.of(1, 5), 10);
        
        when(authentication.getName()).thenReturn(userId.toString());
        when(orderService.getUserOrders(anyLong(), any(Pageable.class)))
            .thenReturn(orderPage);

        ResponseEntity<ApiResponseDTO<Page<OrderDTO>>> response = 
            orderController.getUserOrders(1, 5, authentication);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(orderService, times(1)).getUserOrders(eq(userId), any(Pageable.class));
    }

    /**
     * Test successfully updating order status
     */
    @Test
    void testUpdateOrderStatus_Success() {
        Order.OrderStatus newStatus = Order.OrderStatus.SHIPPED;
        orderDTO.setStatus(newStatus);
        
        when(orderService.updateOrderStatus(anyLong(), any(Order.OrderStatus.class)))
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
     * Test updating order status to all valid statuses
     */
    @Test
    void testUpdateOrderStatus_AllStatuses() {
        for (Order.OrderStatus status : Order.OrderStatus.values()) {
            orderDTO.setStatus(status);
            when(orderService.updateOrderStatus(anyLong(), any(Order.OrderStatus.class)))
                .thenReturn(orderDTO);

            ResponseEntity<ApiResponseDTO<OrderDTO>> response = 
                orderController.updateOrderStatus(orderId, status);

            assertNotNull(response);
            assertEquals(HttpStatus.OK, response.getStatusCode());
        }
    }

    /**
     * Test updating order status for non-existent order
     */
    @Test
    void testUpdateOrderStatus_OrderNotFound() {
        when(orderService.updateOrderStatus(anyLong(), any(Order.OrderStatus.class)))
            .thenThrow(new RuntimeException("Order not found"));

        assertThrows(RuntimeException.class, () -> {
            orderController.updateOrderStatus(999L, Order.OrderStatus.SHIPPED);
        });
    }

    /**
     * Test successfully cancelling an order
     */
    @Test
    void testCancelOrder_Success() {
        orderDTO.setStatus(Order.OrderStatus.CANCELLED);
        when(orderService.cancelOrder(anyLong())).thenReturn(orderDTO);

        ResponseEntity<ApiResponseDTO<OrderDTO>> response = orderController.cancelOrder(orderId);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Order cancelled successfully", response.getBody().getMessage());
        assertEquals(Order.OrderStatus.CANCELLED, response.getBody().getData().getStatus());
        verify(orderService, times(1)).cancelOrder(eq(orderId));
    }

    /**
     * Test cancelling non-existent order
     */
    @Test
    void testCancelOrder_OrderNotFound() {
        when(orderService.cancelOrder(anyLong()))
            .thenThrow(new RuntimeException("Order not found"));

        assertThrows(RuntimeException.class, () -> {
            orderController.cancelOrder(999L);
        });
    }

    /**
     * Test cancelling already cancelled order
     */
    @Test
    void testCancelOrder_AlreadyCancelled() {
        when(orderService.cancelOrder(anyLong()))
            .thenThrow(new IllegalStateException("Order already cancelled"));

        assertThrows(IllegalStateException.class, () -> {
            orderController.cancelOrder(orderId);
        });
    }
}