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
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * JUnit 5 test class for ProductController.
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
        productResponse1.setId(UUID.randomUUID());
        productResponse1.setName("Laptop");
        productResponse1.setDescription("High performance laptop");
        productResponse1.setPrice(BigDecimal.valueOf(999.99));
        productResponse1.setStock(10);

        productResponse2 = new ProductResponse();
        productResponse2.setId(UUID.randomUUID());
        productResponse2.setName("Laptop Bag");
        productResponse2.setDescription("Durable laptop bag");
        productResponse2.setPrice(BigDecimal.valueOf(49.99));
        productResponse2.setStock(25);

        productList = Arrays.asList(productResponse1, productResponse2);
    }

    /**
     * Test searching products successfully with valid keyword.
     * Verifies that matching products are returned with HTTP 200 OK status.
     */
    @Test
    void testSearchProducts_Success() {
        String keyword = "laptop";
        when(productService.searchProducts(eq(keyword))).thenReturn(productList);

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
        when(productService.searchProducts(eq(keyword))).thenReturn(Collections.emptyList());

        ResponseEntity<List<ProductResponse>> response = productController.searchProducts(keyword);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isEmpty());
        verify(productService, times(1)).searchProducts(eq(keyword));
    }

    /**
     * Test searching products with empty keyword.
     * Verifies handling of empty search terms.
     */
    @Test
    void testSearchProducts_EmptyKeyword() {
        String emptyKeyword = "";
        when(productService.searchProducts(eq(emptyKeyword))).thenReturn(Collections.emptyList());

        ResponseEntity<List<ProductResponse>> response = productController.searchProducts(emptyKeyword);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        verify(productService, times(1)).searchProducts(eq(emptyKeyword));
    }

    /**
     * Test searching products with null keyword.
     * Verifies proper null handling.
     */
    @Test
    void testSearchProducts_NullKeyword() {
        when(productService.searchProducts(null))
            .thenThrow(new IllegalArgumentException("Keyword cannot be null"));

        assertThrows(IllegalArgumentException.class, () -> {
            productController.searchProducts(null);
        });
    }

    /**
     * Test searching products with special characters.
     * Verifies that special characters in search terms are handled correctly.
     */
    @Test
    void testSearchProducts_SpecialCharacters() {
        String specialKeyword = "laptop@#$";
        when(productService.searchProducts(eq(specialKeyword))).thenReturn(Collections.emptyList());

        ResponseEntity<List<ProductResponse>> response = productController.searchProducts(specialKeyword);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(productService, times(1)).searchProducts(eq(specialKeyword));
    }

    /**
     * Test searching products with case insensitive keyword.
     * Verifies that search is case insensitive.
     */
    @Test
    void testSearchProducts_CaseInsensitive() {
        String upperCaseKeyword = "LAPTOP";
        when(productService.searchProducts(eq(upperCaseKeyword))).thenReturn(productList);

        ResponseEntity<List<ProductResponse>> response = productController.searchProducts(upperCaseKeyword);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(2, response.getBody().size());
        verify(productService, times(1)).searchProducts(eq(upperCaseKeyword));
    }

    /**
     * Test searching products with whitespace keyword.
     * Verifies handling of whitespace in search terms.
     */
    @Test
    void testSearchProducts_WhitespaceKeyword() {
        String whitespaceKeyword = "  laptop  ";
        when(productService.searchProducts(eq(whitespaceKeyword))).thenReturn(productList);

        ResponseEntity<List<ProductResponse>> response = productController.searchProducts(whitespaceKeyword);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(productService, times(1)).searchProducts(eq(whitespaceKeyword));
    }

    /**
     * Test searching products when service throws exception.
     * Verifies that exceptions from service layer are properly propagated.
     */
    @Test
    void testSearchProducts_ServiceException() {
        String keyword = "laptop";
        when(productService.searchProducts(anyString()))
            .thenThrow(new RuntimeException("Database connection error"));

        assertThrows(RuntimeException.class, () -> {
            productController.searchProducts(keyword);
        });
        verify(productService, times(1)).searchProducts(eq(keyword));
    }

    /**
     * Test searching products with very long keyword.
     * Verifies handling of edge case with long search terms.
     */
    @Test
    void testSearchProducts_LongKeyword() {
        String longKeyword = "a".repeat(1000);
        when(productService.searchProducts(eq(longKeyword))).thenReturn(Collections.emptyList());

        ResponseEntity<List<ProductResponse>> response = productController.searchProducts(longKeyword);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(productService, times(1)).searchProducts(eq(longKeyword));
    }
}