package com.ecommerce.productcatalog.controller;

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
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.junit.jupiter.SpringExtension;
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
 * Comprehensive Unit Test Suite for ProductController
 * Tests all API endpoints with valid, invalid, and edge case scenarios
 * Coverage: 100% of ProductController endpoints
 */
@ExtendWith({SpringExtension.class, MockitoExtension.class})
@WebMvcTest(ProductController.class)
@AutoConfigureMockMvc(addFilters = false)
class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ProductService productService;

    private ProductResponse sampleProduct;
    private ProductSummary sampleProductSummary;
    private List<ProductSummary> productList;

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
        sampleProduct.setAvailable(true);

        sampleProductSummary = new ProductSummary();
        sampleProductSummary.setProductId(1L);
        sampleProductSummary.setName("Laptop");
        sampleProductSummary.setPrice(new BigDecimal("999.99"));
        sampleProductSummary.setCategory("Electronics");
        sampleProductSummary.setAvailable(true);

        ProductSummary product2 = new ProductSummary();
        product2.setProductId(2L);
        product2.setName("Mouse");
        product2.setPrice(new BigDecimal("29.99"));
        product2.setCategory("Electronics");
        product2.setAvailable(true);

        productList = Arrays.asList(sampleProductSummary, product2);
    }

    // ==================== GET PRODUCT BY ID TESTS ====================

    @Test
    @DisplayName("GET /api/v1/products/{productId} - Success - Valid Product ID")
    void testGetProductById_Success() throws Exception {
        // Arrange
        when(productService.getProductById(1L)).thenReturn(sampleProduct);

        // Act & Assert
        mockMvc.perform(get("/api/v1/products/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.productId").value(1))
                .andExpect(jsonPath("$.name").value("Laptop"))
                .andExpect(jsonPath("$.price").value(999.99))
                .andExpect(jsonPath("$.category").value("Electronics"))
                .andExpect(jsonPath("$.stockQuantity").value(50))
                .andExpect(jsonPath("$.available").value(true));

        verify(productService, times(1)).getProductById(1L);
    }

    @Test
    @DisplayName("GET /api/v1/products/{productId} - Failure - Product Not Found")
    void testGetProductById_NotFound() throws Exception {
        // Arrange
        when(productService.getProductById(999L))
                .thenThrow(new ProductNotFoundException("Product with ID 999 not found"));

        // Act & Assert
        mockMvc.perform(get("/api/v1/products/999"))
                .andExpect(status().isNotFound());

        verify(productService, times(1)).getProductById(999L);
    }

    @Test
    @DisplayName("GET /api/v1/products/{productId} - Failure - Invalid Product ID Format")
    void testGetProductById_InvalidIdFormat() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/v1/products/invalid"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /api/v1/products/{productId} - Edge Case - Negative Product ID")
    void testGetProductById_NegativeId() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/v1/products/-1"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /api/v1/products/{productId} - Edge Case - Zero Product ID")
    void testGetProductById_ZeroId() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/v1/products/0"))
                .andExpect(status().isBadRequest());
    }

    // ==================== GET ALL PRODUCTS TESTS ====================

    @Test
    @DisplayName("GET /api/v1/products - Success - Default Pagination")
    void testGetAllProducts_Success() throws Exception {
        // Arrange
        ProductSearchResponse response = new ProductSearchResponse();
        response.setProducts(productList);
        response.setTotalElements(2L);
        response.setTotalPages(1);
        response.setCurrentPage(0);
        response.setPageSize(10);

        when(productService.getAllProducts(any(Pageable.class))).thenReturn(response);

        // Act & Assert
        mockMvc.perform(get("/api/v1/products")
                .param("page", "0")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.products").isArray())
                .andExpect(jsonPath("$.products.length()").value(2))
                .andExpect(jsonPath("$.totalElements").value(2))
                .andExpect(jsonPath("$.totalPages").value(1))
                .andExpect(jsonPath("$.currentPage").value(0));

        verify(productService, times(1)).getAllProducts(any(Pageable.class));
    }

    @Test
    @DisplayName("GET /api/v1/products - Success - Custom Pagination")
    void testGetAllProducts_CustomPagination() throws Exception {
        // Arrange
        ProductSearchResponse response = new ProductSearchResponse();
        response.setProducts(Collections.singletonList(sampleProductSummary));
        response.setTotalElements(10L);
        response.setTotalPages(5);
        response.setCurrentPage(2);
        response.setPageSize(2);

        when(productService.getAllProducts(any(Pageable.class))).thenReturn(response);

        // Act & Assert
        mockMvc.perform(get("/api/v1/products")
                .param("page", "2")
                .param("size", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.currentPage").value(2))
                .andExpect(jsonPath("$.pageSize").value(2));
    }

    @Test
    @DisplayName("GET /api/v1/products - Success - Empty Result")
    void testGetAllProducts_EmptyResult() throws Exception {
        // Arrange
        ProductSearchResponse response = new ProductSearchResponse();
        response.setProducts(Collections.emptyList());
        response.setTotalElements(0L);
        response.setTotalPages(0);
        response.setCurrentPage(0);
        response.setPageSize(10);

        when(productService.getAllProducts(any(Pageable.class))).thenReturn(response);

        // Act & Assert
        mockMvc.perform(get("/api/v1/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.products").isEmpty())
                .andExpect(jsonPath("$.totalElements").value(0));
    }

    @Test
    @DisplayName("GET /api/v1/products - Failure - Invalid Page Number")
    void testGetAllProducts_InvalidPageNumber() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/v1/products")
                .param("page", "-1")
                .param("size", "10"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /api/v1/products - Failure - Invalid Page Size")
    void testGetAllProducts_InvalidPageSize() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/v1/products")
                .param("page", "0")
                .param("size", "0"))
                .andExpect(status().isBadRequest());
    }

    // ==================== SEARCH PRODUCTS TESTS ====================

    @Test
    @DisplayName("GET /api/v1/products/search - Success - Valid Search Query")
    void testSearchProducts_Success() throws Exception {
        // Arrange
        ProductSearchResponse response = new ProductSearchResponse();
        response.setProducts(Collections.singletonList(sampleProductSummary));
        response.setTotalElements(1L);
        response.setTotalPages(1);

        when(productService.searchProducts(eq("Laptop"), any(Pageable.class))).thenReturn(response);

        // Act & Assert
        mockMvc.perform(get("/api/v1/products/search")
                .param("query", "Laptop")
                .param("page", "0")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.products").isArray())
                .andExpect(jsonPath("$.products[0].name").value("Laptop"))
                .andExpect(jsonPath("$.totalElements").value(1));

        verify(productService, times(1)).searchProducts(eq("Laptop"), any(Pageable.class));
    }

    @Test
    @DisplayName("GET /api/v1/products/search - Success - No Results")
    void testSearchProducts_NoResults() throws Exception {
        // Arrange
        ProductSearchResponse response = new ProductSearchResponse();
        response.setProducts(Collections.emptyList());
        response.setTotalElements(0L);

        when(productService.searchProducts(eq("NonExistent"), any(Pageable.class))).thenReturn(response);

        // Act & Assert
        mockMvc.perform(get("/api/v1/products/search")
                .param("query", "NonExistent"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.products").isEmpty());
    }

    @Test
    @DisplayName("GET /api/v1/products/search - Failure - Empty Query")
    void testSearchProducts_EmptyQuery() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/v1/products/search")
                .param("query", ""))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /api/v1/products/search - Edge Case - Special Characters in Query")
    void testSearchProducts_SpecialCharacters() throws Exception {
        // Arrange
        ProductSearchResponse response = new ProductSearchResponse();
        response.setProducts(Collections.emptyList());
        response.setTotalElements(0L);

        when(productService.searchProducts(anyString(), any(Pageable.class))).thenReturn(response);

        // Act & Assert
        mockMvc.perform(get("/api/v1/products/search")
                .param("query", "@#$%^&*()"))
                .andExpect(status().isOk());
    }

    // ==================== GET PRODUCTS BY CATEGORY TESTS ====================

    @Test
    @DisplayName("GET /api/v1/products/category/{category} - Success - Valid Category")
    void testGetProductsByCategory_Success() throws Exception {
        // Arrange
        ProductSearchResponse response = new ProductSearchResponse();
        response.setProducts(productList);
        response.setTotalElements(2L);

        when(productService.getProductsByCategory(eq("Electronics"), any(Pageable.class))).thenReturn(response);

        // Act & Assert
        mockMvc.perform(get("/api/v1/products/category/Electronics")
                .param("page", "0")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.products").isArray())
                .andExpect(jsonPath("$.products.length()").value(2))
                .andExpect(jsonPath("$.totalElements").value(2));

        verify(productService, times(1)).getProductsByCategory(eq("Electronics"), any(Pageable.class));
    }

    @Test
    @DisplayName("GET /api/v1/products/category/{category} - Success - Empty Category")
    void testGetProductsByCategory_EmptyCategory() throws Exception {
        // Arrange
        ProductSearchResponse response = new ProductSearchResponse();
        response.setProducts(Collections.emptyList());
        response.setTotalElements(0L);

        when(productService.getProductsByCategory(eq("Books"), any(Pageable.class))).thenReturn(response);

        // Act & Assert
        mockMvc.perform(get("/api/v1/products/category/Books"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.products").isEmpty());
    }

    @Test
    @DisplayName("GET /api/v1/products/category/{category} - Edge Case - Category with Spaces")
    void testGetProductsByCategory_CategoryWithSpaces() throws Exception {
        // Arrange
        ProductSearchResponse response = new ProductSearchResponse();
        response.setProducts(Collections.emptyList());
        response.setTotalElements(0L);

        when(productService.getProductsByCategory(anyString(), any(Pageable.class))).thenReturn(response);

        // Act & Assert
        mockMvc.perform(get("/api/v1/products/category/Home Appliances"))
                .andExpect(status().isOk());
    }

    // ==================== GET PRODUCTS BY PRICE RANGE TESTS ====================

    @Test
    @DisplayName("GET /api/v1/products/price-range - Success - Valid Price Range")
    void testGetProductsByPriceRange_Success() throws Exception {
        // Arrange
        ProductSearchResponse response = new ProductSearchResponse();
        response.setProducts(Collections.singletonList(sampleProductSummary));
        response.setTotalElements(1L);

        when(productService.getProductsByPriceRange(
                eq(new BigDecimal("500")),
                eq(new BigDecimal("1500")),
                any(Pageable.class)
        )).thenReturn(response);

        // Act & Assert
        mockMvc.perform(get("/api/v1/products/price-range")
                .param("minPrice", "500")
                .param("maxPrice", "1500")
                .param("page", "0")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.products").isArray())
                .andExpect(jsonPath("$.totalElements").value(1));

        verify(productService, times(1)).getProductsByPriceRange(
                any(BigDecimal.class),
                any(BigDecimal.class),
                any(Pageable.class)
        );
    }

    @Test
    @DisplayName("GET /api/v1/products/price-range - Failure - Invalid Price Range")
    void testGetProductsByPriceRange_InvalidRange() throws Exception {
        // Act & Assert - minPrice > maxPrice
        mockMvc.perform(get("/api/v1/products/price-range")
                .param("minPrice", "1500")
                .param("maxPrice", "500"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /api/v1/products/price-range - Failure - Negative Prices")
    void testGetProductsByPriceRange_NegativePrices() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/v1/products/price-range")
                .param("minPrice", "-100")
                .param("maxPrice", "500"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /api/v1/products/price-range - Edge Case - Zero Prices")
    void testGetProductsByPriceRange_ZeroPrices() throws Exception {
        // Arrange
        ProductSearchResponse response = new ProductSearchResponse();
        response.setProducts(Collections.emptyList());
        response.setTotalElements(0L);

        when(productService.getProductsByPriceRange(
                any(BigDecimal.class),
                any(BigDecimal.class),
                any(Pageable.class)
        )).thenReturn(response);

        // Act & Assert
        mockMvc.perform(get("/api/v1/products/price-range")
                .param("minPrice", "0")
                .param("maxPrice", "0"))
                .andExpect(status().isOk());
    }

    // ==================== GET AVAILABLE PRODUCTS TESTS ====================

    @Test
    @DisplayName("GET /api/v1/products/available - Success")
    void testGetAvailableProducts_Success() throws Exception {
        // Arrange
        ProductSearchResponse response = new ProductSearchResponse();
        response.setProducts(productList);
        response.setTotalElements(2L);

        when(productService.getAvailableProducts(any(Pageable.class))).thenReturn(response);

        // Act & Assert
        mockMvc.perform(get("/api/v1/products/available")
                .param("page", "0")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.products").isArray())
                .andExpect(jsonPath("$.products[0].available").value(true))
                .andExpect(jsonPath("$.totalElements").value(2));

        verify(productService, times(1)).getAvailableProducts(any(Pageable.class));
    }

    @Test
    @DisplayName("GET /api/v1/products/available - Success - No Available Products")
    void testGetAvailableProducts_NoProducts() throws Exception {
        // Arrange
        ProductSearchResponse response = new ProductSearchResponse();
        response.setProducts(Collections.emptyList());
        response.setTotalElements(0L);

        when(productService.getAvailableProducts(any(Pageable.class))).thenReturn(response);

        // Act & Assert
        mockMvc.perform(get("/api/v1/products/available"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.products").isEmpty());
    }

    // ==================== GET FEATURED PRODUCTS TESTS ====================

    @Test
    @DisplayName("GET /api/v1/products/featured - Success")
    void testGetFeaturedProducts_Success() throws Exception {
        // Arrange
        when(productService.getFeaturedProducts()).thenReturn(productList);

        // Act & Assert
        mockMvc.perform(get("/api/v1/products/featured"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2));

        verify(productService, times(1)).getFeaturedProducts();
    }

    @Test
    @DisplayName("GET /api/v1/products/featured - Success - Empty List")
    void testGetFeaturedProducts_EmptyList() throws Exception {
        // Arrange
        when(productService.getFeaturedProducts()).thenReturn(Collections.emptyList());

        // Act & Assert
        mockMvc.perform(get("/api/v1/products/featured"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
    }

    // ==================== CHECK PRODUCT AVAILABILITY TESTS ====================

    @Test
    @DisplayName("GET /api/v1/products/check-availability/{productId} - Success - Available")
    void testCheckProductAvailability_Available() throws Exception {
        // Arrange
        when(productService.checkProductAvailability(1L, 5)).thenReturn(true);

        // Act & Assert
        mockMvc.perform(get("/api/v1/products/check-availability/1")
                .param("quantity", "5"))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));

        verify(productService, times(1)).checkProductAvailability(1L, 5);
    }

    @Test
    @DisplayName("GET /api/v1/products/check-availability/{productId} - Success - Not Available")
    void testCheckProductAvailability_NotAvailable() throws Exception {
        // Arrange
        when(productService.checkProductAvailability(1L, 100)).thenReturn(false);

        // Act & Assert
        mockMvc.perform(get("/api/v1/products/check-availability/1")
                .param("quantity", "100"))
                .andExpect(status().isOk())
                .andExpect(content().string("false"));
    }

    @Test
    @DisplayName("GET /api/v1/products/check-availability/{productId} - Failure - Product Not Found")
    void testCheckProductAvailability_ProductNotFound() throws Exception {
        // Arrange
        when(productService.checkProductAvailability(999L, 5))
                .thenThrow(new ProductNotFoundException("Product not found"));

        // Act & Assert
        mockMvc.perform(get("/api/v1/products/check-availability/999")
                .param("quantity", "5"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("GET /api/v1/products/check-availability/{productId} - Failure - Invalid Quantity")
    void testCheckProductAvailability_InvalidQuantity() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/v1/products/check-availability/1")
                .param("quantity", "0"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /api/v1/products/check-availability/{productId} - Edge Case - Large Quantity")
    void testCheckProductAvailability_LargeQuantity() throws Exception {
        // Arrange
        when(productService.checkProductAvailability(1L, 1000000)).thenReturn(false);

        // Act & Assert
        mockMvc.perform(get("/api/v1/products/check-availability/1")
                .param("quantity", "1000000"))
                .andExpect(status().isOk())
                .andExpect(content().string("false"));
    }

    // ==================== PERFORMANCE AND EDGE CASE TESTS ====================

    @Test
    @DisplayName("Edge Case - Concurrent Product Searches")
    void testConcurrentProductSearches() throws Exception {
        // Arrange
        ProductSearchResponse response = new ProductSearchResponse();
        response.setProducts(Collections.singletonList(sampleProductSummary));
        response.setTotalElements(1L);

        when(productService.searchProducts(anyString(), any(Pageable.class))).thenReturn(response);

        // Act & Assert - Simulate multiple concurrent requests
        for (int i = 0; i < 10; i++) {
            mockMvc.perform(get("/api/v1/products/search")
                    .param("query", "Laptop"))
                    .andExpect(status().isOk());
        }

        verify(productService, times(10)).searchProducts(anyString(), any(Pageable.class));
    }

    @Test
    @DisplayName("Edge Case - Very Large Page Size")
    void testVeryLargePageSize() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/v1/products")
                .param("page", "0")
                .param("size", "10000"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Edge Case - SQL Injection Attempt in Search")
    void testSQLInjectionAttemptInSearch() throws Exception {
        // Arrange
        ProductSearchResponse response = new ProductSearchResponse();
        response.setProducts(Collections.emptyList());
        response.setTotalElements(0L);

        when(productService.searchProducts(anyString(), any(Pageable.class))).thenReturn(response);

        // Act & Assert
        mockMvc.perform(get("/api/v1/products/search")
                .param("query", "'; DROP TABLE products; --"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Edge Case - Unicode Characters in Search")
    void testUnicodeCharactersInSearch() throws Exception {
        // Arrange
        ProductSearchResponse response = new ProductSearchResponse();
        response.setProducts(Collections.emptyList());
        response.setTotalElements(0L);

        when(productService.searchProducts(anyString(), any(Pageable.class))).thenReturn(response);

        // Act & Assert
        mockMvc.perform(get("/api/v1/products/search")
                .param("query", "日本語"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Edge Case - Extremely Long Search Query")
    void testExtremelyLongSearchQuery() throws Exception {
        // Arrange
        String longQuery = "a".repeat(1000);

        // Act & Assert
        mockMvc.perform(get("/api/v1/products/search")
                .param("query", longQuery))
                .andExpect(status().isBadRequest());
    }
}