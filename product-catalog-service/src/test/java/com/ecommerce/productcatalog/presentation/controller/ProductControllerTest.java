package com.ecommerce.productcatalog.presentation.controller;

import com.ecommerce.productcatalog.application.service.ProductService;
import com.ecommerce.productcatalog.presentation.dto.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ProductController.class)
class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ProductService productService;

    private UUID testProductId;
    private ProductCreateDTO validCreateDTO;
    private ProductResponseDTO productResponseDTO;
    private Page<ProductResponseDTO> productPage;

    @BeforeEach
    void setUp() {
        testProductId = UUID.randomUUID();

        validCreateDTO = new ProductCreateDTO();
        validCreateDTO.setName("Test Product");
        validCreateDTO.setDescription("Test Description");
        validCreateDTO.setPrice(new BigDecimal("99.99"));
        validCreateDTO.setCategory("Electronics");
        validCreateDTO.setStockQuantity(100);

        productResponseDTO = new ProductResponseDTO();
        productResponseDTO.setId(testProductId);
        productResponseDTO.setName("Test Product");
        productResponseDTO.setDescription("Test Description");
        productResponseDTO.setPrice(new BigDecimal("99.99"));
        productResponseDTO.setCategory("Electronics");
        productResponseDTO.setStockQuantity(100);
        productResponseDTO.setCreatedAt(LocalDateTime.now());

        List<ProductResponseDTO> products = Arrays.asList(productResponseDTO);
        productPage = new PageImpl<>(products, PageRequest.of(0, 10), 1);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("POST /api/v1/products - Success")
    void testCreateProduct_Success() throws Exception {
        when(productService.createProduct(any(ProductCreateDTO.class)))
                .thenReturn(productResponseDTO);

        mockMvc.perform(post("/api/v1/products")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validCreateDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(testProductId.toString()))
                .andExpect(jsonPath("$.name").value("Test Product"))
                .andExpect(jsonPath("$.price").value(99.99));

        verify(productService, times(1)).createProduct(any(ProductCreateDTO.class));
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("POST /api/v1/products - Forbidden for Non-Admin")
    void testCreateProduct_Forbidden() throws Exception {
        mockMvc.perform(post("/api/v1/products")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validCreateDTO)))
                .andExpect(status().isForbidden());

        verify(productService, never()).createProduct(any(ProductCreateDTO.class));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("POST /api/v1/products - Invalid Price")
    void testCreateProduct_InvalidPrice() throws Exception {
        ProductCreateDTO invalidDTO = new ProductCreateDTO();
        invalidDTO.setName("Test Product");
        invalidDTO.setDescription("Test Description");
        invalidDTO.setPrice(new BigDecimal("-10.00"));
        invalidDTO.setCategory("Electronics");
        invalidDTO.setStockQuantity(100);

        mockMvc.perform(post("/api/v1/products")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidDTO)))
                .andExpect(status().isBadRequest());

        verify(productService, never()).createProduct(any(ProductCreateDTO.class));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("POST /api/v1/products - Negative Stock Quantity")
    void testCreateProduct_NegativeStock() throws Exception {
        ProductCreateDTO invalidDTO = new ProductCreateDTO();
        invalidDTO.setName("Test Product");
        invalidDTO.setDescription("Test Description");
        invalidDTO.setPrice(new BigDecimal("99.99"));
        invalidDTO.setCategory("Electronics");
        invalidDTO.setStockQuantity(-5);

        mockMvc.perform(post("/api/v1/products")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidDTO)))
                .andExpect(status().isBadRequest());

        verify(productService, never()).createProduct(any(ProductCreateDTO.class));
    }

    @Test
    @DisplayName("GET /api/v1/products/{productId} - Success")
    void testGetProductById_Success() throws Exception {
        when(productService.getProductById(testProductId)).thenReturn(productResponseDTO);

        mockMvc.perform(get("/api/v1/products/{productId}", testProductId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(testProductId.toString()))
                .andExpect(jsonPath("$.name").value("Test Product"));

        verify(productService, times(1)).getProductById(testProductId);
    }

    @Test
    @DisplayName("GET /api/v1/products/{productId} - Not Found")
    void testGetProductById_NotFound() throws Exception {
        when(productService.getProductById(testProductId))
                .thenThrow(new RuntimeException("Product not found"));

        mockMvc.perform(get("/api/v1/products/{productId}", testProductId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

        verify(productService, times(1)).getProductById(testProductId);
    }

    @Test
    @DisplayName("GET /api/v1/products - Success")
    void testGetAllProducts_Success() throws Exception {
        when(productService.getAllProducts(any(), isNull(), isNull(), isNull()))
                .thenReturn(productPage);

        mockMvc.perform(get("/api/v1/products")
                .param("page", "0")
                .param("size", "10")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(testProductId.toString()))
                .andExpect(jsonPath("$.totalElements").value(1));

        verify(productService, times(1)).getAllProducts(any(), isNull(), isNull(), isNull());
    }

    @Test
    @DisplayName("GET /api/v1/products - With Filters")
    void testGetAllProducts_WithFilters() throws Exception {
        when(productService.getAllProducts(any(), eq("Electronics"), 
                eq(new BigDecimal("50.00")), eq(new BigDecimal("150.00"))))
                .thenReturn(productPage);

        mockMvc.perform(get("/api/v1/products")
                .param("page", "0")
                .param("size", "10")
                .param("category", "Electronics")
                .param("minPrice", "50.00")
                .param("maxPrice", "150.00")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        verify(productService, times(1)).getAllProducts(any(), eq("Electronics"), 
                eq(new BigDecimal("50.00")), eq(new BigDecimal("150.00")));
    }

    @Test
    @DisplayName("GET /api/v1/products/search - Success")
    void testSearchProducts_Success() throws Exception {
        when(productService.searchProducts(eq("laptop"), any()))
                .thenReturn(productPage);

        mockMvc.perform(get("/api/v1/products/search")
                .param("query", "laptop")
                .param("page", "0")
                .param("size", "10")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());

        verify(productService, times(1)).searchProducts(eq("laptop"), any());
    }

    @Test
    @DisplayName("GET /api/v1/products/search - Missing Query Parameter")
    void testSearchProducts_MissingQuery() throws Exception {
        mockMvc.perform(get("/api/v1/products/search")
                .param("page", "0")
                .param("size", "10")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        verify(productService, never()).searchProducts(anyString(), any());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("PUT /api/v1/products/{productId} - Success")
    void testUpdateProduct_Success() throws Exception {
        ProductUpdateDTO updateDTO = new ProductUpdateDTO();
        updateDTO.setName("Updated Product");
        updateDTO.setPrice(new BigDecimal("149.99"));

        ProductResponseDTO updatedResponse = new ProductResponseDTO();
        updatedResponse.setId(testProductId);
        updatedResponse.setName("Updated Product");
        updatedResponse.setPrice(new BigDecimal("149.99"));

        when(productService.updateProduct(eq(testProductId), any(ProductUpdateDTO.class)))
                .thenReturn(updatedResponse);

        mockMvc.perform(put("/api/v1/products/{productId}", testProductId)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Product"))
                .andExpect(jsonPath("$.price").value(149.99));

        verify(productService, times(1)).updateProduct(eq(testProductId), any(ProductUpdateDTO.class));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("PATCH /api/v1/products/{productId}/inventory - Success")
    void testUpdateInventory_Success() throws Exception {
        InventoryUpdateDTO inventoryDTO = new InventoryUpdateDTO();
        inventoryDTO.setStockQuantity(200);

        ProductResponseDTO updatedResponse = new ProductResponseDTO();
        updatedResponse.setId(testProductId);
        updatedResponse.setStockQuantity(200);

        when(productService.updateInventory(eq(testProductId), any(InventoryUpdateDTO.class)))
                .thenReturn(updatedResponse);

        mockMvc.perform(patch("/api/v1/products/{productId}/inventory", testProductId)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(inventoryDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.stockQuantity").value(200));

        verify(productService, times(1)).updateInventory(eq(testProductId), any(InventoryUpdateDTO.class));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("DELETE /api/v1/products/{productId} - Success")
    void testDeleteProduct_Success() throws Exception {
        doNothing().when(productService).deleteProduct(testProductId);

        mockMvc.perform(delete("/api/v1/products/{productId}", testProductId)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());

        verify(productService, times(1)).deleteProduct(testProductId);
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("DELETE /api/v1/products/{productId} - Forbidden")
    void testDeleteProduct_Forbidden() throws Exception {
        mockMvc.perform(delete("/api/v1/products/{productId}", testProductId)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());

        verify(productService, never()).deleteProduct(any(UUID.class));
    }

    @Test
    @DisplayName("GET /api/v1/products/category/{category} - Success")
    void testGetProductsByCategory_Success() throws Exception {
        when(productService.getProductsByCategory(eq("Electronics"), any()))
                .thenReturn(productPage);

        mockMvc.perform(get("/api/v1/products/category/{category}", "Electronics")
                .param("page", "0")
                .param("size", "10")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());

        verify(productService, times(1)).getProductsByCategory(eq("Electronics"), any());
    }
}