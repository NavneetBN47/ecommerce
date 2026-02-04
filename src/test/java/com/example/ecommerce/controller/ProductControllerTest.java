package com.example.ecommerce.controller;

import com.example.ecommerce.dto.ProductResponse;
import com.example.ecommerce.service.ProductService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Test class for ProductController.
 * Tests product search functionality and endpoint behavior.
 */
@WebMvcTest(ProductController.class)
class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ProductService productService;

    private List<ProductResponse> productResponses;

    @BeforeEach
    void setUp() {
        ProductResponse product1 = new ProductResponse(
                UUID.randomUUID(),
                "Laptop",
                "High-performance laptop with 16GB RAM",
                BigDecimal.valueOf(1299.99),
                50
        );

        ProductResponse product2 = new ProductResponse(
                UUID.randomUUID(),
                "Gaming Laptop",
                "Gaming laptop with RTX graphics",
                BigDecimal.valueOf(1499.99),
                25
        );

        productResponses = Arrays.asList(product1, product2);
    }

    /**
     * Test successful product search with results.
     */
    @Test
    void searchProducts_WithResults_ShouldReturnProductList() throws Exception {
        // Given
        String searchTerm = "laptop";
        when(productService.searchProducts(searchTerm)).thenReturn(productResponses);

        // When & Then
        mockMvc.perform(get("/api/products/search")
                        .param("q", searchTerm))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].name").value("Laptop"))
                .andExpect(jsonPath("$[0].price").value(1299.99))
                .andExpect(jsonPath("$[1].name").value("Gaming Laptop"))
                .andExpect(jsonPath("$[1].price").value(1499.99));

        verify(productService).searchProducts(searchTerm);
    }

    /**
     * Test product search with no results.
     */
    @Test
    void searchProducts_NoResults_ShouldReturnEmptyList() throws Exception {
        // Given
        String searchTerm = "nonexistent";
        when(productService.searchProducts(searchTerm)).thenReturn(Collections.emptyList());

        // When & Then
        mockMvc.perform(get("/api/products/search")
                        .param("q", searchTerm))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));

        verify(productService).searchProducts(searchTerm);
    }

    /**
     * Test product search with empty query parameter.
     */
    @Test
    void searchProducts_EmptyQuery_ShouldReturnResults() throws Exception {
        // Given
        String searchTerm = "";
        when(productService.searchProducts(searchTerm)).thenReturn(productResponses);

        // When & Then
        mockMvc.perform(get("/api/products/search")
                        .param("q", searchTerm))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2));

        verify(productService).searchProducts(searchTerm);
    }

    /**
     * Test product search without query parameter.
     */
    @Test
    void searchProducts_MissingQueryParam_ShouldReturn400() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/products/search"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(productService);
    }

    /**
     * Test product search with whitespace-only query.
     */
    @Test
    void searchProducts_WhitespaceQuery_ShouldPassToService() throws Exception {
        // Given
        String searchTerm = "  laptop  ";
        when(productService.searchProducts(searchTerm)).thenReturn(productResponses);

        // When & Then
        mockMvc.perform(get("/api/products/search")
                        .param("q", searchTerm))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2));

        verify(productService).searchProducts(searchTerm);
    }

    /**
     * Test product search with special characters.
     */
    @Test
    void searchProducts_SpecialCharacters_ShouldHandleGracefully() throws Exception {
        // Given
        String searchTerm = "laptop@#$%";
        when(productService.searchProducts(searchTerm)).thenReturn(Collections.emptyList());

        // When & Then
        mockMvc.perform(get("/api/products/search")
                        .param("q", searchTerm))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));

        verify(productService).searchProducts(searchTerm);
    }

    /**
     * Test product search case handling.
     */
    @Test
    void searchProducts_CaseVariations_ShouldReturnResults() throws Exception {
        // Given
        String searchTerm = "LAPTOP";
        when(productService.searchProducts(searchTerm)).thenReturn(productResponses);

        // When & Then
        mockMvc.perform(get("/api/products/search")
                        .param("q", searchTerm))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2));

        verify(productService).searchProducts(searchTerm);
    }

    /**
     * Test product search with very long query string.
     */
    @Test
    void searchProducts_LongQuery_ShouldHandleGracefully() throws Exception {
        // Given
        String searchTerm = "a".repeat(1000);
        when(productService.searchProducts(searchTerm)).thenReturn(Collections.emptyList());

        // When & Then
        mockMvc.perform(get("/api/products/search")
                        .param("q", searchTerm))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));

        verify(productService).searchProducts(searchTerm);
    }
}