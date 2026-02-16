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
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Test class for ProductService
 * Tests product search operations and validation
 * Uses Mockito for mocking repository dependencies
 *
 * @author Test Generation Agent
 * @version 1.0
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ProductService Tests")
class test_ProductService {

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductService productService;

    private List<Product> testProducts;

    @BeforeEach
    void setUp() {
        testProducts = new ArrayList<>();
        
        Product product1 = Product.builder()
            .id(1L)
            .name("Laptop")
            .description("High performance laptop")
            .price(new BigDecimal("999.99"))
            .availableQty(10)
            .build();

        Product product2 = Product.builder()
            .id(2L)
            .name("Laptop Stand")
            .description("Ergonomic laptop stand")
            .price(new BigDecimal("49.99"))
            .availableQty(25)
            .build();

        Product product3 = Product.builder()
            .id(3L)
            .name("Gaming Laptop")
            .description("High-end gaming laptop")
            .price(new BigDecimal("1999.99"))
            .availableQty(5)
            .build();

        testProducts.add(product1);
        testProducts.add(product2);
        testProducts.add(product3);
    }

    /**
     * Test successful product search
     * Verifies that products can be searched with a valid keyword
     */
    @Test
    @DisplayName("Should successfully search products with valid keyword")
    void testSearchProducts_Success() {
        // Arrange
        String keyword = "laptop";
        when(productRepository.searchProducts(keyword)).thenReturn(testProducts);

        // Act
        List<ProductResponse> results = productService.searchProducts(keyword);

        // Assert
        assertNotNull(results);
        assertEquals(3, results.size());
        assertEquals("Laptop", results.get(0).getName());
        assertEquals(new BigDecimal("999.99"), results.get(0).getPrice());
        verify(productRepository).searchProducts(keyword);
    }

    /**
     * Test product search with trimmed keyword
     * Verifies that whitespace is trimmed from search keyword
     */
    @Test
    @DisplayName("Should trim whitespace from search keyword")
    void testSearchProducts_TrimsWhitespace() {
        // Arrange
        String keyword = "  laptop  ";
        String trimmedKeyword = "laptop";
        when(productRepository.searchProducts(trimmedKeyword)).thenReturn(testProducts);

        // Act
        List<ProductResponse> results = productService.searchProducts(keyword);

        // Assert
        assertNotNull(results);
        assertEquals(3, results.size());
        verify(productRepository).searchProducts(trimmedKeyword);
    }

    /**
     * Test product search with no results
     * Verifies that empty list is returned when no products match
     */
    @Test
    @DisplayName("Should return empty list when no products match keyword")
    void testSearchProducts_NoResults() {
        // Arrange
        String keyword = "nonexistent";
        when(productRepository.searchProducts(keyword)).thenReturn(new ArrayList<>());

        // Act
        List<ProductResponse> results = productService.searchProducts(keyword);

        // Assert
        assertNotNull(results);
        assertTrue(results.isEmpty());
        verify(productRepository).searchProducts(keyword);
    }

    /**
     * Test product search with null keyword
     * Verifies that ValidationException is thrown for null keyword
     */
    @Test
    @DisplayName("Should throw ValidationException when keyword is null")
    void testSearchProducts_NullKeyword_ThrowsException() {
        // Arrange
        String keyword = null;

        // Act & Assert
        ValidationException exception = assertThrows(
            ValidationException.class,
            () -> productService.searchProducts(keyword)
        );

        assertEquals("Search keyword cannot be empty", exception.getMessage());
        verify(productRepository, never()).searchProducts(anyString());
    }

    /**
     * Test product search with empty keyword
     * Verifies that ValidationException is thrown for empty keyword
     */
    @Test
    @DisplayName("Should throw ValidationException when keyword is empty")
    void testSearchProducts_EmptyKeyword_ThrowsException() {
        // Arrange
        String keyword = "";

        // Act & Assert
        ValidationException exception = assertThrows(
            ValidationException.class,
            () -> productService.searchProducts(keyword)
        );

        assertEquals("Search keyword cannot be empty", exception.getMessage());
        verify(productRepository, never()).searchProducts(anyString());
    }

