package com.ecommerce.controller;

import com.ecommerce.dto.ProductResponse;
import com.ecommerce.service.ProductService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

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
 * Test class for ProductController
 * Tests product search operations
 */
@ExtendWith(MockitoExtension.class)
class test_ProductController {

    @Mock
    private ProductService productService;

    @InjectMocks
    private ProductController productController;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(productController).build();
    }

    /**
     * Test searching products successfully
     * Verifies that a valid search keyword returns HTTP 200 with matching products
     */
    @Test
    void testSearchProducts_Success() throws Exception {
        ProductResponse product1 = new ProductResponse();
        product1.setId(UUID.randomUUID());
        product1.setName("Laptop");
        product1.setPrice(BigDecimal.valueOf(999.99));

        ProductResponse product2 = new ProductResponse();
        product2.setId(UUID.randomUUID());
        product2.setName("Laptop Bag");
        product2.setPrice(BigDecimal.valueOf(49.99));

        List<ProductResponse> products = Arrays.asList(product1, product2);

        when(productService.searchProducts(anyString())).thenReturn(products);

        mockMvc.perform(get("/api/products/search")
                .param("keyword", "laptop"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Laptop"))
                .andExpect(jsonPath("$[1].name").value("Laptop Bag"))
                .andExpect(jsonPath("$.length()").value(2));

        verify(productService, times(1)).searchProducts("laptop");
    }

    /**
     * Test searching products with no results
     * Verifies that search with no matches returns empty list
     */
    @Test
    void testSearchProducts_NoResults() throws Exception {
        when(productService.searchProducts(anyString())).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/products/search")
                .param("keyword", "nonexistent"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));

        verify(productService, times(1)).searchProducts("nonexistent");
    }

    /**
     * Test searching products with empty keyword
     * Verifies that empty keyword is handled appropriately
     */
    @Test
    void testSearchProducts_EmptyKeyword() throws Exception {
        when(productService.searchProducts(anyString())).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/products/search")
                .param("keyword", ""))
                .andExpect(status().isOk());

        verify(productService, times(1)).searchProducts("");
    }

    /**
     * Test searching products without keyword parameter
     * Verifies that missing keyword parameter is rejected
     */
    @Test
    void testSearchProducts_MissingKeyword() throws Exception {
        mockMvc.perform(get("/api/products/search"))
                .andExpect(status().isBadRequest());

        verify(productService, never()).searchProducts(anyString());
    }

    /**
     * Test searching products with special characters
     * Verifies that special characters in keyword are handled correctly
     */
    @Test
    void testSearchProducts_SpecialCharacters() throws Exception {
        ProductResponse product = new ProductResponse();
        product.setId(UUID.randomUUID());
        product.setName("Product & Service");
        product.setPrice(BigDecimal.valueOf(99.99));

        when(productService.searchProducts(anyString())).thenReturn(Collections.singletonList(product));

        mockMvc.perform(get("/api/products/search")
                .param("keyword", "product&service"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Product & Service"));

        verify(productService, times(1)).searchProducts("product&service");
    }

    /**
     * Test searching products with case sensitivity
     * Verifies that search handles different cases correctly
     */
    @Test
    void testSearchProducts_CaseInsensitive() throws Exception {
        ProductResponse product = new ProductResponse();
        product.setId(UUID.randomUUID());
        product.setName("LAPTOP");
        product.setPrice(BigDecimal.valueOf(999.99));

        when(productService.searchProducts(anyString())).thenReturn(Collections.singletonList(product));

        mockMvc.perform(get("/api/products/search")
                .param("keyword", "LAPTOP"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("LAPTOP"));

        verify(productService, times(1)).searchProducts("LAPTOP");
    }

    /**
     * Test searching products with service exception
     * Verifies proper error handling when product service fails
     */
    @Test
    void testSearchProducts_ServiceException() throws Exception {
        when(productService.searchProducts(anyString()))
                .thenThrow(new RuntimeException("Database error"));

        mockMvc.perform(get("/api/products/search")
                .param("keyword", "laptop"))
                .andExpect(status().isInternalServerError());

        verify(productService, times(1)).searchProducts("laptop");
    }

    /**
     * Test searching products with long keyword
     * Verifies that long search keywords are handled correctly
     */
    @Test
    void testSearchProducts_LongKeyword() throws Exception {
        String longKeyword = "a".repeat(100);
        when(productService.searchProducts(anyString())).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/products/search")
                .param("keyword", longKeyword))
                .andExpect(status().isOk());

        verify(productService, times(1)).searchProducts(longKeyword);
    }
}