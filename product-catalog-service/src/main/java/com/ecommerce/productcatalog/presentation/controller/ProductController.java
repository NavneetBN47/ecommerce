package com.ecommerce.productcatalog.presentation.controller;

import com.ecommerce.productcatalog.application.dto.*;
import com.ecommerce.productcatalog.application.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
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

    @GetMapping("/{id}")
    @Operation(
        summary = "Get product by ID",
        description = "Retrieves detailed information about a specific product"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Product found",
            content = @Content(schema = @Schema(implementation = ProductResponse.class))
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Product not found",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
        )
    })
    public ResponseEntity<ProductResponse> getProductById(
            @PathVariable @Parameter(description = "Product ID", example = "1") Long id) {
        ProductResponse response = productService.getProductById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    @Operation(
        summary = "Get all products",
        description = "Retrieves a paginated list of all products"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Products retrieved successfully",
            content = @Content(schema = @Schema(implementation = ProductSearchResponse.class))
        )
    })
    public ResponseEntity<ProductSearchResponse> getAllProducts(
            @RequestParam(defaultValue = "0") @Parameter(description = "Page number (0-indexed)", example = "0") int page,
            @RequestParam(defaultValue = "20") @Parameter(description = "Page size", example = "20") int size) {
        ProductSearchResponse response = productService.getAllProducts(page, size);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/search")
    @Operation(
        summary = "Search products",
        description = "Searches products by name, category, or other criteria with pagination"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Search completed successfully",
            content = @Content(schema = @Schema(implementation = ProductSearchResponse.class))
        )
    })
    public ResponseEntity<ProductSearchResponse> searchProducts(
            @RequestParam(required = false) @Parameter(description = "Product name keyword", example = "laptop") String name,
            @RequestParam(required = false) @Parameter(description = "Product category", example = "Electronics") String category,
            @RequestParam(required = false) @Parameter(description = "Minimum price", example = "100.00") BigDecimal minPrice,
            @RequestParam(required = false) @Parameter(description = "Maximum price", example = "1000.00") BigDecimal maxPrice,
            @RequestParam(defaultValue = "0") @Parameter(description = "Page number (0-indexed)", example = "0") int page,
            @RequestParam(defaultValue = "20") @Parameter(description = "Page size", example = "20") int size) {
        ProductSearchResponse response = productService.searchProducts(name, category, minPrice, maxPrice, page, size);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/category/{category}")
    @Operation(
        summary = "Get products by category",
        description = "Retrieves all products in a specific category with pagination"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Products retrieved successfully",
            content = @Content(schema = @Schema(implementation = ProductSearchResponse.class))
        )
    })
    public ResponseEntity<ProductSearchResponse> getProductsByCategory(
            @PathVariable @Parameter(description = "Product category", example = "Electronics") String category,
            @RequestParam(defaultValue = "0") @Parameter(description = "Page number (0-indexed)", example = "0") int page,
            @RequestParam(defaultValue = "20") @Parameter(description = "Page size", example = "20") int size) {
        ProductSearchResponse response = productService.getProductsByCategory(category, page, size);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/available")
    @Operation(
        summary = "Get available products",
        description = "Retrieves all products that are currently in stock"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Available products retrieved successfully",
            content = @Content(schema = @Schema(implementation = ProductSearchResponse.class))
        )
    })
    public ResponseEntity<ProductSearchResponse> getAvailableProducts(
            @RequestParam(defaultValue = "0") @Parameter(description = "Page number (0-indexed)", example = "0") int page,
            @RequestParam(defaultValue = "20") @Parameter(description = "Page size", example = "20") int size) {
        ProductSearchResponse response = productService.getAvailableProducts(page, size);
        return ResponseEntity.ok(response);
    }
}