package com.ecommerce.shoppingcart.application.service;

import com.ecommerce.shoppingcart.application.dto.*;
import com.ecommerce.shoppingcart.application.exception.*;
import com.ecommerce.shoppingcart.domain.entity.Cart;
import com.ecommerce.shoppingcart.domain.entity.CartItem;
import com.ecommerce.shoppingcart.domain.repository.CartItemRepository;
import com.ecommerce.shoppingcart.domain.repository.CartRepository;
import com.ecommerce.shoppingcart.infrastructure.client.ProductServiceClient;
import com.ecommerce.shoppingcart.infrastructure.client.UserServiceClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
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
    private AddCartItemRequest addItemRequest;
    private UpdateCartItemRequest updateItemRequest;

    @BeforeEach
    void setUp() {
        testCartItem = CartItem.builder()
                .id(1L)
                .productId(101L)
                .quantity(2)
                .unitPrice(new BigDecimal("99.99"))
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        testCart = Cart.builder()
                .id(1L)
                .userId(1L)
                .items(Arrays.asList(testCartItem))
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        testCartItem.setCart(testCart);

        testProduct = ProductResponse.builder()
                .productId(101L)
                .name("Test Product")
                .description("Test description")
                .category("Electronics")
                .price(new BigDecimal("99.99"))
                .stockQuantity(100)
                .isAvailable(true)
                .build();

        addItemRequest = AddCartItemRequest.builder()
                .productId(101L)
                .quantity(2)
                .build();

        updateItemRequest = UpdateCartItemRequest.builder()
                .quantity(5)
                .build();
    }

    @Test
    @DisplayName("Get Cart By User ID - Existing Cart - Success")
    void testGetCartByUserId_ExistingCart_Success() {
        when(cartRepository.findByUserId(1L)).thenReturn(Optional.of(testCart));
        when(productServiceClient.getProductById(101L)).thenReturn(testProduct);

        CartResponse response = cartService.getCartByUserId(1L);

        assertThat(response).isNotNull();
        assertThat(response.getCartId()).isEqualTo(1L);
        assertThat(response.getUserId()).isEqualTo(1L);
        assertThat(response.getItems()).hasSize(1);
        assertThat(response.getTotalItems()).isEqualTo(2);
        assertThat(response.getTotalPrice()).isEqualByComparingTo(new BigDecimal("199.98"));

        verify(cartRepository, times(1)).findByUserId(1L);
        verify(productServiceClient, times(1)).getProductById(101L);
    }

    @Test
    @DisplayName("Get Cart By User ID - New Cart - Creates Empty Cart")
    void testGetCartByUserId_NewCart_CreatesEmptyCart() {
        when(cartRepository.findByUserId(1L)).thenReturn(Optional.empty());
        when(userServiceClient.validateUser(1L)).thenReturn(true);
        when(cartRepository.save(any(Cart.class))).thenReturn(testCart);

        CartResponse response = cartService.getCartByUserId(1L);

        assertThat(response).isNotNull();
        assertThat(response.getUserId()).isEqualTo(1L);
        assertThat(response.getItems()).isEmpty();
        assertThat(response.getTotalItems()).isEqualTo(0);
        assertThat(response.getTotalPrice()).isEqualByComparingTo(BigDecimal.ZERO);

        verify(cartRepository, times(1)).findByUserId(1L);
        verify(userServiceClient, times(1)).validateUser(1L);
        verify(cartRepository, times(1)).save(any(Cart.class));
    }

    @Test
    @DisplayName("Get Cart By User ID - Invalid User - Throws Exception")
    void testGetCartByUserId_InvalidUser_ThrowsException() {
        when(cartRepository.findByUserId(999L)).thenReturn(Optional.empty());
        when(userServiceClient.validateUser(999L)).thenReturn(false);

        assertThatThrownBy(() -> cartService.getCartByUserId(999L))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessageContaining("User not found");

        verify(cartRepository, times(1)).findByUserId(999L);
        verify(userServiceClient, times(1)).validateUser(999L);
        verify(cartRepository, never()).save(any(Cart.class));
    }

    @Test
    @DisplayName("Add Item To Cart - Valid Request - Success")
    void testAddItemToCart_ValidRequest_Success() {
        when(cartRepository.findByUserId(1L)).thenReturn(Optional.of(testCart));
        when(productServiceClient.getProductById(101L)).thenReturn(testProduct);
        when(productServiceClient.checkAvailability(101L, 2)).thenReturn(true);
        when(cartRepository.save(any(Cart.class))).thenReturn(testCart);

        CartResponse response = cartService.addItemToCart(1L, addItemRequest);

        assertThat(response).isNotNull();
        assertThat(response.getCartId()).isEqualTo(1L);

        verify(cartRepository, times(1)).findByUserId(1L);
        verify(productServiceClient, times(1)).getProductById(101L);
        verify(productServiceClient, times(1)).checkAvailability(101L, 2);
        verify(cartRepository, times(1)).save(any(Cart.class));
        verify(auditLogService, times(1)).logCartItemAdded(anyLong(), anyLong(), anyInt());
    }

    @Test
    @DisplayName("Add Item To Cart - Product Not Found - Throws Exception")
    void testAddItemToCart_ProductNotFound_ThrowsException() {
        when(cartRepository.findByUserId(1L)).thenReturn(Optional.of(testCart));
        when(productServiceClient.getProductById(999L))
                .thenThrow(new ProductNotFoundException("Product not found"));

        AddCartItemRequest invalidRequest = AddCartItemRequest.builder()
                .productId(999L)
                .quantity(2)
                .build();

        assertThatThrownBy(() -> cartService.addItemToCart(1L, invalidRequest))
                .isInstanceOf(ProductNotFoundException.class)
                .hasMessageContaining("Product not found");

        verify(cartRepository, times(1)).findByUserId(1L);
        verify(productServiceClient, times(1)).getProductById(999L);
        verify(cartRepository, never()).save(any(Cart.class));
    }

    @Test
    @DisplayName("Add Item To Cart - Insufficient Stock - Throws Exception")
    void testAddItemToCart_InsufficientStock_ThrowsException() {
        when(cartRepository.findByUserId(1L)).thenReturn(Optional.of(testCart));
        when(productServiceClient.getProductById(101L)).thenReturn(testProduct);
        when(productServiceClient.checkAvailability(101L, 200)).thenReturn(false);

        AddCartItemRequest invalidRequest = AddCartItemRequest.builder()
                .productId(101L)
                .quantity(200)
                .build();

        assertThatThrownBy(() -> cartService.addItemToCart(1L, invalidRequest))
                .isInstanceOf(InsufficientStockException.class)
                .hasMessageContaining("Insufficient stock");

        verify(cartRepository, times(1)).findByUserId(1L);
        verify(productServiceClient, times(1)).getProductById(101L);
        verify(productServiceClient, times(1)).checkAvailability(101L, 200);
        verify(cartRepository, never()).save(any(Cart.class));
    }

    @Test
    @DisplayName("Add Item To Cart - Product Not Available - Throws Exception")
    void testAddItemToCart_ProductNotAvailable_ThrowsException() {
        ProductResponse unavailableProduct = ProductResponse.builder()
                .productId(101L)
                .name("Test Product")
                .price(new BigDecimal("99.99"))
                .stockQuantity(0)
                .isAvailable(false)
                .build();

        when(cartRepository.findByUserId(1L)).thenReturn(Optional.of(testCart));
        when(productServiceClient.getProductById(101L)).thenReturn(unavailableProduct);

        assertThatThrownBy(() -> cartService.addItemToCart(1L, addItemRequest))
                .isInstanceOf(ProductNotAvailableException.class)
                .hasMessageContaining("not available");

        verify(cartRepository, times(1)).findByUserId(1L);
        verify(productServiceClient, times(1)).getProductById(101L);
        verify(cartRepository, never()).save(any(Cart.class));
    }

    @Test
    @DisplayName("Update Cart Item - Valid Request - Success")
    void testUpdateCartItem_ValidRequest_Success() {
        when(cartItemRepository.findById(1L)).thenReturn(Optional.of(testCartItem));
        when(productServiceClient.checkAvailability(101L, 5)).thenReturn(true);
        when(cartRepository.save(any(Cart.class))).thenReturn(testCart);
        when(productServiceClient.getProductById(101L)).thenReturn(testProduct);

        CartResponse response = cartService.updateCartItem(1L, 1L, updateItemRequest);

        assertThat(response).isNotNull();
        assertThat(response.getCartId()).isEqualTo(1L);

        verify(cartItemRepository, times(1)).findById(1L);
        verify(productServiceClient, times(1)).checkAvailability(101L, 5);
        verify(cartRepository, times(1)).save(any(Cart.class));
        verify(auditLogService, times(1)).logCartItemUpdated(anyLong(), anyLong(), anyInt());
    }

    @Test
    @DisplayName("Update Cart Item - Item Not Found - Throws Exception")
    void testUpdateCartItem_ItemNotFound_ThrowsException() {
        when(cartItemRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> cartService.updateCartItem(1L, 999L, updateItemRequest))
                .isInstanceOf(CartItemNotFoundException.class)
                .hasMessageContaining("Cart item not found");

        verify(cartItemRepository, times(1)).findById(999L);
        verify(cartRepository, never()).save(any(Cart.class));
    }

    @Test
    @DisplayName("Update Cart Item - Different User - Throws Exception")
    void testUpdateCartItem_DifferentUser_ThrowsException() {
        when(cartItemRepository.findById(1L)).thenReturn(Optional.of(testCartItem));

        assertThatThrownBy(() -> cartService.updateCartItem(2L, 1L, updateItemRequest))
                .isInstanceOf(ForbiddenException.class)
                .hasMessageContaining("does not own this cart item");

        verify(cartItemRepository, times(1)).findById(1L);
        verify(cartRepository, never()).save(any(Cart.class));
    }

    @Test
    @DisplayName("Update Cart Item - Invalid Quantity - Throws Exception")
    void testUpdateCartItem_InvalidQuantity_ThrowsException() {
        UpdateCartItemRequest invalidRequest = UpdateCartItemRequest.builder()
                .quantity(0)
                .build();

        when(cartItemRepository.findById(1L)).thenReturn(Optional.of(testCartItem));

        assertThatThrownBy(() -> cartService.updateCartItem(1L, 1L, invalidRequest))
                .isInstanceOf(InvalidQuantityException.class)
                .hasMessageContaining("Quantity must be greater than 0");

        verify(cartItemRepository, times(1)).findById(1L);
        verify(cartRepository, never()).save(any(Cart.class));
    }

    @Test
    @DisplayName("Remove Item From Cart - Valid Request - Success")
    void testRemoveItemFromCart_ValidRequest_Success() {
        when(cartItemRepository.findById(1L)).thenReturn(Optional.of(testCartItem));
        when(cartRepository.save(any(Cart.class))).thenReturn(testCart);
        when(productServiceClient.getProductById(101L)).thenReturn(testProduct);

        CartResponse response = cartService.removeItemFromCart(1L, 1L);

        assertThat(response).isNotNull();

        verify(cartItemRepository, times(1)).findById(1L);
        verify(cartItemRepository, times(1)).delete(testCartItem);
        verify(auditLogService, times(1)).logCartItemRemoved(anyLong(), anyLong());
    }

    @Test
    @DisplayName("Remove Item From Cart - Item Not Found - Throws Exception")
    void testRemoveItemFromCart_ItemNotFound_ThrowsException() {
        when(cartItemRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> cartService.removeItemFromCart(1L, 999L))
                .isInstanceOf(CartItemNotFoundException.class)
                .hasMessageContaining("Cart item not found");

        verify(cartItemRepository, times(1)).findById(999L);
        verify(cartItemRepository, never()).delete(any(CartItem.class));
    }

    @Test
    @DisplayName("Remove Item From Cart - Different User - Throws Exception")
    void testRemoveItemFromCart_DifferentUser_ThrowsException() {
        when(cartItemRepository.findById(1L)).thenReturn(Optional.of(testCartItem));

        assertThatThrownBy(() -> cartService.removeItemFromCart(2L, 1L))
                .isInstanceOf(ForbiddenException.class)
                .hasMessageContaining("does not own this cart item");

        verify(cartItemRepository, times(1)).findById(1L);
        verify(cartItemRepository, never()).delete(any(CartItem.class));
    }

    @Test
    @DisplayName("Clear Cart - Valid Request - Success")
    void testClearCart_ValidRequest_Success() {
        when(cartRepository.findByUserId(1L)).thenReturn(Optional.of(testCart));

        cartService.clearCart(1L);

        verify(cartRepository, times(1)).findByUserId(1L);
        verify(cartItemRepository, times(1)).deleteAll(testCart.getItems());
        verify(auditLogService, times(1)).logCartCleared(1L);
    }

    @Test
    @DisplayName("Clear Cart - Cart Not Found - Throws Exception")
    void testClearCart_CartNotFound_ThrowsException() {
        when(cartRepository.findByUserId(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> cartService.clearCart(999L))
                .isInstanceOf(CartNotFoundException.class)
                .hasMessageContaining("Cart not found");

        verify(cartRepository, times(1)).findByUserId(999L);
        verify(cartItemRepository, never()).deleteAll(anyList());
    }
}