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
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CartResponse {
    
    private UUID cartId;
    private List<CartItemResponse> items;
    private BigDecimal grandTotal;
}