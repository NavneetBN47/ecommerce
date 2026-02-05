package com.ecommerce.dto;

import com.ecommerce.entity.Order;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Transfer Object for Order
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderDTO {
    
    private Long id;
    
    private String orderNumber;
    
    @NotNull(message = "User ID is required")
    private Long userId;
    
    private List<OrderItemDTO> items = new ArrayList<>();
    
    private BigDecimal totalAmount;
    
    private Order.OrderStatus status;
    
    @NotBlank(message = "Shipping address is required")
    private String shippingAddress;
    
    private String paymentMethod;
    
    private LocalDateTime createdAt;
    
    private LocalDateTime updatedAt;
}