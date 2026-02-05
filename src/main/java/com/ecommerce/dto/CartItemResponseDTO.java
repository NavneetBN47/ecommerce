package com.ecommerce.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * DTO for cart item in response
 * Used in cart response DTOs
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CartItemResponseDTO {

    private UUID itemId;
    private UUID productId;
    private String name;
    private String description;
    private BigDecimal unitPrice;
    private Integer quantity;
    private BigDecimal total;
}