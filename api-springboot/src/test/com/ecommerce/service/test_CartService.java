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
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit test class for CartService.
 * Tests cart management operations including adding, updating, and removing items.
 * Uses Mockito for mocking repository dependencies.
 * 
 * @author QA Automation Team
 * @version 1.0
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("CartService Unit Tests")
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

    /**
     * Set up test data before each test execution.
     */
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
     * Test adding a new product to cart successfully.
     * Verifies that a new cart is created if one doesn't exist.
     */
    @Test
    @DisplayName("Should add new product to cart and create cart if not exists")
    void testAddProductToCart_NewCart_ShouldCreateCartAndAddProduct() {
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
        verify(userRepository).findById(userId);
        verify(productRepository).findById(productId);
        verify(cartRepository).findByUserId(userId);
        verify(cartRepository).save(any(Cart.class));
        verify(cartItemRepository).save(any(CartItem.class));
    }

    /**
     * Test adding product to existing cart.
     * Verifies that product is added to existing cart without creating new one.
     */
    @Test
    @DisplayName("Should add product to existing cart")
    void testAddProductToCart_ExistingCart_ShouldAddProduct() {
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
        verify(cartRepository, never()).save(any(Cart.class));
        verify(cartItemRepository).save(any(CartItem.class));
    }

    /**
     * Test updating quantity of existing cart item.
     * Verifies that quantity is incremented when same product is added again.
     */
    @Test
    @DisplayName("Should update quantity when adding existing product")
    void testAddProductToCart_ExistingProduct_ShouldUpdateQuantity() {
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
        assertEquals(5, testCartItem.getQuantity()); // 2 + 3
        verify(cartItemRepository).save(testCartItem);
    }

    /**
     * Test adding product with invalid quantity (zero).
     * Verifies that ValidationException is thrown.
     */
    @Test
    @DisplayName("Should throw ValidationException when quantity is zero")
    void testAddProductToCart_ZeroQuantity_ShouldThrowValidationException() {
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
     * Test adding product with negative quantity.
     * Verifies that ValidationException is thrown.
     */
    @Test
    @DisplayName("Should throw ValidationException when quantity is negative")
    void testAddProductToCart_NegativeQuantity_ShouldThrowValidationException() {
        // Arrange
        AddToCartRequest request = AddToCartRequest.builder()
                .productId(productId)
                .quantity(-1)
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
     * Test adding product when user not found.
     * Verifies that ResourceNotFoundException is thrown.
     */
    @Test
    @DisplayName("Should throw ResourceNotFoundException when user not found")
    void testAddProductToCart_UserNotFound_ShouldThrowResourceNotFoundException() {
        // Arrange
        AddToCartRequest request = AddToCartRequest.builder()
                .productId(productId)
                .quantity(1)
                .build();

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> cartService.addProductToCart(userId, request)
        );
        assertEquals("User not found", exception.getMessage());
    }

    /**
     * Test adding product when product not found.
     * Verifies that ResourceNotFoundException is thrown.
     */
    @Test
    @DisplayName("Should throw ResourceNotFoundException when product not found")
    void testAddProductToCart_ProductNotFound_ShouldThrowResourceNotFoundException() {
        // Arrange
        AddToCartRequest request = AddToCartRequest.builder()
                .productId(productId)
                .quantity(1)
                .build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(productRepository.findById(productId)).thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> cartService.addProductToCart(userId, request)
        );
        assertEquals("Product not found", exception.getMessage());
    }

    /**
     * Test updating cart item quantity successfully.
     * Verifies that cart item quantity is updated correctly.
     */
    @Test
    @DisplayName("Should update cart item quantity successfully")
    void testUpdateCartItem_ValidRequest_ShouldUpdateQuantity() {
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
     * Test updating cart item with invalid quantity.
     * Verifies that ValidationException is thrown.
     */
    @Test
    @DisplayName("Should throw ValidationException when update quantity is invalid")
    void testUpdateCartItem_InvalidQuantity_ShouldThrowValidationException() {
        // Arrange
        UpdateCartItemRequest request = UpdateCartItemRequest.builder()
                .quantity(0)
                .build();

        when(cartRepository.findByUserId(userId)).thenReturn(Optional.of(testCart));
        when(cartItemRepository.findById(itemId)).thenReturn(Optional.of(testCartItem));

        // Act & Assert
        ValidationException exception = assertThrows(
                ValidationException.class,
                () -> cartService.updateCartItem(userId, itemId, request)
        );
        assertEquals("Quantity must be greater than 0", exception.getMessage());
    }

    /**
     * Test updating cart item that doesn't belong to user.
     * Verifies that ValidationException is thrown.
     */
    @Test
    @DisplayName("Should throw ValidationException when cart item doesn't belong to user")
    void testUpdateCartItem_ItemNotBelongToUser_ShouldThrowValidationException() {
        // Arrange
        UpdateCartItemRequest request = UpdateCartItemRequest.builder()
                .quantity(3)
                .build();

        Cart differentCart = Cart.builder()
                .id(UUID.randomUUID())
                .user(testUser)
                .build();

        when(cartRepository.findByUserId(userId)).thenReturn(Optional.of(testCart));
        when(cartItemRepository.findById(itemId)).thenReturn(Optional.of(testCartItem));
        testCartItem.setCart(differentCart);

        // Act & Assert
        ValidationException exception = assertThrows(
                ValidationException.class,
                () -> cartService.updateCartItem(userId, itemId, request)
        );
        assertEquals("Cart item does not belong to user", exception.getMessage());
    }

    /**
     * Test removing cart item successfully.
     * Verifies that cart item is deleted from cart.
     */
    @Test
    @DisplayName("Should remove cart item successfully")
    void testRemoveCartItem_ValidItem_ShouldRemoveItem() {
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
        verify(cartRepository, never()).delete(any(Cart.class));
    }

    /**
     * Test removing last cart item deletes entire cart.
     * Verifies that cart is deleted when last item is removed.
     */
    @Test
    @DisplayName("Should delete cart when removing last item")
    void testRemoveCartItem_LastItem_ShouldDeleteCart() {
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
     * Test getting cart for user.
     * Verifies that cart with all items is returned.
     */
    @Test
    @DisplayName("Should get cart with all items")
    void testGetCart_ValidUser_ShouldReturnCart() {
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
     * Test getting cart when cart not found.
     * Verifies that ResourceNotFoundException is thrown.
     */
    @Test
    @DisplayName("Should throw ResourceNotFoundException when cart not found")
    void testGetCart_CartNotFound_ShouldThrowResourceNotFoundException() {
        // Arrange
        when(cartRepository.findByUserId(userId)).thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> cartService.getCart(userId)
        );
        assertEquals("Cart not found", exception.getMessage());
    }

    /**
     * Test clearing cart successfully.
     * Verifies that cart is deleted from database.
     */
    @Test
    @DisplayName("Should clear cart successfully")
    void testClearCart_ValidUser_ShouldDeleteCart() {
        // Arrange
        doNothing().when(cartRepository).deleteByUserId(userId);

        // Act
        cartService.clearCart(userId);

        // Assert
        verify(cartRepository).deleteByUserId(userId);
    }

    /**
     * Test cart response calculation.
     * Verifies that grand total is calculated correctly.
     */
    @Test
    @DisplayName("Should calculate grand total correctly")
    void testGetCart_ShouldCalculateGrandTotalCorrectly() {
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
                .multiply(BigDecimal.valueOf(2))
                .add(new BigDecimal("50.00").multiply(BigDecimal.valueOf(3)));
        assertEquals(0, expectedTotal.compareTo(response.getGrandTotal()));
    }
}