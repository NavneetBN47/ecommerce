package com.ecommerce.productcatalog.presentation.controller;

import com.ecommerce.productcatalog.application.dto.*;
import com.ecommerce.productcatalog.application.exception.ProductNotAvailableException;
import com.ecommerce.productcatalog.application.exception.ProductNotFoundException;
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
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Comprehensive test suite for ProductController
 * Tests all endpoints with valid, invalid, and edge case scenarios
 * Coverage: 100% of API endpoints
 */
@WebMvcTest(ProductController.class)
@ExtendWith(MockitoExtension.class)
class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ProductService productService;

    @Autowired
    private ObjectMapper objectMapper;

    private ProductResponse productResponse1;
    private ProductResponse productResponse2;
    private ProductSearchResponse searchResponse;
    private PaginationMetadata paginationMetadata;

    @BeforeEach
    void setUp() {
        // Setup product responses
        productResponse1 = ProductResponse.builder()
                .productId(1L)
                .name("Laptop")
                .description("High-performance laptop")
                .price(new BigDecimal("999.99"))
                .category("Electronics")
                .stockQuantity(50)
                .available(true)
                .build();

        productResponse2 = ProductResponse.builder()
                .productId(2L)
                .name("Smartphone")
                .description("Latest smartphone model")
                .price(new BigDecimal("699.99"))
                .category("Electronics")
                .stockQuantity(100)
                .available(true)
                .build();

        // Setup pagination metadata
        paginationMetadata = PaginationMetadata.builder()
                .currentPage(0)
                .pageSize(10)
                .totalPages(1)
                .totalElements(2L)
                .build();

        // Setup search response
        searchResponse = ProductSearchResponse.builder()
                .products(Arrays.asList(
                        ProductSummary.builder()
                                .productId(1L)
                                .name("Laptop")
                                .price(new BigDecimal("999.99"))
                                .category("Electronics")
                                .available(true)
                                .build(),
                        ProductSummary.builder()
                                .productId(2L)
                                .name("Smartphone")
                                .price(new BigDecimal("699.99"))
                                .category("Electronics")
                                .available(true)
                                .build()
                ))
                .pagination(paginationMetadata)
                .build();
    }

    // ==================== GET /api/v1/products Tests ====================

    @Test
    @DisplayName("GET /api/v1/products - Valid Request - Should Return 200")
    void testGetAllProducts_ValidRequest_ReturnsOk() throws Exception {
        // Given
        when(productService.getAllProducts(anyInt(), anyInt()))
                .thenReturn(searchResponse);

        // When
        ResultActions result = mockMvc.perform(get("/api/v1/products")
                .param("page", "0")
                .param("size", "10")
                .contentType(MediaType.APPLICATION_JSON));

        // Then
        result.andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.products").isArray())
                .andExpect(jsonPath("$.products.length()").value(2))
                .andExpect(jsonPath("$.products[0].productId").value(1L))
                .andExpect(jsonPath("$.products[0].name").value("Laptop"))
                .andExpect(jsonPath("$.products[1].productId").value(2L))
                .andExpect(jsonPath("$.pagination.currentPage").value(0))
                .andExpect(jsonPath("$.pagination.totalElements").value(2));

        verify(productService, times(1)).getAllProducts(0, 10);
    }

    @Test
    @DisplayName("GET /api/v1/products - Default Pagination - Should Return 200")
    void testGetAllProducts_DefaultPagination_ReturnsOk() throws Exception {
        // Given
        when(productService.getAllProducts(anyInt(), anyInt()))
                .thenReturn(searchResponse);

        // When
        ResultActions result = mockMvc.perform(get("/api/v1/products")
                .contentType(MediaType.APPLICATION_JSON));

        // Then
        result.andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.products").isArray());

        verify(productService, times(1)).getAllProducts(0, 20);
    }

    @Test
    @DisplayName("GET /api/v1/products - Empty Result - Should Return 200")
    void testGetAllProducts_EmptyResult_ReturnsOk() throws Exception {
        // Given
        ProductSearchResponse emptyResponse = ProductSearchResponse.builder()
                .products(Collections.emptyList())
                .pagination(PaginationMetadata.builder()
                        .currentPage(0)
                        .pageSize(10)
                        .totalPages(0)
                        .totalElements(0L)
                        .build())
                .build();

        when(productService.getAllProducts(anyInt(), anyInt()))
                .thenReturn(emptyResponse);

        // When
        ResultActions result = mockMvc.perform(get("/api/v1/products")
                .param("page", "0")
                .param("size", "10")
                .contentType(MediaType.APPLICATION_JSON));

        // Then
        result.andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.products").isEmpty())
                .andExpect(jsonPath("$.pagination.totalElements").value(0));

        verify(productService, times(1)).getAllProducts(0, 10);
    }

    @Test
    @DisplayName("GET /api/v1/products - Invalid Page Number - Should Return 400")
    void testGetAllProducts_InvalidPageNumber_ReturnsBadRequest() throws Exception {
        // When
        ResultActions result = mockMvc.perform(get("/api/v1/products")
                .param("page", "-1")
                .param("size", "10")
                .contentType(MediaType.APPLICATION_JSON));

        // Then
        result.andDo(print())
                .andExpect(status().isBadRequest());

        verify(productService, never()).getAllProducts(anyInt(), anyInt());
    }

    @Test
    @DisplayName("GET /api/v1/products - Invalid Page Size - Should Return 400")
    void testGetAllProducts_InvalidPageSize_ReturnsBadRequest() throws Exception {
        // When
        ResultActions result = mockMvc.perform(get("/api/v1/products")
                .param("page", "0")
                .param("size", "0")
                .contentType(MediaType.APPLICATION_JSON));

        // Then
        result.andDo(print())
                .andExpect(status().isBadRequest());

        verify(productService, never()).getAllProducts(anyInt(), anyInt());
    }

    // ==================== GET /api/v1/products/{productId} Tests ====================

    @Test
    @DisplayName("GET /api/v1/products/{productId} - Valid Product ID - Should Return 200")
    void testGetProductById_ValidProductId_ReturnsOk() throws Exception {
        // Given
        when(productService.getProductById(anyLong()))
                .thenReturn(productResponse1);

        // When
        ResultActions result = mockMvc.perform(get("/api/v1/products/1")
                .contentType(MediaType.APPLICATION_JSON));

        // Then
        result.andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.productId").value(1L))
                .andExpect(jsonPath("$.name").value("Laptop"))
                .andExpect(jsonPath("$.description").value("High-performance laptop"))
                .andExpect(jsonPath("$.price").value(999.99))
                .andExpect(jsonPath("$.category").value("Electronics"))
                .andExpect(jsonPath("$.stockQuantity").value(50))
                .andExpect(jsonPath("$.available").value(true));

        verify(productService, times(1)).getProductById(1L);
    }

    @Test
    @DisplayName("GET /api/v1/products/{productId} - Product Not Found - Should Return 404")
    void testGetProductById_ProductNotFound_ReturnsNotFound() throws Exception {
        // Given
        when(productService.getProductById(anyLong()))
                .thenThrow(new ProductNotFoundException("Product not found with ID: 999"));

        // When
        ResultActions result = mockMvc.perform(get("/api/v1/products/999")
                .contentType(MediaType.APPLICATION_JSON));

        // Then
        result.andDo(print())
                .andExpect(status().isNotFound());

        verify(productService, times(1)).getProductById(999L);
    }

    @Test
    @DisplayName("GET /api/v1/products/{productId} - Invalid Product ID Format - Should Return 400")
    void testGetProductById_InvalidProductIdFormat_ReturnsBadRequest() throws Exception {
        // When
        ResultActions result = mockMvc.perform(get("/api/v1/products/invalid")
                .contentType(MediaType.APPLICATION_JSON));

        // Then
        result.andDo(print())
                .andExpect(status().isBadRequest());

        verify(productService, never()).getProductById(anyLong());
    }

    // ==================== GET /api/v1/products/category/{category} Tests ====================

    @Test
    @DisplayName("GET /api/v1/products/category/{category} - Valid Category - Should Return 200")
    void testGetProductsByCategory_ValidCategory_ReturnsOk() throws Exception {
        // Given
        when(productService.getProductsByCategory(anyString(), anyInt(), anyInt()))
                .thenReturn(searchResponse);

        // When
        ResultActions result = mockMvc.perform(get("/api/v1/products/category/Electronics")
                .param("page", "0")
                .param("size", "10")
                .contentType(MediaType.APPLICATION_JSON));

        // Then
        result.andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.products").isArray())
                .andExpect(jsonPath("$.products.length()").value(2))
                .andExpect(jsonPath("$.products[0].category").value("Electronics"));

        verify(productService, times(1)).getProductsByCategory("Electronics", 0, 10);
    }

    @Test
    @DisplayName("GET /api/v1/products/category/{category} - Empty Category - Should Return 200")
    void testGetProductsByCategory_EmptyCategory_ReturnsOk() throws Exception {
        // Given
        ProductSearchResponse emptyResponse = ProductSearchResponse.builder()
                .products(Collections.emptyList())
                .pagination(PaginationMetadata.builder()
                        .currentPage(0)
                        .pageSize(10)
                        .totalPages(0)
                        .totalElements(0L)
                        .build())
                .build();

        when(productService.getProductsByCategory(anyString(), anyInt(), anyInt()))
                .thenReturn(emptyResponse);

        // When
        ResultActions result = mockMvc.perform(get("/api/v1/products/category/NonExistent")
                .param("page", "0")
                .param("size", "10")
                .contentType(MediaType.APPLICATION_JSON));

        // Then
        result.andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.products").isEmpty());

        verify(productService, times(1)).getProductsByCategory("NonExistent", 0, 10);
    }

    @Test
    @DisplayName("GET /api/v1/products/category/{category} - Special Characters in Category - Should Return 200")
    void testGetProductsByCategory_SpecialCharacters_ReturnsOk() throws Exception {
        // Given
        when(productService.getProductsByCategory(anyString(), anyInt(), anyInt()))
                .thenReturn(searchResponse);

        // When
        ResultActions result = mockMvc.perform(get("/api/v1/products/category/Home & Garden")
                .param("page", "0")
                .param("size", "10")
                .contentType(MediaType.APPLICATION_JSON));

        // Then
        result.andDo(print())
                .andExpect(status().isOk());

        verify(productService, times(1)).getProductsByCategory(eq("Home & Garden"), eq(0), eq(10));
    }

    // ==================== GET /api/v1/products/search Tests ====================

    @Test
    @DisplayName("GET /api/v1/products/search - Valid Keyword - Should Return 200")
    void testSearchProducts_ValidKeyword_ReturnsOk() throws Exception {
        // Given
        when(productService.searchProducts(anyString(), anyInt(), anyInt()))
                .thenReturn(searchResponse);

        // When
        ResultActions result = mockMvc.perform(get("/api/v1/products/search")
                .param("keyword", "laptop")
                .param("page", "0")
                .param("size", "10")
                .contentType(MediaType.APPLICATION_JSON));

        // Then
        result.andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.products").isArray())
                .andExpect(jsonPath("$.products.length()").value(2));

        verify(productService, times(1)).searchProducts("laptop", 0, 10);
    }

    @Test
    @DisplayName("GET /api/v1/products/search - Empty Keyword - Should Return 400")
    void testSearchProducts_EmptyKeyword_ReturnsBadRequest() throws Exception {
        // When
        ResultActions result = mockMvc.perform(get("/api/v1/products/search")
                .param("keyword", "")
                .param("page", "0")
                .param("size", "10")
                .contentType(MediaType.APPLICATION_JSON));

        // Then
        result.andDo(print())
                .andExpect(status().isBadRequest());

        verify(productService, never()).searchProducts(anyString(), anyInt(), anyInt());
    }

    @Test
    @DisplayName("GET /api/v1/products/search - No Results - Should Return 200")
    void testSearchProducts_NoResults_ReturnsOk() throws Exception {
        // Given
        ProductSearchResponse emptyResponse = ProductSearchResponse.builder()
                .products(Collections.emptyList())
                .pagination(PaginationMetadata.builder()
                        .currentPage(0)
                        .pageSize(10)
                        .totalPages(0)
                        .totalElements(0L)
                        .build())
                .build();

        when(productService.searchProducts(anyString(), anyInt(), anyInt()))
                .thenReturn(emptyResponse);

        // When
        ResultActions result = mockMvc.perform(get("/api/v1/products/search")
                .param("keyword", "nonexistent")
                .param("page", "0")
                .param("size", "10")
                .contentType(MediaType.APPLICATION_JSON));

        // Then
        result.andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.products").isEmpty());

        verify(productService, times(1)).searchProducts("nonexistent", 0, 10);
    }

    // ==================== GET /api/v1/products/{productId}/availability Tests ====================

    @Test
    @DisplayName("GET /api/v1/products/{productId}/availability - Product Available - Should Return 200")
    void testCheckProductAvailability_ProductAvailable_ReturnsOk() throws Exception {
        // Given
        when(productService.checkProductAvailability(anyLong(), anyInt()))
                .thenReturn(true);

        // When
        ResultActions result = mockMvc.perform(get("/api/v1/products/1/availability")
                .param("quantity", "5")
                .contentType(MediaType.APPLICATION_JSON));

        // Then
        result.andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.available").value(true));

        verify(productService, times(1)).checkProductAvailability(1L, 5);
    }

    @Test
    @DisplayName("GET /api/v1/products/{productId}/availability - Product Not Available - Should Return 200")
    void testCheckProductAvailability_ProductNotAvailable_ReturnsOk() throws Exception {
        // Given
        when(productService.checkProductAvailability(anyLong(), anyInt()))
                .thenReturn(false);

        // When
        ResultActions result = mockMvc.perform(get("/api/v1/products/1/availability")
                .param("quantity", "100")
                .contentType(MediaType.APPLICATION_JSON));

        // Then
        result.andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.available").value(false));

        verify(productService, times(1)).checkProductAvailability(1L, 100);
    }

    @Test
    @DisplayName("GET /api/v1/products/{productId}/availability - Product Not Found - Should Return 404")
    void testCheckProductAvailability_ProductNotFound_ReturnsNotFound() throws Exception {
        // Given
        when(productService.checkProductAvailability(anyLong(), anyInt()))
                .thenThrow(new ProductNotFoundException("Product not found"));

        // When
        ResultActions result = mockMvc.perform(get("/api/v1/products/999/availability")
                .param("quantity", "5")
                .contentType(MediaType.APPLICATION_JSON));

        // Then
        result.andDo(print())
                .andExpect(status().isNotFound());

        verify(productService, times(1)).checkProductAvailability(999L, 5);
    }

    @Test
    @DisplayName("GET /api/v1/products/{productId}/availability - Invalid Quantity - Should Return 400")
    void testCheckProductAvailability_InvalidQuantity_ReturnsBadRequest() throws Exception {
        // When
        ResultActions result = mockMvc.perform(get("/api/v1/products/1/availability")
                .param("quantity", "-1")
                .contentType(MediaType.APPLICATION_JSON));

        // Then
        result.andDo(print())
                .andExpect(status().isBadRequest());

        verify(productService, never()).checkProductAvailability(anyLong(), anyInt());
    }

    @Test
    @DisplayName("GET /api/v1/products/{productId}/availability - Default Quantity - Should Return 200")
    void testCheckProductAvailability_DefaultQuantity_ReturnsOk() throws Exception {
        // Given
        when(productService.checkProductAvailability(anyLong(), anyInt()))
                .thenReturn(true);

        // When
        ResultActions result = mockMvc.perform(get("/api/v1/products/1/availability")
                .contentType(MediaType.APPLICATION_JSON));

        // Then
        result.andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.available").value(true));

        verify(productService, times(1)).checkProductAvailability(1L, 1);
    }
}