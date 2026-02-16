package com.ecommerce.service;

import com.ecommerce.dto.AddToCartRequest;
import com.ecommerce.dto.CartItemResponse;
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
 * JUnit 5 test class for CartService
 * Tests shopping cart operations including add, update, remove, and retrieve
 * 
 * @author Test Generation Agent
 * @version 1.0
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("CartService Test Suite")
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
    private UUID cartItemId;
    private User testUser;
    private Product testProduct;
    private Cart testCart;
    private CartItem testCartItem;

    /**
     * Setup method to initialize test data before each test
     */
    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        productId = UUID.randomUUID();
        cartId = UUID.randomUUID();
        cartItemId = UUID.randomUUID();

        testUser = User.builder()
                .id(userId)
                .username("testuser")
                .email("test@example.com")
                .build();

        testProduct = Product.builder()
                .id(productId)
                .name("Test Product")
                .description("Test Description")
                .price(new BigDecimal("99.99"))
                .availableQty(100)
                .build();

        testCart = Cart.builder()
                .id(cartId)
                .user(testUser)
                .items(new ArrayList<>())
                .build();

        testCartItem = CartItem.builder()
                .id(cartItemId)
                .cart(testCart)
                .product(testProduct)
                .quantity(2)
                .build();
    }

    /**
     * Test adding product to new cart
     * Verifies that a new cart is created when user doesn't have one
     */
    @Test
    @DisplayName("Should create new cart and add product when cart doesn't exist")
    void testAddProductToCart_NewCart() {
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
        verify(cartRepository).save(any(Cart.class));
        verify(cartItemRepository).save(any(CartItem.class));
    }

    /**
     * Test adding product to existing cart
     * Verifies that product is added to existing cart
     */
    @Test
    @DisplayName("Should add product to existing cart")
    void testAddProductToCart_ExistingCart() {
        // Arrange
        AddToCartRequest request = AddToCartRequest.builder()
                .productId(productId)
                .quantity(2)
                .build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(productRepository.findById(productId)).thenReturn(Optional.of(testProduct));
        when(cartRepository.findByUserId(userId)).thenReturn(Optional.of(testCart));
        when(cartItemRepository.findByCartIdAndProductId(cartId, productId)).thenReturn(Optional.empty());
        when(cartItemRepository.save(any(CartItem.class))).thenReturn(testCartItem);

        testCart.getItems().add(testCartItem);

        // Act
        CartResponse response = cartService.addProductToCart(userId, request);

        // Assert
        assertNotNull(response);
        verify(cartRepository, never()).save(any(Cart.class));
        verify(cartItemRepository).save(any(CartItem.class));
    }

    /**
     * Test updating quantity of existing cart item
     * Verifies that quantity is updated when product already in cart
     */
    @Test
    @DisplayName("Should update quantity when product already in cart")
    void testAddProductToCart_UpdateExistingItem() {
        // Arrange
        AddToCartRequest request = AddToCartRequest.builder()
                .productId(productId)
                .quantity(3)
                .build();

        testCart.getItems().add(testCartItem);

        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(productRepository.findById(productId)).thenReturn(Optional.of(testProduct));
        when(cartRepository.findByUserId(userId)).thenReturn(Optional.of(testCart));
        when(cartItemRepository.findByCartIdAndProductId(cartId, productId)).thenReturn(Optional.of(testCartItem));
        when(cartItemRepository.save(any(CartItem.class))).thenReturn(testCartItem);

        // Act
        CartResponse response = cartService.addProductToCart(userId, request);

        // Assert
        assertNotNull(response);
        assertEquals(5, testCartItem.getQuantity());
        verify(cartItemRepository).save(testCartItem);
    }

    /**
     * Test adding product with invalid quantity
     * Verifies that validation exception is thrown for zero or negative quantity
     */
    @Test
    @DisplayName("Should throw ValidationException when quantity is zero or negative")
    void testAddProductToCart_InvalidQuantity() {
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
    }

    /**
     * Test adding non-existent product
     * Verifies that exception is thrown when product not found
     */
    @Test
    @DisplayName("Should throw ResourceNotFoundException when product not found")
    void testAddProductToCart_ProductNotFound() {
        // Arrange
        AddToCartRequest request = AddToCartRequest.builder()
                .productId(productId)
                .quantity(2)
                .build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(productRepository.findById(productId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(
                ResourceNotFoundException.class,
                () -> cartService.addProductToCart(userId, request)
        );
    }

    /**
     * Test adding product for non-existent user
     * Verifies that exception is thrown when user not found
     */
    @Test
    @DisplayName("Should throw ResourceNotFoundException when user not found")
    void testAddProductToCart_UserNotFound() {
        // Arrange
        AddToCartRequest request = AddToCartRequest.builder()
                .productId(productId)
                .quantity(2)
                .build();

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(
                ResourceNotFoundException.class,
                () -> cartService.addProductToCart(userId, request)
        );
    }

    /**
     * Test updating cart item successfully
     * Verifies that cart item quantity is updated
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
        when(cartItemRepository.findById(cartItemId)).thenReturn(Optional.of(testCartItem));
        when(cartItemRepository.save(any(CartItem.class))).thenReturn(testCartItem);

        // Act
        CartResponse response = cartService.updateCartItem(userId, cartItemId, request);

        // Assert
        assertNotNull(response);
        assertEquals(5, testCartItem.getQuantity());
        verify(cartItemRepository).save(testCartItem);
    }

    /**
     * Test updating cart item with invalid quantity
     * Verifies that validation exception is thrown
     */
    @Test
    @DisplayName("Should throw ValidationException when update quantity is invalid")
    void testUpdateCartItem_InvalidQuantity() {
        // Arrange
        UpdateCartItemRequest request = UpdateCartItemRequest.builder()
                .quantity(-1)
                .build();

        when(cartRepository.findByUserId(userId)).thenReturn(Optional.of(testCart));
        when(cartItemRepository.findById(cartItemId)).thenReturn(Optional.of(testCartItem));

        // Act & Assert
        ValidationException exception = assertThrows(
                ValidationException.class,
                () -> cartService.updateCartItem(userId, cartItemId, request)
        );

        assertEquals("Quantity must be greater than 0", exception.getMessage());
    }

    /**
     * Test updating cart item that doesn't belong to user
     * Verifies that validation exception is thrown
     */
    @Test
    @DisplayName("Should throw ValidationException when cart item doesn't belong to user")
    void testUpdateCartItem_ItemNotBelongToUser() {
        // Arrange
        UpdateCartItemRequest request = UpdateCartItemRequest.builder()
                .quantity(5)
                .build();

        Cart differentCart = Cart.builder()
                .id(UUID.randomUUID())
                .user(testUser)
                .build();

        when(cartRepository.findByUserId(userId)).thenReturn(Optional.of(testCart));
        when(cartItemRepository.findById(cartItemId)).thenReturn(Optional.of(testCartItem));
        testCartItem.setCart(differentCart);

        // Act & Assert
        ValidationException exception = assertThrows(
                ValidationException.class,
                () -> cartService.updateCartItem(userId, cartItemId, request)
        );

        assertEquals("Cart item does not belong to user", exception.getMessage());
    }

    /**
     * Test removing cart item successfully
     * Verifies that cart item is removed from cart
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
        when(cartItemRepository.findById(cartItemId)).thenReturn(Optional.of(testCartItem));
        doNothing().when(cartItemRepository).delete(testCartItem);

        // Act
        CartResponse response = cartService.removeCartItem(userId, cartItemId);

        // Assert
        assertNotNull(response);
        verify(cartItemRepository).delete(testCartItem);
        verify(cartRepository, never()).delete(any(Cart.class));
    }

    /**
     * Test removing last cart item
     * Verifies that cart is deleted when last item is removed
     */
    @Test
    @DisplayName("Should delete cart when last item is removed")
    void testRemoveCartItem_DeleteEmptyCart() {
        // Arrange
        testCart.getItems().add(testCartItem);

        when(cartRepository.findByUserId(userId)).thenReturn(Optional.of(testCart));
        when(cartItemRepository.findById(cartItemId)).thenReturn(Optional.of(testCartItem));
        doNothing().when(cartItemRepository).delete(testCartItem);
        doNothing().when(cartRepository).delete(testCart);

        // Act
        CartResponse response = cartService.removeCartItem(userId, cartItemId);

        // Assert
        assertNull(response);
        verify(cartItemRepository).delete(testCartItem);
        verify(cartRepository).delete(testCart);
    }

    /**
     * Test getting cart successfully
     * Verifies that cart with items is retrieved
     */
    @Test
    @DisplayName("Should successfully retrieve cart with items")
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
     * Test getting non-existent cart
     * Verifies that exception is thrown when cart not found
     */
    @Test
    @DisplayName("Should throw ResourceNotFoundException when cart not found")
    void testGetCart_NotFound() {
        // Arrange
        when(cartRepository.findByUserId(userId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(
                ResourceNotFoundException.class,
                () -> cartService.getCart(userId)
        );
    }

    /**
     * Test clearing cart successfully
     * Verifies that cart is deleted
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
     * Test cart response calculation
     * Verifies that grand total is calculated correctly
     */
    @Test
    @DisplayName("Should calculate grand total correctly")
    void testGetCart_CorrectTotalCalculation() {
        // Arrange
        testCart.getItems().add(testCartItem);
        CartItem secondItem = CartItem.builder()
                .id(UUID.randomUUID())
                .cart(testCart)
                .product(Product.builder()
                        .id(UUID.randomUUID())
                        .name("Product 2")
                        .price(new BigDecimal("50.00"))
                        .build())
                .quantity(3)
                .build();
        testCart.getItems().add(secondItem);

        when(cartRepository.findByUserId(userId)).thenReturn(Optional.of(testCart));

        // Act
        CartResponse response = cartService.getCart(userId);

        // Assert
        assertNotNull(response);
        BigDecimal expectedTotal = new BigDecimal("99.99")
                .multiply(new BigDecimal("2"))
                .add(new BigDecimal("50.00").multiply(new BigDecimal("3")));
        assertEquals(0, expectedTotal.compareTo(response.getGrandTotal()));
    }
}