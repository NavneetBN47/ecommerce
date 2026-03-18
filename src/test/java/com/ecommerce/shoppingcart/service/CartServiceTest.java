package com.ecommerce.shoppingcart.service;

import com.ecommerce.shoppingcart.dto.AddToCartRequest;
import com.ecommerce.shoppingcart.dto.CartResponse;
import com.ecommerce.shoppingcart.entity.Cart;
import com.ecommerce.shoppingcart.entity.CartItem;
import com.ecommerce.shoppingcart.repository.CartRepository;
import com.ecommerce.shoppingcart.repository.CartItemRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CartServiceTest {

    @Mock
    private CartRepository cartRepository;

    @Mock
    private CartItemRepository cartItemRepository;

    @Mock
    private ProductServiceClient productServiceClient;

    @InjectMocks
    private CartService cartService;

    private Cart cart;
    private CartItem cartItem;
    private AddToCartRequest addToCartRequest;

    @BeforeEach
    void setUp() {
        cart = new Cart();
        cart.setId(1L);
        cart.setUserId(1L);
        cart.setItems(new ArrayList<>());
        cart.setTotalAmount(BigDecimal.ZERO);

        cartItem = new CartItem();
        cartItem.setId(1L);
        cartItem.setProductId(1L);
        cartItem.setQuantity(2);
        cartItem.setPrice(new BigDecimal("99.99"));
        cartItem.setCart(cart);

        addToCartRequest = new AddToCartRequest();
        addToCartRequest.setUserId(1L);
        addToCartRequest.setProductId(1L);
        addToCartRequest.setQuantity(2);
    }

    @Test
    @DisplayName("Add to Cart - New Cart")
    void testAddToCart_NewCart() {
        when(cartRepository.findByUserId(anyLong())).thenReturn(Optional.empty());
        when(cartRepository.save(any(Cart.class))).thenReturn(cart);
        when(cartItemRepository.save(any(CartItem.class))).thenReturn(cartItem);

        CartResponse response = cartService.addToCart(addToCartRequest);

        assertNotNull(response);
        assertEquals(1L, response.getUserId());
        verify(cartRepository, times(2)).save(any(Cart.class));
    }

    @Test
    @DisplayName("Add to Cart - Existing Cart")
    void testAddToCart_ExistingCart() {
        cart.getItems().add(cartItem);
        when(cartRepository.findByUserId(anyLong())).thenReturn(Optional.of(cart));
        when(cartRepository.save(any(Cart.class))).thenReturn(cart);
        when(cartItemRepository.save(any(CartItem.class))).thenReturn(cartItem);

        CartResponse response = cartService.addToCart(addToCartRequest);

        assertNotNull(response);
        assertEquals(1L, response.getUserId());
    }

    @Test
    @DisplayName("Get Cart - Success")
    void testGetCart_Success() {
        when(cartRepository.findByUserId(anyLong())).thenReturn(Optional.of(cart));

        CartResponse response = cartService.getCart(1L);

        assertNotNull(response);
        assertEquals(1L, response.getUserId());
    }

    @Test
    @DisplayName("Get Cart - Not Found")
    void testGetCart_NotFound() {
        when(cartRepository.findByUserId(anyLong())).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> cartService.getCart(999L));
    }

    @Test
    @DisplayName("Update Cart Item - Success")
    void testUpdateCartItem_Success() {
        cart.getItems().add(cartItem);
        when(cartRepository.findByUserId(anyLong())).thenReturn(Optional.of(cart));
        when(cartRepository.save(any(Cart.class))).thenReturn(cart);

        CartResponse response = cartService.updateCartItem(1L, 1L, 5);

        assertNotNull(response);
        verify(cartRepository, times(1)).save(any(Cart.class));
    }

    @Test
    @DisplayName("Remove from Cart - Success")
    void testRemoveFromCart_Success() {
        cart.getItems().add(cartItem);
        when(cartRepository.findByUserId(anyLong())).thenReturn(Optional.of(cart));
        when(cartRepository.save(any(Cart.class))).thenReturn(cart);
        doNothing().when(cartItemRepository).delete(any(CartItem.class));

        CartResponse response = cartService.removeFromCart(1L, 1L);

        assertNotNull(response);
        verify(cartItemRepository, times(1)).delete(any(CartItem.class));
    }

    @Test
    @DisplayName("Clear Cart - Success")
    void testClearCart_Success() {
        when(cartRepository.findByUserId(anyLong())).thenReturn(Optional.of(cart));
        doNothing().when(cartRepository).delete(any(Cart.class));

        cartService.clearCart(1L);

        verify(cartRepository, times(1)).delete(any(Cart.class));
    }

    @Test
    @DisplayName("Calculate Total - Success")
    void testCalculateTotal_Success() {
        cart.getItems().add(cartItem);
        
        BigDecimal total = cartService.calculateTotal(cart);

        assertNotNull(total);
        assertEquals(new BigDecimal("199.98"), total);
    }
}
