package com.ecommerce.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Transfer Object for Cart
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CartDTO {
    
    private Long id;
    
    private Long userId;
    
    private List<CartItemDTO> items = new ArrayList<>();
    
    private BigDecimal totalAmount;
    
    private Integer totalItems;
    
    private LocalDateTime createdAt;
    
    private LocalDateTime updatedAt;
}