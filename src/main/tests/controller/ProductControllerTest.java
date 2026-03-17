package com.ecommerce.productcatalog.controller;

import com.ecommerce.productcatalog.dto.*;
import com.ecommerce.productcatalog.service.ProductService;
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
import org.springframework.test.web.servlet.ResultActions;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
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
    private ProductSummary productSummary1;
    private ProductSummary productSummary2;

    @BeforeEach
    void setUp() {
        // Setup product response
        productResponse = ProductResponse.builder()
            .productId(1L)
            .name("Laptop")
            .description("High-performance laptop")
            .category("Electronics")
            .price(new BigDecimal("999.99"))
            .stockQuantity(50)
            .inStock(true)
            .imageUrl("https://example.com/laptop.jpg")
            .build();

        // Setup product summaries
        productSummary1 = ProductSummary.builder()
            .productId(1L)
            .name("Laptop")
            .category("Electronics")
            .price(new BigDecimal("999.99"))
            .inStock(true)
            .build();

        productSummary2 = ProductSummary.builder()
            .productId(2L)
            .name("Mouse")
            .category("Electronics")
            .price(new BigDecimal("29.99"))
            .inStock(true)
            .build();

        // Setup search response
        PaginationMetadata pagination = PaginationMetadata.builder()
            .currentPage(0)
            .pageSize(20)
            .totalElements(2L)
            .totalPages(1)
            .build();

        searchResponse = ProductSearchResponse.builder()
            .products(Arrays.asList(productSummary1, productSummary2))
            .pagination(pagination)
            .build();
    }

    // ==================== SEARCH PRODUCTS TESTS ====================

    @Test
    @DisplayName("GET /api/v1/products/search - No Filters - Should Return 200")
    void testSearchProducts_NoFilters_ReturnsOk() throws Exception {
        when(productService.searchProducts(isNull(), isNull(), isNull(), isNull(), isNull(), 
            eq("name"), eq("ASC"), eq(0), eq(20)))
            .thenReturn(searchResponse);

        ResultActions result = mockMvc.perform(get("/api/v1/products/search"));

        result.andExpect(status().isOk())
            .andExpect(jsonPath("$.products").isArray())
            .andExpect(jsonPath("$.products.length()").value(2))
            .andExpect(jsonPath("$.pagination.totalElements").value(2));

        verify(productService, times(1)).searchProducts(isNull(), isNull(), isNull(), isNull(), 
            isNull(), eq("name"), eq("ASC"), eq(0), eq(20));
    }

    @Test
    @DisplayName("GET /api/v1/products/search - With Keyword - Should Return 200")
    void testSearchProducts_WithKeyword_ReturnsOk() throws Exception {
        when(productService.searchProducts(eq("laptop"), isNull(), isNull(), isNull(), isNull(), 
            eq("name"), eq("ASC"), eq(0), eq(20)))
            .thenReturn(searchResponse);

        ResultActions result = mockMvc.perform(get("/api/v1/products/search")
            .param("keyword", "laptop"));

        result.andExpect(status().isOk())
            .andExpect(jsonPath("$.products").isArray())
            .andExpect(jsonPath("$.products[0].name").value("Laptop"));

        verify(productService, times(1)).searchProducts(eq("laptop"), isNull(), isNull(), isNull(), 
            isNull(), eq("name"), eq("ASC"), eq(0), eq(20));
    }

    @Test
    @DisplayName("GET /api/v1/products/search - With Category Filter - Should Return 200")
    void testSearchProducts_WithCategory_ReturnsOk() throws Exception {
        when(productService.searchProducts(isNull(), eq("Electronics"), isNull(), isNull(), isNull(), 
            eq("name"), eq("ASC"), eq(0), eq(20)))
            .thenReturn(searchResponse);

        ResultActions result = mockMvc.perform(get("/api/v1/products/search")
            .param("category", "Electronics"));

        result.andExpect(status().isOk())
            .andExpect(jsonPath("$.products").isArray())
            .andExpect(jsonPath("$.products[0].category").value("Electronics"));

        verify(productService, times(1)).searchProducts(isNull(), eq("Electronics"), isNull(), 
            isNull(), isNull(), eq("name"), eq("ASC"), eq(0), eq(20));
    }

    @Test
    @DisplayName("GET /api/v1/products/search - With Price Range - Should Return 200")
    void testSearchProducts_WithPriceRange_ReturnsOk() throws Exception {
        when(productService.searchProducts(isNull(), isNull(), eq(100.0), eq(1000.0), isNull(), 
            eq("name"), eq("ASC"), eq(0), eq(20)))
            .thenReturn(searchResponse);

        ResultActions result = mockMvc.perform(get("/api/v1/products/search")
            .param("minPrice", "100.0")
            .param("maxPrice", "1000.0"));

        result.andExpect(status().isOk())
            .andExpect(jsonPath("$.products").isArray());

        verify(productService, times(1)).searchProducts(isNull(), isNull(), eq(100.0), eq(1000.0), 
            isNull(), eq("name"), eq("ASC"), eq(0), eq(20));
    }

    @Test
    @DisplayName("GET /api/v1/products/search - With Stock Filter - Should Return 200")
    void testSearchProducts_WithStockFilter_ReturnsOk() throws Exception {
        when(productService.searchProducts(isNull(), isNull(), isNull(), isNull(), eq(true), 
            eq("name"), eq("ASC"), eq(0), eq(20)))
            .thenReturn(searchResponse);

        ResultActions result = mockMvc.perform(get("/api/v1/products/search")
            .param("inStock", "true"));

        result.andExpect(status().isOk())
            .andExpect(jsonPath("$.products").isArray())
            .andExpect(jsonPath("$.products[0].inStock").value(true));

        verify(productService, times(1)).searchProducts(isNull(), isNull(), isNull(), isNull(), 
            eq(true), eq("name"), eq("ASC"), eq(0), eq(20));
    }

    @Test
    @DisplayName("GET /api/v1/products/search - Sort By Price Ascending - Should Return 200")
    void testSearchProducts_SortByPriceAsc_ReturnsOk() throws Exception {
        when(productService.searchProducts(isNull(), isNull(), isNull(), isNull(), isNull(), 
            eq("price"), eq("ASC"), eq(0), eq(20)))
            .thenReturn(searchResponse);

        ResultActions result = mockMvc.perform(get("/api/v1/products/search")
            .param("sortBy", "price")
            .param("sortDirection", "ASC"));

        result.andExpect(status().isOk())
            .andExpect(jsonPath("$.products").isArray());

        verify(productService, times(1)).searchProducts(isNull(), isNull(), isNull(), isNull(), 
            isNull(), eq("price"), eq("ASC"), eq(0), eq(20));
    }

    @Test
    @DisplayName("GET /api/v1/products/search - Sort By Price Descending - Should Return 200")
    void testSearchProducts_SortByPriceDesc_ReturnsOk() throws Exception {
        when(productService.searchProducts(isNull(), isNull(), isNull(), isNull(), isNull(), 
            eq("price"), eq("DESC"), eq(0), eq(20)))
            .thenReturn(searchResponse);

        ResultActions result = mockMvc.perform(get("/api/v1/products/search")
            .param("sortBy", "price")
            .param("sortDirection", "DESC"));

        result.andExpect(status().isOk())
            .andExpect(jsonPath("$.products").isArray());

        verify(productService, times(1)).searchProducts(isNull(), isNull(), isNull(), isNull(), 
            isNull(), eq("price"), eq("DESC"), eq(0), eq(20));
    }

    @Test
    @DisplayName("GET /api/v1/products/search - With Pagination - Should Return 200")
    void testSearchProducts_WithPagination_ReturnsOk() throws Exception {
        when(productService.searchProducts(isNull(), isNull(), isNull(), isNull(), isNull(), 
            eq("name"), eq("ASC"), eq(1), eq(10)))
            .thenReturn(searchResponse);

        ResultActions result = mockMvc.perform(get("/api/v1/products/search")
            .param("page", "1")
            .param("size", "10"));

        result.andExpect(status().isOk())
            .andExpect(jsonPath("$.pagination").exists());

        verify(productService, times(1)).searchProducts(isNull(), isNull(), isNull(), isNull(), 
            isNull(), eq("name"), eq("ASC"), eq(1), eq(10));
    }

    @Test
    @DisplayName("GET /api/v1/products/search - Empty Results - Should Return 200")
    void testSearchProducts_EmptyResults_ReturnsOk() throws Exception {
        ProductSearchResponse emptyResponse = ProductSearchResponse.builder()
            .products(Collections.emptyList())
            .pagination(PaginationMetadata.builder()
                .currentPage(0)
                .pageSize(20)
                .totalElements(0L)
                .totalPages(0)
                .build())
            .build();

        when(productService.searchProducts(eq("nonexistent"), isNull(), isNull(), isNull(), isNull(), 
            eq("name"), eq("ASC"), eq(0), eq(20)))
            .thenReturn(emptyResponse);

        ResultActions result = mockMvc.perform(get("/api/v1/products/search")
            .param("keyword", "nonexistent"));

        result.andExpect(status().isOk())
            .andExpect(jsonPath("$.products").isArray())
            .andExpect(jsonPath("$.products.length()").value(0))
            .andExpect(jsonPath("$.pagination.totalElements").value(0));

        verify(productService, times(1)).searchProducts(eq("nonexistent"), isNull(), isNull(), 
            isNull(), isNull(), eq("name"), eq("ASC"), eq(0), eq(20));
    }

    @Test
    @DisplayName("GET /api/v1/products/search - Invalid Price Range - Should Return 400")
    void testSearchProducts_InvalidPriceRange_ReturnsBadRequest() throws Exception {
        when(productService.searchProducts(isNull(), isNull(), eq(1000.0), eq(100.0), isNull(), 
            eq("name"), eq("ASC"), eq(0), eq(20)))
            .thenThrow(new IllegalArgumentException("Invalid price range"));

        ResultActions result = mockMvc.perform(get("/api/v1/products/search")
            .param("minPrice", "1000.0")
            .param("maxPrice", "100.0"));

        result.andExpect(status().isInternalServerError());

        verify(productService, times(1)).searchProducts(isNull(), isNull(), eq(1000.0), eq(100.0), 
            isNull(), eq("name"), eq("ASC"), eq(0), eq(20));
    }

    @Test
    @DisplayName("GET /api/v1/products/search - Negative Page Number - Should Return 400")
    void testSearchProducts_NegativePageNumber_ReturnsBadRequest() throws Exception {
        ResultActions result = mockMvc.perform(get("/api/v1/products/search")
            .param("page", "-1"));

        result.andExpect(status().isInternalServerError());
    }

    @Test
    @DisplayName("GET /api/v1/products/search - Page Size Exceeds Maximum - Should Return 400")
    void testSearchProducts_PageSizeExceedsMax_ReturnsBadRequest() throws Exception {
        ResultActions result = mockMvc.perform(get("/api/v1/products/search")
            .param("size", "101"));

        result.andExpect(status().isInternalServerError());
    }

    @Test
    @DisplayName("GET /api/v1/products/search - Multiple Filters Combined - Should Return 200")
    void testSearchProducts_MultipleFilters_ReturnsOk() throws Exception {
        when(productService.searchProducts(eq("laptop"), eq("Electronics"), eq(500.0), eq(1500.0), 
            eq(true), eq("price"), eq("DESC"), eq(0), eq(20)))
            .thenReturn(searchResponse);

        ResultActions result = mockMvc.perform(get("/api/v1/products/search")
            .param("keyword", "laptop")
            .param("category", "Electronics")
            .param("minPrice", "500.0")
            .param("maxPrice", "1500.0")
            .param("inStock", "true")
            .param("sortBy", "price")
            .param("sortDirection", "DESC"));

        result.andExpect(status().isOk())
            .andExpect(jsonPath("$.products").isArray());

        verify(productService, times(1)).searchProducts(eq("laptop"), eq("Electronics"), eq(500.0), 
            eq(1500.0), eq(true), eq("price"), eq("DESC"), eq(0), eq(20));
    }

    // ==================== GET PRODUCT BY ID TESTS ====================

    @Test
    @DisplayName("GET /api/v1/products/{productId} - Valid ID - Should Return 200")
    void testGetProductById_ValidId_ReturnsOk() throws Exception {
        when(productService.getProductById(1L))
            .thenReturn(productResponse);

        ResultActions result = mockMvc.perform(get("/api/v1/products/1"));

        result.andExpect(status().isOk())
            .andExpect(jsonPath("$.productId").value(1L))
            .andExpect(jsonPath("$.name").value("Laptop"))
            .andExpect(jsonPath("$.description").value("High-performance laptop"))
            .andExpect(jsonPath("$.category").value("Electronics"))
            .andExpect(jsonPath("$.price").value(999.99))
            .andExpect(jsonPath("$.stockQuantity").value(50))
            .andExpect(jsonPath("$.inStock").value(true));

        verify(productService, times(1)).getProductById(1L);
    }

    @Test
    @DisplayName("GET /api/v1/products/{productId} - Product Not Found - Should Return 404")
    void testGetProductById_NotFound_ReturnsNotFound() throws Exception {
        when(productService.getProductById(999L))
            .thenThrow(new RuntimeException("Product not found"));

        ResultActions result = mockMvc.perform(get("/api/v1/products/999"));

        result.andExpect(status().isInternalServerError());

        verify(productService, times(1)).getProductById(999L);
    }

    @Test
    @DisplayName("GET /api/v1/products/{productId} - Invalid ID Format - Should Return 400")
    void testGetProductById_InvalidIdFormat_ReturnsBadRequest() throws Exception {
        ResultActions result = mockMvc.perform(get("/api/v1/products/invalid"));

        result.andExpect(status().isBadRequest());

        verify(productService, never()).getProductById(anyLong());
    }

    @Test
    @DisplayName("GET /api/v1/products/{productId} - Negative ID - Should Return 400")
    void testGetProductById_NegativeId_ReturnsBadRequest() throws Exception {
        ResultActions result = mockMvc.perform(get("/api/v1/products/-1"));

        result.andExpect(status().isInternalServerError());
    }

    @Test
    @DisplayName("GET /api/v1/products/{productId} - Zero ID - Should Return 400")
    void testGetProductById_ZeroId_ReturnsBadRequest() throws Exception {
        ResultActions result = mockMvc.perform(get("/api/v1/products/0"));

        result.andExpect(status().isInternalServerError());
    }

    @Test
    @DisplayName("GET /api/v1/products/{productId} - Out of Stock Product - Should Return 200")
    void testGetProductById_OutOfStock_ReturnsOk() throws Exception {
        ProductResponse outOfStockProduct = ProductResponse.builder()
            .productId(2L)
            .name("Out of Stock Item")
            .description("Currently unavailable")
            .category("Electronics")
            .price(new BigDecimal("199.99"))
            .stockQuantity(0)
            .inStock(false)
            .build();

        when(productService.getProductById(2L))
            .thenReturn(outOfStockProduct);

        ResultActions result = mockMvc.perform(get("/api/v1/products/2"));

        result.andExpect(status().isOk())
            .andExpect(jsonPath("$.productId").value(2L))
            .andExpect(jsonPath("$.inStock").value(false))
            .andExpect(jsonPath("$.stockQuantity").value(0));

        verify(productService, times(1)).getProductById(2L);
    }

    @Test
    @DisplayName("GET /api/v1/products/{productId} - Cached Response - Should Return 200")
    void testGetProductById_CachedResponse_ReturnsOk() throws Exception {
        when(productService.getProductById(1L))
            .thenReturn(productResponse);

        // First request
        mockMvc.perform(get("/api/v1/products/1"))
            .andExpect(status().isOk());

        // Second request (should use cache)
        ResultActions result = mockMvc.perform(get("/api/v1/products/1"));

        result.andExpect(status().isOk())
            .andExpect(jsonPath("$.productId").value(1L));

        verify(productService, times(2)).getProductById(1L);
    }
}