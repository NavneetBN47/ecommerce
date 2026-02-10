package com.ecommerce.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for product search request
 * Supports case-insensitive search
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductSearchRequest {

    private String searchTerm;
    private String category;
    private Double minPrice;
    private Double maxPrice;
    private Boolean inStockOnly = true;
}