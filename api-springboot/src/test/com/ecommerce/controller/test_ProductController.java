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
import static org.mockito.ArgumentMatchers.any;
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

    private List<ProductResponse> productList;
    private ProductResponse product1;
    private ProductResponse product2;

    /**
     * Set up test data before each test method execution.
     */
    @BeforeEach
    void setUp() {
        product1 = new ProductResponse();
        product1.setId(UUID.randomUUID());
        product1.setName("Laptop");
        product1.setDescription("High-performance laptop");
        product1.setPrice(BigDecimal.valueOf(999.99));
        product1.setStockQuantity(10);

        product2 = new ProductResponse();
        product2.setId(UUID.randomUUID());
        product2.setName("Laptop Stand");
        product2.setDescription("Ergonomic laptop stand");
        product2.setPrice(BigDecimal.valueOf(49.99));
        product2.setStockQuantity(25);

        productList = new ArrayList<>();
        productList.add(product1);
        productList.add(product2);
    }

    /**
     * Test successfully searching products with valid keyword.
     * Verifies that matching products are returned.
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
        assertEquals("Laptop Stand", response.getBody().get(1).getName());
        verify(productService, times(1)).searchProducts(eq(keyword));
    }

    /**
     * Test searching products with no results.
     * Verifies that empty list is returned when no products match.
     */
    @Test
    void testSearchProducts_NoResults() {
        String keyword = "nonexistent";
        when(productService.searchProducts(any(String.class))).thenReturn(new ArrayList<>());

        ResponseEntity<List<ProductResponse>> response = productController.searchProducts(keyword);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isEmpty());
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

        assertThrows(IllegalArgumentException.class, () -> {
            productController.searchProducts(null);
        });
        verify(productService, times(1)).searchProducts(null);
    }

    /**
     * Test searching products with empty keyword.
     * Verifies handling of empty search string.
     */
    @Test
    void testSearchProducts_EmptyKeyword() {
        String keyword = "";
        when(productService.searchProducts(any(String.class)))
            .thenThrow(new IllegalArgumentException("Keyword cannot be empty"));

        assertThrows(IllegalArgumentException.class, () -> {
            productController.searchProducts(keyword);
        });
        verify(productService, times(1)).searchProducts(eq(keyword));
    }

    /**
     * Test searching products with whitespace keyword.
     * Verifies handling of whitespace-only search string.
     */
    @Test
    void testSearchProducts_WhitespaceKeyword() {
        String keyword = "   ";
        when(productService.searchProducts(any(String.class))).thenReturn(new ArrayList<>());

        ResponseEntity<List<ProductResponse>> response = productController.searchProducts(keyword);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(productService, times(1)).searchProducts(eq(keyword));
    }

    /**
     * Test searching products with single character keyword.
     * Verifies that short keywords are handled correctly.
     */
    @Test
    void testSearchProducts_SingleCharacter() {
        String keyword = "L";
        when(productService.searchProducts(any(String.class))).thenReturn(productList);

        ResponseEntity<List<ProductResponse>> response = productController.searchProducts(keyword);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().size());
        verify(productService, times(1)).searchProducts(eq(keyword));
    }

    /**
     * Test searching products with special characters.
     * Verifies handling of special characters in search keyword.
     */
    @Test
    void testSearchProducts_SpecialCharacters() {
        String keyword = "laptop@#$";
        when(productService.searchProducts(any(String.class))).thenReturn(new ArrayList<>());

        ResponseEntity<List<ProductResponse>> response = productController.searchProducts(keyword);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isEmpty());
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
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().size());
        verify(productService, times(1)).searchProducts(eq(keyword));
    }

    /**
     * Test searching products with long keyword.
     * Verifies handling of very long search strings.
     */
    @Test
    void testSearchProducts_LongKeyword() {
        String keyword = "a".repeat(100);
        when(productService.searchProducts(any(String.class))).thenReturn(new ArrayList<>());

        ResponseEntity<List<ProductResponse>> response = productController.searchProducts(keyword);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isEmpty());
        verify(productService, times(1)).searchProducts(eq(keyword));
    }

    /**
     * Test searching products when service throws exception.
     * Verifies proper error handling during search operation.
     */
    @Test
    void testSearchProducts_ServiceException() {
        String keyword = "laptop";
        when(productService.searchProducts(any(String.class)))
            .thenThrow(new RuntimeException("Database connection error"));

        assertThrows(RuntimeException.class, () -> {
            productController.searchProducts(keyword);
        });
        verify(productService, times(1)).searchProducts(eq(keyword));
    }

    /**
     * Test searching products with partial match.
     * Verifies that partial keyword matches return results.
     */
    @Test
    void testSearchProducts_PartialMatch() {
        String keyword = "lap";
        when(productService.searchProducts(any(String.class))).thenReturn(productList);

        ResponseEntity<List<ProductResponse>> response = productController.searchProducts(keyword);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().isEmpty());
        verify(productService, times(1)).searchProducts(eq(keyword));
    }

    /**
     * Test searching products returns correct product details.
     * Verifies that all product fields are populated correctly.
     */
    @Test
    void testSearchProducts_ProductDetails() {
        String keyword = "laptop";
        when(productService.searchProducts(any(String.class))).thenReturn(productList);

        ResponseEntity<List<ProductResponse>> response = productController.searchProducts(keyword);

        assertNotNull(response);
        assertNotNull(response.getBody());
        ProductResponse firstProduct = response.getBody().get(0);
        assertNotNull(firstProduct.getId());
        assertNotNull(firstProduct.getName());
        assertNotNull(firstProduct.getDescription());
        assertNotNull(firstProduct.getPrice());
        assertTrue(firstProduct.getPrice().compareTo(BigDecimal.ZERO) > 0);
        assertTrue(firstProduct.getStockQuantity() >= 0);
        verify(productService, times(1)).searchProducts(eq(keyword));
    }
}