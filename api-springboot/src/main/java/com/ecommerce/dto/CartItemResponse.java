package com.ecommerce.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * DTO for cart item in cart response
 * Implements LLD cart item response contract
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CartItemResponse {

    private UUID itemId;
    private UUID productId;
    private String name;
    private Integer quantity;
    private BigDecimal price;
    private BigDecimal total;
}