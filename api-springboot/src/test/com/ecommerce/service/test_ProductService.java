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
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Test class for ProductService
 * 
 * This test class verifies the business logic for product management,
 * including product search and filtering operations.
 * 
 * @author Shopping Cart System Team
 * @version 1.0.0
 */
@ExtendWith(MockitoExtension.class)
class test_ProductService {

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductService productService;

    private Product product1;
    private Product product2;
    private Product inactiveProduct;
    private List<Product> activeProducts;

    /**
     * Setup method to initialize test data before each test
     */
    @BeforeEach
    void setUp() {
        product1 = Product.builder()
                .id(UUID.randomUUID())
                .name("Laptop")
                .description("High-performance laptop")
                .price(new BigDecimal("999.99"))
                .availableQty(10)
                .sku("LAP-001")
                .isActive(true)
                .build();

        product2 = Product.builder()
                .id(UUID.randomUUID())
                .name("Laptop Stand")
                .description("Ergonomic laptop stand")
                .price(new BigDecimal("49.99"))
                .availableQty(25)
                .sku("ACC-001")
                .isActive(true)
                .build();

        inactiveProduct = Product.builder()
                .id(UUID.randomUUID())
                .name("Old Laptop")
                .description("Discontinued laptop model")
                .price(new BigDecimal("499.99"))
                .availableQty(0)
                .sku("LAP-OLD")
                .isActive(false)
                .build();

        activeProducts = Arrays.asList(product1, product2);
    }

    /**
     * Test successful product search with keyword
     * 
     * Verifies that products can be searched by keyword and
     * returns only active products matching the keyword.
     */
    @Test
    void searchProducts_WithKeyword_ShouldReturnMatchingActiveProducts() {
        String keyword = "laptop";
        when(productRepository.searchByKeyword(keyword)).thenReturn(activeProducts);

        List<ProductResponse> results = productService.searchProducts(keyword);

        assertThat(results).isNotNull();
        assertThat(results).hasSize(2);
        assertThat(results).allMatch(p -> p.getName().toLowerCase().contains("laptop"));
        assertThat(results).allMatch(ProductResponse::getIsActive);
        verify(productRepository, times(1)).searchByKeyword(keyword);
    }

    /**
     * Test product search with null keyword returns all active products
     * 
     * Verifies that searching with null keyword returns all active products.
     */
    @Test
    void searchProducts_WithNullKeyword_ShouldReturnAllActiveProducts() {
        when(productRepository.findByIsActiveTrue()).thenReturn(activeProducts);

        List<ProductResponse> results = productService.searchProducts(null);

        assertThat(results).isNotNull();
        assertThat(results).hasSize(2);
        assertThat(results).allMatch(ProductResponse::getIsActive);
        verify(productRepository, times(1)).findByIsActiveTrue();
        verify(productRepository, never()).searchByKeyword(anyString());
    }

    /**
     * Test product search with empty keyword returns all active products
     * 
     * Verifies that searching with empty keyword returns all active products.
     */
    @Test
    void searchProducts_WithEmptyKeyword_ShouldReturnAllActiveProducts() {
        String emptyKeyword = "";
        when(productRepository.findByIsActiveTrue()).thenReturn(activeProducts);

        List<ProductResponse> results = productService.searchProducts(emptyKeyword);

        assertThat(results).isNotNull();
        assertThat(results).hasSize(2);
        verify(productRepository, times(1)).findByIsActiveTrue();
        verify(productRepository, never()).searchByKeyword(anyString());
    }

    /**
     * Test product search with whitespace keyword returns all active products
     * 
     * Verifies that searching with whitespace-only keyword
     * returns all active products.
     */
    @Test
    void searchProducts_WithWhitespaceKeyword_ShouldReturnAllActiveProducts() {
        String whitespaceKeyword = "   ";
        when(productRepository.findByIsActiveTrue()).thenReturn(activeProducts);

        List<ProductResponse> results = productService.searchProducts(whitespaceKeyword);

        assertThat(results).isNotNull();
        assertThat(results).hasSize(2);
        verify(productRepository, times(1)).findByIsActiveTrue();
    }

