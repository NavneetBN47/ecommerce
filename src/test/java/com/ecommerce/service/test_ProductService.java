package com.ecommerce.service;

import com.ecommerce.dto.ProductResponse;
import com.ecommerce.entity.Product;
import com.ecommerce.exception.ResourceNotFoundException;
import com.ecommerce.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Comprehensive JUnit 5 test class for ProductService.
 * Tests all public methods with proper mocking of ProductRepository dependency.
 * Covers normal execution paths, edge cases, and exception scenarios.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ProductService Tests")
class test_ProductService {

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductService productService;

    private Product testProduct1;
    private Product testProduct2;
    private Product inactiveProduct;

    @BeforeEach
    void setUp() {
        testProduct1 = new Product();
        testProduct1.setProductId(1L);
        testProduct1.setProductName("Gaming Laptop");
        testProduct1.setDescription("High performance gaming laptop");
        testProduct1.setPrice(BigDecimal.valueOf(1500.00));
        testProduct1.setAvailableQty(10);
        testProduct1.setIsActive(true);

        testProduct2 = new Product();
        testProduct2.setProductId(2L);
        testProduct2.setProductName("Business Laptop");
        testProduct2.setDescription("Professional business laptop");
        testProduct2.setPrice(BigDecimal.valueOf(1200.00));
        testProduct2.setAvailableQty(5);
        testProduct2.setIsActive(true);

        inactiveProduct = new Product();
        inactiveProduct.setProductId(3L);
        inactiveProduct.setProductName("Discontinued Laptop");
        inactiveProduct.setDescription("No longer available");
        inactiveProduct.setPrice(BigDecimal.valueOf(800.00));
        inactiveProduct.setAvailableQty(0);
        inactiveProduct.setIsActive(false);
    }

    /**
     * Test successful product search with all parameters.
     * Verifies that the service returns paginated results with search query.
     */
    @Test
    @DisplayName("Should search products with all parameters")
    void testSearchProducts_WithAllParameters() {
        // Given
        String search = "laptop";
        Integer page = 0;
        Integer size = 10;
        Pageable pageable = PageRequest.of(page, size);
        List<Product> products = Arrays.asList(testProduct1, testProduct2);
        Page<Product> productPage = new PageImpl<>(products, pageable, products.size());

        when(productRepository.searchActiveProducts(search, pageable)).thenReturn(productPage);

        // When
        List<ProductResponse> result = productService.searchProducts(search, page, size);

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("Gaming Laptop", result.get(0).getProductName());
        assertEquals("Business Laptop", result.get(1).getProductName());
        verify(productRepository, times(1)).searchActiveProducts(search, pageable);
    }

    /**
     * Test product search with search parameter only.
     * Verifies that the service uses repository search without pagination.
     */
    @Test
    @DisplayName("Should search products with search parameter only")
    void testSearchProducts_SearchOnly() {
        // Given
        String search = "gaming";
        Integer page = null;
        Integer size = null;
        List<Product> products = Arrays.asList(testProduct1);

        when(productRepository.searchByName(search)).thenReturn(products);

        // When
        List<ProductResponse> result = productService.searchProducts(search, page, size);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Gaming Laptop", result.get(0).getProductName());
        assertEquals(BigDecimal.valueOf(1500.00), result.get(0).getPrice());
        verify(productRepository, times(1)).searchByName(search);
        verify(productRepository, never()).searchActiveProducts(any(), any());
        verify(productRepository, never()).findByIsActiveTrue(any());
    }

    /**
     * Test product search with null search parameter.
     * Verifies that the service returns all active products with default pagination.
     */
    @Test
    @DisplayName("Should return all active products when search is null")
    void testSearchProducts_NullSearch() {
        // Given
        String search = null;
        Integer page = null;
        Integer size = null;
        Pageable pageable = PageRequest.of(0, 20); // Default pagination
        List<Product> products = Arrays.asList(testProduct1, testProduct2);
        Page<Product> productPage = new PageImpl<>(products, pageable, products.size());

        when(productRepository.findByIsActiveTrue(pageable)).thenReturn(productPage);

        // When
        List<ProductResponse> result = productService.searchProducts(search, page, size);

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(productRepository, times(1)).findByIsActiveTrue(pageable);
    }

