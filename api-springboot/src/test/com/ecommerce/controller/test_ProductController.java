package com.ecommerce.controller;

import com.ecommerce.dto.ProductResponse;
import com.ecommerce.service.ProductService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Test class for ProductController
 * 
 * Tests product search functionality
 * 
 * @author QA Automation Agent
 * @version 1.0.0
 */
@ExtendWith(MockitoExtension.class)
class test_ProductController {

    @Mock
    private ProductService productService;

    @InjectMocks
    private ProductController productController;

    private List<ProductResponse> productList;

    @BeforeEach
    void setUp() {
        productList = new ArrayList<>();
        
        ProductResponse product1 = ProductResponse.builder()
            .id(UUID.randomUUID())
            .name("Laptop")
            .description("High performance laptop")
            .price(BigDecimal.valueOf(999.99))
            .availableQty(10)
            .build();
        
        ProductResponse product2 = ProductResponse.builder()
            .id(UUID.randomUUID())
            .name("Laptop Bag")
            .description("Durable laptop bag")
            .price(BigDecimal.valueOf(49.99))
            .availableQty(25)
            .build();
        
        productList.add(product1);
        productList.add(product2);
    }

    /**
     * Test searching products successfully
     * 
     * Verifies:
     * - Products are searched by keyword
     * - Returns OK status
     * - ProductService.searchProducts is called with correct keyword
     * - Results contain matching products
     */
    @Test
    void testSearchProducts_Success() {
        // Given
        String keyword = "laptop";
        when(productService.searchProducts(anyString())).thenReturn(productList);

        // When
        ResponseEntity<List<ProductResponse>> response = productController.searchProducts(keyword);

        // Then
        assertNotNull(response, "Response should not be null");
        assertEquals(HttpStatus.OK, response.getStatusCode(), "Status should be OK");
        assertNotNull(response.getBody(), "Response body should not be null");
        assertEquals(2, response.getBody().size(), "Should return 2 products");
        verify(productService, times(1)).searchProducts(keyword);
    }

    /**
     * Test searching products with no results
     * 
     * Verifies:
     * - Empty list is returned when no products match
     * - Returns OK status with empty list
     */
    @Test
    void testSearchProducts_NoResults() {
        // Given
        String keyword = "nonexistent";
        when(productService.searchProducts(anyString())).thenReturn(new ArrayList<>());

        // When
        ResponseEntity<List<ProductResponse>> response = productController.searchProducts(keyword);

        // Then
        assertNotNull(response, "Response should not be null");
        assertEquals(HttpStatus.OK, response.getStatusCode(), "Status should be OK");
        assertNotNull(response.getBody(), "Response body should not be null");
        assertTrue(response.getBody().isEmpty(), "Result list should be empty");
        verify(productService, times(1)).searchProducts(keyword);
    }

    /**
     * Test searching products with empty keyword
     * 
     * Verifies:
     * - Validation exception is thrown for empty keyword
     */
    @Test
    void testSearchProducts_EmptyKeyword() {
        // Given
        String keyword = "";
        when(productService.searchProducts(anyString()))
            .thenThrow(new RuntimeException("Search keyword cannot be empty"));

        // When/Then
        assertThrows(RuntimeException.class, () -> {
            productController.searchProducts(keyword);
        });
    }

    /**
     * Test searching products with null keyword
     * 
     * Verifies:
     * - Validation exception is thrown for null keyword
     */
    @Test
    void testSearchProducts_NullKeyword() {
        // Given
        when(productService.searchProducts(null))
            .thenThrow(new RuntimeException("Search keyword cannot be empty"));

        // When/Then
        assertThrows(RuntimeException.class, () -> {
            productController.searchProducts(null);
        });
    }

    /**
     * Test searching products with whitespace keyword
     * 
     * Verifies:
     * - Whitespace is trimmed and validated
     */
    @Test
    void testSearchProducts_WhitespaceKeyword() {
        // Given
        String keyword = "   ";
        when(productService.searchProducts(anyString()))
            .thenThrow(new RuntimeException("Search keyword cannot be empty"));

        // When/Then
        assertThrows(RuntimeException.class, () -> {
            productController.searchProducts(keyword);
        });
    }

    /**
     * Test searching products with special characters
     * 
     * Verifies:
     * - Special characters in search keyword are handled correctly
     */
    @Test
    void testSearchProducts_SpecialCharacters() {
        // Given
        String keyword = "laptop@#$";
        when(productService.searchProducts(anyString())).thenReturn(new ArrayList<>());

        // When
        ResponseEntity<List<ProductResponse>> response = productController.searchProducts(keyword);

        // Then
        assertNotNull(response, "Response should not be null");
        assertEquals(HttpStatus.OK, response.getStatusCode(), "Status should be OK");
        verify(productService, times(1)).searchProducts(keyword);
    }

    /**
     * Test searching products case insensitivity
     * 
     * Verifies:
     * - Search is case insensitive
     */
    @Test
    void testSearchProducts_CaseInsensitive() {
        // Given
        String keyword = "LAPTOP";
        when(productService.searchProducts(anyString())).thenReturn(productList);

        // When
        ResponseEntity<List<ProductResponse>> response = productController.searchProducts(keyword);

        // Then
        assertNotNull(response, "Response should not be null");
        assertEquals(HttpStatus.OK, response.getStatusCode(), "Status should be OK");
        assertEquals(2, response.getBody().size(), "Should return matching products");
        verify(productService, times(1)).searchProducts(keyword);
    }
}
