package com.ecommerce.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

/**
 * Cart Item Response DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CartItemResponse {

    private Long itemId;
    private Long productId;
    private String name;
    private Integer quantity;
    private BigDecimal price;
    private BigDecimal total;
}