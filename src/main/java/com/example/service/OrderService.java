package com.example.service;

import com.example.dto.CreateOrderRequest;
import com.example.dto.OrderDTO;
import com.example.dto.OrderItemDTO;
import com.example.entity.*;
import com.example.exception.BusinessException;
import com.example.exception.ResourceNotFoundException;
import com.example.repository.*;
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
 * Service class for Order entity operations.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class OrderService {
    
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final CartRepository cartRepository;
    private final ProductService productService;
    
    /**
     * Create order from cart.
     */
    public OrderDTO createOrder(Long userId, CreateOrderRequest request) {
        log.info("Creating order for user: {}", userId);
        
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));
        
        Cart cart = cartRepository.findByUserIdWithItems(userId)
            .orElseThrow(() -> new BusinessException("Cart is empty. Cannot create order."));
        
        if (cart.isEmpty()) {
            throw new BusinessException("Cart is empty. Cannot create order.");
        }
        
        // Create order
        Order order = Order.builder()
            .orderNumber(generateOrderNumber())
            .user(user)
            .status(Order.OrderStatus.PENDING)
            .shippingAddress(request.getShippingAddress())
            .billingAddress(request.getBillingAddress() != null ? 
                request.getBillingAddress() : request.getShippingAddress())
            .paymentMethod(request.getPaymentMethod())
            .paymentStatus(Order.PaymentStatus.PENDING)
            .orderDate(LocalDateTime.now())
            .notes(request.getNotes())
            .build();
        
        // Convert cart items to order items and reserve stock
        for (CartItem cartItem : cart.getItems()) {
            // Reserve stock
            productService.reserveStock(cartItem.getProduct().getId(), cartItem.getQuantity());
            
            // Create order item
            OrderItem orderItem = OrderItem.builder()
                .order(order)
                .product(cartItem.getProduct())
                .productName(cartItem.getProduct().getName())
                .productSku(cartItem.getProduct().getSku())
                .quantity(cartItem.getQuantity())
                .unitPrice(cartItem.getUnitPrice())
                .build();
            orderItem.calculateSubtotal();
            order.addItem(orderItem);
        }
        
        // Calculate totals
        order.calculateTotals();
        
        // Save order
        order = orderRepository.save(order);
        
        // Clear cart after successful order creation
        cartRepository.delete(cart);
        
        log.info("Order created successfully: {}", order.getOrderNumber());
        return convertToDTO(order);
    }
    
    /**
     * Get order by ID.
     */
    @Transactional(readOnly = true)
    public OrderDTO getOrderById(Long id) {
        log.debug("Fetching order by ID: {}", id);
        Order order = orderRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Order not found with ID: " + id));
        return convertToDTO(order);
    }
    
    /**
     * Get order by order number.
     */
    @Transactional(readOnly = true)
    public OrderDTO getOrderByOrderNumber(String orderNumber) {
        log.debug("Fetching order by order number: {}", orderNumber);
        Order order = orderRepository.findByOrderNumber(orderNumber)
            .orElseThrow(() -> new ResourceNotFoundException("Order not found with order number: " + orderNumber));
        return convertToDTO(order);
    }
    
    /**
     * Get orders by user.
     */
    @Transactional(readOnly = true)
    public Page<OrderDTO> getOrdersByUserId(Long userId, Pageable pageable) {
        log.debug("Fetching orders for user: {}", userId);
        return orderRepository.findByUserId(userId, pageable)
            .map(this::convertToDTO);
    }
    
    /**
     * Get all orders.
     */
    @Transactional(readOnly = true)
    public Page<OrderDTO> getAllOrders(Pageable pageable) {
        log.debug("Fetching all orders");
        return orderRepository.findAll(pageable)
            .map(this::convertToDTO);
    }
    
    /**
     * Update order status.
     */
    public OrderDTO updateOrderStatus(Long id, Order.OrderStatus status) {
        log.info("Updating order status: {} to {}", id, status);
        
        Order order = orderRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Order not found with ID: " + id));
        
        order.setStatus(status);
        
        // Update timestamps based on status
        if (status == Order.OrderStatus.SHIPPED && order.getShippedDate() == null) {
            order.setShippedDate(LocalDateTime.now());
        } else if (status == Order.OrderStatus.DELIVERED && order.getDeliveredDate() == null) {
            order.setDeliveredDate(LocalDateTime.now());
        }
        
        order = orderRepository.save(order);
        log.info("Order status updated successfully: {}", id);
        
        return convertToDTO(order);
    }
    
    /**
     * Cancel order.
     */
    public OrderDTO cancelOrder(Long id) {
        log.info("Cancelling order: {}", id);
        
        Order order = orderRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Order not found with ID: " + id));
        
        // Only allow cancellation for pending or confirmed orders
        if (order.getStatus() != Order.OrderStatus.PENDING && 
            order.getStatus() != Order.OrderStatus.CONFIRMED) {
            throw new BusinessException("Cannot cancel order with status: " + order.getStatus());
        }
        
        order.setStatus(Order.OrderStatus.CANCELLED);
        order = orderRepository.save(order);
        
        // TODO: Restore stock quantities
        
        log.info("Order cancelled successfully: {}", id);
        return convertToDTO(order);
    }
    
    /**
     * Generate unique order number.
     */
    private String generateOrderNumber() {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String random = String.format("%04d", (int)(Math.random() * 10000));
        return "ORD-" + timestamp + "-" + random;
    }
    
    /**
     * Convert Order entity to DTO.
     */
    private OrderDTO convertToDTO(Order order) {
        return OrderDTO.builder()
            .id(order.getId())
            .orderNumber(order.getOrderNumber())
            .userId(order.getUser().getId())
            .items(order.getItems().stream()
                .map(this::convertItemToDTO)
                .collect(Collectors.toList()))
            .status(order.getStatus())
            .totalAmount(order.getTotalAmount())
            .totalItems(order.getTotalItems())
            .shippingAddress(order.getShippingAddress())
            .billingAddress(order.getBillingAddress())
            .paymentMethod(order.getPaymentMethod())
            .paymentStatus(order.getPaymentStatus())
            .orderDate(order.getOrderDate())
            .shippedDate(order.getShippedDate())
            .deliveredDate(order.getDeliveredDate())
            .notes(order.getNotes())
            .createdAt(order.getCreatedAt())
            .updatedAt(order.getUpdatedAt())
            .build();
    }
    
    /**
     * Convert OrderItem entity to DTO.
     */
    private OrderItemDTO convertItemToDTO(OrderItem item) {
        return OrderItemDTO.builder()
            .id(item.getId())
            .productId(item.getProduct().getId())
            .productName(item.getProductName())
            .productSku(item.getProductSku())
            .quantity(item.getQuantity())
            .unitPrice(item.getUnitPrice())
            .subtotal(item.getSubtotal())
            .createdAt(item.getCreatedAt())
            .build();
    }
}