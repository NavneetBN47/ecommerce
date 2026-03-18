package com.ecommerce.shoppingcart.application.service;

import com.ecommerce.shoppingcart.application.dto.*;
import com.ecommerce.shoppingcart.application.exception.*;
import com.ecommerce.shoppingcart.domain.entity.Cart;
import com.ecommerce.shoppingcart.domain.entity.CartItem;
import com.ecommerce.shoppingcart.domain.repository.CartRepository;
import com.ecommerce.shoppingcart.domain.repository.CartItemRepository;
import com.ecommerce.shoppingcart.infrastructure.client.UserServiceClient;
import com.ecommerce.shoppingcart.infrastructure.client.ProductServiceClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Cart Service Tests")
class CartServiceTest {

    @Mock
    private CartRepository cartRepository;

    @Mock
    private CartItemRepository cartItemRepository;

    @Mock
    private UserServiceClient userServiceClient;

    @Mock
    private ProductServiceClient productServiceClient;

    @Mock
    private AuditLogService auditLogService;

    @InjectMocks
    private CartService cartService;

    private Cart testCart;
    private CartItem testCartItem;
    private ProductResponse testProduct;

    @BeforeEach
    void setUp() {
        testCart = new Cart();
        testCart.setCartId(1L);
        testCart.setUserId(1L);
        testCart.setItems(new ArrayList<>());
        testCart.setCreatedAt(LocalDateTime.now());
        testCart.setUpdatedAt(LocalDateTime.now());

        testCartItem = new CartItem();
        testCartItem.setItemId(1L);
        testCartItem.setCart(testCart);
        testCartItem.setProductId(1L);
        testCartItem.setQuantity(2);
        testCartItem.setPrice(new BigDecimal("99.99"));
        testCartItem.setAddedAt(LocalDateTime.now());

        testProduct = new ProductResponse();
        testProduct.setProductId(1L);
        testProduct.setName("Test Product");
        testProduct.setPrice(new BigDecimal("99.99"));
        testProduct.setStockQuantity(100);
        testProduct.setIsAvailable(true);
    }

    @Test
    @DisplayName("Get Cart - Success (Existing Cart)")
    void testGetCart_ExistingCart() {
        when(userServiceClient.getUserIdByEmail(anyString())).thenReturn(1L);
        when(cartRepository.findByUserId(anyLong())).thenReturn(Optional.of(testCart));

        CartResponse response = cartService.getCart("test@example.com");

        assertThat(response).isNotNull();
        assertThat(response.getCartId()).isEqualTo(1L);
        assertThat(response.getUserId()).isEqualTo(1L);

        verify(userServiceClient, times(1)).getUserIdByEmail("test@example.com");
        verify(cartRepository, times(1)).findByUserId(1L);
    }

    @Test
    @DisplayName("Get Cart - Auto-Create New Cart")
    void testGetCart_AutoCreate() {
        when(userServiceClient.getUserIdByEmail(anyString())).thenReturn(1L);
        when(cartRepository.findByUserId(anyLong())).thenReturn(Optional.empty());
        when(cartRepository.save(any(Cart.class))).thenReturn(testCart);

        CartResponse response = cartService.getCart("test@example.com");

        assertThat(response).isNotNull();
        assertThat(response.getUserId()).isEqualTo(1L);

        verify(cartRepository, times(1)).save(any(Cart.class));
    }

    @Test
    @DisplayName("Get Cart - User Not Found")
    void testGetCart_UserNotFound() {
        when(userServiceClient.getUserIdByEmail(anyString()))
                .thenThrow(new UserNotFoundException("User not found"));

        assertThatThrownBy(() -> cartService.getCart("nonexistent@example.com"))
                .isInstanceOf(UserNotFoundException.class);

        verify(cartRepository, never()).findByUserId(anyLong());
    }

    @Test
    @DisplayName("Add Item To Cart - Success (New Item)")
    void testAddItemToCart_NewItem() {
        AddCartItemRequest request = new AddCartItemRequest();
        request.setProductId(1L);
        request.setQuantity(2);

        when(userServiceClient.getUserIdByEmail(anyString())).thenReturn(1L);
        when(cartRepository.findByUserId(anyLong())).thenReturn(Optional.of(testCart));
        when(productServiceClient.getProduct(anyLong())).thenReturn(testProduct);
        when(productServiceClient.checkAvailability(anyLong(), anyInt())).thenReturn(true);
        when(cartItemRepository.findByCartAndProductId(any(Cart.class), anyLong())).thenReturn(Optional.empty());
        when(cartItemRepository.save(any(CartItem.class))).thenReturn(testCartItem);
        when(cartRepository.save(any(Cart.class))).thenReturn(testCart);

        CartResponse response = cartService.addItemToCart("test@example.com", request);

        assertThat(response).isNotNull();

        verify(productServiceClient, times(1)).getProduct(1L);
        verify(productServiceClient, times(1)).checkAvailability(1L, 2);
        verify(cartItemRepository, times(1)).save(any(CartItem.class));
        verify(auditLogService, times(1)).logCartItemAdded(anyLong(), anyLong(), anyInt());
    }

