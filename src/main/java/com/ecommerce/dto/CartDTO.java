package com.ecommerce.dto;

import com.ecommerce.entity.Cart;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Cart Data Transfer Object
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CartDTO {

    private Long id;

    private Long userId;

    private String username;

    @Builder.Default
    private List<CartItemDTO> items = new ArrayList<>();

    private Cart.CartStatus status;

    @Builder.Default
    private BigDecimal totalAmount = BigDecimal.ZERO;

    @Builder.Default
    private Integer totalItems = 0;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}