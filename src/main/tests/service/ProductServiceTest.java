package com.ecommerce.productcatalog.service;

import com.ecommerce.productcatalog.dto.ProductDTO;
import com.ecommerce.productcatalog.dto.PageResponseDTO;
import com.ecommerce.productcatalog.entity.Product;
import com.ecommerce.productcatalog.repository.ProductRepository;
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

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Product Service Tests")
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private ProductCacheService productCacheService;

    @InjectMocks
    private ProductService productService;

    private Product product;
    private ProductDTO productDTO;

    @BeforeEach
    void setUp() {
        product = Product.builder()
                .id(UUID.randomUUID())
                .name("Laptop")
                .description("High-performance laptop")
                .price(new BigDecimal("999.99"))
                .stock(50)
                .available(true)
                .build();

        productDTO = ProductDTO.builder()
                .id(product.getId())
                .name("Laptop")
                .description("High-performance laptop")
                .price(new BigDecimal("999.99"))
                .stock(50)
                .available(true)
                .build();
    }

    @Test
    @DisplayName("Should search products successfully with keyword")
    void testSearchProducts_WithKeyword() {
        PageRequest pageRequest = PageRequest.of(0, 10);
        Page<Product> productPage = new PageImpl<>(Arrays.asList(product));

        when(productRepository.searchProducts("laptop", pageRequest)).thenReturn(productPage);

        PageResponseDTO<ProductDTO> result = productService.searchProducts("laptop", pageRequest);

        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getName()).isEqualTo("Laptop");
        assertThat(result.getTotalElements()).isEqualTo(1);

        verify(productRepository, times(1)).searchProducts("laptop", pageRequest);
    }

    @Test
    @DisplayName("Should search products successfully without keyword")
    void testSearchProducts_WithoutKeyword() {
        PageRequest pageRequest = PageRequest.of(0, 10);
        Page<Product> productPage = new PageImpl<>(Arrays.asList(product));

        when(productRepository.findAll(pageRequest)).thenReturn(productPage);

        PageResponseDTO<ProductDTO> result = productService.searchProducts("", pageRequest);

        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);

        verify(productRepository, times(1)).findAll(pageRequest);
        verify(productRepository, never()).searchProducts(anyString(), any(PageRequest.class));
    }

    @Test
    @DisplayName("Should return empty results when no products match")
    void testSearchProducts_NoResults() {
        PageRequest pageRequest = PageRequest.of(0, 10);
        Page<Product> emptyPage = new PageImpl<>(Collections.emptyList());

        when(productRepository.searchProducts("nonexistent", pageRequest)).thenReturn(emptyPage);

        PageResponseDTO<ProductDTO> result = productService.searchProducts("nonexistent", pageRequest);

        assertThat(result).isNotNull();
        assertThat(result.getContent()).isEmpty();
        assertThat(result.getTotalElements()).isEqualTo(0);

        verify(productRepository, times(1)).searchProducts("nonexistent", pageRequest);
    }

    @Test
    @DisplayName("Should get product by ID from cache")
    void testGetProductById_CacheHit() {
        UUID productId = UUID.randomUUID();
        when(productCacheService.getProduct(productId)).thenReturn(Optional.of(productDTO));

        ProductDTO result = productService.getProductById(productId);

        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("Laptop");

        verify(productCacheService, times(1)).getProduct(productId);
        verify(productRepository, never()).findById(productId);
    }

    @Test
    @DisplayName("Should get product by ID from database when cache miss")
    void testGetProductById_CacheMiss() {
        UUID productId = UUID.randomUUID();
        when(productCacheService.getProduct(productId)).thenReturn(Optional.empty());
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        doNothing().when(productCacheService).saveProduct(any(ProductDTO.class));

        ProductDTO result = productService.getProductById(productId);

        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("Laptop");

        verify(productCacheService, times(1)).getProduct(productId);
        verify(productRepository, times(1)).findById(productId);
        verify(productCacheService, times(1)).saveProduct(any(ProductDTO.class));
    }

    @Test
    @DisplayName("Should throw exception when product not found")
    void testGetProductById_NotFound() {
        UUID productId = UUID.randomUUID();
        when(productCacheService.getProduct(productId)).thenReturn(Optional.empty());
        when(productRepository.findById(productId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.getProductById(productId))
                .isInstanceOf(ProductNotFoundException.class)
                .hasMessageContaining("Product not found");

        verify(productCacheService, times(1)).getProduct(productId);
        verify(productRepository, times(1)).findById(productId);
    }

    @Test
    @DisplayName("Should check product availability successfully")
    void testIsProductAvailable_Available() {
        UUID productId = UUID.randomUUID();
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));

        boolean result = productService.isProductAvailable(productId, 10);

        assertThat(result).isTrue();
        verify(productRepository, times(1)).findById(productId);
    }

    @Test
    @DisplayName("Should return false when product stock insufficient")
    void testIsProductAvailable_InsufficientStock() {
        UUID productId = UUID.randomUUID();
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));

        boolean result = productService.isProductAvailable(productId, 100);

        assertThat(result).isFalse();
        verify(productRepository, times(1)).findById(productId);
    }

    @Test
    @DisplayName("Should return false when product not available")
    void testIsProductAvailable_NotAvailable() {
        UUID productId = UUID.randomUUID();
        Product unavailableProduct = Product.builder()
                .id(productId)
                .name("Out of Stock Item")
                .stock(0)
                .available(false)
                .build();

        when(productRepository.findById(productId)).thenReturn(Optional.of(unavailableProduct));

        boolean result = productService.isProductAvailable(productId, 1);

        assertThat(result).isFalse();
        verify(productRepository, times(1)).findById(productId);
    }

    @Test
    @DisplayName("Should handle pagination correctly")
    void testSearchProducts_Pagination() {
        PageRequest pageRequest = PageRequest.of(2, 20);
        Page<Product> productPage = new PageImpl<>(Arrays.asList(product), pageRequest, 100);

        when(productRepository.searchProducts("laptop", pageRequest)).thenReturn(productPage);

        PageResponseDTO<ProductDTO> result = productService.searchProducts("laptop", pageRequest);

        assertThat(result).isNotNull();
        assertThat(result.getPageNumber()).isEqualTo(2);
        assertThat(result.getPageSize()).isEqualTo(20);
        assertThat(result.getTotalElements()).isEqualTo(100);
        assertThat(result.getTotalPages()).isEqualTo(5);

        verify(productRepository, times(1)).searchProducts("laptop", pageRequest);
    }
}