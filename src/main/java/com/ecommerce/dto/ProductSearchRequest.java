package com.ecommerce.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductSearchRequest {

    private String keyword;
    private String category;
    private String brand;
    private BigDecimal minPrice;
    private BigDecimal maxPrice;
    private Boolean activeOnly;
    private Integer page;
    private Integer size;
    private String sortBy;
    private String sortDirection;
}