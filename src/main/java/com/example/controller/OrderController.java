package com.example.controller;

import com.example.dto.ApiResponse;
import com.example.dto.CreateOrderRequest;
import com.example.dto.OrderDTO;
import com.example.entity.Order;
import com.example.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST Controller for Order operations.
 */
@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Order Management", description = "APIs for order creation and management")
public class OrderController {
    
    private final OrderService orderService;
    
    @PostMapping("/user/{userId}")
    @Operation(summary = "Create order from cart")
    public ResponseEntity<ApiResponse<OrderDTO>> createOrder(
            @PathVariable Long userId,
            @Valid @RequestBody CreateOrderRequest request) {
        log.info("Creating order for user: {}", userId);
        OrderDTO order = orderService.createOrder(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.success("Order created successfully", order));
    }
    
    @GetMapping("/{id}")
    @Operation(summary = "Get order by ID")
    public ResponseEntity<ApiResponse<OrderDTO>> getOrderById(@PathVariable Long id) {
        log.info("Fetching order with ID: {}", id);
        OrderDTO order = orderService.getOrderById(id);
        return ResponseEntity.ok(ApiResponse.success(order));
    }
    
    @GetMapping("/number/{orderNumber}")
    @Operation(summary = "Get order by order number")
    public ResponseEntity<ApiResponse<OrderDTO>> getOrderByOrderNumber(@PathVariable String orderNumber) {
        log.info("Fetching order with order number: {}", orderNumber);
        OrderDTO order = orderService.getOrderByOrderNumber(orderNumber);
        return ResponseEntity.ok(ApiResponse.success(order));
    }
    
    @GetMapping("/user/{userId}")
    @Operation(summary = "Get orders by user")
    public ResponseEntity<ApiResponse<Page<OrderDTO>>> getOrdersByUserId(
            @PathVariable Long userId,
            @PageableDefault(size = 20) Pageable pageable) {
        log.info("Fetching orders for user: {}", userId);
        Page<OrderDTO> orders = orderService.getOrdersByUserId(userId, pageable);
        return ResponseEntity.ok(ApiResponse.success(orders));
    }
    
    @GetMapping
    @Operation(summary = "Get all orders")
    public ResponseEntity<ApiResponse<Page<OrderDTO>>> getAllOrders(
            @PageableDefault(size = 20) Pageable pageable) {
        log.info("Fetching all orders");
        Page<OrderDTO> orders = orderService.getAllOrders(pageable);
        return ResponseEntity.ok(ApiResponse.success(orders));
    }
    
    @PutMapping("/{id}/status")
    @Operation(summary = "Update order status")
    public ResponseEntity<ApiResponse<OrderDTO>> updateOrderStatus(
            @PathVariable Long id,
            @RequestParam Order.OrderStatus status) {
        log.info("Updating order status: {} to {}", id, status);
        OrderDTO order = orderService.updateOrderStatus(id, status);
        return ResponseEntity.ok(ApiResponse.success("Order status updated successfully", order));
    }
    
    @PostMapping("/{id}/cancel")
    @Operation(summary = "Cancel order")
    public ResponseEntity<ApiResponse<OrderDTO>> cancelOrder(@PathVariable Long id) {
        log.info("Cancelling order: {}", id);
        OrderDTO order = orderService.cancelOrder(id);
        return ResponseEntity.ok(ApiResponse.success("Order cancelled successfully", order));
    }
}