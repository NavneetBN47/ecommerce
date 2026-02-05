package com.ecommerce.service;

import com.ecommerce.dto.OrderDTO;
import com.ecommerce.entity.*;
import com.ecommerce.exception.InsufficientStockException;
import com.ecommerce.exception.ResourceNotFoundException;
import com.ecommerce.mapper.OrderMapper;
import com.ecommerce.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service class for Order operations
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final UserRepository userRepository;
    private final CartRepository cartRepository;
    private final ProductRepository productRepository;
    private final AddressRepository addressRepository;
    private final OrderMapper orderMapper;

    /**
     * Create order from cart
     */
    public OrderDTO createOrderFromCart(Long userId, Long shippingAddressId) {
        log.info("Creating order from cart for user ID: {}", userId);

        // Get user
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));

        // Get cart
        Cart cart = cartRepository.findByUserId(userId)
            .orElseThrow(() -> new ResourceNotFoundException("Cart not found for user ID: " + userId));

        if (cart.isEmpty()) {
            throw new IllegalStateException("Cannot create order from empty cart");
        }

        // Get shipping address
        Address shippingAddress = addressRepository.findById(shippingAddressId)
            .orElseThrow(() -> new ResourceNotFoundException("Address not found with ID: " + shippingAddressId));

        // Create order
        Order order = Order.builder()
            .orderNumber(generateOrderNumber())
            .user(user)
            .shippingAddress(shippingAddress)
            .status(Order.OrderStatus.PENDING)
            .orderDate(LocalDateTime.now())
            .build();

        // Create order items from cart items and reduce stock
        for (CartItem cartItem : cart.getItems()) {
            Product product = cartItem.getProduct();

            // Check stock availability
            if (!product.hasStock(cartItem.getQuantity())) {
                throw new InsufficientStockException("Insufficient stock for product: " + product.getName());
            }

            // Reduce stock
            product.reduceStock(cartItem.getQuantity());
            productRepository.save(product);

            // Create order item
            OrderItem orderItem = OrderItem.builder()
                .order(order)
                .product(product)
                .quantity(cartItem.getQuantity())
                .price(cartItem.getPrice())
                .build();

            order.addItem(orderItem);
        }

        // Calculate totals
        order.calculateTotalAmount();

        // Save order
        Order savedOrder = orderRepository.save(order);

        // Clear cart after order creation
        cartRepository.delete(cart);
        log.info("Order created successfully: {}", savedOrder.getOrderNumber());

        return orderMapper.toDTO(savedOrder);
    }

    /**
     * Get order by ID
     */
    @Transactional(readOnly = true)
    public OrderDTO getOrderById(Long id) {
        log.debug("Fetching order by ID: {}", id);
        Order order = orderRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Order not found with ID: " + id));
        return orderMapper.toDTO(order);
    }

    /**
     * Get order by order number
     */
    @Transactional(readOnly = true)
    public OrderDTO getOrderByOrderNumber(String orderNumber) {
        log.debug("Fetching order by order number: {}", orderNumber);
        Order order = orderRepository.findByOrderNumber(orderNumber)
            .orElseThrow(() -> new ResourceNotFoundException("Order not found with order number: " + orderNumber));
        return orderMapper.toDTO(order);
    }

    /**
     * Get all orders for a user
     */
    @Transactional(readOnly = true)
    public List<OrderDTO> getOrdersByUserId(Long userId) {
        log.debug("Fetching orders for user ID: {}", userId);
        return orderRepository.findByUserId(userId).stream()
            .map(orderMapper::toDTO)
            .collect(Collectors.toList());
    }

    /**
     * Get orders for a user with pagination
     */
    @Transactional(readOnly = true)
    public Page<OrderDTO> getOrdersByUserId(Long userId, Pageable pageable) {
        log.debug("Fetching orders for user ID with pagination: {}", userId);
        return orderRepository.findByUserId(userId, pageable)
            .map(orderMapper::toDTO);
    }

    /**
     * Update order status
     */
    public OrderDTO updateOrderStatus(Long orderId, Order.OrderStatus status) {
        log.info("Updating order status for order ID: {} to {}", orderId, status);

        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new ResourceNotFoundException("Order not found with ID: " + orderId));

        order.setStatus(status);
        Order updatedOrder = orderRepository.save(order);
        log.info("Order status updated successfully: {}", updatedOrder.getOrderNumber());

        return orderMapper.toDTO(updatedOrder);
    }

    /**
     * Cancel order
     */
    public OrderDTO cancelOrder(Long orderId) {
        log.info("Cancelling order with ID: {}", orderId);

        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new ResourceNotFoundException("Order not found with ID: " + orderId));

        // Only allow cancellation for pending or confirmed orders
        if (order.getStatus() != Order.OrderStatus.PENDING && 
            order.getStatus() != Order.OrderStatus.CONFIRMED) {
            throw new IllegalStateException("Cannot cancel order with status: " + order.getStatus());
        }

        // Restore stock for cancelled order
        for (OrderItem orderItem : order.getItems()) {
            Product product = orderItem.getProduct();
            product.increaseStock(orderItem.getQuantity());
            productRepository.save(product);
        }

        order.setStatus(Order.OrderStatus.CANCELLED);
        Order cancelledOrder = orderRepository.save(order);
        log.info("Order cancelled successfully: {}", cancelledOrder.getOrderNumber());

        return orderMapper.toDTO(cancelledOrder);
    }

    /**
     * Generate unique order number
     */
    private String generateOrderNumber() {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String random = String.format("%04d", (int) (Math.random() * 10000));
        return "ORD-" + timestamp + "-" + random;
    }
}