    @Test
    @DisplayName("Add Item To Cart - Update Existing Item")
    void testAddItemToCart_UpdateExisting() {
        AddCartItemRequest request = new AddCartItemRequest();
        request.setProductId(1L);
        request.setQuantity(3);

        testCart.getItems().add(testCartItem);

        when(userServiceClient.getUserIdByEmail(anyString())).thenReturn(1L);
        when(cartRepository.findByUserId(anyLong())).thenReturn(Optional.of(testCart));
        when(productServiceClient.getProduct(anyLong())).thenReturn(testProduct);
        when(productServiceClient.checkAvailability(anyLong(), anyInt())).thenReturn(true);
        when(cartItemRepository.findByCartAndProductId(any(Cart.class), anyLong())).thenReturn(Optional.of(testCartItem));
        when(cartItemRepository.save(any(CartItem.class))).thenReturn(testCartItem);
        when(cartRepository.save(any(Cart.class))).thenReturn(testCart);

        CartResponse response = cartService.addItemToCart("test@example.com", request);

        assertThat(response).isNotNull();
        verify(cartItemRepository, times(1)).save(any(CartItem.class));
    }

    @Test
    @DisplayName("Add Item To Cart - Product Not Found")
    void testAddItemToCart_ProductNotFound() {
        AddCartItemRequest request = new AddCartItemRequest();
        request.setProductId(999L);
        request.setQuantity(2);

        when(userServiceClient.getUserIdByEmail(anyString())).thenReturn(1L);
        when(cartRepository.findByUserId(anyLong())).thenReturn(Optional.of(testCart));
        when(productServiceClient.getProduct(anyLong()))
                .thenThrow(new ProductNotFoundException("Product not found"));

        assertThatThrownBy(() -> cartService.addItemToCart("test@example.com", request))
                .isInstanceOf(ProductNotFoundException.class);

        verify(cartItemRepository, never()).save(any(CartItem.class));
    }

    @Test
    @DisplayName("Add Item To Cart - Insufficient Stock")
    void testAddItemToCart_InsufficientStock() {
        AddCartItemRequest request = new AddCartItemRequest();
        request.setProductId(1L);
        request.setQuantity(200);

        when(userServiceClient.getUserIdByEmail(anyString())).thenReturn(1L);
        when(cartRepository.findByUserId(anyLong())).thenReturn(Optional.of(testCart));
        when(productServiceClient.getProduct(anyLong())).thenReturn(testProduct);
        when(productServiceClient.checkAvailability(anyLong(), anyInt())).thenReturn(false);

        assertThatThrownBy(() -> cartService.addItemToCart("test@example.com", request))
                .isInstanceOf(InsufficientStockException.class);

        verify(cartItemRepository, never()).save(any(CartItem.class));
    }

    @Test
    @DisplayName("Add Item To Cart - Invalid Quantity")
    void testAddItemToCart_InvalidQuantity() {
        AddCartItemRequest request = new AddCartItemRequest();
        request.setProductId(1L);
        request.setQuantity(0);

        when(userServiceClient.getUserIdByEmail(anyString())).thenReturn(1L);
        when(cartRepository.findByUserId(anyLong())).thenReturn(Optional.of(testCart));

        assertThatThrownBy(() -> cartService.addItemToCart("test@example.com", request))
                .isInstanceOf(InvalidQuantityException.class);

        verify(productServiceClient, never()).getProduct(anyLong());
    }

    @Test
    @DisplayName("Update Cart Item - Success")
    void testUpdateCartItem_Success() {
        UpdateCartItemRequest request = new UpdateCartItemRequest();
        request.setQuantity(5);

        testCart.getItems().add(testCartItem);

        when(userServiceClient.getUserIdByEmail(anyString())).thenReturn(1L);
        when(cartRepository.findByUserId(anyLong())).thenReturn(Optional.of(testCart));
        when(cartItemRepository.findById(anyLong())).thenReturn(Optional.of(testCartItem));
        when(productServiceClient.checkAvailability(anyLong(), anyInt())).thenReturn(true);
        when(cartItemRepository.save(any(CartItem.class))).thenReturn(testCartItem);
        when(cartRepository.save(any(Cart.class))).thenReturn(testCart);

        CartResponse response = cartService.updateCartItem("test@example.com", 1L, request);

        assertThat(response).isNotNull();
        verify(cartItemRepository, times(1)).save(any(CartItem.class));
        verify(auditLogService, times(1)).logCartItemUpdated(anyLong(), anyLong(), anyInt());
    }

    @Test
    @DisplayName("Update Cart Item - Item Not Found")
    void testUpdateCartItem_ItemNotFound() {
        UpdateCartItemRequest request = new UpdateCartItemRequest();
        request.setQuantity(5);

        when(userServiceClient.getUserIdByEmail(anyString())).thenReturn(1L);
        when(cartRepository.findByUserId(anyLong())).thenReturn(Optional.of(testCart));
        when(cartItemRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> cartService.updateCartItem("test@example.com", 999L, request))
                .isInstanceOf(CartItemNotFoundException.class);

        verify(cartItemRepository, never()).save(any(CartItem.class));
    }

