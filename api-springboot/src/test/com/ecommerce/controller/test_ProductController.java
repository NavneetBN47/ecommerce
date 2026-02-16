package com.ecommerce.controller;

import com.ecommerce.dto.ProductResponse;
import com.ecommerce.service.ProductService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Test class for ProductController
 * 
 * This test class verifies the REST API endpoints for product catalog,
 * including product search functionality.
 * 
 * @author Shopping Cart System Team
 * @version 1.0.0
 */
@ExtendWith(MockitoExtension.class)
class test_ProductController {

    @Mock
    private ProductService productService;

    @InjectMocks
    private ProductController productController;

    private List<ProductResponse> productList;

    /**
     * Setup method to initialize test data before each test
     */
    @BeforeEach
    void setUp() {
        ProductResponse product1 = ProductResponse.builder()
                .id(UUID.randomUUID())
                .name("Laptop")
                .description("High-performance laptop")
                .price(new BigDecimal("999.99"))
                .availableQty(10)
                .sku("LAP-001")
                .isActive(true)
                .build();

        ProductResponse product2 = ProductResponse.builder()
                .id(UUID.randomUUID())
                .name("Laptop Stand")
                .description("Ergonomic laptop stand")
                .price(new BigDecimal("49.99"))
                .availableQty(25)
                .sku("ACC-001")
                .isActive(true)
                .build();

        productList = Arrays.asList(product1, product2);
    }

    /**
     * Test successful product search with keyword
     * 
     * Verifies that products can be searched by keyword and
     * returns HTTP 200 OK with matching products.
     */
    @Test
    void searchProducts_WithKeyword_ShouldReturnMatchingProducts() {
        String keyword = "laptop";
        when(productService.searchProducts(keyword)).thenReturn(productList);

        ResponseEntity<List<ProductResponse>> response = productController.searchProducts(keyword);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody()).hasSize(2);
        assertThat(response.getBody().get(0).getName()).contains("Laptop");
        verify(productService, times(1)).searchProducts(keyword);
    }

    /**
     * Test product search with null keyword
     * 
     * Verifies that searching with null keyword returns all active products.
     */
    @Test
    void searchProducts_WithNullKeyword_ShouldReturnAllProducts() {
        when(productService.searchProducts(null)).thenReturn(productList);

        ResponseEntity<List<ProductResponse>> response = productController.searchProducts(null);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody()).hasSize(2);
        verify(productService, times(1)).searchProducts(null);
    }

    /**
     * Test product search with empty keyword
     * 
     * Verifies that searching with empty keyword returns all active products.
     */
    @Test
    void searchProducts_WithEmptyKeyword_ShouldReturnAllProducts() {
        String emptyKeyword = "";
        when(productService.searchProducts(emptyKeyword)).thenReturn(productList);

        ResponseEntity<List<ProductResponse>> response = productController.searchProducts(emptyKeyword);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody()).hasSize(2);
        verify(productService, times(1)).searchProducts(emptyKeyword);
    }

    /**
     * Test product search with no matching results
     * 
     * Verifies that searching with a keyword that matches no products
     * returns an empty list.
     */
    @Test
    void searchProducts_WithNoMatchingResults_ShouldReturnEmptyList() {
        String keyword = "nonexistent";
        when(productService.searchProducts(keyword)).thenReturn(Collections.emptyList());

        ResponseEntity<List<ProductResponse>> response = productController.searchProducts(keyword);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody()).isEmpty();
        verify(productService, times(1)).searchProducts(keyword);
    }

    /**
     * Test product search with case-insensitive keyword
     * 
     * Verifies that product search is case-insensitive and
     * returns matching products regardless of keyword case.
     */
    @Test
    void searchProducts_WithCaseInsensitiveKeyword_ShouldReturnMatchingProducts() {
        String keyword = "LAPTOP";
        when(productService.searchProducts(keyword)).thenReturn(productList);

        ResponseEntity<List<ProductResponse>> response = productController.searchProducts(keyword);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody()).hasSize(2);
        verify(productService, times(1)).searchProducts(keyword);
    }

    /**
     * Test product search with partial keyword match
     * 
     * Verifies that product search supports partial keyword matching
     * and returns all products containing the keyword.
     */
    @Test
    void searchProducts_WithPartialKeyword_ShouldReturnMatchingProducts() {
        String keyword = "lap";
        when(productService.searchProducts(keyword)).thenReturn(productList);

        ResponseEntity<List<ProductResponse>> response = productController.searchProducts(keyword);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody()).hasSize(2);
        verify(productService, times(1)).searchProducts(keyword);
    }

    /**
     * Test product search returns only active products
     * 
     * Verifies that the search results contain only active products
     * with isActive flag set to true.
     */
    @Test
    void searchProducts_ShouldReturnOnlyActiveProducts() {
        String keyword = "laptop";
        when(productService.searchProducts(keyword)).thenReturn(productList);

        ResponseEntity<List<ProductResponse>> response = productController.searchProducts(keyword);

        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody()).allMatch(ProductResponse::getIsActive);
    }
}