package com.ecommerce.controller;

import com.ecommerce.dto.ProductResponse;
import com.ecommerce.service.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller for product catalog operations
 */
@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
@Slf4j
public class ProductController {
    
    private final ProductService productService;
    
    /**
     * GET /api/products?search=keyword - Search products
     */
    @GetMapping
    public ResponseEntity<List<ProductResponse>> searchProducts(
            @RequestParam(required = false) String search) {
        log.info("Product search request with keyword: {}", search);
        List<ProductResponse> products = productService.searchProducts(search);
        return ResponseEntity.ok(products);
    }
}