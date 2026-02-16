package com.ecommerce.service;

import com.ecommerce.dto.*;
import com.ecommerce.entity.*;
import com.ecommerce.exception.BusinessException;
import com.ecommerce.exception.ResourceNotFoundException;
import com.ecommerce.repository.OrderRepository;
import com.ecommerce.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
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
 * Unit test class for OrderService.
 * Tests order management operations including creation, retrieval, and status updates.
 * Uses Mockito for mocking repository and service dependencies.
 * 
 * @author QA Automation Team
 * @version 1.0
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("OrderService Unit Tests")
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
    private CreateOrderRequestDTO createOrderRequest;
    private CartDTO cartDTO;
    private Long userId;
    private Long orderId;

    /**
     * Set up test data before each test execution.
     */
    @BeforeEach
    void setUp() {
        userId = 1L;
        orderId = 100L;

        testUser = User.builder()
                .id(userId)
                .username("testuser")
                .email("test@example.com")
                .firstName("Test")
                .lastName("User")
                .build();

        Product product = Product.builder()
                .id(1L)
                .name("Test Product")
                .sku("TEST-001")
                .price(new BigDecimal("99.99"))
                .stockQuantity(100)
                .build();

        testOrder = Order.builder()
                .id(orderId)
                .orderNumber("ORD-20240101120000-1234")
                .user(testUser)
                .totalAmount(new BigDecimal("199.98"))
                .status(Order.OrderStatus.PENDING)
                .shippingAddress("123 Test St")
                .billingAddress("123 Test St")
                .paymentMethod("CREDIT_CARD")
                .paymentStatus("PENDING")
                .items(new ArrayList<>())
                .build();

        OrderItem orderItem = OrderItem.builder()
                .id(1L)
                .order(testOrder)
                .product(product)
                .quantity(2)
                .price(new BigDecimal("99.99"))
                .subtotal(new BigDecimal("199.98"))
                .build();
        testOrder.getItems().add(orderItem);

        createOrderRequest = CreateOrderRequestDTO.builder()
                .shippingAddress("123 Test St")
                .billingAddress("123 Test St")
                .paymentMethod("CREDIT_CARD")
                .build();

        CartItemDTO cartItemDTO = CartItemDTO.builder()
                .productId(1L)
                .quantity(2)
                .price(new BigDecimal("99.99"))
                .build();

        cartDTO = CartDTO.builder()
                .items(Arrays.asList(cartItemDTO))
                .totalAmount(new BigDecimal("199.98"))
                .build();
    }

    /**
     * Test creating order successfully from cart.
     * Verifies that order is created with correct details and cart is cleared.
     */
    @Test
    @DisplayName("Should create order successfully from cart")
    void testCreateOrder_WithValidCart_ShouldCreateOrder() {
        // Arrange
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(cartService.getCartByUserId(userId)).thenReturn(cartDTO);
        when(orderRepository.save(any(Order.class))).thenReturn(testOrder);
        doNothing().when(productService).reduceStock(anyLong(), anyInt());
        doNothing().when(cartService).clearCart(userId);

        // Act
        OrderDTO result = orderService.createOrder(userId, createOrderRequest);

        // Assert
        assertNotNull(result);
        assertEquals(testOrder.getOrderNumber(), result.getOrderNumber());
        assertEquals(userId, result.getUserId());
        assertEquals(Order.OrderStatus.PENDING, result.getStatus());
        verify(userRepository).findById(userId);
        verify(cartService).getCartByUserId(userId);
        verify(productService).reduceStock(1L, 2);
        verify(orderRepository).save(any(Order.class));
        verify(cartService).clearCart(userId);
    }

    /**
     * Test creating order when user not found.
     * Verifies that ResourceNotFoundException is thrown.
     */
    @Test
    @DisplayName("Should throw ResourceNotFoundException when user not found")
    void testCreateOrder_UserNotFound_ShouldThrowResourceNotFoundException() {
        // Arrange
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> orderService.createOrder(userId, createOrderRequest)
        );
        assertTrue(exception.getMessage().contains("User not found"));
        verify(cartService, never()).getCartByUserId(anyLong());
    }

    /**
     * Test creating order with empty cart.
     * Verifies that BusinessException is thrown.
     */
    @Test
    @DisplayName("Should throw BusinessException when cart is empty")
    void testCreateOrder_EmptyCart_ShouldThrowBusinessException() {
        // Arrange
        CartDTO emptyCart = CartDTO.builder()
                .items(new ArrayList<>())
                .totalAmount(BigDecimal.ZERO)
                .build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(cartService.getCartByUserId(userId)).thenReturn(emptyCart);

        // Act & Assert
        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> orderService.createOrder(userId, createOrderRequest)
        );
        assertEquals("Cannot create order from empty cart", exception.getMessage());
        verify(orderRepository, never()).save(any(Order.class));
    }

    /**
     * Test creating order uses billing address from shipping when not provided.
     * Verifies that billing address defaults to shipping address.
     */
    @Test
    @DisplayName("Should use shipping address as billing address when billing not provided")
    void testCreateOrder_NoBillingAddress_ShouldUseSameAsShipping() {
        // Arrange
        createOrderRequest.setBillingAddress(null);
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(cartService.getCartByUserId(userId)).thenReturn(cartDTO);
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> {
            Order order = invocation.getArgument(0);
            assertEquals(order.getShippingAddress(), order.getBillingAddress());
            return testOrder;
        });
        doNothing().when(productService).reduceStock(anyLong(), anyInt());
        doNothing().when(cartService).clearCart(userId);

        // Act
        OrderDTO result = orderService.createOrder(userId, createOrderRequest);

        // Assert
        assertNotNull(result);
        verify(orderRepository).save(any(Order.class));
    }

    /**
     * Test getting order by ID successfully.
     * Verifies that order details are retrieved correctly.
     */
    @Test
    @DisplayName("Should get order by ID successfully")
    void testGetOrderById_ValidId_ShouldReturnOrder() {
        // Arrange
        when(orderRepository.findByIdWithItems(orderId)).thenReturn(Optional.of(testOrder));

        // Act
        OrderDTO result = orderService.getOrderById(orderId);

        // Assert
        assertNotNull(result);
        assertEquals(orderId, result.getId());
        assertEquals(testOrder.getOrderNumber(), result.getOrderNumber());
        assertFalse(result.getItems().isEmpty());
        verify(orderRepository).findByIdWithItems(orderId);
    }

    /**
     * Test getting order by ID when order not found.
     * Verifies that ResourceNotFoundException is thrown.
     */
    @Test
    @DisplayName("Should throw ResourceNotFoundException when order ID not found")
    void testGetOrderById_InvalidId_ShouldThrowResourceNotFoundException() {
        // Arrange
        when(orderRepository.findByIdWithItems(orderId)).thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> orderService.getOrderById(orderId)
        );
        assertTrue(exception.getMessage().contains("Order not found"));
    }

    /**
     * Test getting order by order number successfully.
     * Verifies that order can be retrieved using order number.
     */
    @Test
    @DisplayName("Should get order by order number successfully")
    void testGetOrderByOrderNumber_ValidNumber_ShouldReturnOrder() {
        // Arrange
        String orderNumber = "ORD-20240101120000-1234";
        when(orderRepository.findByOrderNumber(orderNumber)).thenReturn(Optional.of(testOrder));

        // Act
        OrderDTO result = orderService.getOrderByOrderNumber(orderNumber);

        // Assert
        assertNotNull(result);
        assertEquals(orderNumber, result.getOrderNumber());
        verify(orderRepository).findByOrderNumber(orderNumber);
    }

    /**
     * Test getting order by order number when not found.
     * Verifies that ResourceNotFoundException is thrown.
     */
    @Test
    @DisplayName("Should throw ResourceNotFoundException when order number not found")
    void testGetOrderByOrderNumber_InvalidNumber_ShouldThrowResourceNotFoundException() {
        // Arrange
        String orderNumber = "INVALID-ORDER";
        when(orderRepository.findByOrderNumber(orderNumber)).thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> orderService.getOrderByOrderNumber(orderNumber)
        );
        assertTrue(exception.getMessage().contains("Order not found"));
    }

    /**
     * Test getting user orders with pagination.
     * Verifies that paginated list of orders is returned.
     */
    @Test
    @DisplayName("Should get user orders with pagination")
    void testGetUserOrders_ValidUser_ShouldReturnPagedOrders() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        Page<Order> orderPage = new PageImpl<>(Arrays.asList(testOrder));
        when(orderRepository.findByUserId(userId, pageable)).thenReturn(orderPage);

        // Act
        Page<OrderDTO> result = orderService.getUserOrders(userId, pageable);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(testOrder.getOrderNumber(), result.getContent().get(0).getOrderNumber());
        verify(orderRepository).findByUserId(userId, pageable);
    }

    /**
     * Test updating order status successfully.
     * Verifies that order status is updated correctly.
     */
    @Test
    @DisplayName("Should update order status successfully")
    void testUpdateOrderStatus_ValidStatus_ShouldUpdateStatus() {
        // Arrange
        Order.OrderStatus newStatus = Order.OrderStatus.PROCESSING;
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(testOrder));
        when(orderRepository.save(any(Order.class))).thenReturn(testOrder);

        // Act
        OrderDTO result = orderService.updateOrderStatus(orderId, newStatus);

        // Assert
        assertNotNull(result);
        assertEquals(newStatus, testOrder.getStatus());
        verify(orderRepository).findById(orderId);
        verify(orderRepository).save(testOrder);
    }

    /**
     * Test updating order status when order not found.
     * Verifies that ResourceNotFoundException is thrown.
     */
    @Test
    @DisplayName("Should throw ResourceNotFoundException when updating non-existent order")
    void testUpdateOrderStatus_OrderNotFound_ShouldThrowResourceNotFoundException() {
        // Arrange
        when(orderRepository.findById(orderId)).thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> orderService.updateOrderStatus(orderId, Order.OrderStatus.PROCESSING)
        );
        assertTrue(exception.getMessage().contains("Order not found"));
    }

    /**
     * Test cancelling order successfully.
     * Verifies that order status is set to CANCELLED.
     */
    @Test
    @DisplayName("Should cancel order successfully")
    void testCancelOrder_ValidOrder_ShouldCancelOrder() {
        // Arrange
        when(orderRepository.findByIdWithItems(orderId)).thenReturn(Optional.of(testOrder));
        when(orderRepository.save(any(Order.class))).thenReturn(testOrder);

        // Act
        OrderDTO result = orderService.cancelOrder(orderId);

        // Assert
        assertNotNull(result);
        assertEquals(Order.OrderStatus.CANCELLED, testOrder.getStatus());
        verify(orderRepository).findByIdWithItems(orderId);
        verify(orderRepository).save(testOrder);
    }

    /**
     * Test cancelling already delivered order.
     * Verifies that BusinessException is thrown.
     */
    @Test
    @DisplayName("Should throw BusinessException when cancelling delivered order")
    void testCancelOrder_DeliveredOrder_ShouldThrowBusinessException() {
        // Arrange
        testOrder.setStatus(Order.OrderStatus.DELIVERED);
        when(orderRepository.findByIdWithItems(orderId)).thenReturn(Optional.of(testOrder));

        // Act & Assert
        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> orderService.cancelOrder(orderId)
        );
        assertTrue(exception.getMessage().contains("Cannot cancel order"));
        verify(orderRepository, never()).save(any(Order.class));
    }

    /**
     * Test cancelling already cancelled order.
     * Verifies that BusinessException is thrown.
     */
    @Test
    @DisplayName("Should throw BusinessException when cancelling already cancelled order")
    void testCancelOrder_AlreadyCancelled_ShouldThrowBusinessException() {
        // Arrange
        testOrder.setStatus(Order.OrderStatus.CANCELLED);
        when(orderRepository.findByIdWithItems(orderId)).thenReturn(Optional.of(testOrder));

        // Act & Assert
        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> orderService.cancelOrder(orderId)
        );
        assertTrue(exception.getMessage().contains("Cannot cancel order"));
    }

    /**
     * Test that order number is generated with correct format.
     * Verifies order number format and uniqueness.
     */
    @Test
    @DisplayName("Should generate unique order number")
    void testCreateOrder_ShouldGenerateUniqueOrderNumber() {
        // Arrange
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(cartService.getCartByUserId(userId)).thenReturn(cartDTO);
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> {
            Order order = invocation.getArgument(0);
            assertNotNull(order.getOrderNumber());
            assertTrue(order.getOrderNumber().startsWith("ORD-"));
            return testOrder;
        });
        doNothing().when(productService).reduceStock(anyLong(), anyInt());
        doNothing().when(cartService).clearCart(userId);

        // Act
        OrderDTO result = orderService.createOrder(userId, createOrderRequest);

        // Assert
        assertNotNull(result);
        verify(orderRepository).save(any(Order.class));
    }

    /**
     * Test that order items are created correctly from cart items.
     * Verifies order item details match cart items.
     */
    @Test
    @DisplayName("Should create order items correctly from cart items")
    void testCreateOrder_ShouldCreateOrderItemsFromCart() {
        // Arrange
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(cartService.getCartByUserId(userId)).thenReturn(cartDTO);
        when(orderRepository.save(any(Order.class))).thenReturn(testOrder);
        doNothing().when(productService).reduceStock(anyLong(), anyInt());
        doNothing().when(cartService).clearCart(userId);

        // Act
        OrderDTO result = orderService.createOrder(userId, createOrderRequest);

        // Assert
        assertNotNull(result);
        assertFalse(result.getItems().isEmpty());
        OrderItemDTO item = result.getItems().get(0);
        assertEquals(1L, item.getProductId());
        assertEquals(2, item.getQuantity());
        verify(productService).reduceStock(1L, 2);
    }
}