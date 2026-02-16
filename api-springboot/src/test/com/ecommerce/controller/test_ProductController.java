package com.ecommerce.controller;

import com.ecommerce.dto.ProductResponse;
import com.ecommerce.service.ProductService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Test class for ProductController
 * 
 * This test class verifies the REST API endpoints for product catalog management.
 * It tests:
 * - Product search with keyword
 * - Product search without keyword (all products)
 * - Empty search results
 * - Case-insensitive search
 * 
 * @author Test Generation Agent
 * @version 1.0.0
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ProductController Test Suite")
class test_ProductController {

    @Mock
    private ProductService productService;

    @InjectMocks
    private ProductController productController;

    private MockMvc mockMvc;
    private List<ProductResponse> productList;

    /**
     * Setup method executed before each test
     * Initializes test data and MockMvc instance
     */
    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(productController).build();
        
        productList = Arrays.asList(
            ProductResponse.builder()
                .id(UUID.randomUUID())
                .name("Laptop")
                .description("High-performance laptop")
                .price(new BigDecimal("999.99"))
                .availableQty(10)
                .sku("LAP-001")
                .isActive(true)
                .build(),
            ProductResponse.builder()
                .id(UUID.randomUUID())
                .name("Mouse")
                .description("Wireless mouse")
                .price(new BigDecimal("29.99"))
                .availableQty(50)
                .sku("MOU-001")
                .isActive(true)
                .build()
        );
    }

    /**
     * Test successful product search with keyword
     * 
     * Verifies that:
     * - HTTP 200 OK status is returned
     * - Products matching keyword are returned
     * - Service method is called with correct keyword
     */
    @Test
    @DisplayName("Should successfully search products with keyword")
    void testSearchProducts_WithKeyword() throws Exception {
        // Arrange
        String keyword = "laptop";
        List<ProductResponse> filteredProducts = List.of(productList.get(0));
        
        when(productService.searchProducts(keyword))
            .thenReturn(filteredProducts);

        // Act & Assert
        mockMvc.perform(get("/products/search")
                .param("keyword", keyword))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$.length()").value(1))
            .andExpect(jsonPath("$[0].name").value("Laptop"))
            .andExpect(jsonPath("$[0].price").value(999.99));

        verify(productService, times(1)).searchProducts(keyword);
    }

    /**
     * Test product search without keyword
     * 
     * Verifies that:
     * - HTTP 200 OK status is returned
     * - All active products are returned
     * - Service method is called with null keyword
     */
    @Test
    @DisplayName("Should return all products when no keyword provided")
    void testSearchProducts_WithoutKeyword() throws Exception {
        // Arrange
        when(productService.searchProducts(null))
            .thenReturn(productList);

        // Act & Assert
        mockMvc.perform(get("/products/search"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$.length()").value(2))
            .andExpect(jsonPath("$[0].name").value("Laptop"))
            .andExpect(jsonPath("$[1].name").value("Mouse"));

        verify(productService, times(1)).searchProducts(null);
    }

    /**
     * Test product search with empty keyword
     * 
     * Verifies that:
     * - HTTP 200 OK status is returned
     * - All products are returned for empty keyword
     */
    @Test
    @DisplayName("Should return all products when empty keyword provided")
    void testSearchProducts_EmptyKeyword() throws Exception {
        // Arrange
        when(productService.searchProducts(""))
            .thenReturn(productList);

        // Act & Assert
        mockMvc.perform(get("/products/search")
                .param("keyword", ""))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$.length()").value(2));

        verify(productService, times(1)).searchProducts("");
    }

    /**
     * Test product search with no matching results
     * 
     * Verifies that:
     * - HTTP 200 OK status is returned
     * - Empty list is returned when no products match
     */
    @Test
    @DisplayName("Should return empty list when no products match keyword")
    void testSearchProducts_NoResults() throws Exception {
        // Arrange
        String keyword = "nonexistent";
        when(productService.searchProducts(keyword))
            .thenReturn(new ArrayList<>());

        // Act & Assert
        mockMvc.perform(get("/products/search")
                .param("keyword", keyword))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$.length()").value(0));

        verify(productService, times(1)).searchProducts(keyword);
    }

    /**
     * Test product search with special characters
     * 
     * Verifies that:
     * - HTTP 200 OK status is returned
     * - Special characters in keyword are handled correctly
     */
    @Test
    @DisplayName("Should handle special characters in search keyword")
    void testSearchProducts_SpecialCharacters() throws Exception {
        // Arrange
        String keyword = "laptop@#$%";
        when(productService.searchProducts(keyword))
            .thenReturn(new ArrayList<>());

        // Act & Assert
        mockMvc.perform(get("/products/search")
                .param("keyword", keyword))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isArray());

        verify(productService, times(1)).searchProducts(keyword);
    }

    /**
     * Test product search with long keyword
     * 
     * Verifies that:
     * - HTTP 200 OK status is returned
     * - Long keywords are handled correctly
     */
    @Test
    @DisplayName("Should handle long search keywords")
    void testSearchProducts_LongKeyword() throws Exception {
        // Arrange
        String keyword = "a".repeat(100);
        when(productService.searchProducts(keyword))
            .thenReturn(new ArrayList<>());

        // Act & Assert
        mockMvc.perform(get("/products/search")
                .param("keyword", keyword))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isArray());

        verify(productService, times(1)).searchProducts(keyword);
    }

    /**
     * Test product search returns correct product details
     * 
     * Verifies that:
     * - All product fields are returned correctly
     * - Data types and values are accurate
     */
    @Test
    @DisplayName("Should return complete product details in search results")
    void testSearchProducts_CompleteProductDetails() throws Exception {
        // Arrange
        ProductResponse product = productList.get(0);
        when(productService.searchProducts(any()))
            .thenReturn(List.of(product));

        // Act & Assert
        mockMvc.perform(get("/products/search")
                .param("keyword", "laptop"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].id").value(product.getId().toString()))
            .andExpect(jsonPath("$[0].name").value(product.getName()))
            .andExpect(jsonPath("$[0].description").value(product.getDescription()))
            .andExpect(jsonPath("$[0].price").value(product.getPrice().doubleValue()))
            .andExpect(jsonPath("$[0].availableQty").value(product.getAvailableQty()))
            .andExpect(jsonPath("$[0].sku").value(product.getSku()))
            .andExpect(jsonPath("$[0].isActive").value(product.getIsActive()));

        verify(productService, times(1)).searchProducts(any());
    }
}