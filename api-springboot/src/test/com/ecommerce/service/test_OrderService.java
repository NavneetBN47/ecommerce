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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

/**
 * JUnit 5 test class for OrderService
 * Tests order management operations including creation, retrieval, and status updates
 * 
 * @author Test Generation Agent
 * @version 1.0
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("OrderService Test Suite")
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

    /**
     * Setup method to initialize test data before each test
     */
    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L)
                .username("testuser")
                .email("test@example.com")
                .build();

        Product product = Product.builder()
                .id(1L)
                .name("Test Product")
                .sku("TEST-SKU-001")
                .price(new BigDecimal("99.99"))
                .build();

        testOrder = Order.builder()
                .id(1L)
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
     * Test creating order successfully
     * Verifies that order is created from cart items
     */
    @Test
    @DisplayName("Should successfully create order from cart")
    void testCreateOrder_Success() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(cartService.getCartByUserId(1L)).thenReturn(cartDTO);
        when(orderRepository.save(any(Order.class))).thenReturn(testOrder);
        doNothing().when(productService).reduceStock(anyLong(), anyInt());
        doNothing().when(cartService).clearCart(1L);

        // Act
        OrderDTO result = orderService.createOrder(1L, createOrderRequest);

        // Assert
        assertNotNull(result);
        assertEquals(testOrder.getOrderNumber(), result.getOrderNumber());
        assertEquals(Order.OrderStatus.PENDING, result.getStatus());
        verify(orderRepository).save(any(Order.class));
        verify(cartService).clearCart(1L);
        verify(productService).reduceStock(anyLong(), anyInt());
    }

    /**
     * Test creating order with empty cart
     * Verifies that exception is thrown when cart is empty
     */
    @Test
    @DisplayName("Should throw BusinessException when cart is empty")
    void testCreateOrder_EmptyCart() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        CartDTO emptyCart = CartDTO.builder()
                .items(new ArrayList<>())
                .totalAmount(BigDecimal.ZERO)
                .build();
        when(cartService.getCartByUserId(1L)).thenReturn(emptyCart);

        // Act & Assert
        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> orderService.createOrder(1L, createOrderRequest)
        );

        assertEquals("Cannot create order from empty cart", exception.getMessage());
        verify(orderRepository, never()).save(any(Order.class));
    }

    /**
     * Test creating order for non-existent user
     * Verifies that exception is thrown when user not found
     */
    @Test
    @DisplayName("Should throw ResourceNotFoundException when user not found")
    void testCreateOrder_UserNotFound() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> orderService.createOrder(1L, createOrderRequest)
        );

        assertTrue(exception.getMessage().contains("User not found"));
    }

    /**
     * Test creating order with null billing address
     * Verifies that shipping address is used as billing address
     */
    @Test
    @DisplayName("Should use shipping address as billing address when billing is null")
    void testCreateOrder_NullBillingAddress() {
        // Arrange
        createOrderRequest.setBillingAddress(null);
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(cartService.getCartByUserId(1L)).thenReturn(cartDTO);
        when(orderRepository.save(any(Order.class))).thenReturn(testOrder);
        doNothing().when(productService).reduceStock(anyLong(), anyInt());
        doNothing().when(cartService).clearCart(1L);

        // Act
        OrderDTO result = orderService.createOrder(1L, createOrderRequest);

        // Assert
        assertNotNull(result);
        assertEquals(result.getShippingAddress(), result.getBillingAddress());
    }

    /**
     * Test getting order by ID successfully
     * Verifies that order is retrieved with all details
     */
    @Test
    @DisplayName("Should successfully retrieve order by ID")
    void testGetOrderById_Success() {
        // Arrange
        when(orderRepository.findByIdWithItems(1L)).thenReturn(Optional.of(testOrder));

        // Act
        OrderDTO result = orderService.getOrderById(1L);

        // Assert
        assertNotNull(result);
        assertEquals(testOrder.getId(), result.getId());
        assertEquals(testOrder.getOrderNumber(), result.getOrderNumber());
        assertFalse(result.getItems().isEmpty());
    }

    /**
     * Test getting non-existent order by ID
     * Verifies that exception is thrown when order not found
     */
    @Test
    @DisplayName("Should throw ResourceNotFoundException when order not found by ID")
    void testGetOrderById_NotFound() {
        // Arrange
        when(orderRepository.findByIdWithItems(1L)).thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> orderService.getOrderById(1L)
        );

        assertTrue(exception.getMessage().contains("Order not found"));
    }

    /**
     * Test getting order by order number successfully
     * Verifies that order is retrieved by order number
     */
    @Test
    @DisplayName("Should successfully retrieve order by order number")
    void testGetOrderByOrderNumber_Success() {
        // Arrange
        String orderNumber = "ORD-20240101120000-1234";
        when(orderRepository.findByOrderNumber(orderNumber)).thenReturn(Optional.of(testOrder));

        // Act
        OrderDTO result = orderService.getOrderByOrderNumber(orderNumber);

        // Assert
        assertNotNull(result);
        assertEquals(orderNumber, result.getOrderNumber());
    }

    /**
     * Test getting non-existent order by order number
     * Verifies that exception is thrown when order not found
     */
    @Test
    @DisplayName("Should throw ResourceNotFoundException when order not found by order number")
    void testGetOrderByOrderNumber_NotFound() {
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
     * Test getting user orders with pagination
     * Verifies that paginated orders are retrieved for user
     */
    @Test
    @DisplayName("Should successfully retrieve user orders with pagination")
    void testGetUserOrders_Success() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        List<Order> orders = Arrays.asList(testOrder);
        Page<Order> orderPage = new PageImpl<>(orders, pageable, 1);
        when(orderRepository.findByUserId(1L, pageable)).thenReturn(orderPage);

        // Act
        Page<OrderDTO> result = orderService.getUserOrders(1L, pageable);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(testOrder.getOrderNumber(), result.getContent().get(0).getOrderNumber());
    }

    /**
     * Test updating order status successfully
     * Verifies that order status is updated
     */
    @Test
    @DisplayName("Should successfully update order status")
    void testUpdateOrderStatus_Success() {
        // Arrange
        when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));
        when(orderRepository.save(any(Order.class))).thenReturn(testOrder);

        // Act
        OrderDTO result = orderService.updateOrderStatus(1L, Order.OrderStatus.PROCESSING);

        // Assert
        assertNotNull(result);
        assertEquals(Order.OrderStatus.PROCESSING, testOrder.getStatus());
        verify(orderRepository).save(testOrder);
    }

    /**
     * Test updating status of non-existent order
     * Verifies that exception is thrown when order not found
     */
    @Test
    @DisplayName("Should throw ResourceNotFoundException when updating non-existent order")
    void testUpdateOrderStatus_OrderNotFound() {
        // Arrange
        when(orderRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(
                ResourceNotFoundException.class,
                () -> orderService.updateOrderStatus(1L, Order.OrderStatus.PROCESSING)
        );
    }

    /**
     * Test cancelling order successfully
     * Verifies that order is cancelled
     */
    @Test
    @DisplayName("Should successfully cancel order")
    void testCancelOrder_Success() {
        // Arrange
        when(orderRepository.findByIdWithItems(1L)).thenReturn(Optional.of(testOrder));
        when(orderRepository.save(any(Order.class))).thenReturn(testOrder);

        // Act
        OrderDTO result = orderService.cancelOrder(1L);

        // Assert
        assertNotNull(result);
        assertEquals(Order.OrderStatus.CANCELLED, testOrder.getStatus());
        verify(orderRepository).save(testOrder);
    }

    /**
     * Test cancelling delivered order
     * Verifies that exception is thrown when trying to cancel delivered order
     */
    @Test
    @DisplayName("Should throw BusinessException when cancelling delivered order")
    void testCancelOrder_DeliveredOrder() {
        // Arrange
        testOrder.setStatus(Order.OrderStatus.DELIVERED);
        when(orderRepository.findByIdWithItems(1L)).thenReturn(Optional.of(testOrder));

        // Act & Assert
        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> orderService.cancelOrder(1L)
        );

        assertTrue(exception.getMessage().contains("Cannot cancel order"));
        verify(orderRepository, never()).save(any(Order.class));
    }

    /**
     * Test cancelling already cancelled order
     * Verifies that exception is thrown when order already cancelled
     */
    @Test
    @DisplayName("Should throw BusinessException when cancelling already cancelled order")
    void testCancelOrder_AlreadyCancelled() {
        // Arrange
        testOrder.setStatus(Order.OrderStatus.CANCELLED);
        when(orderRepository.findByIdWithItems(1L)).thenReturn(Optional.of(testOrder));

        // Act & Assert
        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> orderService.cancelOrder(1L)
        );

        assertTrue(exception.getMessage().contains("Cannot cancel order"));
    }

    /**
     * Test order number generation format
     * Verifies that generated order numbers follow expected format
     */
    @Test
    @DisplayName("Should generate order number in correct format")
    void testCreateOrder_OrderNumberFormat() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(cartService.getCartByUserId(1L)).thenReturn(cartDTO);
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> {
            Order order = invocation.getArgument(0);
            order.setId(1L);
            return order;
        });
        doNothing().when(productService).reduceStock(anyLong(), anyInt());
        doNothing().when(cartService).clearCart(1L);

        // Act
        OrderDTO result = orderService.createOrder(1L, createOrderRequest);

        // Assert
        assertNotNull(result.getOrderNumber());
        assertTrue(result.getOrderNumber().startsWith("ORD-"));
        assertTrue(result.getOrderNumber().matches("ORD-\\d{14}-\\d{4}"));
    }

    /**
     * Test order DTO mapping includes all fields
     * Verifies that all order fields are properly mapped to DTO
     */
    @Test
    @DisplayName("Should map all order fields to DTO")
    void testGetOrderById_CompleteMapping() {
        // Arrange
        when(orderRepository.findByIdWithItems(1L)).thenReturn(Optional.of(testOrder));

        // Act
        OrderDTO result = orderService.getOrderById(1L);

        // Assert
        assertNotNull(result);
        assertEquals(testOrder.getId(), result.getId());
        assertEquals(testOrder.getOrderNumber(), result.getOrderNumber());
        assertEquals(testOrder.getUser().getId(), result.getUserId());
        assertEquals(testOrder.getTotalAmount(), result.getTotalAmount());
        assertEquals(testOrder.getStatus(), result.getStatus());
        assertEquals(testOrder.getShippingAddress(), result.getShippingAddress());
        assertEquals(testOrder.getBillingAddress(), result.getBillingAddress());
        assertEquals(testOrder.getPaymentMethod(), result.getPaymentMethod());
        assertEquals(testOrder.getPaymentStatus(), result.getPaymentStatus());
        assertNotNull(result.getItems());
        assertEquals(1, result.getItems().size());
    }
}