    /**
     * Test product search with empty search string.
     * Verifies that empty search string is treated as null.
     */
    @Test
    @DisplayName("Should handle empty search string")
    void testSearchProducts_EmptySearch() {
        // Given
        String search = "";
        Integer page = 0;
        Integer size = 5;
        Pageable pageable = PageRequest.of(0, 5);
        List<Product> products = Arrays.asList(testProduct1);
        Page<Product> productPage = new PageImpl<>(products, pageable, products.size());

        when(productRepository.findByIsActiveTrue(pageable)).thenReturn(productPage);

        // When
        List<ProductResponse> result = productService.searchProducts(search, page, size);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(productRepository, times(1)).findByIsActiveTrue(pageable);
    }

    /**
     * Test product search with whitespace-only search string.
     * Verifies that whitespace-only search is treated as empty.
     */
    @Test
    @DisplayName("Should handle whitespace-only search string")
    void testSearchProducts_WhitespaceSearch() {
        // Given
        String search = "   ";
        Integer page = 1;
        Integer size = 15;
        Pageable pageable = PageRequest.of(1, 15);
        List<Product> products = Arrays.asList(testProduct2);
        Page<Product> productPage = new PageImpl<>(products, pageable, products.size());

        when(productRepository.findByIsActiveTrue(pageable)).thenReturn(productPage);

        // When
        List<ProductResponse> result = productService.searchProducts(search, page, size);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(productRepository, times(1)).findByIsActiveTrue(pageable);
    }

    /**
     * Test product search returning empty results.
     * Verifies that the service handles empty search results correctly.
     */
    @Test
    @DisplayName("Should handle empty search results")
    void testSearchProducts_EmptyResults() {
        // Given
        String search = "nonexistent";
        Integer page = 0;
        Integer size = 10;
        Pageable pageable = PageRequest.of(0, 10);
        Page<Product> emptyPage = new PageImpl<>(Collections.emptyList(), pageable, 0);

        when(productRepository.searchActiveProducts(search, pageable)).thenReturn(emptyPage);

        // When
        List<ProductResponse> result = productService.searchProducts(search, page, size);

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(productRepository, times(1)).searchActiveProducts(search, pageable);
    }

    /**
     * Test product search with large page size.
     * Verifies that the service handles large page sizes correctly.
     */
    @Test
    @DisplayName("Should handle large page size")
    void testSearchProducts_LargePageSize() {
        // Given
        String search = "laptop";
        Integer page = 0;
        Integer size = 1000;
        Pageable pageable = PageRequest.of(0, 1000);
        List<Product> products = Arrays.asList(testProduct1, testProduct2);
        Page<Product> productPage = new PageImpl<>(products, pageable, products.size());

        when(productRepository.searchActiveProducts(search, pageable)).thenReturn(productPage);

        // When
        List<ProductResponse> result = productService.searchProducts(search, page, size);

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(productRepository, times(1)).searchActiveProducts(search, pageable);
    }

    /**
     * Test successful product retrieval by ID.
     * Verifies that the service returns correct product response.
     */
    @Test
    @DisplayName("Should get product by ID successfully")
    void testGetProductById_Success() {
        // Given
        Long productId = 1L;

        when(productRepository.findById(productId)).thenReturn(Optional.of(testProduct1));

        // When
        ProductResponse result = productService.getProductById(productId);

        // Then
        assertNotNull(result);
        assertEquals(productId, result.getProductId());
        assertEquals("Gaming Laptop", result.getProductName());
        assertEquals("High performance gaming laptop", result.getDescription());
        assertEquals(BigDecimal.valueOf(1500.00), result.getPrice());
        assertEquals(10, result.getAvailableQty());
        assertTrue(result.getIsActive());
        verify(productRepository, times(1)).findById(productId);
    }

