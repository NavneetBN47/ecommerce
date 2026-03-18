package com.ecommerce.productcatalog.presentation.controller;

import com.ecommerce.productcatalog.application.dto.ProductResponse;
import com.ecommerce.productcatalog.application.dto.ProductSearchResponse;
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

@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
@Tag(name = "Product Catalog", description = "APIs for product search and retrieval with Redis caching")
public class ProductController {

    private final ProductService productService;

    @GetMapping("/search")
    @Operation(
        summary = "Search products",
        description = "Search products by keyword with pagination support. Supports case-insensitive full-text search across product names and descriptions. Results are cached in Redis for 5 minutes."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Products retrieved successfully",
            content = @Content(schema = @Schema(implementation = ProductSearchResponse.class))
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid pagination parameters",
            content = @Content(schema = @Schema(implementation = String.class))
        ),
        @ApiResponse(
            responseCode = "500",
            description = "Internal server error",
            content = @Content(schema = @Schema(implementation = String.class))
        )
    })
    public ResponseEntity<ProductSearchResponse> searchProducts(
        @Parameter(
            description = "Search keyword (searches in product name and description)",
            example = "laptop"
        )
        @RequestParam(required = false) String keyword,
        
        @Parameter(
            description = "Page number (0-indexed)",
            example = "0"
        )
        @RequestParam(defaultValue = "0") int page,
        
        @Parameter(
            description = "Number of items per page",
            example = "20"
        )
        @RequestParam(defaultValue = "20") int size
    ) {
        ProductSearchResponse response = productService.searchProducts(keyword, page, size);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{productId}")
    @Operation(
        summary = "Get product details",
        description = "Retrieves detailed information about a specific product by ID. Product data is cached in Redis with 10-minute TTL."
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
            content = @Content(schema = @Schema(implementation = String.class))
        ),
        @ApiResponse(
            responseCode = "500",
            description = "Internal server error",
            content = @Content(schema = @Schema(implementation = String.class))
        )
    })
    public ResponseEntity<ProductResponse> getProductById(
        @Parameter(
            description = "Unique product identifier",
            required = true,
            example = "12345"
        )
        @PathVariable Long productId
    ) {
        ProductResponse response = productService.getProductById(productId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/category/{category}")
    @Operation(
        summary = "Get products by category",
        description = "Retrieves all products in a specific category with pagination support. Results are cached."
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
            content = @Content(schema = @Schema(implementation = String.class))
        )
    })
    public ResponseEntity<ProductSearchResponse> getProductsByCategory(
        @Parameter(
            description = "Product category",
            required = true,
            example = "Electronics"
        )
        @PathVariable String category,
        
        @Parameter(
            description = "Page number (0-indexed)",
            example = "0"
        )
        @RequestParam(defaultValue = "0") int page,
        
        @Parameter(
            description = "Number of items per page",
            example = "20"
        )
        @RequestParam(defaultValue = "20") int size
    ) {
        ProductSearchResponse response = productService.getProductsByCategory(category, page, size);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/available")
    @Operation(
        summary = "Get available products",
        description = "Retrieves all products that are currently in stock (availableQuantity > 0)"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Available products retrieved successfully",
            content = @Content(schema = @Schema(implementation = ProductSearchResponse.class))
        )
    })
    public ResponseEntity<ProductSearchResponse> getAvailableProducts(
        @Parameter(description = "Page number (0-indexed)", example = "0")
        @RequestParam(defaultValue = "0") int page,
        
        @Parameter(description = "Number of items per page", example = "20")
        @RequestParam(defaultValue = "20") int size
    ) {
        ProductSearchResponse response = productService.getAvailableProducts(page, size);
        return ResponseEntity.ok(response);
    }
}