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
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Product Catalog", description = "APIs for product search and retrieval")
public class ProductController {

    private final ProductService productService;

    @GetMapping("/search")
    @Operation(
        summary = "Search products",
        description = "Search products by keyword with pagination support. Searches across product name, description, and category."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Products retrieved successfully",
            content = @Content(schema = @Schema(implementation = ProductSearchResponse.class))
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid search parameters",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
        ),
        @ApiResponse(
            responseCode = "500",
            description = "Internal server error",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
        )
    })
    public ResponseEntity<ProductSearchResponse> searchProducts(
            @RequestParam(required = false) 
            @Parameter(description = "Search keyword for product name, description, or category") 
            String keyword,
            @RequestParam(defaultValue = "0") 
            @Parameter(description = "Page number (0-indexed)") 
            int page,
            @RequestParam(defaultValue = "20") 
            @Parameter(description = "Number of items per page (max 100)") 
            int size) {
        log.info("Product search request - keyword: {}, page: {}, size: {}", keyword, page, size);
        ProductSearchResponse response = productService.searchProducts(keyword, page, size);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{productId}")
    @Operation(
        summary = "Get product details",
        description = "Retrieves detailed information about a specific product by its ID"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Product details retrieved successfully",
            content = @Content(schema = @Schema(implementation = ProductResponse.class))
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Product not found",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
        ),
        @ApiResponse(
            responseCode = "500",
            description = "Internal server error",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
        )
    })
    public ResponseEntity<ProductResponse> getProductById(
            @PathVariable 
            @Parameter(description = "Unique product identifier", required = true) 
            Long productId) {
        log.info("Fetching product details for productId: {}", productId);
        ProductResponse response = productService.getProductById(productId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/category/{category}")
    @Operation(
        summary = "Get products by category",
        description = "Retrieves all products in a specific category with pagination support"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Products retrieved successfully",
            content = @Content(schema = @Schema(implementation = ProductSearchResponse.class))
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid category or pagination parameters",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
        ),
        @ApiResponse(
            responseCode = "500",
            description = "Internal server error",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
        )
    })
    public ResponseEntity<ProductSearchResponse> getProductsByCategory(
            @PathVariable 
            @Parameter(description = "Product category name", required = true) 
            String category,
            @RequestParam(defaultValue = "0") 
            @Parameter(description = "Page number (0-indexed)") 
            int page,
            @RequestParam(defaultValue = "20") 
            @Parameter(description = "Number of items per page (max 100)") 
            int size) {
        log.info("Fetching products for category: {}, page: {}, size: {}", category, page, size);
        ProductSearchResponse response = productService.getProductsByCategory(category, page, size);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/available")
    @Operation(
        summary = "Get available products",
        description = "Retrieves all products that are currently in stock with pagination support"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Available products retrieved successfully",
            content = @Content(schema = @Schema(implementation = ProductSearchResponse.class))
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid pagination parameters",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
        ),
        @ApiResponse(
            responseCode = "500",
            description = "Internal server error",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
        )
    })
    public ResponseEntity<ProductSearchResponse> getAvailableProducts(
            @RequestParam(defaultValue = "0") 
            @Parameter(description = "Page number (0-indexed)") 
            int page,
            @RequestParam(defaultValue = "20") 
            @Parameter(description = "Number of items per page (max 100)") 
            int size) {
        log.info("Fetching available products - page: {}, size: {}", page, size);
        ProductSearchResponse response = productService.getAvailableProducts(page, size);
        return ResponseEntity.ok(response);
    }
}