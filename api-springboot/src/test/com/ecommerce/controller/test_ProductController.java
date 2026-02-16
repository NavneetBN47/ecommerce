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
import static org.mockito.Mockito.*;

/**
 * Unit test class for ProductController
 * Tests product search operations
 */
@ExtendWith(MockitoExtension.class)
class test_ProductController {

    @Mock
    private ProductService productService;

    @InjectMocks
    private ProductController productController;

    private List<ProductResponse> productResponses;
    private ProductResponse productResponse1;
    private ProductResponse productResponse2;

    @BeforeEach
    void setUp() {
        productResponse1 = new ProductResponse();
        productResponse1.setId(UUID.randomUUID());
        productResponse1.setName("Laptop");
        productResponse1.setDescription("High performance laptop");
        productResponse1.setPrice(BigDecimal.valueOf(999.99));
        productResponse1.setStockQuantity(10);

        productResponse2 = new ProductResponse();
        productResponse2.setId(UUID.randomUUID());
        productResponse2.setName("Laptop Bag");
        productResponse2.setDescription("Protective laptop bag");
        productResponse2.setPrice(BigDecimal.valueOf(49.99));
        productResponse2.setStockQuantity(25);

        productResponses = Arrays.asList(productResponse1, productResponse2);
    }

    /**
     * Test successfully searching products with valid keyword
     */
    @Test
    void testSearchProducts_Success() {
        String keyword = "laptop";
        when(productService.searchProducts(anyString())).thenReturn(productResponses);

        ResponseEntity<List<ProductResponse>> response = productController.searchProducts(keyword);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().size());
        assertEquals(productResponse1.getName(), response.getBody().get(0).getName());
        assertEquals(productResponse2.getName(), response.getBody().get(1).getName());
        verify(productService, times(1)).searchProducts(eq(keyword));
    }

    /**
     * Test searching products with empty result
     */
    @Test
    void testSearchProducts_EmptyResult() {
        String keyword = "nonexistent";
        when(productService.searchProducts(anyString())).thenReturn(Collections.emptyList());

        ResponseEntity<List<ProductResponse>> response = productController.searchProducts(keyword);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isEmpty());
        verify(productService, times(1)).searchProducts(eq(keyword));
    }

    /**
     * Test searching products with null keyword
     */
    @Test
    void testSearchProducts_NullKeyword() {
        when(productService.searchProducts(any()))
            .thenThrow(new IllegalArgumentException("Keyword cannot be null"));

        assertThrows(IllegalArgumentException.class, () -> {
            productController.searchProducts(null);
        });
    }

    /**
     * Test searching products with empty keyword
     */
    @Test
    void testSearchProducts_EmptyKeyword() {
        String keyword = "";
        when(productService.searchProducts(anyString()))
            .thenThrow(new IllegalArgumentException("Keyword cannot be empty"));

        assertThrows(IllegalArgumentException.class, () -> {
            productController.searchProducts(keyword);
        });
    }

    /**
     * Test searching products with whitespace keyword
     */
    @Test
    void testSearchProducts_WhitespaceKeyword() {
        String keyword = "   ";
        when(productService.searchProducts(anyString())).thenReturn(Collections.emptyList());

        ResponseEntity<List<ProductResponse>> response = productController.searchProducts(keyword);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(productService, times(1)).searchProducts(eq(keyword));
    }

    /**
     * Test searching products with special characters
     */
    @Test
    void testSearchProducts_SpecialCharacters() {
        String keyword = "@#$%";
        when(productService.searchProducts(anyString())).thenReturn(Collections.emptyList());

        ResponseEntity<List<ProductResponse>> response = productController.searchProducts(keyword);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isEmpty());
        verify(productService, times(1)).searchProducts(eq(keyword));
    }

    /**
     * Test searching products with case-insensitive keyword
     */
    @Test
    void testSearchProducts_CaseInsensitive() {
        String keyword = "LAPTOP";
        when(productService.searchProducts(anyString())).thenReturn(productResponses);

        ResponseEntity<List<ProductResponse>> response = productController.searchProducts(keyword);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().size());
        verify(productService, times(1)).searchProducts(eq(keyword));
    }

    /**
     * Test searching products with partial match
     */
    @Test
    void testSearchProducts_PartialMatch() {
        String keyword = "lap";
        when(productService.searchProducts(anyString())).thenReturn(productResponses);

        ResponseEntity<List<ProductResponse>> response = productController.searchProducts(keyword);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().isEmpty());
        verify(productService, times(1)).searchProducts(eq(keyword));
    }

    /**
     * Test searching products when service throws exception
     */
    @Test
    void testSearchProducts_ServiceException() {
        String keyword = "laptop";
        when(productService.searchProducts(anyString()))
            .thenThrow(new RuntimeException("Database connection failed"));

        assertThrows(RuntimeException.class, () -> {
            productController.searchProducts(keyword);
        });
        verify(productService, times(1)).searchProducts(eq(keyword));
    }

    /**
     * Test searching products with long keyword
     */
    @Test
    void testSearchProducts_LongKeyword() {
        String keyword = "a".repeat(100);
        when(productService.searchProducts(anyString())).thenReturn(Collections.emptyList());

        ResponseEntity<List<ProductResponse>> response = productController.searchProducts(keyword);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(productService, times(1)).searchProducts(eq(keyword));
    }

    /**
     * Test searching products returns single result
     */
    @Test
    void testSearchProducts_SingleResult() {
        String keyword = "laptop bag";
        when(productService.searchProducts(anyString()))
            .thenReturn(Collections.singletonList(productResponse2));

        ResponseEntity<List<ProductResponse>> response = productController.searchProducts(keyword);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        assertEquals(productResponse2.getName(), response.getBody().get(0).getName());
        verify(productService, times(1)).searchProducts(eq(keyword));
    }
}