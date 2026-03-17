package com.ecommerce.productcatalog.presentation.controller;

import com.ecommerce.productcatalog.application.dto.*;
import com.ecommerce.productcatalog.application.service.ProductService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ProductController.class)
@ExtendWith(MockitoExtension.class)
@DisplayName("Product Controller Tests")
class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ProductService productService;

    private ProductResponse productResponse;
    private ProductSearchResponse searchResponse;
    private UUID productId;

    @BeforeEach
    void setUp() {
        productId = UUID.randomUUID();

        // Setup product response
        productResponse = new ProductResponse(
            productId,
            "Dell XPS 15 Laptop",
            "High-performance laptop with Intel i7 processor, 16GB RAM, 512GB SSD",
            new BigDecimal("1299.99"),
            50,
            true,
            LocalDateTime.now(),
            LocalDateTime.now()
        );

        // Setup product summaries
        ProductSummary summary1 = new ProductSummary(
            UUID.randomUUID(),
            "Dell XPS 15 Laptop",
            new BigDecimal("1299.99"),
            true
        );

        ProductSummary summary2 = new ProductSummary(
            UUID.randomUUID(),
            "HP Pavilion Laptop",
            new BigDecimal("899.99"),
            true
        );

        // Setup pagination metadata
        PaginationMetadata pagination = new PaginationMetadata(
            0,
            20,
            2,
            1,
            false,
            false
        );

        // Setup search response
        searchResponse = new ProductSearchResponse(
            Arrays.asList(summary1, summary2),
            pagination
        );
    }

    // ==================== SEARCH PRODUCTS TESTS ====================

    @Test
    @DisplayName("GET /api/v1/products/search - Valid Keyword - Should Return 200")
    void testSearchProducts_ValidKeyword_ShouldReturn200() throws Exception {
        when(productService.searchProducts(eq("laptop"), eq(0), eq(20)))
            .thenReturn(searchResponse);

        mockMvc.perform(get("/api/v1/products/search")
                .param("keyword", "laptop")
                .param("page", "0")
                .param("size", "20"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.products").isArray())
            .andExpect(jsonPath("$.products.length()").value(2))
            .andExpect(jsonPath("$.products[0].name").value("Dell XPS 15 Laptop"))
            .andExpect(jsonPath("$.products[0].price").value(1299.99))
            .andExpect(jsonPath("$.products[0].available").value(true))
            .andExpect(jsonPath("$.pagination.currentPage").value(0))
            .andExpect(jsonPath("$.pagination.pageSize").value(20))
            .andExpect(jsonPath("$.pagination.totalItems").value(2))
            .andExpect(jsonPath("$.pagination.totalPages").value(1));
    }

    @Test
    @DisplayName("GET /api/v1/products/search - Default Pagination - Should Return 200")
    void testSearchProducts_DefaultPagination_ShouldReturn200() throws Exception {
        when(productService.searchProducts(eq("laptop"), eq(0), eq(20)))
            .thenReturn(searchResponse);

        mockMvc.perform(get("/api/v1/products/search")
                .param("keyword", "laptop"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.products").isArray())
            .andExpect(jsonPath("$.pagination.currentPage").value(0))
            .andExpect(jsonPath("$.pagination.pageSize").value(20));
    }

    @Test
    @DisplayName("GET /api/v1/products/search - Custom Pagination - Should Return 200")
    void testSearchProducts_CustomPagination_ShouldReturn200() throws Exception {
        PaginationMetadata customPagination = new PaginationMetadata(
            2,
            10,
            25,
            3,
            false,
            true
        );

        ProductSearchResponse customResponse = new ProductSearchResponse(
            Collections.singletonList(new ProductSummary(
                UUID.randomUUID(),
                "Dell XPS 15 Laptop",
                new BigDecimal("1299.99"),
                true
            )),
            customPagination
        );

        when(productService.searchProducts(eq("laptop"), eq(2), eq(10)))
            .thenReturn(customResponse);

        mockMvc.perform(get("/api/v1/products/search")
                .param("keyword", "laptop")
                .param("page", "2")
                .param("size", "10"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.pagination.currentPage").value(2))
            .andExpect(jsonPath("$.pagination.pageSize").value(10))
            .andExpect(jsonPath("$.pagination.hasNext").value(false))
            .andExpect(jsonPath("$.pagination.hasPrevious").value(true));
    }

    @Test
    @DisplayName("GET /api/v1/products/search - No Results - Should Return 200 with Empty List")
    void testSearchProducts_NoResults_ShouldReturn200WithEmptyList() throws Exception {
        PaginationMetadata emptyPagination = new PaginationMetadata(
            0,
            20,
            0,
            0,
            false,
            false
        );

        ProductSearchResponse emptyResponse = new ProductSearchResponse(
            Collections.emptyList(),
            emptyPagination
        );

        when(productService.searchProducts(eq("nonexistent"), eq(0), eq(20)))
            .thenReturn(emptyResponse);

        mockMvc.perform(get("/api/v1/products/search")
                .param("keyword", "nonexistent"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.products").isArray())
            .andExpect(jsonPath("$.products.length()").value(0))
            .andExpect(jsonPath("$.pagination.totalItems").value(0));
    }

    @Test
    @DisplayName("GET /api/v1/products/search - Missing Keyword - Should Return 400")
    void testSearchProducts_MissingKeyword_ShouldReturn400() throws Exception {
        mockMvc.perform(get("/api/v1/products/search"))
            .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /api/v1/products/search - Negative Page Number - Should Return 400")
    void testSearchProducts_NegativePageNumber_ShouldReturn400() throws Exception {
        mockMvc.perform(get("/api/v1/products/search")
                .param("keyword", "laptop")
                .param("page", "-1"))
            .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /api/v1/products/search - Negative Page Size - Should Return 400")
    void testSearchProducts_NegativePageSize_ShouldReturn400() throws Exception {
        mockMvc.perform(get("/api/v1/products/search")
                .param("keyword", "laptop")
                .param("size", "-1"))
            .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /api/v1/products/search - Page Size Exceeds Maximum - Should Return 400")
    void testSearchProducts_PageSizeExceedsMaximum_ShouldReturn400() throws Exception {
        mockMvc.perform(get("/api/v1/products/search")
                .param("keyword", "laptop")
                .param("size", "101"))
            .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /api/v1/products/search - Special Characters in Keyword - Should Return 200")
    void testSearchProducts_SpecialCharactersInKeyword_ShouldReturn200() throws Exception {
        when(productService.searchProducts(eq("laptop & accessories"), eq(0), eq(20)))
            .thenReturn(searchResponse);

        mockMvc.perform(get("/api/v1/products/search")
                .param("keyword", "laptop & accessories"))
            .andExpect(status().isOk());
    }

    // ==================== GET PRODUCT BY ID TESTS ====================

    @Test
    @DisplayName("GET /api/v1/products/{productId} - Valid Product ID - Should Return 200")
    void testGetProductById_ValidProductId_ShouldReturn200() throws Exception {
        when(productService.getProductById(eq(productId)))
            .thenReturn(productResponse);

        mockMvc.perform(get("/api/v1/products/{productId}", productId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.productId").value(productId.toString()))
            .andExpect(jsonPath("$.name").value("Dell XPS 15 Laptop"))
            .andExpect(jsonPath("$.description").value("High-performance laptop with Intel i7 processor, 16GB RAM, 512GB SSD"))
            .andExpect(jsonPath("$.price").value(1299.99))
            .andExpect(jsonPath("$.stockQuantity").value(50))
            .andExpect(jsonPath("$.available").value(true))
            .andExpect(jsonPath("$.createdAt").exists())
            .andExpect(jsonPath("$.updatedAt").exists());
    }

    @Test
    @DisplayName("GET /api/v1/products/{productId} - Product Not Found - Should Return 404")
    void testGetProductById_ProductNotFound_ShouldReturn404() throws Exception {
        UUID nonExistentId = UUID.randomUUID();
        when(productService.getProductById(eq(nonExistentId)))
            .thenThrow(new RuntimeException("Product not found"));

        mockMvc.perform(get("/api/v1/products/{productId}", nonExistentId))
            .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("GET /api/v1/products/{productId} - Invalid UUID Format - Should Return 400")
    void testGetProductById_InvalidUuidFormat_ShouldReturn400() throws Exception {
        mockMvc.perform(get("/api/v1/products/{productId}", "invalid-uuid"))
            .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /api/v1/products/{productId} - Out of Stock Product - Should Return 200")
    void testGetProductById_OutOfStockProduct_ShouldReturn200() throws Exception {
        ProductResponse outOfStockProduct = new ProductResponse(
            productId,
            "Dell XPS 15 Laptop",
            "High-performance laptop with Intel i7 processor, 16GB RAM, 512GB SSD",
            new BigDecimal("1299.99"),
            0,
            false,
            LocalDateTime.now(),
            LocalDateTime.now()
        );

        when(productService.getProductById(eq(productId)))
            .thenReturn(outOfStockProduct);

        mockMvc.perform(get("/api/v1/products/{productId}", productId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.stockQuantity").value(0))
            .andExpect(jsonPath("$.available").value(false));
    }

    @Test
    @DisplayName("GET /api/v1/products/{productId} - Product with Zero Price - Should Return 200")
    void testGetProductById_ProductWithZeroPrice_ShouldReturn200() throws Exception {
        ProductResponse freeProduct = new ProductResponse(
            productId,
            "Free Sample Product",
            "Free product for testing",
            BigDecimal.ZERO,
            100,
            true,
            LocalDateTime.now(),
            LocalDateTime.now()
        );

        when(productService.getProductById(eq(productId)))
            .thenReturn(freeProduct);

        mockMvc.perform(get("/api/v1/products/{productId}", productId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.price").value(0));
    }
}