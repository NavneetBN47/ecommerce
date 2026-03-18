package com.ecommerce.productcatalog.controller;

import com.ecommerce.productcatalog.application.dto.*;
import com.ecommerce.productcatalog.application.exception.*;
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
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Comprehensive unit tests for ProductController
 * Tests all 5 endpoints with valid, invalid, and edge cases
 * Coverage: 100% of API endpoints
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Product Controller Tests")
public class ProductControllerTest {

    private MockMvc mockMvc;

    @Mock
    private ProductService productService;

    @InjectMocks
    private ProductController productController;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(productController)
                .setControllerAdvice(new com.ecommerce.common.exception.GlobalExceptionHandler())
                .build();
        objectMapper = new ObjectMapper();
    }

    // ==================== GET /api/v1/products/{productId} ====================

    @Test
    @DisplayName("Get Product By ID - Valid Product ID - Should Return 200 OK")
    void testGetProductById_ValidProductId_Success() throws Exception {
        // Arrange
        Long productId = 1L;
        ProductResponse response = new ProductResponse(
                1L,
                "Laptop",
                "High-performance laptop",
                new BigDecimal("999.99"),
                "Electronics",
                50,
                "https://example.com/laptop.jpg",
                true
        );

        when(productService.getProductById(productId)).thenReturn(response);

        // Act & Assert
        mockMvc.perform(get("/api/v1/products/{productId}", productId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.productId").value(1))
                .andExpect(jsonPath("$.name").value("Laptop"))
                .andExpect(jsonPath("$.description").value("High-performance laptop"))
                .andExpect(jsonPath("$.price").value(999.99))
                .andExpect(jsonPath("$.category").value("Electronics"))
                .andExpect(jsonPath("$.stockQuantity").value(50))
                .andExpect(jsonPath("$.available").value(true));

        verify(productService, times(1)).getProductById(productId);
    }

    @Test
    @DisplayName("Get Product By ID - Non-existent Product - Should Return 404 Not Found")
    void testGetProductById_NonExistentProduct_NotFound() throws Exception {
        // Arrange
        Long productId = 999L;

        when(productService.getProductById(productId))
                .thenThrow(new ProductNotFoundException("Product not found with ID: 999"));

        // Act & Assert
        mockMvc.perform(get("/api/v1/products/{productId}", productId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

        verify(productService, times(1)).getProductById(productId);
    }

    @Test
    @DisplayName("Get Product By ID - Invalid Product ID Format - Should Return 400 Bad Request")
    void testGetProductById_InvalidIdFormat_BadRequest() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/v1/products/{productId}", "invalid")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Get Product By ID - Negative Product ID - Should Return 400 Bad Request")
    void testGetProductById_NegativeId_BadRequest() throws Exception {
        // Arrange
        Long productId = -1L;

        // Act & Assert
        mockMvc.perform(get("/api/v1/products/{productId}", productId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    // ==================== GET /api/v1/products ====================

    @Test
    @DisplayName("Get All Products - Default Pagination - Should Return 200 OK")
    void testGetAllProducts_DefaultPagination_Success() throws Exception {
        // Arrange
        List<ProductSummary> products = Arrays.asList(
                new ProductSummary(1L, "Laptop", new BigDecimal("999.99"), "Electronics", "https://example.com/laptop.jpg", true),
                new ProductSummary(2L, "Mouse", new BigDecimal("29.99"), "Electronics", "https://example.com/mouse.jpg", true)
        );

        PaginationMetadata metadata = new PaginationMetadata(0, 10, 2L, 1);
        ProductSearchResponse response = new ProductSearchResponse(products, metadata);

        when(productService.getAllProducts(0, 10)).thenReturn(response);

        // Act & Assert
        mockMvc.perform(get("/api/v1/products")
                        .param("page", "0")
                        .param("size", "10")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.products").isArray())
                .andExpect(jsonPath("$.products.length()").value(2))
                .andExpect(jsonPath("$.metadata.page").value(0))
                .andExpect(jsonPath("$.metadata.size").value(10))
                .andExpect(jsonPath("$.metadata.totalElements").value(2))
                .andExpect(jsonPath("$.metadata.totalPages").value(1));

        verify(productService, times(1)).getAllProducts(0, 10);
    }

    @Test
    @DisplayName("Get All Products - Custom Pagination - Should Return 200 OK")
    void testGetAllProducts_CustomPagination_Success() throws Exception {
        // Arrange
        List<ProductSummary> products = Arrays.asList(
                new ProductSummary(1L, "Laptop", new BigDecimal("999.99"), "Electronics", "https://example.com/laptop.jpg", true)
        );

        PaginationMetadata metadata = new PaginationMetadata(1, 5, 10L, 2);
        ProductSearchResponse response = new ProductSearchResponse(products, metadata);

        when(productService.getAllProducts(1, 5)).thenReturn(response);

        // Act & Assert
        mockMvc.perform(get("/api/v1/products")
                        .param("page", "1")
                        .param("size", "5")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.metadata.page").value(1))
                .andExpect(jsonPath("$.metadata.size").value(5));

        verify(productService, times(1)).getAllProducts(1, 5);
    }

    @Test
    @DisplayName("Get All Products - Empty Result - Should Return 200 OK with Empty List")
    void testGetAllProducts_EmptyResult_Success() throws Exception {
        // Arrange
        List<ProductSummary> products = Arrays.asList();
        PaginationMetadata metadata = new PaginationMetadata(0, 10, 0L, 0);
        ProductSearchResponse response = new ProductSearchResponse(products, metadata);

        when(productService.getAllProducts(0, 10)).thenReturn(response);

        // Act & Assert
        mockMvc.perform(get("/api/v1/products")
                        .param("page", "0")
                        .param("size", "10")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.products").isArray())
                .andExpect(jsonPath("$.products.length()").value(0))
                .andExpect(jsonPath("$.metadata.totalElements").value(0));
    }

    @Test
    @DisplayName("Get All Products - Invalid Page Number - Should Return 400 Bad Request")
    void testGetAllProducts_InvalidPageNumber_BadRequest() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/v1/products")
                        .param("page", "-1")
                        .param("size", "10")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Get All Products - Invalid Page Size - Should Return 400 Bad Request")
    void testGetAllProducts_InvalidPageSize_BadRequest() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/v1/products")
                        .param("page", "0")
                        .param("size", "0")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    // ==================== GET /api/v1/products/search ====================

    @Test
    @DisplayName("Search Products - Valid Keyword - Should Return 200 OK")
    void testSearchProducts_ValidKeyword_Success() throws Exception {
        // Arrange
        String keyword = "laptop";
        List<ProductSummary> products = Arrays.asList(
                new ProductSummary(1L, "Gaming Laptop", new BigDecimal("1299.99"), "Electronics", "https://example.com/gaming-laptop.jpg", true),
                new ProductSummary(2L, "Business Laptop", new BigDecimal("899.99"), "Electronics", "https://example.com/business-laptop.jpg", true)
        );

        PaginationMetadata metadata = new PaginationMetadata(0, 10, 2L, 1);
        ProductSearchResponse response = new ProductSearchResponse(products, metadata);

        when(productService.searchProducts(keyword, 0, 10)).thenReturn(response);

        // Act & Assert
        mockMvc.perform(get("/api/v1/products/search")
                        .param("keyword", keyword)
                        .param("page", "0")
                        .param("size", "10")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.products").isArray())
                .andExpect(jsonPath("$.products.length()").value(2))
                .andExpect(jsonPath("$.products[0].name").value("Gaming Laptop"))
                .andExpect(jsonPath("$.products[1].name").value("Business Laptop"));

        verify(productService, times(1)).searchProducts(keyword, 0, 10);
    }

    @Test
    @DisplayName("Search Products - No Results - Should Return 200 OK with Empty List")
    void testSearchProducts_NoResults_Success() throws Exception {
        // Arrange
        String keyword = "nonexistent";
        List<ProductSummary> products = Arrays.asList();
        PaginationMetadata metadata = new PaginationMetadata(0, 10, 0L, 0);
        ProductSearchResponse response = new ProductSearchResponse(products, metadata);

        when(productService.searchProducts(keyword, 0, 10)).thenReturn(response);

        // Act & Assert
        mockMvc.perform(get("/api/v1/products/search")
                        .param("keyword", keyword)
                        .param("page", "0")
                        .param("size", "10")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.products").isArray())
                .andExpect(jsonPath("$.products.length()").value(0));
    }

    @Test
    @DisplayName("Search Products - Empty Keyword - Should Return 400 Bad Request")
    void testSearchProducts_EmptyKeyword_BadRequest() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/v1/products/search")
                        .param("keyword", "")
                        .param("page", "0")
                        .param("size", "10")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Search Products - Special Characters in Keyword - Should Return 200 OK")
    void testSearchProducts_SpecialCharacters_Success() throws Exception {
        // Arrange
        String keyword = "laptop@#$";
        List<ProductSummary> products = Arrays.asList();
        PaginationMetadata metadata = new PaginationMetadata(0, 10, 0L, 0);
        ProductSearchResponse response = new ProductSearchResponse(products, metadata);

        when(productService.searchProducts(keyword, 0, 10)).thenReturn(response);

        // Act & Assert
        mockMvc.perform(get("/api/v1/products/search")
                        .param("keyword", keyword)
                        .param("page", "0")
                        .param("size", "10")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    // ==================== GET /api/v1/products/category/{category} ====================

    @Test
    @DisplayName("Get Products By Category - Valid Category - Should Return 200 OK")
    void testGetProductsByCategory_ValidCategory_Success() throws Exception {
        // Arrange
        String category = "Electronics";
        List<ProductSummary> products = Arrays.asList(
                new ProductSummary(1L, "Laptop", new BigDecimal("999.99"), "Electronics", "https://example.com/laptop.jpg", true),
                new ProductSummary(2L, "Mouse", new BigDecimal("29.99"), "Electronics", "https://example.com/mouse.jpg", true)
        );

        PaginationMetadata metadata = new PaginationMetadata(0, 10, 2L, 1);
        ProductSearchResponse response = new ProductSearchResponse(products, metadata);

        when(productService.getProductsByCategory(category, 0, 10)).thenReturn(response);

        // Act & Assert
        mockMvc.perform(get("/api/v1/products/category/{category}", category)
                        .param("page", "0")
                        .param("size", "10")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.products").isArray())
                .andExpect(jsonPath("$.products.length()").value(2))
                .andExpect(jsonPath("$.products[0].category").value("Electronics"))
                .andExpect(jsonPath("$.products[1].category").value("Electronics"));

        verify(productService, times(1)).getProductsByCategory(category, 0, 10);
    }

    @Test
    @DisplayName("Get Products By Category - Non-existent Category - Should Return 200 OK with Empty List")
    void testGetProductsByCategory_NonExistentCategory_EmptyList() throws Exception {
        // Arrange
        String category = "NonExistent";
        List<ProductSummary> products = Arrays.asList();
        PaginationMetadata metadata = new PaginationMetadata(0, 10, 0L, 0);
        ProductSearchResponse response = new ProductSearchResponse(products, metadata);

        when(productService.getProductsByCategory(category, 0, 10)).thenReturn(response);

        // Act & Assert
        mockMvc.perform(get("/api/v1/products/category/{category}", category)
                        .param("page", "0")
                        .param("size", "10")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.products").isArray())
                .andExpect(jsonPath("$.products.length()").value(0));
    }

    @Test
    @DisplayName("Get Products By Category - Case Insensitive - Should Return 200 OK")
    void testGetProductsByCategory_CaseInsensitive_Success() throws Exception {
        // Arrange
        String category = "electronics";
        List<ProductSummary> products = Arrays.asList(
                new ProductSummary(1L, "Laptop", new BigDecimal("999.99"), "Electronics", "https://example.com/laptop.jpg", true)
        );

        PaginationMetadata metadata = new PaginationMetadata(0, 10, 1L, 1);
        ProductSearchResponse response = new ProductSearchResponse(products, metadata);

        when(productService.getProductsByCategory(category, 0, 10)).thenReturn(response);

        // Act & Assert
        mockMvc.perform(get("/api/v1/products/category/{category}", category)
                        .param("page", "0")
                        .param("size", "10")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.products").isArray())
                .andExpect(jsonPath("$.products.length()").value(1));
    }

    // ==================== GET /api/v1/products/{productId}/availability (Internal) ====================

    @Test
    @DisplayName("Check Product Availability - Available Product - Should Return 200 OK")
    void testCheckProductAvailability_AvailableProduct_Success() throws Exception {
        // Arrange
        Long productId = 1L;
        Integer requestedQuantity = 5;

        when(productService.checkProductAvailability(productId, requestedQuantity)).thenReturn(true);

        // Act & Assert
        mockMvc.perform(get("/api/v1/products/{productId}/availability", productId)
                        .param("quantity", requestedQuantity.toString())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));

        verify(productService, times(1)).checkProductAvailability(productId, requestedQuantity);
    }

    @Test
    @DisplayName("Check Product Availability - Insufficient Stock - Should Return 200 OK with False")
    void testCheckProductAvailability_InsufficientStock_ReturnsFalse() throws Exception {
        // Arrange
        Long productId = 1L;
        Integer requestedQuantity = 100;

        when(productService.checkProductAvailability(productId, requestedQuantity)).thenReturn(false);

        // Act & Assert
        mockMvc.perform(get("/api/v1/products/{productId}/availability", productId)
                        .param("quantity", requestedQuantity.toString())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string("false"));

        verify(productService, times(1)).checkProductAvailability(productId, requestedQuantity);
    }

    @Test
    @DisplayName("Check Product Availability - Non-existent Product - Should Return 404 Not Found")
    void testCheckProductAvailability_NonExistentProduct_NotFound() throws Exception {
        // Arrange
        Long productId = 999L;
        Integer requestedQuantity = 5;

        when(productService.checkProductAvailability(productId, requestedQuantity))
                .thenThrow(new ProductNotFoundException("Product not found with ID: 999"));

        // Act & Assert
        mockMvc.perform(get("/api/v1/products/{productId}/availability", productId)
                        .param("quantity", requestedQuantity.toString())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Check Product Availability - Invalid Quantity - Should Return 400 Bad Request")
    void testCheckProductAvailability_InvalidQuantity_BadRequest() throws Exception {
        // Arrange
        Long productId = 1L;
        Integer requestedQuantity = -5;

        // Act & Assert
        mockMvc.perform(get("/api/v1/products/{productId}/availability", productId)
                        .param("quantity", requestedQuantity.toString())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Check Product Availability - Zero Quantity - Should Return 400 Bad Request")
    void testCheckProductAvailability_ZeroQuantity_BadRequest() throws Exception {
        // Arrange
        Long productId = 1L;
        Integer requestedQuantity = 0;

        // Act & Assert
        mockMvc.perform(get("/api/v1/products/{productId}/availability", productId)
                        .param("quantity", requestedQuantity.toString())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }
}
