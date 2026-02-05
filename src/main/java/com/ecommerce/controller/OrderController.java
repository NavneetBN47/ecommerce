package com.ecommerce.controller;

import com.ecommerce.dto.ApiResponse;
import com.ecommerce.dto.OrderDTO;
import com.ecommerce.entity.Order;
import com.ecommerce.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for Order operations
 */
@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
@Tag(name = "Order Management", description = "APIs for managing orders")
public class OrderController {

    private final OrderService orderService;

    @PostMapping("/user/{userId}")
    @Operation(summary = "Create order from cart")
    public ResponseEntity<ApiResponse<OrderDTO>> createOrderFromCart(
            @PathVariable Long userId,
            @RequestParam Long shippingAddressId) {
        OrderDTO order = orderService.createOrderFromCart(userId, shippingAddressId);
        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(ApiResponse.success("Order created successfully", order));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get order by ID")
    public ResponseEntity<ApiResponse<OrderDTO>> getOrderById(@PathVariable Long id) {
        OrderDTO order = orderService.getOrderById(id);
        return ResponseEntity.ok(ApiResponse.success(order));
    }

    @GetMapping("/number/{orderNumber}")
    @Operation(summary = "Get order by order number")
    public ResponseEntity<ApiResponse<OrderDTO>> getOrderByOrderNumber(@PathVariable String orderNumber) {
        OrderDTO order = orderService.getOrderByOrderNumber(orderNumber);
        return ResponseEntity.ok(ApiResponse.success(order));
    }

    @GetMapping("/user/{userId}")
    @Operation(summary = "Get all orders for a user")
    public ResponseEntity<ApiResponse<List<OrderDTO>>> getOrdersByUserId(@PathVariable Long userId) {
        List<OrderDTO> orders = orderService.getOrdersByUserId(userId);
        return ResponseEntity.ok(ApiResponse.success(orders));
    }

    @GetMapping("/user/{userId}/page")
    @Operation(summary = "Get orders for a user with pagination")
    public ResponseEntity<ApiResponse<Page<OrderDTO>>> getOrdersByUserIdPaged(
            @PathVariable Long userId,
            Pageable pageable) {
        Page<OrderDTO> orders = orderService.getOrdersByUserId(userId, pageable);
        return ResponseEntity.ok(ApiResponse.success(orders));
    }

    @PatchMapping("/{orderId}/status")
    @Operation(summary = "Update order status")
    public ResponseEntity<ApiResponse<OrderDTO>> updateOrderStatus(
            @PathVariable Long orderId,
            @RequestParam Order.OrderStatus status) {
        OrderDTO order = orderService.updateOrderStatus(orderId, status);
        return ResponseEntity.ok(ApiResponse.success("Order status updated successfully", order));
    }

    @PostMapping("/{orderId}/cancel")
    @Operation(summary = "Cancel order")
    public ResponseEntity<ApiResponse<OrderDTO>> cancelOrder(@PathVariable Long orderId) {
        OrderDTO order = orderService.cancelOrder(orderId);
        return ResponseEntity.ok(ApiResponse.success("Order cancelled successfully", order));
    }
}