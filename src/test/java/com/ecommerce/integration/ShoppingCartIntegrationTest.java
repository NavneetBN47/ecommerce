package com.ecommerce.integration;

import com.ecommerce.shoppingcart.application.dto.*;
import com.ecommerce.usermanagement.application.dto.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("Shopping Cart Integration Tests")
class ShoppingCartIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private static String jwtToken;
    private static Long cartItemId;

    @BeforeAll
    static void setupUser(@Autowired MockMvc mockMvc, @Autowired ObjectMapper objectMapper) throws Exception {
        UserRegistrationRequest regRequest = new UserRegistrationRequest();
        regRequest.setEmail("cart.test@example.com");
        regRequest.setPassword("CartTest123!");
        regRequest.setFirstName("Cart");
        regRequest.setLastName("Test");
        regRequest.setPhoneNumber("+1234567890");

        mockMvc.perform(post("/api/v1/users/register")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(regRequest)));

        UserLoginRequest loginRequest = new UserLoginRequest();
        loginRequest.setEmail("cart.test@example.com");
        loginRequest.setPassword("CartTest123!");

        MvcResult result = mockMvc.perform(post("/api/v1/users/login")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andReturn();

        String responseBody = result.getResponse().getContentAsString();
        UserLoginResponse response = objectMapper.readValue(responseBody, UserLoginResponse.class);
        jwtToken = response.getToken();
    }

    @Test
    @Order(1)
    @DisplayName("Integration: Get Empty Cart")
    void testGetEmptyCart() throws Exception {
        mockMvc.perform(get("/api/v1/cart")
                .with(csrf())
                .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items").isEmpty())
                .andExpect(jsonPath("$.totalAmount").value(0.00));
    }

    @Test
    @Order(2)
    @DisplayName("Integration: Add Item To Cart")
    void testAddItemToCart() throws Exception {
        AddCartItemRequest request = new AddCartItemRequest();
        request.setProductId(1L);
        request.setQuantity(2);

        MvcResult result = mockMvc.perform(post("/api/v1/cart/items")
                .with(csrf())
                .header("Authorization", "Bearer " + jwtToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items").isArray())
                .andExpect(jsonPath("$.items[0].productId").value(1))
                .andExpect(jsonPath("$.items[0].quantity").value(2))
                .andReturn();

        String responseBody = result.getResponse().getContentAsString();
        CartResponse response = objectMapper.readValue(responseBody, CartResponse.class);
        cartItemId = response.getItems().get(0).getItemId();
    }

    @Test
    @Order(3)
    @DisplayName("Integration: Update Cart Item")
    void testUpdateCartItem() throws Exception {
        UpdateCartItemRequest request = new UpdateCartItemRequest();
        request.setQuantity(5);

        mockMvc.perform(put("/api/v1/cart/items/{itemId}", cartItemId)
                .with(csrf())
                .header("Authorization", "Bearer " + jwtToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].quantity").value(5));
    }

    @Test
    @Order(4)
    @DisplayName("Integration: Get Cart With Items")
    void testGetCartWithItems() throws Exception {
        mockMvc.perform(get("/api/v1/cart")
                .with(csrf())
                .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items").isNotEmpty())
                .andExpect(jsonPath("$.itemCount").value(1))
                .andExpect(jsonPath("$.totalAmount").exists());
    }

    @Test
    @Order(5)
    @DisplayName("Integration: Remove Item From Cart")
    void testRemoveItemFromCart() throws Exception {
        mockMvc.perform(delete("/api/v1/cart/items/{itemId}", cartItemId)
                .with(csrf())
                .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items").isEmpty());
    }

    @Test
    @Order(6)
    @DisplayName("Integration: Clear Cart")
    void testClearCart() throws Exception {
        mockMvc.perform(delete("/api/v1/cart")
                .with(csrf())
                .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Cart cleared successfully"));
    }
}