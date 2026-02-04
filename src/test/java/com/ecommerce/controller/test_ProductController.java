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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Comprehensive JUnit 5 test class for ProductController.
 * Tests all public endpoints with proper mocking of ProductService dependency.
 * Covers normal execution paths, edge cases, and exception scenarios.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ProductController Tests")
class test_ProductController {

    @Mock
    private ProductService productService;

    @InjectMocks
    private ProductController productController;

    /**
     * Test successful product search with all parameters.
     * Verifies that the controller returns product list with OK status.
     */
    @Test
    @DisplayName("Should search products successfully with all parameters")
    void testSearchProducts_WithAllParameters() {
        // Given
        String search = "laptop";
        Integer page = 0;
        Integer size = 10;
        
        List<ProductResponse> expectedProducts = Arrays.asList(
            ProductResponse.builder()
                .productId(1L)
                .productName("Gaming Laptop")
                .description("High performance gaming laptop")
                .price(BigDecimal.valueOf(1500.00))
                .availableQty(5)
                .isActive(true)
                .build(),
            ProductResponse.builder()
                .productId(2L)
                .productName("Business Laptop")
                .description("Professional business laptop")
                .price(BigDecimal.valueOf(1200.00))
                .availableQty(10)
                .isActive(true)
                .build()
        );

        when(productService.searchProducts(search, page, size)).thenReturn(expectedProducts);

        // When
        ResponseEntity<List<ProductResponse>> response = productController.searchProducts(search, page, size);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(expectedProducts, response.getBody());
        assertEquals(2, response.getBody().size());
        verify(productService, times(1)).searchProducts(search, page, size);
    }

    /**
     * Test product search with only search parameter.
     * Verifies that the controller handles partial parameters correctly.
     */
    @Test
    @DisplayName("Should search products with search parameter only")
    void testSearchProducts_SearchOnly() {
        // Given
        String search = "phone";
        Integer page = null;
        Integer size = null;
        
        List<ProductResponse> expectedProducts = Arrays.asList(
            ProductResponse.builder()
                .productId(3L)
                .productName("Smartphone")
                .description("Latest smartphone")
                .price(BigDecimal.valueOf(800.00))
                .availableQty(20)
                .isActive(true)
                .build()
        );

        when(productService.searchProducts(search, page, size)).thenReturn(expectedProducts);

        // When
        ResponseEntity<List<ProductResponse>> response = productController.searchProducts(search, page, size);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(expectedProducts, response.getBody());
        assertEquals(1, response.getBody().size());
        verify(productService, times(1)).searchProducts(search, page, size);
    }

    /**
     * Test product search with no parameters.
     * Verifies that the controller handles empty parameter scenario.
     */
    @Test
    @DisplayName("Should search products with no parameters")
    void testSearchProducts_NoParameters() {
        // Given
        String search = null;
        Integer page = null;
        Integer size = null;
        
        List<ProductResponse> expectedProducts = Arrays.asList(
            ProductResponse.builder()
                .productId(1L)
                .productName("Product 1")
                .description("Description 1")
                .price(BigDecimal.valueOf(100.00))
                .availableQty(5)
                .isActive(true)
                .build(),
            ProductResponse.builder()
                .productId(2L)
                .productName("Product 2")
                .description("Description 2")
                .price(BigDecimal.valueOf(200.00))
                .availableQty(3)
                .isActive(true)
                .build()
        );

        when(productService.searchProducts(search, page, size)).thenReturn(expectedProducts);

        // When
        ResponseEntity<List<ProductResponse>> response = productController.searchProducts(search, page, size);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(expectedProducts, response.getBody());
        assertEquals(2, response.getBody().size());
        verify(productService, times(1)).searchProducts(search, page, size);
    }

    /**
     * Test product search with empty search string.
     * Verifies that empty search string is handled correctly.
     */
    @Test
    @DisplayName("Should handle empty search string")
    void testSearchProducts_EmptySearch() {
        // Given
        String search = "";
        Integer page = 0;
        Integer size = 5;
        
        List<ProductResponse> expectedProducts = Arrays.asList(
            ProductResponse.builder()
                .productId(1L)
                .productName("Default Product")
                .description("Default description")
                .price(BigDecimal.valueOf(50.00))
                .availableQty(10)
                .isActive(true)
                .build()
        );

        when(productService.searchProducts(search, page, size)).thenReturn(expectedProducts);

        // When
        ResponseEntity<List<ProductResponse>> response = productController.searchProducts(search, page, size);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(expectedProducts, response.getBody());
        verify(productService, times(1)).searchProducts(search, page, size);
    }

