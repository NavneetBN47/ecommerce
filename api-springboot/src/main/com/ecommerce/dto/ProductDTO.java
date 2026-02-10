package com.ecommerce.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DTO for product response
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductDTO {

    private Long productId;
    private String productName;
    private String description;
    private String category;
    private BigDecimal price;
    private Integer stockQuantity;
    private String imageUrl;
    private Boolean isActive;
}