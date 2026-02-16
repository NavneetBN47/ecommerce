package com.ecommerce.service;

import com.ecommerce.dto.AddToCartRequest;
import com.ecommerce.dto.CartResponse;
import com.ecommerce.dto.UpdateCartItemRequest;
import com.ecommerce.entity.Cart;
import com.ecommerce.entity.CartItem;
import com.ecommerce.entity.Product;
import com.ecommerce.entity.User;
import com.ecommerce.repository.CartItemRepository;
import com.ecommerce.repository.CartRepository;
import com.ecommerce.repository.ProductRepository;
import com.ecommerce.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Test class for CartService
 * 
 * Tests cart operations including add, update, remove items and cart retrieval
 * 
 * @author QA Automation Agent
 * @version 1.0.0
 */
@ExtendWith(MockitoExtension.class)
class test_CartService {

    @Mock
    private CartRepository cartRepository;

    @Mock
    private CartItemRepository cartItemRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CartService cartService;

    private User user;
    private Product product;
    private Cart cart;
    private CartItem cartItem;
    private UUID userId;
    private UUID productId;
    private UUID cartId;
    private UUID itemId;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        productId = UUID.randomUUID();
        cartId = UUID.randomUUID();
        itemId = UUID.randomUUID();

        user = User.builder()
            .id(userId)
            .username("testuser")
            .email("test@example.com")
            .build();

        product = Product.builder()
            .id(productId)
            .name("Test Product")
            .price(BigDecimal.valueOf(99.99))
            .availableQty(10)
            .build();

        cart = Cart.builder()
            .id(cartId)
            .user(user)
            .items(new ArrayList<>())
            .build();

