package com.ecommerce.controller;

import com.ecommerce.dto.ApiResponse;
import com.ecommerce.dto.ProductDTO;
import com.ecommerce.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * REST Controller for Product operations
 */
@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    /**
     * Create a new product (Admin only)
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<ProductDTO>> createProduct(
            @Valid @RequestBody ProductDTO productDTO) {
        ProductDTO created = productService.createProduct(productDTO);
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.success("Product created successfully", created));
    }

    /**
     * Get all active products with pagination
     */
    @GetMapping
    public ResponseEntity<ApiResponse<Page<ProductDTO>>> getAllProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "productId") String sortBy,
            @RequestParam(defaultValue = "ASC") String sortDir) {
        
        Sort sort = sortDir.equalsIgnoreCase("DESC") ? 
            Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<ProductDTO> products = productService.getAllActiveProducts(pageable);
        return ResponseEntity.ok(ApiResponse.success(products));
    }

    /**
     * Get product by ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ProductDTO>> getProductById(@PathVariable Long id) {
        ProductDTO product = productService.getProductById(id);
        return ResponseEntity.ok(ApiResponse.success(product));
    }

    /**
     * Search products by name (case-insensitive)
     */
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<Page<ProductDTO>>> searchProducts(
            @RequestParam String query,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        Pageable pageable = PageRequest.of(page, size);
        Page<ProductDTO> products = productService.searchProducts(query, pageable);
        return ResponseEntity.ok(ApiResponse.success(products));
    }

    /**
     * Get products by category
     */
    @GetMapping("/category/{categoryId}")
    public ResponseEntity<ApiResponse<Page<ProductDTO>>> getProductsByCategory(
            @PathVariable Long categoryId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        Pageable pageable = PageRequest.of(page, size);
        Page<ProductDTO> products = productService.getProductsByCategory(categoryId, pageable);
        return ResponseEntity.ok(ApiResponse.success(products));
    }

    /**
     * Update product (Admin only)
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<ProductDTO>> updateProduct(
            @PathVariable Long id,
            @Valid @RequestBody ProductDTO productDTO) {
        ProductDTO updated = productService.updateProduct(id, productDTO);
        return ResponseEntity.ok(ApiResponse.success("Product updated successfully", updated));
    }

    /**
     * Delete product (Admin only)
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteProduct(@PathVariable Long id) {
        productService.deleteProduct(id);
        return ResponseEntity.ok(ApiResponse.success("Product deleted successfully", null));
    }
}