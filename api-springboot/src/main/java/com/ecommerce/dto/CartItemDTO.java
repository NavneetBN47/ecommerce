package com.ecommerce.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO for Cart Item
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CartItemDTO {

    private Long cartItemId;
    private Long productId;
    private String productName;
    private String productImageUrl;
    private Integer quantity;
    private BigDecimal price;
    private BigDecimal subtotal;
    private LocalDateTime addedAt;
    private LocalDateTime updatedAt;
}