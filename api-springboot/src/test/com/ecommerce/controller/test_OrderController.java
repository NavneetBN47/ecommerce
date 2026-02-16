package com.ecommerce.controller;

import com.ecommerce.dto.ApiResponseDTO;
import com.ecommerce.dto.CreateOrderRequestDTO;
import com.ecommerce.dto.OrderDTO;
import com.ecommerce.entity.Order;
import com.ecommerce.service.OrderService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * JUnit 5 test class for OrderController
 * Tests order management operations including creation, retrieval, status updates, and cancellation
 */
@WebMvcTest(OrderController.class)
@DisplayName("OrderController Tests")
class test_OrderController {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private OrderService orderService;

    @Autowired
    private ObjectMapper objectMapper;

    private CreateOrderRequestDTO createOrderRequest;
    private OrderDTO orderDTO;
    private Authentication mockAuth;

    /**
     * Set up test data before each test
     */
    @BeforeEach
    void setUp() {
        createOrderRequest = new CreateOrderRequestDTO();
        createOrderRequest.setShippingAddress("123 Test Street");
        createOrderRequest.setPaymentMethod("CREDIT_CARD");

        orderDTO = new OrderDTO();
        orderDTO.setId(1L);
        orderDTO.setOrderNumber("ORD-12345");
        orderDTO.setUserId(1L);
        orderDTO.setStatus(Order.OrderStatus.PENDING);
        orderDTO.setTotalAmount(BigDecimal.valueOf(150.00));
        orderDTO.setShippingAddress("123 Test Street");
        orderDTO.setCreatedAt(LocalDateTime.now());

        mockAuth = mock(Authentication.class);
        when(mockAuth.getName()).thenReturn("1");
    }

