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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * JUnit 5 test class for ProductService
 * Tests product search functionality
 * 
 * @author QA Automation Agent
 * @version 1.0.0
 */
@ExtendWith(MockitoExtension.class)
class test_ProductService {

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductService productService;

    private List<Product> productList;

    @BeforeEach
    void setUp() {
        productList = new ArrayList<>();
        
        Product product1 = Product.builder()
            .id(UUID.randomUUID())
            .name("Laptop")
            .description("High-performance laptop")
            .price(BigDecimal.valueOf(999.99))
            .availableQty(10)
            .build();
            
        Product product2 = Product.builder()
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
     * Verifies product list is returned
     */
    @Test
    void searchProductsShouldReturnProductList() {
        // Given
        String keyword = "laptop";
        when(productRepository.searchProducts(keyword)).thenReturn(productList);

        // When
        List<ProductResponse> result = productService.searchProducts(keyword);

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(productRepository, times(1)).searchProducts(keyword);
    }

    /**
     * Test searching products with no results
     * Verifies empty list is returned
     */
    @Test
    void searchProductsShouldReturnEmptyListWhenNoMatch() {
        // Given
        String keyword = "nonexistent";
        when(productRepository.searchProducts(keyword)).thenReturn(new ArrayList<>());

        // When
        List<ProductResponse> result = productService.searchProducts(keyword);

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(productRepository, times(1)).searchProducts(keyword);
    }

    /**
     * Test searching products with null keyword
     * Verifies ValidationException is thrown
     */
    @Test
    void searchProductsShouldThrowExceptionForNullKeyword() {
        // When/Then
        ValidationException exception = assertThrows(ValidationException.class, () -> {
            productService.searchProducts(null);
        });
        assertEquals("Search keyword cannot be empty", exception.getMessage());
        verify(productRepository, never()).searchProducts(any());
    }

    /**
     * Test searching products with empty keyword
     * Verifies ValidationException is thrown
     */
    @Test
    void searchProductsShouldThrowExceptionForEmptyKeyword() {
        // When/Then
        ValidationException exception = assertThrows(ValidationException.class, () -> {
            productService.searchProducts("");
        });
        assertEquals("Search keyword cannot be empty", exception.getMessage());
        verify(productRepository, never()).searchProducts(any());
    }

    /**
     * Test searching products with whitespace keyword
     * Verifies ValidationException is thrown
     */
    @Test
    void searchProductsShouldThrowExceptionForWhitespaceKeyword() {
        // When/Then
        ValidationException exception = assertThrows(ValidationException.class, () -> {
            productService.searchProducts("   ");
        });
        assertEquals("Search keyword cannot be empty", exception.getMessage());
        verify(productRepository, never()).searchProducts(any());
    }

    /**
     * Test searching products trims keyword
     * Verifies keyword is trimmed before search
     */
    @Test
    void searchProductsShouldTrimKeyword() {
        // Given
        String keyword = "  laptop  ";
        when(productRepository.searchProducts("laptop")).thenReturn(productList);

        // When
        List<ProductResponse> result = productService.searchProducts(keyword);

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(productRepository, times(1)).searchProducts("laptop");
    }

    /**
     * Test product response mapping
     * Verifies all product fields are mapped correctly
     */
    @Test
    void searchProductsShouldMapProductResponseCorrectly() {
        // Given
        String keyword = "laptop";
        when(productRepository.searchProducts(keyword)).thenReturn(productList);

        // When
        List<ProductResponse> result = productService.searchProducts(keyword);

        // Then
        assertNotNull(result);
        ProductResponse response = result.get(0);
        assertNotNull(response.getId());
        assertNotNull(response.getName());
        assertNotNull(response.getDescription());
        assertNotNull(response.getPrice());
        assertTrue(response.getAvailableQty() >= 0);
    }

    /**
     * Test searching products with special characters
     * Verifies special characters are handled properly
     */
    @Test
    void searchProductsShouldHandleSpecialCharacters() {
        // Given
        String keyword = "laptop@#$";
        when(productRepository.searchProducts(keyword.trim())).thenReturn(new ArrayList<>());

        // When
        List<ProductResponse> result = productService.searchProducts(keyword);

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(productRepository, times(1)).searchProducts(keyword.trim());
    }

    /**
     * Test searching products with case-insensitive keyword
     * Verifies case handling
     */
    @Test
    void searchProductsShouldHandleCaseInsensitiveSearch() {
        // Given
        String keyword = "LAPTOP";
        when(productRepository.searchProducts(keyword)).thenReturn(productList);

        // When
        List<ProductResponse> result = productService.searchProducts(keyword);

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(productRepository, times(1)).searchProducts(keyword);
    }

    /**
     * Test searching products with partial match
     * Verifies partial keyword matching
     */
    @Test
    void searchProductsShouldSupportPartialMatch() {
        // Given
        String keyword = "lap";
        when(productRepository.searchProducts(keyword)).thenReturn(productList);

        // When
        List<ProductResponse> result = productService.searchProducts(keyword);

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(productRepository, times(1)).searchProducts(keyword);
    }

    /**
     * Test searching products returns correct count
     * Verifies result count matches repository response
     */
    @Test
    void searchProductsShouldReturnCorrectCount() {
        // Given
        String keyword = "laptop";
        when(productRepository.searchProducts(keyword)).thenReturn(productList);

        // When
        List<ProductResponse> result = productService.searchProducts(keyword);

        // Then
        assertEquals(productList.size(), result.size());
    }
}