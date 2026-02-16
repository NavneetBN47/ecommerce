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
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * JUnit 5 test class for ProductController
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
            .description("High-performance laptop")
            .price(BigDecimal.valueOf(999.99))
            .availableQty(10)
            .build();
            
        ProductResponse product2 = ProductResponse.builder()
            .id(UUID.randomUUID())
            .name("Laptop Stand")
            .description("Ergonomic laptop stand")
            .price(BigDecimal.valueOf(49.99))
            .availableQty(25)
            .build();
            
        productList.add(product1);
        productList.add(product2);
    }

    /**
     * Test searching products with valid keyword
     * Verifies HTTP 200 status and product list
     */
    @Test
    void searchProductsShouldReturnOkStatusWithProductList() {
        // Given
        String keyword = "laptop";
        when(productService.searchProducts(keyword)).thenReturn(productList);

        // When
        ResponseEntity<List<ProductResponse>> response = productController.searchProducts(keyword);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().size());
        verify(productService, times(1)).searchProducts(keyword);
    }

    /**
     * Test searching products with no results
     * Verifies empty list is returned
     */
    @Test
    void searchProductsShouldReturnEmptyListWhenNoMatch() {
        // Given
        String keyword = "nonexistent";
        when(productService.searchProducts(keyword)).thenReturn(new ArrayList<>());

        // When
        ResponseEntity<List<ProductResponse>> response = productController.searchProducts(keyword);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isEmpty());
        verify(productService, times(1)).searchProducts(keyword);
    }

    /**
     * Test searching products with empty keyword
     * Verifies validation exception handling
     */
    @Test
    void searchProductsShouldHandleEmptyKeyword() {
        // Given
        String emptyKeyword = "";
        when(productService.searchProducts(emptyKeyword))
            .thenThrow(new RuntimeException("Search keyword cannot be empty"));

        // When/Then
        assertThrows(RuntimeException.class, () -> {
            productController.searchProducts(emptyKeyword);
        });
        verify(productService, times(1)).searchProducts(emptyKeyword);
    }

    /**
     * Test searching products with null keyword
     * Verifies null pointer exception handling
     */
    @Test
    void searchProductsShouldHandleNullKeyword() {
        // Given
        when(productService.searchProducts(null))
            .thenThrow(new RuntimeException("Search keyword cannot be null"));

        // When/Then
        assertThrows(RuntimeException.class, () -> {
            productController.searchProducts(null);
        });
    }

    /**
     * Test searching products with whitespace keyword
     * Verifies trimming and validation
     */
    @Test
    void searchProductsShouldHandleWhitespaceKeyword() {
        // Given
        String whitespaceKeyword = "   ";
        when(productService.searchProducts(whitespaceKeyword))
            .thenThrow(new RuntimeException("Search keyword cannot be empty"));

        // When/Then
        assertThrows(RuntimeException.class, () -> {
            productController.searchProducts(whitespaceKeyword);
        });
    }

    /**
     * Test searching products with special characters
     * Verifies proper handling of special characters in search
     */
    @Test
    void searchProductsShouldHandleSpecialCharacters() {
        // Given
        String keyword = "laptop@#$";
        when(productService.searchProducts(keyword)).thenReturn(new ArrayList<>());

        // When
        ResponseEntity<List<ProductResponse>> response = productController.searchProducts(keyword);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(productService, times(1)).searchProducts(keyword);
    }

    /**
     * Test searching products with case-insensitive keyword
     * Verifies case-insensitive search functionality
     */
    @Test
    void searchProductsShouldBeCaseInsensitive() {
        // Given
        String keyword = "LAPTOP";
        when(productService.searchProducts(keyword)).thenReturn(productList);

        // When
        ResponseEntity<List<ProductResponse>> response = productController.searchProducts(keyword);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(2, response.getBody().size());
        verify(productService, times(1)).searchProducts(keyword);
    }

    /**
     * Test searching products with partial match
     * Verifies partial keyword matching
     */
    @Test
    void searchProductsShouldSupportPartialMatch() {
        // Given
        String keyword = "lap";
        when(productService.searchProducts(keyword)).thenReturn(productList);

        // When
        ResponseEntity<List<ProductResponse>> response = productController.searchProducts(keyword);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(2, response.getBody().size());
        verify(productService, times(1)).searchProducts(keyword);
    }

    /**
     * Test searching products returns correct product details
     * Verifies product response structure
     */
    @Test
    void searchProductsShouldReturnCompleteProductDetails() {
        // Given
        String keyword = "laptop";
        when(productService.searchProducts(keyword)).thenReturn(productList);

        // When
        ResponseEntity<List<ProductResponse>> response = productController.searchProducts(keyword);

        // Then
        assertNotNull(response.getBody());
        ProductResponse product = response.getBody().get(0);
        assertNotNull(product.getId());
        assertNotNull(product.getName());
        assertNotNull(product.getDescription());
        assertNotNull(product.getPrice());
        assertTrue(product.getAvailableQty() >= 0);
    }
}