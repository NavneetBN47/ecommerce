package com.ecommerce.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO for Cart response
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CartDTO {

    private Long cartId;
    private Long userId;
    private String status;
    private BigDecimal totalAmount;
    private Integer totalItems;
    private List<CartItemDTO> items;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}