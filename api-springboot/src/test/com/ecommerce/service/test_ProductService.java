package com.ecommerce.service;

import com.ecommerce.dto.ProductResponse;
import com.ecommerce.entity.Product;
import com.ecommerce.exception.ValidationException;
import com.ecommerce.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * JUnit 5 test class for ProductService
 * Tests product search and retrieval operations
 * 
 * @author Test Generation Agent
 * @version 1.0
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ProductService Test Suite")
class test_ProductService {

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductService productService;

    private Product testProduct1;
    private Product testProduct2;

    /**
     * Setup method to initialize test data before each test
     */
    @BeforeEach
    void setUp() {
        testProduct1 = Product.builder()
                .id(1L)
                .name("Laptop Computer")
                .description("High-performance laptop")
                .price(new BigDecimal("999.99"))
                .availableQty(50)
                .build();

        testProduct2 = Product.builder()
                .id(2L)
                .name("Wireless Mouse")
                .description("Ergonomic wireless mouse")
                .price(new BigDecimal("29.99"))
                .availableQty(100)
                .build();
    }

    /**
     * Test searching products with valid keyword
     * Verifies that products matching the keyword are returned
     */
    @Test
    @DisplayName("Should successfully search products with valid keyword")
    void testSearchProducts_Success() {
        // Arrange
        String keyword = "laptop";
        List<Product> products = Arrays.asList(testProduct1);
        when(productRepository.searchProducts(keyword)).thenReturn(products);

        // Act
        List<ProductResponse> result = productService.searchProducts(keyword);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Laptop Computer", result.get(0).getName());
        assertEquals(new BigDecimal("999.99"), result.get(0).getPrice());
        verify(productRepository).searchProducts(keyword);
    }

    /**
     * Test searching products with multiple results
     * Verifies that all matching products are returned
     */
    @Test
    @DisplayName("Should return multiple products when multiple matches found")
    void testSearchProducts_MultipleResults() {
        // Arrange
        String keyword = "wireless";
        List<Product> products = Arrays.asList(testProduct1, testProduct2);
        when(productRepository.searchProducts(keyword)).thenReturn(products);

        // Act
        List<ProductResponse> result = productService.searchProducts(keyword);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(productRepository).searchProducts(keyword);
    }

    /**
     * Test searching products with no results
     * Verifies that empty list is returned when no products match
     */
    @Test
    @DisplayName("Should return empty list when no products match keyword")
    void testSearchProducts_NoResults() {
        // Arrange
        String keyword = "nonexistent";
        when(productRepository.searchProducts(keyword)).thenReturn(Collections.emptyList());

        // Act
        List<ProductResponse> result = productService.searchProducts(keyword);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(productRepository).searchProducts(keyword);
    }

    /**
     * Test searching products with null keyword
     * Verifies that ValidationException is thrown for null keyword
     */
    @Test
    @DisplayName("Should throw ValidationException when keyword is null")
    void testSearchProducts_NullKeyword() {
        // Act & Assert
        ValidationException exception = assertThrows(
                ValidationException.class,
                () -> productService.searchProducts(null)
        );

        assertEquals("Search keyword cannot be empty", exception.getMessage());
        verify(productRepository, never()).searchProducts(any());
    }

    /**
     * Test searching products with empty keyword
     * Verifies that ValidationException is thrown for empty keyword
     */
    @Test
    @DisplayName("Should throw ValidationException when keyword is empty")
    void testSearchProducts_EmptyKeyword() {
        // Act & Assert
        ValidationException exception = assertThrows(
                ValidationException.class,
                () -> productService.searchProducts("")
        );

        assertEquals("Search keyword cannot be empty", exception.getMessage());
        verify(productRepository, never()).searchProducts(any());
    }

