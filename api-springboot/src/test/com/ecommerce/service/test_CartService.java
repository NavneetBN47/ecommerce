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
import org.junit.jupiter.api.DisplayName;
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
 * Tests shopping cart operations including add, update, remove, and retrieve
 * Uses Mockito for mocking repository dependencies
 *
 * @author Test Generation Agent
 * @version 1.0
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("CartService Tests")
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
            .price(new BigDecimal("99.99"))
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
     * Test adding a new product to cart
     * Verifies that a product can be added to a new cart
     */
    @Test
    @DisplayName("Should successfully add product to new cart")
    void testAddProductToCart_NewCart_Success() {
        // Arrange
        AddToCartRequest request = AddToCartRequest.builder()
            .productId(productId)
            .quantity(2)
            .build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(productRepository.findById(productId)).thenReturn(Optional.of(testProduct));
        when(cartRepository.findByUserId(userId)).thenReturn(Optional.empty());
        when(cartRepository.save(any(Cart.class))).thenReturn(testCart);
        when(cartItemRepository.findByCartIdAndProductId(any(), any())).thenReturn(Optional.empty());
        when(cartItemRepository.save(any(CartItem.class))).thenReturn(testCartItem);

        // Act
        CartResponse response = cartService.addProductToCart(userId, request);

        // Assert
        assertNotNull(response);
        assertEquals(cartId, response.getCartId());
        verify(userRepository).findById(userId);
        verify(productRepository).findById(productId);
        verify(cartRepository).save(any(Cart.class));
        verify(cartItemRepository).save(any(CartItem.class));
    }

    /**
     * Test adding product to existing cart
     * Verifies that a product can be added to an existing cart
     */
    @Test
    @DisplayName("Should successfully add product to existing cart")
    void testAddProductToCart_ExistingCart_Success() {
        // Arrange
        AddToCartRequest request = AddToCartRequest.builder()
            .productId(productId)
            .quantity(1)
            .build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(productRepository.findById(productId)).thenReturn(Optional.of(testProduct));
        when(cartRepository.findByUserId(userId)).thenReturn(Optional.of(testCart));
        when(cartItemRepository.findByCartIdAndProductId(cartId, productId)).thenReturn(Optional.empty());
        when(cartItemRepository.save(any(CartItem.class))).thenReturn(testCartItem);

        // Act
        CartResponse response = cartService.addProductToCart(userId, request);

        // Assert
        assertNotNull(response);
        verify(cartRepository).findByUserId(userId);
        verify(cartItemRepository).save(any(CartItem.class));
    }

    /**
     * Test updating quantity of existing cart item
     * Verifies that quantity is updated when product already exists in cart
     */
    @Test
    @DisplayName("Should update quantity when product already exists in cart")
    void testAddProductToCart_UpdateExistingItem_Success() {
        // Arrange
        AddToCartRequest request = AddToCartRequest.builder()
            .productId(productId)
            .quantity(3)
            .build();

        testCart.getItems().add(testCartItem);

        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(productRepository.findById(productId)).thenReturn(Optional.of(testProduct));
        when(cartRepository.findByUserId(userId)).thenReturn(Optional.of(testCart));
        when(cartItemRepository.findByCartIdAndProductId(cartId, productId))
            .thenReturn(Optional.of(testCartItem));
        when(cartItemRepository.save(any(CartItem.class))).thenReturn(testCartItem);

        // Act
        CartResponse response = cartService.addProductToCart(userId, request);

        // Assert
        assertNotNull(response);
        assertEquals(5, testCartItem.getQuantity()); // 2 + 3
        verify(cartItemRepository).save(testCartItem);
    }

    /**
     * Test adding product with invalid quantity
     * Verifies that ValidationException is thrown for zero or negative quantity
     */
    @Test
    @DisplayName("Should throw ValidationException when quantity is zero or negative")
    void testAddProductToCart_InvalidQuantity_ThrowsException() {
        // Arrange
        AddToCartRequest request = AddToCartRequest.builder()
            .productId(productId)
            .quantity(0)
            .build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(productRepository.findById(productId)).thenReturn(Optional.of(testProduct));

        // Act & Assert
        ValidationException exception = assertThrows(
            ValidationException.class,
            () -> cartService.addProductToCart(userId, request)
        );

        assertEquals("Quantity must be greater than 0", exception.getMessage());
        verify(cartRepository, never()).save(any());
    }

    /**
     * Test adding product when user not found
     * Verifies that ResourceNotFoundException is thrown for invalid user
     */
    @Test
    @DisplayName("Should throw ResourceNotFoundException when user not found")
    void testAddProductToCart_UserNotFound_ThrowsException() {
        // Arrange
        AddToCartRequest request = AddToCartRequest.builder()
            .productId(productId)
            .quantity(1)
            .build();

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(
            ResourceNotFoundException.class,
            () -> cartService.addProductToCart(userId, request)
        );

        verify(userRepository).findById(userId);
        verify(productRepository, never()).findById(any());
    }

    /**
     * Test adding product when product not found
     * Verifies that ResourceNotFoundException is thrown for invalid product
     */
    @Test
    @DisplayName("Should throw ResourceNotFoundException when product not found")
    void testAddProductToCart_ProductNotFound_ThrowsException() {
        // Arrange
        AddToCartRequest request = AddToCartRequest.builder()
            .productId(productId)
            .quantity(1)
            .build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(productRepository.findById(productId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(
            ResourceNotFoundException.class,
            () -> cartService.addProductToCart(userId, request)
        );

        verify(productRepository).findById(productId);
    }

    /**
     * Test updating cart item quantity
     * Verifies that cart item quantity can be updated successfully
     */
    @Test
    @DisplayName("Should successfully update cart item quantity")
    void testUpdateCartItem_Success() {
        // Arrange
        UpdateCartItemRequest request = UpdateCartItemRequest.builder()
            .quantity(5)
            .build();

        testCart.getItems().add(testCartItem);

        when(cartRepository.findByUserId(userId)).thenReturn(Optional.of(testCart));
        when(cartItemRepository.findById(itemId)).thenReturn(Optional.of(testCartItem));
        when(cartItemRepository.save(any(CartItem.class))).thenReturn(testCartItem);

        // Act
        CartResponse response = cartService.updateCartItem(userId, itemId, request);

        // Assert
        assertNotNull(response);
        assertEquals(5, testCartItem.getQuantity());
        verify(cartItemRepository).save(testCartItem);
    }

    /**
     * Test updating cart item with invalid quantity
     * Verifies that ValidationException is thrown for invalid quantity
     */
    @Test
    @DisplayName("Should throw ValidationException when update quantity is invalid")
    void testUpdateCartItem_InvalidQuantity_ThrowsException() {
        // Arrange
        UpdateCartItemRequest request = UpdateCartItemRequest.builder()
            .quantity(-1)
            .build();

        when(cartRepository.findByUserId(userId)).thenReturn(Optional.of(testCart));
        when(cartItemRepository.findById(itemId)).thenReturn(Optional.of(testCartItem));

        // Act & Assert
        assertThrows(
            ValidationException.class,
            () -> cartService.updateCartItem(userId, itemId, request)
        );

        verify(cartItemRepository, never()).save(any());
    }

    /**
     * Test updating cart item that doesn't belong to user
     * Verifies that ValidationException is thrown for unauthorized access
     */
    @Test
    @DisplayName("Should throw ValidationException when cart item doesn't belong to user")
    void testUpdateCartItem_UnauthorizedAccess_ThrowsException() {
        // Arrange
        UpdateCartItemRequest request = UpdateCartItemRequest.builder()
            .quantity(3)
            .build();

        Cart differentCart = Cart.builder()
            .id(UUID.randomUUID())
            .user(testUser)
            .build();

        testCartItem.setCart(differentCart);

        when(cartRepository.findByUserId(userId)).thenReturn(Optional.of(testCart));
        when(cartItemRepository.findById(itemId)).thenReturn(Optional.of(testCartItem));

        // Act & Assert
        ValidationException exception = assertThrows(
            ValidationException.class,
            () -> cartService.updateCartItem(userId, itemId, request)
        );

        assertEquals("Cart item does not belong to user", exception.getMessage());
    }

    /**
     * Test removing cart item
     * Verifies that cart item can be removed successfully
     */
    @Test
    @DisplayName("Should successfully remove cart item")
    void testRemoveCartItem_Success() {
        // Arrange
        testCart.getItems().add(testCartItem);
        CartItem anotherItem = CartItem.builder()
            .id(UUID.randomUUID())
            .cart(testCart)
            .product(testProduct)
            .quantity(1)
            .build();
        testCart.getItems().add(anotherItem);

        when(cartRepository.findByUserId(userId)).thenReturn(Optional.of(testCart));
        when(cartItemRepository.findById(itemId)).thenReturn(Optional.of(testCartItem));
        doNothing().when(cartItemRepository).delete(testCartItem);

        // Act
        CartResponse response = cartService.removeCartItem(userId, itemId);

        // Assert
        assertNotNull(response);
        verify(cartItemRepository).delete(testCartItem);
        verify(cartRepository, never()).delete(any());
    }

    /**
     * Test removing last cart item deletes cart
     * Verifies that cart is deleted when last item is removed
     */
    @Test
    @DisplayName("Should delete cart when last item is removed")
    void testRemoveCartItem_LastItem_DeletesCart() {
        // Arrange
        testCart.getItems().add(testCartItem);

        when(cartRepository.findByUserId(userId)).thenReturn(Optional.of(testCart));
        when(cartItemRepository.findById(itemId)).thenReturn(Optional.of(testCartItem));
        doNothing().when(cartItemRepository).delete(testCartItem);
        doNothing().when(cartRepository).delete(testCart);

        // Act
        CartResponse response = cartService.removeCartItem(userId, itemId);

        // Assert
        assertNull(response);
        verify(cartItemRepository).delete(testCartItem);
        verify(cartRepository).delete(testCart);
    }

    /**
     * Test getting cart
     * Verifies that cart can be retrieved successfully
     */
    @Test
    @DisplayName("Should successfully get cart")
    void testGetCart_Success() {
        // Arrange
        testCart.getItems().add(testCartItem);

        when(cartRepository.findByUserId(userId)).thenReturn(Optional.of(testCart));

        // Act
        CartResponse response = cartService.getCart(userId);

        // Assert
        assertNotNull(response);
        assertEquals(cartId, response.getCartId());
        assertFalse(response.getItems().isEmpty());
        verify(cartRepository).findByUserId(userId);
    }

    /**
     * Test getting cart when cart not found
     * Verifies that ResourceNotFoundException is thrown when cart doesn't exist
     */
    @Test
    @DisplayName("Should throw ResourceNotFoundException when cart not found")
    void testGetCart_NotFound_ThrowsException() {
        // Arrange
        when(cartRepository.findByUserId(userId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(
            ResourceNotFoundException.class,
            () -> cartService.getCart(userId)
        );

        verify(cartRepository).findByUserId(userId);
    }

    /**
     * Test clearing cart
     * Verifies that cart can be cleared successfully
     */
    @Test
    @DisplayName("Should successfully clear cart")
    void testClearCart_Success() {
        // Arrange
        doNothing().when(cartRepository).deleteByUserId(userId);

        // Act
        cartService.clearCart(userId);

        // Assert
        verify(cartRepository).deleteByUserId(userId);
    }

    /**
     * Test cart total calculation
     * Verifies that cart total is calculated correctly
     */
    @Test
    @DisplayName("Should calculate cart total correctly")
    void testGetCart_CalculatesTotalCorrectly() {
        // Arrange
        testCart.getItems().add(testCartItem);
        BigDecimal expectedTotal = testProduct.getPrice()
            .multiply(BigDecimal.valueOf(testCartItem.getQuantity()));

        when(cartRepository.findByUserId(userId)).thenReturn(Optional.of(testCart));

        // Act
        CartResponse response = cartService.getCart(userId);

        // Assert
        assertNotNull(response);
        assertEquals(expectedTotal, response.getGrandTotal());
    }
}