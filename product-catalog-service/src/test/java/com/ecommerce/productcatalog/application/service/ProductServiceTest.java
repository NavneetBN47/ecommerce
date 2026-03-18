package com.ecommerce.productcatalog.application.service;

import com.ecommerce.productcatalog.application.dto.*;
import com.ecommerce.productcatalog.application.exception.*;
import com.ecommerce.productcatalog.domain.entity.Product;
import com.ecommerce.productcatalog.domain.repository.ProductRepository;
import com.ecommerce.productcatalog.infrastructure.cache.ProductCacheService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
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
    private PageRequest pageRequest;

    @BeforeEach
    void setUp() {
        testProduct = new Product();
        testProduct.setProductId(1L);
        testProduct.setName("Test Product");
        testProduct.setDescription("Test Description");
        testProduct.setPrice(new BigDecimal("99.99"));
        testProduct.setCategory("Electronics");
        testProduct.setStockQuantity(100);
        testProduct.setIsAvailable(true);
        testProduct.setCreatedAt(LocalDateTime.now());
        testProduct.setUpdatedAt(LocalDateTime.now());

        pageRequest = PageRequest.of(0, 10);
    }

    @Test
    @DisplayName("Search Products - Success")
    void testSearchProducts_Success() {
        List<Product> products = Arrays.asList(testProduct);
        Page<Product> productPage = new PageImpl<>(products, pageRequest, 1);

        when(productRepository.searchProducts(anyString(), any(Pageable.class))).thenReturn(productPage);

        ProductSearchResponse response = productService.searchProducts("test", pageRequest);

        assertThat(response).isNotNull();
        assertThat(response.getProducts()).hasSize(1);
        assertThat(response.getProducts().get(0).getName()).isEqualTo("Test Product");
        assertThat(response.getMetadata().getTotalItems()).isEqualTo(1L);
        assertThat(response.getMetadata().getCurrentPage()).isEqualTo(1);

        verify(productRepository, times(1)).searchProducts("test", pageRequest);
    }

    @Test
    @DisplayName("Search Products - Empty Results")
    void testSearchProducts_EmptyResults() {
        Page<Product> emptyPage = new PageImpl<>(Arrays.asList(), pageRequest, 0);

        when(productRepository.searchProducts(anyString(), any(Pageable.class))).thenReturn(emptyPage);

        ProductSearchResponse response = productService.searchProducts("nonexistent", pageRequest);

        assertThat(response).isNotNull();
        assertThat(response.getProducts()).isEmpty();
        assertThat(response.getMetadata().getTotalItems()).isEqualTo(0L);
    }

    @Test
    @DisplayName("Search Products - Null Query")
    void testSearchProducts_NullQuery() {
        assertThatThrownBy(() -> productService.searchProducts(null, pageRequest))
                .isInstanceOf(IllegalArgumentException.class);

        verify(productRepository, never()).searchProducts(anyString(), any(Pageable.class));
    }

    @Test
    @DisplayName("Get Product By ID - Success (Cache Miss)")
    void testGetProductById_CacheMiss() {
        when(cacheService.getProduct(anyLong())).thenReturn(null);
        when(productRepository.findById(anyLong())).thenReturn(Optional.of(testProduct));

        ProductResponse response = productService.getProductById(1L);

        assertThat(response).isNotNull();
        assertThat(response.getProductId()).isEqualTo(1L);
        assertThat(response.getName()).isEqualTo("Test Product");
        assertThat(response.getPrice()).isEqualByComparingTo(new BigDecimal("99.99"));

        verify(cacheService, times(1)).getProduct(1L);
        verify(productRepository, times(1)).findById(1L);
        verify(cacheService, times(1)).cacheProduct(anyLong(), any(ProductResponse.class));
    }

    @Test
    @DisplayName("Get Product By ID - Success (Cache Hit)")
    void testGetProductById_CacheHit() {
        ProductResponse cachedResponse = new ProductResponse();
        cachedResponse.setProductId(1L);
        cachedResponse.setName("Test Product");

        when(cacheService.getProduct(anyLong())).thenReturn(cachedResponse);

        ProductResponse response = productService.getProductById(1L);

        assertThat(response).isNotNull();
        assertThat(response.getProductId()).isEqualTo(1L);

        verify(cacheService, times(1)).getProduct(1L);
        verify(productRepository, never()).findById(anyLong());
    }

    @Test
    @DisplayName("Get Product By ID - Not Found")
    void testGetProductById_NotFound() {
        when(cacheService.getProduct(anyLong())).thenReturn(null);
        when(productRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.getProductById(999L))
                .isInstanceOf(ProductNotFoundException.class)
                .hasMessageContaining("999");

        verify(productRepository, times(1)).findById(999L);
        verify(cacheService, never()).cacheProduct(anyLong(), any(ProductResponse.class));
    }

    @Test
    @DisplayName("Get Product By ID - Invalid ID")
    void testGetProductById_InvalidId() {
        assertThatThrownBy(() -> productService.getProductById(-1L))
                .isInstanceOf(IllegalArgumentException.class);

        verify(productRepository, never()).findById(anyLong());
    }

    @Test
    @DisplayName("Get Products By Category - Success")
    void testGetProductsByCategory_Success() {
        List<Product> products = Arrays.asList(testProduct);
        Page<Product> productPage = new PageImpl<>(products, pageRequest, 1);

        when(productRepository.findByCategory(anyString(), any(Pageable.class))).thenReturn(productPage);

        ProductSearchResponse response = productService.getProductsByCategory("Electronics", pageRequest);

        assertThat(response).isNotNull();
        assertThat(response.getProducts()).hasSize(1);
        assertThat(response.getProducts().get(0).getCategory()).isEqualTo("Electronics");

        verify(productRepository, times(1)).findByCategory("Electronics", pageRequest);
    }

    @Test
    @DisplayName("Get Products By Category - Empty Category")
    void testGetProductsByCategory_EmptyCategory() {
        assertThatThrownBy(() -> productService.getProductsByCategory("", pageRequest))
                .isInstanceOf(IllegalArgumentException.class);

        verify(productRepository, never()).findByCategory(anyString(), any(Pageable.class));
    }

    @Test
    @DisplayName("Get Available Products - Success")
    void testGetAvailableProducts_Success() {
        List<Product> products = Arrays.asList(testProduct);
        Page<Product> productPage = new PageImpl<>(products, pageRequest, 1);

        when(productRepository.findByIsAvailableTrue(any(Pageable.class))).thenReturn(productPage);

        ProductSearchResponse response = productService.getAvailableProducts(pageRequest);

        assertThat(response).isNotNull();
        assertThat(response.getProducts()).hasSize(1);
        assertThat(response.getProducts().get(0).getProductId()).isEqualTo(1L);

        verify(productRepository, times(1)).findByIsAvailableTrue(pageRequest);
    }

    @Test
    @DisplayName("Get Available Products - Only Available Products Returned")
    void testGetAvailableProducts_OnlyAvailable() {
        Product unavailableProduct = new Product();
        unavailableProduct.setProductId(2L);
        unavailableProduct.setIsAvailable(false);

        List<Product> products = Arrays.asList(testProduct);
        Page<Product> productPage = new PageImpl<>(products, pageRequest, 1);

        when(productRepository.findByIsAvailableTrue(any(Pageable.class))).thenReturn(productPage);

        ProductSearchResponse response = productService.getAvailableProducts(pageRequest);

        assertThat(response.getProducts()).allMatch(p -> p.getProductId().equals(1L));
    }

    @Test
    @DisplayName("Check Product Availability - Available")
    void testCheckProductAvailability_Available() {
        when(productRepository.findById(anyLong())).thenReturn(Optional.of(testProduct));

        boolean isAvailable = productService.checkProductAvailability(1L, 50);

        assertThat(isAvailable).isTrue();
        verify(productRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("Check Product Availability - Insufficient Stock")
    void testCheckProductAvailability_InsufficientStock() {
        when(productRepository.findById(anyLong())).thenReturn(Optional.of(testProduct));

        boolean isAvailable = productService.checkProductAvailability(1L, 150);

        assertThat(isAvailable).isFalse();
    }

    @Test
    @DisplayName("Check Product Availability - Product Not Available")
    void testCheckProductAvailability_NotAvailable() {
        testProduct.setIsAvailable(false);
        when(productRepository.findById(anyLong())).thenReturn(Optional.of(testProduct));

        assertThatThrownBy(() -> productService.checkProductAvailability(1L, 50))
                .isInstanceOf(ProductNotAvailableException.class);
    }

    @Test
    @DisplayName("Check Product Availability - Product Not Found")
    void testCheckProductAvailability_NotFound() {
        when(productRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.checkProductAvailability(999L, 50))
                .isInstanceOf(ProductNotFoundException.class);
    }

    @Test
    @DisplayName("Pagination - Multiple Pages")
    void testPagination_MultiplePages() {
        List<Product> page1Products = Arrays.asList(testProduct);
        Page<Product> page1 = new PageImpl<>(page1Products, PageRequest.of(0, 1), 3);

        when(productRepository.findByIsAvailableTrue(any(Pageable.class))).thenReturn(page1);

        ProductSearchResponse response = productService.getAvailableProducts(PageRequest.of(0, 1));

        assertThat(response.getMetadata().getTotalPages()).isEqualTo(3);
        assertThat(response.getMetadata().getCurrentPage()).isEqualTo(1);
        assertThat(response.getMetadata().getTotalItems()).isEqualTo(3L);
    }

    @Test
    @DisplayName("Cache Eviction - Product Update")
    void testCacheEviction_ProductUpdate() {
        when(productRepository.findById(anyLong())).thenReturn(Optional.of(testProduct));
        when(productRepository.save(any(Product.class))).thenReturn(testProduct);

        productService.updateProduct(1L, testProduct);

        verify(cacheService, times(1)).evictProduct(1L);
    }
}