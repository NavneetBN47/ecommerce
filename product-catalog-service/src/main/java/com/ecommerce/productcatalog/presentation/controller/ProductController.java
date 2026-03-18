package com.ecommerce.productcatalog.presentation.controller;

import com.ecommerce.productcatalog.application.dto.*;
import com.ecommerce.productcatalog.application.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
@Tag(name = "Product Catalog", description = "APIs for browsing and searching products")
public class ProductController {

    private final ProductService productService;

    @GetMapping("/{productId}")
    @Operation(
        summary = "Get product by ID",
        description = "Retrieves detailed information about a specific product by its ID. Results are cached for improved performance."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Product found",
            content = @Content(schema = @Schema(implementation = ProductResponse.class))),
        @ApiResponse(responseCode = "404", description = "Product not found",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<ProductResponse> getProductById(
            @PathVariable @Parameter(description = "Product ID", example = "123") Long productId) {
        ProductResponse response = productService.getProductById(productId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/search")
    @Operation(
        summary = "Search products",
        description = "Searches for products based on various criteria including name, category, price range, and availability. Supports pagination and sorting."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Search results retrieved",
            content = @Content(schema = @Schema(implementation = ProductSearchResponse.class))),
        @ApiResponse(responseCode = "400", description = "Invalid search parameters",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<ProductSearchResponse> searchProducts(
            @RequestParam(required = false) @Parameter(description = "Search keyword", example = "laptop") String keyword,
            @RequestParam(required = false) @Parameter(description = "Product category", example = "Electronics") String category,
            @RequestParam(required = false) @Parameter(description = "Minimum price", example = "100.00") BigDecimal minPrice,
            @RequestParam(required = false) @Parameter(description = "Maximum price", example = "1000.00") BigDecimal maxPrice,
            @RequestParam(required = false, defaultValue = "true") @Parameter(description = "Filter by availability") Boolean inStock,
            @RequestParam(defaultValue = "0") @Parameter(description = "Page number (0-indexed)", example = "0") int page,
            @RequestParam(defaultValue = "20") @Parameter(description = "Page size", example = "20") int size,
            @RequestParam(defaultValue = "name") @Parameter(description = "Sort field", example = "price") String sortBy,
            @RequestParam(defaultValue = "asc") @Parameter(description = "Sort direction (asc/desc)", example = "asc") String sortDirection) {
        ProductSearchResponse response = productService.searchProducts(
                keyword, category, minPrice, maxPrice, inStock, page, size, sortBy, sortDirection);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/category/{category}")
    @Operation(
        summary = "Get products by category",
        description = "Retrieves all products in a specific category with pagination support"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Products retrieved",
            content = @Content(schema = @Schema(implementation = ProductSearchResponse.class))),
        @ApiResponse(responseCode = "400", description = "Invalid category",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<ProductSearchResponse> getProductsByCategory(
            @PathVariable @Parameter(description = "Product category", example = "Electronics") String category,
            @RequestParam(defaultValue = "0") @Parameter(description = "Page number") int page,
            @RequestParam(defaultValue = "20") @Parameter(description = "Page size") int size) {
        ProductSearchResponse response = productService.getProductsByCategory(category, page, size);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/featured")
    @Operation(
        summary = "Get featured products",
        description = "Retrieves a list of featured products for homepage display"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Featured products retrieved",
            content = @Content(schema = @Schema(implementation = ProductSearchResponse.class)))
    })
    public ResponseEntity<ProductSearchResponse> getFeaturedProducts(
            @RequestParam(defaultValue = "10") @Parameter(description = "Number of products to retrieve") int limit) {
        ProductSearchResponse response = productService.getFeaturedProducts(limit);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/check-availability/{productId}")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(
        summary = "Check product availability",
        description = "Checks if a product is available in the requested quantity. Used by Shopping Cart Service."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Availability checked",
            content = @Content(schema = @Schema(implementation = Boolean.class))),
        @ApiResponse(responseCode = "404", description = "Product not found",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<Boolean> checkProductAvailability(
            @PathVariable @Parameter(description = "Product ID") Long productId,
            @RequestParam @Parameter(description = "Requested quantity", example = "5") Integer quantity) {
        Boolean available = productService.checkAvailability(productId, quantity);
        return ResponseEntity.ok(available);
    }
}