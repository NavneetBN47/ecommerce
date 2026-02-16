package com.ecommerce.controller;

import com.ecommerce.dto.AddToCartRequest;
import com.ecommerce.dto.CartItemResponse;
import com.ecommerce.dto.CartResponse;
import com.ecommerce.dto.UpdateCartItemRequest;
import com.ecommerce.security.UserPrincipal;
import com.ecommerce.service.CartService;
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
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Test class for CartController
 * 
 * This test class verifies the REST API endpoints for shopping cart management,
 * including adding items, updating quantities, removing items, and retrieving cart.
 * 
 * @author Shopping Cart System Team
 * @version 1.0.0
 */
@ExtendWith(MockitoExtension.class)
class test_CartController {

    @Mock
    private CartService cartService;

    @Mock
    private UserPrincipal currentUser;

    @InjectMocks
    private CartController cartController;

    private UUID userId;
    private UUID productId;
    private UUID cartId;
    private UUID itemId;
    private CartResponse cartResponse;

    /**
     * Setup method to initialize test data before each test
     */
    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        productId = UUID.randomUUID();
        cartId = UUID.randomUUID();
        itemId = UUID.randomUUID();

        when(currentUser.getId()).thenReturn(userId);

        CartItemResponse cartItem = CartItemResponse.builder()
                .id(itemId)
                .productId(productId)
                .productName("Test Product")
                .quantity(2)
                .unitPrice(new BigDecimal("99.99"))
                .totalPrice(new BigDecimal("199.98"))
                .build();

