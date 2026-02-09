package com.ecommerce.controller;

import com.ecommerce.dto.ApiResponse;
import com.ecommerce.dto.ProductDTO;
import com.ecommerce.service.ProductService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Test class for ProductController
 * Tests all public endpoints for product management operations
 * Mocks ProductService layer to isolate controller logic
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ProductController Tests")
class test_ProductController {

    @Mock
    private ProductService productService;

    @InjectMocks
    private ProductController productController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(productController).build();
        objectMapper = new ObjectMapper();
    }

    /**
     * Test creating product successfully
     * Verifies that products are created with proper validation
     */
    @Test
    @DisplayName("Should successfully create new product")
    void testCreateProduct_Success() throws Exception {
        // Given
        ProductDTO productDTO = new ProductDTO();
        productDTO.setName("Test Product");
        productDTO.setDescription("Test Description");
        productDTO.setPrice(BigDecimal.valueOf(99.99));
        productDTO.setCategory("Electronics");
        productDTO.setStock(100);
        
        ProductDTO createdProduct = new ProductDTO();
        createdProduct.setId(1L);
        createdProduct.setName("Test Product");
        createdProduct.setDescription("Test Description");
        createdProduct.setPrice(BigDecimal.valueOf(99.99));
        createdProduct.setCategory("Electronics");
        createdProduct.setStock(100);
        
        when(productService.createProduct(any(ProductDTO.class))).thenReturn(createdProduct);

        // When & Then
        mockMvc.perform(post("/api/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(productDTO)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Product created successfully"))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.name").value("Test Product"))
                .andExpect(jsonPath("$.data.price").value(99.99));

        verify(productService).createProduct(any(ProductDTO.class));
    }

    /**
     * Test creating product with invalid data
     * Verifies that validation errors are handled properly
     */
    @Test
    @DisplayName("Should return validation error for invalid product data")
    void testCreateProduct_InvalidData() throws Exception {
        // Given
        ProductDTO invalidProduct = new ProductDTO();
        // Missing required fields

        // When & Then
        mockMvc.perform(post("/api/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidProduct)))
                .andExpect(status().isBadRequest());
    }

    /**
     * Test getting product by ID successfully
     * Verifies that products can be retrieved by ID
     */
    @Test
    @DisplayName("Should successfully get product by ID")
    void testGetProductById_Success() throws Exception {
        // Given
        Long productId = 1L;
        ProductDTO productDTO = new ProductDTO();
        productDTO.setId(productId);
        productDTO.setName("Test Product");
        productDTO.setPrice(BigDecimal.valueOf(99.99));
        productDTO.setCategory("Electronics");
        
        when(productService.getProductById(productId)).thenReturn(productDTO);

        // When & Then
        mockMvc.perform(get("/api/products/{id}", productId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.name").value("Test Product"))
                .andExpect(jsonPath("$.data.price").value(99.99));

        verify(productService).getProductById(eq(productId));
    }

    /**
     * Test getting all products successfully
     * Verifies that all products can be retrieved
     */
    @Test
    @DisplayName("Should successfully get all products")
    void testGetAllProducts_Success() throws Exception {
        // Given
        ProductDTO product1 = new ProductDTO();
        product1.setId(1L);
        product1.setName("Product 1");
        product1.setPrice(BigDecimal.valueOf(99.99));
        
        ProductDTO product2 = new ProductDTO();
        product2.setId(2L);
        product2.setName("Product 2");
        product2.setPrice(BigDecimal.valueOf(149.99));
        
        List<ProductDTO> products = Arrays.asList(product1, product2);
        
        when(productService.getAllProducts()).thenReturn(products);

        // When & Then
        mockMvc.perform(get("/api/products"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(2))
                .andExpect(jsonPath("$.data[0].name").value("Product 1"))
                .andExpect(jsonPath("$.data[1].name").value("Product 2"));

        verify(productService).getAllProducts();
    }

    /**
     * Test searching products by name successfully
     * Verifies that products can be searched by query string
     */
    @Test
    @DisplayName("Should successfully search products by name")
    void testSearchProducts_Success() throws Exception {
        // Given
        String query = "laptop";
        ProductDTO product = new ProductDTO();
        product.setId(1L);
        product.setName("Gaming Laptop");
        product.setPrice(BigDecimal.valueOf(1299.99));
        
        List<ProductDTO> searchResults = Arrays.asList(product);
        
        when(productService.searchProducts(query)).thenReturn(searchResults);

        // When & Then
        mockMvc.perform(get("/api/products/search")
                .param("query", query))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].name").value("Gaming Laptop"));

        verify(productService).searchProducts(eq(query));
    }

    /**
     * Test getting products by category successfully
     * Verifies that products can be filtered by category
     */
    @Test
    @DisplayName("Should successfully get products by category")
    void testGetProductsByCategory_Success() throws Exception {
        // Given
        String category = "Electronics";
        ProductDTO product = new ProductDTO();
        product.setId(1L);
        product.setName("Smartphone");
        product.setCategory(category);
        product.setPrice(BigDecimal.valueOf(699.99));
        
        List<ProductDTO> categoryProducts = Arrays.asList(product);
        
        when(productService.getProductsByCategory(category)).thenReturn(categoryProducts);

        // When & Then
        mockMvc.perform(get("/api/products/category/{category}", category))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].category").value(category));

        verify(productService).getProductsByCategory(eq(category));
    }

    /**
     * Test updating product successfully
     * Verifies that products can be updated
     */
    @Test
    @DisplayName("Should successfully update product")
    void testUpdateProduct_Success() throws Exception {
        // Given
        Long productId = 1L;
        ProductDTO productDTO = new ProductDTO();
        productDTO.setName("Updated Product");
        productDTO.setPrice(BigDecimal.valueOf(149.99));
        
        ProductDTO updatedProduct = new ProductDTO();
        updatedProduct.setId(productId);
        updatedProduct.setName("Updated Product");
        updatedProduct.setPrice(BigDecimal.valueOf(149.99));
        
        when(productService.updateProduct(eq(productId), any(ProductDTO.class))).thenReturn(updatedProduct);

        // When & Then
        mockMvc.perform(put("/api/products/{id}", productId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(productDTO)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Product updated successfully"))
                .andExpect(jsonPath("$.data.name").value("Updated Product"))
                .andExpect(jsonPath("$.data.price").value(149.99));

        verify(productService).updateProduct(eq(productId), any(ProductDTO.class));
    }

    /**
     * Test deleting product successfully
     * Verifies that products can be deleted
     */
    @Test
    @DisplayName("Should successfully delete product")
    void testDeleteProduct_Success() throws Exception {
        // Given
        Long productId = 1L;

        // When & Then
        mockMvc.perform(delete("/api/products/{id}", productId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Product deleted successfully"))
                .andExpect(jsonPath("$.data").isEmpty());

        verify(productService).deleteProduct(eq(productId));
    }

    /**
     * Test getting non-existent product
     * Verifies that proper error handling occurs for invalid product IDs
     */
    @Test
    @DisplayName("Should handle getting non-existent product")
    void testGetProductById_NotFound() throws Exception {
        // Given
        Long nonExistentProductId = 999L;
        
        when(productService.getProductById(nonExistentProductId))
                .thenThrow(new RuntimeException("Product not found"));

        // When & Then
        mockMvc.perform(get("/api/products/{id}", nonExistentProductId))
                .andExpect(status().isInternalServerError());

        verify(productService).getProductById(eq(nonExistentProductId));
    }

    /**
     * Test searching products with empty query
     * Verifies that empty search queries are handled properly
     */
    @Test
    @DisplayName("Should handle search with empty query")
    void testSearchProducts_EmptyQuery() throws Exception {
        // Given
        String emptyQuery = "";
        
        when(productService.searchProducts(emptyQuery)).thenReturn(Arrays.asList());

        // When & Then
        mockMvc.perform(get("/api/products/search")
                .param("query", emptyQuery))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(0));

        verify(productService).searchProducts(eq(emptyQuery));
    }
}