    /**
     * Test successfully creating an order
     * Verifies that valid order request returns 201 CREATED with order data
     */
    @Test
    @WithMockUser(username = "1")
    @DisplayName("Should create order successfully")
    void testCreateOrder_Success() throws Exception {
        when(orderService.createOrder(anyLong(), any(CreateOrderRequestDTO.class)))
                .thenReturn(orderDTO);

        mockMvc.perform(post("/api/orders")
                .with(authentication(mockAuth))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createOrderRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Order created successfully"))
                .andExpect(jsonPath("$.data.orderNumber").value("ORD-12345"))
                .andExpect(jsonPath("$.data.totalAmount").value(150.00));

        verify(orderService, times(1)).createOrder(anyLong(), any(CreateOrderRequestDTO.class));
    }

    /**
     * Test creating order with invalid data
     * Verifies that validation errors are properly handled
     */
    @Test
    @WithMockUser(username = "1")
    @DisplayName("Should return 400 when order data is invalid")
    void testCreateOrder_InvalidData() throws Exception {
        CreateOrderRequestDTO invalidRequest = new CreateOrderRequestDTO();
        invalidRequest.setShippingAddress("");

        mockMvc.perform(post("/api/orders")
                .with(authentication(mockAuth))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());

        verify(orderService, never()).createOrder(anyLong(), any(CreateOrderRequestDTO.class));
    }

    /**
     * Test creating order without authentication
     * Verifies that unauthenticated requests are rejected
     */
    @Test
    @DisplayName("Should return 401 when creating order without authentication")
    void testCreateOrder_Unauthenticated() throws Exception {
        mockMvc.perform(post("/api/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createOrderRequest)))
                .andExpect(status().isUnauthorized());

        verify(orderService, never()).createOrder(anyLong(), any(CreateOrderRequestDTO.class));
    }

    /**
     * Test successfully retrieving order by ID
     * Verifies that order retrieval returns order data
     */
    @Test
    @DisplayName("Should retrieve order by ID successfully")
    void testGetOrderById_Success() throws Exception {
        when(orderService.getOrderById(anyLong())).thenReturn(orderDTO);

        mockMvc.perform(get("/api/orders/{orderId}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.orderNumber").value("ORD-12345"));

        verify(orderService, times(1)).getOrderById(1L);
    }

    /**
     * Test retrieving non-existent order
     * Verifies that service exception is properly handled
     */
    @Test
    @DisplayName("Should return error when order not found")
    void testGetOrderById_NotFound() throws Exception {
        when(orderService.getOrderById(anyLong()))
                .thenThrow(new RuntimeException("Order not found"));

        mockMvc.perform(get("/api/orders/{orderId}", 999L))
                .andExpect(status().is5xxServerError());

        verify(orderService, times(1)).getOrderById(999L);
    }

    /**
     * Test successfully retrieving order by order number
     * Verifies that order retrieval by order number works correctly
     */
    @Test
    @DisplayName("Should retrieve order by order number successfully")
    void testGetOrderByOrderNumber_Success() throws Exception {
        when(orderService.getOrderByOrderNumber(anyString())).thenReturn(orderDTO);

        mockMvc.perform(get("/api/orders/number/{orderNumber}", "ORD-12345"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.orderNumber").value("ORD-12345"));

        verify(orderService, times(1)).getOrderByOrderNumber("ORD-12345");
    }

    /**
     * Test retrieving order with invalid order number
     * Verifies that invalid order number is handled
     */
    @Test
    @DisplayName("Should return error when order number is invalid")
    void testGetOrderByOrderNumber_Invalid() throws Exception {
        when(orderService.getOrderByOrderNumber(anyString()))
                .thenThrow(new RuntimeException("Invalid order number"));

        mockMvc.perform(get("/api/orders/number/{orderNumber}", "INVALID"))
                .andExpect(status().is5xxServerError());

        verify(orderService, times(1)).getOrderByOrderNumber("INVALID");
    }

    /**
     * Test successfully retrieving user orders
     * Verifies that paginated user orders are returned
     */
    @Test
    @WithMockUser(username = "1")
    @DisplayName("Should retrieve user orders successfully")
    void testGetUserOrders_Success() throws Exception {
        List<OrderDTO> orders = Arrays.asList(orderDTO);
        Page<OrderDTO> orderPage = new PageImpl<>(orders, PageRequest.of(0, 10), 1);
        when(orderService.getUserOrders(anyLong(), any())).thenReturn(orderPage);

        mockMvc.perform(get("/api/orders")
                .with(authentication(mockAuth))
                .param("page", "0")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content[0].orderNumber").value("ORD-12345"));

        verify(orderService, times(1)).getUserOrders(anyLong(), any());
    }

    /**
     * Test retrieving user orders with custom pagination
     * Verifies that custom page size is respected
     */
    @Test
    @WithMockUser(username = "1")
    @DisplayName("Should retrieve user orders with custom pagination")
    void testGetUserOrders_CustomPagination() throws Exception {
        List<OrderDTO> orders = new ArrayList<>();
        Page<OrderDTO> orderPage = new PageImpl<>(orders, PageRequest.of(1, 5), 0);
        when(orderService.getUserOrders(anyLong(), any())).thenReturn(orderPage);

        mockMvc.perform(get("/api/orders")
                .with(authentication(mockAuth))
                .param("page", "1")
                .param("size", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        verify(orderService, times(1)).getUserOrders(eq(1L), any());
    }

    /**
     * Test successfully updating order status
     * Verifies that order status update works correctly
     */
    @Test
    @DisplayName("Should update order status successfully")
    void testUpdateOrderStatus_Success() throws Exception {
        OrderDTO updatedOrder = new OrderDTO();
        updatedOrder.setId(1L);
        updatedOrder.setStatus(Order.OrderStatus.SHIPPED);
        when(orderService.updateOrderStatus(anyLong(), any(Order.OrderStatus.class)))
                .thenReturn(updatedOrder);

        mockMvc.perform(patch("/api/orders/{orderId}/status", 1L)
                .param("status", "SHIPPED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Order status updated successfully"));

        verify(orderService, times(1)).updateOrderStatus(1L, Order.OrderStatus.SHIPPED);
    }

    /**
     * Test updating order status with invalid status
     * Verifies that invalid status is handled
     */
    @Test
    @DisplayName("Should return error when status is invalid")
    void testUpdateOrderStatus_InvalidStatus() throws Exception {
        mockMvc.perform(patch("/api/orders/{orderId}/status", 1L)
                .param("status", "INVALID_STATUS"))
                .andExpect(status().isBadRequest());

        verify(orderService, never()).updateOrderStatus(anyLong(), any(Order.OrderStatus.class));
    }

    /**
     * Test successfully cancelling order
     * Verifies that order cancellation works correctly
     */
    @Test
    @DisplayName("Should cancel order successfully")
    void testCancelOrder_Success() throws Exception {
        OrderDTO cancelledOrder = new OrderDTO();
        cancelledOrder.setId(1L);
        cancelledOrder.setStatus(Order.OrderStatus.CANCELLED);
        when(orderService.cancelOrder(anyLong())).thenReturn(cancelledOrder);

        mockMvc.perform(post("/api/orders/{orderId}/cancel", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Order cancelled successfully"));

        verify(orderService, times(1)).cancelOrder(1L);
    }

    /**
     * Test cancelling non-existent order
     * Verifies that service exception is properly handled
     */
    @Test
    @DisplayName("Should return error when cancelling non-existent order")
    void testCancelOrder_NotFound() throws Exception {
        when(orderService.cancelOrder(anyLong()))
                .thenThrow(new RuntimeException("Order not found"));

        mockMvc.perform(post("/api/orders/{orderId}/cancel", 999L))
                .andExpect(status().is5xxServerError());

        verify(orderService, times(1)).cancelOrder(999L);
    }

    /**
     * Test cancelling already cancelled order
     * Verifies that business rule violation is handled
     */
    @Test
    @DisplayName("Should return error when cancelling already cancelled order")
    void testCancelOrder_AlreadyCancelled() throws Exception {
        when(orderService.cancelOrder(anyLong()))
                .thenThrow(new RuntimeException("Order already cancelled"));

        mockMvc.perform(post("/api/orders/{orderId}/cancel", 1L))
                .andExpect(status().is5xxServerError());

        verify(orderService, times(1)).cancelOrder(1L);
    }
}