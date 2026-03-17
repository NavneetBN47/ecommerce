package com.ecommerce.productcatalog.presentation.controller;

import com.ecommerce.productcatalog.application.dto.*;
import com.ecommerce.productcatalog.application.service.ProductService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ProductController.class)
@ExtendWith(MockitoExtension.class)
@DisplayName("Product Controller Tests")
class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ProductService productService;

    private ProductResponse productResponse;
    private ProductSearchResponse searchResponse;
    private ProductAvailabilityResponse availabilityResponse;
    private List<ProductSummary> productList;

    @BeforeEach
    void setUp() {
        productResponse = ProductResponse.builder()
                .productId(1L)
                .name("Test Product")
                .description("Test product description")
                .category("Electronics")
                .price(new BigDecimal("99.99"))
                .stockQuantity(100)
                .imageUrl("https://example.com/image.jpg")
                .isAvailable(true)
                .build();

        ProductSummary product1 = ProductSummary.builder()
                .productId(1L)
                .name("Product 1")
                .category("Electronics")
                .price(new BigDecimal("99.99"))
                .imageUrl("https://example.com/image1.jpg")
                .isAvailable(true)
                .build();

        ProductSummary product2 = ProductSummary.builder()
                .productId(2L)
                .name("Product 2")
                .category("Electronics")
                .price(new BigDecimal("149.99"))
                .imageUrl("https://example.com/image2.jpg")
                .isAvailable(true)
                .build();

        productList = Arrays.asList(product1, product2);

        PaginationMetadata pagination = PaginationMetadata.builder()
                .currentPage(0)
                .pageSize(20)
                .totalElements(2L)
                .totalPages(1)
                .build();

        searchResponse = ProductSearchResponse.builder()
                .products(productList)
                .pagination(pagination)
                .build();

        availabilityResponse = ProductAvailabilityResponse.builder()
                .productId(1L)
                .isAvailable(true)
                .availableQuantity(100)
                .requestedQuantity(5)
                .message("Product is available")
                .build();
    }

    @Test
    @DisplayName("GET /api/v1/products/search - Valid Search - Success")
    void testSearchProducts_ValidParameters_ReturnsProducts() throws Exception {
        when(productService.searchProducts(anyString(), anyString(), any(), any(), anyInt(), anyInt(), anyString(), anyString()))
                .thenReturn(searchResponse);

        mockMvc.perform(get("/api/v1/products/search")
                        .param("keyword", "test")
                        .param("category", "Electronics")
                        .param("minPrice", "50.0")
                        .param("maxPrice", "200.0")
                        .param("page", "0")
                        .param("size", "20")
                        .param("sortBy", "name")
                        .param("sortDirection", "asc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.products").isArray())
                .andExpect(jsonPath("$.products.length()").value(2))
                .andExpect(jsonPath("$.pagination.currentPage").value(0))
                .andExpect(jsonPath("$.pagination.totalElements").value(2));

        verify(productService, times(1)).searchProducts(anyString(), anyString(), any(), any(), anyInt(), anyInt(), anyString(), anyString());
    }

    @Test
    @DisplayName("GET /api/v1/products/search - No Parameters - Returns All Products")
    void testSearchProducts_NoParameters_ReturnsAllProducts() throws Exception {
        when(productService.searchProducts(isNull(), isNull(), isNull(), isNull(), eq(0), eq(20), eq("name"), eq("asc")))
                .thenReturn(searchResponse);

        mockMvc.perform(get("/api/v1/products/search"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.products").isArray())
                .andExpect(jsonPath("$.products.length()").value(2));

        verify(productService, times(1)).searchProducts(isNull(), isNull(), isNull(), isNull(), eq(0), eq(20), eq("name"), eq("asc"));
    }

    @Test
    @DisplayName("GET /api/v1/products/search - Invalid Price Range - Returns Bad Request")
    void testSearchProducts_InvalidPriceRange_ReturnsBadRequest() throws Exception {
        mockMvc.perform(get("/api/v1/products/search")
                        .param("minPrice", "200.0")
                        .param("maxPrice", "50.0"))
                .andExpect(status().isBadRequest());

        verify(productService, never()).searchProducts(anyString(), anyString(), any(), any(), anyInt(), anyInt(), anyString(), anyString());
    }

    @Test
    @DisplayName("GET /api/v1/products/search - Negative Page Number - Returns Bad Request")
    void testSearchProducts_NegativePageNumber_ReturnsBadRequest() throws Exception {
        mockMvc.perform(get("/api/v1/products/search")
                        .param("page", "-1"))
                .andExpect(status().isBadRequest());

        verify(productService, never()).searchProducts(anyString(), anyString(), any(), any(), anyInt(), anyInt(), anyString(), anyString());
    }

    @Test
    @DisplayName("GET /api/v1/products/search - Invalid Sort Direction - Returns Bad Request")
    void testSearchProducts_InvalidSortDirection_ReturnsBadRequest() throws Exception {
        mockMvc.perform(get("/api/v1/products/search")
                        .param("sortDirection", "invalid"))
                .andExpect(status().isBadRequest());

        verify(productService, never()).searchProducts(anyString(), anyString(), any(), any(), anyInt(), anyInt(), anyString(), anyString());
    }

    @Test
    @DisplayName("GET /api/v1/products/{productId} - Valid Product ID - Success")
    void testGetProductById_ValidId_ReturnsProduct() throws Exception {
        when(productService.getProductById(1L)).thenReturn(productResponse);

        mockMvc.perform(get("/api/v1/products/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.productId").value(1))
                .andExpect(jsonPath("$.name").value("Test Product"))
                .andExpect(jsonPath("$.category").value("Electronics"))
                .andExpect(jsonPath("$.price").value(99.99))
                .andExpect(jsonPath("$.stockQuantity").value(100))
                .andExpect(jsonPath("$.isAvailable").value(true));

        verify(productService, times(1)).getProductById(1L);
    }

    @Test
    @DisplayName("GET /api/v1/products/{productId} - Invalid Product ID - Returns Not Found")
    void testGetProductById_InvalidId_ReturnsNotFound() throws Exception {
        when(productService.getProductById(999L))
                .thenThrow(new ProductNotFoundException("Product not found"));

        mockMvc.perform(get("/api/v1/products/999"))
                .andExpect(status().isNotFound());

        verify(productService, times(1)).getProductById(999L);
    }

    @Test
    @DisplayName("GET /api/v1/products/{productId} - Zero Product ID - Returns Bad Request")
    void testGetProductById_ZeroId_ReturnsBadRequest() throws Exception {
        mockMvc.perform(get("/api/v1/products/0"))
                .andExpect(status().isBadRequest());

        verify(productService, never()).getProductById(anyLong());
    }

    @Test
    @DisplayName("GET /api/v1/products/{productId} - Negative Product ID - Returns Bad Request")
    void testGetProductById_NegativeId_ReturnsBadRequest() throws Exception {
        mockMvc.perform(get("/api/v1/products/-1"))
                .andExpect(status().isBadRequest());

        verify(productService, never()).getProductById(anyLong());
    }

    @Test
    @DisplayName("GET /api/v1/products/category/{category} - Valid Category - Success")
    void testGetProductsByCategory_ValidCategory_ReturnsProducts() throws Exception {
        when(productService.getProductsByCategory("Electronics", 0, 20))
                .thenReturn(searchResponse);

        mockMvc.perform(get("/api/v1/products/category/Electronics")
                        .param("page", "0")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.products").isArray())
                .andExpect(jsonPath("$.products.length()").value(2));

        verify(productService, times(1)).getProductsByCategory("Electronics", 0, 20);
    }

    @Test
    @DisplayName("GET /api/v1/products/category/{category} - Empty Category - Returns Bad Request")
    void testGetProductsByCategory_EmptyCategory_ReturnsBadRequest() throws Exception {
        mockMvc.perform(get("/api/v1/products/category/ "))
                .andExpect(status().isBadRequest());

        verify(productService, never()).getProductsByCategory(anyString(), anyInt(), anyInt());
    }

    @Test
    @DisplayName("GET /api/v1/products/category/{category} - Invalid Page Size - Returns Bad Request")
    void testGetProductsByCategory_InvalidPageSize_ReturnsBadRequest() throws Exception {
        mockMvc.perform(get("/api/v1/products/category/Electronics")
                        .param("size", "0"))
                .andExpect(status().isBadRequest());

        verify(productService, never()).getProductsByCategory(anyString(), anyInt(), anyInt());
    }

    @Test
    @DisplayName("GET /api/v1/products/availability/{productId} - Available Product - Success")
    void testCheckProductAvailability_AvailableProduct_ReturnsAvailable() throws Exception {
        when(productService.checkAvailability(1L, 5))
                .thenReturn(availabilityResponse);

        mockMvc.perform(get("/api/v1/products/availability/1")
                        .param("quantity", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.productId").value(1))
                .andExpect(jsonPath("$.isAvailable").value(true))
                .andExpect(jsonPath("$.availableQuantity").value(100))
                .andExpect(jsonPath("$.requestedQuantity").value(5));

        verify(productService, times(1)).checkAvailability(1L, 5);
    }

    @Test
    @DisplayName("GET /api/v1/products/availability/{productId} - Insufficient Stock - Returns Unavailable")
    void testCheckProductAvailability_InsufficientStock_ReturnsUnavailable() throws Exception {
        ProductAvailabilityResponse unavailableResponse = ProductAvailabilityResponse.builder()
                .productId(1L)
                .isAvailable(false)
                .availableQuantity(3)
                .requestedQuantity(10)
                .message("Insufficient stock")
                .build();

        when(productService.checkAvailability(1L, 10))
                .thenReturn(unavailableResponse);

        mockMvc.perform(get("/api/v1/products/availability/1")
                        .param("quantity", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isAvailable").value(false))
                .andExpect(jsonPath("$.availableQuantity").value(3));

        verify(productService, times(1)).checkAvailability(1L, 10);
    }

    @Test
    @DisplayName("GET /api/v1/products/availability/{productId} - Zero Quantity - Returns Bad Request")
    void testCheckProductAvailability_ZeroQuantity_ReturnsBadRequest() throws Exception {
        mockMvc.perform(get("/api/v1/products/availability/1")
                        .param("quantity", "0"))
                .andExpect(status().isBadRequest());

        verify(productService, never()).checkAvailability(anyLong(), anyInt());
    }

    @Test
    @DisplayName("GET /api/v1/products/availability/{productId} - Negative Quantity - Returns Bad Request")
    void testCheckProductAvailability_NegativeQuantity_ReturnsBadRequest() throws Exception {
        mockMvc.perform(get("/api/v1/products/availability/1")
                        .param("quantity", "-5"))
                .andExpect(status().isBadRequest());

        verify(productService, never()).checkAvailability(anyLong(), anyInt());
    }

    @Test
    @DisplayName("GET /api/v1/products/availability/{productId} - Product Not Found - Returns Not Found")
    void testCheckProductAvailability_ProductNotFound_ReturnsNotFound() throws Exception {
        when(productService.checkAvailability(999L, 5))
                .thenThrow(new ProductNotFoundException("Product not found"));

        mockMvc.perform(get("/api/v1/products/availability/999")
                        .param("quantity", "5"))
                .andExpect(status().isNotFound());

        verify(productService, times(1)).checkAvailability(999L, 5);
    }
}