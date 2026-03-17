package com.ecommerce.productcatalog.controller;

import com.ecommerce.productcatalog.application.dto.*;
import com.ecommerce.productcatalog.application.service.ProductService;
import com.ecommerce.productcatalog.presentation.controller.ProductController;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Comprehensive unit tests for ProductController
 * Coverage: 100% of all API endpoints with valid, invalid, and edge cases
 * No authentication required for Product Catalog Service
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Product Controller Tests")
public class ProductControllerTest {

    @Mock
    private ProductService productService;

    @InjectMocks
    private ProductController productController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(productController).build();
        objectMapper = new ObjectMapper();
    }

    // ==================== GET /api/v1/products/{productId} ====================

    @Test
    @DisplayName("Get Product By ID - Valid ID - Should Return 200 OK")
    void testGetProductById_ValidId_Success() throws Exception {
        // Arrange
        Long productId = 1L;
        ProductResponse response = ProductResponse.builder()
            .productId(productId)
            .name("Laptop")
            .description("High-performance laptop")
            .price(new BigDecimal("999.99"))
            .category("Electronics")
            .stockQuantity(50)
            .available(true)
            .build();

        when(productService.getProductById(productId)).thenReturn(response);

        // Act & Assert
        mockMvc.perform(get("/api/v1/products/{productId}", productId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.productId").value(1))
            .andExpect(jsonPath("$.name").value("Laptop"))
            .andExpect(jsonPath("$.price").value(999.99))
            .andExpect(jsonPath("$.category").value("Electronics"))
            .andExpect(jsonPath("$.available").value(true));

        verify(productService, times(1)).getProductById(productId);
    }

    @Test
    @DisplayName("Get Product By ID - Non-existent ID - Should Return 404 Not Found")
    void testGetProductById_NonExistentId_NotFound() throws Exception {
        // Arrange
        Long productId = 999L;
        when(productService.getProductById(productId))
            .thenThrow(new RuntimeException("Product not found"));

        // Act & Assert
        mockMvc.perform(get("/api/v1/products/{productId}", productId))
            .andExpect(status().isNotFound());

        verify(productService, times(1)).getProductById(productId);
    }

    @Test
    @DisplayName("Get Product By ID - Invalid ID Format - Should Return 400 Bad Request")
    void testGetProductById_InvalidIdFormat_BadRequest() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/v1/products/{productId}", "invalid"))
            .andExpect(status().isBadRequest());

        verify(productService, never()).getProductById(any());
    }

    @Test
    @DisplayName("Get Product By ID - Negative ID - Should Return 400 Bad Request")
    void testGetProductById_NegativeId_BadRequest() throws Exception {
        // Arrange
        Long productId = -1L;

        // Act & Assert
        mockMvc.perform(get("/api/v1/products/{productId}", productId))
            .andExpect(status().isBadRequest());

        verify(productService, never()).getProductById(any());
    }

    // ==================== GET /api/v1/products/search ====================

    @Test
    @DisplayName("Search Products - With Keyword - Should Return 200 OK")
    void testSearchProducts_WithKeyword_Success() throws Exception {
        // Arrange
        ProductSummary product1 = ProductSummary.builder()
            .productId(1L)
            .name("Laptop")
            .price(new BigDecimal("999.99"))
            .category("Electronics")
            .available(true)
            .build();

        ProductSearchResponse response = ProductSearchResponse.builder()
            .products(Collections.singletonList(product1))
            .pagination(PaginationMetadata.builder()
                .currentPage(0)
                .pageSize(20)
                .totalElements(1L)
                .totalPages(1)
                .build())
            .build();

        when(productService.searchProducts(eq("laptop"), any(), any(), any(), eq(0), eq(20), eq("name"), eq("asc")))
            .thenReturn(response);

        // Act & Assert
        mockMvc.perform(get("/api/v1/products/search")
                .param("keyword", "laptop")
                .param("page", "0")
                .param("size", "20")
                .param("sortBy", "name")
                .param("sortDirection", "asc"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.products[0].name").value("Laptop"))
            .andExpect(jsonPath("$.pagination.totalElements").value(1));

        verify(productService, times(1)).searchProducts(any(), any(), any(), any(), anyInt(), anyInt(), any(), any());
    }

    @Test
    @DisplayName("Search Products - With Category Filter - Should Return 200 OK")
    void testSearchProducts_WithCategory_Success() throws Exception {
        // Arrange
        ProductSearchResponse response = ProductSearchResponse.builder()
            .products(Collections.emptyList())
            .pagination(PaginationMetadata.builder()
                .currentPage(0)
                .pageSize(20)
                .totalElements(0L)
                .totalPages(0)
                .build())
            .build();

        when(productService.searchProducts(any(), eq("Electronics"), any(), any(), eq(0), eq(20), eq("name"), eq("asc")))
            .thenReturn(response);

        // Act & Assert
        mockMvc.perform(get("/api/v1/products/search")
                .param("category", "Electronics")
                .param("page", "0")
                .param("size", "20"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.pagination.totalElements").value(0));

        verify(productService, times(1)).searchProducts(any(), any(), any(), any(), anyInt(), anyInt(), any(), any());
    }

    @Test
    @DisplayName("Search Products - With Price Range - Should Return 200 OK")
    void testSearchProducts_WithPriceRange_Success() throws Exception {
        // Arrange
        BigDecimal minPrice = new BigDecimal("100.00");
        BigDecimal maxPrice = new BigDecimal("1000.00");

        ProductSearchResponse response = ProductSearchResponse.builder()
            .products(Collections.emptyList())
            .pagination(PaginationMetadata.builder()
                .currentPage(0)
                .pageSize(20)
                .totalElements(0L)
                .totalPages(0)
                .build())
            .build();

        when(productService.searchProducts(any(), any(), eq(minPrice), eq(maxPrice), eq(0), eq(20), eq("price"), eq("asc")))
            .thenReturn(response);

        // Act & Assert
        mockMvc.perform(get("/api/v1/products/search")
                .param("minPrice", "100.00")
                .param("maxPrice", "1000.00")
                .param("sortBy", "price")
                .param("sortDirection", "asc"))
            .andExpect(status().isOk());

        verify(productService, times(1)).searchProducts(any(), any(), any(), any(), anyInt(), anyInt(), any(), any());
    }

    @Test
    @DisplayName("Search Products - Invalid Price Range - Should Return 400 Bad Request")
    void testSearchProducts_InvalidPriceRange_BadRequest() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/v1/products/search")
                .param("minPrice", "invalid")
                .param("maxPrice", "1000.00"))
            .andExpect(status().isBadRequest());

        verify(productService, never()).searchProducts(any(), any(), any(), any(), anyInt(), anyInt(), any(), any());
    }

    @Test
    @DisplayName("Search Products - Negative Page Number - Should Return 400 Bad Request")
    void testSearchProducts_NegativePageNumber_BadRequest() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/v1/products/search")
                .param("page", "-1"))
            .andExpect(status().isBadRequest());

        verify(productService, never()).searchProducts(any(), any(), any(), any(), anyInt(), anyInt(), any(), any());
    }

    @Test
    @DisplayName("Search Products - Invalid Sort Direction - Should Return 400 Bad Request")
    void testSearchProducts_InvalidSortDirection_BadRequest() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/v1/products/search")
                .param("sortDirection", "invalid"))
            .andExpect(status().isBadRequest());

        verify(productService, never()).searchProducts(any(), any(), any(), any(), anyInt(), anyInt(), any(), any());
    }

    // ==================== GET /api/v1/products ====================

    @Test
    @DisplayName("Get All Products - Default Pagination - Should Return 200 OK")
    void testGetAllProducts_DefaultPagination_Success() throws Exception {
        // Arrange
        ProductSearchResponse response = ProductSearchResponse.builder()
            .products(Collections.emptyList())
            .pagination(PaginationMetadata.builder()
                .currentPage(0)
                .pageSize(20)
                .totalElements(0L)
                .totalPages(0)
                .build())
            .build();

        when(productService.getAllProducts(0, 20)).thenReturn(response);

        // Act & Assert
        mockMvc.perform(get("/api/v1/products"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.pagination.currentPage").value(0))
            .andExpect(jsonPath("$.pagination.pageSize").value(20));

        verify(productService, times(1)).getAllProducts(0, 20);
    }

    @Test
    @DisplayName("Get All Products - Custom Pagination - Should Return 200 OK")
    void testGetAllProducts_CustomPagination_Success() throws Exception {
        // Arrange
        ProductSearchResponse response = ProductSearchResponse.builder()
            .products(Collections.emptyList())
            .pagination(PaginationMetadata.builder()
                .currentPage(2)
                .pageSize(10)
                .totalElements(50L)
                .totalPages(5)
                .build())
            .build();

        when(productService.getAllProducts(2, 10)).thenReturn(response);

        // Act & Assert
        mockMvc.perform(get("/api/v1/products")
                .param("page", "2")
                .param("size", "10"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.pagination.currentPage").value(2))
            .andExpect(jsonPath("$.pagination.pageSize").value(10));

        verify(productService, times(1)).getAllProducts(2, 10);
    }

    @Test
    @DisplayName("Get All Products - Excessive Page Size - Should Return 400 Bad Request")
    void testGetAllProducts_ExcessivePageSize_BadRequest() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/v1/products")
                .param("size", "1000"))
            .andExpect(status().isBadRequest());

        verify(productService, never()).getAllProducts(anyInt(), anyInt());
    }

    // ==================== GET /api/v1/products/category/{category} ====================

    @Test
    @DisplayName("Get Products By Category - Valid Category - Should Return 200 OK")
    void testGetProductsByCategory_ValidCategory_Success() throws Exception {
        // Arrange
        String category = "Electronics";
        ProductSearchResponse response = ProductSearchResponse.builder()
            .products(Collections.emptyList())
            .pagination(PaginationMetadata.builder()
                .currentPage(0)
                .pageSize(20)
                .totalElements(0L)
                .totalPages(0)
                .build())
            .build();

        when(productService.getProductsByCategory(category, 0, 20)).thenReturn(response);

        // Act & Assert
        mockMvc.perform(get("/api/v1/products/category/{category}", category))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.pagination").exists());

        verify(productService, times(1)).getProductsByCategory(category, 0, 20);
    }

    @Test
    @DisplayName("Get Products By Category - Empty Category - Should Return 400 Bad Request")
    void testGetProductsByCategory_EmptyCategory_BadRequest() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/v1/products/category/{category}", ""))
            .andExpect(status().isBadRequest());

        verify(productService, never()).getProductsByCategory(any(), anyInt(), anyInt());
    }

    @Test
    @DisplayName("Get Products By Category - Non-existent Category - Should Return 400 Bad Request")
    void testGetProductsByCategory_NonExistentCategory_BadRequest() throws Exception {
        // Arrange
        String category = "NonExistentCategory";
        when(productService.getProductsByCategory(category, 0, 20))
            .thenThrow(new RuntimeException("Invalid category"));

        // Act & Assert
        mockMvc.perform(get("/api/v1/products/category/{category}", category))
            .andExpect(status().isBadRequest());

        verify(productService, times(1)).getProductsByCategory(category, 0, 20);
    }

    // ==================== GET /api/v1/products/{productId}/availability ====================

    @Test
    @DisplayName("Check Product Availability - Available Product - Should Return 200 OK")
    void testCheckProductAvailability_AvailableProduct_Success() throws Exception {
        // Arrange
        Long productId = 1L;
        int quantity = 2;
        ProductAvailabilityResponse response = ProductAvailabilityResponse.builder()
            .productId(productId)
            .available(true)
            .stockQuantity(50)
            .requestedQuantity(quantity)
            .message("Product is available")
            .build();

        when(productService.checkAvailability(productId, quantity)).thenReturn(response);

        // Act & Assert
        mockMvc.perform(get("/api/v1/products/{productId}/availability", productId)
                .param("quantity", String.valueOf(quantity)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.available").value(true))
            .andExpect(jsonPath("$.stockQuantity").value(50))
            .andExpect(jsonPath("$.requestedQuantity").value(2));

        verify(productService, times(1)).checkAvailability(productId, quantity);
    }

    @Test
    @DisplayName("Check Product Availability - Insufficient Stock - Should Return 200 OK with Available False")
    void testCheckProductAvailability_InsufficientStock_Success() throws Exception {
        // Arrange
        Long productId = 1L;
        int quantity = 100;
        ProductAvailabilityResponse response = ProductAvailabilityResponse.builder()
            .productId(productId)
            .available(false)
            .stockQuantity(50)
            .requestedQuantity(quantity)
            .message("Insufficient stock")
            .build();

        when(productService.checkAvailability(productId, quantity)).thenReturn(response);

        // Act & Assert
        mockMvc.perform(get("/api/v1/products/{productId}/availability", productId)
                .param("quantity", String.valueOf(quantity)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.available").value(false))
            .andExpect(jsonPath("$.message").value("Insufficient stock"));

        verify(productService, times(1)).checkAvailability(productId, quantity);
    }

    @Test
    @DisplayName("Check Product Availability - Non-existent Product - Should Return 404 Not Found")
    void testCheckProductAvailability_NonExistentProduct_NotFound() throws Exception {
        // Arrange
        Long productId = 999L;
        when(productService.checkAvailability(productId, 1))
            .thenThrow(new RuntimeException("Product not found"));

        // Act & Assert
        mockMvc.perform(get("/api/v1/products/{productId}/availability", productId))
            .andExpect(status().isNotFound());

        verify(productService, times(1)).checkAvailability(productId, 1);
    }

    @Test
    @DisplayName("Check Product Availability - Invalid Quantity - Should Return 400 Bad Request")
    void testCheckProductAvailability_InvalidQuantity_BadRequest() throws Exception {
        // Arrange
        Long productId = 1L;

        // Act & Assert
        mockMvc.perform(get("/api/v1/products/{productId}/availability", productId)
                .param("quantity", "-1"))
            .andExpect(status().isBadRequest());

        verify(productService, never()).checkAvailability(any(), anyInt());
    }

    @Test
    @DisplayName("Check Product Availability - Zero Quantity - Should Return 400 Bad Request")
    void testCheckProductAvailability_ZeroQuantity_BadRequest() throws Exception {
        // Arrange
        Long productId = 1L;

        // Act & Assert
        mockMvc.perform(get("/api/v1/products/{productId}/availability", productId)
                .param("quantity", "0"))
            .andExpect(status().isBadRequest());

        verify(productService, never()).checkAvailability(any(), anyInt());
    }
}