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
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Unit test class for ProductController
 * Tests product search and retrieval operations
 */
@ExtendWith(MockitoExtension.class)
class test_ProductController {

    @Mock
    private ProductService productService;

    @InjectMocks
    private ProductController productController;

    private ProductResponse productResponse1;
    private ProductResponse productResponse2;
    private String searchKeyword;

    @BeforeEach
    void setUp() {
        searchKeyword = "laptop";

        productResponse1 = new ProductResponse();
        productResponse1.setId(UUID.randomUUID());
        productResponse1.setName("Dell Laptop");
        productResponse1.setDescription("High performance laptop");
        productResponse1.setPrice(BigDecimal.valueOf(999.99));
        productResponse1.setStockQuantity(10);

        productResponse2 = new ProductResponse();
        productResponse2.setId(UUID.randomUUID());
        productResponse2.setName("HP Laptop");
        productResponse2.setDescription("Business laptop");
        productResponse2.setPrice(BigDecimal.valueOf(899.99));
        productResponse2.setStockQuantity(5);
    }

    /**
     * Test searching products successfully
     * Verifies that products can be searched by keyword
     */
    @Test
    void testSearchProducts_Success() {
        List<ProductResponse> products = Arrays.asList(productResponse1, productResponse2);
        when(productService.searchProducts(any(String.class))).thenReturn(products);

        ResponseEntity<List<ProductResponse>> response = productController.searchProducts(searchKeyword);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().size());
        assertEquals("Dell Laptop", response.getBody().get(0).getName());
        assertEquals("HP Laptop", response.getBody().get(1).getName());
        verify(productService, times(1)).searchProducts(eq(searchKeyword));
    }

    /**
     * Test searching products with no results
     * Verifies proper handling when no products match the search
     */
    @Test
    void testSearchProducts_NoResults() {
        when(productService.searchProducts(any(String.class))).thenReturn(Collections.emptyList());

        ResponseEntity<List<ProductResponse>> response = productController.searchProducts("nonexistent");

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isEmpty());
        verify(productService, times(1)).searchProducts(eq("nonexistent"));
    }

    /**
     * Test searching products with empty keyword
     * Verifies handling of empty search keyword
     */
    @Test
    void testSearchProducts_EmptyKeyword() {
        when(productService.searchProducts(any(String.class))).thenReturn(Collections.emptyList());

        ResponseEntity<List<ProductResponse>> response = productController.searchProducts("");

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(productService, times(1)).searchProducts(eq(""));
    }

    /**
     * Test searching products with null keyword
     * Verifies proper handling of null keyword
     */
    @Test
    void testSearchProducts_NullKeyword() {
        when(productService.searchProducts(null))
            .thenThrow(new IllegalArgumentException("Keyword cannot be null"));

        assertThrows(IllegalArgumentException.class, () -> {
            productController.searchProducts(null);
        });
        verify(productService, times(1)).searchProducts(null);
    }

    /**
     * Test searching products with special characters
     * Verifies proper handling of special characters in search
     */
    @Test
    void testSearchProducts_SpecialCharacters() {
        String specialKeyword = "laptop@#$%";
        when(productService.searchProducts(any(String.class))).thenReturn(Collections.emptyList());

        ResponseEntity<List<ProductResponse>> response = productController.searchProducts(specialKeyword);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(productService, times(1)).searchProducts(eq(specialKeyword));
    }

    /**
     * Test searching products with whitespace keyword
     * Verifies proper handling of whitespace in search
     */
    @Test
    void testSearchProducts_WhitespaceKeyword() {
        String whitespaceKeyword = "   laptop   ";
        List<ProductResponse> products = Arrays.asList(productResponse1);
        when(productService.searchProducts(any(String.class))).thenReturn(products);

        ResponseEntity<List<ProductResponse>> response = productController.searchProducts(whitespaceKeyword);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        verify(productService, times(1)).searchProducts(eq(whitespaceKeyword));
    }

    /**
     * Test searching products with case insensitive keyword
     * Verifies case insensitive search functionality
     */
    @Test
    void testSearchProducts_CaseInsensitive() {
        String uppercaseKeyword = "LAPTOP";
        List<ProductResponse> products = Arrays.asList(productResponse1, productResponse2);
        when(productService.searchProducts(any(String.class))).thenReturn(products);

        ResponseEntity<List<ProductResponse>> response = productController.searchProducts(uppercaseKeyword);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().size());
        verify(productService, times(1)).searchProducts(eq(uppercaseKeyword));
    }

    /**
     * Test searching products when service throws exception
     * Verifies proper error handling when service fails
     */
    @Test
    void testSearchProducts_ServiceException() {
        when(productService.searchProducts(any(String.class)))
            .thenThrow(new RuntimeException("Database connection error"));

        assertThrows(RuntimeException.class, () -> {
            productController.searchProducts(searchKeyword);
        });
        verify(productService, times(1)).searchProducts(eq(searchKeyword));
    }

    /**
     * Test searching products with partial match
     * Verifies partial keyword matching functionality
     */
    @Test
    void testSearchProducts_PartialMatch() {
        String partialKeyword = "lap";
        List<ProductResponse> products = Arrays.asList(productResponse1, productResponse2);
        when(productService.searchProducts(any(String.class))).thenReturn(products);

        ResponseEntity<List<ProductResponse>> response = productController.searchProducts(partialKeyword);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().size());
        verify(productService, times(1)).searchProducts(eq(partialKeyword));
    }
}