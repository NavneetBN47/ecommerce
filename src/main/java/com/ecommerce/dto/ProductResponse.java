package com.ecommerce.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * DTO for product response
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductResponse {
    
    private UUID id;
    private String name;
    private String description;
    private BigDecimal price;
    private Integer availableQty;
}