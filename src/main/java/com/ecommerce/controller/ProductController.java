package com.ecommerce.controller;

import com.ecommerce.dto.ProductResponseDTO;
import com.ecommerce.service.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Product Controller
 * Handles product catalog endpoints
 * API Contracts as per LLD Section 4.2
 */
@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
@Slf4j
public class ProductController {

    private final ProductService productService;

    /**
     * GET /api/products?search=keyword
     * Search products by keyword (case-insensitive)
     * @param search optional search keyword
     * @return 200 OK with list of products
     */
    @GetMapping
    public ResponseEntity<List<ProductResponseDTO>> searchProducts(
            @RequestParam(required = false) String search) {
        log.info("Product search request with keyword: {}", search);
        List<ProductResponseDTO> products = productService.searchProducts(search);
        return ResponseEntity.ok(products);
    }
}