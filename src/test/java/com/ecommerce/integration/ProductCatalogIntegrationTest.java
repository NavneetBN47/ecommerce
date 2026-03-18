package com.ecommerce.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("Product Catalog Integration Tests")
class ProductCatalogIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @Order(1)
    @DisplayName("Integration: Search Products")
    void testSearchProducts() throws Exception {
        mockMvc.perform(get("/api/v1/products/search")
                .param("query", "laptop")
                .param("page", "0")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.products").isArray())
                .andExpect(jsonPath("$.metadata").exists());
    }

    @Test
    @Order(2)
    @DisplayName("Integration: Get Product By ID")
    void testGetProductById() throws Exception {
        mockMvc.perform(get("/api/v1/products/{productId}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.productId").value(1))
                .andExpect(jsonPath("$.name").exists())
                .andExpect(jsonPath("$.price").exists());
    }

    @Test
    @Order(3)
    @DisplayName("Integration: Get Products By Category")
    void testGetProductsByCategory() throws Exception {
        mockMvc.perform(get("/api/v1/products/category/{category}", "Electronics")
                .param("page", "0")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.products").isArray());
    }

    @Test
    @Order(4)
    @DisplayName("Integration: Get Available Products")
    void testGetAvailableProducts() throws Exception {
        mockMvc.perform(get("/api/v1/products/available")
                .param("page", "0")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.products").isArray())
                .andExpect(jsonPath("$.metadata.totalItems").exists());
    }

    @Test
    @Order(5)
    @DisplayName("Integration: Product Cache Performance")
    void testProductCachePerformance() throws Exception {
        long startTime = System.currentTimeMillis();
        mockMvc.perform(get("/api/v1/products/{productId}", 1L))
                .andExpect(status().isOk());
        long firstCallTime = System.currentTimeMillis() - startTime;

        startTime = System.currentTimeMillis();
        mockMvc.perform(get("/api/v1/products/{productId}", 1L))
                .andExpect(status().isOk());
        long secondCallTime = System.currentTimeMillis() - startTime;

        Assertions.assertTrue(secondCallTime <= firstCallTime, "Cache should improve performance");
    }
}