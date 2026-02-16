package com.ecommerce.service;

import com.ecommerce.dto.CreateOrderRequestDTO;
import com.ecommerce.dto.OrderDTO;
import com.ecommerce.entity.Order;
import com.ecommerce.entity.OrderItem;
import com.ecommerce.entity.Product;
import com.ecommerce.entity.User;
import com.ecommerce.repository.OrderRepository;
import com.ecommerce.repository.UserRepository;
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

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Test class for OrderService
 * 
 * Tests order operations including create, retrieve, update status, and cancel
 * 
 * @author QA Automation Agent
 * @version 1.0.0
 */
@ExtendWith(MockitoExtension.class)
class test_OrderService {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private CartService cartService;

    @Mock
    private ProductService productService;

    @InjectMocks
    private OrderService orderService;

    private User user;
    private Order order;
    private CreateOrderRequestDTO createOrderRequest;
    private Long userId;
    private Long orderId;
    private String orderNumber;

    @BeforeEach
    void setUp() {
        userId = 1L;
        orderId = 100L;
        orderNumber = "ORD-20240101-1234";

        user = User.builder()
            .id(userId)
            .username("testuser")
            .email("test@example.com")
            .build();

        createOrderRequest = new CreateOrderRequestDTO();
        createOrderRequest.setShippingAddress("123 Main St");
        createOrderRequest.setBillingAddress("123 Main St");
        createOrderRequest.setPaymentMethod("CREDIT_CARD");

        order = Order.builder()
            .id(orderId)
            .orderNumber(orderNumber)
            .user(user)
            .totalAmount(BigDecimal.valueOf(150.00))
            .status(Order.OrderStatus.PENDING)
            .shippingAddress("123 Main St")
            .billingAddress("123 Main St")
            .paymentMethod("CREDIT_CARD")
            .paymentStatus("PENDING")
            .items(new ArrayList<>())
            .build();
    }

    /**
     * Test getting order by ID successfully
     * 
     * Verifies:
     * - Order is retrieved by ID
     * - OrderDTO is returned with correct data
     */
    @Test
    void testGetOrderById_Success() {
        // Given
        when(orderRepository.findByIdWithItems(anyLong())).thenReturn(Optional.of(order));

        // When
        OrderDTO result = orderService.getOrderById(orderId);

        // Then
        assertNotNull(result, "Result should not be null");
        assertEquals(orderId, result.getId());
        assertEquals(orderNumber, result.getOrderNumber());
        verify(orderRepository, times(1)).findByIdWithItems(orderId);
    }

    /**
     * Test getting order by non-existent ID
     * 
     * Verifies:
     * - ResourceNotFoundException is thrown for non-existent order
     */
    @Test
    void testGetOrderById_NotFound() {
        // Given
        when(orderRepository.findByIdWithItems(anyLong())).thenReturn(Optional.empty());

        // When/Then
        assertThrows(Exception.class, () -> {
            orderService.getOrderById(999L);
        });
    }

    /**
     * Test getting order by order number successfully
     * 
     * Verifies:
     * - Order is retrieved by order number
     * - OrderDTO is returned with correct data
     */
    @Test
    void testGetOrderByOrderNumber_Success() {
        // Given
        when(orderRepository.findByOrderNumber(anyString())).thenReturn(Optional.of(order));

        // When
        OrderDTO result = orderService.getOrderByOrderNumber(orderNumber);

        // Then
        assertNotNull(result, "Result should not be null");
        assertEquals(orderNumber, result.getOrderNumber());
        verify(orderRepository, times(1)).findByOrderNumber(orderNumber);
    }

    /**
     * Test getting order by non-existent order number
     * 
     * Verifies:
     * - ResourceNotFoundException is thrown for non-existent order number
     */
    @Test
    void testGetOrderByOrderNumber_NotFound() {
        // Given
        when(orderRepository.findByOrderNumber(anyString())).thenReturn(Optional.empty());

        // When/Then
        assertThrows(Exception.class, () -> {
            orderService.getOrderByOrderNumber("INVALID-ORDER");
        });
    }

    /**
     * Test getting user orders with pagination
     * 
     * Verifies:
     * - User orders are retrieved with pagination
     * - Page contains correct orders
     */
    @Test
    void testGetUserOrders_Success() {
        // Given
        List<Order> orders = new ArrayList<>();
        orders.add(order);
        Page<Order> orderPage = new PageImpl<>(orders, PageRequest.of(0, 10), 1);
        
        when(orderRepository.findByUserId(anyLong(), any(Pageable.class)))
            .thenReturn(orderPage);

        // When
        Page<OrderDTO> result = orderService.getUserOrders(userId, PageRequest.of(0, 10));

        // Then
        assertNotNull(result, "Result should not be null");
        assertEquals(1, result.getTotalElements());
        assertEquals(orderNumber, result.getContent().get(0).getOrderNumber());
        verify(orderRepository, times(1)).findByUserId(eq(userId), any(Pageable.class));
    }

