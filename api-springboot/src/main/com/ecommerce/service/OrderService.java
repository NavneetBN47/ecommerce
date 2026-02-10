package com.ecommerce.service;

import com.ecommerce.dto.CreateOrderRequestDTO;
import com.ecommerce.dto.OrderDTO;
import com.ecommerce.dto.OrderItemDTO;
import com.ecommerce.dto.ProductDTO;
import com.ecommerce.entity.*;
import com.ecommerce.exception.BusinessException;
import com.ecommerce.exception.ResourceNotFoundException;
import com.ecommerce.repository.OrderRepository;
import com.ecommerce.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.stream.Collectors;

/**
 * Service class for Order entity operations
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class OrderService {

    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final CartService cartService;
    private final ProductService productService;

    /**
     * Create order from cart
     */
    public OrderDTO createOrder(Long userId, CreateOrderRequestDTO request) {
        log.info("Creating order for user: {}", userId);

        // Get user
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));

        // Get cart
        var cartDTO = cartService.getCartByUserId(userId);
        if (cartDTO.getItems() == null || cartDTO.getItems().isEmpty()) {
            throw new BusinessException("Cannot create order from empty cart");
        }

        // Create order
        Order order = Order.builder()
            .orderNumber(generateOrderNumber())
            .user(user)
            .totalAmount(cartDTO.getTotalAmount())
            .status(Order.OrderStatus.PENDING)
            .shippingAddress(request.getShippingAddress())
            .billingAddress(request.getBillingAddress() != null ? request.getBillingAddress() : request.getShippingAddress())
            .paymentMethod(request.getPaymentMethod())
            .paymentStatus("PENDING")
            .build();

        // Create order items from cart items
        for (var cartItemDTO : cartDTO.getItems()) {
            // Reduce product stock
            productService.reduceStock(cartItemDTO.getProductId(), cartItemDTO.getQuantity());

            OrderItem orderItem = OrderItem.builder()
                .order(order)
                .product(Product.builder().id(cartItemDTO.getProductId()).build())
                .quantity(cartItemDTO.getQuantity())
                .price(cartItemDTO.getPrice())
                .build();
            orderItem.calculateSubtotal();
            order.getItems().add(orderItem);
        }

        order = orderRepository.save(order);

        // Clear cart after order creation
        cartService.clearCart(userId);

        log.info("Order created successfully: {}", order.getOrderNumber());
        return mapToDTO(order);
    }

    /**
     * Get order by ID
     */
    @Transactional(readOnly = true)
    public OrderDTO getOrderById(Long orderId) {
        log.debug("Fetching order by ID: {}", orderId);
        Order order = orderRepository.findByIdWithItems(orderId)
            .orElseThrow(() -> new ResourceNotFoundException("Order not found with ID: " + orderId));
        return mapToDTO(order);
    }

    /**
     * Get order by order number
     */
    @Transactional(readOnly = true)
    public OrderDTO getOrderByOrderNumber(String orderNumber) {
        log.debug("Fetching order by order number: {}", orderNumber);
        Order order = orderRepository.findByOrderNumber(orderNumber)
            .orElseThrow(() -> new ResourceNotFoundException("Order not found with order number: " + orderNumber));
        return mapToDTO(order);
    }

    /**
     * Get all orders for a user
     */
    @Transactional(readOnly = true)
    public Page<OrderDTO> getUserOrders(Long userId, Pageable pageable) {
        log.debug("Fetching orders for user: {}", userId);
        return orderRepository.findByUserId(userId, pageable)
            .map(this::mapToDTO);
    }

    /**
     * Update order status
     */
    public OrderDTO updateOrderStatus(Long orderId, Order.OrderStatus status) {
        log.info("Updating order: {} status to: {}", orderId, status);
        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new ResourceNotFoundException("Order not found with ID: " + orderId));
        
        order.setStatus(status);
        order = orderRepository.save(order);
        
        log.info("Order status updated successfully: {}", order.getOrderNumber());
        return mapToDTO(order);
    }

    /**
     * Cancel order
     */
    public OrderDTO cancelOrder(Long orderId) {
        log.info("Cancelling order: {}", orderId);
        Order order = orderRepository.findByIdWithItems(orderId)
            .orElseThrow(() -> new ResourceNotFoundException("Order not found with ID: " + orderId));

        if (order.getStatus() == Order.OrderStatus.DELIVERED || 
            order.getStatus() == Order.OrderStatus.CANCELLED) {
            throw new BusinessException("Cannot cancel order with status: " + order.getStatus());
        }

        order.setStatus(Order.OrderStatus.CANCELLED);
        order = orderRepository.save(order);

        log.info("Order cancelled successfully: {}", order.getOrderNumber());
        return mapToDTO(order);
    }

    /**
     * Generate unique order number
     */
    private String generateOrderNumber() {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String random = String.format("%04d", (int) (Math.random() * 10000));
        return "ORD-" + timestamp + "-" + random;
    }

    /**
     * Map Order entity to OrderDTO
     */
    private OrderDTO mapToDTO(Order order) {
        return OrderDTO.builder()
            .id(order.getId())
            .orderNumber(order.getOrderNumber())
            .userId(order.getUser().getId())
            .items(order.getItems().stream()
                .map(this::mapOrderItemToDTO)
                .collect(Collectors.toList()))
            .totalAmount(order.getTotalAmount())
            .status(order.getStatus())
            .shippingAddress(order.getShippingAddress())
            .billingAddress(order.getBillingAddress())
            .paymentMethod(order.getPaymentMethod())
            .paymentStatus(order.getPaymentStatus())
            .createdAt(order.getCreatedAt())
            .updatedAt(order.getUpdatedAt())
            .build();
    }

    /**
     * Map OrderItem entity to OrderItemDTO
     */
    private OrderItemDTO mapOrderItemToDTO(OrderItem orderItem) {
        return OrderItemDTO.builder()
            .id(orderItem.getId())
            .productId(orderItem.getProduct().getId())
            .product(mapProductToDTO(orderItem.getProduct()))
            .quantity(orderItem.getQuantity())
            .price(orderItem.getPrice())
            .subtotal(orderItem.getSubtotal())
            .build();
    }

    /**
     * Map Product entity to ProductDTO
     */
    private ProductDTO mapProductToDTO(Product product) {
        return ProductDTO.builder()
            .id(product.getId())
            .name(product.getName())
            .sku(product.getSku())
            .price(product.getPrice())
            .imageUrl(product.getImageUrl())
            .build();
    }
}