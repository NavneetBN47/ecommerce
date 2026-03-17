package com.ecommerce.productcatalog.application.service;

import com.ecommerce.productcatalog.application.dto.*;
import com.ecommerce.productcatalog.application.exception.ProductNotFoundException;
import com.ecommerce.productcatalog.domain.entity.Product;
import com.ecommerce.productcatalog.domain.repository.ProductRepository;
import com.ecommerce.productcatalog.infrastructure.cache.ProductCacheService;
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
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Product Service Tests")
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private ProductCacheService cacheService;

    @InjectMocks
    private ProductService productService;

    private Product testProduct;
    private List<Product> productList;
    private Page<Product> productPage;

    @BeforeEach
    void setUp() {
        testProduct = Product.builder()
                .id(1L)
                .name("Test Product")
                .description("Test product description")
                .category("Electronics")
                .price(new BigDecimal("99.99"))
                .stockQuantity(100)
                .imageUrl("https://example.com/image.jpg")
                .isAvailable(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        Product product2 = Product.builder()
                .id(2L)
                .name("Another Product")
                .description("Another product description")
                .category("Electronics")
                .price(new BigDecimal("149.99"))
                .stockQuantity(50)
                .imageUrl("https://example.com/image2.jpg")
                .isAvailable(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        productList = Arrays.asList(testProduct, product2);
        productPage = new PageImpl<>(productList, PageRequest.of(0, 20), 2);
    }

    @Test
    @DisplayName("Search Products - With Keyword - Success")
    void testSearchProducts_WithKeyword_Success() {
        when(productRepository.searchProducts(anyString(), any(Pageable.class)))
                .thenReturn(productPage);

        ProductSearchResponse response = productService.searchProducts(
                "test", null, null, null, 0, 20, "name", "asc"
        );

        assertThat(response).isNotNull();
        assertThat(response.getProducts()).hasSize(2);
        assertThat(response.getPagination().getTotalElements()).isEqualTo(2);
        assertThat(response.getPagination().getCurrentPage()).isEqualTo(0);

        verify(productRepository, times(1)).searchProducts(anyString(), any(Pageable.class));
    }

    @Test
    @DisplayName("Search Products - With Category - Success")
    void testSearchProducts_WithCategory_Success() {
        when(productRepository.findByCategory(anyString(), any(Pageable.class)))
                .thenReturn(productPage);

        ProductSearchResponse response = productService.searchProducts(
                null, "Electronics", null, null, 0, 20, "name", "asc"
        );

        assertThat(response).isNotNull();
        assertThat(response.getProducts()).hasSize(2);

        verify(productRepository, times(1)).findByCategory(anyString(), any(Pageable.class));
    }

    @Test
    @DisplayName("Search Products - With Price Range - Success")
    void testSearchProducts_WithPriceRange_Success() {
        when(productRepository.findByPriceBetween(any(BigDecimal.class), any(BigDecimal.class), any(Pageable.class)))
                .thenReturn(productPage);

        ProductSearchResponse response = productService.searchProducts(
                null, null, 50.0, 200.0, 0, 20, "price", "asc"
        );

        assertThat(response).isNotNull();
        assertThat(response.getProducts()).hasSize(2);

        verify(productRepository, times(1)).findByPriceBetween(any(BigDecimal.class), any(BigDecimal.class), any(Pageable.class));
    }

    @Test
    @DisplayName("Search Products - No Filters - Returns All Products")
    void testSearchProducts_NoFilters_ReturnsAllProducts() {
        when(productRepository.findAll(any(Pageable.class)))
                .thenReturn(productPage);

        ProductSearchResponse response = productService.searchProducts(
                null, null, null, null, 0, 20, "name", "asc"
        );

        assertThat(response).isNotNull();
        assertThat(response.getProducts()).hasSize(2);

        verify(productRepository, times(1)).findAll(any(Pageable.class));
    }

    @Test
    @DisplayName("Search Products - Empty Results - Returns Empty List")
    void testSearchProducts_EmptyResults_ReturnsEmptyList() {
        Page<Product> emptyPage = new PageImpl<>(Arrays.asList(), PageRequest.of(0, 20), 0);
        when(productRepository.searchProducts(anyString(), any(Pageable.class)))
                .thenReturn(emptyPage);

        ProductSearchResponse response = productService.searchProducts(
                "nonexistent", null, null, null, 0, 20, "name", "asc"
        );

        assertThat(response).isNotNull();
        assertThat(response.getProducts()).isEmpty();
        assertThat(response.getPagination().getTotalElements()).isEqualTo(0);

        verify(productRepository, times(1)).searchProducts(anyString(), any(Pageable.class));
    }

    @Test
    @DisplayName("Get Product By ID - Valid ID - Success")
    void testGetProductById_ValidId_Success() {
        when(cacheService.getProduct(1L)).thenReturn(null);
        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));

        ProductResponse response = productService.getProductById(1L);

        assertThat(response).isNotNull();
        assertThat(response.getProductId()).isEqualTo(1L);
        assertThat(response.getName()).isEqualTo("Test Product");
        assertThat(response.getCategory()).isEqualTo("Electronics");
        assertThat(response.getPrice()).isEqualByComparingTo(new BigDecimal("99.99"));
        assertThat(response.getStockQuantity()).isEqualTo(100);
        assertThat(response.isAvailable()).isTrue();

        verify(cacheService, times(1)).getProduct(1L);
        verify(productRepository, times(1)).findById(1L);
        verify(cacheService, times(1)).cacheProduct(any(Product.class));
    }

    @Test
    @DisplayName("Get Product By ID - From Cache - Success")
    void testGetProductById_FromCache_Success() {
        when(cacheService.getProduct(1L)).thenReturn(testProduct);

        ProductResponse response = productService.getProductById(1L);

        assertThat(response).isNotNull();
        assertThat(response.getProductId()).isEqualTo(1L);

        verify(cacheService, times(1)).getProduct(1L);
        verify(productRepository, never()).findById(anyLong());
    }

    @Test
    @DisplayName("Get Product By ID - Invalid ID - Throws Exception")
    void testGetProductById_InvalidId_ThrowsException() {
        when(cacheService.getProduct(999L)).thenReturn(null);
        when(productRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.getProductById(999L))
                .isInstanceOf(ProductNotFoundException.class)
                .hasMessageContaining("Product not found");

        verify(cacheService, times(1)).getProduct(999L);
        verify(productRepository, times(1)).findById(999L);
    }

    @Test
    @DisplayName("Get Products By Category - Valid Category - Success")
    void testGetProductsByCategory_ValidCategory_Success() {
        when(productRepository.findByCategory("Electronics", PageRequest.of(0, 20)))
                .thenReturn(productPage);

        ProductSearchResponse response = productService.getProductsByCategory("Electronics", 0, 20);

        assertThat(response).isNotNull();
        assertThat(response.getProducts()).hasSize(2);
        assertThat(response.getProducts()).allMatch(p -> p.getCategory().equals("Electronics"));

        verify(productRepository, times(1)).findByCategory("Electronics", PageRequest.of(0, 20));
    }

    @Test
    @DisplayName("Get Products By Category - Empty Category - Returns Empty List")
    void testGetProductsByCategory_EmptyCategory_ReturnsEmptyList() {
        Page<Product> emptyPage = new PageImpl<>(Arrays.asList(), PageRequest.of(0, 20), 0);
        when(productRepository.findByCategory("NonExistentCategory", PageRequest.of(0, 20)))
                .thenReturn(emptyPage);

        ProductSearchResponse response = productService.getProductsByCategory("NonExistentCategory", 0, 20);

        assertThat(response).isNotNull();
        assertThat(response.getProducts()).isEmpty();

        verify(productRepository, times(1)).findByCategory("NonExistentCategory", PageRequest.of(0, 20));
    }

    @Test
    @DisplayName("Check Availability - Sufficient Stock - Returns Available")
    void testCheckAvailability_SufficientStock_ReturnsAvailable() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));

        ProductAvailabilityResponse response = productService.checkAvailability(1L, 5);

        assertThat(response).isNotNull();
        assertThat(response.getProductId()).isEqualTo(1L);
        assertThat(response.isAvailable()).isTrue();
        assertThat(response.getAvailableQuantity()).isEqualTo(100);
        assertThat(response.getRequestedQuantity()).isEqualTo(5);
        assertThat(response.getMessage()).contains("available");

        verify(productRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("Check Availability - Insufficient Stock - Returns Unavailable")
    void testCheckAvailability_InsufficientStock_ReturnsUnavailable() {
        testProduct.setStockQuantity(3);
        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));

        ProductAvailabilityResponse response = productService.checkAvailability(1L, 10);

        assertThat(response).isNotNull();
        assertThat(response.isAvailable()).isFalse();
        assertThat(response.getAvailableQuantity()).isEqualTo(3);
        assertThat(response.getRequestedQuantity()).isEqualTo(10);
        assertThat(response.getMessage()).contains("Insufficient stock");

        verify(productRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("Check Availability - Product Not Available - Returns Unavailable")
    void testCheckAvailability_ProductNotAvailable_ReturnsUnavailable() {
        testProduct.setAvailable(false);
        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));

        ProductAvailabilityResponse response = productService.checkAvailability(1L, 5);

        assertThat(response).isNotNull();
        assertThat(response.isAvailable()).isFalse();
        assertThat(response.getMessage()).contains("not available");

        verify(productRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("Check Availability - Product Not Found - Throws Exception")
    void testCheckAvailability_ProductNotFound_ThrowsException() {
        when(productRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.checkAvailability(999L, 5))
                .isInstanceOf(ProductNotFoundException.class)
                .hasMessageContaining("Product not found");

        verify(productRepository, times(1)).findById(999L);
    }

    @Test
    @DisplayName("Check Availability - Zero Stock - Returns Unavailable")
    void testCheckAvailability_ZeroStock_ReturnsUnavailable() {
        testProduct.setStockQuantity(0);
        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));

        ProductAvailabilityResponse response = productService.checkAvailability(1L, 1);

        assertThat(response).isNotNull();
        assertThat(response.isAvailable()).isFalse();
        assertThat(response.getAvailableQuantity()).isEqualTo(0);

        verify(productRepository, times(1)).findById(1L);
    }
}