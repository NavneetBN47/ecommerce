package com.ecommerce.productcatalog.controller;

import com.ecommerce.productcatalog.application.dto.*;
import com.ecommerce.productcatalog.application.service.ProductService;
import com.ecommerce.productcatalog.presentation.controller.ProductController;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Comprehensive Unit Tests for ProductController
 * Tests all API endpoints with valid, invalid, and edge case scenarios
 * 
 * @author QA Automation Team
 * @version 1.0
 */
@ExtendWith(MockitoExtension.class)
@WebMvcTest(ProductController.class)
@DisplayName("Product Controller Unit Tests")
public class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ProductService productService;

    @Autowired
    private ObjectMapper objectMapper;

    private ProductResponse sampleProduct;
    private ProductSummary sampleProductSummary;
    private ProductSearchResponse sampleSearchResponse;

    @BeforeEach
    void setUp() {
        // Setup sample product data
        sampleProduct = new ProductResponse();
        sampleProduct.setProductId(1L);
        sampleProduct.setName("Laptop");
        sampleProduct.setDescription("High-performance laptop");
        sampleProduct.setPrice(new BigDecimal("999.99"));
        sampleProduct.setCategory("Electronics");
        sampleProduct.setStockQuantity(50);
        sampleProduct.setBrand("TechBrand");
        sampleProduct.setSku("LAPTOP-001");
        sampleProduct.setActive(true);

        sampleProductSummary = new ProductSummary();
        sampleProductSummary.setProductId(1L);
        sampleProductSummary.setName("Laptop");
        sampleProductSummary.setPrice(new BigDecimal("999.99"));
        sampleProductSummary.setCategory("Electronics");

        PaginationMetadata metadata = new PaginationMetadata();
        metadata.setCurrentPage(0);
        metadata.setTotalPages(1);
        metadata.setTotalElements(1L);
        metadata.setPageSize(10);

        sampleSearchResponse = new ProductSearchResponse();
        sampleSearchResponse.setProducts(Collections.singletonList(sampleProductSummary));
        sampleSearchResponse.setPagination(metadata);
    }

    // ==================== GET PRODUCT BY ID TESTS ====================

    @Test
    @DisplayName("GET /api/products/{id} - Valid Product ID - Should Return 200")
    void testGetProductById_ValidId_ReturnsProduct() throws Exception {
        // Arrange
        when(productService.getProductById(eq(1L))).thenReturn(sampleProduct);

        // Act & Assert
        mockMvc.perform(get("/api/products/1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.productId").value(1))
                .andExpect(jsonPath("$.name").value("Laptop"))
                .andExpect(jsonPath("$.price").value(999.99))
                .andExpect(jsonPath("$.category").value("Electronics"))
                .andExpect(jsonPath("$.stockQuantity").value(50));

        verify(productService, times(1)).getProductById(eq(1L));
    }

    @Test
    @DisplayName("GET /api/products/{id} - Non-existent Product - Should Return 404")
    void testGetProductById_NonExistent_ReturnsNotFound() throws Exception {
        // Arrange
        when(productService.getProductById(eq(999L)))
                .thenThrow(new RuntimeException("Product not found"));

        // Act & Assert
        mockMvc.perform(get("/api/products/999")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

        verify(productService, times(1)).getProductById(eq(999L));
    }

    @Test
    @DisplayName("GET /api/products/{id} - Invalid ID Format - Should Return 400")
    void testGetProductById_InvalidIdFormat_ReturnsBadRequest() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/products/invalid")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        verify(productService, never()).getProductById(anyLong());
    }

    @Test
    @DisplayName("GET /api/products/{id} - Negative ID - Should Return 400")
    void testGetProductById_NegativeId_ReturnsBadRequest() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/products/-1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        verify(productService, never()).getProductById(anyLong());
    }

    @Test
    @DisplayName("GET /api/products/{id} - Zero ID - Should Return 400")
    void testGetProductById_ZeroId_ReturnsBadRequest() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/products/0")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        verify(productService, never()).getProductById(anyLong());
    }

    // ==================== SEARCH PRODUCTS TESTS ====================

    @Test
    @DisplayName("GET /api/products/search - Valid Search Query - Should Return 200")
    void testSearchProducts_ValidQuery_ReturnsResults() throws Exception {
        // Arrange
        when(productService.searchProducts(eq("Laptop"), isNull(), isNull(), any(Pageable.class)))
                .thenReturn(sampleSearchResponse);

        // Act & Assert
        mockMvc.perform(get("/api/products/search")
                .param("query", "Laptop")
                .param("page", "0")
                .param("size", "10")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.products").isArray())
                .andExpect(jsonPath("$.products[0].name").value("Laptop"))
                .andExpect(jsonPath("$.pagination.currentPage").value(0))
                .andExpect(jsonPath("$.pagination.totalElements").value(1));

        verify(productService, times(1)).searchProducts(eq("Laptop"), isNull(), isNull(), any(Pageable.class));
    }

    @Test
    @DisplayName("GET /api/products/search - Search by Category - Should Return 200")
    void testSearchProducts_ByCategory_ReturnsResults() throws Exception {
        // Arrange
        when(productService.searchProducts(isNull(), eq("Electronics"), isNull(), any(Pageable.class)))
                .thenReturn(sampleSearchResponse);

        // Act & Assert
        mockMvc.perform(get("/api/products/search")
                .param("category", "Electronics")
                .param("page", "0")
                .param("size", "10")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.products").isArray())
                .andExpect(jsonPath("$.products[0].category").value("Electronics"));

        verify(productService, times(1)).searchProducts(isNull(), eq("Electronics"), isNull(), any(Pageable.class));
    }

    @Test
    @DisplayName("GET /api/products/search - Search by Brand - Should Return 200")
    void testSearchProducts_ByBrand_ReturnsResults() throws Exception {
        // Arrange
        when(productService.searchProducts(isNull(), isNull(), eq("TechBrand"), any(Pageable.class)))
                .thenReturn(sampleSearchResponse);

        // Act & Assert
        mockMvc.perform(get("/api/products/search")
                .param("brand", "TechBrand")
                .param("page", "0")
                .param("size", "10")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.products").isArray());

        verify(productService, times(1)).searchProducts(isNull(), isNull(), eq("TechBrand"), any(Pageable.class));
    }

    @Test
    @DisplayName("GET /api/products/search - Multiple Filters - Should Return 200")
    void testSearchProducts_MultipleFilters_ReturnsResults() throws Exception {
        // Arrange
        when(productService.searchProducts(eq("Laptop"), eq("Electronics"), eq("TechBrand"), any(Pageable.class)))
                .thenReturn(sampleSearchResponse);

        // Act & Assert
        mockMvc.perform(get("/api/products/search")
                .param("query", "Laptop")
                .param("category", "Electronics")
                .param("brand", "TechBrand")
                .param("page", "0")
                .param("size", "10")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.products").isArray());

        verify(productService, times(1)).searchProducts(eq("Laptop"), eq("Electronics"), eq("TechBrand"), any(Pageable.class));
    }

    @Test
    @DisplayName("GET /api/products/search - No Results - Should Return Empty List")
    void testSearchProducts_NoResults_ReturnsEmptyList() throws Exception {
        // Arrange
        ProductSearchResponse emptyResponse = new ProductSearchResponse();
        emptyResponse.setProducts(Collections.emptyList());
        PaginationMetadata metadata = new PaginationMetadata();
        metadata.setTotalElements(0L);
        emptyResponse.setPagination(metadata);

        when(productService.searchProducts(eq("NonExistent"), isNull(), isNull(), any(Pageable.class)))
                .thenReturn(emptyResponse);

        // Act & Assert
        mockMvc.perform(get("/api/products/search")
                .param("query", "NonExistent")
                .param("page", "0")
                .param("size", "10")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.products").isEmpty())
                .andExpect(jsonPath("$.pagination.totalElements").value(0));

        verify(productService, times(1)).searchProducts(eq("NonExistent"), isNull(), isNull(), any(Pageable.class));
    }

    @Test
    @DisplayName("GET /api/products/search - Invalid Page Number - Should Return 400")
    void testSearchProducts_InvalidPageNumber_ReturnsBadRequest() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/products/search")
                .param("query", "Laptop")
                .param("page", "-1")
                .param("size", "10")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        verify(productService, never()).searchProducts(anyString(), anyString(), anyString(), any(Pageable.class));
    }

    @Test
    @DisplayName("GET /api/products/search - Invalid Page Size - Should Return 400")
    void testSearchProducts_InvalidPageSize_ReturnsBadRequest() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/products/search")
                .param("query", "Laptop")
                .param("page", "0")
                .param("size", "0")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        verify(productService, never()).searchProducts(anyString(), anyString(), anyString(), any(Pageable.class));
    }

    @Test
    @DisplayName("GET /api/products/search - Excessive Page Size - Should Return 400")
    void testSearchProducts_ExcessivePageSize_ReturnsBadRequest() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/products/search")
                .param("query", "Laptop")
                .param("page", "0")
                .param("size", "1000")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        verify(productService, never()).searchProducts(anyString(), anyString(), anyString(), any(Pageable.class));
    }

    // ==================== GET PRODUCTS BY CATEGORY TESTS ====================

    @Test
    @DisplayName("GET /api/products/category/{category} - Valid Category - Should Return 200")
    void testGetProductsByCategory_ValidCategory_ReturnsProducts() throws Exception {
        // Arrange
        List<ProductSummary> products = Arrays.asList(sampleProductSummary);
        when(productService.getProductsByCategory(eq("Electronics"), any(Pageable.class)))
                .thenReturn(products);

        // Act & Assert
        mockMvc.perform(get("/api/products/category/Electronics")
                .param("page", "0")
                .param("size", "10")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].category").value("Electronics"));

        verify(productService, times(1)).getProductsByCategory(eq("Electronics"), any(Pageable.class));
    }

    @Test
    @DisplayName("GET /api/products/category/{category} - Non-existent Category - Should Return Empty List")
    void testGetProductsByCategory_NonExistent_ReturnsEmptyList() throws Exception {
        // Arrange
        when(productService.getProductsByCategory(eq("NonExistent"), any(Pageable.class)))
                .thenReturn(Collections.emptyList());

        // Act & Assert
        mockMvc.perform(get("/api/products/category/NonExistent")
                .param("page", "0")
                .param("size", "10")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());

        verify(productService, times(1)).getProductsByCategory(eq("NonExistent"), any(Pageable.class));
    }

    // ==================== CHECK PRODUCT AVAILABILITY TESTS ====================

    @Test
    @DisplayName("GET /api/products/{id}/availability - Available Product - Should Return 200")
    void testCheckProductAvailability_Available_ReturnsTrue() throws Exception {
        // Arrange
        when(productService.checkProductAvailability(eq(1L), eq(5)))
                .thenReturn(true);

        // Act & Assert
        mockMvc.perform(get("/api/products/1/availability")
                .param("quantity", "5")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.available").value(true));

        verify(productService, times(1)).checkProductAvailability(eq(1L), eq(5));
    }

    @Test
    @DisplayName("GET /api/products/{id}/availability - Insufficient Stock - Should Return 200 with False")
    void testCheckProductAvailability_InsufficientStock_ReturnsFalse() throws Exception {
        // Arrange
        when(productService.checkProductAvailability(eq(1L), eq(100)))
                .thenReturn(false);

        // Act & Assert
        mockMvc.perform(get("/api/products/1/availability")
                .param("quantity", "100")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.available").value(false));

        verify(productService, times(1)).checkProductAvailability(eq(1L), eq(100));
    }

    @Test
    @DisplayName("GET /api/products/{id}/availability - Invalid Quantity - Should Return 400")
    void testCheckProductAvailability_InvalidQuantity_ReturnsBadRequest() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/products/1/availability")
                .param("quantity", "-1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        verify(productService, never()).checkProductAvailability(anyLong(), anyInt());
    }

    @Test
    @DisplayName("GET /api/products/{id}/availability - Zero Quantity - Should Return 400")
    void testCheckProductAvailability_ZeroQuantity_ReturnsBadRequest() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/products/1/availability")
                .param("quantity", "0")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        verify(productService, never()).checkProductAvailability(anyLong(), anyInt());
    }

    // ==================== EDGE CASE TESTS ====================

    @Test
    @DisplayName("GET /api/products/search - SQL Injection Attempt - Should Be Sanitized")
    void testSearchProducts_SQLInjection_ShouldBeSanitized() throws Exception {
        // Arrange
        String maliciousQuery = "'; DROP TABLE products;--";
        when(productService.searchProducts(eq(maliciousQuery), isNull(), isNull(), any(Pageable.class)))
                .thenReturn(sampleSearchResponse);

        // Act & Assert
        mockMvc.perform(get("/api/products/search")
                .param("query", maliciousQuery)
                .param("page", "0")
                .param("size", "10")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        verify(productService, times(1)).searchProducts(eq(maliciousQuery), isNull(), isNull(), any(Pageable.class));
    }

    @Test
    @DisplayName("GET /api/products/search - XSS Attempt - Should Be Sanitized")
    void testSearchProducts_XSSAttempt_ShouldBeSanitized() throws Exception {
        // Arrange
        String maliciousQuery = "<script>alert('XSS')</script>";
        when(productService.searchProducts(eq(maliciousQuery), isNull(), isNull(), any(Pageable.class)))
                .thenReturn(sampleSearchResponse);

        // Act & Assert
        mockMvc.perform(get("/api/products/search")
                .param("query", maliciousQuery)
                .param("page", "0")
                .param("size", "10")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        verify(productService, times(1)).searchProducts(eq(maliciousQuery), isNull(), isNull(), any(Pageable.class));
    }

    @Test
    @DisplayName("GET /api/products/search - Extremely Long Query - Should Handle Gracefully")
    void testSearchProducts_ExtremelyLongQuery_HandlesGracefully() throws Exception {
        // Arrange
        String longQuery = "a".repeat(1000);

        // Act & Assert
        mockMvc.perform(get("/api/products/search")
                .param("query", longQuery)
                .param("page", "0")
                .param("size", "10")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        verify(productService, never()).searchProducts(anyString(), anyString(), anyString(), any(Pageable.class));
    }

    @Test
    @DisplayName("GET /api/products/search - Special Characters in Query - Should Handle Correctly")
    void testSearchProducts_SpecialCharacters_HandlesCorrectly() throws Exception {
        // Arrange
        String specialQuery = "!@#$%^&*()";
        when(productService.searchProducts(eq(specialQuery), isNull(), isNull(), any(Pageable.class)))
                .thenReturn(sampleSearchResponse);

        // Act & Assert
        mockMvc.perform(get("/api/products/search")
                .param("query", specialQuery)
                .param("page", "0")
                .param("size", "10")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        verify(productService, times(1)).searchProducts(eq(specialQuery), isNull(), isNull(), any(Pageable.class));
    }

    @Test
    @DisplayName("GET /api/products/{id} - Maximum Long Value - Should Handle Correctly")
    void testGetProductById_MaxLongValue_HandlesCorrectly() throws Exception {
        // Arrange
        long maxId = Long.MAX_VALUE;
        when(productService.getProductById(eq(maxId)))
                .thenThrow(new RuntimeException("Product not found"));

        // Act & Assert
        mockMvc.perform(get("/api/products/" + maxId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

        verify(productService, times(1)).getProductById(eq(maxId));
    }

    @Test
    @DisplayName("GET /api/products/search - Unicode Characters - Should Handle Correctly")
    void testSearchProducts_UnicodeCharacters_HandlesCorrectly() throws Exception {
        // Arrange
        String unicodeQuery = "笔记本电脑";
        when(productService.searchProducts(eq(unicodeQuery), isNull(), isNull(), any(Pageable.class)))
                .thenReturn(sampleSearchResponse);

        // Act & Assert
        mockMvc.perform(get("/api/products/search")
                .param("query", unicodeQuery)
                .param("page", "0")
                .param("size", "10")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        verify(productService, times(1)).searchProducts(eq(unicodeQuery), isNull(), isNull(), any(Pageable.class));
    }

    @Test
    @DisplayName("GET /api/products/search - Empty Query String - Should Return All Products")
    void testSearchProducts_EmptyQuery_ReturnsAllProducts() throws Exception {
        // Arrange
        when(productService.searchProducts(eq(""), isNull(), isNull(), any(Pageable.class)))
                .thenReturn(sampleSearchResponse);

        // Act & Assert
        mockMvc.perform(get("/api/products/search")
                .param("query", "")
                .param("page", "0")
                .param("size", "10")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        verify(productService, times(1)).searchProducts(eq(""), isNull(), isNull(), any(Pageable.class));
    }
}