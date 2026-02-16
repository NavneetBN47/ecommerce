package com.ecommerce.service;

import com.ecommerce.dto.AddToCartRequest;
import com.ecommerce.dto.CartResponse;
import com.ecommerce.dto.UpdateCartItemRequest;
import com.ecommerce.entity.Cart;
import com.ecommerce.entity.CartItem;
import com.ecommerce.entity.Product;
import com.ecommerce.entity.User;
import com.ecommerce.exception.InvalidOperationException;
import com.ecommerce.exception.ResourceNotFoundException;
import com.ecommerce.repository.CartItemRepository;
import com.ecommerce.repository.CartRepository;
import com.ecommerce.repository.ProductRepository;
import com.ecommerce.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Test class for CartService
 * 
 * Tests business logic for cart operations including:
 * - Adding products to cart
 * - Updating cart item quantities
 * - Removing items from cart
 * - Cart retrieval
 * - Cart cleanup on logout
 * 
 * @author Test Generation System
 * @version 1.0.0
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

    private User testUser;
    private Product testProduct;
    private Cart testCart;
    private CartItem testCartItem;
    private UUID testUserId;
    private UUID testProductId;
    private UUID testCartId;
    private UUID testCartItemId;

    /**
     * Set up test data before each test
     */
    @BeforeEach
    void setUp() {
        testUserId = UUID.randomUUID();
        testProductId = UUID.randomUUID();
        testCartId = UUID.randomUUID();
        testCartItemId = UUID.randomUUID();

        testUser = User.builder()
            .id(testUserId)
            .username("testuser")
            .email("test@example.com")
            .fullName("Test User")
            .password("password")
            .isActive(true)
            .build();

        testProduct = Product.builder()
            .id(testProductId)
            .name("Test Product")
            .description("Test Description")
            .price(BigDecimal.valueOf(99.99))
            .availableQty(10)
            .sku("TEST-001")
            .isActive(true)
            .build();

        testCart = Cart.builder()
            .id(testCartId)
            .user(testUser)
            .items(new ArrayList<>())
            .build();

        testCartItem = CartItem.builder()
            .id(testCartItemId)
            .cart(testCart)
            .product(testProduct)
            .quantity(2)
            .unitPrice(testProduct.getPrice())
            .build();
    }

    /**
     * Test adding product to cart successfully (new cart)
     * 
     * Validates:
     * - Cart is created if not exists
     * - Product is added to cart
     * - Correct response returned
     */
    @Test
    @DisplayName("Should add product to new cart successfully")
    void testAddProductToCart_NewCart_Success() {
        AddToCartRequest request = AddToCartRequest.builder()
            .productId(testProductId)
            .quantity(2)
            .build();

        when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));
        when(productRepository.findById(testProductId)).thenReturn(Optional.of(testProduct));
        when(cartRepository.findByUser(testUser)).thenReturn(Optional.empty());
        when(cartRepository.save(any(Cart.class))).thenReturn(testCart);
        when(cartItemRepository.findByCartAndProduct(any(Cart.class), any(Product.class)))
            .thenReturn(Optional.empty());
        when(cartItemRepository.save(any(CartItem.class))).thenReturn(testCartItem);
        when(cartRepository.findById(testCartId)).thenReturn(Optional.of(testCart));
        when(cartItemRepository.findByCart(testCart)).thenReturn(List.of(testCartItem));

        CartResponse response = cartService.addProductToCart(testUserId, request);

        assertThat(response).isNotNull();
        assertThat(response.getCartId()).isEqualTo(testCartId);
        verify(cartRepository).save(any(Cart.class));
        verify(cartItemRepository).save(any(CartItem.class));
    }

    /**
     * Test adding product to existing cart
     * 
     * Validates:
     * - Existing cart is used
     * - Product is added to cart
     */
    @Test
    @DisplayName("Should add product to existing cart successfully")
    void testAddProductToCart_ExistingCart_Success() {
        AddToCartRequest request = AddToCartRequest.builder()
            .productId(testProductId)
            .quantity(2)
            .build();

        when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));
        when(productRepository.findById(testProductId)).thenReturn(Optional.of(testProduct));
        when(cartRepository.findByUser(testUser)).thenReturn(Optional.of(testCart));
        when(cartItemRepository.findByCartAndProduct(testCart, testProduct))
            .thenReturn(Optional.empty());
        when(cartItemRepository.save(any(CartItem.class))).thenReturn(testCartItem);
        when(cartRepository.findById(testCartId)).thenReturn(Optional.of(testCart));
        when(cartItemRepository.findByCart(testCart)).thenReturn(List.of(testCartItem));

        CartResponse response = cartService.addProductToCart(testUserId, request);

        assertThat(response).isNotNull();
        verify(cartRepository, never()).save(any(Cart.class));
        verify(cartItemRepository).save(any(CartItem.class));
    }

    /**
     * Test adding product with quantity exceeding available stock
     * 
     * Validates:
     * - InvalidOperationException is thrown
     * - Appropriate error message
     */
    @Test
    @DisplayName("Should throw exception when quantity exceeds available stock")
    void testAddProductToCart_ExceedsStock() {
        AddToCartRequest request = AddToCartRequest.builder()
            .productId(testProductId)
            .quantity(20)
            .build();

        when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));
        when(productRepository.findById(testProductId)).thenReturn(Optional.of(testProduct));

        assertThatThrownBy(() -> cartService.addProductToCart(testUserId, request))
            .isInstanceOf(InvalidOperationException.class)
            .hasMessageContaining("exceeds available quantity");
    }

    /**
     * Test adding inactive product to cart
     * 
     * Validates:
     * - InvalidOperationException is thrown
     * - Product availability is checked
     */
    @Test
    @DisplayName("Should throw exception when adding inactive product")
    void testAddProductToCart_InactiveProduct() {
        testProduct.setIsActive(false);
        AddToCartRequest request = AddToCartRequest.builder()
            .productId(testProductId)
            .quantity(2)
            .build();

        when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));
        when(productRepository.findById(testProductId)).thenReturn(Optional.of(testProduct));

        assertThatThrownBy(() -> cartService.addProductToCart(testUserId, request))
            .isInstanceOf(InvalidOperationException.class)
            .hasMessageContaining("not available");
    }

    /**
     * Test updating cart item quantity
     * 
     * Validates:
     * - Quantity is updated correctly
     * - Cart response is returned
     */
    @Test
    @DisplayName("Should update cart item quantity successfully")
    void testUpdateCartItem_Success() {
        UpdateCartItemRequest request = UpdateCartItemRequest.builder()
            .quantity(5)
            .build();

        testCartItem.setCart(testCart);
        testCart.setUser(testUser);

        when(cartItemRepository.findByIdWithCartAndProduct(testCartItemId))
            .thenReturn(Optional.of(testCartItem));
        when(cartItemRepository.save(any(CartItem.class))).thenReturn(testCartItem);
        when(cartRepository.findById(testCartId)).thenReturn(Optional.of(testCart));
        when(cartItemRepository.findByCart(testCart)).thenReturn(List.of(testCartItem));

        CartResponse response = cartService.updateCartItem(testUserId, testCartItemId, request);

        assertThat(response).isNotNull();
        verify(cartItemRepository).save(any(CartItem.class));
    }

    /**
     * Test updating cart item with quantity exceeding stock
     * 
     * Validates:
     * - InvalidOperationException is thrown
     */
    @Test
    @DisplayName("Should throw exception when update quantity exceeds stock")
    void testUpdateCartItem_ExceedsStock() {
        UpdateCartItemRequest request = UpdateCartItemRequest.builder()
            .quantity(20)
            .build();

        testCartItem.setCart(testCart);
        testCart.setUser(testUser);

        when(cartItemRepository.findByIdWithCartAndProduct(testCartItemId))
            .thenReturn(Optional.of(testCartItem));

        assertThatThrownBy(() -> cartService.updateCartItem(testUserId, testCartItemId, request))
            .isInstanceOf(InvalidOperationException.class)
            .hasMessageContaining("exceeds available quantity");
    }

    /**
     * Test removing cart item
     * 
     * Validates:
     * - Item is removed from cart
     * - Cart response is returned
     */
    @Test
    @DisplayName("Should remove cart item successfully")
    void testRemoveCartItem_Success() {
        testCartItem.setCart(testCart);
        testCart.setUser(testUser);

        when(cartItemRepository.findByIdWithCartAndProduct(testCartItemId))
            .thenReturn(Optional.of(testCartItem));
        when(cartItemRepository.countByCart(testCart)).thenReturn(1L);
        when(cartRepository.findById(testCartId)).thenReturn(Optional.of(testCart));
        when(cartItemRepository.findByCart(testCart)).thenReturn(new ArrayList<>());

        CartResponse response = cartService.removeCartItem(testUserId, testCartItemId);

        assertThat(response).isNotNull();
        verify(cartItemRepository).delete(testCartItem);
    }

    /**
     * Test removing last item from cart
     * 
     * Validates:
     * - Cart is deleted when empty
     * - Empty cart response is returned
     */
    @Test
    @DisplayName("Should delete cart when removing last item")
    void testRemoveCartItem_EmptyCart() {
        testCartItem.setCart(testCart);
        testCart.setUser(testUser);

        when(cartItemRepository.findByIdWithCartAndProduct(testCartItemId))
            .thenReturn(Optional.of(testCartItem));
        when(cartItemRepository.countByCart(testCart)).thenReturn(0L);

        CartResponse response = cartService.removeCartItem(testUserId, testCartItemId);

        assertThat(response).isNotNull();
        assertThat(response.getCartId()).isNull();
        assertThat(response.getTotalItems()).isZero();
        verify(cartRepository).delete(testCart);
    }

    /**
     * Test getting cart
     * 
     * Validates:
     * - Cart details are returned
     * - Items are included
     */
    @Test
    @DisplayName("Should get cart successfully")
    void testGetCart_Success() {
        when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));
        when(cartRepository.findByUserIdWithItems(testUserId)).thenReturn(Optional.of(testCart));
        when(cartRepository.findById(testCartId)).thenReturn(Optional.of(testCart));
        when(cartItemRepository.findByCart(testCart)).thenReturn(List.of(testCartItem));

        CartResponse response = cartService.getCart(testUserId);

        assertThat(response).isNotNull();
        assertThat(response.getCartId()).isEqualTo(testCartId);
    }

    /**
     * Test getting cart for non-existent user
     * 
     * Validates:
     * - ResourceNotFoundException is thrown
     */
    @Test
    @DisplayName("Should throw exception when user not found")
    void testGetCart_UserNotFound() {
        when(userRepository.findById(testUserId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> cartService.getCart(testUserId))
            .isInstanceOf(ResourceNotFoundException.class)
            .hasMessageContaining("User");
    }

    /**
     * Test cleanup cart on logout
     * 
     * Validates:
     * - Cart is deleted
     * - No exception when cart doesn't exist
     */
    @Test
    @DisplayName("Should cleanup cart on logout")
    void testCleanupCartOnLogout_Success() {
        when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));
        when(cartRepository.findByUser(testUser)).thenReturn(Optional.of(testCart));

        cartService.cleanupCartOnLogout(testUserId);

        verify(cartRepository).delete(testCart);
    }

    /**
     * Test cleanup when no cart exists
     * 
     * Validates:
     * - No exception is thrown
     * - Delete is not called
     */
    @Test
    @DisplayName("Should handle cleanup when no cart exists")
    void testCleanupCartOnLogout_NoCart() {
        when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));
        when(cartRepository.findByUser(testUser)).thenReturn(Optional.empty());

        cartService.cleanupCartOnLogout(testUserId);

        verify(cartRepository, never()).delete(any(Cart.class));
    }
}