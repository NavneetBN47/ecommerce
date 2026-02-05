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

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Order Service with checkout and order management
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final CartRepository cartRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final AddressRepository addressRepository;

    @Transactional(readOnly = true)
    public List<OrderDTO> getAllOrders() {
        log.info("Fetching all orders");
        return orderRepository.findAll().stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<OrderDTO> getUserOrders(Long userId) {
        log.info("Fetching orders for user: {}", userId);
        return orderRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public OrderDTO getOrderById(Long orderId) {
        log.info("Fetching order by id: {}", orderId);
        Order order = orderRepository.findByIdWithItems(orderId)
            .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + orderId));
        return convertToDTO(order);
    }

    /**
     * Checkout cart and create order
     */
    @Transactional
    public OrderDTO checkoutCart(Long userId, Long shippingAddressId, String paymentMethod) {
        log.info("Checking out cart for user: {}", userId);
        
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        Cart cart = cartRepository.findActiveCartByUserId(userId)
            .orElseThrow(() -> new ResourceNotFoundException("No active cart found for user"));

        if (cart.isEmpty()) {
            throw new IllegalStateException("Cannot checkout empty cart");
        }

        Address shippingAddress = null;
        if (shippingAddressId != null) {
            shippingAddress = addressRepository.findById(shippingAddressId)
                .orElseThrow(() -> new ResourceNotFoundException("Address not found"));
        }

        // Create order
        Order order = Order.builder()
            .orderNumber(generateOrderNumber())
            .user(user)
            .shippingAddress(shippingAddress)
            .status(Order.OrderStatus.PENDING)
            .paymentMethod(paymentMethod)
            .paymentStatus("PENDING")
            .build();

        // Convert cart items to order items and reduce stock
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
        order.calculateTotals();
        Order savedOrder = orderRepository.save(order);

        // Mark cart as checked out and delete
        cart.setStatus(Cart.CartStatus.CHECKED_OUT);
        cartRepository.delete(cart);

        log.info("Order created successfully: {}", savedOrder.getOrderNumber());
        return convertToDTO(savedOrder);
    }

    @Transactional
    public OrderDTO updateOrderStatus(Long orderId, Order.OrderStatus status) {
        log.info("Updating order status: {}, new status: {}", orderId, status);
        
        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + orderId));

        order.setStatus(status);
        Order updatedOrder = orderRepository.save(order);
        
        log.info("Order status updated successfully");
        return convertToDTO(updatedOrder);
    }

    @Transactional
    public OrderDTO cancelOrder(Long orderId) {
        log.info("Cancelling order: {}", orderId);
        
        Order order = orderRepository.findByIdWithItems(orderId)
            .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + orderId));

        if (order.getStatus() == Order.OrderStatus.DELIVERED || 
            order.getStatus() == Order.OrderStatus.CANCELLED) {
            throw new IllegalStateException("Cannot cancel order in current status: " + order.getStatus());
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

    private String generateOrderNumber() {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        return "ORD-" + timestamp;
    }

    private OrderDTO convertToDTO(Order order) {
        List<OrderItemDTO> itemDTOs = order.getItems().stream()
            .map(this::convertItemToDTO)
            .collect(Collectors.toList());

        return OrderDTO.builder()
            .id(order.getId())
            .orderNumber(order.getOrderNumber())
            .userId(order.getUser().getId())
            .items(itemDTOs)
            .shippingAddressId(order.getShippingAddress() != null ? order.getShippingAddress().getId() : null)
            .status(order.getStatus())
            .totalAmount(order.getTotalAmount())
            .totalItems(order.getTotalItems())
            .paymentMethod(order.getPaymentMethod())
            .paymentStatus(order.getPaymentStatus())
            .notes(order.getNotes())
            .createdAt(order.getCreatedAt())
            .updatedAt(order.getUpdatedAt())
            .build();
    }

    private OrderItemDTO convertItemToDTO(OrderItem item) {
        return OrderItemDTO.builder()
            .id(item.getId())
            .orderId(item.getOrder().getId())
            .productId(item.getProduct().getId())
            .productName(item.getProduct().getName())
            .productSku(item.getProduct().getSku())
            .quantity(item.getQuantity())
            .price(item.getPrice())
            .subtotal(item.getSubtotal())
            .createdAt(item.getCreatedAt())
            .build();
    }
}