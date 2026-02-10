package com.ecommerce.controller;

import com.ecommerce.dto.ApiResponse;
import com.ecommerce.dto.ProductDTO;
import com.ecommerce.service.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller for product endpoints
 * Implements case-insensitive product search
 */
@RestController
@RequestMapping("/products")
@RequiredArgsConstructor
@Slf4j
public class ProductController {

    private final ProductService productService;

    /**
     * Get all products
     * GET /api/products
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<ProductDTO>>> getAllProducts() {
        log.info("Get all products request received");
        List<ProductDTO> products = productService.getAllProducts();
        return ResponseEntity.ok(ApiResponse.success("Products retrieved successfully", products));
    }

    /**
     * Get product by ID
     * GET /api/products/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ProductDTO>> getProductById(@PathVariable Long id) {
        log.info("Get product by ID request received: {}", id);
        ProductDTO product = productService.getProductById(id);
        return ResponseEntity.ok(ApiResponse.success("Product retrieved successfully", product));
    }

    /**
     * Search products (case-insensitive)
     * GET /api/products/search?q={searchTerm}
     * Business Rule: Product search must be case-insensitive
     */
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<ProductDTO>>> searchProducts(
            @RequestParam(name = "q", required = false) String searchTerm) {
        log.info("Product search request received with term: {}", searchTerm);
        List<ProductDTO> products = productService.searchProducts(searchTerm);
        return ResponseEntity.ok(ApiResponse.success("Products retrieved successfully", products));
    }

    /**
     * Get products by category
     * GET /api/products/category/{category}
     */
    @GetMapping("/category/{category}")
    public ResponseEntity<ApiResponse<List<ProductDTO>>> getProductsByCategory(@PathVariable String category) {
        log.info("Get products by category request received: {}", category);
        List<ProductDTO> products = productService.getProductsByCategory(category);
        return ResponseEntity.ok(ApiResponse.success("Products retrieved successfully", products));
    }

    /**
     * Get products in stock
     * GET /api/products/in-stock
     */
    @GetMapping("/in-stock")
    public ResponseEntity<ApiResponse<List<ProductDTO>>> getProductsInStock() {
        log.info("Get products in stock request received");
        List<ProductDTO> products = productService.getProductsInStock();
        return ResponseEntity.ok(ApiResponse.success("Products retrieved successfully", products));
    }
}