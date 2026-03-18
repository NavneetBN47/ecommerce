package com.ecommerce.product.controller;

import com.ecommerce.product.dto.ProductRequest;
import com.ecommerce.product.dto.ProductResponse;
import com.ecommerce.product.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/products")
@Tag(name = "Product Catalog", description = "APIs for managing product catalog, inventory, and product search")
public class ProductController {
    
    @Autowired
    private ProductService productService;
    
    @PostMapping
    @Operation(
        summary = "Create a new product",
        description = "Adds a new product to the catalog with the provided information."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "201",
            description = "Product created successfully",
            content = @Content(schema = @Schema(implementation = ProductResponse.class))
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid input or product SKU already exists",
            content = @Content
        )
    })
    public ResponseEntity<ProductResponse> createProduct(
        @Valid @RequestBody ProductRequest request
    ) {
        ProductResponse response = productService.createProduct(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    @GetMapping("/{productId}")
    @Operation(
        summary = "Get product by ID",
        description = "Retrieves detailed information about a specific product."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Product retrieved successfully",
            content = @Content(schema = @Schema(implementation = ProductResponse.class))
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Product not found",
            content = @Content
        )
    })
    public ResponseEntity<ProductResponse> getProduct(
        @Parameter(description = "Product ID", required = true, example = "1")
        @PathVariable Long productId
    ) {
        ProductResponse response = productService.getProduct(productId);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping
    @Operation(
        summary = "Get all products",
        description = "Retrieves a paginated list of all active products in the catalog."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Products retrieved successfully",
            content = @Content(schema = @Schema(implementation = Page.class))
        )
    })
    public ResponseEntity<Page<ProductResponse>> getAllProducts(
        @Parameter(description = "Page number (0-indexed)", example = "0")
        @RequestParam(defaultValue = "0") int page,
        @Parameter(description = "Page size", example = "20")
        @RequestParam(defaultValue = "20") int size,
        @Parameter(description = "Sort field", example = "name")
        @RequestParam(defaultValue = "name") String sortBy
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortBy));
        Page<ProductResponse> response = productService.getAllProducts(pageable);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/search")
    @Operation(
        summary = "Search products",
        description = "Searches for products by keyword in name or description."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Search completed successfully",
            content = @Content(schema = @Schema(implementation = Page.class))
        )
    })
    public ResponseEntity<Page<ProductResponse>> searchProducts(
        @Parameter(description = "Search keyword", required = true, example = "laptop")
        @RequestParam String keyword,
        @Parameter(description = "Page number (0-indexed)", example = "0")
        @RequestParam(defaultValue = "0") int page,
        @Parameter(description = "Page size", example = "20")
        @RequestParam(defaultValue = "20") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        Page<ProductResponse> response = productService.searchProducts(keyword, pageable);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/category/{category}")
    @Operation(
        summary = "Get products by category",
        description = "Retrieves all products in a specific category."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Products retrieved successfully",
            content = @Content(schema = @Schema(implementation = Page.class))
        )
    })
    public ResponseEntity<Page<ProductResponse>> getProductsByCategory(
        @Parameter(description = "Product category", required = true, example = "Electronics")
        @PathVariable String category,
        @Parameter(description = "Page number (0-indexed)", example = "0")
        @RequestParam(defaultValue = "0") int page,
        @Parameter(description = "Page size", example = "20")
        @RequestParam(defaultValue = "20") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        Page<ProductResponse> response = productService.getProductsByCategory(category, pageable);
        return ResponseEntity.ok(response);
    }
    
    @PutMapping("/{productId}")
    @Operation(
        summary = "Update product",
        description = "Updates an existing product's information."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Product updated successfully",
            content = @Content(schema = @Schema(implementation = ProductResponse.class))
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Product not found",
            content = @Content
        )
    })
    public ResponseEntity<ProductResponse> updateProduct(
        @Parameter(description = "Product ID", required = true, example = "1")
        @PathVariable Long productId,
        @Valid @RequestBody ProductRequest request
    ) {
        ProductResponse response = productService.updateProduct(productId, request);
        return ResponseEntity.ok(response);
    }
    
    @DeleteMapping("/{productId}")
    @Operation(
        summary = "Delete product",
        description = "Soft deletes a product by marking it as inactive."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "204",
            description = "Product deleted successfully",
            content = @Content
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Product not found",
            content = @Content
        )
    })
    public ResponseEntity<Void> deleteProduct(
        @Parameter(description = "Product ID", required = true, example = "1")
        @PathVariable Long productId
    ) {
        productService.deleteProduct(productId);
        return ResponseEntity.noContent().build();
    }
}