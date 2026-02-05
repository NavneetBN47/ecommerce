package com.example.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.math.BigDecimal;

/**
 * Data Transfer Object for Cart Item
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CartItemDTO {

    @JsonProperty("item_id")
    private Long itemId;

    @JsonProperty("product_id")
    private Long productId;

    @JsonProperty("product_name")
    private String productName;

    @JsonProperty("product_sku")
    private String productSku;

    @JsonProperty("quantity")
    private Integer quantity;

    @JsonProperty("unit_price")
    private BigDecimal unitPrice;

    @JsonProperty("subtotal")
    private BigDecimal subtotal;

    @JsonProperty("stock_available")
    private Integer stockAvailable;
}