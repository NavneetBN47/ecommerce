package com.ecommerce.service;

import com.ecommerce.dto.CreateOrderRequestDTO;
import com.ecommerce.dto.OrderDTO;
import com.ecommerce.entity.Order;
import com.ecommerce.entity.OrderItem;
import com.ecommerce.entity.Product;
import com.ecommerce.entity.User;
import com.ecommerce.exception.BusinessException;
import com.ecommerce.exception.ResourceNotFoundException;
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
import static org.mockito.Mockito.*;

/**
 * JUnit 5 test class for OrderService
 * Tests order management operations including create, retrieve, update status, and cancel
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

    private Long userId;
    private Long orderId;
    private String orderNumber;
    private User testUser;
    private Order testOrder;

    @BeforeEach
    void setUp() {
        userId = 1L;
        orderId = 100L;
        orderNumber = "ORD-20240101-1234";

        testUser = User.builder()
            .id(userId)
            .username("testuser")
            .email("test@example.com")
            .build();

        testOrder = Order.builder()
            .id(orderId)
            .orderNumber(orderNumber)
            .user(testUser)
            .totalAmount(BigDecimal.valueOf(250.00))
            .status(Order.OrderStatus.PENDING)
            .shippingAddress("123 Main St")
            .billingAddress("123 Main St")
            .paymentMethod("CREDIT_CARD")
            .paymentStatus("PENDING")
            .items(new ArrayList<>())
            .build();
    }

    /**
     * Test creating order from cart successfully
     * Verifies order creation with cart items
     */
    @Test
    void createOrderShouldCreateOrderFromCart() {
        // Given
        CreateOrderRequestDTO request = new CreateOrderRequestDTO();
        request.setShippingAddress("123 Main St");
        request.setPaymentMethod("CREDIT_CARD");

        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(orderRepository.save(any(Order.class))).thenReturn(testOrder);
        doNothing().when(cartService).clearCart(userId);

        // When
        OrderDTO result = orderService.createOrder(userId, request);

        // Then
        assertNotNull(result);
        assertEquals(orderNumber, result.getOrderNumber());
        verify(orderRepository, times(1)).save(any(Order.class));
        verify(cartService, times(1)).clearCart(userId);
    }

    /**
     * Test creating order from empty cart
     * Verifies BusinessException is thrown
     */
    @Test
    void createOrderShouldThrowExceptionForEmptyCart() {
        // Given
        CreateOrderRequestDTO request = new CreateOrderRequestDTO();
        request.setShippingAddress("123 Main St");

        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));

        // When/Then
        assertThrows(BusinessException.class, () -> {
            orderService.createOrder(userId, request);
        });
    }

    /**
     * Test creating order for non-existent user
     * Verifies ResourceNotFoundException is thrown
     */
    @Test
    void createOrderShouldThrowExceptionForNonExistentUser() {
        // Given
        CreateOrderRequestDTO request = new CreateOrderRequestDTO();
        request.setShippingAddress("123 Main St");

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // When/Then
        assertThrows(ResourceNotFoundException.class, () -> {
            orderService.createOrder(userId, request);
        });
    }

    /**
     * Test getting order by ID
     * Verifies order retrieval
     */
    @Test
    void getOrderByIdShouldReturnOrder() {
        // Given
        when(orderRepository.findByIdWithItems(orderId)).thenReturn(Optional.of(testOrder));

        // When
        OrderDTO result = orderService.getOrderById(orderId);

        // Then
        assertNotNull(result);
        assertEquals(orderId, result.getId());
        assertEquals(orderNumber, result.getOrderNumber());
        verify(orderRepository, times(1)).findByIdWithItems(orderId);
    }

    /**
     * Test getting non-existent order by ID
     * Verifies ResourceNotFoundException is thrown
     */
    @Test
    void getOrderByIdShouldThrowExceptionForNonExistentOrder() {
        // Given
        when(orderRepository.findByIdWithItems(orderId)).thenReturn(Optional.empty());

        // When/Then
        assertThrows(ResourceNotFoundException.class, () -> {
            orderService.getOrderById(orderId);
        });
    }

    /**
     * Test getting order by order number
     * Verifies order retrieval by order number
     */
    @Test
    void getOrderByOrderNumberShouldReturnOrder() {
        // Given
        when(orderRepository.findByOrderNumber(orderNumber)).thenReturn(Optional.of(testOrder));

        // When
        OrderDTO result = orderService.getOrderByOrderNumber(orderNumber);

        // Then
        assertNotNull(result);
        assertEquals(orderNumber, result.getOrderNumber());
        verify(orderRepository, times(1)).findByOrderNumber(orderNumber);
    }

    /**
     * Test getting order by invalid order number
     * Verifies ResourceNotFoundException is thrown
     */
    @Test
    void getOrderByOrderNumberShouldThrowExceptionForInvalidOrderNumber() {
        // Given
        when(orderRepository.findByOrderNumber(orderNumber)).thenReturn(Optional.empty());

        // When/Then
        assertThrows(ResourceNotFoundException.class, () -> {
            orderService.getOrderByOrderNumber(orderNumber);
        });
    }

    /**
     * Test getting user orders with pagination
     * Verifies paginated order retrieval
     */
    @Test
    void getUserOrdersShouldReturnPaginatedOrders() {
        // Given
        List<Order> orders = new ArrayList<>();
        orders.add(testOrder);
        Page<Order> orderPage = new PageImpl<>(orders, PageRequest.of(0, 10), 1);

        when(orderRepository.findByUserId(eq(userId), any(Pageable.class))).thenReturn(orderPage);

        // When
        Page<OrderDTO> result = orderService.getUserOrders(userId, PageRequest.of(0, 10));

        // Then
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(orderRepository, times(1)).findByUserId(eq(userId), any(Pageable.class));
    }

    /**
     * Test updating order status
     * Verifies status update functionality
     */
    @Test
    void updateOrderStatusShouldUpdateStatus() {
        // Given
        Order.OrderStatus newStatus = Order.OrderStatus.SHIPPED;
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(testOrder));
        when(orderRepository.save(any(Order.class))).thenReturn(testOrder);

        // When
        OrderDTO result = orderService.updateOrderStatus(orderId, newStatus);

        // Then
        assertNotNull(result);
        assertEquals(newStatus, testOrder.getStatus());
        verify(orderRepository, times(1)).save(testOrder);
    }

    /**
     * Test updating status of non-existent order
     * Verifies ResourceNotFoundException is thrown
     */
    @Test
    void updateOrderStatusShouldThrowExceptionForNonExistentOrder() {
        // Given
        when(orderRepository.findById(orderId)).thenReturn(Optional.empty());

        // When/Then
        assertThrows(ResourceNotFoundException.class, () -> {
            orderService.updateOrderStatus(orderId, Order.OrderStatus.SHIPPED);
        });
    }

    /**
     * Test cancelling order
     * Verifies order cancellation
     */
    @Test
    void cancelOrderShouldCancelOrder() {
        // Given
        when(orderRepository.findByIdWithItems(orderId)).thenReturn(Optional.of(testOrder));
        when(orderRepository.save(any(Order.class))).thenReturn(testOrder);

        // When
        OrderDTO result = orderService.cancelOrder(orderId);

        // Then
        assertNotNull(result);
        assertEquals(Order.OrderStatus.CANCELLED, testOrder.getStatus());
        verify(orderRepository, times(1)).save(testOrder);
    }

    /**
     * Test cancelling delivered order
     * Verifies BusinessException is thrown
     */
    @Test
    void cancelOrderShouldThrowExceptionForDeliveredOrder() {
        // Given
        testOrder.setStatus(Order.OrderStatus.DELIVERED);
        when(orderRepository.findByIdWithItems(orderId)).thenReturn(Optional.of(testOrder));

        // When/Then
        assertThrows(BusinessException.class, () -> {
            orderService.cancelOrder(orderId);
        });
    }

    /**
     * Test cancelling already cancelled order
     * Verifies BusinessException is thrown
     */
    @Test
    void cancelOrderShouldThrowExceptionForAlreadyCancelledOrder() {
        // Given
        testOrder.setStatus(Order.OrderStatus.CANCELLED);
        when(orderRepository.findByIdWithItems(orderId)).thenReturn(Optional.of(testOrder));

        // When/Then
        assertThrows(BusinessException.class, () -> {
            orderService.cancelOrder(orderId);
        });
    }

    /**
     * Test order number generation
     * Verifies unique order number format
     */
    @Test
    void createOrderShouldGenerateUniqueOrderNumber() {
        // Given
        CreateOrderRequestDTO request = new CreateOrderRequestDTO();
        request.setShippingAddress("123 Main St");
        request.setPaymentMethod("CREDIT_CARD");

        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> {
            Order order = invocation.getArgument(0);
            assertNotNull(order.getOrderNumber());
            assertTrue(order.getOrderNumber().startsWith("ORD-"));
            return order;
        });
        doNothing().when(cartService).clearCart(userId);

        // When
        orderService.createOrder(userId, request);

        // Then
        verify(orderRepository, times(1)).save(any(Order.class));
    }
}