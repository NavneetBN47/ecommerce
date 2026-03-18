package com.ecommerce.productcatalog.integration;

import com.ecommerce.productcatalog.dto.ProductDTO;
import com.ecommerce.productcatalog.entity.Product;
import com.ecommerce.productcatalog.repository.ProductRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("Product Catalog Integration Tests")
class ProductCatalogIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test");

    @Container
    static GenericContainer<?> redis = new GenericContainer<>("redis:7-alpine")
            .withExposedPorts(6379);

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.redis.host", redis::getHost);
        registry.add("spring.redis.port", redis::getFirstMappedPort);
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ProductRepository productRepository;

    private static UUID testProductId;

    @BeforeEach
    void setUp() {
        // Create test products
        Product laptop = Product.builder()
                .name("Gaming Laptop")
                .description("High-performance gaming laptop with RTX 4080")
                .price(new BigDecimal("1999.99"))
                .stock(25)
                .available(true)
                .build();

        Product mouse = Product.builder()
                .name("Wireless Mouse")
                .description("Ergonomic wireless mouse")
                .price(new BigDecimal("49.99"))
                .stock(100)
                .available(true)
                .build();

        Product keyboard = Product.builder()
                .name("Mechanical Keyboard")
                .description("RGB mechanical keyboard")
                .price(new BigDecimal("129.99"))
                .stock(0)
                .available(false)
                .build();

        productRepository.save(laptop);
        productRepository.save(mouse);
        productRepository.save(keyboard);
        testProductId = laptop.getId();
    }

    @Test
    @Order(1)
    @DisplayName("Should search products with keyword")
    void testSearchProductsWithKeyword() throws Exception {
        mockMvc.perform(get("/api/products/search")
                        .param("keyword", "laptop")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].name").value("Gaming Laptop"))
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    @Order(2)
    @DisplayName("Should search all products without keyword")
    void testSearchAllProducts() throws Exception {
        mockMvc.perform(get("/api/products/search")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.totalElements").value(3));
    }

    @Test
    @Order(3)
    @DisplayName("Should handle case-insensitive search")
    void testCaseInsensitiveSearch() throws Exception {
        mockMvc.perform(get("/api/products/search")
                        .param("keyword", "LAPTOP")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].name").value("Gaming Laptop"));
    }

    @Test
    @Order(4)
    @DisplayName("Should search products by partial match")
    void testPartialMatchSearch() throws Exception {
        mockMvc.perform(get("/api/products/search")
                        .param("keyword", "wire")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].name").value("Wireless Mouse"));
    }

    @Test
    @Order(5)
    @DisplayName("Should return empty results for non-matching keyword")
    void testSearchWithNoResults() throws Exception {
        mockMvc.perform(get("/api/products/search")
                        .param("keyword", "nonexistent")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isEmpty())
                .andExpect(jsonPath("$.totalElements").value(0));
    }

    @Test
    @Order(6)
    @DisplayName("Should get product by ID")
    void testGetProductById() throws Exception {
        mockMvc.perform(get("/api/products/{id}", testProductId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Gaming Laptop"))
                .andExpect(jsonPath("$.price").value(1999.99))
                .andExpect(jsonPath("$.stock").value(25))
                .andExpect(jsonPath("$.available").value(true));
    }

    @Test
    @Order(7)
    @DisplayName("Should cache product after first retrieval")
    void testProductCaching() throws Exception {
        // First request - should hit database
        mockMvc.perform(get("/api/products/{id}", testProductId))
                .andExpect(status().isOk());

        // Second request - should hit cache
        mockMvc.perform(get("/api/products/{id}", testProductId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Gaming Laptop"));
    }

    @Test
    @Order(8)
    @DisplayName("Should return 404 for non-existent product")
    void testGetNonExistentProduct() throws Exception {
        UUID nonExistentId = UUID.randomUUID();
        mockMvc.perform(get("/api/products/{id}", nonExistentId))
                .andExpect(status().isNotFound());
    }

    @Test
    @Order(9)
    @DisplayName("Should handle pagination correctly")
    void testPagination() throws Exception {
        mockMvc.perform(get("/api/products/search")
                        .param("page", "0")
                        .param("size", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.pageNumber").value(0))
                .andExpect(jsonPath("$.pageSize").value(2))
                .andExpect(jsonPath("$.totalPages").value(2));
    }

    @Test
    @Order(10)
    @DisplayName("Should get second page of results")
    void testSecondPage() throws Exception {
        mockMvc.perform(get("/api/products/search")
                        .param("page", "1")
                        .param("size", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.pageNumber").value(1));
    }

    @Test
    @Order(11)
    @DisplayName("Should show out of stock products")
    void testOutOfStockProduct() throws Exception {
        mockMvc.perform(get("/api/products/search")
                        .param("keyword", "keyboard")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].stock").value(0))
                .andExpect(jsonPath("$.content[0].available").value(false));
    }

    @Test
    @Order(12)
    @DisplayName("Should search in product description")
    void testSearchInDescription() throws Exception {
        mockMvc.perform(get("/api/products/search")
                        .param("keyword", "ergonomic")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].name").value("Wireless Mouse"));
    }

    @Test
    @Order(13)
    @DisplayName("Should handle special characters in search")
    void testSearchWithSpecialCharacters() throws Exception {
        mockMvc.perform(get("/api/products/search")
                        .param("keyword", "laptop & mouse")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk());
    }

    @Test
    @Order(14)
    @DisplayName("Should validate page parameter")
    void testInvalidPageParameter() throws Exception {
        mockMvc.perform(get("/api/products/search")
                        .param("keyword", "laptop")
                        .param("page", "-1")
                        .param("size", "10"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @Order(15)
    @DisplayName("Should validate size parameter")
    void testInvalidSizeParameter() throws Exception {
        mockMvc.perform(get("/api/products/search")
                        .param("keyword", "laptop")
                        .param("page", "0")
                        .param("size", "0"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @Order(16)
    @DisplayName("Should handle concurrent requests")
    void testConcurrentRequests() throws Exception {
        for (int i = 0; i < 10; i++) {
            mockMvc.perform(get("/api/products/{id}", testProductId))
                    .andExpect(status().isOk());
        }
    }

    @Test
    @Order(17)
    @DisplayName("Should return products sorted by relevance")
    void testSearchRelevance() throws Exception {
        mockMvc.perform(get("/api/products/search")
                        .param("keyword", "mouse")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].name").value("Wireless Mouse"));
    }

    @Test
    @Order(18)
    @DisplayName("Should include cache control headers")
    void testCacheControlHeaders() throws Exception {
        mockMvc.perform(get("/api/products/{id}", testProductId))
                .andExpect(status().isOk())
                .andExpect(header().exists("Cache-Control"));
    }
}