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
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Unit test class for ProductService.
 * Tests product search operations and validation logic.
 * Uses Mockito for mocking repository dependencies.
 * 
 * @author QA Automation Team
 * @version 1.0
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ProductService Unit Tests")
class test_ProductService {

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductService productService;

    private Product testProduct1;
    private Product testProduct2;
    private List<Product> productList;

    /**
     * Set up test data before each test execution.
     */
    @BeforeEach
    void setUp() {
        testProduct1 = Product.builder()
                .id(1L)
                .name("Laptop")
                .description("High performance laptop")
                .price(new BigDecimal("999.99"))
                .availableQty(50)
                .build();

        testProduct2 = Product.builder()
                .id(2L)
                .name("Laptop Stand")
                .description("Ergonomic laptop stand")
                .price(new BigDecimal("49.99"))
                .availableQty(100)
                .build();

        productList = Arrays.asList(testProduct1, testProduct2);
    }

    /**
     * Test searching products with valid keyword.
     * Verifies that products matching the keyword are returned.
     */
    @Test
    @DisplayName("Should search products successfully with valid keyword")
    void testSearchProducts_WithValidKeyword_ShouldReturnProducts() {
        // Arrange
        String keyword = "laptop";
        when(productRepository.searchProducts(keyword)).thenReturn(productList);

        // Act
        List<ProductResponse> results = productService.searchProducts(keyword);

        // Assert
        assertNotNull(results);
        assertEquals(2, results.size());
        assertEquals("Laptop", results.get(0).getName());
        assertEquals("Laptop Stand", results.get(1).getName());
        verify(productRepository).searchProducts(keyword);
    }

    /**
     * Test searching products with keyword that has leading/trailing spaces.
     * Verifies that keyword is trimmed before searching.
     */
    @Test
    @DisplayName("Should trim keyword before searching")
    void testSearchProducts_WithSpaces_ShouldTrimKeyword() {
        // Arrange
        String keyword = "  laptop  ";
        when(productRepository.searchProducts("laptop")).thenReturn(productList);

        // Act
        List<ProductResponse> results = productService.searchProducts(keyword);

        // Assert
        assertNotNull(results);
        assertEquals(2, results.size());
        verify(productRepository).searchProducts("laptop");
    }

    /**
     * Test searching products with null keyword.
     * Verifies that ValidationException is thrown.
     */
    @Test
    @DisplayName("Should throw ValidationException when keyword is null")
    void testSearchProducts_WithNullKeyword_ShouldThrowValidationException() {
        // Act & Assert
        ValidationException exception = assertThrows(
                ValidationException.class,
                () -> productService.searchProducts(null)
        );
        assertEquals("Search keyword cannot be empty", exception.getMessage());
        verify(productRepository, never()).searchProducts(anyString());
    }

    /**
     * Test searching products with empty keyword.
     * Verifies that ValidationException is thrown.
     */
    @Test
    @DisplayName("Should throw ValidationException when keyword is empty")
    void testSearchProducts_WithEmptyKeyword_ShouldThrowValidationException() {
        // Act & Assert
        ValidationException exception = assertThrows(
                ValidationException.class,
                () -> productService.searchProducts("")
        );
        assertEquals("Search keyword cannot be empty", exception.getMessage());
        verify(productRepository, never()).searchProducts(anyString());
    }

    /**
     * Test searching products with whitespace-only keyword.
     * Verifies that ValidationException is thrown.
     */
    @Test
    @DisplayName("Should throw ValidationException when keyword is only whitespace")
    void testSearchProducts_WithWhitespaceKeyword_ShouldThrowValidationException() {
        // Act & Assert
        ValidationException exception = assertThrows(
                ValidationException.class,
                () -> productService.searchProducts("   ")
        );
        assertEquals("Search keyword cannot be empty", exception.getMessage());
        verify(productRepository, never()).searchProducts(anyString());
    }

    /**
     * Test searching products when no results found.
     * Verifies that empty list is returned.
     */
    @Test
    @DisplayName("Should return empty list when no products match keyword")
    void testSearchProducts_NoResults_ShouldReturnEmptyList() {
        // Arrange
        String keyword = "nonexistent";
        when(productRepository.searchProducts(keyword)).thenReturn(Collections.emptyList());

        // Act
        List<ProductResponse> results = productService.searchProducts(keyword);

        // Assert
        assertNotNull(results);
        assertTrue(results.isEmpty());
        verify(productRepository).searchProducts(keyword);
    }

