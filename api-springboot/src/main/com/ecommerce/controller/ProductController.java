package com.ecommerce.controller;

import com.ecommerce.dto.ProductDTO;
import com.ecommerce.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Product Controller
 * Handles product search and retrieval operations
 */
@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Product Management", description = "APIs for product search and retrieval")
public class ProductController {

    private final ProductService productService;

    @GetMapping("/search")
    @Operation(summary = "Search Products", description = "Case-insensitive product search by name")
    public ResponseEntity<List<ProductDTO>> searchProducts(@RequestParam String query) {
        log.info("GET /api/products/search?query={} - Product search request", query);
        List<ProductDTO> products = productService.searchProducts(query);
        return ResponseEntity.ok(products);
    }

    @GetMapping
    @Operation(summary = "Get All Products", description = "Retrieve all products")
    public ResponseEntity<List<ProductDTO>> getAllProducts() {
        log.info("GET /api/products - Fetch all products");
        List<ProductDTO> products = productService.getAllProducts();
        return ResponseEntity.ok(products);
    }

    @GetMapping("/{productId}")
    @Operation(summary = "Get Product by ID", description = "Retrieve product details by ID")
    public ResponseEntity<ProductDTO> getProductById(@PathVariable Long productId) {
        log.info("GET /api/products/{} - Fetch product by ID", productId);
        ProductDTO product = productService.getProductById(productId);
        return ResponseEntity.ok(product);
    }

    @GetMapping("/available")
    @Operation(summary = "Get Available Products", description = "Retrieve products with quantity > 0")
    public ResponseEntity<List<ProductDTO>> getAvailableProducts() {
        log.info("GET /api/products/available - Fetch available products");
        List<ProductDTO> products = productService.getAvailableProducts();
        return ResponseEntity.ok(products);
    }
}