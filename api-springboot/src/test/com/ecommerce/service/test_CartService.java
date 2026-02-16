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
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Test class for CartService
 * Tests shopping cart operations including add, update, remove, and clear functionality
 * 
 * @author QA Automation Team
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

    private User testUser;
    private Product testProduct;
    private Cart testCart;
    private CartItem testCartItem;
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
            .id(itemId)
            .cart(testCart)
            .product(testProduct)
            .quantity(2)
            .build();
    }

    /**
     * Test adding a new product to an empty cart
     * Verifies that a new cart is created and product is added successfully
     */
    @Test
    @DisplayName("Should add product to new cart successfully")
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
        when(cartItemRepository.findByCartIdAndProductId(any(), eq(productId))).thenReturn(Optional.empty());
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
     * Test adding a product to an existing cart
     * Verifies that product is added to existing cart
     */
    @Test
    @DisplayName("Should add product to existing cart successfully")
    void testAddProductToCart_ExistingCart() {
        // Arrange
        AddToCartRequest request = AddToCartRequest.builder()
            .productId(productId)
            .quantity(3)
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
        assertEquals(cartId, response.getCartId());
        verify(cartRepository, never()).save(any(Cart.class));
        verify(cartItemRepository).save(any(CartItem.class));
    }

    /**
     * Test updating quantity of existing cart item
     * Verifies that existing item quantity is incremented
     */
    @Test
    @DisplayName("Should update existing cart item quantity")
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
        when(cartItemRepository.save(testCartItem)).thenReturn(testCartItem);

        // Act
        CartResponse response = cartService.addProductToCart(userId, request);

        // Assert
        assertNotNull(response);
        assertEquals(5, testCartItem.getQuantity());
        verify(cartItemRepository).save(testCartItem);
    }

    /**
     * Test adding product with invalid quantity
     * Verifies that ValidationException is thrown for quantity <= 0
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
        ValidationException exception = assertThrows(ValidationException.class, () -> {
            cartService.addProductToCart(userId, request);
        });

        assertEquals("Quantity must be greater than 0", exception.getMessage());
        verify(cartRepository, never()).save(any());
    }

    /**
     * Test adding product when user not found
     * Verifies that ResourceNotFoundException is thrown
     */
    @Test
    @DisplayName("Should throw ResourceNotFoundException when user not found")
    void testAddProductToCart_UserNotFound() {
        // Arrange
        AddToCartRequest request = AddToCartRequest.builder()
            .productId(productId)
            .quantity(1)
            .build();

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            cartService.addProductToCart(userId, request);
        });

        assertEquals("User not found", exception.getMessage());
        verify(productRepository, never()).findById(any());
    }

    /**
     * Test adding product when product not found
     * Verifies that ResourceNotFoundException is thrown
     */
    @Test
    @DisplayName("Should throw ResourceNotFoundException when product not found")
    void testAddProductToCart_ProductNotFound() {
        // Arrange
        AddToCartRequest request = AddToCartRequest.builder()
            .productId(productId)
            .quantity(1)
            .build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(productRepository.findById(productId)).thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            cartService.addProductToCart(userId, request);
        });

        assertEquals("Product not found", exception.getMessage());
    }

    /**
     * Test updating cart item quantity
     * Verifies that cart item quantity is updated successfully
     */
    @Test
    @DisplayName("Should update cart item quantity successfully")
    void testUpdateCartItem_Success() {
        // Arrange
        UpdateCartItemRequest request = UpdateCartItemRequest.builder()
            .quantity(5)
            .build();

        testCart.getItems().add(testCartItem);

        when(cartRepository.findByUserId(userId)).thenReturn(Optional.of(testCart));
        when(cartItemRepository.findById(itemId)).thenReturn(Optional.of(testCartItem));
        when(cartItemRepository.save(testCartItem)).thenReturn(testCartItem);

        // Act
        CartResponse response = cartService.updateCartItem(userId, itemId, request);

        // Assert
        assertNotNull(response);
        assertEquals(5, testCartItem.getQuantity());
        verify(cartItemRepository).save(testCartItem);
    }

    /**
     * Test updating cart item with invalid quantity
     * Verifies that ValidationException is thrown
     */
    @Test
    @DisplayName("Should throw ValidationException when update quantity is invalid")
    void testUpdateCartItem_InvalidQuantity() {
        // Arrange
        UpdateCartItemRequest request = UpdateCartItemRequest.builder()
            .quantity(-1)
            .build();

        when(cartRepository.findByUserId(userId)).thenReturn(Optional.of(testCart));
        when(cartItemRepository.findById(itemId)).thenReturn(Optional.of(testCartItem));

        // Act & Assert
        ValidationException exception = assertThrows(ValidationException.class, () -> {
            cartService.updateCartItem(userId, itemId, request);
        });

        assertEquals("Quantity must be greater than 0", exception.getMessage());
    }

    /**
     * Test updating cart item that doesn't belong to user
     * Verifies that ValidationException is thrown
     */
    @Test
    @DisplayName("Should throw ValidationException when cart item doesn't belong to user")
    void testUpdateCartItem_ItemNotBelongToUser() {
        // Arrange
        UpdateCartItemRequest request = UpdateCartItemRequest.builder()
            .quantity(5)
            .build();

        Cart anotherCart = Cart.builder()
            .id(UUID.randomUUID())
            .user(testUser)
            .build();
        testCartItem.setCart(anotherCart);

        when(cartRepository.findByUserId(userId)).thenReturn(Optional.of(testCart));
        when(cartItemRepository.findById(itemId)).thenReturn(Optional.of(testCartItem));

        // Act & Assert
        ValidationException exception = assertThrows(ValidationException.class, () -> {
            cartService.updateCartItem(userId, itemId, request);
        });

        assertEquals("Cart item does not belong to user", exception.getMessage());
    }

    /**
     * Test removing cart item successfully
     * Verifies that cart item is removed from cart
     */
    @Test
    @DisplayName("Should remove cart item successfully")
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
     * Test removing last cart item
     * Verifies that cart is deleted when last item is removed
     */
    @Test
    @DisplayName("Should delete cart when last item is removed")
    void testRemoveCartItem_DeleteEmptyCart() {
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
     * Test getting cart for user
     * Verifies that cart is retrieved with all items
     */
    @Test
    @DisplayName("Should get cart successfully")
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
     * Verifies that ResourceNotFoundException is thrown
     */
    @Test
    @DisplayName("Should throw ResourceNotFoundException when cart not found")
    void testGetCart_NotFound() {
        // Arrange
        when(cartRepository.findByUserId(userId)).thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            cartService.getCart(userId);
        });

        assertEquals("Cart not found", exception.getMessage());
    }

    /**
     * Test clearing cart
     * Verifies that all cart items are removed
     */
    @Test
    @DisplayName("Should clear cart successfully")
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
    @DisplayName("Should calculate cart grand total correctly")
    void testCartResponse_GrandTotalCalculation() {
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
        assertEquals(2, response.getItems().size());
        BigDecimal expectedTotal = new BigDecimal("99.99")
            .multiply(new BigDecimal("2"))
            .add(new BigDecimal("50.00").multiply(new BigDecimal("3")));
        assertEquals(0, expectedTotal.compareTo(response.getGrandTotal()));
    }
}