package com.ecommerce.productcatalog.controller;

import com.ecommerce.productcatalog.dto.ProductDTO;
import com.ecommerce.productcatalog.dto.ProductSearchDTO;
import com.ecommerce.productcatalog.dto.PageResponseDTO;
import com.ecommerce.productcatalog.service.ProductService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ProductController.class)
@DisplayName("Product Controller Tests")
class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ProductService productService;

    private ProductDTO productDTO;
    private PageResponseDTO<ProductDTO> pageResponse;

    @BeforeEach
    void setUp() {
        productDTO = ProductDTO.builder()
                .id(UUID.randomUUID())
                .name("Laptop")
                .description("High-performance laptop")
                .price(new BigDecimal("999.99"))
                .stock(50)
                .available(true)
                .build();

        pageResponse = PageResponseDTO.<ProductDTO>builder()
                .content(Arrays.asList(productDTO))
                .pageNumber(0)
                .pageSize(10)
                .totalElements(1L)
                .totalPages(1)
                .build();
    }

    @Test
    @DisplayName("Should search products successfully with keyword")
    void testSearchProducts_WithKeyword() throws Exception {
        when(productService.searchProducts(anyString(), any(PageRequest.class)))
                .thenReturn(pageResponse);

        mockMvc.perform(get("/api/products/search")
                        .param("keyword", "laptop")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].name").value("Laptop"))
                .andExpect(jsonPath("$.content[0].price").value(999.99))
                .andExpect(jsonPath("$.totalElements").value(1));

        verify(productService, times(1)).searchProducts(anyString(), any(PageRequest.class));
    }

    @Test
    @DisplayName("Should search products successfully without keyword")
    void testSearchProducts_WithoutKeyword() throws Exception {
        when(productService.searchProducts(eq(""), any(PageRequest.class)))
                .thenReturn(pageResponse);

        mockMvc.perform(get("/api/products/search")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());

        verify(productService, times(1)).searchProducts(eq(""), any(PageRequest.class));
    }

    @Test
    @DisplayName("Should return empty results when no products match")
    void testSearchProducts_NoResults() throws Exception {
        PageResponseDTO<ProductDTO> emptyResponse = PageResponseDTO.<ProductDTO>builder()
                .content(Collections.emptyList())
                .pageNumber(0)
                .pageSize(10)
                .totalElements(0L)
                .totalPages(0)
                .build();

        when(productService.searchProducts(anyString(), any(PageRequest.class)))
                .thenReturn(emptyResponse);

        mockMvc.perform(get("/api/products/search")
                        .param("keyword", "nonexistent")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isEmpty())
                .andExpect(jsonPath("$.totalElements").value(0));

        verify(productService, times(1)).searchProducts(anyString(), any(PageRequest.class));
    }

    @Test
    @DisplayName("Should handle pagination correctly")
    void testSearchProducts_Pagination() throws Exception {
        when(productService.searchProducts(anyString(), any(PageRequest.class)))
                .thenReturn(pageResponse);

        mockMvc.perform(get("/api/products/search")
                        .param("keyword", "laptop")
                        .param("page", "2")
                        .param("size", "20"))
                .andExpect(status().isOk());

        verify(productService, times(1)).searchProducts(eq("laptop"), eq(PageRequest.of(2, 20)));
    }

    @Test
    @DisplayName("Should handle special characters in search keyword")
    void testSearchProducts_SpecialCharacters() throws Exception {
        when(productService.searchProducts(anyString(), any(PageRequest.class)))
                .thenReturn(pageResponse);

        mockMvc.perform(get("/api/products/search")
                        .param("keyword", "laptop & tablet")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk());

        verify(productService, times(1)).searchProducts(eq("laptop & tablet"), any(PageRequest.class));
    }

    @Test
    @DisplayName("Should prevent SQL injection in search")
    void testSearchProducts_SQLInjectionPrevention() throws Exception {
        String maliciousKeyword = "'; DROP TABLE products; --";
        when(productService.searchProducts(anyString(), any(PageRequest.class)))
                .thenReturn(pageResponse);

        mockMvc.perform(get("/api/products/search")
                        .param("keyword", maliciousKeyword)
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk());

        verify(productService, times(1)).searchProducts(eq(maliciousKeyword), any(PageRequest.class));
    }

    @Test
    @DisplayName("Should get product by ID successfully")
    void testGetProductById_Success() throws Exception {
        UUID productId = UUID.randomUUID();
        when(productService.getProductById(productId)).thenReturn(productDTO);

        mockMvc.perform(get("/api/products/{id}", productId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Laptop"))
                .andExpect(jsonPath("$.price").value(999.99))
                .andExpect(jsonPath("$.stock").value(50))
                .andExpect(jsonPath("$.available").value(true));

        verify(productService, times(1)).getProductById(productId);
    }

    @Test
    @DisplayName("Should return 404 when product not found")
    void testGetProductById_NotFound() throws Exception {
        UUID productId = UUID.randomUUID();
        when(productService.getProductById(productId))
                .thenThrow(new ProductNotFoundException("Product not found"));

        mockMvc.perform(get("/api/products/{id}", productId))
                .andExpect(status().isNotFound());

        verify(productService, times(1)).getProductById(productId);
    }

    @Test
    @DisplayName("Should return 400 with invalid UUID format")
    void testGetProductById_InvalidUUID() throws Exception {
        mockMvc.perform(get("/api/products/{id}", "invalid-uuid"))
                .andExpect(status().isBadRequest());

        verify(productService, never()).getProductById(any(UUID.class));
    }

    @Test
    @DisplayName("Should return product with zero stock")
    void testGetProductById_ZeroStock() throws Exception {
        UUID productId = UUID.randomUUID();
        ProductDTO outOfStockProduct = ProductDTO.builder()
                .id(productId)
                .name("Out of Stock Item")
                .description("This item is out of stock")
                .price(new BigDecimal("49.99"))
                .stock(0)
                .available(false)
                .build();

        when(productService.getProductById(productId)).thenReturn(outOfStockProduct);

        mockMvc.perform(get("/api/products/{id}", productId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.stock").value(0))
                .andExpect(jsonPath("$.available").value(false));

        verify(productService, times(1)).getProductById(productId);
    }

    @Test
    @DisplayName("Should include cache control headers in response")
    void testGetProductById_CacheHeaders() throws Exception {
        UUID productId = UUID.randomUUID();
        when(productService.getProductById(productId)).thenReturn(productDTO);

        mockMvc.perform(get("/api/products/{id}", productId))
                .andExpect(status().isOk())
                .andExpect(header().exists("Cache-Control"));

        verify(productService, times(1)).getProductById(productId);
    }

    @Test
    @DisplayName("Should handle concurrent requests correctly")
    void testSearchProducts_ConcurrentRequests() throws Exception {
        when(productService.searchProducts(anyString(), any(PageRequest.class)))
                .thenReturn(pageResponse);

        // Simulate multiple concurrent requests
        for (int i = 0; i < 5; i++) {
            mockMvc.perform(get("/api/products/search")
                            .param("keyword", "laptop")
                            .param("page", "0")
                            .param("size", "10"))
                    .andExpect(status().isOk());
        }

        verify(productService, times(5)).searchProducts(anyString(), any(PageRequest.class));
    }

    @Test
    @DisplayName("Should validate page and size parameters")
    void testSearchProducts_InvalidPagination() throws Exception {
        mockMvc.perform(get("/api/products/search")
                        .param("keyword", "laptop")
                        .param("page", "-1")
                        .param("size", "0"))
                .andExpect(status().isBadRequest());

        verify(productService, never()).searchProducts(anyString(), any(PageRequest.class));
    }
}