    /**
     * Test product retrieval for inactive product.
     * Verifies that the service returns inactive product correctly.
     */
    @Test
    @DisplayName("Should get inactive product by ID")
    void testGetProductById_InactiveProduct() {
        // Given
        Long productId = 3L;

        when(productRepository.findById(productId)).thenReturn(Optional.of(inactiveProduct));

        // When
        ProductResponse result = productService.getProductById(productId);

        // Then
        assertNotNull(result);
        assertEquals(productId, result.getProductId());
        assertEquals("Discontinued Laptop", result.getProductName());
        assertFalse(result.getIsActive());
        assertEquals(0, result.getAvailableQty());
        verify(productRepository, times(1)).findById(productId);
    }

    /**
     * Test product retrieval with zero stock.
     * Verifies that the service handles zero stock products correctly.
     */
    @Test
    @DisplayName("Should get product with zero stock")
    void testGetProductById_ZeroStock() {
        // Given
        Long productId = 2L;
        testProduct2.setAvailableQty(0);

        when(productRepository.findById(productId)).thenReturn(Optional.of(testProduct2));

        // When
        ProductResponse result = productService.getProductById(productId);

        // Then
        assertNotNull(result);
        assertEquals(productId, result.getProductId());
        assertEquals(0, result.getAvailableQty());
        assertTrue(result.getIsActive());
        verify(productRepository, times(1)).findById(productId);
    }

    /**
     * Test product retrieval for non-existent product.
     * Verifies that the service throws ResourceNotFoundException.
     */
    @Test
    @DisplayName("Should throw exception for non-existent product")
    void testGetProductById_NotFound() {
        // Given
        Long productId = 999L;

        when(productRepository.findById(productId)).thenReturn(Optional.empty());

        // When & Then
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            productService.getProductById(productId);
        });
        
        assertEquals("Product not found: " + productId, exception.getMessage());
        verify(productRepository, times(1)).findById(productId);
    }

    /**
     * Test product retrieval with null ID.
     * Verifies that the service handles null ID appropriately.
     */
    @Test
    @DisplayName("Should handle null product ID")
    void testGetProductById_NullId() {
        // Given
        Long productId = null;

        when(productRepository.findById(productId)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(ResourceNotFoundException.class, () -> {
            productService.getProductById(productId);
        });
        
        verify(productRepository, times(1)).findById(productId);
    }

    /**
     * Test product mapping with all fields.
     * Verifies that the service correctly maps all product fields to response.
     */
    @Test
    @DisplayName("Should map all product fields correctly")
    void testProductMapping_AllFields() {
        // Given
        Long productId = 1L;
        testProduct1.setDescription("Very detailed description of the gaming laptop");
        testProduct1.setPrice(BigDecimal.valueOf(2499.99));
        testProduct1.setAvailableQty(25);

        when(productRepository.findById(productId)).thenReturn(Optional.of(testProduct1));

        // When
        ProductResponse result = productService.getProductById(productId);

        // Then
        assertNotNull(result);
        assertEquals(1L, result.getProductId());
        assertEquals("Gaming Laptop", result.getProductName());
        assertEquals("Very detailed description of the gaming laptop", result.getDescription());
        assertEquals(BigDecimal.valueOf(2499.99), result.getPrice());
        assertEquals(25, result.getAvailableQty());
        assertTrue(result.getIsActive());
    }

    /**
     * Test product search with case-insensitive search.
     * Verifies that the service delegates case sensitivity to repository.
     */
    @Test
    @DisplayName("Should handle case-insensitive search")
    void testSearchProducts_CaseInsensitive() {
        // Given
        String search = "LAPTOP";
        List<Product> products = Arrays.asList(testProduct1, testProduct2);

        when(productRepository.searchByName(search)).thenReturn(products);

        // When
        List<ProductResponse> result = productService.searchProducts(search, null, null);

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(productRepository, times(1)).searchByName(search);
    }

    /**
     * Test product search with special characters.
     * Verifies that the service handles special characters in search queries.
     */
    @Test
    @DisplayName("Should handle special characters in search")
    void testSearchProducts_SpecialCharacters() {
        // Given
        String search = "laptop-2023";
        List<Product> products = Arrays.asList(testProduct1);

        when(productRepository.searchByName(search)).thenReturn(products);

        // When
        List<ProductResponse> result = productService.searchProducts(search, null, null);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(productRepository, times(1)).searchByName(search);
    }
}