    @Test
    @DisplayName("Update Cart Item - Unauthorized Access")
    void testUpdateCartItem_UnauthorizedAccess() {
        UpdateCartItemRequest request = new UpdateCartItemRequest();
        request.setQuantity(5);

        Cart anotherCart = new Cart();
        anotherCart.setCartId(2L);
        anotherCart.setUserId(2L);
        testCartItem.setCart(anotherCart);

        when(userServiceClient.getUserIdByEmail(anyString())).thenReturn(1L);
        when(cartRepository.findByUserId(anyLong())).thenReturn(Optional.of(testCart));
        when(cartItemRepository.findById(anyLong())).thenReturn(Optional.of(testCartItem));

        assertThatThrownBy(() -> cartService.updateCartItem("test@example.com", 1L, request))
                .isInstanceOf(ForbiddenException.class);
    }

    @Test
    @DisplayName("Remove Item From Cart - Success")
    void testRemoveItemFromCart_Success() {
        testCart.getItems().add(testCartItem);

        when(userServiceClient.getUserIdByEmail(anyString())).thenReturn(1L);
        when(cartRepository.findByUserId(anyLong())).thenReturn(Optional.of(testCart));
        when(cartItemRepository.findById(anyLong())).thenReturn(Optional.of(testCartItem));
        when(cartRepository.save(any(Cart.class))).thenReturn(testCart);

        CartResponse response = cartService.removeItemFromCart("test@example.com", 1L);

        assertThat(response).isNotNull();
        verify(cartItemRepository, times(1)).delete(testCartItem);
        verify(auditLogService, times(1)).logCartItemRemoved(anyLong(), anyLong());
    }

    @Test
    @DisplayName("Remove Item From Cart - Item Not Found")
    void testRemoveItemFromCart_ItemNotFound() {
        when(userServiceClient.getUserIdByEmail(anyString())).thenReturn(1L);
        when(cartRepository.findByUserId(anyLong())).thenReturn(Optional.of(testCart));
        when(cartItemRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> cartService.removeItemFromCart("test@example.com", 999L))
                .isInstanceOf(CartItemNotFoundException.class);

        verify(cartItemRepository, never()).delete(any(CartItem.class));
    }

    @Test
    @DisplayName("Clear Cart - Success")
    void testClearCart_Success() {
        testCart.getItems().add(testCartItem);

        when(userServiceClient.getUserIdByEmail(anyString())).thenReturn(1L);
        when(cartRepository.findByUserId(anyLong())).thenReturn(Optional.of(testCart));

        MessageResponse response = cartService.clearCart("test@example.com");

        assertThat(response).isNotNull();
        assertThat(response.getMessage()).contains("cleared");

        verify(cartItemRepository, times(1)).deleteAll(anyList());
        verify(auditLogService, times(1)).logCartCleared(anyLong());
    }

    @Test
    @DisplayName("Clear Cart - Empty Cart")
    void testClearCart_EmptyCart() {
        when(userServiceClient.getUserIdByEmail(anyString())).thenReturn(1L);
        when(cartRepository.findByUserId(anyLong())).thenReturn(Optional.of(testCart));

        MessageResponse response = cartService.clearCart("test@example.com");

        assertThat(response).isNotNull();
        verify(cartItemRepository, times(1)).deleteAll(anyList());
    }

    @Test
    @DisplayName("Calculate Total - Multiple Items")
    void testCalculateTotal_MultipleItems() {
        CartItem item1 = new CartItem();
        item1.setQuantity(2);
        item1.setPrice(new BigDecimal("99.99"));

        CartItem item2 = new CartItem();
        item2.setQuantity(3);
        item2.setPrice(new BigDecimal("49.99"));

        testCart.getItems().add(item1);
        testCart.getItems().add(item2);

        when(userServiceClient.getUserIdByEmail(anyString())).thenReturn(1L);
        when(cartRepository.findByUserId(anyLong())).thenReturn(Optional.of(testCart));

        CartResponse response = cartService.getCart("test@example.com");

        BigDecimal expectedTotal = new BigDecimal("99.99")
                .multiply(new BigDecimal("2"))
                .add(new BigDecimal("49.99").multiply(new BigDecimal("3")));

        assertThat(response.getTotalAmount()).isEqualByComparingTo(expectedTotal);
        assertThat(response.getItemCount()).isEqualTo(2);
    }

    @Test
    @DisplayName("Service Client Failure - Circuit Breaker")
    void testServiceClientFailure_CircuitBreaker() {
        AddCartItemRequest request = new AddCartItemRequest();
        request.setProductId(1L);
        request.setQuantity(2);

        when(userServiceClient.getUserIdByEmail(anyString())).thenReturn(1L);
        when(cartRepository.findByUserId(anyLong())).thenReturn(Optional.of(testCart));
        when(productServiceClient.getProduct(anyLong()))
                .thenThrow(new RuntimeException("Service unavailable"));

        assertThatThrownBy(() -> cartService.addItemToCart("test@example.com", request))
                .isInstanceOf(RuntimeException.class);
    }
}