package com.example.controller;

import com.example.dto.ApiResponse;
import com.example.dto.ProductDTO;
import com.example.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST Controller for Product operations
 */
@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Products", description = "Product management APIs")
public class ProductController {

    private final ProductService productService;

    @GetMapping("/{productId}")
    @Operation(summary = "Get product by ID", description = "Retrieve a product by its ID")
    public ResponseEntity<ApiResponse<ProductDTO>> getProductById(@PathVariable Long productId) {
        log.info("GET /api/v1/products/{}", productId);
        
        ProductDTO product = productService.getProductById(productId);
        return ResponseEntity.ok(ApiResponse.success("Product retrieved successfully", product));
    }

    @GetMapping
    @Operation(summary = "Get all products", description = "Retrieve all products with pagination")
    public ResponseEntity<ApiResponse<Page<ProductDTO>>> getAllProducts(
            @PageableDefault(size = 20, sort = "name", direction = Sort.Direction.ASC) Pageable pageable) {
        log.info("GET /api/v1/products - pageable: {}", pageable);
        
        Page<ProductDTO> products = productService.getAllProducts(pageable);
        return ResponseEntity.ok(ApiResponse.success("Products retrieved successfully", products));
    }

    @GetMapping("/search")
    @Operation(summary = "Search products", description = "Search products by name (case-insensitive)")
    public ResponseEntity<ApiResponse<Page<ProductDTO>>> searchProducts(
            @RequestParam String q,
            @PageableDefault(size = 20, sort = "name", direction = Sort.Direction.ASC) Pageable pageable) {
        log.info("GET /api/v1/products/search?q={}", q);
        
        Page<ProductDTO> products = productService.searchProducts(q, pageable);
        return ResponseEntity.ok(ApiResponse.success("Products search completed", products));
    }

    @GetMapping("/category/{category}")
    @Operation(summary = "Get products by category", description = "Retrieve products filtered by category")
    public ResponseEntity<ApiResponse<Page<ProductDTO>>> getProductsByCategory(
            @PathVariable String category,
            @PageableDefault(size = 20, sort = "name", direction = Sort.Direction.ASC) Pageable pageable) {
        log.info("GET /api/v1/products/category/{}", category);
        
        Page<ProductDTO> products = productService.getProductsByCategory(category, pageable);
        return ResponseEntity.ok(ApiResponse.success("Products retrieved successfully", products));
    }
}