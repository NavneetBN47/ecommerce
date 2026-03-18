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

@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
@Tag(name = "Product Catalog", description = "APIs for product search, retrieval, and catalog management")
public class ProductController {

    private final ProductService productService;

    @GetMapping("/search")
    @Operation(
        summary = "Search products",
        description = "Search products by keyword with pagination support. Uses case-insensitive full-text search with PostgreSQL GIN indexes. Results are cached in Redis for 10 minutes."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Search results retrieved successfully",
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
        @RequestParam(required = false) @Parameter(description = "Search keyword for product name or description") String keyword,
        @RequestParam(defaultValue = "0") @Parameter(description = "Page number (0-indexed)") int page,
        @RequestParam(defaultValue = "20") @Parameter(description = "Number of items per page (max 100)") int size,
        @RequestParam(defaultValue = "name") @Parameter(description = "Sort field (name, price, createdAt)") String sortBy,
        @RequestParam(defaultValue = "asc") @Parameter(description = "Sort direction (asc, desc)") String sortDirection
    ) {
        ProductSearchResponse response = productService.searchProducts(keyword, page, size, sortBy, sortDirection);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{productId}")
    @Operation(
        summary = "Get product details",
        description = "Retrieves detailed information about a specific product by ID. Results are cached in Redis for 10 minutes."
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
        @PathVariable @Parameter(description = "Product ID", required = true) Long productId
    ) {
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
        )
    })
    public ResponseEntity<ProductSearchResponse> getProductsByCategory(
        @PathVariable @Parameter(description = "Product category", required = true) String category,
        @RequestParam(defaultValue = "0") @Parameter(description = "Page number (0-indexed)") int page,
        @RequestParam(defaultValue = "20") @Parameter(description = "Number of items per page") int size
    ) {
        ProductSearchResponse response = productService.getProductsByCategory(category, page, size);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/validate/{productId}")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(
        summary = "Validate product availability",
        description = "Internal API to validate product existence and availability (used by other microservices like Shopping Cart)"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Product validation result",
            content = @Content(schema = @Schema(implementation = ProductValidationResponse.class))
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Unauthorized - Invalid or missing JWT token",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
        )
    })
    public ResponseEntity<ProductValidationResponse> validateProduct(
        @PathVariable @Parameter(description = "Product ID to validate", required = true) Long productId
    ) {
        ProductValidationResponse response = productService.validateProduct(productId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/featured")
    @Operation(
        summary = "Get featured products",
        description = "Retrieves a list of featured/promoted products for homepage display"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Featured products retrieved successfully",
            content = @Content(schema = @Schema(implementation = ProductSearchResponse.class))
        )
    })
    public ResponseEntity<ProductSearchResponse> getFeaturedProducts(
        @RequestParam(defaultValue = "10") @Parameter(description = "Number of featured products to return") int limit
    ) {
        ProductSearchResponse response = productService.getFeaturedProducts(limit);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/check-stock")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(
        summary = "Check stock availability",
        description = "Checks if requested quantity is available for a product (used by Shopping Cart service)"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Stock check completed",
            content = @Content(schema = @Schema(implementation = StockCheckResponse.class))
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Unauthorized",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
        )
    })
    public ResponseEntity<StockCheckResponse> checkStock(
        @RequestBody @Parameter(description = "Stock check request", required = true) StockCheckRequest request
    ) {
        StockCheckResponse response = productService.checkStock(request);
        return ResponseEntity.ok(response);
    }
}

class ErrorResponse {
    private String message;
    private String error;
    private int status;
    private String timestamp;
}

class ProductValidationResponse {
    private boolean exists;
    private boolean available;
    private Long productId;
    private String name;
    private Double price;
    private Integer stockQuantity;
}

class StockCheckRequest {
    private Long productId;
    private Integer requestedQuantity;
}

class StockCheckResponse {
    private boolean available;
    private Integer availableQuantity;
    private String message;
}