package com.ecommerce.service;

import com.ecommerce.dto.AddToCartRequest;
import com.ecommerce.dto.CartResponse;
import com.ecommerce.dto.UpdateCartItemRequest;
import com.ecommerce.entity.Cart;
import com.ecommerce.entity.CartItem;
import com.ecommerce.entity.Product;
import com.ecommerce.entity.User;
import com.ecommerce.exception.ResourceNotFoundException;
import com.ecommerce.exception.ValidationException;
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
 * JUnit 5 test class for CartService
 * Tests shopping cart operations including add, update, remove, and retrieve
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

    private UUID userId;
    private UUID productId;
    private UUID cartId;
    private UUID itemId;
    private User testUser;
    private Product testProduct;
    private Cart testCart;
    private CartItem testCartItem;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        productId = UUID.randomUUID();
        cartId = UUID.randomUUID();
        itemId = UUID.randomUUID();

        testUser = User.builder()
            .id(userId)
            .username("testuser")
            .email("test@example.com")
            .build();

        testProduct = Product.builder()
            .id(productId)
            .name("Test Product")
            .price(BigDecimal.valueOf(50.00))
            .availableQty(100)
            .build();

        testCart = Cart.builder()
            .id(cartId)
            .user(testUser)
            .items(new ArrayList<>())
            .build();

        testCartItem = CartItem.builder()
            .id(itemId)
            .cart(testCart)
            .product(testProduct)
            .quantity(2)
            .build();
    }

    /**
     * Test adding product to new cart
     * Verifies lazy cart creation and product addition
     */
    @Test
    void addProductToCartShouldCreateNewCartIfNotExists() {
        // Given
        AddToCartRequest request = new AddToCartRequest();
        request.setProductId(productId);
        request.setQuantity(2);

        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(productRepository.findById(productId)).thenReturn(Optional.of(testProduct));
        when(cartRepository.findByUserId(userId)).thenReturn(Optional.empty());
        when(cartRepository.save(any(Cart.class))).thenReturn(testCart);
        when(cartItemRepository.findByCartIdAndProductId(any(), any())).thenReturn(Optional.empty());
        when(cartItemRepository.save(any(CartItem.class))).thenReturn(testCartItem);

        // When
        CartResponse response = cartService.addProductToCart(userId, request);

        // Then
        assertNotNull(response);
        verify(cartRepository, times(1)).save(any(Cart.class));
        verify(cartItemRepository, times(1)).save(any(CartItem.class));
    }

    /**
     * Test adding product to existing cart
     * Verifies product is added to existing cart
     */
    @Test
    void addProductToCartShouldAddToExistingCart() {
        // Given
        AddToCartRequest request = new AddToCartRequest();
        request.setProductId(productId);
        request.setQuantity(2);

        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(productRepository.findById(productId)).thenReturn(Optional.of(testProduct));
        when(cartRepository.findByUserId(userId)).thenReturn(Optional.of(testCart));
        when(cartItemRepository.findByCartIdAndProductId(cartId, productId)).thenReturn(Optional.empty());
        when(cartItemRepository.save(any(CartItem.class))).thenReturn(testCartItem);

        // When
        CartResponse response = cartService.addProductToCart(userId, request);

        // Then
        assertNotNull(response);
        verify(cartRepository, never()).save(any(Cart.class));
        verify(cartItemRepository, times(1)).save(any(CartItem.class));
    }

    /**
     * Test adding existing product increases quantity
     * Verifies quantity update for existing cart item
     */
    @Test
    void addProductToCartShouldIncreaseQuantityForExistingProduct() {
        // Given
        AddToCartRequest request = new AddToCartRequest();
        request.setProductId(productId);
        request.setQuantity(3);

        testCart.getItems().add(testCartItem);

        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(productRepository.findById(productId)).thenReturn(Optional.of(testProduct));
        when(cartRepository.findByUserId(userId)).thenReturn(Optional.of(testCart));
        when(cartItemRepository.findByCartIdAndProductId(cartId, productId))
            .thenReturn(Optional.of(testCartItem));
        when(cartItemRepository.save(any(CartItem.class))).thenReturn(testCartItem);

        // When
        CartResponse response = cartService.addProductToCart(userId, request);

        // Then
        assertNotNull(response);
        assertEquals(5, testCartItem.getQuantity());
        verify(cartItemRepository, times(1)).save(testCartItem);
    }

    /**
     * Test adding product with invalid quantity
     * Verifies ValidationException is thrown
     */
    @Test
    void addProductToCartShouldThrowExceptionForInvalidQuantity() {
        // Given
        AddToCartRequest request = new AddToCartRequest();
        request.setProductId(productId);
        request.setQuantity(-1);

        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(productRepository.findById(productId)).thenReturn(Optional.of(testProduct));

        // When/Then
        assertThrows(ValidationException.class, () -> {
            cartService.addProductToCart(userId, request);
        });
    }

    /**
     * Test adding non-existent product
     * Verifies ResourceNotFoundException is thrown
     */
    @Test
    void addProductToCartShouldThrowExceptionForNonExistentProduct() {
        // Given
        AddToCartRequest request = new AddToCartRequest();
        request.setProductId(productId);
        request.setQuantity(1);

        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(productRepository.findById(productId)).thenReturn(Optional.empty());

        // When/Then
        assertThrows(ResourceNotFoundException.class, () -> {
            cartService.addProductToCart(userId, request);
        });
    }

    /**
     * Test updating cart item quantity
     * Verifies quantity update functionality
     */
    @Test
    void updateCartItemShouldUpdateQuantity() {
        // Given
        UpdateCartItemRequest request = new UpdateCartItemRequest();
        request.setQuantity(5);

        testCart.getItems().add(testCartItem);

        when(cartRepository.findByUserId(userId)).thenReturn(Optional.of(testCart));
        when(cartItemRepository.findById(itemId)).thenReturn(Optional.of(testCartItem));
        when(cartItemRepository.save(any(CartItem.class))).thenReturn(testCartItem);

        // When
        CartResponse response = cartService.updateCartItem(userId, itemId, request);

        // Then
        assertNotNull(response);
        assertEquals(5, testCartItem.getQuantity());
        verify(cartItemRepository, times(1)).save(testCartItem);
    }

    /**
     * Test updating cart item with invalid quantity
     * Verifies ValidationException is thrown
     */
    @Test
    void updateCartItemShouldThrowExceptionForInvalidQuantity() {
        // Given
        UpdateCartItemRequest request = new UpdateCartItemRequest();
        request.setQuantity(0);

        when(cartRepository.findByUserId(userId)).thenReturn(Optional.of(testCart));
        when(cartItemRepository.findById(itemId)).thenReturn(Optional.of(testCartItem));

        // When/Then
        assertThrows(ValidationException.class, () -> {
            cartService.updateCartItem(userId, itemId, request);
        });
    }

    /**
     * Test updating non-existent cart item
     * Verifies ResourceNotFoundException is thrown
     */
    @Test
    void updateCartItemShouldThrowExceptionForNonExistentItem() {
        // Given
        UpdateCartItemRequest request = new UpdateCartItemRequest();
        request.setQuantity(3);

        when(cartRepository.findByUserId(userId)).thenReturn(Optional.of(testCart));
        when(cartItemRepository.findById(itemId)).thenReturn(Optional.empty());

        // When/Then
        assertThrows(ResourceNotFoundException.class, () -> {
            cartService.updateCartItem(userId, itemId, request);
        });
    }

    /**
     * Test removing cart item
     * Verifies item removal functionality
     */
    @Test
    void removeCartItemShouldRemoveItem() {
        // Given
        testCart.getItems().add(testCartItem);

        when(cartRepository.findByUserId(userId)).thenReturn(Optional.of(testCart));
        when(cartItemRepository.findById(itemId)).thenReturn(Optional.of(testCartItem));
        doNothing().when(cartItemRepository).delete(testCartItem);

        // When
        CartResponse response = cartService.removeCartItem(userId, itemId);

        // Then
        verify(cartItemRepository, times(1)).delete(testCartItem);
    }

    /**
     * Test removing last cart item deletes cart
     * Verifies cart auto-deletion when empty
     */
    @Test
    void removeCartItemShouldDeleteCartWhenEmpty() {
        // Given
        testCart.getItems().add(testCartItem);

        when(cartRepository.findByUserId(userId)).thenReturn(Optional.of(testCart));
        when(cartItemRepository.findById(itemId)).thenReturn(Optional.of(testCartItem));
        doNothing().when(cartItemRepository).delete(testCartItem);
        doNothing().when(cartRepository).delete(testCart);

        // When
        CartResponse response = cartService.removeCartItem(userId, itemId);

        // Then
        assertNull(response);
        verify(cartRepository, times(1)).delete(testCart);
    }

    /**
     * Test getting cart
     * Verifies cart retrieval with items
     */
    @Test
    void getCartShouldReturnCartWithItems() {
        // Given
        testCart.getItems().add(testCartItem);

        when(cartRepository.findByUserId(userId)).thenReturn(Optional.of(testCart));

        // When
        CartResponse response = cartService.getCart(userId);

        // Then
        assertNotNull(response);
        assertEquals(cartId, response.getCartId());
        assertFalse(response.getItems().isEmpty());
    }

    /**
     * Test getting non-existent cart
     * Verifies ResourceNotFoundException is thrown
     */
    @Test
    void getCartShouldThrowExceptionForNonExistentCart() {
        // Given
        when(cartRepository.findByUserId(userId)).thenReturn(Optional.empty());

        // When/Then
        assertThrows(ResourceNotFoundException.class, () -> {
            cartService.getCart(userId);
        });
    }

    /**
     * Test clearing cart
     * Verifies cart deletion
     */
    @Test
    void clearCartShouldDeleteCart() {
        // Given
        doNothing().when(cartRepository).deleteByUserId(userId);

        // When
        cartService.clearCart(userId);

        // Then
        verify(cartRepository, times(1)).deleteByUserId(userId);
    }

    /**
     * Test cart total calculation
     * Verifies grand total is calculated correctly
     */
    @Test
    void getCartShouldCalculateGrandTotalCorrectly() {
        // Given
        testCart.getItems().add(testCartItem);
        when(cartRepository.findByUserId(userId)).thenReturn(Optional.of(testCart));

        // When
        CartResponse response = cartService.getCart(userId);

        // Then
        assertNotNull(response.getGrandTotal());
        assertEquals(BigDecimal.valueOf(100.00), response.getGrandTotal());
    }
}