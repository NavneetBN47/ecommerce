package com.ecommerce.service;

import com.ecommerce.dto.*;
import com.ecommerce.entity.*;
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
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

/**
 * Test class for OrderService
 * Tests order management operations including creation, retrieval, and status updates
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

    private User testUser;
    private Order testOrder;
    private CartDTO testCartDTO;
    private CreateOrderRequestDTO createOrderRequest;
    private Long userId;
    private Long orderId;

    @BeforeEach
    void setUp() {
        userId = 1L;
        orderId = 100L;

        testUser = User.builder()
            .id(userId)
            .username("testuser")
            .email("test@example.com")
            .build();

        CartItemDTO cartItem = CartItemDTO.builder()
            .productId(1L)
            .productName("Test Product")
            .quantity(2)
            .price(new BigDecimal("50.00"))
            .build();

        testCartDTO = CartDTO.builder()
            .items(Arrays.asList(cartItem))
            .totalAmount(new BigDecimal("100.00"))
            .build();

        createOrderRequest = CreateOrderRequestDTO.builder()
            .shippingAddress("123 Test St")
            .billingAddress("123 Test St")
            .paymentMethod("CREDIT_CARD")
            .build();

        testOrder = Order.builder()
            .id(orderId)
            .orderNumber("ORD-20240101120000-1234")
            .user(testUser)
            .totalAmount(new BigDecimal("100.00"))
            .status(Order.OrderStatus.PENDING)
            .shippingAddress("123 Test St")
            .billingAddress("123 Test St")
            .paymentMethod("CREDIT_CARD")
            .paymentStatus("PENDING")
            .items(new ArrayList<>())
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .build();
    }

    /**
     * Test creating order successfully
     */
    @Test
    void testCreateOrder_Success() {
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(cartService.getCartByUserId(userId)).thenReturn(testCartDTO);
        when(orderRepository.save(any(Order.class))).thenReturn(testOrder);
        doNothing().when(productService).reduceStock(anyLong(), anyInt());
        doNothing().when(cartService).clearCart(userId);

        OrderDTO result = orderService.createOrder(userId, createOrderRequest);

        assertNotNull(result);
        assertEquals(orderId, result.getId());
        assertEquals("ORD-20240101120000-1234", result.getOrderNumber());
        assertEquals(new BigDecimal("100.00"), result.getTotalAmount());
        verify(orderRepository).save(any(Order.class));
        verify(cartService).clearCart(userId);
    }

    /**
     * Test creating order when user not found
     */
    @Test
    void testCreateOrder_UserNotFound_ThrowsException() {
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            orderService.createOrder(userId, createOrderRequest);
        });

        assertTrue(exception.getMessage().contains("User not found"));
        verify(orderRepository, never()).save(any());
    }

    /**
     * Test creating order with empty cart
     */
    @Test
    void testCreateOrder_EmptyCart_ThrowsException() {
        CartDTO emptyCart = CartDTO.builder()
            .items(new ArrayList<>())
            .totalAmount(BigDecimal.ZERO)
            .build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(cartService.getCartByUserId(userId)).thenReturn(emptyCart);

        BusinessException exception = assertThrows(BusinessException.class, () -> {
            orderService.createOrder(userId, createOrderRequest);
        });

        assertEquals("Cannot create order from empty cart", exception.getMessage());
        verify(orderRepository, never()).save(any());
    }

    /**
     * Test creating order with null cart items
     */
    @Test
    void testCreateOrder_NullCartItems_ThrowsException() {
        CartDTO nullItemsCart = CartDTO.builder()
            .items(null)
            .totalAmount(BigDecimal.ZERO)
            .build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(cartService.getCartByUserId(userId)).thenReturn(nullItemsCart);

        BusinessException exception = assertThrows(BusinessException.class, () -> {
            orderService.createOrder(userId, createOrderRequest);
        });

        assertEquals("Cannot create order from empty cart", exception.getMessage());
    }

    /**
     * Test creating order uses shipping address as billing when billing not provided
     */
    @Test
    void testCreateOrder_NoBillingAddress_UsesShippingAddress() {
        CreateOrderRequestDTO requestWithoutBilling = CreateOrderRequestDTO.builder()
            .shippingAddress("456 Shipping St")
            .paymentMethod("PAYPAL")
            .build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(cartService.getCartByUserId(userId)).thenReturn(testCartDTO);
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> {
            Order order = invocation.getArgument(0);
            assertEquals("456 Shipping St", order.getShippingAddress());
            assertEquals("456 Shipping St", order.getBillingAddress());
            return testOrder;
        });
        doNothing().when(productService).reduceStock(anyLong(), anyInt());
        doNothing().when(cartService).clearCart(userId);

        orderService.createOrder(userId, requestWithoutBilling);

        verify(orderRepository).save(any(Order.class));
    }

    /**
     * Test getting order by ID successfully
     */
    @Test
    void testGetOrderById_Success() {
        when(orderRepository.findByIdWithItems(orderId)).thenReturn(Optional.of(testOrder));

        OrderDTO result = orderService.getOrderById(orderId);

        assertNotNull(result);
        assertEquals(orderId, result.getId());
        assertEquals("ORD-20240101120000-1234", result.getOrderNumber());
        verify(orderRepository).findByIdWithItems(orderId);
    }

    /**
     * Test getting order by ID when not found
     */
    @Test
    void testGetOrderById_NotFound_ThrowsException() {
        when(orderRepository.findByIdWithItems(orderId)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            orderService.getOrderById(orderId);
        });

        assertTrue(exception.getMessage().contains("Order not found"));
    }

    /**
     * Test getting order by order number successfully
     */
    @Test
    void testGetOrderByOrderNumber_Success() {
        String orderNumber = "ORD-20240101120000-1234";
        when(orderRepository.findByOrderNumber(orderNumber)).thenReturn(Optional.of(testOrder));

        OrderDTO result = orderService.getOrderByOrderNumber(orderNumber);

        assertNotNull(result);
        assertEquals(orderNumber, result.getOrderNumber());
        verify(orderRepository).findByOrderNumber(orderNumber);
    }

    /**
     * Test getting order by order number when not found
     */
    @Test
    void testGetOrderByOrderNumber_NotFound_ThrowsException() {
        String orderNumber = "ORD-INVALID";
        when(orderRepository.findByOrderNumber(orderNumber)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            orderService.getOrderByOrderNumber(orderNumber);
        });

        assertTrue(exception.getMessage().contains("Order not found"));
    }

    /**
     * Test getting user orders successfully
     */
    @Test
    void testGetUserOrders_Success() {
        Pageable pageable = PageRequest.of(0, 10);
        List<Order> orders = Arrays.asList(testOrder);
        Page<Order> orderPage = new PageImpl<>(orders, pageable, 1);

        when(orderRepository.findByUserId(userId, pageable)).thenReturn(orderPage);

        Page<OrderDTO> result = orderService.getUserOrders(userId, pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(orderId, result.getContent().get(0).getId());
        verify(orderRepository).findByUserId(userId, pageable);
    }

    /**
     * Test updating order status successfully
     */
    @Test
    void testUpdateOrderStatus_Success() {
        Order.OrderStatus newStatus = Order.OrderStatus.PROCESSING;
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(testOrder));
        when(orderRepository.save(any(Order.class))).thenReturn(testOrder);

        OrderDTO result = orderService.updateOrderStatus(orderId, newStatus);

        assertNotNull(result);
        assertEquals(newStatus, testOrder.getStatus());
        verify(orderRepository).save(testOrder);
    }

    /**
     * Test updating order status when order not found
     */
    @Test
    void testUpdateOrderStatus_OrderNotFound_ThrowsException() {
        when(orderRepository.findById(orderId)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            orderService.updateOrderStatus(orderId, Order.OrderStatus.PROCESSING);
        });

        assertTrue(exception.getMessage().contains("Order not found"));
    }

    /**
     * Test cancelling order successfully
     */
    @Test
    void testCancelOrder_Success() {
        testOrder.setStatus(Order.OrderStatus.PENDING);
        when(orderRepository.findByIdWithItems(orderId)).thenReturn(Optional.of(testOrder));
        when(orderRepository.save(any(Order.class))).thenReturn(testOrder);

        OrderDTO result = orderService.cancelOrder(orderId);

        assertNotNull(result);
        assertEquals(Order.OrderStatus.CANCELLED, testOrder.getStatus());
        verify(orderRepository).save(testOrder);
    }

    /**
     * Test cancelling already delivered order
     */
    @Test
    void testCancelOrder_AlreadyDelivered_ThrowsException() {
        testOrder.setStatus(Order.OrderStatus.DELIVERED);
        when(orderRepository.findByIdWithItems(orderId)).thenReturn(Optional.of(testOrder));

        BusinessException exception = assertThrows(BusinessException.class, () -> {
            orderService.cancelOrder(orderId);
        });

        assertTrue(exception.getMessage().contains("Cannot cancel order"));
        verify(orderRepository, never()).save(any());
    }

    /**
     * Test cancelling already cancelled order
     */
    @Test
    void testCancelOrder_AlreadyCancelled_ThrowsException() {
        testOrder.setStatus(Order.OrderStatus.CANCELLED);
        when(orderRepository.findByIdWithItems(orderId)).thenReturn(Optional.of(testOrder));

        BusinessException exception = assertThrows(BusinessException.class, () -> {
            orderService.cancelOrder(orderId);
        });

        assertTrue(exception.getMessage().contains("Cannot cancel order"));
    }

    /**
     * Test cancelling order when order not found
     */
    @Test
    void testCancelOrder_OrderNotFound_ThrowsException() {
        when(orderRepository.findByIdWithItems(orderId)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            orderService.cancelOrder(orderId);
        });

        assertTrue(exception.getMessage().contains("Order not found"));
    }

    /**
     * Test order DTO mapping includes all fields
     */
    @Test
    void testGetOrderById_MapsAllFields() {
        OrderItem orderItem = OrderItem.builder()
            .id(1L)
            .product(Product.builder().id(1L).name("Product").sku("SKU123").price(new BigDecimal("50.00")).build())
            .quantity(2)
            .price(new BigDecimal("50.00"))
            .subtotal(new BigDecimal("100.00"))
            .build();
        testOrder.getItems().add(orderItem);

        when(orderRepository.findByIdWithItems(orderId)).thenReturn(Optional.of(testOrder));

        OrderDTO result = orderService.getOrderById(orderId);

        assertNotNull(result);
        assertEquals(orderId, result.getId());
        assertEquals(userId, result.getUserId());
        assertEquals(Order.OrderStatus.PENDING, result.getStatus());
        assertEquals("123 Test St", result.getShippingAddress());
        assertEquals("123 Test St", result.getBillingAddress());
        assertEquals("CREDIT_CARD", result.getPaymentMethod());
        assertEquals("PENDING", result.getPaymentStatus());
        assertNotNull(result.getItems());
        assertEquals(1, result.getItems().size());
    }
}