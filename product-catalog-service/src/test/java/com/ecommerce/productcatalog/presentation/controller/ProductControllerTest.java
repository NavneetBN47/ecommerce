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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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

@ExtendWith(MockitoExtension.class)
@WebMvcTest(ProductController.class)
@DisplayName("Product Controller Tests")
class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ProductService productService;

    private ProductResponseDTO productResponseDTO;
    private ProductCreateDTO productCreateDTO;
    private String productId;

    @BeforeEach
    void setUp() {
        productId = UUID.randomUUID().toString();

        productResponseDTO = new ProductResponseDTO();
        productResponseDTO.setProductId(productId);
        productResponseDTO.setName("Test Product");
        productResponseDTO.setDescription("Test Description");
        productResponseDTO.setPrice(new BigDecimal("99.99"));
        productResponseDTO.setCategory("Electronics");
        productResponseDTO.setStockQuantity(100);
        productResponseDTO.setImageUrl("https://example.com/image.jpg");
        productResponseDTO.setCreatedAt(LocalDateTime.now());
        productResponseDTO.setUpdatedAt(LocalDateTime.now());

        productCreateDTO = new ProductCreateDTO();
        productCreateDTO.setName("Test Product");
        productCreateDTO.setDescription("Test Description");
        productCreateDTO.setPrice(new BigDecimal("99.99"));
        productCreateDTO.setCategory("Electronics");
        productCreateDTO.setStockQuantity(100);
        productCreateDTO.setImageUrl("https://example.com/image.jpg");
    }

    @Test
    @DisplayName("GET /api/v1/products - Get All Products - Success")
    void testGetAllProducts_ReturnsOk() throws Exception {
        List<ProductResponseDTO> products = Arrays.asList(productResponseDTO);
        Page<ProductResponseDTO> page = new PageImpl<>(products, PageRequest.of(0, 10), 1);

        when(productService.getAllProducts(any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/api/v1/products")
                .param("page", "0")
                .param("size", "10")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].productId").value(productId))
                .andExpect(jsonPath("$.content[0].name").value("Test Product"));

        verify(productService, times(1)).getAllProducts(any(Pageable.class));
    }

    @Test
    @DisplayName("GET /api/v1/products/{productId} - Valid Product ID - Success")
    void testGetProductById_ValidId_ReturnsOk() throws Exception {
        when(productService.getProductById(productId)).thenReturn(productResponseDTO);

        mockMvc.perform(get("/api/v1/products/{productId}", productId)
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.productId").value(productId))
                .andExpect(jsonPath("$.name").value("Test Product"))
                .andExpect(jsonPath("$.price").value(99.99));

        verify(productService, times(1)).getProductById(productId);
    }

    @Test
    @DisplayName("GET /api/v1/products/{productId} - Product Not Found - Not Found")
    void testGetProductById_NotFound_ReturnsNotFound() throws Exception {
        when(productService.getProductById(productId))
                .thenThrow(new RuntimeException("Product not found"));

        mockMvc.perform(get("/api/v1/products/{productId}", productId)
                .with(csrf()))
                .andExpect(status().isInternalServerError());

        verify(productService, times(1)).getProductById(productId);
    }

    @Test
    @DisplayName("GET /api/v1/products/search - Search with Keyword - Success")
    void testSearchProducts_WithKeyword_ReturnsOk() throws Exception {
        List<ProductResponseDTO> products = Arrays.asList(productResponseDTO);
        Page<ProductResponseDTO> page = new PageImpl<>(products, PageRequest.of(0, 10), 1);

        when(productService.searchProducts(eq("laptop"), isNull(), isNull(), isNull(), any(Pageable.class)))
                .thenReturn(page);

        mockMvc.perform(get("/api/v1/products/search")
                .param("keyword", "laptop")
                .param("page", "0")
                .param("size", "10")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].productId").value(productId));

        verify(productService, times(1)).searchProducts(eq("laptop"), isNull(), isNull(), isNull(), any(Pageable.class));
    }

    @Test
    @DisplayName("GET /api/v1/products/search - Search with Price Range - Success")
    void testSearchProducts_WithPriceRange_ReturnsOk() throws Exception {
        List<ProductResponseDTO> products = Arrays.asList(productResponseDTO);
        Page<ProductResponseDTO> page = new PageImpl<>(products, PageRequest.of(0, 10), 1);

        when(productService.searchProducts(isNull(), isNull(), any(BigDecimal.class), any(BigDecimal.class), any(Pageable.class)))
                .thenReturn(page);

        mockMvc.perform(get("/api/v1/products/search")
                .param("minPrice", "50.00")
                .param("maxPrice", "150.00")
                .param("page", "0")
                .param("size", "10")
                .with(csrf()))
                .andExpect(status().isOk());

        verify(productService, times(1)).searchProducts(isNull(), isNull(), any(BigDecimal.class), any(BigDecimal.class), any(Pageable.class));
    }

    @Test
    @DisplayName("GET /api/v1/products/category/{category} - Valid Category - Success")
    void testGetProductsByCategory_ValidCategory_ReturnsOk() throws Exception {
        List<ProductResponseDTO> products = Arrays.asList(productResponseDTO);
        Page<ProductResponseDTO> page = new PageImpl<>(products, PageRequest.of(0, 10), 1);

        when(productService.getProductsByCategory(eq("Electronics"), any(Pageable.class)))
                .thenReturn(page);

        mockMvc.perform(get("/api/v1/products/category/{category}", "Electronics")
                .param("page", "0")
                .param("size", "10")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].category").value("Electronics"));

        verify(productService, times(1)).getProductsByCategory(eq("Electronics"), any(Pageable.class));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("POST /api/v1/products - Valid Product - Created")
    void testCreateProduct_ValidInput_ReturnsCreated() throws Exception {
        when(productService.createProduct(any(ProductCreateDTO.class))).thenReturn(productResponseDTO);

        mockMvc.perform(post("/api/v1/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(productCreateDTO))
                .with(csrf()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.productId").value(productId))
                .andExpect(jsonPath("$.name").value("Test Product"));

        verify(productService, times(1)).createProduct(any(ProductCreateDTO.class));
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("POST /api/v1/products - User Role - Forbidden")
    void testCreateProduct_UserRole_ReturnsForbidden() throws Exception {
        mockMvc.perform(post("/api/v1/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(productCreateDTO))
                .with(csrf()))
                .andExpect(status().isForbidden());

        verify(productService, never()).createProduct(any(ProductCreateDTO.class));
    }

    @Test
    @DisplayName("POST /api/v1/products - No Authentication - Unauthorized")
    void testCreateProduct_NoAuth_ReturnsUnauthorized() throws Exception {
        mockMvc.perform(post("/api/v1/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(productCreateDTO))
                .with(csrf()))
                .andExpect(status().isUnauthorized());

        verify(productService, never()).createProduct(any(ProductCreateDTO.class));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("POST /api/v1/products - Invalid Price - Bad Request")
    void testCreateProduct_InvalidPrice_ReturnsBadRequest() throws Exception {
        productCreateDTO.setPrice(new BigDecimal("-10.00"));

        mockMvc.perform(post("/api/v1/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(productCreateDTO))
                .with(csrf()))
                .andExpect(status().isBadRequest());

        verify(productService, never()).createProduct(any(ProductCreateDTO.class));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("POST /api/v1/products - Missing Required Fields - Bad Request")
    void testCreateProduct_MissingFields_ReturnsBadRequest() throws Exception {
        ProductCreateDTO incompleteDTO = new ProductCreateDTO();
        incompleteDTO.setName("Test Product");

        mockMvc.perform(post("/api/v1/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(incompleteDTO))
                .with(csrf()))
                .andExpect(status().isBadRequest());

        verify(productService, never()).createProduct(any(ProductCreateDTO.class));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("PUT /api/v1/products/{productId} - Valid Update - Success")
    void testUpdateProduct_ValidUpdate_ReturnsOk() throws Exception {
        ProductUpdateDTO updateDTO = new ProductUpdateDTO();
        updateDTO.setName("Updated Product");
        updateDTO.setPrice(new BigDecimal("149.99"));

        ProductResponseDTO updatedResponse = new ProductResponseDTO();
        updatedResponse.setProductId(productId);
        updatedResponse.setName("Updated Product");
        updatedResponse.setPrice(new BigDecimal("149.99"));

        when(productService.updateProduct(eq(productId), any(ProductUpdateDTO.class)))
                .thenReturn(updatedResponse);

        mockMvc.perform(put("/api/v1/products/{productId}", productId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateDTO))
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Product"))
                .andExpect(jsonPath("$.price").value(149.99));

        verify(productService, times(1)).updateProduct(eq(productId), any(ProductUpdateDTO.class));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("DELETE /api/v1/products/{productId} - Valid Product - No Content")
    void testDeleteProduct_ValidProduct_ReturnsNoContent() throws Exception {
        doNothing().when(productService).deleteProduct(productId);

        mockMvc.perform(delete("/api/v1/products/{productId}", productId)
                .with(csrf()))
                .andExpect(status().isNoContent());

        verify(productService, times(1)).deleteProduct(productId);
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("DELETE /api/v1/products/{productId} - User Role - Forbidden")
    void testDeleteProduct_UserRole_ReturnsForbidden() throws Exception {
        mockMvc.perform(delete("/api/v1/products/{productId}", productId)
                .with(csrf()))
                .andExpect(status().isForbidden());

        verify(productService, never()).deleteProduct(anyString());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("PATCH /api/v1/products/{productId}/stock - Valid Stock Update - Success")
    void testUpdateProductStock_ValidUpdate_ReturnsOk() throws Exception {
        StockUpdateDTO stockUpdateDTO = new StockUpdateDTO();
        stockUpdateDTO.setStockQuantity(150);

        ProductResponseDTO updatedResponse = new ProductResponseDTO();
        updatedResponse.setProductId(productId);
        updatedResponse.setStockQuantity(150);

        when(productService.updateStock(eq(productId), any(StockUpdateDTO.class)))
                .thenReturn(updatedResponse);

        mockMvc.perform(patch("/api/v1/products/{productId}/stock", productId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(stockUpdateDTO))
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.stockQuantity").value(150));

        verify(productService, times(1)).updateStock(eq(productId), any(StockUpdateDTO.class));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("PATCH /api/v1/products/{productId}/stock - Negative Stock - Bad Request")
    void testUpdateProductStock_NegativeStock_ReturnsBadRequest() throws Exception {
        StockUpdateDTO stockUpdateDTO = new StockUpdateDTO();
        stockUpdateDTO.setStockQuantity(-10);

        mockMvc.perform(patch("/api/v1/products/{productId}/stock", productId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(stockUpdateDTO))
                .with(csrf()))
                .andExpect(status().isBadRequest());

        verify(productService, never()).updateStock(anyString(), any(StockUpdateDTO.class));
    }

    @Test
    @DisplayName("GET /api/v1/products - Empty Result - Success")
    void testGetAllProducts_EmptyResult_ReturnsOk() throws Exception {
        Page<ProductResponseDTO> emptyPage = new PageImpl<>(Arrays.asList(), PageRequest.of(0, 10), 0);

        when(productService.getAllProducts(any(Pageable.class))).thenReturn(emptyPage);

        mockMvc.perform(get("/api/v1/products")
                .param("page", "0")
                .param("size", "10")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isEmpty());

        verify(productService, times(1)).getAllProducts(any(Pageable.class));
    }
}