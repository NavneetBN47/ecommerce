package com.ecommerce.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductResponse {

    private UUID productId;
    private String name;
    private String description;
    private String sku;
    private UUID categoryId;
    private String categoryName;
    private BigDecimal price;
    private Integer quantityAvailable;
    private Boolean isActive;
    private Boolean isFeatured;
}