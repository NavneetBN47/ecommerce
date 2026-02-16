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
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

/**
 * Test class for OrderService
 * Tests order management operations including creation, retrieval, and status updates
 * Uses Mockito for mocking repository and service dependencies
 *
 * @author Test Generation Agent
 * @version 1.0
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("OrderService Tests")
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
    private Product testProduct;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
            .id(1L)
            .username("testuser")
            .email("test@example.com")
            .build();

        testProduct = Product.builder()
            .id(1L)
            .name("Test Product")
            .sku("TEST-001")
            .price(new BigDecimal("99.99"))
            .build();

        CartItemDTO cartItem = CartItemDTO.builder()
            .productId(1L)
            .productName("Test Product")
            .quantity(2)
            .price(new BigDecimal("99.99"))
            .build();

        cartDTO = CartDTO.builder()
            .items(List.of(cartItem))
            .totalAmount(new BigDecimal("199.98"))
            .build();

        createOrderRequest = CreateOrderRequestDTO.builder()
            .shippingAddress("123 Test St")
            .billingAddress("123 Test St")
            .paymentMethod("CREDIT_CARD")
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
            .createdAt(LocalDateTime.now())
            .build();
    }

    /**
     * Test successful order creation
     * Verifies that an order can be created from cart items
     */
    @Test
    @DisplayName("Should successfully create order from cart")
    void testCreateOrder_Success() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(cartService.getCartByUserId(1L)).thenReturn(cartDTO);
        when(productService.reduceStock(anyLong(), anyInt())).thenReturn(true);
        when(orderRepository.save(any(Order.class))).thenReturn(testOrder);
        doNothing().when(cartService).clearCart(1L);

        // Act
        OrderDTO result = orderService.createOrder(1L, createOrderRequest);

        // Assert
        assertNotNull(result);
        assertEquals(testOrder.getOrderNumber(), result.getOrderNumber());
        assertEquals(testOrder.getTotalAmount(), result.getTotalAmount());
        assertEquals(Order.OrderStatus.PENDING, result.getStatus());
        verify(userRepository).findById(1L);
        verify(cartService).getCartByUserId(1L);
        verify(orderRepository).save(any(Order.class));
        verify(cartService).clearCart(1L);
    }

    /**
     * Test order creation with empty cart
     * Verifies that BusinessException is thrown when cart is empty
     */
    @Test
    @DisplayName("Should throw BusinessException when cart is empty")
    void testCreateOrder_EmptyCart_ThrowsException() {
        // Arrange
        CartDTO emptyCart = CartDTO.builder()
            .items(new ArrayList<>())
            .totalAmount(BigDecimal.ZERO)
            .build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(cartService.getCartByUserId(1L)).thenReturn(emptyCart);

        // Act & Assert
        BusinessException exception = assertThrows(
            BusinessException.class,
            () -> orderService.createOrder(1L, createOrderRequest)
        );

        assertEquals("Cannot create order from empty cart", exception.getMessage());
        verify(orderRepository, never()).save(any());
        verify(cartService, never()).clearCart(anyLong());
    }

    /**
     * Test order creation with user not found
     * Verifies that ResourceNotFoundException is thrown for invalid user
     */
    @Test
    @DisplayName("Should throw ResourceNotFoundException when user not found")
    void testCreateOrder_UserNotFound_ThrowsException() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(
            ResourceNotFoundException.class,
            () -> orderService.createOrder(1L, createOrderRequest)
        );

        verify(userRepository).findById(1L);
        verify(cartService, never()).getCartByUserId(anyLong());
    }

    /**
     * Test order creation with null billing address uses shipping address
     * Verifies that billing address defaults to shipping address when null
     */
    @Test
    @DisplayName("Should use shipping address when billing address is null")
    void testCreateOrder_NullBillingAddress_UsesShippingAddress() {
        // Arrange
        createOrderRequest.setBillingAddress(null);
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(cartService.getCartByUserId(1L)).thenReturn(cartDTO);
        when(productService.reduceStock(anyLong(), anyInt())).thenReturn(true);
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> {
            Order order = invocation.getArgument(0);
            assertEquals(order.getShippingAddress(), order.getBillingAddress());
            return testOrder;
        });
        doNothing().when(cartService).clearCart(1L);

        // Act
        OrderDTO result = orderService.createOrder(1L, createOrderRequest);

        // Assert
        assertNotNull(result);
        verify(orderRepository).save(any(Order.class));
    }

    /**
     * Test getting order by ID
     * Verifies that order can be retrieved by ID
     */
    @Test
    @DisplayName("Should successfully get order by ID")
    void testGetOrderById_Success() {
        // Arrange
        when(orderRepository.findByIdWithItems(1L)).thenReturn(Optional.of(testOrder));

        // Act
        OrderDTO result = orderService.getOrderById(1L);

        // Assert
        assertNotNull(result);
        assertEquals(testOrder.getId(), result.getId());
        assertEquals(testOrder.getOrderNumber(), result.getOrderNumber());
        verify(orderRepository).findByIdWithItems(1L);
    }

    /**
     * Test getting order by ID when not found
     * Verifies that ResourceNotFoundException is thrown for invalid order ID
     */
    @Test
    @DisplayName("Should throw ResourceNotFoundException when order not found by ID")
    void testGetOrderById_NotFound_ThrowsException() {
        // Arrange
        when(orderRepository.findByIdWithItems(1L)).thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(
            ResourceNotFoundException.class,
            () -> orderService.getOrderById(1L)
        );

        assertTrue(exception.getMessage().contains("Order not found with ID: 1"));
        verify(orderRepository).findByIdWithItems(1L);
    }

    /**
     * Test getting order by order number
     * Verifies that order can be retrieved by order number
     */
    @Test
    @DisplayName("Should successfully get order by order number")
    void testGetOrderByOrderNumber_Success() {
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
     * Test getting order by order number when not found
     * Verifies that ResourceNotFoundException is thrown for invalid order number
     */
    @Test
    @DisplayName("Should throw ResourceNotFoundException when order not found by order number")
    void testGetOrderByOrderNumber_NotFound_ThrowsException() {
        // Arrange
        String orderNumber = "INVALID-ORDER";
        when(orderRepository.findByOrderNumber(orderNumber)).thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(
            ResourceNotFoundException.class,
            () -> orderService.getOrderByOrderNumber(orderNumber)
        );

        assertTrue(exception.getMessage().contains("Order not found with order number"));
        verify(orderRepository).findByOrderNumber(orderNumber);
    }

    /**
     * Test getting user orders with pagination
     * Verifies that user orders can be retrieved with pagination
     */
    @Test
    @DisplayName("Should successfully get user orders with pagination")
    void testGetUserOrders_Success() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        Page<Order> orderPage = new PageImpl<>(List.of(testOrder));
        when(orderRepository.findByUserId(1L, pageable)).thenReturn(orderPage);

        // Act
        Page<OrderDTO> result = orderService.getUserOrders(1L, pageable);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(testOrder.getOrderNumber(), result.getContent().get(0).getOrderNumber());
        verify(orderRepository).findByUserId(1L, pageable);
    }

    /**
     * Test updating order status
     * Verifies that order status can be updated successfully
     */
    @Test
    @DisplayName("Should successfully update order status")
    void testUpdateOrderStatus_Success() {
        // Arrange
        when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));
        when(orderRepository.save(any(Order.class))).thenReturn(testOrder);

        // Act
        OrderDTO result = orderService.updateOrderStatus(1L, Order.OrderStatus.SHIPPED);

        // Assert
        assertNotNull(result);
        verify(orderRepository).findById(1L);
        verify(orderRepository).save(testOrder);
    }

    /**
     * Test updating order status when order not found
     * Verifies that ResourceNotFoundException is thrown for invalid order
     */
    @Test
    @DisplayName("Should throw ResourceNotFoundException when updating non-existent order")
    void testUpdateOrderStatus_NotFound_ThrowsException() {
        // Arrange
        when(orderRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(
            ResourceNotFoundException.class,
            () -> orderService.updateOrderStatus(1L, Order.OrderStatus.SHIPPED)
        );

        verify(orderRepository).findById(1L);
        verify(orderRepository, never()).save(any());
    }

    /**
     * Test cancelling order
     * Verifies that order can be cancelled successfully
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
        verify(orderRepository).findByIdWithItems(1L);
        verify(orderRepository).save(testOrder);
    }

    /**
     * Test cancelling delivered order
     * Verifies that BusinessException is thrown when trying to cancel delivered order
     */
    @Test
    @DisplayName("Should throw BusinessException when cancelling delivered order")
    void testCancelOrder_Delivered_ThrowsException() {
        // Arrange
        testOrder.setStatus(Order.OrderStatus.DELIVERED);
        when(orderRepository.findByIdWithItems(1L)).thenReturn(Optional.of(testOrder));

        // Act & Assert
        BusinessException exception = assertThrows(
            BusinessException.class,
            () -> orderService.cancelOrder(1L)
        );

        assertTrue(exception.getMessage().contains("Cannot cancel order with status"));
        verify(orderRepository, never()).save(any());
    }

    /**
     * Test cancelling already cancelled order
     * Verifies that BusinessException is thrown when trying to cancel already cancelled order
     */
    @Test
    @DisplayName("Should throw BusinessException when cancelling already cancelled order")
    void testCancelOrder_AlreadyCancelled_ThrowsException() {
        // Arrange
        testOrder.setStatus(Order.OrderStatus.CANCELLED);
        when(orderRepository.findByIdWithItems(1L)).thenReturn(Optional.of(testOrder));

        // Act & Assert
        BusinessException exception = assertThrows(
            BusinessException.class,
            () -> orderService.cancelOrder(1L)
        );

        assertTrue(exception.getMessage().contains("Cannot cancel order with status"));
        verify(orderRepository, never()).save(any());
    }

    /**
     * Test order number generation format
     * Verifies that generated order numbers follow the expected format
     */
    @Test
    @DisplayName("Should generate order number in correct format")
    void testCreateOrder_GeneratesValidOrderNumber() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(cartService.getCartByUserId(1L)).thenReturn(cartDTO);
        when(productService.reduceStock(anyLong(), anyInt())).thenReturn(true);
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> {
            Order order = invocation.getArgument(0);
            assertTrue(order.getOrderNumber().startsWith("ORD-"));
            assertTrue(order.getOrderNumber().length() > 10);
            return order;
        });
        doNothing().when(cartService).clearCart(1L);

        // Act
        orderService.createOrder(1L, createOrderRequest);

        // Assert
        verify(orderRepository).save(any(Order.class));
    }

    /**
     * Test order creation reduces product stock
     * Verifies that product stock is reduced when order is created
     */
    @Test
    @DisplayName("Should reduce product stock when creating order")
    void testCreateOrder_ReducesProductStock() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(cartService.getCartByUserId(1L)).thenReturn(cartDTO);
        when(productService.reduceStock(1L, 2)).thenReturn(true);
        when(orderRepository.save(any(Order.class))).thenReturn(testOrder);
        doNothing().when(cartService).clearCart(1L);

        // Act
        orderService.createOrder(1L, createOrderRequest);

        // Assert
        verify(productService).reduceStock(1L, 2);
    }
}