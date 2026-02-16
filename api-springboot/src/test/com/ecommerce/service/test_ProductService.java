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
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Test class for ProductService
 * Tests product search and retrieval operations
 * 
 * @author QA Automation Team
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
    private List<Product> testProducts;

    @BeforeEach
    void setUp() {
        testProduct1 = Product.builder()
            .id(UUID.randomUUID())
            .name("Laptop Computer")
            .description("High performance laptop")
            .price(new BigDecimal("1299.99"))
            .availableQty(50)
            .build();

        testProduct2 = Product.builder()
            .id(UUID.randomUUID())
            .name("Wireless Mouse")
            .description("Ergonomic wireless mouse")
            .price(new BigDecimal("29.99"))
            .availableQty(200)
            .build();

        testProducts = Arrays.asList(testProduct1, testProduct2);
    }

    /**
     * Test successful product search with valid keyword
     * Verifies that products matching the keyword are returned
     */
    @Test
    @DisplayName("Should search products successfully with valid keyword")
    void testSearchProducts_Success() {
        // Arrange
        String keyword = "laptop";
        when(productRepository.searchProducts(keyword)).thenReturn(Arrays.asList(testProduct1));

        // Act
        List<ProductResponse> results = productService.searchProducts(keyword);

        // Assert
        assertNotNull(results);
        assertEquals(1, results.size());
        assertEquals(testProduct1.getName(), results.get(0).getName());
        assertEquals(testProduct1.getDescription(), results.get(0).getDescription());
        assertEquals(testProduct1.getPrice(), results.get(0).getPrice());
        assertEquals(testProduct1.getAvailableQty(), results.get(0).getAvailableQty());
        verify(productRepository).searchProducts(keyword);
    }

    /**
     * Test product search with multiple results
     * Verifies that all matching products are returned
     */
    @Test
    @DisplayName("Should return multiple products when multiple matches found")
    void testSearchProducts_MultipleResults() {
        // Arrange
        String keyword = "computer";
        when(productRepository.searchProducts(keyword)).thenReturn(testProducts);

        // Act
        List<ProductResponse> results = productService.searchProducts(keyword);

        // Assert
        assertNotNull(results);
        assertEquals(2, results.size());
        verify(productRepository).searchProducts(keyword);
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
        when(productRepository.searchProducts(keyword)).thenReturn(Collections.emptyList());

        // Act
        List<ProductResponse> results = productService.searchProducts(keyword);

        // Assert
        assertNotNull(results);
        assertTrue(results.isEmpty());
        verify(productRepository).searchProducts(keyword);
    }

    /**
     * Test product search with null keyword
     * Verifies that ValidationException is thrown
     */
    @Test
    @DisplayName("Should throw ValidationException when keyword is null")
    void testSearchProducts_NullKeyword() {
        // Arrange
        String keyword = null;

        // Act & Assert
        ValidationException exception = assertThrows(ValidationException.class, () -> {
            productService.searchProducts(keyword);
        });

        assertEquals("Search keyword cannot be empty", exception.getMessage());
        verify(productRepository, never()).searchProducts(anyString());
    }

    /**
     * Test product search with empty keyword
     * Verifies that ValidationException is thrown
     */
    @Test
    @DisplayName("Should throw ValidationException when keyword is empty")
    void testSearchProducts_EmptyKeyword() {
        // Arrange
        String keyword = "";

        // Act & Assert
        ValidationException exception = assertThrows(ValidationException.class, () -> {
            productService.searchProducts(keyword);
        });

        assertEquals("Search keyword cannot be empty", exception.getMessage());
        verify(productRepository, never()).searchProducts(anyString());
    }

    /**
     * Test product search with whitespace-only keyword
     * Verifies that ValidationException is thrown
     */
    @Test
    @DisplayName("Should throw ValidationException when keyword is only whitespace")
    void testSearchProducts_WhitespaceKeyword() {
        // Arrange
        String keyword = "   ";

        // Act & Assert
        ValidationException exception = assertThrows(ValidationException.class, () -> {
            productService.searchProducts(keyword);
        });

        assertEquals("Search keyword cannot be empty", exception.getMessage());
        verify(productRepository, never()).searchProducts(anyString());
    }

    /**
     * Test product search with keyword containing leading/trailing spaces
     * Verifies that keyword is trimmed before search
     */
    @Test
    @DisplayName("Should trim keyword before searching")
    void testSearchProducts_KeywordWithSpaces() {
        // Arrange
        String keyword = "  laptop  ";
        String trimmedKeyword = "laptop";
        when(productRepository.searchProducts(trimmedKeyword)).thenReturn(Arrays.asList(testProduct1));

        // Act
        List<ProductResponse> results = productService.searchProducts(keyword);

        // Assert
        assertNotNull(results);
        assertEquals(1, results.size());
        verify(productRepository).searchProducts(trimmedKeyword);
    }

    /**
     * Test product search with special characters
     * Verifies that special characters in keyword are handled correctly
     */
    @Test
    @DisplayName("Should handle special characters in keyword")
    void testSearchProducts_SpecialCharacters() {
        // Arrange
        String keyword = "laptop@2024";
        when(productRepository.searchProducts(keyword)).thenReturn(Collections.emptyList());

        // Act
        List<ProductResponse> results = productService.searchProducts(keyword);

        // Assert
        assertNotNull(results);
        assertTrue(results.isEmpty());
        verify(productRepository).searchProducts(keyword);
    }

    /**
     * Test product search with case-sensitive keyword
     * Verifies that search handles different cases
     */
    @Test
    @DisplayName("Should search products with case-sensitive keyword")
    void testSearchProducts_CaseSensitive() {
        // Arrange
        String keyword = "LAPTOP";
        when(productRepository.searchProducts(keyword)).thenReturn(Arrays.asList(testProduct1));

        // Act
        List<ProductResponse> results = productService.searchProducts(keyword);

        // Assert
        assertNotNull(results);
        assertFalse(results.isEmpty());
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
        when(productRepository.searchProducts(keyword)).thenReturn(Arrays.asList(testProduct1));

        // Act
        List<ProductResponse> results = productService.searchProducts(keyword);

        // Assert
        assertNotNull(results);
        assertEquals(1, results.size());
        ProductResponse response = results.get(0);
        assertEquals(testProduct1.getId(), response.getId());
        assertEquals(testProduct1.getName(), response.getName());
        assertEquals(testProduct1.getDescription(), response.getDescription());
        assertEquals(testProduct1.getPrice(), response.getPrice());
        assertEquals(testProduct1.getAvailableQty(), response.getAvailableQty());
    }

    /**
     * Test product search with numeric keyword
     * Verifies that numeric keywords are handled correctly
     */
    @Test
    @DisplayName("Should handle numeric keyword")
    void testSearchProducts_NumericKeyword() {
        // Arrange
        String keyword = "1299";
        when(productRepository.searchProducts(keyword)).thenReturn(Arrays.asList(testProduct1));

        // Act
        List<ProductResponse> results = productService.searchProducts(keyword);

        // Assert
        assertNotNull(results);
        assertEquals(1, results.size());
        verify(productRepository).searchProducts(keyword);
    }

    /**
     * Test product search with very long keyword
     * Verifies that long keywords are handled without errors
     */
    @Test
    @DisplayName("Should handle very long keyword")
    void testSearchProducts_LongKeyword() {
        // Arrange
        String keyword = "a".repeat(500);
        when(productRepository.searchProducts(keyword)).thenReturn(Collections.emptyList());

        // Act
        List<ProductResponse> results = productService.searchProducts(keyword);

        // Assert
        assertNotNull(results);
        assertTrue(results.isEmpty());
        verify(productRepository).searchProducts(keyword);
    }

    /**
     * Test product search with single character keyword
     * Verifies that single character searches work correctly
     */
    @Test
    @DisplayName("Should handle single character keyword")
    void testSearchProducts_SingleCharacter() {
        // Arrange
        String keyword = "L";
        when(productRepository.searchProducts(keyword)).thenReturn(Arrays.asList(testProduct1));

        // Act
        List<ProductResponse> results = productService.searchProducts(keyword);

        // Assert
        assertNotNull(results);
        assertEquals(1, results.size());
        verify(productRepository).searchProducts(keyword);
    }
}