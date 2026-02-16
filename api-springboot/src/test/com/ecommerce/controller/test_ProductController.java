package com.ecommerce.controller;

import com.ecommerce.dto.ProductResponse;
import com.ecommerce.service.ProductService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * JUnit 5 test class for ProductController
 * Tests product search operations
 */
@WebMvcTest(ProductController.class)
@DisplayName("ProductController Tests")
class test_ProductController {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ProductService productService;

    private List<ProductResponse> productList;

    /**
     * Set up test data before each test
     */
    @BeforeEach
    void setUp() {
        ProductResponse product1 = new ProductResponse();
        product1.setId(UUID.randomUUID());
        product1.setName("Laptop");
        product1.setDescription("High performance laptop");
        product1.setPrice(BigDecimal.valueOf(999.99));
        product1.setStockQuantity(10);

        ProductResponse product2 = new ProductResponse();
        product2.setId(UUID.randomUUID());
        product2.setName("Laptop Stand");
        product2.setDescription("Ergonomic laptop stand");
        product2.setPrice(BigDecimal.valueOf(49.99));
        product2.setStockQuantity(25);

        productList = Arrays.asList(product1, product2);
    }

    /**
     * Test successfully searching products
     * Verifies that product search returns matching products
     */
    @Test
    @DisplayName("Should search products successfully")
    void testSearchProducts_Success() throws Exception {
        when(productService.searchProducts(anyString())).thenReturn(productList);

        mockMvc.perform(get("/api/products/search")
                .param("keyword", "laptop"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Laptop"))
                .andExpect(jsonPath("$[0].price").value(999.99))
                .andExpect(jsonPath("$[1].name").value("Laptop Stand"))
                .andExpect(jsonPath("$.length()").value(2));

        verify(productService, times(1)).searchProducts("laptop");
    }

    /**
     * Test searching products with no results
     * Verifies that empty list is returned when no products match
     */
    @Test
    @DisplayName("Should return empty list when no products match")
    void testSearchProducts_NoResults() throws Exception {
        when(productService.searchProducts(anyString())).thenReturn(new ArrayList<>());

        mockMvc.perform(get("/api/products/search")
                .param("keyword", "nonexistent"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));

        verify(productService, times(1)).searchProducts("nonexistent");
    }

    /**
     * Test searching products with empty keyword
     * Verifies that empty keyword returns all products or empty list
     */
    @Test
    @DisplayName("Should handle empty keyword search")
    void testSearchProducts_EmptyKeyword() throws Exception {
        when(productService.searchProducts(anyString())).thenReturn(new ArrayList<>());

        mockMvc.perform(get("/api/products/search")
                .param("keyword", ""))
                .andExpect(status().isOk());

        verify(productService, times(1)).searchProducts("");
    }

    /**
     * Test searching products without keyword parameter
     * Verifies that missing keyword parameter is handled
     */
    @Test
    @DisplayName("Should return 400 when keyword parameter is missing")
    void testSearchProducts_MissingKeyword() throws Exception {
        mockMvc.perform(get("/api/products/search"))
                .andExpect(status().isBadRequest());

        verify(productService, never()).searchProducts(anyString());
    }

    /**
     * Test searching products with special characters
     * Verifies that special characters in keyword are handled
     */
    @Test
    @DisplayName("Should handle special characters in keyword")
    void testSearchProducts_SpecialCharacters() throws Exception {
        when(productService.searchProducts(anyString())).thenReturn(new ArrayList<>());

        mockMvc.perform(get("/api/products/search")
                .param("keyword", "@#$%"))
                .andExpect(status().isOk());

        verify(productService, times(1)).searchProducts("@#$%");
    }

    /**
     * Test searching products with long keyword
     * Verifies that long keywords are handled properly
     */
    @Test
    @DisplayName("Should handle long keyword search")
    void testSearchProducts_LongKeyword() throws Exception {
        String longKeyword = "a".repeat(100);
        when(productService.searchProducts(anyString())).thenReturn(new ArrayList<>());

        mockMvc.perform(get("/api/products/search")
                .param("keyword", longKeyword))
                .andExpect(status().isOk());

        verify(productService, times(1)).searchProducts(longKeyword);
    }

    /**
     * Test searching products with case-insensitive keyword
     * Verifies that search is case-insensitive
     */
    @Test
    @DisplayName("Should perform case-insensitive search")
    void testSearchProducts_CaseInsensitive() throws Exception {
        when(productService.searchProducts(anyString())).thenReturn(productList);

        mockMvc.perform(get("/api/products/search")
                .param("keyword", "LAPTOP"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));

        verify(productService, times(1)).searchProducts("LAPTOP");
    }

    /**
     * Test searching products with service exception
     * Verifies that service exceptions are properly handled
     */
    @Test
    @DisplayName("Should handle service exception during search")
    void testSearchProducts_ServiceException() throws Exception {
        when(productService.searchProducts(anyString()))
                .thenThrow(new RuntimeException("Database error"));

        mockMvc.perform(get("/api/products/search")
                .param("keyword", "laptop"))
                .andExpect(status().is5xxServerError());

        verify(productService, times(1)).searchProducts("laptop");
    }

    /**
     * Test searching products with whitespace keyword
     * Verifies that whitespace-only keywords are handled
     */
    @Test
    @DisplayName("Should handle whitespace-only keyword")
    void testSearchProducts_WhitespaceKeyword() throws Exception {
        when(productService.searchProducts(anyString())).thenReturn(new ArrayList<>());

        mockMvc.perform(get("/api/products/search")
                .param("keyword", "   "))
                .andExpect(status().isOk());

        verify(productService, times(1)).searchProducts("   ");
    }

    /**
     * Test searching products with numeric keyword
     * Verifies that numeric keywords are handled
     */
    @Test
    @DisplayName("Should handle numeric keyword search")
    void testSearchProducts_NumericKeyword() throws Exception {
        when(productService.searchProducts(anyString())).thenReturn(new ArrayList<>());

        mockMvc.perform(get("/api/products/search")
                .param("keyword", "12345"))
                .andExpect(status().isOk());

        verify(productService, times(1)).searchProducts("12345");
    }
}