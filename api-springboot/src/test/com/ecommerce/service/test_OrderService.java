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
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Test class for OrderService
 * Tests order management operations including creation, retrieval, status updates, and cancellation
 * 
 * @author QA Automation Team
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
    private Product testProduct;
    private CreateOrderRequestDTO createOrderRequest;
    private CartDTO cartDTO;
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
            .firstName("Test")
            .lastName("User")
            .build();

        testProduct = Product.builder()
            .id(1L)
            .name("Test Product")
            .sku("TEST-SKU-001")
            .price(new BigDecimal("99.99"))
            .imageUrl("http://example.com/image.jpg")
            .stockQuantity(100)
            .build();

        createOrderRequest = CreateOrderRequestDTO.builder()
            .shippingAddress("123 Test St, Test City, TC 12345")
            .billingAddress("123 Test St, Test City, TC 12345")
            .paymentMethod("CREDIT_CARD")
            .build();

        CartItemDTO cartItem = CartItemDTO.builder()
            .productId(1L)
            .productName("Test Product")
            .quantity(2)
            .price(new BigDecimal("99.99"))
            .subtotal(new BigDecimal("199.98"))
            .build();

        cartDTO = CartDTO.builder()
            .items(Arrays.asList(cartItem))
            .totalAmount(new BigDecimal("199.98"))
            .build();

        testOrder = Order.builder()
            .id(orderId)
            .orderNumber("ORD-20240101120000-1234")
            .user(testUser)
            .totalAmount(new BigDecimal("199.98"))
            .status(Order.OrderStatus.PENDING)
            .shippingAddress("123 Test St, Test City, TC 12345")
            .billingAddress("123 Test St, Test City, TC 12345")
            .paymentMethod("CREDIT_CARD")
            .paymentStatus("PENDING")
            .items(new ArrayList<>())
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .build();

        OrderItem orderItem = OrderItem.builder()
            .id(1L)
            .order(testOrder)
            .product(testProduct)
            .quantity(2)
            .price(new BigDecimal("99.99"))
            .subtotal(new BigDecimal("199.98"))
            .build();
        testOrder.getItems().add(orderItem);
    }

    /**
     * Test successful order creation from cart
     * Verifies that order is created with all items from cart
     */
    @Test
    @DisplayName("Should create order from cart successfully")
    void testCreateOrder_Success() {
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
        assertEquals(testOrder.getTotalAmount(), result.getTotalAmount());
        assertEquals(Order.OrderStatus.PENDING, result.getStatus());
        assertFalse(result.getItems().isEmpty());
        verify(userRepository).findById(userId);
        verify(cartService).getCartByUserId(userId);
        verify(productService).reduceStock(anyLong(), anyInt());
        verify(cartService).clearCart(userId);
        verify(orderRepository).save(any(Order.class));
    }

    /**
     * Test order creation when user not found
     * Verifies that ResourceNotFoundException is thrown
     */
    @Test
    @DisplayName("Should throw ResourceNotFoundException when user not found")
    void testCreateOrder_UserNotFound() {
        // Arrange
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            orderService.createOrder(userId, createOrderRequest);
        });

        assertTrue(exception.getMessage().contains("User not found"));
        verify(cartService, never()).getCartByUserId(anyLong());
        verify(orderRepository, never()).save(any());
    }

    /**
     * Test order creation with empty cart
     * Verifies that BusinessException is thrown
     */
    @Test
    @DisplayName("Should throw BusinessException when cart is empty")
    void testCreateOrder_EmptyCart() {
        // Arrange
        CartDTO emptyCart = CartDTO.builder()
            .items(new ArrayList<>())
            .totalAmount(BigDecimal.ZERO)
            .build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(cartService.getCartByUserId(userId)).thenReturn(emptyCart);

        // Act & Assert
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            orderService.createOrder(userId, createOrderRequest);
        });

        assertEquals("Cannot create order from empty cart", exception.getMessage());
        verify(orderRepository, never()).save(any());
    }

    /**
     * Test order creation with null billing address
     * Verifies that shipping address is used as billing address
     */
    @Test
    @DisplayName("Should use shipping address as billing address when billing address is null")
    void testCreateOrder_NullBillingAddress() {
        // Arrange
        createOrderRequest.setBillingAddress(null);
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(cartService.getCartByUserId(userId)).thenReturn(cartDTO);
        when(orderRepository.save(any(Order.class))).thenReturn(testOrder);
        doNothing().when(productService).reduceStock(anyLong(), anyInt());
        doNothing().when(cartService).clearCart(userId);

        // Act
        OrderDTO result = orderService.createOrder(userId, createOrderRequest);

        // Assert
        assertNotNull(result);
        assertEquals(result.getShippingAddress(), result.getBillingAddress());
    }

    /**
     * Test getting order by ID
     * Verifies that order is retrieved successfully
     */
    @Test
    @DisplayName("Should get order by ID successfully")
    void testGetOrderById_Success() {
        // Arrange
        when(orderRepository.findByIdWithItems(orderId)).thenReturn(Optional.of(testOrder));

        // Act
        OrderDTO result = orderService.getOrderById(orderId);

        // Assert
        assertNotNull(result);
        assertEquals(orderId, result.getId());
        assertEquals(testOrder.getOrderNumber(), result.getOrderNumber());
        verify(orderRepository).findByIdWithItems(orderId);
    }

    /**
     * Test getting order by ID when not found
     * Verifies that ResourceNotFoundException is thrown
     */
    @Test
    @DisplayName("Should throw ResourceNotFoundException when order not found by ID")
    void testGetOrderById_NotFound() {
        // Arrange
        when(orderRepository.findByIdWithItems(orderId)).thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            orderService.getOrderById(orderId);
        });

        assertTrue(exception.getMessage().contains("Order not found"));
    }

    /**
     * Test getting order by order number
     * Verifies that order is retrieved by order number
     */
    @Test
    @DisplayName("Should get order by order number successfully")
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
     * Verifies that ResourceNotFoundException is thrown
     */
    @Test
    @DisplayName("Should throw ResourceNotFoundException when order not found by order number")
    void testGetOrderByOrderNumber_NotFound() {
        // Arrange
        String orderNumber = "INVALID-ORDER-NUMBER";
        when(orderRepository.findByOrderNumber(orderNumber)).thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            orderService.getOrderByOrderNumber(orderNumber);
        });

        assertTrue(exception.getMessage().contains("Order not found"));
    }

    /**
     * Test getting user orders with pagination
     * Verifies that paginated orders are retrieved
     */
    @Test
    @DisplayName("Should get user orders with pagination")
    void testGetUserOrders_Success() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        List<Order> orders = Arrays.asList(testOrder);
        Page<Order> orderPage = new PageImpl<>(orders, pageable, orders.size());

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
     * Test updating order status
     * Verifies that order status is updated successfully
     */
    @Test
    @DisplayName("Should update order status successfully")
    void testUpdateOrderStatus_Success() {
        // Arrange
        Order.OrderStatus newStatus = Order.OrderStatus.PROCESSING;
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(testOrder));
        when(orderRepository.save(testOrder)).thenReturn(testOrder);

        // Act
        OrderDTO result = orderService.updateOrderStatus(orderId, newStatus);

        // Assert
        assertNotNull(result);
        assertEquals(newStatus, testOrder.getStatus());
        verify(orderRepository).findById(orderId);
        verify(orderRepository).save(testOrder);
    }

    /**
     * Test updating order status when order not found
     * Verifies that ResourceNotFoundException is thrown
     */
    @Test
    @DisplayName("Should throw ResourceNotFoundException when updating status of non-existent order")
    void testUpdateOrderStatus_OrderNotFound() {
        // Arrange
        when(orderRepository.findById(orderId)).thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            orderService.updateOrderStatus(orderId, Order.OrderStatus.PROCESSING);
        });

        assertTrue(exception.getMessage().contains("Order not found"));
        verify(orderRepository, never()).save(any());
    }

    /**
     * Test cancelling order successfully
     * Verifies that order status is changed to CANCELLED
     */
    @Test
    @DisplayName("Should cancel order successfully")
    void testCancelOrder_Success() {
        // Arrange
        when(orderRepository.findByIdWithItems(orderId)).thenReturn(Optional.of(testOrder));
        when(orderRepository.save(testOrder)).thenReturn(testOrder);

        // Act
        OrderDTO result = orderService.cancelOrder(orderId);

        // Assert
        assertNotNull(result);
        assertEquals(Order.OrderStatus.CANCELLED, testOrder.getStatus());
        verify(orderRepository).findByIdWithItems(orderId);
        verify(orderRepository).save(testOrder);
    }

    /**
     * Test cancelling already delivered order
     * Verifies that BusinessException is thrown
     */
    @Test
    @DisplayName("Should throw BusinessException when cancelling delivered order")
    void testCancelOrder_AlreadyDelivered() {
        // Arrange
        testOrder.setStatus(Order.OrderStatus.DELIVERED);
        when(orderRepository.findByIdWithItems(orderId)).thenReturn(Optional.of(testOrder));

        // Act & Assert
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            orderService.cancelOrder(orderId);
        });

        assertTrue(exception.getMessage().contains("Cannot cancel order"));
        verify(orderRepository, never()).save(any());
    }

    /**
     * Test cancelling already cancelled order
     * Verifies that BusinessException is thrown
     */
    @Test
    @DisplayName("Should throw BusinessException when cancelling already cancelled order")
    void testCancelOrder_AlreadyCancelled() {
        // Arrange
        testOrder.setStatus(Order.OrderStatus.CANCELLED);
        when(orderRepository.findByIdWithItems(orderId)).thenReturn(Optional.of(testOrder));

        // Act & Assert
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            orderService.cancelOrder(orderId);
        });

        assertTrue(exception.getMessage().contains("Cannot cancel order"));
        verify(orderRepository, never()).save(any());
    }

    /**
     * Test order number generation
     * Verifies that order numbers are unique and properly formatted
     */
    @Test
    @DisplayName("Should generate unique order number")
    void testOrderNumberGeneration() {
        // Arrange
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(cartService.getCartByUserId(userId)).thenReturn(cartDTO);
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> {
            Order order = invocation.getArgument(0);
            order.setId(orderId);
            return order;
        });
        doNothing().when(productService).reduceStock(anyLong(), anyInt());
        doNothing().when(cartService).clearCart(userId);

        // Act
        OrderDTO result = orderService.createOrder(userId, createOrderRequest);

        // Assert
        assertNotNull(result.getOrderNumber());
        assertTrue(result.getOrderNumber().startsWith("ORD-"));
        assertTrue(result.getOrderNumber().length() > 10);
    }

    /**
     * Test order DTO mapping
     * Verifies that Order entity is correctly mapped to OrderDTO
     */
    @Test
    @DisplayName("Should correctly map Order entity to OrderDTO")
    void testOrderDTOMapping() {
        // Arrange
        when(orderRepository.findByIdWithItems(orderId)).thenReturn(Optional.of(testOrder));

        // Act
        OrderDTO result = orderService.getOrderById(orderId);

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
        assertEquals(testOrder.getItems().size(), result.getItems().size());
    }
}