    /**
     * Test searching products with whitespace-only keyword
     * Verifies that ValidationException is thrown for whitespace keyword
     */
    @Test
    @DisplayName("Should throw ValidationException when keyword is only whitespace")
    void testSearchProducts_WhitespaceKeyword() {
        // Act & Assert
        ValidationException exception = assertThrows(
                ValidationException.class,
                () -> productService.searchProducts("   ")
        );

        assertEquals("Search keyword cannot be empty", exception.getMessage());
        verify(productRepository, never()).searchProducts(any());
    }

    /**
     * Test searching products with keyword containing leading/trailing spaces
     * Verifies that keyword is trimmed before search
     */
    @Test
    @DisplayName("Should trim keyword before searching")
    void testSearchProducts_KeywordWithSpaces() {
        // Arrange
        String keyword = "  laptop  ";
        String trimmedKeyword = "laptop";
        List<Product> products = Arrays.asList(testProduct1);
        when(productRepository.searchProducts(trimmedKeyword)).thenReturn(products);

        // Act
        List<ProductResponse> result = productService.searchProducts(keyword);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(productRepository).searchProducts(trimmedKeyword);
    }

    /**
     * Test product to ProductResponse mapping
     * Verifies that all product fields are correctly mapped to response DTO
     */
    @Test
    @DisplayName("Should correctly map Product entity to ProductResponse DTO")
    void testSearchProducts_CorrectMapping() {
        // Arrange
        String keyword = "laptop";
        List<Product> products = Arrays.asList(testProduct1);
        when(productRepository.searchProducts(keyword)).thenReturn(products);

        // Act
        List<ProductResponse> result = productService.searchProducts(keyword);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        ProductResponse response = result.get(0);
        assertEquals(testProduct1.getId(), response.getId());
        assertEquals(testProduct1.getName(), response.getName());
        assertEquals(testProduct1.getDescription(), response.getDescription());
        assertEquals(testProduct1.getPrice(), response.getPrice());
        assertEquals(testProduct1.getAvailableQty(), response.getAvailableQty());
    }

    /**
     * Test searching products with special characters
     * Verifies that search handles special characters properly
     */
    @Test
    @DisplayName("Should handle search with special characters")
    void testSearchProducts_SpecialCharacters() {
        // Arrange
        String keyword = "laptop@#$";
        List<Product> products = Collections.emptyList();
        when(productRepository.searchProducts(keyword)).thenReturn(products);

        // Act
        List<ProductResponse> result = productService.searchProducts(keyword);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(productRepository).searchProducts(keyword);
    }

    /**
     * Test searching products with case sensitivity
     * Verifies that search is case-insensitive (repository behavior)
     */
    @Test
    @DisplayName("Should handle case-insensitive search")
    void testSearchProducts_CaseInsensitive() {
        // Arrange
        String keyword = "LAPTOP";
        List<Product> products = Arrays.asList(testProduct1);
        when(productRepository.searchProducts(keyword)).thenReturn(products);

        // Act
        List<ProductResponse> result = productService.searchProducts(keyword);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(productRepository).searchProducts(keyword);
    }

    /**
     * Test searching products with numeric keyword
     * Verifies that numeric search terms are handled
     */
    @Test
    @DisplayName("Should handle numeric search keywords")
    void testSearchProducts_NumericKeyword() {
        // Arrange
        String keyword = "999";
        List<Product> products = Arrays.asList(testProduct1);
        when(productRepository.searchProducts(keyword)).thenReturn(products);

        // Act
        List<ProductResponse> result = productService.searchProducts(keyword);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(productRepository).searchProducts(keyword);
    }

    /**
     * Test searching products with very long keyword
     * Verifies that long search terms are handled properly
     */
    @Test
    @DisplayName("Should handle very long search keywords")
    void testSearchProducts_LongKeyword() {
        // Arrange
        String keyword = "a".repeat(100);
        List<Product> products = Collections.emptyList();
        when(productRepository.searchProducts(keyword)).thenReturn(products);

        // Act
        List<ProductResponse> result = productService.searchProducts(keyword);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(productRepository).searchProducts(keyword);
    }
}