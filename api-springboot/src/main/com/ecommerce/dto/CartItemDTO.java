package com.ecommerce.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Data Transfer Object for CartItem
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CartItemDTO {

    @JsonProperty("itemId")
    private UUID itemId;

    @JsonProperty("productId")
    private UUID productId;

    @JsonProperty("productName")
    private String productName;

    @JsonProperty("quantity")
    private Integer quantity;

    @JsonProperty("unitPrice")
    private BigDecimal unitPrice;

    @JsonProperty("totalPrice")
    private BigDecimal totalPrice;

    @JsonProperty("addedAt")
    private LocalDateTime addedAt;
}