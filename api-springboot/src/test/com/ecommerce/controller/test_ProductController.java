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
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * JUnit test class for ProductController.
 * Tests product search and retrieval operations.
 */
@ExtendWith(MockitoExtension.class)
class test_ProductController {

    @Mock
    private ProductService productService;

    @InjectMocks
    private ProductController productController;

    private ProductResponse productResponse1;
    private ProductResponse productResponse2;
    private List<ProductResponse> productList;

    @BeforeEach
    void setUp() {
        productResponse1 = new ProductResponse();
        productResponse1.setId(1L);
        productResponse1.setName("Laptop");
        productResponse1.setDescription("High-performance laptop");
        productResponse1.setPrice(BigDecimal.valueOf(999.99));
        productResponse1.setStock(10);

        productResponse2 = new ProductResponse();
        productResponse2.setId(2L);
        productResponse2.setName("Laptop Bag");
        productResponse2.setDescription("Durable laptop bag");
        productResponse2.setPrice(BigDecimal.valueOf(49.99));
        productResponse2.setStock(25);

        productList = Arrays.asList(productResponse1, productResponse2);
    }

    /**
     * Test successfully searching products with keyword.
     * Verifies that products matching the keyword are returned.
     */
    @Test
    void testSearchProducts_Success() {
        String keyword = "laptop";
        when(productService.searchProducts(any(String.class))).thenReturn(productList);

        ResponseEntity<List<ProductResponse>> response = productController.searchProducts(keyword);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().size());
        assertEquals("Laptop", response.getBody().get(0).getName());
        assertEquals("Laptop Bag", response.getBody().get(1).getName());
        verify(productService, times(1)).searchProducts(eq(keyword));
    }

    /**
     * Test searching products with no results.
     * Verifies that empty list is returned when no products match.
     */
    @Test
    void testSearchProducts_NoResults() {
        String keyword = "nonexistent";
        when(productService.searchProducts(any(String.class))).thenReturn(Collections.emptyList());

        ResponseEntity<List<ProductResponse>> response = productController.searchProducts(keyword);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isEmpty());
        verify(productService, times(1)).searchProducts(eq(keyword));
    }

    /**
     * Test searching products with empty keyword.
     * Verifies handling of empty search keyword.
     */
    @Test
    void testSearchProducts_EmptyKeyword() {
        String keyword = "";
        when(productService.searchProducts(any(String.class))).thenReturn(Collections.emptyList());

        ResponseEntity<List<ProductResponse>> response = productController.searchProducts(keyword);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        verify(productService, times(1)).searchProducts(eq(keyword));
    }

    /**
     * Test searching products with null keyword.
     * Verifies proper handling of null keyword.
     */
    @Test
    void testSearchProducts_NullKeyword() {
        when(productService.searchProducts(null))
            .thenThrow(new IllegalArgumentException("Keyword cannot be null"));

        assertThrows(IllegalArgumentException.class, 
            () -> productController.searchProducts(null));
        verify(productService, times(1)).searchProducts(null);
    }

    /**
     * Test searching products with special characters.
     * Verifies handling of special characters in search keyword.
     */
    @Test
    void testSearchProducts_SpecialCharacters() {
        String keyword = "laptop@#$";
        when(productService.searchProducts(any(String.class))).thenReturn(Collections.emptyList());

        ResponseEntity<List<ProductResponse>> response = productController.searchProducts(keyword);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(productService, times(1)).searchProducts(eq(keyword));
    }

    /**
     * Test searching products with case-insensitive keyword.
     * Verifies that search is case-insensitive.
     */
    @Test
    void testSearchProducts_CaseInsensitive() {
        String keyword = "LAPTOP";
        when(productService.searchProducts(any(String.class))).thenReturn(productList);

        ResponseEntity<List<ProductResponse>> response = productController.searchProducts(keyword);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(2, response.getBody().size());
        verify(productService, times(1)).searchProducts(eq(keyword));
    }

    /**
     * Test searching products when service throws exception.
     * Verifies proper exception propagation from service layer.
     */
    @Test
    void testSearchProducts_ServiceException() {
        String keyword = "laptop";
        when(productService.searchProducts(any(String.class)))
            .thenThrow(new RuntimeException("Database connection error"));

        assertThrows(RuntimeException.class, 
            () -> productController.searchProducts(keyword));
        verify(productService, times(1)).searchProducts(eq(keyword));
    }

    /**
     * Test searching products with very long keyword.
     * Verifies handling of excessively long search keywords.
     */
    @Test
    void testSearchProducts_LongKeyword() {
        String keyword = "a".repeat(1000);
        when(productService.searchProducts(any(String.class))).thenReturn(Collections.emptyList());

        ResponseEntity<List<ProductResponse>> response = productController.searchProducts(keyword);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(productService, times(1)).searchProducts(eq(keyword));
    }
}