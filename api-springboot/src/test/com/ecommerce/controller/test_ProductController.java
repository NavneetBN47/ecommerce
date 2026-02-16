package com.ecommerce.controller;

import com.ecommerce.dto.ProductResponse;
import com.ecommerce.service.ProductService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Test class for ProductController
 * 
 * Tests product search functionality including:
 * - Search with keyword
 * - Search without keyword (all products)
 * - Empty search results
 * 
 * @author Test Generation System
 * @version 1.0.0
 */
@WebMvcTest(ProductController.class)
@DisplayName("ProductController Tests")
class test_ProductController {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ProductService productService;

    private List<ProductResponse> testProducts;

    /**
     * Set up test data before each test
     */
    @BeforeEach
    void setUp() {
        ProductResponse product1 = ProductResponse.builder()
            .id(UUID.randomUUID())
            .name("Test Product 1")
            .description("Test Description 1")
            .price(BigDecimal.valueOf(99.99))
            .availableQty(10)
            .sku("TEST-001")
            .isActive(true)
            .build();

        ProductResponse product2 = ProductResponse.builder()
            .id(UUID.randomUUID())
            .name("Test Product 2")
            .description("Test Description 2")
            .price(BigDecimal.valueOf(149.99))
            .availableQty(5)
            .sku("TEST-002")
            .isActive(true)
            .build();

        testProducts = Arrays.asList(product1, product2);
    }

    /**
     * Test searching products with keyword
     * 
     * Validates:
     * - HTTP 200 OK status
     * - Correct number of products returned
     * - Product details in response
     */
    @Test
    @DisplayName("Should search products with keyword successfully")
    void testSearchProducts_WithKeyword() throws Exception {
        when(productService.searchProducts("Test"))
            .thenReturn(testProducts);

        mockMvc.perform(get("/products/search")
                .param("keyword", "Test"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$.length()").value(2))
            .andExpect(jsonPath("$[0].name").value("Test Product 1"))
            .andExpect(jsonPath("$[1].name").value("Test Product 2"));
    }

    /**
     * Test searching products without keyword
     * 
     * Validates:
     * - HTTP 200 OK status
     * - All active products returned
     */
    @Test
    @DisplayName("Should return all products when no keyword provided")
    void testSearchProducts_NoKeyword() throws Exception {
        when(productService.searchProducts(null))
            .thenReturn(testProducts);

        mockMvc.perform(get("/products/search"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$.length()").value(2));
    }

    /**
     * Test searching products with empty keyword
     * 
     * Validates:
     * - HTTP 200 OK status
     * - All products returned for empty string
     */
    @Test
    @DisplayName("Should return all products for empty keyword")
    void testSearchProducts_EmptyKeyword() throws Exception {
        when(productService.searchProducts(""))
            .thenReturn(testProducts);

        mockMvc.perform(get("/products/search")
                .param("keyword", ""))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isArray());
    }

    /**
     * Test searching products with no results
     * 
     * Validates:
     * - HTTP 200 OK status
     * - Empty array returned
     */
    @Test
    @DisplayName("Should return empty list when no products match")
    void testSearchProducts_NoResults() throws Exception {
        when(productService.searchProducts(anyString()))
            .thenReturn(Collections.emptyList());

        mockMvc.perform(get("/products/search")
                .param("keyword", "NonExistent"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$.length()").value(0));
    }
}