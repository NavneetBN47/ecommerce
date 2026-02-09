package com.ecommerce.controller;

import com.ecommerce.dto.ApiResponse;
import com.ecommerce.dto.ProductResponse;
import com.ecommerce.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller for product catalog operations
 * Implements LLD product search API contract
 */
@RestController
@RequestMapping("/products")
@Tag(name = "Product Catalog", description = "APIs for product search and browsing")
public class ProductController {

    private static final Logger logger = LoggerFactory.getLogger(ProductController.class);

    @Autowired
    private ProductService productService;

    /**
     * Product search endpoint (case-insensitive)
     * Implements LLD GET /api/products/search?keyword=...
     */
    @GetMapping("/search")
    @Operation(summary = "Search products", description = "Search products by keyword (case-insensitive)")
    public ResponseEntity<ApiResponse<List<ProductResponse>>> searchProducts(
            @RequestParam(name = "keyword") String keyword) {
        logger.info("Product search request received with keyword: {}", keyword);
        List<ProductResponse> products = productService.searchProducts(keyword);
        return ResponseEntity.ok(ApiResponse.success(products));
    }
}