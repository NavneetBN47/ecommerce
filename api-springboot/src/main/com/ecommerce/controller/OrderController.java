package com.ecommerce.controller;

import com.ecommerce.dto.ApiResponseDTO;
import com.ecommerce.dto.CreateOrderRequestDTO;
import com.ecommerce.dto.OrderDTO;
import com.ecommerce.entity.Order;
import com.ecommerce.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

/**
 * REST Controller for order operations
 */
@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
@Slf4j
public class OrderController {

    private final OrderService orderService;

    /**
     * Create order from cart
     */
    @PostMapping
    public ResponseEntity<ApiResponseDTO<OrderDTO>> createOrder(
            @Valid @RequestBody CreateOrderRequestDTO request,
            Authentication authentication) {
        Long userId = Long.parseLong(authentication.getName());
        log.info("Create order request for user: {}", userId);
        OrderDTO order = orderService.createOrder(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponseDTO.success("Order created successfully", order));
    }

    /**
     * Get order by ID
     */
    @GetMapping("/{orderId}")
    public ResponseEntity<ApiResponseDTO<OrderDTO>> getOrderById(@PathVariable Long orderId) {
        log.info("Get order request for ID: {}", orderId);
        OrderDTO order = orderService.getOrderById(orderId);
        return ResponseEntity.ok(ApiResponseDTO.success(order));
    }

    /**
     * Get order by order number
     */
    @GetMapping("/number/{orderNumber}")
    public ResponseEntity<ApiResponseDTO<OrderDTO>> getOrderByOrderNumber(@PathVariable String orderNumber) {
        log.info("Get order request for order number: {}", orderNumber);
        OrderDTO order = orderService.getOrderByOrderNumber(orderNumber);
        return ResponseEntity.ok(ApiResponseDTO.success(order));
    }

    /**
     * Get all orders for current user
     */
    @GetMapping
    public ResponseEntity<ApiResponseDTO<Page<OrderDTO>>> getUserOrders(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Authentication authentication) {
        Long userId = Long.parseLong(authentication.getName());
        log.info("Get orders request for user: {}", userId);
        Pageable pageable = PageRequest.of(page, size);
        Page<OrderDTO> orders = orderService.getUserOrders(userId, pageable);
        return ResponseEntity.ok(ApiResponseDTO.success(orders));
    }

    /**
     * Update order status (admin only)
     */
    @PatchMapping("/{orderId}/status")
    public ResponseEntity<ApiResponseDTO<OrderDTO>> updateOrderStatus(
            @PathVariable Long orderId,
            @RequestParam Order.OrderStatus status) {
        log.info("Update order status request for order: {}, status: {}", orderId, status);
        OrderDTO order = orderService.updateOrderStatus(orderId, status);
        return ResponseEntity.ok(ApiResponseDTO.success("Order status updated successfully", order));
    }

    /**
     * Cancel order
     */
    @PostMapping("/{orderId}/cancel")
    public ResponseEntity<ApiResponseDTO<OrderDTO>> cancelOrder(@PathVariable Long orderId) {
        log.info("Cancel order request for order: {}", orderId);
        OrderDTO order = orderService.cancelOrder(orderId);
        return ResponseEntity.ok(ApiResponseDTO.success("Order cancelled successfully", order));
    }
}