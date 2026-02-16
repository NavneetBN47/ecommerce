package com.ecommerce.service;

import com.ecommerce.dto.ProductResponse;
import com.ecommerce.entity.Product;
import com.ecommerce.exception.ValidationException;
import com.ecommerce.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Test class for ProductService
 * Tests product search and retrieval operations
 */
@ExtendWith(MockitoExtension.class)
class test_ProductService {

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductService productService;

    private Product testProduct1;
    private Product testProduct2;

    @BeforeEach
    void setUp() {
        testProduct1 = Product.builder()
            .id(1L)
            .name("Laptop")
            .description("High performance laptop")
            .price(new BigDecimal("999.99"))
            .availableQty(10)
            .build();

        testProduct2 = Product.builder()
            .id(2L)
            .name("Mouse")
            .description("Wireless mouse")
            .price(new BigDecimal("29.99"))
            .availableQty(50)
            .build();
    }

    /**
     * Test searching products with valid keyword
     */
    @Test
    void testSearchProducts_ValidKeyword_Success() {
        String keyword = "laptop";
        List<Product> products = Arrays.asList(testProduct1);
        when(productRepository.searchProducts(keyword)).thenReturn(products);

        List<ProductResponse> result = productService.searchProducts(keyword);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Laptop", result.get(0).getName());
        assertEquals(new BigDecimal("999.99"), result.get(0).getPrice());
        assertEquals(10, result.get(0).getAvailableQty());
        verify(productRepository).searchProducts(keyword);
    }

    /**
     * Test searching products with keyword returning multiple results
     */
    @Test
    void testSearchProducts_MultipleResults_Success() {
        String keyword = "wireless";
        List<Product> products = Arrays.asList(testProduct1, testProduct2);
        when(productRepository.searchProducts(keyword)).thenReturn(products);

        List<ProductResponse> result = productService.searchProducts(keyword);

        assertNotNull(result);
        assertEquals(2, result.size());
        verify(productRepository).searchProducts(keyword);
    }

    /**
     * Test searching products with no results
     */
    @Test
    void testSearchProducts_NoResults_ReturnsEmptyList() {
        String keyword = "nonexistent";
        when(productRepository.searchProducts(keyword)).thenReturn(Collections.emptyList());

        List<ProductResponse> result = productService.searchProducts(keyword);

        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(productRepository).searchProducts(keyword);
    }

    /**
     * Test searching products with null keyword
     */
    @Test
    void testSearchProducts_NullKeyword_ThrowsException() {
        ValidationException exception = assertThrows(ValidationException.class, () -> {
            productService.searchProducts(null);
        });

        assertEquals("Search keyword cannot be empty", exception.getMessage());
        verify(productRepository, never()).searchProducts(anyString());
    }

    /**
     * Test searching products with empty keyword
     */
    @Test
    void testSearchProducts_EmptyKeyword_ThrowsException() {
        ValidationException exception = assertThrows(ValidationException.class, () -> {
            productService.searchProducts("");
        });

        assertEquals("Search keyword cannot be empty", exception.getMessage());
        verify(productRepository, never()).searchProducts(anyString());
    }

    /**
     * Test searching products with whitespace-only keyword
     */
    @Test
    void testSearchProducts_WhitespaceKeyword_ThrowsException() {
        ValidationException exception = assertThrows(ValidationException.class, () -> {
            productService.searchProducts("   ");
        });

        assertEquals("Search keyword cannot be empty", exception.getMessage());
        verify(productRepository, never()).searchProducts(anyString());
    }

    /**
     * Test searching products trims keyword
     */
    @Test
    void testSearchProducts_TrimsKeyword_Success() {
        String keyword = "  laptop  ";
        String trimmedKeyword = "laptop";
        List<Product> products = Arrays.asList(testProduct1);
        when(productRepository.searchProducts(trimmedKeyword)).thenReturn(products);

        List<ProductResponse> result = productService.searchProducts(keyword);

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(productRepository).searchProducts(trimmedKeyword);
    }

    /**
     * Test product response mapping includes all fields
     */
    @Test
    void testSearchProducts_MapsAllFields_Success() {
        String keyword = "laptop";
        List<Product> products = Arrays.asList(testProduct1);
        when(productRepository.searchProducts(keyword)).thenReturn(products);

        List<ProductResponse> result = productService.searchProducts(keyword);

        assertNotNull(result);
        assertEquals(1, result.size());
        ProductResponse response = result.get(0);
        assertEquals(1L, response.getId());
        assertEquals("Laptop", response.getName());
        assertEquals("High performance laptop", response.getDescription());
        assertEquals(new BigDecimal("999.99"), response.getPrice());
        assertEquals(10, response.getAvailableQty());
    }

    /**
     * Test searching products with special characters
     */
    @Test
    void testSearchProducts_SpecialCharacters_Success() {
        String keyword = "laptop@123";
        List<Product> products = Arrays.asList(testProduct1);
        when(productRepository.searchProducts(keyword)).thenReturn(products);

        List<ProductResponse> result = productService.searchProducts(keyword);

        assertNotNull(result);
        verify(productRepository).searchProducts(keyword);
    }

    /**
     * Test searching products with case sensitivity
     */
    @Test
    void testSearchProducts_CaseSensitivity_Success() {
        String keyword = "LAPTOP";
        List<Product> products = Arrays.asList(testProduct1);
        when(productRepository.searchProducts(keyword)).thenReturn(products);

        List<ProductResponse> result = productService.searchProducts(keyword);

        assertNotNull(result);
        verify(productRepository).searchProducts(keyword);
    }

    /**
     * Test searching products with numeric keyword
     */
    @Test
    void testSearchProducts_NumericKeyword_Success() {
        String keyword = "999";
        List<Product> products = Arrays.asList(testProduct1);
        when(productRepository.searchProducts(keyword)).thenReturn(products);

        List<ProductResponse> result = productService.searchProducts(keyword);

        assertNotNull(result);
        verify(productRepository).searchProducts(keyword);
    }
}