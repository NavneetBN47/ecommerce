package com.ecommerce.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Data Transfer Object for Cart
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CartDTO {

    @JsonProperty("cartId")
    private UUID cartId;

    @JsonProperty("userId")
    private UUID userId;

    @JsonProperty("cartStatus")
    private String cartStatus;

    @JsonProperty("sessionId")
    private String sessionId;

    @JsonProperty("totalAmount")
    private BigDecimal totalAmount;

    @JsonProperty("totalItems")
    private Integer totalItems;

    @JsonProperty("currencyCode")
    private String currencyCode;

    @JsonProperty("items")
    private List<CartItemDTO> items = new ArrayList<>();

    @JsonProperty("createdAt")
    private LocalDateTime createdAt;

    @JsonProperty("updatedAt")
    private LocalDateTime updatedAt;

    @JsonProperty("expiresAt")
    private LocalDateTime expiresAt;
}