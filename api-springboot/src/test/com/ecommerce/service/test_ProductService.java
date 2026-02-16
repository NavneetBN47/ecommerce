package com.ecommerce.service;

import com.ecommerce.dto.ProductResponse;
import com.ecommerce.entity.Product;
import com.ecommerce.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

/**
 * Test class for ProductService
 * 
 * Tests product search and retrieval functionality including:
 * - Searching products by keyword
 * - Retrieving all active products
 * - Handling empty search results
 * 
 * @author Test Generation System
 * @version 1.0.0
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ProductService Tests")
class test_ProductService {

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductService productService;

    private List<Product> testProducts;
    private Product testProduct1;
    private Product testProduct2;

    /**
     * Set up test data before each test
     */
    @BeforeEach
    void setUp() {
        testProduct1 = Product.builder()
            .id(UUID.randomUUID())
            .name("Laptop")
            .description("High performance laptop")
            .price(BigDecimal.valueOf(999.99))
            .availableQty(10)
            .sku("LAP-001")
            .isActive(true)
            .build();

        testProduct2 = Product.builder()
            .id(UUID.randomUUID())
            .name("Mouse")
            .description("Wireless mouse")
            .price(BigDecimal.valueOf(29.99))
            .availableQty(50)
            .sku("MOU-001")
            .isActive(true)
            .build();

        testProducts = Arrays.asList(testProduct1, testProduct2);
    }

    /**
     * Test searching products with valid keyword
     * 
     * Validates:
     * - Products matching keyword are returned
     * - Only active products are included
     * - Correct product details
     */
    @Test
    @DisplayName("Should search products by keyword successfully")
    void testSearchProducts_WithKeyword_Success() {
        when(productRepository.searchByKeyword("Laptop"))
            .thenReturn(List.of(testProduct1));

        List<ProductResponse> results = productService.searchProducts("Laptop");

        assertThat(results).isNotEmpty();
        assertThat(results).hasSize(1);
        assertThat(results.get(0).getName()).isEqualTo("Laptop");
        assertThat(results.get(0).getPrice()).isEqualByComparingTo(BigDecimal.valueOf(999.99));
    }

    /**
     * Test searching with null keyword
     * 
     * Validates:
     * - All active products are returned
     * - Repository method for active products is called
     */
    @Test
    @DisplayName("Should return all active products when keyword is null")
    void testSearchProducts_NullKeyword() {
        when(productRepository.findByIsActiveTrue())
            .thenReturn(testProducts);

        List<ProductResponse> results = productService.searchProducts(null);

        assertThat(results).isNotEmpty();
        assertThat(results).hasSize(2);
    }

    /**
     * Test searching with empty keyword
     * 
     * Validates:
     * - All active products are returned
     * - Empty string is treated as no keyword
     */
    @Test
    @DisplayName("Should return all active products when keyword is empty")
    void testSearchProducts_EmptyKeyword() {
        when(productRepository.findByIsActiveTrue())
            .thenReturn(testProducts);

        List<ProductResponse> results = productService.searchProducts("");

        assertThat(results).isNotEmpty();
        assertThat(results).hasSize(2);
    }

    /**
     * Test searching with whitespace keyword
     * 
     * Validates:
     * - Whitespace is trimmed
     * - All active products are returned
     */
    @Test
    @DisplayName("Should return all active products when keyword is whitespace")
    void testSearchProducts_WhitespaceKeyword() {
        when(productRepository.findByIsActiveTrue())
            .thenReturn(testProducts);

        List<ProductResponse> results = productService.searchProducts("   ");

        assertThat(results).isNotEmpty();
        assertThat(results).hasSize(2);
    }

    /**
     * Test searching with no matching products
     * 
     * Validates:
     * - Empty list is returned
     * - No exception is thrown
     */
    @Test
    @DisplayName("Should return empty list when no products match")
    void testSearchProducts_NoResults() {
        when(productRepository.searchByKeyword(anyString()))
            .thenReturn(Collections.emptyList());

        List<ProductResponse> results = productService.searchProducts("NonExistent");

        assertThat(results).isEmpty();
    }

    /**
     * Test filtering inactive products from search results
     * 
     * Validates:
     * - Inactive products are filtered out
     * - Only active products are returned
     */
    @Test
    @DisplayName("Should filter out inactive products from search results")
    void testSearchProducts_FilterInactive() {
        Product inactiveProduct = Product.builder()
            .id(UUID.randomUUID())
            .name("Inactive Product")
            .description("This is inactive")
            .price(BigDecimal.valueOf(50.00))
            .availableQty(5)
            .sku("INA-001")
            .isActive(false)
            .build();

        when(productRepository.searchByKeyword("Product"))
            .thenReturn(Arrays.asList(testProduct1, inactiveProduct));

        List<ProductResponse> results = productService.searchProducts("Product");

        assertThat(results).hasSize(1);
        assertThat(results.get(0).getIsActive()).isTrue();
    }

    /**
     * Test case-insensitive search
     * 
     * Validates:
     * - Search is case-insensitive
     * - Products are found regardless of case
     */
    @Test
    @DisplayName("Should perform case-insensitive search")
    void testSearchProducts_CaseInsensitive() {
        when(productRepository.searchByKeyword("laptop"))
            .thenReturn(List.of(testProduct1));

        List<ProductResponse> results = productService.searchProducts("laptop");

        assertThat(results).isNotEmpty();
        assertThat(results.get(0).getName()).isEqualTo("Laptop");
    }
}