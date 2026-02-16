package com.ecommerce.service;

import com.ecommerce.dto.ProductResponse;
import com.ecommerce.entity.Product;
import com.ecommerce.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Test class for ProductService
 * 
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
            .description("High performance laptop")
            .price(BigDecimal.valueOf(999.99))
            .availableQty(10)
            .build();
        
        Product product2 = Product.builder()
            .id(UUID.randomUUID())
            .name("Laptop Bag")
            .description("Durable laptop bag")
            .price(BigDecimal.valueOf(49.99))
            .availableQty(25)
            .build();
        
        productList.add(product1);
        productList.add(product2);
    }

    /**
     * Test searching products successfully
     * 
     * Verifies:
     * - Products are searched by keyword
     * - List of ProductResponse is returned
     * - Results contain matching products
     */
    @Test
    void testSearchProducts_Success() {
        // Given
        String keyword = "laptop";
        when(productRepository.searchProducts(anyString())).thenReturn(productList);

        // When
        List<ProductResponse> result = productService.searchProducts(keyword);

        // Then
        assertNotNull(result, "Result should not be null");
        assertEquals(2, result.size(), "Should return 2 products");
        assertEquals("Laptop", result.get(0).getName());
        verify(productRepository, times(1)).searchProducts(keyword.trim());
    }

    /**
     * Test searching products with no results
     * 
     * Verifies:
     * - Empty list is returned when no products match
     */
    @Test
    void testSearchProducts_NoResults() {
        // Given
        String keyword = "nonexistent";
        when(productRepository.searchProducts(anyString())).thenReturn(new ArrayList<>());

        // When
        List<ProductResponse> result = productService.searchProducts(keyword);

        // Then
        assertNotNull(result, "Result should not be null");
        assertTrue(result.isEmpty(), "Result list should be empty");
        verify(productRepository, times(1)).searchProducts(keyword.trim());
    }

    /**
     * Test searching products with null keyword
     * 
     * Verifies:
     * - ValidationException is thrown for null keyword
     */
    @Test
    void testSearchProducts_NullKeyword() {
        // When/Then
        assertThrows(Exception.class, () -> {
            productService.searchProducts(null);
        });
        verify(productRepository, never()).searchProducts(anyString());
    }

    /**
     * Test searching products with empty keyword
     * 
     * Verifies:
     * - ValidationException is thrown for empty keyword
     */
    @Test
    void testSearchProducts_EmptyKeyword() {
        // When/Then
        assertThrows(Exception.class, () -> {
            productService.searchProducts("");
        });
        verify(productRepository, never()).searchProducts(anyString());
    }

    /**
     * Test searching products with whitespace keyword
     * 
     * Verifies:
     * - ValidationException is thrown for whitespace-only keyword
     */
    @Test
    void testSearchProducts_WhitespaceKeyword() {
        // When/Then
        assertThrows(Exception.class, () -> {
            productService.searchProducts("   ");
        });
        verify(productRepository, never()).searchProducts(anyString());
    }

    /**
     * Test searching products trims keyword
     * 
     * Verifies:
     * - Leading and trailing whitespace is trimmed from keyword
     */
    @Test
    void testSearchProducts_TrimsKeyword() {
        // Given
        String keyword = "  laptop  ";
        when(productRepository.searchProducts(anyString())).thenReturn(productList);

        // When
        List<ProductResponse> result = productService.searchProducts(keyword);

        // Then
        assertNotNull(result, "Result should not be null");
        verify(productRepository, times(1)).searchProducts("laptop");
    }

    /**
     * Test searching products with special characters
     * 
     * Verifies:
     * - Special characters in search keyword are handled correctly
     */
    @Test
    void testSearchProducts_SpecialCharacters() {
        // Given
        String keyword = "laptop@#$";
        when(productRepository.searchProducts(anyString())).thenReturn(new ArrayList<>());

        // When
        List<ProductResponse> result = productService.searchProducts(keyword);

        // Then
        assertNotNull(result, "Result should not be null");
        verify(productRepository, times(1)).searchProducts(keyword.trim());
    }

    /**
     * Test product response mapping
     * 
     * Verifies:
     * - Product entity is correctly mapped to ProductResponse
     * - All fields are properly transferred
     */
    @Test
    void testSearchProducts_ResponseMapping() {
        // Given
        String keyword = "laptop";
        when(productRepository.searchProducts(anyString())).thenReturn(productList);

        // When
        List<ProductResponse> result = productService.searchProducts(keyword);

        // Then
        assertNotNull(result, "Result should not be null");
        ProductResponse firstProduct = result.get(0);
        assertEquals("Laptop", firstProduct.getName());
        assertEquals("High performance laptop", firstProduct.getDescription());
        assertEquals(BigDecimal.valueOf(999.99), firstProduct.getPrice());
        assertEquals(10, firstProduct.getAvailableQty());
    }
}
