package com.ecommerce.productcatalog.service;

import com.ecommerce.productcatalog.dto.ProductDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Product Cache Service Tests")
class ProductCacheServiceTest {

    @Mock
    private RedisTemplate<String, ProductDTO> redisTemplate;

    @Mock
    private ValueOperations<String, ProductDTO> valueOperations;

    @InjectMocks
    private ProductCacheService productCacheService;

    private ProductDTO productDTO;
    private UUID productId;

    @BeforeEach
    void setUp() {
        productId = UUID.randomUUID();
        productDTO = ProductDTO.builder()
                .id(productId)
                .name("Laptop")
                .description("High-performance laptop")
                .price(new BigDecimal("999.99"))
                .stock(50)
                .available(true)
                .build();

        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    }

    @Test
    @DisplayName("Should save product to cache successfully")
    void testSaveProduct_Success() {
        doNothing().when(valueOperations).set(anyString(), any(ProductDTO.class), anyLong(), any(TimeUnit.class));

        productCacheService.saveProduct(productDTO);

        verify(valueOperations, times(1)).set(
                eq("product:" + productId),
                eq(productDTO),
                eq(600L),
                eq(TimeUnit.SECONDS)
        );
    }

    @Test
    @DisplayName("Should get product from cache successfully")
    void testGetProduct_CacheHit() {
        when(valueOperations.get(anyString())).thenReturn(productDTO);

        Optional<ProductDTO> result = productCacheService.getProduct(productId);

        assertThat(result).isPresent();
        assertThat(result.get().getName()).isEqualTo("Laptop");
        assertThat(result.get().getPrice()).isEqualByComparingTo(new BigDecimal("999.99"));

        verify(valueOperations, times(1)).get("product:" + productId);
    }

    @Test
    @DisplayName("Should return empty when product not in cache")
    void testGetProduct_CacheMiss() {
        when(valueOperations.get(anyString())).thenReturn(null);

        Optional<ProductDTO> result = productCacheService.getProduct(productId);

        assertThat(result).isEmpty();
        verify(valueOperations, times(1)).get("product:" + productId);
    }

    @Test
    @DisplayName("Should evict product from cache successfully")
    void testEvictProduct_Success() {
        when(redisTemplate.delete(anyString())).thenReturn(true);

        productCacheService.evictProduct(productId);

        verify(redisTemplate, times(1)).delete("product:" + productId);
    }

    @Test
    @DisplayName("Should handle cache eviction failure gracefully")
    void testEvictProduct_Failure() {
        when(redisTemplate.delete(anyString())).thenReturn(false);

        assertThatCode(() -> productCacheService.evictProduct(productId))
                .doesNotThrowAnyException();

        verify(redisTemplate, times(1)).delete("product:" + productId);
    }

    @Test
    @DisplayName("Should clear all products from cache")
    void testClearAllProducts() {
        when(redisTemplate.keys("product:*")).thenReturn(Set.of("product:1", "product:2"));
        when(redisTemplate.delete(anyCollection())).thenReturn(2L);

        productCacheService.clearAllProducts();

        verify(redisTemplate, times(1)).keys("product:*");
        verify(redisTemplate, times(1)).delete(anyCollection());
    }

    @Test
    @DisplayName("Should handle Redis connection failure")
    void testGetProduct_RedisConnectionFailure() {
        when(valueOperations.get(anyString())).thenThrow(new RuntimeException("Redis connection failed"));

        Optional<ProductDTO> result = productCacheService.getProduct(productId);

        assertThat(result).isEmpty();
        verify(valueOperations, times(1)).get("product:" + productId);
    }

    @Test
    @DisplayName("Should update product in cache")
    void testUpdateProduct() {
        ProductDTO updatedProduct = ProductDTO.builder()
                .id(productId)
                .name("Updated Laptop")
                .price(new BigDecimal("1099.99"))
                .stock(45)
                .available(true)
                .build();

        doNothing().when(valueOperations).set(anyString(), any(ProductDTO.class), anyLong(), any(TimeUnit.class));

        productCacheService.saveProduct(updatedProduct);

        verify(valueOperations, times(1)).set(
                eq("product:" + productId),
                eq(updatedProduct),
                eq(600L),
                eq(TimeUnit.SECONDS)
        );
    }

    @Test
    @DisplayName("Should check if product exists in cache")
    void testHasProduct() {
        when(redisTemplate.hasKey(anyString())).thenReturn(true);

        boolean result = productCacheService.hasProduct(productId);

        assertThat(result).isTrue();
        verify(redisTemplate, times(1)).hasKey("product:" + productId);
    }

    @Test
    @DisplayName("Should get cache TTL for product")
    void testGetProductTTL() {
        when(redisTemplate.getExpire(anyString(), any(TimeUnit.class))).thenReturn(300L);

        Long ttl = productCacheService.getProductTTL(productId);

        assertThat(ttl).isEqualTo(300L);
        verify(redisTemplate, times(1)).getExpire("product:" + productId, TimeUnit.SECONDS);
    }

    @Test
    @DisplayName("Should save multiple products to cache")
    void testSaveMultipleProducts() {
        ProductDTO product1 = ProductDTO.builder().id(UUID.randomUUID()).name("Product 1").build();
        ProductDTO product2 = ProductDTO.builder().id(UUID.randomUUID()).name("Product 2").build();

        doNothing().when(valueOperations).set(anyString(), any(ProductDTO.class), anyLong(), any(TimeUnit.class));

        productCacheService.saveProduct(product1);
        productCacheService.saveProduct(product2);

        verify(valueOperations, times(2)).set(anyString(), any(ProductDTO.class), anyLong(), any(TimeUnit.class));
    }

    @Test
    @DisplayName("Should handle serialization errors gracefully")
    void testSaveProduct_SerializationError() {
        doThrow(new RuntimeException("Serialization failed"))
                .when(valueOperations).set(anyString(), any(ProductDTO.class), anyLong(), any(TimeUnit.class));

        assertThatCode(() -> productCacheService.saveProduct(productDTO))
                .doesNotThrowAnyException();

        verify(valueOperations, times(1)).set(anyString(), any(ProductDTO.class), anyLong(), any(TimeUnit.class));
    }
}