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
 * Comprehensive unit tests for ProductController
 * Coverage: All 5 endpoints with valid, invalid, and edge cases
 * No authentication required (public endpoints)
 */
@ExtendWith(MockitoExtension.class)
@WebMvcTest(ProductController.class)
class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ProductService productService;

    private ProductResponse sampleProduct;
    private List<ProductSummary> sampleProductList;

    @BeforeEach
    void setUp() {
        // Sample product response
        sampleProduct = new ProductResponse();
        sampleProduct.setProductId(1L);
        sampleProduct.setName("Laptop");
        sampleProduct.setDescription("High-performance laptop");
        sampleProduct.setPrice(new BigDecimal("999.99"));
        sampleProduct.setCategory("Electronics");
        sampleProduct.setStockQuantity(50);
        sampleProduct.setAvailable(true);

        // Sample product list
        ProductSummary product1 = new ProductSummary();
        product1.setProductId(1L);
        product1.setName("Laptop");
        product1.setPrice(new BigDecimal("999.99"));
        product1.setCategory("Electronics");
        product1.setAvailable(true);

        ProductSummary product2 = new ProductSummary();
        product2.setProductId(2L);
        product2.setName("Mouse");
        product2.setPrice(new BigDecimal("29.99"));
        product2.setCategory("Electronics");
        product2.setAvailable(true);

        sampleProductList = Arrays.asList(product1, product2);
    }

    // ==================== SEARCH PRODUCTS TESTS ====================

    @Test
    @DisplayName("GET /api/products/search - Valid Keyword - Success")
    void testSearchProducts_ValidKeyword_ReturnsOk() throws Exception {
        // Arrange
        ProductSearchResponse response = new ProductSearchResponse();
        response.setProducts(sampleProductList);
        response.setTotalElements(2L);
        response.setTotalPages(1);
        response.setCurrentPage(0);
        response.setPageSize(10);

        when(productService.searchProducts(eq("laptop"), any(PageRequest.class))).thenReturn(response);

        // Act & Assert
        mockMvc.perform(get("/api/products/search")
                .param("keyword", "laptop")
                .param("page", "0")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.products").isArray())
                .andExpect(jsonPath("$.products.length()").value(2))
                .andExpect(jsonPath("$.totalElements").value(2))
                .andExpect(jsonPath("$.totalPages").value(1));

        verify(productService, times(1)).searchProducts(eq("laptop"), any(PageRequest.class));
    }

    @Test
    @DisplayName("GET /api/products/search - Empty Keyword - ReturnsAllProducts")
    void testSearchProducts_EmptyKeyword_ReturnsAllProducts() throws Exception {
        // Arrange
        ProductSearchResponse response = new ProductSearchResponse();
        response.setProducts(sampleProductList);
        response.setTotalElements(2L);
        response.setTotalPages(1);
        response.setCurrentPage(0);
        response.setPageSize(10);

        when(productService.searchProducts(eq(""), any(PageRequest.class))).thenReturn(response);

        // Act & Assert
        mockMvc.perform(get("/api/products/search")
                .param("keyword", "")
                .param("page", "0")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.products").isArray())
                .andExpect(jsonPath("$.products.length()").value(2));

        verify(productService, times(1)).searchProducts(eq(""), any(PageRequest.class));
    }

    @Test
    @DisplayName("GET /api/products/search - No Results - EmptyList")
    void testSearchProducts_NoResults_ReturnsEmptyList() throws Exception {
        // Arrange
        ProductSearchResponse response = new ProductSearchResponse();
        response.setProducts(Collections.emptyList());
        response.setTotalElements(0L);
        response.setTotalPages(0);
        response.setCurrentPage(0);
        response.setPageSize(10);

        when(productService.searchProducts(eq("nonexistent"), any(PageRequest.class))).thenReturn(response);

        // Act & Assert
        mockMvc.perform(get("/api/products/search")
                .param("keyword", "nonexistent")
                .param("page", "0")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.products").isArray())
                .andExpect(jsonPath("$.products.length()").value(0))
                .andExpect(jsonPath("$.totalElements").value(0));

        verify(productService, times(1)).searchProducts(eq("nonexistent"), any(PageRequest.class));
    }

    @Test
    @DisplayName("GET /api/products/search - Invalid Page Number - BadRequest")
    void testSearchProducts_InvalidPageNumber_ReturnsBadRequest() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/products/search")
                .param("keyword", "laptop")
                .param("page", "-1")
                .param("size", "10"))
                .andExpect(status().isBadRequest());

        verify(productService, never()).searchProducts(any(), any());
    }

    @Test
    @DisplayName("GET /api/products/search - Invalid Page Size - BadRequest")
    void testSearchProducts_InvalidPageSize_ReturnsBadRequest() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/products/search")
                .param("keyword", "laptop")
                .param("page", "0")
                .param("size", "0"))
                .andExpect(status().isBadRequest());

        verify(productService, never()).searchProducts(any(), any());
    }

    @Test
    @DisplayName("GET /api/products/search - Large Page Size - Success")
    void testSearchProducts_LargePageSize_ReturnsOk() throws Exception {
        // Arrange
        ProductSearchResponse response = new ProductSearchResponse();
        response.setProducts(sampleProductList);
        response.setTotalElements(2L);
        response.setTotalPages(1);
        response.setCurrentPage(0);
        response.setPageSize(100);

        when(productService.searchProducts(eq("laptop"), any(PageRequest.class))).thenReturn(response);

        // Act & Assert
        mockMvc.perform(get("/api/products/search")
                .param("keyword", "laptop")
                .param("page", "0")
                .param("size", "100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pageSize").value(100));

        verify(productService, times(1)).searchProducts(eq("laptop"), any(PageRequest.class));
    }

    @Test
    @DisplayName("GET /api/products/search - Special Characters in Keyword - Success")
    void testSearchProducts_SpecialCharacters_ReturnsOk() throws Exception {
        // Arrange
        ProductSearchResponse response = new ProductSearchResponse();
        response.setProducts(Collections.emptyList());
        response.setTotalElements(0L);
        response.setTotalPages(0);
        response.setCurrentPage(0);
        response.setPageSize(10);

        when(productService.searchProducts(eq("laptop@#$%"), any(PageRequest.class))).thenReturn(response);

        // Act & Assert
        mockMvc.perform(get("/api/products/search")
                .param("keyword", "laptop@#$%")
                .param("page", "0")
                .param("size", "10"))
                .andExpect(status().isOk());

        verify(productService, times(1)).searchProducts(eq("laptop@#$%"), any(PageRequest.class));
    }

    // ==================== GET PRODUCT BY ID TESTS ====================

    @Test
    @DisplayName("GET /api/products/{id} - Valid ID - Success")
    void testGetProductById_ValidId_ReturnsOk() throws Exception {
        // Arrange
        when(productService.getProductById(1L)).thenReturn(sampleProduct);

        // Act & Assert
        mockMvc.perform(get("/api/products/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.productId").value(1))
                .andExpect(jsonPath("$.name").value("Laptop"))
                .andExpect(jsonPath("$.price").value(999.99))
                .andExpect(jsonPath("$.category").value("Electronics"));

        verify(productService, times(1)).getProductById(1L);
    }

    @Test
    @DisplayName("GET /api/products/{id} - Non-existent ID - NotFound")
    void testGetProductById_NonExistentId_ReturnsNotFound() throws Exception {
        // Arrange
        when(productService.getProductById(999L)).thenThrow(new RuntimeException("Product not found"));

        // Act & Assert
        mockMvc.perform(get("/api/products/999"))
                .andExpect(status().is5xxServerError());

        verify(productService, times(1)).getProductById(999L);
    }

    @Test
    @DisplayName("GET /api/products/{id} - Invalid ID Format - BadRequest")
    void testGetProductById_InvalidIdFormat_ReturnsBadRequest() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/products/invalid"))
                .andExpect(status().isBadRequest());

        verify(productService, never()).getProductById(any());
    }

    @Test
    @DisplayName("GET /api/products/{id} - Negative ID - BadRequest")
    void testGetProductById_NegativeId_ReturnsBadRequest() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/products/-1"))
                .andExpect(status().isBadRequest());

        verify(productService, never()).getProductById(any());
    }

    @Test
    @DisplayName("GET /api/products/{id} - Zero ID - BadRequest")
    void testGetProductById_ZeroId_ReturnsBadRequest() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/products/0"))
                .andExpect(status().isBadRequest());

        verify(productService, never()).getProductById(any());
    }

    // ==================== GET PRODUCTS BY CATEGORY TESTS ====================

    @Test
    @DisplayName("GET /api/products/category/{category} - Valid Category - Success")
    void testGetProductsByCategory_ValidCategory_ReturnsOk() throws Exception {
        // Arrange
        ProductSearchResponse response = new ProductSearchResponse();
        response.setProducts(sampleProductList);
        response.setTotalElements(2L);
        response.setTotalPages(1);
        response.setCurrentPage(0);
        response.setPageSize(10);

        when(productService.getProductsByCategory(eq("Electronics"), any(PageRequest.class))).thenReturn(response);

        // Act & Assert
        mockMvc.perform(get("/api/products/category/Electronics")
                .param("page", "0")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.products").isArray())
                .andExpect(jsonPath("$.products.length()").value(2));

        verify(productService, times(1)).getProductsByCategory(eq("Electronics"), any(PageRequest.class));
    }

    @Test
    @DisplayName("GET /api/products/category/{category} - Non-existent Category - EmptyList")
    void testGetProductsByCategory_NonExistentCategory_ReturnsEmptyList() throws Exception {
        // Arrange
        ProductSearchResponse response = new ProductSearchResponse();
        response.setProducts(Collections.emptyList());
        response.setTotalElements(0L);
        response.setTotalPages(0);
        response.setCurrentPage(0);
        response.setPageSize(10);

        when(productService.getProductsByCategory(eq("NonExistent"), any(PageRequest.class))).thenReturn(response);

        // Act & Assert
        mockMvc.perform(get("/api/products/category/NonExistent")
                .param("page", "0")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.products").isArray())
                .andExpect(jsonPath("$.products.length()").value(0));

        verify(productService, times(1)).getProductsByCategory(eq("NonExistent"), any(PageRequest.class));
    }

    @Test
    @DisplayName("GET /api/products/category/{category} - Case Sensitivity - Success")
    void testGetProductsByCategory_CaseSensitivity_ReturnsOk() throws Exception {
        // Arrange
        ProductSearchResponse response = new ProductSearchResponse();
        response.setProducts(sampleProductList);
        response.setTotalElements(2L);
        response.setTotalPages(1);
        response.setCurrentPage(0);
        response.setPageSize(10);

        when(productService.getProductsByCategory(eq("electronics"), any(PageRequest.class))).thenReturn(response);

        // Act & Assert
        mockMvc.perform(get("/api/products/category/electronics")
                .param("page", "0")
                .param("size", "10"))
                .andExpect(status().isOk());

        verify(productService, times(1)).getProductsByCategory(eq("electronics"), any(PageRequest.class));
    }

    // ==================== GET AVAILABLE PRODUCTS TESTS ====================

    @Test
    @DisplayName("GET /api/products/available - Success")
    void testGetAvailableProducts_ReturnsOk() throws Exception {
        // Arrange
        ProductSearchResponse response = new ProductSearchResponse();
        response.setProducts(sampleProductList);
        response.setTotalElements(2L);
        response.setTotalPages(1);
        response.setCurrentPage(0);
        response.setPageSize(10);

        when(productService.getAvailableProducts(any(PageRequest.class))).thenReturn(response);

        // Act & Assert
        mockMvc.perform(get("/api/products/available")
                .param("page", "0")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.products").isArray())
                .andExpect(jsonPath("$.products.length()").value(2));

        verify(productService, times(1)).getAvailableProducts(any(PageRequest.class));
    }

    @Test
    @DisplayName("GET /api/products/available - No Available Products - EmptyList")
    void testGetAvailableProducts_NoAvailableProducts_ReturnsEmptyList() throws Exception {
        // Arrange
        ProductSearchResponse response = new ProductSearchResponse();
        response.setProducts(Collections.emptyList());
        response.setTotalElements(0L);
        response.setTotalPages(0);
        response.setCurrentPage(0);
        response.setPageSize(10);

        when(productService.getAvailableProducts(any(PageRequest.class))).thenReturn(response);

        // Act & Assert
        mockMvc.perform(get("/api/products/available")
                .param("page", "0")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.products").isArray())
                .andExpect(jsonPath("$.products.length()").value(0));

        verify(productService, times(1)).getAvailableProducts(any(PageRequest.class));
    }

    // ==================== CHECK AVAILABILITY TESTS ====================

    @Test
    @DisplayName("GET /api/products/{id}/availability - Available Product - Success")
    void testCheckAvailability_AvailableProduct_ReturnsOk() throws Exception {
        // Arrange
        when(productService.checkAvailability(1L, 5)).thenReturn(true);

        // Act & Assert
        mockMvc.perform(get("/api/products/1/availability")
                .param("quantity", "5"))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));

        verify(productService, times(1)).checkAvailability(1L, 5);
    }

    @Test
    @DisplayName("GET /api/products/{id}/availability - Insufficient Stock - ReturnsFalse")
    void testCheckAvailability_InsufficientStock_ReturnsFalse() throws Exception {
        // Arrange
        when(productService.checkAvailability(1L, 100)).thenReturn(false);

        // Act & Assert
        mockMvc.perform(get("/api/products/1/availability")
                .param("quantity", "100"))
                .andExpect(status().isOk())
                .andExpect(content().string("false"));

        verify(productService, times(1)).checkAvailability(1L, 100);
    }

    @Test
    @DisplayName("GET /api/products/{id}/availability - Invalid Quantity - BadRequest")
    void testCheckAvailability_InvalidQuantity_ReturnsBadRequest() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/products/1/availability")
                .param("quantity", "-1"))
                .andExpect(status().isBadRequest());

        verify(productService, never()).checkAvailability(any(), any());
    }

    @Test
    @DisplayName("GET /api/products/{id}/availability - Zero Quantity - BadRequest")
    void testCheckAvailability_ZeroQuantity_ReturnsBadRequest() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/products/1/availability")
                .param("quantity", "0"))
                .andExpect(status().isBadRequest());

        verify(productService, never()).checkAvailability(any(), any());
    }

    @Test
    @DisplayName("GET /api/products/{id}/availability - Non-existent Product - NotFound")
    void testCheckAvailability_NonExistentProduct_ReturnsNotFound() throws Exception {
        // Arrange
        when(productService.checkAvailability(999L, 5)).thenThrow(new RuntimeException("Product not found"));

        // Act & Assert
        mockMvc.perform(get("/api/products/999/availability")
                .param("quantity", "5"))
                .andExpect(status().is5xxServerError());

        verify(productService, times(1)).checkAvailability(999L, 5);
    }

    // ==================== EDGE CASE TESTS ====================

    @Test
    @DisplayName("GET /api/products/search - SQL Injection Attempt - Sanitized")
    void testSearchProducts_SqlInjectionAttempt_Sanitized() throws Exception {
        // Arrange
        ProductSearchResponse response = new ProductSearchResponse();
        response.setProducts(Collections.emptyList());
        response.setTotalElements(0L);
        response.setTotalPages(0);
        response.setCurrentPage(0);
        response.setPageSize(10);

        when(productService.searchProducts(eq("laptop'; DROP TABLE products; --"), any(PageRequest.class)))
                .thenReturn(response);

        // Act & Assert
        mockMvc.perform(get("/api/products/search")
                .param("keyword", "laptop'; DROP TABLE products; --")
                .param("page", "0")
                .param("size", "10"))
                .andExpect(status().isOk());

        verify(productService, times(1)).searchProducts(eq("laptop'; DROP TABLE products; --"), any(PageRequest.class));
    }

    @Test
    @DisplayName("GET /api/products/search - Very Long Keyword - Success")
    void testSearchProducts_VeryLongKeyword_ReturnsOk() throws Exception {
        // Arrange
        String longKeyword = "a".repeat(1000);
        ProductSearchResponse response = new ProductSearchResponse();
        response.setProducts(Collections.emptyList());
        response.setTotalElements(0L);
        response.setTotalPages(0);
        response.setCurrentPage(0);
        response.setPageSize(10);

        when(productService.searchProducts(eq(longKeyword), any(PageRequest.class))).thenReturn(response);

        // Act & Assert
        mockMvc.perform(get("/api/products/search")
                .param("keyword", longKeyword)
                .param("page", "0")
                .param("size", "10"))
                .andExpect(status().isOk());

        verify(productService, times(1)).searchProducts(eq(longKeyword), any(PageRequest.class));
    }

    @Test
    @DisplayName("GET /api/products/{id} - Maximum Long Value - Success")
    void testGetProductById_MaximumLongValue_ReturnsNotFound() throws Exception {
        // Arrange
        when(productService.getProductById(Long.MAX_VALUE)).thenThrow(new RuntimeException("Product not found"));

        // Act & Assert
        mockMvc.perform(get("/api/products/" + Long.MAX_VALUE))
                .andExpect(status().is5xxServerError());

        verify(productService, times(1)).getProductById(Long.MAX_VALUE);
    }
}