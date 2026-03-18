package com.ecommerce.shoppingcart.integration;

import com.ecommerce.shoppingcart.dto.*;
import com.ecommerce.shoppingcart.entity.Product;
import com.ecommerce.shoppingcart.repository.ProductRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.testcontainers.containers.PostgreSQLContainer;
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
@DisplayName("Shopping Cart Integration Tests")
class ShoppingCartIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ProductRepository productRepository;

    private static String accessToken = "mock_jwt_token";
    private static UUID testProductId;
    private static UUID testCartItemId;

    @BeforeEach
    void setUp() {
        Product product = Product.builder()
                .name("Test Laptop")
                .description("Test laptop for cart operations")
                .price(new BigDecimal("999.99"))
                .stock(50)
                .available(true)
                .build();
        Product savedProduct = productRepository.save(product);
        testProductId = savedProduct.getId();
    }

    @Test
    @Order(1)
    @DisplayName("Should get empty cart initially")
    void testGetEmptyCart() throws Exception {
        mockMvc.perform(get("/api/cart")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items").isEmpty())
                .andExpect(jsonPath("$.totalItems").value(0))
                .andExpect(jsonPath("$.totalPrice").value(0));
    }

    @Test
    @Order(2)
    @DisplayName("Should add item to cart")
    void testAddItemToCart() throws Exception {
        AddCartItemDTO addDTO = AddCartItemDTO.builder()
                .productId(testProductId)
                .quantity(2)
                .build();

        MvcResult result = mockMvc.perform(post("/api/cart/items")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(addDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items").isArray())
                .andExpect(jsonPath("$.items[0].productName").value("Test Laptop"))
                .andExpect(jsonPath("$.items[0].quantity").value(2))
                .andExpect(jsonPath("$.totalItems").value(2))
                .andReturn();

        String responseBody = result.getResponse().getContentAsString();
        CartDTO cartDTO = objectMapper.readValue(responseBody, CartDTO.class);
        testCartItemId = cartDTO.getItems().get(0).getId();
    }

    @Test
    @Order(3)
    @DisplayName("Should get cart with items")
    void testGetCartWithItems() throws Exception {
        mockMvc.perform(get("/api/cart")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items").isArray())
                .andExpect(jsonPath("$.items.length()").value(1))
                .andExpect(jsonPath("$.totalItems").value(2));
    }

    @Test
    @Order(4)
    @DisplayName("Should update cart item quantity")
    void testUpdateCartItem() throws Exception {
        UpdateCartItemDTO updateDTO = UpdateCartItemDTO.builder()
                .quantity(5)
                .build();

        mockMvc.perform(put("/api/cart/items/{itemId}", testCartItemId)
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].quantity").value(5))
                .andExpect(jsonPath("$.totalItems").value(5));
    }

    @Test
    @Order(5)
    @DisplayName("Should calculate cart total correctly")
    void testCartTotalCalculation() throws Exception {
        mockMvc.perform(get("/api/cart")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalPrice").value(4999.95)); // 999.99 * 5
    }

    @Test
    @Order(6)
    @DisplayName("Should add another item to cart")
    void testAddAnotherItem() throws Exception {
        Product anotherProduct = Product.builder()
                .name("Wireless Mouse")
                .price(new BigDecimal("49.99"))
                .stock(100)
                .available(true)
                .build();
        Product savedProduct = productRepository.save(anotherProduct);

        AddCartItemDTO addDTO = AddCartItemDTO.builder()
                .productId(savedProduct.getId())
                .quantity(3)
                .build();

        mockMvc.perform(post("/api/cart/items")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(addDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items.length()").value(2))
                .andExpect(jsonPath("$.totalItems").value(8)); // 5 + 3
    }

    @Test
    @Order(7)
    @DisplayName("Should prevent adding item with invalid quantity")
    void testAddItemWithInvalidQuantity() throws Exception {
        AddCartItemDTO invalidDTO = AddCartItemDTO.builder()
                .productId(testProductId)
                .quantity(0)
                .build();

        mockMvc.perform(post("/api/cart/items")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDTO)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @Order(8)
    @DisplayName("Should prevent adding item exceeding stock")
    void testAddItemExceedingStock() throws Exception {
        AddCartItemDTO exceedingDTO = AddCartItemDTO.builder()
                .productId(testProductId)
                .quantity(100)
                .build();

        mockMvc.perform(post("/api/cart/items")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(exceedingDTO)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @Order(9)
    @DisplayName("Should prevent adding non-existent product")
    void testAddNonExistentProduct() throws Exception {
        AddCartItemDTO nonExistentDTO = AddCartItemDTO.builder()
                .productId(UUID.randomUUID())
                .quantity(1)
                .build();

        mockMvc.perform(post("/api/cart/items")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(nonExistentDTO)))
                .andExpect(status().isNotFound());
    }

    @Test
    @Order(10)
    @DisplayName("Should remove item from cart")
    void testRemoveCartItem() throws Exception {
        mockMvc.perform(delete("/api/cart/items/{itemId}", testCartItemId)
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items.length()").value(1));
    }

    @Test
    @Order(11)
    @DisplayName("Should return 404 when removing non-existent item")
    void testRemoveNonExistentItem() throws Exception {
        UUID nonExistentItemId = UUID.randomUUID();
        mockMvc.perform(delete("/api/cart/items/{itemId}", nonExistentItemId)
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isNotFound());
    }

    @Test
    @Order(12)
    @DisplayName("Should update item to maximum allowed quantity")
    void testUpdateToMaxQuantity() throws Exception {
        UpdateCartItemDTO updateDTO = UpdateCartItemDTO.builder()
                .quantity(99)
                .build();

        mockMvc.perform(put("/api/cart/items/{itemId}", testCartItemId)
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].quantity").value(99));
    }

    @Test
    @Order(13)
    @DisplayName("Should prevent updating to quantity exceeding limit")
    void testUpdateToExceedingQuantity() throws Exception {
        UpdateCartItemDTO exceedingDTO = UpdateCartItemDTO.builder()
                .quantity(100)
                .build();

        mockMvc.perform(put("/api/cart/items/{itemId}", testCartItemId)
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(exceedingDTO)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @Order(14)
    @DisplayName("Should clear cart successfully")
    void testClearCart() throws Exception {
        mockMvc.perform(delete("/api/cart")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isNoContent());
    }

    @Test
    @Order(15)
    @DisplayName("Should have empty cart after clearing")
    void testCartEmptyAfterClearing() throws Exception {
        mockMvc.perform(get("/api/cart")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items").isEmpty())
                .andExpect(jsonPath("$.totalItems").value(0))
                .andExpect(jsonPath("$.totalPrice").value(0));
    }

    @Test
    @Order(16)
    @DisplayName("Should reject cart operations without authentication")
    void testCartOperationsWithoutAuth() throws Exception {
        mockMvc.perform(get("/api/cart"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(post("/api/cart/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isUnauthorized());
    }
}