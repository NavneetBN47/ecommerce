package com.ecommerce.productcatalog.presentation.controller;

import com.ecommerce.productcatalog.application.dto.*;
import com.ecommerce.productcatalog.application.service.ProductService;
import com.ecommerce.productcatalog.application.exception.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ProductController.class)
@DisplayName("Product Controller Tests")
class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ProductService productService;

    private ProductResponse productResponse;
    private ProductSummary productSummary;

    @BeforeEach
    void setUp() {
        productResponse = new ProductResponse();
        productResponse.setProductId(1L);
        productResponse.setName("Test Product");
        productResponse.setDescription("Test Description");
        productResponse.setPrice(new BigDecimal("99.99"));
        productResponse.setCategory("Electronics");
        productResponse.setStockQuantity(100);
        productResponse.setIsAvailable(true);

        productSummary = new ProductSummary();
        productSummary.setProductId(1L);
        productSummary.setName("Test Product");
        productSummary.setPrice(new BigDecimal("99.99"));
        productSummary.setCategory("Electronics");
    }

    @Test
    @DisplayName("GET /api/v1/products/search - Success")
    void testSearchProducts_Success() throws Exception {
        List<ProductSummary> products = Arrays.asList(productSummary);
        PaginationMetadata metadata = new PaginationMetadata(1, 10, 1L, 1);
        ProductSearchResponse response = new ProductSearchResponse(products, metadata);

        when(productService.searchProducts(anyString(), any(PageRequest.class))).thenReturn(response);

        mockMvc.perform(get("/api/v1/products/search")
                .param("query", "test")
                .param("page", "0")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.products").isArray())
                .andExpect(jsonPath("$.products[0].productId").value(1))
                .andExpect(jsonPath("$.products[0].name").value("Test Product"))
                .andExpect(jsonPath("$.metadata.currentPage").value(1))
                .andExpect(jsonPath("$.metadata.totalItems").value(1));

        verify(productService, times(1)).searchProducts(anyString(), any(PageRequest.class));
    }

    @Test
    @DisplayName("GET /api/v1/products/search - Empty Query")
    void testSearchProducts_EmptyQuery() throws Exception {
        mockMvc.perform(get("/api/v1/products/search")
                .param("query", "")
                .param("page", "0")
                .param("size", "10"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /api/v1/products/search - No Results")
    void testSearchProducts_NoResults() throws Exception {
        ProductSearchResponse response = new ProductSearchResponse(
                Arrays.asList(),
                new PaginationMetadata(1, 10, 0L, 0)
        );

        when(productService.searchProducts(anyString(), any(PageRequest.class))).thenReturn(response);

        mockMvc.perform(get("/api/v1/products/search")
                .param("query", "nonexistent")
                .param("page", "0")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.products").isEmpty())
                .andExpect(jsonPath("$.metadata.totalItems").value(0));
    }

    @Test
    @DisplayName("GET /api/v1/products/search - Invalid Pagination")
    void testSearchProducts_InvalidPagination() throws Exception {
        mockMvc.perform(get("/api/v1/products/search")
                .param("query", "test")
                .param("page", "-1")
                .param("size", "0"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /api/v1/products/{productId} - Success")
    void testGetProductById_Success() throws Exception {
        when(productService.getProductById(anyLong())).thenReturn(productResponse);

        mockMvc.perform(get("/api/v1/products/{productId}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.productId").value(1))
                .andExpect(jsonPath("$.name").value("Test Product"))
                .andExpect(jsonPath("$.price").value(99.99))
                .andExpect(jsonPath("$.category").value("Electronics"))
                .andExpect(jsonPath("$.stockQuantity").value(100));

        verify(productService, times(1)).getProductById(1L);
    }

    @Test
    @DisplayName("GET /api/v1/products/{productId} - Product Not Found")
    void testGetProductById_NotFound() throws Exception {
        when(productService.getProductById(anyLong()))
                .thenThrow(new ProductNotFoundException("Product not found with ID: 999"));

        mockMvc.perform(get("/api/v1/products/{productId}", 999L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Product not found with ID: 999"));
    }

    @Test
    @DisplayName("GET /api/v1/products/{productId} - Invalid Product ID")
    void testGetProductById_InvalidId() throws Exception {
        mockMvc.perform(get("/api/v1/products/{productId}", "invalid"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /api/v1/products/category/{category} - Success")
    void testGetProductsByCategory_Success() throws Exception {
        List<ProductSummary> products = Arrays.asList(productSummary);
        PaginationMetadata metadata = new PaginationMetadata(1, 10, 1L, 1);
        ProductSearchResponse response = new ProductSearchResponse(products, metadata);

        when(productService.getProductsByCategory(anyString(), any(PageRequest.class))).thenReturn(response);

        mockMvc.perform(get("/api/v1/products/category/{category}", "Electronics")
                .param("page", "0")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.products").isArray())
                .andExpect(jsonPath("$.products[0].category").value("Electronics"));

        verify(productService, times(1)).getProductsByCategory(anyString(), any(PageRequest.class));
    }

    @Test
    @DisplayName("GET /api/v1/products/category/{category} - Empty Category")
    void testGetProductsByCategory_EmptyCategory() throws Exception {
        mockMvc.perform(get("/api/v1/products/category/{category}", "")
                .param("page", "0")
                .param("size", "10"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("GET /api/v1/products/available - Success")
    void testGetAvailableProducts_Success() throws Exception {
        List<ProductSummary> products = Arrays.asList(productSummary);
        PaginationMetadata metadata = new PaginationMetadata(1, 10, 1L, 1);
        ProductSearchResponse response = new ProductSearchResponse(products, metadata);

        when(productService.getAvailableProducts(any(PageRequest.class))).thenReturn(response);

        mockMvc.perform(get("/api/v1/products/available")
                .param("page", "0")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.products").isArray())
                .andExpect(jsonPath("$.products[0].productId").value(1));

        verify(productService, times(1)).getAvailableProducts(any(PageRequest.class));
    }

    @Test
    @DisplayName("GET /api/v1/products/available - Large Page Size")
    void testGetAvailableProducts_LargePageSize() throws Exception {
        mockMvc.perform(get("/api/v1/products/available")
                .param("page", "0")
                .param("size", "1000"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Product Service - Cache Hit")
    void testProductCache_Hit() throws Exception {
        when(productService.getProductById(anyLong())).thenReturn(productResponse);

        mockMvc.perform(get("/api/v1/products/{productId}", 1L))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/v1/products/{productId}", 1L))
                .andExpect(status().isOk());

        verify(productService, times(2)).getProductById(1L);
    }

    @Test
    @DisplayName("Product Search - Special Characters")
    void testSearchProducts_SpecialCharacters() throws Exception {
        ProductSearchResponse response = new ProductSearchResponse(
                Arrays.asList(),
                new PaginationMetadata(1, 10, 0L, 0)
        );

        when(productService.searchProducts(anyString(), any(PageRequest.class))).thenReturn(response);

        mockMvc.perform(get("/api/v1/products/search")
                .param("query", "test@#$%")
                .param("page", "0")
                .param("size", "10"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Product Search - SQL Injection Attempt")
    void testSearchProducts_SQLInjection() throws Exception {
        ProductSearchResponse response = new ProductSearchResponse(
                Arrays.asList(),
                new PaginationMetadata(1, 10, 0L, 0)
        );

        when(productService.searchProducts(anyString(), any(PageRequest.class))).thenReturn(response);

        mockMvc.perform(get("/api/v1/products/search")
                .param("query", "'; DROP TABLE products; --")
                .param("page", "0")
                .param("size", "10"))
                .andExpect(status().isOk());

        verify(productService, times(1)).searchProducts(anyString(), any(PageRequest.class));
    }
}