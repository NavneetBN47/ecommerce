package com.ecommerce.controller;

import com.ecommerce.dto.ApiResponseDTO;
import com.ecommerce.dto.ProductDTO;
import com.ecommerce.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller for product operations
 * Implements case-insensitive search
 */
@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
@Slf4j
public class ProductController {

    private final ProductService productService;

    /**
     * Create a new product (admin only)
     */
    @PostMapping
    public ResponseEntity<ApiResponseDTO<ProductDTO>> createProduct(@Valid @RequestBody ProductDTO productDTO) {
        log.info("Create product request received: {}", productDTO.getName());
        ProductDTO createdProduct = productService.createProduct(productDTO);
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponseDTO.success("Product created successfully", createdProduct));
    }

    /**
     * Get product by ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponseDTO<ProductDTO>> getProductById(@PathVariable Long id) {
        log.info("Get product request for ID: {}", id);
        ProductDTO product = productService.getProductById(id);
        return ResponseEntity.ok(ApiResponseDTO.success(product));
    }

    /**
     * Get all active products
     */
    @GetMapping
    public ResponseEntity<ApiResponseDTO<List<ProductDTO>>> getAllProducts() {
        log.info("Get all products request received");
        List<ProductDTO> products = productService.getAllActiveProducts();
        return ResponseEntity.ok(ApiResponseDTO.success(products));
    }

    /**
     * Search products by name or description (case-insensitive)
     */
    @GetMapping("/search")
    public ResponseEntity<ApiResponseDTO<Page<ProductDTO>>> searchProducts(
            @RequestParam String query,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "name") String sortBy) {
        log.info("Search products request with query: {}", query);
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortBy));
        Page<ProductDTO> products = productService.searchProducts(query, pageable);
        return ResponseEntity.ok(ApiResponseDTO.success(products));
    }

    /**
     * Get products by category (case-insensitive)
     */
    @GetMapping("/category/{category}")
    public ResponseEntity<ApiResponseDTO<List<ProductDTO>>> getProductsByCategory(@PathVariable String category) {
        log.info("Get products by category request: {}", category);
        List<ProductDTO> products = productService.getProductsByCategory(category);
        return ResponseEntity.ok(ApiResponseDTO.success(products));
    }

    /**
     * Update product (admin only)
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponseDTO<ProductDTO>> updateProduct(
            @PathVariable Long id,
            @Valid @RequestBody ProductDTO productDTO) {
        log.info("Update product request for ID: {}", id);
        ProductDTO updatedProduct = productService.updateProduct(id, productDTO);
        return ResponseEntity.ok(ApiResponseDTO.success("Product updated successfully", updatedProduct));
    }

    /**
     * Delete product (admin only - soft delete)
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponseDTO<Void>> deleteProduct(@PathVariable Long id) {
        log.info("Delete product request for ID: {}", id);
        productService.deleteProduct(id);
        return ResponseEntity.ok(ApiResponseDTO.success("Product deleted successfully", null));
    }
}