        cartItem = CartItem.builder()
            .id(itemId)
            .cart(cart)
            .product(product)
            .quantity(2)
            .build();
    }

    /**
     * Test adding product to new cart
     * 
     * Verifies:
     * - New cart is created if not exists
     * - Product is added to cart
     * - CartResponse is returned with correct data
     */
    @Test
    void testAddProductToCart_NewCart() {
        // Given
        AddToCartRequest request = new AddToCartRequest();
        request.setProductId(productId);
        request.setQuantity(2);

        when(userRepository.findById(any(UUID.class))).thenReturn(Optional.of(user));
        when(productRepository.findById(any(UUID.class))).thenReturn(Optional.of(product));
        when(cartRepository.findByUserId(any(UUID.class))).thenReturn(Optional.empty());
        when(cartRepository.save(any(Cart.class))).thenReturn(cart);
        when(cartItemRepository.findByCartIdAndProductId(any(UUID.class), any(UUID.class)))
            .thenReturn(Optional.empty());
        when(cartItemRepository.save(any(CartItem.class))).thenReturn(cartItem);

        // When
        CartResponse response = cartService.addProductToCart(userId, request);

        // Then
        assertNotNull(response, "Response should not be null");
        verify(cartRepository, times(1)).save(any(Cart.class));
        verify(cartItemRepository, times(1)).save(any(CartItem.class));
    }

    /**
     * Test adding product to existing cart
     * 
     * Verifies:
     * - Product is added to existing cart
     * - No new cart is created
     */
    @Test
    void testAddProductToCart_ExistingCart() {
        // Given
        AddToCartRequest request = new AddToCartRequest();
        request.setProductId(productId);
        request.setQuantity(2);

        when(userRepository.findById(any(UUID.class))).thenReturn(Optional.of(user));
        when(productRepository.findById(any(UUID.class))).thenReturn(Optional.of(product));
        when(cartRepository.findByUserId(any(UUID.class))).thenReturn(Optional.of(cart));
        when(cartItemRepository.findByCartIdAndProductId(any(UUID.class), any(UUID.class)))
            .thenReturn(Optional.empty());
        when(cartItemRepository.save(any(CartItem.class))).thenReturn(cartItem);

        // When
        CartResponse response = cartService.addProductToCart(userId, request);

        // Then
        assertNotNull(response, "Response should not be null");
        verify(cartRepository, never()).save(any(Cart.class));
        verify(cartItemRepository, times(1)).save(any(CartItem.class));
    }

    /**
     * Test adding existing product increases quantity
     * 
     * Verifies:
     * - Quantity is increased for existing cart item
     * - No new cart item is created
     */
    @Test
    void testAddProductToCart_ExistingProduct() {
        // Given
        AddToCartRequest request = new AddToCartRequest();
        request.setProductId(productId);
        request.setQuantity(3);

        when(userRepository.findById(any(UUID.class))).thenReturn(Optional.of(user));
        when(productRepository.findById(any(UUID.class))).thenReturn(Optional.of(product));
        when(cartRepository.findByUserId(any(UUID.class))).thenReturn(Optional.of(cart));
        when(cartItemRepository.findByCartIdAndProductId(any(UUID.class), any(UUID.class)))
            .thenReturn(Optional.of(cartItem));
        when(cartItemRepository.save(any(CartItem.class))).thenReturn(cartItem);

        // When
        CartResponse response = cartService.addProductToCart(userId, request);

        // Then
        assertNotNull(response, "Response should not be null");
        assertEquals(5, cartItem.getQuantity(), "Quantity should be increased");
        verify(cartItemRepository, times(1)).save(cartItem);
    }

    /**
     * Test adding product with invalid quantity
     * 
     * Verifies:
     * - ValidationException is thrown for zero or negative quantity
     */
    @Test
    void testAddProductToCart_InvalidQuantity() {
        // Given
        AddToCartRequest request = new AddToCartRequest();
        request.setProductId(productId);
        request.setQuantity(0);

        when(userRepository.findById(any(UUID.class))).thenReturn(Optional.of(user));
        when(productRepository.findById(any(UUID.class))).thenReturn(Optional.of(product));

        // When/Then
        assertThrows(Exception.class, () -> {
            cartService.addProductToCart(userId, request);
        });
    }

    /**
     * Test adding non-existent product
     * 
     * Verifies:
     * - ResourceNotFoundException is thrown for non-existent product
     */
    @Test
    void testAddProductToCart_ProductNotFound() {
        // Given
        AddToCartRequest request = new AddToCartRequest();
        request.setProductId(productId);
        request.setQuantity(2);

        when(userRepository.findById(any(UUID.class))).thenReturn(Optional.of(user));
        when(productRepository.findById(any(UUID.class))).thenReturn(Optional.empty());

        // When/Then
        assertThrows(Exception.class, () -> {
            cartService.addProductToCart(userId, request);
        });
    }

    /**
     * Test updating cart item quantity
     * 
     * Verifies:
     * - Cart item quantity is updated
     * - CartResponse is returned with updated data
     */
    @Test
    void testUpdateCartItem_Success() {
        // Given
        UpdateCartItemRequest request = new UpdateCartItemRequest();
        request.setQuantity(5);

        cart.addItem(cartItem);
        when(cartRepository.findByUserId(any(UUID.class))).thenReturn(Optional.of(cart));
        when(cartItemRepository.findById(any(UUID.class))).thenReturn(Optional.of(cartItem));
        when(cartItemRepository.save(any(CartItem.class))).thenReturn(cartItem);

        // When
        CartResponse response = cartService.updateCartItem(userId, itemId, request);

        // Then
        assertNotNull(response, "Response should not be null");
        assertEquals(5, cartItem.getQuantity(), "Quantity should be updated");
        verify(cartItemRepository, times(1)).save(cartItem);
    }

    /**
     * Test updating cart item with invalid quantity
     * 
     * Verifies:
     * - ValidationException is thrown for invalid quantity
     */
    @Test
    void testUpdateCartItem_InvalidQuantity() {
        // Given
        UpdateCartItemRequest request = new UpdateCartItemRequest();
        request.setQuantity(-1);

        when(cartRepository.findByUserId(any(UUID.class))).thenReturn(Optional.of(cart));
        when(cartItemRepository.findById(any(UUID.class))).thenReturn(Optional.of(cartItem));

        // When/Then
        assertThrows(Exception.class, () -> {
            cartService.updateCartItem(userId, itemId, request);
        });
    }

    /**
     * Test removing cart item
     * 
     * Verifies:
     * - Cart item is removed
     * - CartResponse is returned with updated cart
     */
    @Test
    void testRemoveCartItem_Success() {
        // Given
        cart.addItem(cartItem);
        CartItem anotherItem = CartItem.builder()
            .id(UUID.randomUUID())
            .cart(cart)
            .product(product)
            .quantity(1)
            .build();
        cart.addItem(anotherItem);

        when(cartRepository.findByUserId(any(UUID.class))).thenReturn(Optional.of(cart));
        when(cartItemRepository.findById(any(UUID.class))).thenReturn(Optional.of(cartItem));
        doNothing().when(cartItemRepository).delete(any(CartItem.class));

        // When
        CartResponse response = cartService.removeCartItem(userId, itemId);

        // Then
        assertNotNull(response, "Response should not be null");
        verify(cartItemRepository, times(1)).delete(cartItem);
    }

    /**
     * Test removing last cart item deletes cart
     * 
     * Verifies:
     * - Cart is deleted when last item is removed
     * - Null response is returned
     */
    @Test
    void testRemoveCartItem_LastItem() {
        // Given
        cart.addItem(cartItem);
        when(cartRepository.findByUserId(any(UUID.class))).thenReturn(Optional.of(cart));
        when(cartItemRepository.findById(any(UUID.class))).thenReturn(Optional.of(cartItem));
        doNothing().when(cartItemRepository).delete(any(CartItem.class));
        doNothing().when(cartRepository).delete(any(Cart.class));

        // When
        CartResponse response = cartService.removeCartItem(userId, itemId);

        // Then
        assertNull(response, "Response should be null for empty cart");
        verify(cartRepository, times(1)).delete(cart);
    }

    /**
     * Test getting cart
     * 
     * Verifies:
     * - Cart is retrieved with all items
     * - CartResponse contains correct data
     */
    @Test
    void testGetCart_Success() {
        // Given
        cart.addItem(cartItem);
        when(cartRepository.findByUserId(any(UUID.class))).thenReturn(Optional.of(cart));

        // When
        CartResponse response = cartService.getCart(userId);

        // Then
        assertNotNull(response, "Response should not be null");
        assertNotNull(response.getItems(), "Items should not be null");
        assertFalse(response.getItems().isEmpty(), "Items should not be empty");
        verify(cartRepository, times(1)).findByUserId(userId);
    }

    /**
     * Test getting non-existent cart
     * 
     * Verifies:
     * - ResourceNotFoundException is thrown for non-existent cart
     */
    @Test
    void testGetCart_NotFound() {
        // Given
        when(cartRepository.findByUserId(any(UUID.class))).thenReturn(Optional.empty());

        // When/Then
        assertThrows(Exception.class, () -> {
            cartService.getCart(userId);
        });
    }

    /**
     * Test clearing cart
     * 
     * Verifies:
     * - Cart is deleted
     * - All cart items are removed
     */
    @Test
    void testClearCart_Success() {
        // Given
        doNothing().when(cartRepository).deleteByUserId(any(UUID.class));

        // When
        cartService.clearCart(userId);

        // Then
        verify(cartRepository, times(1)).deleteByUserId(userId);
    }
}
