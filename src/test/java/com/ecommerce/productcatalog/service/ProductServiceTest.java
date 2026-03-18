package com.ecommerce.productcatalog.service;

import com.ecommerce.productcatalog.dto.ProductRequest;
import com.ecommerce.productcatalog.dto.ProductResponse;
import com.ecommerce.productcatalog.entity.Product;
import com.ecommerce.productcatalog.repository.ProductRepository;
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

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private ProductCacheService productCacheService;

    @InjectMocks
    private ProductService productService;

    private Product product;
    private ProductRequest productRequest;

    @BeforeEach
    void setUp() {
        product = new Product();
        product.setId(1L);
        product.setName("Test Product");
        product.setDescription("Test Description");
        product.setPrice(new BigDecimal("99.99"));
        product.setCategory("Electronics");
        product.setStockQuantity(100);

        productRequest = new ProductRequest();
        productRequest.setName("Test Product");
        productRequest.setDescription("Test Description");
        productRequest.setPrice(new BigDecimal("99.99"));
        productRequest.setCategory("Electronics");
        productRequest.setStockQuantity(100);
    }

    @Test
    @DisplayName("Create Product - Success")
    void testCreateProduct_Success() {
        when(productRepository.save(any(Product.class))).thenReturn(product);

        ProductResponse response = productService.createProduct(productRequest);

        assertNotNull(response);
        assertEquals("Test Product", response.getName());
        assertEquals(new BigDecimal("99.99"), response.getPrice());
        verify(productRepository, times(1)).save(any(Product.class));
    }

    @Test
    @DisplayName("Get All Products - Success")
    void testGetAllProducts_Success() {
        Page<Product> page = new PageImpl<>(Arrays.asList(product));
        when(productRepository.findAll(any(PageRequest.class))).thenReturn(page);

        Page<ProductResponse> result = productService.getAllProducts(PageRequest.of(0, 10));

        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertEquals("Test Product", result.getContent().get(0).getName());
    }

    @Test
    @DisplayName("Get Product By ID - Success")
    void testGetProductById_Success() {
        when(productRepository.findById(anyLong())).thenReturn(Optional.of(product));

        ProductResponse response = productService.getProductById(1L);

        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals("Test Product", response.getName());
    }

    @Test
    @DisplayName("Get Product By ID - Not Found")
    void testGetProductById_NotFound() {
        when(productRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> productService.getProductById(999L));
    }

    @Test
    @DisplayName("Update Product - Success")
    void testUpdateProduct_Success() {
        when(productRepository.findById(anyLong())).thenReturn(Optional.of(product));
        when(productRepository.save(any(Product.class))).thenReturn(product);

        ProductResponse response = productService.updateProduct(1L, productRequest);

        assertNotNull(response);
        verify(productRepository, times(1)).save(any(Product.class));
    }

    @Test
    @DisplayName("Delete Product - Success")
    void testDeleteProduct_Success() {
        when(productRepository.existsById(anyLong())).thenReturn(true);
        doNothing().when(productRepository).deleteById(anyLong());

        productService.deleteProduct(1L);

        verify(productRepository, times(1)).deleteById(1L);
    }

    @Test
    @DisplayName("Search Products - Success")
    void testSearchProducts_Success() {
        when(productRepository.findByNameContainingIgnoreCase(anyString()))
                .thenReturn(Arrays.asList(product));

        List<ProductResponse> results = productService.searchProducts("Test");

        assertNotNull(results);
        assertEquals(1, results.size());
        assertEquals("Test Product", results.get(0).getName());
    }

    @Test
    @DisplayName("Get Products By Category - Success")
    void testGetProductsByCategory_Success() {
        when(productRepository.findByCategory(anyString())).thenReturn(Arrays.asList(product));

        List<ProductResponse> results = productService.getProductsByCategory("Electronics");

        assertNotNull(results);
        assertEquals(1, results.size());
        assertEquals("Electronics", results.get(0).getCategory());
    }
}
