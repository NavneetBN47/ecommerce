package com.ecommerce.productcatalog.controller;

import com.ecommerce.productcatalog.dto.ProductRequest;
import com.ecommerce.productcatalog.dto.ProductResponse;
import com.ecommerce.productcatalog.service.ProductService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ProductService productService;

    @Autowired
    private ObjectMapper objectMapper;

    private ProductRequest productRequest;
    private ProductResponse productResponse;
    private List<ProductResponse> productList;

    @BeforeEach
    void setUp() {
        productRequest = new ProductRequest();
        productRequest.setName("Test Product");
        productRequest.setDescription("Test Description");
        productRequest.setPrice(new BigDecimal("99.99"));
        productRequest.setCategory("Electronics");
        productRequest.setStockQuantity(100);

        productResponse = new ProductResponse();
        productResponse.setId(1L);
        productResponse.setName("Test Product");
        productResponse.setDescription("Test Description");
        productResponse.setPrice(new BigDecimal("99.99"));
        productResponse.setCategory("Electronics");
        productResponse.setStockQuantity(100);

        productList = Arrays.asList(productResponse);
    }

    @Test
    @DisplayName("POST /api/products - Create Product Success")
    @WithMockUser(roles = "ADMIN")
    void testCreateProduct_Success() throws Exception {
        when(productService.createProduct(any(ProductRequest.class))).thenReturn(productResponse);

        mockMvc.perform(post("/api/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(productRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Test Product"))
                .andExpect(jsonPath("$.price").value(99.99));
    }

    @Test
    @DisplayName("POST /api/products - Invalid Price")
    @WithMockUser(roles = "ADMIN")
    void testCreateProduct_InvalidPrice() throws Exception {
        productRequest.setPrice(new BigDecimal("-10.00"));

        mockMvc.perform(post("/api/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(productRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/products - Missing Required Fields")
    @WithMockUser(roles = "ADMIN")
    void testCreateProduct_MissingFields() throws Exception {
        productRequest.setName(null);

        mockMvc.perform(post("/api/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(productRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /api/products - Get All Products Success")
    void testGetAllProducts_Success() throws Exception {
        Page<ProductResponse> page = new PageImpl<>(productList, PageRequest.of(0, 10), 1);
        when(productService.getAllProducts(any(PageRequest.class))).thenReturn(page);

        mockMvc.perform(get("/api/products")
                .param("page", "0")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(1))
                .andExpect(jsonPath("$.content[0].name").value("Test Product"));
    }

    @Test
    @DisplayName("GET /api/products/{id} - Get Product By ID Success")
    void testGetProductById_Success() throws Exception {
        when(productService.getProductById(anyLong())).thenReturn(productResponse);

        mockMvc.perform(get("/api/products/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Test Product"));
    }

    @Test
    @DisplayName("GET /api/products/{id} - Product Not Found")
    void testGetProductById_NotFound() throws Exception {
        when(productService.getProductById(anyLong()))
                .thenThrow(new RuntimeException("Product not found"));

        mockMvc.perform(get("/api/products/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("PUT /api/products/{id} - Update Product Success")
    @WithMockUser(roles = "ADMIN")
    void testUpdateProduct_Success() throws Exception {
        when(productService.updateProduct(anyLong(), any(ProductRequest.class))).thenReturn(productResponse);

        mockMvc.perform(put("/api/products/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(productRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    @DisplayName("DELETE /api/products/{id} - Delete Product Success")
    @WithMockUser(roles = "ADMIN")
    void testDeleteProduct_Success() throws Exception {
        mockMvc.perform(delete("/api/products/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("GET /api/products/search - Search Products Success")
    void testSearchProducts_Success() throws Exception {
        when(productService.searchProducts(anyString())).thenReturn(productList);

        mockMvc.perform(get("/api/products/search")
                .param("keyword", "Test"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Test Product"));
    }

    @Test
    @DisplayName("GET /api/products/category/{category} - Filter By Category Success")
    void testFilterByCategory_Success() throws Exception {
        when(productService.getProductsByCategory(anyString())).thenReturn(productList);

        mockMvc.perform(get("/api/products/category/Electronics"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].category").value("Electronics"));
    }
}
