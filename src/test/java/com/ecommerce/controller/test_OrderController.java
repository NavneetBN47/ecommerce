package com.ecommerce.controller;

import com.ecommerce.dto.ApiResponse;
import com.ecommerce.dto.OrderDTO;
import com.ecommerce.entity.Order;
import com.ecommerce.service.OrderService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Test class for OrderController
 * Tests all public endpoints for order management operations
 * Mocks OrderService layer to isolate controller logic
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("OrderController Tests")
class test_OrderController {

    @Mock
    private OrderService orderService;

    @InjectMocks
    private OrderController orderController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(orderController).build();
        objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules(); // For LocalDateTime serialization
    }

    /**
     * Test creating order from cart successfully
     * Verifies that orders are created with proper validation
     */
    @Test
    @DisplayName("Should successfully create order from cart")
    void testCreateOrder_Success() throws Exception {
        // Given
        Long userId = 1L;
        OrderDTO orderDTO = new OrderDTO();
        orderDTO.setShippingAddress("123 Main St, City, State");
        orderDTO.setPaymentMethod("CREDIT_CARD");
        
        OrderDTO createdOrder = new OrderDTO();
        createdOrder.setId(1L);
        createdOrder.setOrderNumber("ORD-001");
        createdOrder.setUserId(userId);
        createdOrder.setTotalAmount(BigDecimal.valueOf(199.99));
        createdOrder.setStatus(Order.OrderStatus.PENDING);
        createdOrder.setOrderDate(LocalDateTime.now());
        
        when(orderService.createOrderFromCart(eq(userId), any(OrderDTO.class))).thenReturn(createdOrder);

        // When & Then
        mockMvc.perform(post("/api/orders/user/{userId}", userId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(orderDTO)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Order created successfully"))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.orderNumber").value("ORD-001"))
                .andExpect(jsonPath("$.data.totalAmount").value(199.99));

        verify(orderService).createOrderFromCart(eq(userId), any(OrderDTO.class));
    }

    /**
     * Test creating order with invalid data
     * Verifies that validation errors are handled properly
     */
    @Test
    @DisplayName("Should return validation error for invalid order data")
    void testCreateOrder_InvalidData() throws Exception {
        // Given
        Long userId = 1L;
        OrderDTO invalidOrder = new OrderDTO();
        // Missing required fields

        // When & Then
        mockMvc.perform(post("/api/orders/user/{userId}", userId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidOrder)))
                .andExpect(status().isBadRequest());
    }

    /**
     * Test getting order by ID successfully
     * Verifies that orders can be retrieved by ID
     */
    @Test
    @DisplayName("Should successfully get order by ID")
    void testGetOrderById_Success() throws Exception {
        // Given
        Long orderId = 1L;
        OrderDTO orderDTO = new OrderDTO();
        orderDTO.setId(orderId);
        orderDTO.setOrderNumber("ORD-001");
        orderDTO.setUserId(1L);
        orderDTO.setTotalAmount(BigDecimal.valueOf(199.99));
        orderDTO.setStatus(Order.OrderStatus.PENDING);
        
        when(orderService.getOrderById(orderId)).thenReturn(orderDTO);

        // When & Then
        mockMvc.perform(get("/api/orders/{orderId}", orderId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.orderNumber").value("ORD-001"))
                .andExpect(jsonPath("$.data.totalAmount").value(199.99));

        verify(orderService).getOrderById(eq(orderId));
    }

    /**
     * Test getting order by order number successfully
     * Verifies that orders can be retrieved by order number
     */
    @Test
    @DisplayName("Should successfully get order by order number")
    void testGetOrderByOrderNumber_Success() throws Exception {
        // Given
        String orderNumber = "ORD-001";
        OrderDTO orderDTO = new OrderDTO();
        orderDTO.setId(1L);
        orderDTO.setOrderNumber(orderNumber);
        orderDTO.setUserId(1L);
        orderDTO.setTotalAmount(BigDecimal.valueOf(199.99));
        
        when(orderService.getOrderByOrderNumber(orderNumber)).thenReturn(orderDTO);

        // When & Then
        mockMvc.perform(get("/api/orders/number/{orderNumber}", orderNumber))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.orderNumber").value(orderNumber))
                .andExpect(jsonPath("$.data.totalAmount").value(199.99));

        verify(orderService).getOrderByOrderNumber(eq(orderNumber));
    }

    /**
     * Test getting user orders successfully
     * Verifies that all orders for a user can be retrieved
     */
    @Test
    @DisplayName("Should successfully get all orders for user")
    void testGetUserOrders_Success() throws Exception {
        // Given
        Long userId = 1L;
        OrderDTO order1 = new OrderDTO();
        order1.setId(1L);
        order1.setOrderNumber("ORD-001");
        order1.setUserId(userId);
        
        OrderDTO order2 = new OrderDTO();
        order2.setId(2L);
        order2.setOrderNumber("ORD-002");
        order2.setUserId(userId);
        
        List<OrderDTO> orders = Arrays.asList(order1, order2);
        
        when(orderService.getUserOrders(userId)).thenReturn(orders);

        // When & Then
        mockMvc.perform(get("/api/orders/user/{userId}", userId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(2))
                .andExpect(jsonPath("$.data[0].orderNumber").value("ORD-001"))
                .andExpect(jsonPath("$.data[1].orderNumber").value("ORD-002"));

        verify(orderService).getUserOrders(eq(userId));
    }

    /**
     * Test updating order status successfully
     * Verifies that order status can be updated
     */
    @Test
    @DisplayName("Should successfully update order status")
    void testUpdateOrderStatus_Success() throws Exception {
        // Given
        Long orderId = 1L;
        Order.OrderStatus newStatus = Order.OrderStatus.SHIPPED;
        
        OrderDTO updatedOrder = new OrderDTO();
        updatedOrder.setId(orderId);
        updatedOrder.setStatus(newStatus);
        updatedOrder.setOrderNumber("ORD-001");
        
        when(orderService.updateOrderStatus(orderId, newStatus)).thenReturn(updatedOrder);

        // When & Then
        mockMvc.perform(put("/api/orders/{orderId}/status", orderId)
                .param("status", newStatus.toString()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Order status updated"))
                .andExpect(jsonPath("$.data.status").value(newStatus.toString()));

        verify(orderService).updateOrderStatus(eq(orderId), eq(newStatus));
    }

    /**
     * Test cancelling order successfully
     * Verifies that orders can be cancelled
     */
    @Test
    @DisplayName("Should successfully cancel order")
    void testCancelOrder_Success() throws Exception {
        // Given
        Long orderId = 1L;
        
        OrderDTO cancelledOrder = new OrderDTO();
        cancelledOrder.setId(orderId);
        cancelledOrder.setStatus(Order.OrderStatus.CANCELLED);
        cancelledOrder.setOrderNumber("ORD-001");
        
        when(orderService.cancelOrder(orderId)).thenReturn(cancelledOrder);

        // When & Then
        mockMvc.perform(post("/api/orders/{orderId}/cancel", orderId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Order cancelled"))
                .andExpect(jsonPath("$.data.status").value("CANCELLED"));

        verify(orderService).cancelOrder(eq(orderId));
    }

    /**
     * Test getting non-existent order
     * Verifies that proper error handling occurs for invalid order IDs
     */
    @Test
    @DisplayName("Should handle getting non-existent order")
    void testGetOrderById_NotFound() throws Exception {
        // Given
        Long nonExistentOrderId = 999L;
        
        when(orderService.getOrderById(nonExistentOrderId))
                .thenThrow(new RuntimeException("Order not found"));

        // When & Then
        mockMvc.perform(get("/api/orders/{orderId}", nonExistentOrderId))
                .andExpect(status().isInternalServerError());

        verify(orderService).getOrderById(eq(nonExistentOrderId));
    }
}