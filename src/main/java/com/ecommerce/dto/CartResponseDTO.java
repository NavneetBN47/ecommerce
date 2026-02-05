package com.ecommerce.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/**
 * DTO for cart response
 * Used in GET /api/cart and cart modification responses
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CartResponseDTO {

    private UUID cartId;
    private List<CartItemResponseDTO> items;
    private BigDecimal grandTotal;
    private Integer itemCount;
}