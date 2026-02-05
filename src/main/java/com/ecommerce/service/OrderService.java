package com.ecommerce.service;

import com.ecommerce.dto.request.CheckoutRequest;
import com.ecommerce.dto.response.OrderItemResponse;
import com.ecommerce.dto.response.OrderResponse;
import com.ecommerce.entity.*;
import com.ecommerce.exception.BusinessException;
import com.ecommerce.exception.InsufficientStockException;
import com.ecommerce.exception.ResourceNotFoundException;
import com.ecommerce.repository.AddressRepository;
import com.ecommerce.repository.OrderRepository;
import com.ecommerce.repository.ProductRepository;
import com.ecommerce.repository.ShoppingCartRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service for order management operations
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {

    private final OrderRepository orderRepository;
    private final ShoppingCartRepository cartRepository;
    private final AddressRepository addressRepository;
    private final ProductRepository productRepository;
    private final UserService userService;

    @Value("${app.order.tax-rate:0.08}")
    private double taxRate;

    @Value("${app.order.default-shipping-cost:5.99}")
    private double defaultShippingCost;

    @Value("${app.order.free-shipping-threshold:50.00}")
    private double freeShippingThreshold;

    @Transactional
    public OrderResponse checkout(CheckoutRequest request) {
        User currentUser = userService.getCurrentUser();
        log.info("Processing checkout for user {}", currentUser.getId());

        // Get active cart with items
        ShoppingCart cart = cartRepository.findActiveCartByUserIdWithItems(currentUser.getId())
            .orElseThrow(() -> new BusinessException("No active cart found"));

        if (cart.isEmpty()) {
            throw new BusinessException("Cannot checkout with empty cart");
        }

        // Validate all items are in stock
        for (CartItem item : cart.getItems()) {
            Product product = item.getProduct();
            if (!product.hasStock(item.getQuantity())) {
                throw new InsufficientStockException(
                    product.getName(), item.getQuantity(), product.getStockQuantity());
            }
        }

        // Get shipping address
        Address address = addressRepository.findById(request.getAddressId())
            .orElseThrow(() -> new ResourceNotFoundException("Address", "id", request.getAddressId()));

        if (!address.getUser().getId().equals(currentUser.getId())) {
            throw new BusinessException("Address does not belong to current user");
        }

        // Calculate totals
        BigDecimal subtotal = cart.calculateTotal();
        BigDecimal tax = subtotal.multiply(BigDecimal.valueOf(taxRate));
        BigDecimal shippingCost = subtotal.compareTo(BigDecimal.valueOf(freeShippingThreshold)) >= 0
            ? BigDecimal.ZERO
            : BigDecimal.valueOf(defaultShippingCost);
        BigDecimal total = subtotal.add(tax).add(shippingCost);

        // Create order
        Order order = Order.builder()
            .user(currentUser)
            .orderNumber(generateOrderNumber())
            .status(Order.OrderStatus.PENDING)
            .subtotal(subtotal)
            .tax(tax)
            .shippingCost(shippingCost)
            .total(total)
            .shippingAddress(address.getFullAddress())
            .paymentMethod(request.getPaymentMethod())
            .paymentStatus(Order.PaymentStatus.PENDING)
            .build();

        // Create order items and update product stock
        for (CartItem cartItem : cart.getItems()) {
            Product product = cartItem.getProduct();
            
            OrderItem orderItem = OrderItem.builder()
                .order(order)
                .product(product)
                .quantity(cartItem.getQuantity())
                .price(cartItem.getPrice())
                .subtotal(cartItem.getSubtotal())
                .build();
            
            order.addItem(orderItem);

            // Update product stock
            product.setStockQuantity(product.getStockQuantity() - cartItem.getQuantity());
            productRepository.save(product);
        }

        order = orderRepository.save(order);

        // Clear cart and mark as checked out
        cart.clearItems();
        cart.setStatus(ShoppingCart.CartStatus.CHECKED_OUT);
        cartRepository.save(cart);

        log.info("Order created successfully: {}", order.getOrderNumber());
        return mapToOrderResponse(order);
    }

    @Transactional(readOnly = true)
    public Page<OrderResponse> getUserOrders(int page, int size) {
        User currentUser = userService.getCurrentUser();
        log.info("Fetching orders for user {}", currentUser.getId());

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return orderRepository.findByUserId(currentUser.getId(), pageable)
            .map(this::mapToOrderResponse);
    }

    @Transactional(readOnly = true)
    public OrderResponse getOrderById(Long orderId) {
        User currentUser = userService.getCurrentUser();
        log.info("Fetching order {} for user {}", orderId, currentUser.getId());

        Order order = orderRepository.findByIdWithItems(orderId)
            .orElseThrow(() -> new ResourceNotFoundException("Order", "id", orderId));

        if (!order.getUser().getId().equals(currentUser.getId())) {
            throw new BusinessException("Order does not belong to current user");
        }

        return mapToOrderResponse(order);
    }

    @Transactional
    public OrderResponse cancelOrder(Long orderId) {
        User currentUser = userService.getCurrentUser();
        log.info("Cancelling order {} for user {}", orderId, currentUser.getId());

        Order order = orderRepository.findByIdWithItems(orderId)
            .orElseThrow(() -> new ResourceNotFoundException("Order", "id", orderId));

        if (!order.getUser().getId().equals(currentUser.getId())) {
            throw new BusinessException("Order does not belong to current user");
        }

        if (!order.canBeCancelled()) {
            throw new BusinessException(
                "Order cannot be cancelled. Current status: " + order.getStatus());
        }

        // Restore product stock
        for (OrderItem item : order.getItems()) {
            Product product = item.getProduct();
            product.setStockQuantity(product.getStockQuantity() + item.getQuantity());
            productRepository.save(product);
        }

        order.setStatus(Order.OrderStatus.CANCELLED);
        order = orderRepository.save(order);

        log.info("Order cancelled successfully: {}", order.getOrderNumber());
        return mapToOrderResponse(order);
    }

    private String generateOrderNumber() {
        return "ORD-" + LocalDateTime.now().toString().replaceAll("[^0-9]", "").substring(0, 14)
            + "-" + UUID.randomUUID().toString().substring(0, 4).toUpperCase();
    }

    private OrderResponse mapToOrderResponse(Order order) {
        List<OrderItemResponse> items = order.getItems().stream()
            .map(this::mapToOrderItemResponse)
            .collect(Collectors.toList());

        return OrderResponse.builder()
            .id(order.getId())
            .orderNumber(order.getOrderNumber())
            .userId(order.getUser().getId())
            .status(order.getStatus().name())
            .subtotal(order.getSubtotal())
            .tax(order.getTax())
            .shippingCost(order.getShippingCost())
            .total(order.getTotal())
            .shippingAddress(order.getShippingAddress())
            .paymentMethod(order.getPaymentMethod())
            .paymentStatus(order.getPaymentStatus().name())
            .items(items)
            .createdAt(order.getCreatedAt())
            .updatedAt(order.getUpdatedAt())
            .build();
    }

    private OrderItemResponse mapToOrderItemResponse(OrderItem item) {
        return OrderItemResponse.builder()
            .id(item.getId())
            .productId(item.getProduct().getId())
            .productName(item.getProduct().getName())
            .productSku(item.getProduct().getSku())
            .quantity(item.getQuantity())
            .price(item.getPrice())
            .subtotal(item.getSubtotal())
            .build();
    }
}