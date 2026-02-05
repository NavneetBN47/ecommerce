package com.example.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Data Transfer Object for Product
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductDTO {

    @JsonProperty("product_id")
    private Long productId;

    @JsonProperty("sku")
    private String sku;

    @JsonProperty("name")
    private String name;

    @JsonProperty("description")
    private String description;

    @JsonProperty("price")
    private BigDecimal price;

    @JsonProperty("stock_quantity")
    private Integer stockQuantity;

    @JsonProperty("category")
    private String category;

    @JsonProperty("image_url")
    private String imageUrl;

    @JsonProperty("is_active")
    private Boolean isActive;

    @JsonProperty("created_at")
    private LocalDateTime createdAt;

    @JsonProperty("updated_at")
    private LocalDateTime updatedAt;
}