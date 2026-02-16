package com.ecommerce.controller;

import com.ecommerce.dto.ApiResponseDTO;
import com.ecommerce.dto.CreateOrderRequestDTO;
import com.ecommerce.dto.OrderDTO;
import com.ecommerce.entity.Order;
import com.ecommerce.service.OrderService;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Test class for OrderController
 * Tests order management operations including create, retrieve, update status, and cancel
 */
@ExtendWith(MockitoExtension.class)
class test_OrderController {

    @Mock
    private OrderService orderService;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private OrderController orderController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(orderController).build();
        objectMapper = new ObjectMapper();
    }

    /**
     * Test creating order successfully
     * Verifies that a valid order creation request returns HTTP 201 with order details
     */
    @Test
    void testCreateOrder_Success() throws Exception {
        when(authentication.getName()).thenReturn("1");

        CreateOrderRequestDTO request = new CreateOrderRequestDTO();
        request.setShippingAddress("123 Main St");
        request.setPaymentMethod("CREDIT_CARD");

        OrderDTO orderDTO = new OrderDTO();
        orderDTO.setId(1L);
        orderDTO.setOrderNumber("ORD-001");
        orderDTO.setTotalAmount(BigDecimal.valueOf(100.00));
        orderDTO.setStatus(Order.OrderStatus.PENDING);

        when(orderService.createOrder(anyLong(), any(CreateOrderRequestDTO.class))).thenReturn(orderDTO);

        mockMvc.perform(post("/api/orders")
                .principal(authentication)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Order created successfully"))
                .andExpect(jsonPath("$.data.orderNumber").value("ORD-001"));

        verify(orderService, times(1)).createOrder(anyLong(), any(CreateOrderRequestDTO.class));
    }

    /**
     * Test creating order with invalid data
     * Verifies that invalid order data is rejected
     */
    @Test
    void testCreateOrder_InvalidData() throws Exception {
        when(authentication.getName()).thenReturn("1");

        CreateOrderRequestDTO request = new CreateOrderRequestDTO();
        request.setShippingAddress("");
        request.setPaymentMethod("");

        mockMvc.perform(post("/api/orders")
                .principal(authentication)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(orderService, never()).createOrder(anyLong(), any(CreateOrderRequestDTO.class));
    }

    /**
     * Test getting order by ID successfully
     * Verifies that a valid order ID returns HTTP 200 with order details
     */
    @Test
    void testGetOrderById_Success() throws Exception {
        OrderDTO orderDTO = new OrderDTO();
        orderDTO.setId(1L);
        orderDTO.setOrderNumber("ORD-001");
        orderDTO.setTotalAmount(BigDecimal.valueOf(100.00));

        when(orderService.getOrderById(anyLong())).thenReturn(orderDTO);

        mockMvc.perform(get("/api/orders/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.orderNumber").value("ORD-001"));

        verify(orderService, times(1)).getOrderById(1L);
    }

    /**
     * Test getting order by non-existent ID
     * Verifies that non-existent order ID throws exception
     */
    @Test
    void testGetOrderById_NotFound() throws Exception {
        when(orderService.getOrderById(anyLong()))
                .thenThrow(new RuntimeException("Order not found"));

        mockMvc.perform(get("/api/orders/999"))
                .andExpect(status().isInternalServerError());

        verify(orderService, times(1)).getOrderById(999L);
    }

    /**
     * Test getting order by order number successfully
     * Verifies that a valid order number returns HTTP 200 with order details
     */
    @Test
    void testGetOrderByOrderNumber_Success() throws Exception {
        OrderDTO orderDTO = new OrderDTO();
        orderDTO.setId(1L);
        orderDTO.setOrderNumber("ORD-001");
        orderDTO.setTotalAmount(BigDecimal.valueOf(100.00));

        when(orderService.getOrderByOrderNumber(anyString())).thenReturn(orderDTO);

        mockMvc.perform(get("/api/orders/number/ORD-001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.orderNumber").value("ORD-001"));

        verify(orderService, times(1)).getOrderByOrderNumber("ORD-001");
    }

    /**
     * Test getting user orders successfully
     * Verifies that authenticated user can retrieve their orders with pagination
     */
    @Test
    void testGetUserOrders_Success() throws Exception {
        when(authentication.getName()).thenReturn("1");

        OrderDTO orderDTO = new OrderDTO();
        orderDTO.setId(1L);
        orderDTO.setOrderNumber("ORD-001");

        Page<OrderDTO> orderPage = new PageImpl<>(Collections.singletonList(orderDTO));

        when(orderService.getUserOrders(anyLong(), any(Pageable.class))).thenReturn(orderPage);

        mockMvc.perform(get("/api/orders")
                .principal(authentication)
                .param("page", "0")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content[0].orderNumber").value("ORD-001"));

        verify(orderService, times(1)).getUserOrders(eq(1L), any(Pageable.class));
    }

    /**
     * Test getting user orders with custom pagination
     * Verifies that custom page and size parameters are applied correctly
     */
    @Test
    void testGetUserOrders_CustomPagination() throws Exception {
        when(authentication.getName()).thenReturn("1");

        Page<OrderDTO> orderPage = new PageImpl<>(Collections.emptyList());

        when(orderService.getUserOrders(anyLong(), any(Pageable.class))).thenReturn(orderPage);

        mockMvc.perform(get("/api/orders")
                .principal(authentication)
                .param("page", "2")
                .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        verify(orderService, times(1)).getUserOrders(eq(1L), eq(PageRequest.of(2, 20)));
    }

    /**
     * Test updating order status successfully
     * Verifies that admin can update order status
     */
    @Test
    void testUpdateOrderStatus_Success() throws Exception {
        OrderDTO orderDTO = new OrderDTO();
        orderDTO.setId(1L);
        orderDTO.setOrderNumber("ORD-001");
        orderDTO.setStatus(Order.OrderStatus.SHIPPED);

        when(orderService.updateOrderStatus(anyLong(), any(Order.OrderStatus.class))).thenReturn(orderDTO);

        mockMvc.perform(patch("/api/orders/1/status")
                .param("status", "SHIPPED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Order status updated successfully"))
                .andExpect(jsonPath("$.data.status").value("SHIPPED"));

        verify(orderService, times(1)).updateOrderStatus(1L, Order.OrderStatus.SHIPPED);
    }

    /**
     * Test updating order status with invalid status
     * Verifies that invalid status value is rejected
     */
    @Test
    void testUpdateOrderStatus_InvalidStatus() throws Exception {
        mockMvc.perform(patch("/api/orders/1/status")
                .param("status", "INVALID_STATUS"))
                .andExpect(status().isBadRequest());

        verify(orderService, never()).updateOrderStatus(anyLong(), any(Order.OrderStatus.class));
    }

    /**
     * Test cancelling order successfully
     * Verifies that order can be cancelled
     */
    @Test
    void testCancelOrder_Success() throws Exception {
        OrderDTO orderDTO = new OrderDTO();
        orderDTO.setId(1L);
        orderDTO.setOrderNumber("ORD-001");
        orderDTO.setStatus(Order.OrderStatus.CANCELLED);

        when(orderService.cancelOrder(anyLong())).thenReturn(orderDTO);

        mockMvc.perform(post("/api/orders/1/cancel"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Order cancelled successfully"))
                .andExpect(jsonPath("$.data.status").value("CANCELLED"));

        verify(orderService, times(1)).cancelOrder(1L);
    }

    /**
     * Test cancelling non-existent order
     * Verifies that cancelling non-existent order throws exception
     */
    @Test
    void testCancelOrder_NotFound() throws Exception {
        when(orderService.cancelOrder(anyLong()))
                .thenThrow(new RuntimeException("Order not found"));

        mockMvc.perform(post("/api/orders/999/cancel"))
                .andExpect(status().isInternalServerError());

        verify(orderService, times(1)).cancelOrder(999L);
    }

    /**
     * Test cancelling already cancelled order
     * Verifies proper handling of already cancelled order
     */
    @Test
    void testCancelOrder_AlreadyCancelled() throws Exception {
        when(orderService.cancelOrder(anyLong()))
                .thenThrow(new RuntimeException("Order already cancelled"));

        mockMvc.perform(post("/api/orders/1/cancel"))
                .andExpect(status().isInternalServerError());

        verify(orderService, times(1)).cancelOrder(1L);
    }
}