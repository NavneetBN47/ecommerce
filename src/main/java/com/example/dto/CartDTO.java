package com.example.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Data Transfer Object for Cart
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CartDTO {

    @JsonProperty("cart_id")
    private Long cartId;

    @JsonProperty("user_id")
    private Long userId;

    @JsonProperty("total_amount")
    private BigDecimal totalAmount;

    @JsonProperty("total_items")
    private Integer totalItems;

    @JsonProperty("items")
    private List<CartItemDTO> items;

    @JsonProperty("created_at")
    private LocalDateTime createdAt;

    @JsonProperty("updated_at")
    private LocalDateTime updatedAt;
}