    /**
     * Test product search with no matching results
     * 
     * Verifies that searching with keyword that matches no products
     * returns an empty list.
     */
    @Test
    void searchProducts_WithNoMatchingResults_ShouldReturnEmptyList() {
        String keyword = "nonexistent";
        when(productRepository.searchByKeyword(keyword)).thenReturn(Collections.emptyList());

        List<ProductResponse> results = productService.searchProducts(keyword);

        assertThat(results).isNotNull();
        assertThat(results).isEmpty();
        verify(productRepository, times(1)).searchByKeyword(keyword);
    }

    /**
     * Test product search filters out inactive products
     * 
     * Verifies that search results only include active products
     * and inactive products are filtered out.
     */
    @Test
    void searchProducts_ShouldFilterOutInactiveProducts() {
        String keyword = "laptop";
        List<Product> mixedProducts = Arrays.asList(product1, inactiveProduct, product2);
        when(productRepository.searchByKeyword(keyword)).thenReturn(mixedProducts);

        List<ProductResponse> results = productService.searchProducts(keyword);

        assertThat(results).isNotNull();
        assertThat(results).hasSize(2);
        assertThat(results).allMatch(ProductResponse::getIsActive);
        assertThat(results).noneMatch(p -> p.getName().equals("Old Laptop"));
    }

    /**
     * Test product search with case-insensitive keyword
     * 
     * Verifies that product search handles keywords in different cases
     * by trimming and passing to repository.
     */
    @Test
    void searchProducts_WithCaseInsensitiveKeyword_ShouldSearchCorrectly() {
        String keyword = "LAPTOP";
        when(productRepository.searchByKeyword(keyword)).thenReturn(activeProducts);

        List<ProductResponse> results = productService.searchProducts(keyword);

        assertThat(results).isNotNull();
        assertThat(results).hasSize(2);
        verify(productRepository, times(1)).searchByKeyword(keyword);
    }

    /**
     * Test product search trims keyword whitespace
     * 
     * Verifies that leading and trailing whitespace is trimmed
     * from search keywords.
     */
    @Test
    void searchProducts_ShouldTrimKeywordWhitespace() {
        String keywordWithSpaces = "  laptop  ";
        when(productRepository.searchByKeyword("laptop")).thenReturn(activeProducts);

        List<ProductResponse> results = productService.searchProducts(keywordWithSpaces);

        assertThat(results).isNotNull();
        verify(productRepository, times(1)).searchByKeyword("laptop");
    }

    /**
     * Test product response mapping includes all fields
     * 
     * Verifies that Product entities are correctly mapped to
     * ProductResponse DTOs with all fields populated.
     */
    @Test
    void searchProducts_ShouldMapAllProductFields() {
        String keyword = "laptop";
        when(productRepository.searchByKeyword(keyword)).thenReturn(Arrays.asList(product1));

        List<ProductResponse> results = productService.searchProducts(keyword);

        assertThat(results).hasSize(1);
        ProductResponse response = results.get(0);
        assertThat(response.getId()).isEqualTo(product1.getId());
        assertThat(response.getName()).isEqualTo(product1.getName());
        assertThat(response.getDescription()).isEqualTo(product1.getDescription());
        assertThat(response.getPrice()).isEqualTo(product1.getPrice());
        assertThat(response.getAvailableQty()).isEqualTo(product1.getAvailableQty());
        assertThat(response.getSku()).isEqualTo(product1.getSku());
        assertThat(response.getIsActive()).isEqualTo(product1.getIsActive());
    }

    /**
     * Test product search with partial keyword match
     * 
     * Verifies that product search supports partial keyword matching.
     */
    @Test
    void searchProducts_WithPartialKeyword_ShouldReturnMatchingProducts() {
        String keyword = "lap";
        when(productRepository.searchByKeyword(keyword)).thenReturn(activeProducts);

        List<ProductResponse> results = productService.searchProducts(keyword);

        assertThat(results).isNotNull();
        assertThat(results).hasSize(2);
        verify(productRepository, times(1)).searchByKeyword(keyword);
    }
}