    /**
     * Test getting user orders with empty result
     * 
     * Verifies:
     * - Empty page is returned when user has no orders
     */
    @Test
    void testGetUserOrders_EmptyResult() {
        // Given
        Page<Order> emptyPage = new PageImpl<>(new ArrayList<>(), PageRequest.of(0, 10), 0);
        when(orderRepository.findByUserId(anyLong(), any(Pageable.class)))
            .thenReturn(emptyPage);

        // When
        Page<OrderDTO> result = orderService.getUserOrders(userId, PageRequest.of(0, 10));

        // Then
        assertNotNull(result, "Result should not be null");
        assertTrue(result.isEmpty(), "Result should be empty");
        assertEquals(0, result.getTotalElements());
    }

    /**
     * Test updating order status successfully
     * 
     * Verifies:
     * - Order status is updated
     * - Updated order is saved
     * - OrderDTO is returned with new status
     */
    @Test
    void testUpdateOrderStatus_Success() {
        // Given
        Order.OrderStatus newStatus = Order.OrderStatus.SHIPPED;
        when(orderRepository.findById(anyLong())).thenReturn(Optional.of(order));
        when(orderRepository.save(any(Order.class))).thenReturn(order);

        // When
        OrderDTO result = orderService.updateOrderStatus(orderId, newStatus);

        // Then
        assertNotNull(result, "Result should not be null");
        assertEquals(newStatus, order.getStatus());
        verify(orderRepository, times(1)).save(order);
    }

    /**
     * Test updating status of non-existent order
     * 
     * Verifies:
     * - ResourceNotFoundException is thrown for non-existent order
     */
    @Test
    void testUpdateOrderStatus_OrderNotFound() {
        // Given
        when(orderRepository.findById(anyLong())).thenReturn(Optional.empty());

        // When/Then
        assertThrows(Exception.class, () -> {
            orderService.updateOrderStatus(999L, Order.OrderStatus.SHIPPED);
        });
    }

    /**
     * Test cancelling order successfully
     * 
     * Verifies:
     * - Order status is changed to CANCELLED
     * - Cancelled order is saved
     * - OrderDTO is returned with CANCELLED status
     */
    @Test
    void testCancelOrder_Success() {
        // Given
        when(orderRepository.findByIdWithItems(anyLong())).thenReturn(Optional.of(order));
        when(orderRepository.save(any(Order.class))).thenReturn(order);

        // When
        OrderDTO result = orderService.cancelOrder(orderId);

        // Then
        assertNotNull(result, "Result should not be null");
        assertEquals(Order.OrderStatus.CANCELLED, order.getStatus());
        verify(orderRepository, times(1)).save(order);
    }

    /**
     * Test cancelling already delivered order
     * 
     * Verifies:
     * - BusinessException is thrown for delivered orders
     */
    @Test
    void testCancelOrder_AlreadyDelivered() {
        // Given
        order.setStatus(Order.OrderStatus.DELIVERED);
        when(orderRepository.findByIdWithItems(anyLong())).thenReturn(Optional.of(order));

        // When/Then
        assertThrows(Exception.class, () -> {
            orderService.cancelOrder(orderId);
        });
    }

    /**
     * Test cancelling already cancelled order
     * 
     * Verifies:
     * - BusinessException is thrown for already cancelled orders
     */
    @Test
    void testCancelOrder_AlreadyCancelled() {
        // Given
        order.setStatus(Order.OrderStatus.CANCELLED);
        when(orderRepository.findByIdWithItems(anyLong())).thenReturn(Optional.of(order));

        // When/Then
        assertThrows(Exception.class, () -> {
            orderService.cancelOrder(orderId);
        });
    }

    /**
     * Test cancelling non-existent order
     * 
     * Verifies:
     * - ResourceNotFoundException is thrown for non-existent order
     */
    @Test
    void testCancelOrder_OrderNotFound() {
        // Given
        when(orderRepository.findByIdWithItems(anyLong())).thenReturn(Optional.empty());

        // When/Then
        assertThrows(Exception.class, () -> {
            orderService.cancelOrder(999L);
        });
    }
}