    /**
     * Test product search returning empty list.
     * Verifies that empty result scenario is handled correctly.
     */
    @Test
    @DisplayName("Should handle empty search results")
    void testSearchProducts_EmptyResults() {
        // Given
        String search = "nonexistent";
        Integer page = 0;
        Integer size = 10;
        
        List<ProductResponse> expectedProducts = Arrays.asList();

        when(productService.searchProducts(search, page, size)).thenReturn(expectedProducts);

        // When
        ResponseEntity<List<ProductResponse>> response = productController.searchProducts(search, page, size);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(expectedProducts, response.getBody());
        assertTrue(response.getBody().isEmpty());
        verify(productService, times(1)).searchProducts(search, page, size);
    }

    /**
     * Test successful product retrieval by ID.
     * Verifies that the controller returns product with OK status.
     */
    @Test
    @DisplayName("Should get product by ID successfully")
    void testGetProductById_Success() {
        // Given
        Long productId = 1L;
        ProductResponse expectedProduct = ProductResponse.builder()
            .productId(productId)
            .productName("Test Product")
            .description("Test product description")
            .price(BigDecimal.valueOf(99.99))
            .availableQty(15)
            .isActive(true)
            .build();

        when(productService.getProductById(productId)).thenReturn(expectedProduct);

        // When
        ResponseEntity<ProductResponse> response = productController.getProductById(productId);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(expectedProduct, response.getBody());
        assertEquals(productId, response.getBody().getProductId());
        assertEquals("Test Product", response.getBody().getProductName());
        verify(productService, times(1)).getProductById(productId);
    }

    /**
     * Test product retrieval with zero stock.
     * Verifies that products with zero stock are handled correctly.
     */
    @Test
    @DisplayName("Should get product with zero stock")
    void testGetProductById_ZeroStock() {
        // Given
        Long productId = 2L;
        ProductResponse expectedProduct = ProductResponse.builder()
            .productId(productId)
            .productName("Out of Stock Product")
            .description("Product currently out of stock")
            .price(BigDecimal.valueOf(199.99))
            .availableQty(0)
            .isActive(true)
            .build();

        when(productService.getProductById(productId)).thenReturn(expectedProduct);

        // When
        ResponseEntity<ProductResponse> response = productController.getProductById(productId);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(expectedProduct, response.getBody());
        assertEquals(0, response.getBody().getAvailableQty());
        verify(productService, times(1)).getProductById(productId);
    }

    /**
     * Test product retrieval with inactive product.
     * Verifies that inactive products are handled correctly.
     */
    @Test
    @DisplayName("Should get inactive product")
    void testGetProductById_InactiveProduct() {
        // Given
        Long productId = 3L;
        ProductResponse expectedProduct = ProductResponse.builder()
            .productId(productId)
            .productName("Inactive Product")
            .description("This product is inactive")
            .price(BigDecimal.valueOf(299.99))
            .availableQty(5)
            .isActive(false)
            .build();

        when(productService.getProductById(productId)).thenReturn(expectedProduct);

        // When
        ResponseEntity<ProductResponse> response = productController.getProductById(productId);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(expectedProduct, response.getBody());
        assertFalse(response.getBody().getIsActive());
        verify(productService, times(1)).getProductById(productId);
    }

    /**
     * Test product retrieval with non-existent ID.
     * Verifies that service layer exceptions are properly propagated.
     */
    @Test
    @DisplayName("Should handle non-existent product ID")
    void testGetProductById_NotFound() {
        // Given
        Long productId = 999L;

        when(productService.getProductById(productId))
            .thenThrow(new RuntimeException("Product not found: " + productId));

        // When & Then
        assertThrows(RuntimeException.class, () -> {
            productController.getProductById(productId);
        });
        
        verify(productService, times(1)).getProductById(productId);
    }

    /**
     * Test product retrieval with null ID.
     * Verifies that null ID scenarios are handled appropriately.
     */
    @Test
    @DisplayName("Should handle null product ID")
    void testGetProductById_NullId() {
        // Given
        Long productId = null;

        when(productService.getProductById(productId))
            .thenThrow(new IllegalArgumentException("Product ID cannot be null"));

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            productController.getProductById(productId);
        });
        
        verify(productService, times(1)).getProductById(productId);
    }
}