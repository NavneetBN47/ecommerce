package com.ecommerce.service;

import com.ecommerce.dto.OrderDTO;
import com.ecommerce.dto.OrderItemDTO;
import com.ecommerce.entity.*;
import com.ecommerce.exception.InsufficientStockException;
import com.ecommerce.exception.ResourceNotFoundException;
import com.ecommerce.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Order Service - Handles order-related business logic
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final CartRepository cartRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;

    /**
     * Create order from cart
     */
    public OrderDTO createOrderFromCart(Long userId, OrderDTO orderDTO) {
        log.info("Creating order from cart for user ID: {}", userId);

        // Validate user
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));

        // Get cart
        Cart cart = cartRepository.findByUserId(userId)
            .orElseThrow(() -> new ResourceNotFoundException("Cart not found for user ID: " + userId));

        if (cart.isEmpty()) {
            throw new IllegalStateException("Cannot create order from empty cart");
        }

        // Create order
        Order order = Order.builder()
            .user(user)
            .totalAmount(cart.getTotalAmount())
            .totalItems(cart.getTotalItems())
            .status(Order.OrderStatus.PENDING)
            .shippingAddress(orderDTO.getShippingAddress())
            .billingAddress(orderDTO.getBillingAddress())
            .paymentMethod(orderDTO.getPaymentMethod())
            .notes(orderDTO.getNotes())
            .build();

        Order savedOrder = orderRepository.save(order);

        // Create order items from cart items
        for (CartItem cartItem : cart.getItems()) {
            Product product = cartItem.getProduct();

            // Check stock availability
            if (!product.hasSufficientStock(cartItem.getQuantity())) {
                throw new InsufficientStockException("Insufficient stock for product: " + product.getName());
            }

            // Reduce stock
            product.reduceStock(cartItem.getQuantity());
            productRepository.save(product);

            // Create order item
            OrderItem orderItem = OrderItem.builder()
                .order(savedOrder)
                .product(product)
                .quantity(cartItem.getQuantity())
                .price(cartItem.getPrice())
                .build();

            savedOrder.addItem(orderItem);
            orderItemRepository.save(orderItem);
        }

        savedOrder.recalculateTotals();
        Order finalOrder = orderRepository.save(savedOrder);

        // Clear cart after order creation
        cartRepository.delete(cart);

        log.info("Order created successfully with order number: {}", finalOrder.getOrderNumber());
        return convertToDTO(finalOrder);
    }

    /**
     * Get order by ID
     */
    @Transactional(readOnly = true)
    public OrderDTO getOrderById(Long orderId) {
        log.info("Fetching order by ID: {}", orderId);
        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new ResourceNotFoundException("Order not found with ID: " + orderId));
        return convertToDTO(order);
    }

    /**
     * Get order by order number
     */
    @Transactional(readOnly = true)
    public OrderDTO getOrderByOrderNumber(String orderNumber) {
        log.info("Fetching order by order number: {}", orderNumber);
        Order order = orderRepository.findByOrderNumber(orderNumber)
            .orElseThrow(() -> new ResourceNotFoundException("Order not found with order number: " + orderNumber));
        return convertToDTO(order);
    }

    /**
     * Get user orders
     */
    @Transactional(readOnly = true)
    public List<OrderDTO> getUserOrders(Long userId) {
        log.info("Fetching orders for user ID: {}", userId);
        return orderRepository.findByUserId(userId).stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList());
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
        log.info("Order status updated successfully");

        return convertToDTO(updatedOrder);
    }

    /**
     * Cancel order
     */
    public OrderDTO cancelOrder(Long orderId) {
        log.info("Cancelling order ID: {}", orderId);

        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new ResourceNotFoundException("Order not found with ID: " + orderId));

        if (order.getStatus() == Order.OrderStatus.SHIPPED || 
            order.getStatus() == Order.OrderStatus.DELIVERED) {
            throw new IllegalStateException("Cannot cancel order in status: " + order.getStatus());
        }

        // Restore stock
        for (OrderItem item : order.getItems()) {
            Product product = item.getProduct();
            product.increaseStock(item.getQuantity());
            productRepository.save(product);
        }

        order.setStatus(Order.OrderStatus.CANCELLED);
        Order cancelledOrder = orderRepository.save(order);
        log.info("Order cancelled successfully");

        return convertToDTO(cancelledOrder);
    }

    /**
     * Convert Order entity to DTO
     */
    private OrderDTO convertToDTO(Order order) {
        return OrderDTO.builder()
            .id(order.getId())
            .orderNumber(order.getOrderNumber())
            .userId(order.getUser().getId())
            .items(order.getItems().stream()
                .map(this::convertItemToDTO)
                .collect(Collectors.toList()))
            .totalAmount(order.getTotalAmount())
            .totalItems(order.getTotalItems())
            .status(order.getStatus())
            .shippingAddress(order.getShippingAddress())
            .billingAddress(order.getBillingAddress())
            .paymentMethod(order.getPaymentMethod())
            .paymentStatus(order.getPaymentStatus())
            .notes(order.getNotes())
            .createdAt(order.getCreatedAt())
            .updatedAt(order.getUpdatedAt())
            .build();
    }

    /**
     * Convert OrderItem entity to DTO
     */
    private OrderItemDTO convertItemToDTO(OrderItem item) {
        return OrderItemDTO.builder()
            .id(item.getId())
            .orderId(item.getOrder().getId())
            .productId(item.getProduct().getId())
            .productName(item.getProductName())
            .productSku(item.getProductSku())
            .quantity(item.getQuantity())
            .price(item.getPrice())
            .subtotal(item.getSubtotal())
            .createdAt(item.getCreatedAt())
            .build();
    }
}