    /**
     * Test product search with whitespace-only keyword
     * Verifies that ValidationException is thrown for whitespace-only keyword
     */
    @Test
    @DisplayName("Should throw ValidationException when keyword is only whitespace")
    void testSearchProducts_WhitespaceOnlyKeyword_ThrowsException() {
        // Arrange
        String keyword = "   ";

        // Act & Assert
        ValidationException exception = assertThrows(
            ValidationException.class,
            () -> productService.searchProducts(keyword)
        );

        assertEquals("Search keyword cannot be empty", exception.getMessage());
        verify(productRepository, never()).searchProducts(anyString());
    }

    /**
     * Test product search with special characters
     * Verifies that search works with special characters in keyword
     */
    @Test
    @DisplayName("Should handle special characters in search keyword")
    void testSearchProducts_SpecialCharacters() {
        // Arrange
        String keyword = "laptop-2024";
        when(productRepository.searchProducts(keyword)).thenReturn(testProducts);

        // Act
        List<ProductResponse> results = productService.searchProducts(keyword);

        // Assert
        assertNotNull(results);
        assertEquals(3, results.size());
        verify(productRepository).searchProducts(keyword);
    }

    /**
     * Test product search with numeric keyword
     * Verifies that search works with numeric keywords
     */
    @Test
    @DisplayName("Should handle numeric search keyword")
    void testSearchProducts_NumericKeyword() {
        // Arrange
        String keyword = "999";
        when(productRepository.searchProducts(keyword)).thenReturn(List.of(testProducts.get(0)));

        // Act
        List<ProductResponse> results = productService.searchProducts(keyword);

        // Assert
        assertNotNull(results);
        assertEquals(1, results.size());
        verify(productRepository).searchProducts(keyword);
    }

    /**
     * Test product search with case sensitivity
     * Verifies that search handles different cases
     */
    @Test
    @DisplayName("Should handle case variations in search keyword")
    void testSearchProducts_CaseVariations() {
        // Arrange
        String keyword = "LAPTOP";
        when(productRepository.searchProducts(keyword)).thenReturn(testProducts);

        // Act
        List<ProductResponse> results = productService.searchProducts(keyword);

        // Assert
        assertNotNull(results);
        assertEquals(3, results.size());
        verify(productRepository).searchProducts(keyword);
    }

    /**
     * Test ProductResponse mapping
     * Verifies that Product entity is correctly mapped to ProductResponse DTO
     */
    @Test
    @DisplayName("Should correctly map Product entity to ProductResponse DTO")
    void testProductResponseMapping() {
        // Arrange
        String keyword = "laptop";
        when(productRepository.searchProducts(keyword)).thenReturn(testProducts);

        // Act
        List<ProductResponse> results = productService.searchProducts(keyword);

        // Assert
        assertNotNull(results);
        ProductResponse firstResult = results.get(0);
        assertEquals(testProducts.get(0).getId(), firstResult.getId());
        assertEquals(testProducts.get(0).getName(), firstResult.getName());
        assertEquals(testProducts.get(0).getDescription(), firstResult.getDescription());
        assertEquals(testProducts.get(0).getPrice(), firstResult.getPrice());
        assertEquals(testProducts.get(0).getAvailableQty(), firstResult.getAvailableQty());
    }

    /**
     * Test product search with single result
     * Verifies that search works correctly with single matching product
     */
    @Test
    @DisplayName("Should return single product when only one matches")
    void testSearchProducts_SingleResult() {
        // Arrange
        String keyword = "gaming";
        when(productRepository.searchProducts(keyword))
            .thenReturn(List.of(testProducts.get(2)));

        // Act
        List<ProductResponse> results = productService.searchProducts(keyword);

        // Assert
        assertNotNull(results);
        assertEquals(1, results.size());
        assertEquals("Gaming Laptop", results.get(0).getName());
        verify(productRepository).searchProducts(keyword);
    }

    /**
     * Test product search with long keyword
     * Verifies that search handles long keywords properly
     */
    @Test
    @DisplayName("Should handle long search keywords")
    void testSearchProducts_LongKeyword() {
        // Arrange
        String keyword = "high performance gaming laptop with advanced features";
        when(productRepository.searchProducts(keyword)).thenReturn(new ArrayList<>());

        // Act
        List<ProductResponse> results = productService.searchProducts(keyword);

        // Assert
        assertNotNull(results);
        assertTrue(results.isEmpty());
        verify(productRepository).searchProducts(keyword);
    }
}