    /**
     * Test that product response mapping includes all required fields.
     * Verifies that all product details are correctly mapped to response DTO.
     */
    @Test
    @DisplayName("Should map all product fields correctly to ProductResponse")
    void testSearchProducts_ShouldMapAllFieldsCorrectly() {
        // Arrange
        String keyword = "laptop";
        when(productRepository.searchProducts(keyword)).thenReturn(Arrays.asList(testProduct1));

        // Act
        List<ProductResponse> results = productService.searchProducts(keyword);

        // Assert
        assertNotNull(results);
        assertEquals(1, results.size());
        ProductResponse response = results.get(0);
        assertEquals(1L, response.getId());
        assertEquals("Laptop", response.getName());
        assertEquals("High performance laptop", response.getDescription());
        assertEquals(0, new BigDecimal("999.99").compareTo(response.getPrice()));
        assertEquals(50, response.getAvailableQty());
    }

    /**
     * Test searching products with single character keyword.
     * Verifies that single character searches are allowed.
     */
    @Test
    @DisplayName("Should allow single character keyword search")
    void testSearchProducts_WithSingleCharacter_ShouldSearch() {
        // Arrange
        String keyword = "L";
        when(productRepository.searchProducts(keyword)).thenReturn(productList);

        // Act
        List<ProductResponse> results = productService.searchProducts(keyword);

        // Assert
        assertNotNull(results);
        assertEquals(2, results.size());
        verify(productRepository).searchProducts(keyword);
    }

    /**
     * Test searching products with special characters in keyword.
     * Verifies that special characters are handled correctly.
     */
    @Test
    @DisplayName("Should handle special characters in keyword")
    void testSearchProducts_WithSpecialCharacters_ShouldSearch() {
        // Arrange
        String keyword = "laptop-2024";
        when(productRepository.searchProducts(keyword)).thenReturn(Collections.emptyList());

        // Act
        List<ProductResponse> results = productService.searchProducts(keyword);

        // Assert
        assertNotNull(results);
        assertTrue(results.isEmpty());
        verify(productRepository).searchProducts(keyword);
    }

    /**
     * Test searching products with numeric keyword.
     * Verifies that numeric searches work correctly.
     */
    @Test
    @DisplayName("Should handle numeric keyword search")
    void testSearchProducts_WithNumericKeyword_ShouldSearch() {
        // Arrange
        String keyword = "999";
        when(productRepository.searchProducts(keyword)).thenReturn(Arrays.asList(testProduct1));

        // Act
        List<ProductResponse> results = productService.searchProducts(keyword);

        // Assert
        assertNotNull(results);
        assertEquals(1, results.size());
        verify(productRepository).searchProducts(keyword);
    }

    /**
     * Test searching products with case-sensitive keyword.
     * Verifies that keyword case is preserved during search.
     */
    @Test
    @DisplayName("Should preserve keyword case during search")
    void testSearchProducts_WithMixedCase_ShouldPreserveCase() {
        // Arrange
        String keyword = "LaPtOp";
        when(productRepository.searchProducts(keyword)).thenReturn(productList);

        // Act
        List<ProductResponse> results = productService.searchProducts(keyword);

        // Assert
        assertNotNull(results);
        assertEquals(2, results.size());
        verify(productRepository).searchProducts(keyword);
    }

    /**
     * Test that product response list maintains order from repository.
     * Verifies that product order is preserved.
     */
    @Test
    @DisplayName("Should maintain product order from repository")
    void testSearchProducts_ShouldMaintainOrder() {
        // Arrange
        String keyword = "laptop";
        when(productRepository.searchProducts(keyword)).thenReturn(productList);

        // Act
        List<ProductResponse> results = productService.searchProducts(keyword);

        // Assert
        assertNotNull(results);
        assertEquals(2, results.size());
        assertEquals(1L, results.get(0).getId());
        assertEquals(2L, results.get(1).getId());
    }
}