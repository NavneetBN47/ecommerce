package com.ecommerce.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Data Transfer Object for OrderItem
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderItemDTO {
    
    private Long id;
    
    private Long orderId;
    
    private Long productId;
    
    private String productName;
    
    private String productSku;
    
    private Integer quantity;
    
    private BigDecimal price;
    
    private BigDecimal subtotal;
    
    private LocalDateTime createdAt;
}