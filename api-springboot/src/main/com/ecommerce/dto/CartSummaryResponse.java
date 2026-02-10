package com.ecommerce.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * DTO for cart summary response
 * Includes totals calculation (subtotal, tax, grand total)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CartSummaryResponse {

    private Long cartId;
    private List<CartItemDTO> items;
    private Integer totalItems;
    private BigDecimal subtotal;
    private BigDecimal taxAmount;
    private BigDecimal taxRate;
    private BigDecimal grandTotal;
}