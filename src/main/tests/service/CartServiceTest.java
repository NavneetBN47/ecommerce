package com.ecommerce.shoppingcart.service;

import com.ecommerce.shoppingcart.dto.*;
import com.ecommerce.shoppingcart.entity.Cart;
import com.ecommerce.shoppingcart.entity.CartItem;
import com.ecommerce.shoppingcart.repository.CartRepository;
import com.ecommerce.shoppingcart.repository.CartItemRepository;
import com.ecommerce.shoppingcart.client.UserServiceClient;
import com.ecommerce.shoppingcart.client.ProductServiceClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Optional;
import java.util.UUID;

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

    @InjectMocks
    private CartService cartService;

    private Cart cart;
    private CartItem cartItem;
    private ProductDTO productDTO;

    @BeforeEach
    void setUp() {
        UUID userId = UUID.randomUUID();
        UUID productId = UUID.randomUUID();

        productDTO = ProductDTO.builder()
                .id(productId)
                .name("Laptop")
                .price(new BigDecimal("999.99"))
                .stock(50)
                .available(true)
                .build();

        cartItem = CartItem.builder()
                .id(UUID.randomUUID())
                .productId(productId)
                .quantity(2)
                .build();

        cart = Cart.builder()
                .id(UUID.randomUUID())
                .userId(userId)
                .items(Arrays.asList(cartItem))
                .build();

        cartItem.setCart(cart);
    }

    @Test
    @DisplayName("Should get cart successfully")
    void testGetCart_Success() {
        UUID userId = UUID.randomUUID();
        when(cartRepository.findByUserId(userId)).thenReturn(Optional.of(cart));
        when(productServiceClient.getProduct(any(UUID.class))).thenReturn(productDTO);

        CartDTO result = cartService.getCart(userId);

        assertThat(result).isNotNull();
        assertThat(result.getItems()).hasSize(1);
        assertThat(result.getTotalItems()).isEqualTo(2);

        verify(cartRepository, times(1)).findByUserId(userId);
    }

    @Test
    @DisplayName("Should create new cart when not exists")
    void testGetCart_CreateNew() {
        UUID userId = UUID.randomUUID();
        Cart newCart = Cart.builder()
                .id(UUID.randomUUID())
                .userId(userId)
                .items(Collections.emptyList())
                .build();

        when(cartRepository.findByUserId(userId)).thenReturn(Optional.empty());
        when(cartRepository.save(any(Cart.class))).thenReturn(newCart);

        CartDTO result = cartService.getCart(userId);

        assertThat(result).isNotNull();
        assertThat(result.getItems()).isEmpty();
        assertThat(result.getTotalItems()).isEqualTo(0);

        verify(cartRepository, times(1)).findByUserId(userId);
        verify(cartRepository, times(1)).save(any(Cart.class));
    }

    @Test
    @DisplayName("Should add item to cart successfully")
    void testAddItemToCart_Success() {
        UUID userId = UUID.randomUUID();
        AddCartItemDTO addDTO = AddCartItemDTO.builder()
                .productId(UUID.randomUUID())
                .quantity(1)
                .build();

        when(cartRepository.findByUserId(userId)).thenReturn(Optional.of(cart));
        when(userServiceClient.validateUser(userId)).thenReturn(true);
        when(productServiceClient.getProduct(any(UUID.class))).thenReturn(productDTO);
        when(productServiceClient.isProductAvailable(any(UUID.class), anyInt())).thenReturn(true);
        when(cartItemRepository.save(any(CartItem.class))).thenReturn(cartItem);
        when(cartRepository.save(any(Cart.class))).thenReturn(cart);

        CartDTO result = cartService.addItemToCart(userId, addDTO);

        assertThat(result).isNotNull();
        verify(cartRepository, times(1)).findByUserId(userId);
        verify(productServiceClient, times(1)).isProductAvailable(any(UUID.class), anyInt());
    }

    @Test
    @DisplayName("Should throw exception when adding unavailable product")
    void testAddItemToCart_ProductNotAvailable() {
        UUID userId = UUID.randomUUID();
        AddCartItemDTO addDTO = AddCartItemDTO.builder()
                .productId(UUID.randomUUID())
                .quantity(100)
                .build();

        when(cartRepository.findByUserId(userId)).thenReturn(Optional.of(cart));
        when(userServiceClient.validateUser(userId)).thenReturn(true);
        when(productServiceClient.getProduct(any(UUID.class))).thenReturn(productDTO);
        when(productServiceClient.isProductAvailable(any(UUID.class), anyInt())).thenReturn(false);

        assertThatThrownBy(() -> cartService.addItemToCart(userId, addDTO))
                .isInstanceOf(ProductNotAvailableException.class);

        verify(cartRepository, times(1)).findByUserId(userId);
        verify(productServiceClient, times(1)).isProductAvailable(any(UUID.class), anyInt());
        verify(cartItemRepository, never()).save(any(CartItem.class));
    }

    @Test
    @DisplayName("Should update cart item successfully")
    void testUpdateCartItem_Success() {
        UUID userId = UUID.randomUUID();
        UUID itemId = cartItem.getId();
        UpdateCartItemDTO updateDTO = UpdateCartItemDTO.builder()
                .quantity(5)
                .build();

        when(cartRepository.findByUserId(userId)).thenReturn(Optional.of(cart));
        when(cartItemRepository.findById(itemId)).thenReturn(Optional.of(cartItem));
        when(productServiceClient.getProduct(any(UUID.class))).thenReturn(productDTO);
        when(productServiceClient.isProductAvailable(any(UUID.class), anyInt())).thenReturn(true);
        when(cartItemRepository.save(any(CartItem.class))).thenReturn(cartItem);

        CartDTO result = cartService.updateCartItem(userId, itemId, updateDTO);

        assertThat(result).isNotNull();
        verify(cartItemRepository, times(1)).findById(itemId);
        verify(productServiceClient, times(1)).isProductAvailable(any(UUID.class), eq(5));
    }

    @Test
    @DisplayName("Should throw exception when updating non-existent cart item")
    void testUpdateCartItem_NotFound() {
        UUID userId = UUID.randomUUID();
        UUID itemId = UUID.randomUUID();
        UpdateCartItemDTO updateDTO = UpdateCartItemDTO.builder()
                .quantity(5)
                .build();

        when(cartRepository.findByUserId(userId)).thenReturn(Optional.of(cart));
        when(cartItemRepository.findById(itemId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> cartService.updateCartItem(userId, itemId, updateDTO))
                .isInstanceOf(CartItemNotFoundException.class);

        verify(cartItemRepository, times(1)).findById(itemId);
        verify(cartItemRepository, never()).save(any(CartItem.class));
    }

    @Test
    @DisplayName("Should remove cart item successfully")
    void testRemoveCartItem_Success() {
        UUID userId = UUID.randomUUID();
        UUID itemId = cartItem.getId();

        when(cartRepository.findByUserId(userId)).thenReturn(Optional.of(cart));
        when(cartItemRepository.findById(itemId)).thenReturn(Optional.of(cartItem));
        doNothing().when(cartItemRepository).delete(any(CartItem.class));
        when(productServiceClient.getProduct(any(UUID.class))).thenReturn(productDTO);

        CartDTO result = cartService.removeCartItem(userId, itemId);

        assertThat(result).isNotNull();
        verify(cartItemRepository, times(1)).findById(itemId);
        verify(cartItemRepository, times(1)).delete(cartItem);
    }

    @Test
    @DisplayName("Should clear cart successfully")
    void testClearCart_Success() {
        UUID userId = UUID.randomUUID();
        when(cartRepository.findByUserId(userId)).thenReturn(Optional.of(cart));
        doNothing().when(cartItemRepository).deleteAll(anyList());

        cartService.clearCart(userId);

        verify(cartRepository, times(1)).findByUserId(userId);
        verify(cartItemRepository, times(1)).deleteAll(anyList());
    }

    @Test
    @DisplayName("Should calculate cart total correctly")
    void testCalculateCartTotal() {
        when(productServiceClient.getProduct(any(UUID.class))).thenReturn(productDTO);

        CartDTO result = cartService.getCart(cart.getUserId());

        BigDecimal expectedTotal = new BigDecimal("999.99").multiply(new BigDecimal("2"));
        assertThat(result.getTotalPrice()).isEqualByComparingTo(expectedTotal);
    }

    @Test
    @DisplayName("Should validate user before cart operations")
    void testAddItemToCart_InvalidUser() {
        UUID userId = UUID.randomUUID();
        AddCartItemDTO addDTO = AddCartItemDTO.builder()
                .productId(UUID.randomUUID())
                .quantity(1)
                .build();

        when(cartRepository.findByUserId(userId)).thenReturn(Optional.of(cart));
        when(userServiceClient.validateUser(userId)).thenReturn(false);

        assertThatThrownBy(() -> cartService.addItemToCart(userId, addDTO))
                .isInstanceOf(UserNotFoundException.class);

        verify(userServiceClient, times(1)).validateUser(userId);
        verify(productServiceClient, never()).getProduct(any(UUID.class));
    }
}