        cartResponse = CartResponse.builder()
                .cartId(cartId)
                .items(Arrays.asList(cartItem))
                .grandTotal(new BigDecimal("199.98"))
                .totalItems(1)
                .build();
    }

    /**
     * Test successful addition of product to cart
     * 
     * Verifies that a product can be added to the cart and
     * returns HTTP 201 CREATED with the updated cart response.
     */
    @Test
    void addToCart_WithValidRequest_ShouldReturnCreatedWithCartResponse() {
        AddToCartRequest request = new AddToCartRequest();
        request.setProductId(productId);
        request.setQuantity(2);

        when(cartService.addProductToCart(eq(userId), any(AddToCartRequest.class)))
                .thenReturn(cartResponse);

        ResponseEntity<CartResponse> response = cartController.addToCart(currentUser, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getCartId()).isEqualTo(cartId);
        assertThat(response.getBody().getTotalItems()).isEqualTo(1);
        verify(cartService, times(1)).addProductToCart(eq(userId), any(AddToCartRequest.class));
    }

    /**
     * Test adding product to cart with multiple items
     * 
     * Verifies that multiple products can be added to the cart
     * and the grand total is calculated correctly.
     */
    @Test
    void addToCart_WithMultipleItems_ShouldCalculateCorrectGrandTotal() {
        AddToCartRequest request = new AddToCartRequest();
        request.setProductId(productId);
        request.setQuantity(3);

        CartItemResponse item1 = CartItemResponse.builder()
                .id(UUID.randomUUID())
                .productId(productId)
                .productName("Product 1")
                .quantity(2)
                .unitPrice(new BigDecimal("50.00"))
                .totalPrice(new BigDecimal("100.00"))
                .build();

        CartItemResponse item2 = CartItemResponse.builder()
                .id(UUID.randomUUID())
                .productId(UUID.randomUUID())
                .productName("Product 2")
                .quantity(1)
                .unitPrice(new BigDecimal("75.00"))
                .totalPrice(new BigDecimal("75.00"))
                .build();

        CartResponse multiItemCart = CartResponse.builder()
                .cartId(cartId)
                .items(Arrays.asList(item1, item2))
                .grandTotal(new BigDecimal("175.00"))
                .totalItems(2)
                .build();

        when(cartService.addProductToCart(eq(userId), any(AddToCartRequest.class)))
                .thenReturn(multiItemCart);

        ResponseEntity<CartResponse> response = cartController.addToCart(currentUser, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody().getTotalItems()).isEqualTo(2);
        assertThat(response.getBody().getGrandTotal()).isEqualTo(new BigDecimal("175.00"));
    }

    /**
     * Test successful update of cart item quantity
     * 
     * Verifies that a cart item quantity can be updated and
     * returns HTTP 200 OK with the updated cart response.
     */
    @Test
    void updateCartItem_WithValidRequest_ShouldReturnOkWithUpdatedCart() {
        UpdateCartItemRequest request = new UpdateCartItemRequest();
        request.setQuantity(5);

        CartItemResponse updatedItem = CartItemResponse.builder()
                .id(itemId)
                .productId(productId)
                .productName("Test Product")
                .quantity(5)
                .unitPrice(new BigDecimal("99.99"))
                .totalPrice(new BigDecimal("499.95"))
                .build();

        CartResponse updatedCart = CartResponse.builder()
                .cartId(cartId)
                .items(Arrays.asList(updatedItem))
                .grandTotal(new BigDecimal("499.95"))
                .totalItems(1)
                .build();

        when(cartService.updateCartItem(eq(userId), eq(itemId), any(UpdateCartItemRequest.class)))
                .thenReturn(updatedCart);

        ResponseEntity<CartResponse> response = cartController.updateCartItem(currentUser, itemId, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getItems().get(0).getQuantity()).isEqualTo(5);
        assertThat(response.getBody().getGrandTotal()).isEqualTo(new BigDecimal("499.95"));
        verify(cartService, times(1)).updateCartItem(eq(userId), eq(itemId), any(UpdateCartItemRequest.class));
    }

    /**
     * Test successful removal of cart item
     * 
     * Verifies that a cart item can be removed and
     * returns HTTP 200 OK with the updated cart response.
     */
    @Test
    void removeCartItem_WithValidItemId_ShouldReturnOkWithUpdatedCart() {
        CartResponse emptyCart = CartResponse.builder()
                .cartId(cartId)
                .items(Arrays.asList())
                .grandTotal(BigDecimal.ZERO)
                .totalItems(0)
                .build();

        when(cartService.removeCartItem(userId, itemId)).thenReturn(emptyCart);

        ResponseEntity<CartResponse> response = cartController.removeCartItem(currentUser, itemId);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getTotalItems()).isEqualTo(0);
        verify(cartService, times(1)).removeCartItem(userId, itemId);
    }

    /**
     * Test removal of last cart item returns no content
     * 
     * Verifies that when the last item is removed from cart,
     * returns HTTP 204 NO CONTENT.
     */
    @Test
    void removeCartItem_WhenLastItemRemoved_ShouldReturnNoContent() {
        CartResponse emptyCart = CartResponse.builder()
                .cartId(null)
                .items(Arrays.asList())
                .grandTotal(BigDecimal.ZERO)
                .totalItems(0)
                .build();

        when(cartService.removeCartItem(userId, itemId)).thenReturn(emptyCart);

        ResponseEntity<CartResponse> response = cartController.removeCartItem(currentUser, itemId);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        verify(cartService, times(1)).removeCartItem(userId, itemId);
    }

    /**
     * Test successful retrieval of user's cart
     * 
     * Verifies that a user can retrieve their cart and
     * returns HTTP 200 OK with the cart response.
     */
    @Test
    void getCart_WithValidUser_ShouldReturnOkWithCartResponse() {
        when(cartService.getCart(userId)).thenReturn(cartResponse);

        ResponseEntity<CartResponse> response = cartController.getCart(currentUser);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getCartId()).isEqualTo(cartId);
        assertThat(response.getBody().getTotalItems()).isEqualTo(1);
        verify(cartService, times(1)).getCart(userId);
    }

    /**
     * Test retrieval of empty cart
     * 
     * Verifies that retrieving an empty cart returns
     * a valid response with zero items and zero total.
     */
    @Test
    void getCart_WhenCartIsEmpty_ShouldReturnEmptyCart() {
        CartResponse emptyCart = CartResponse.builder()
                .cartId(cartId)
                .items(Arrays.asList())
                .grandTotal(BigDecimal.ZERO)
                .totalItems(0)
                .build();

        when(cartService.getCart(userId)).thenReturn(emptyCart);

        ResponseEntity<CartResponse> response = cartController.getCart(currentUser);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getTotalItems()).isEqualTo(0);
        assertThat(response.getBody().getGrandTotal()).isEqualTo(BigDecimal.ZERO);
    }
}