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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Arrays;
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
 * This test class verifies the business logic for shopping cart management,
 * including cart creation, item management, and cart cleanup operations.
 * 
 * @author Shopping Cart System Team
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

    /**
     * Setup method to initialize test data before each test
     */
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
                .price(new BigDecimal("99.99"))
                .availableQty(10)
                .isActive(true)
                .build();

        cart = Cart.builder()
                .id(cartId)
                .user(user)
                .build();

        cartItem = CartItem.builder()
                .id(itemId)
                .cart(cart)
                .product(product)
                .quantity(2)
                .unitPrice(new BigDecimal("99.99"))
                .build();
    }

    /**
     * Test successful addition of product to cart with lazy cart creation
     * 
     * Verifies that a new cart is created if user doesn't have one,
     * and product is added successfully.
     */
    @Test
    void addProductToCart_WithNewCart_ShouldCreateCartAndAddProduct() {
        AddToCartRequest request = new AddToCartRequest();
        request.setProductId(productId);
        request.setQuantity(2);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(cartRepository.findByUser(user)).thenReturn(Optional.empty());
        when(cartRepository.save(any(Cart.class))).thenReturn(cart);
        when(cartItemRepository.findByCartAndProduct(any(Cart.class), any(Product.class)))
                .thenReturn(Optional.empty());
        when(cartItemRepository.save(any(CartItem.class))).thenReturn(cartItem);
        when(cartRepository.findById(cartId)).thenReturn(Optional.of(cart));
        when(cartItemRepository.findByCart(cart)).thenReturn(Arrays.asList(cartItem));

        CartResponse response = cartService.addProductToCart(userId, request);

        assertThat(response).isNotNull();
        assertThat(response.getCartId()).isEqualTo(cartId);
        verify(cartRepository, times(1)).save(any(Cart.class));
        verify(cartItemRepository, times(1)).save(any(CartItem.class));
    }

    /**
     * Test adding product to existing cart
     * 
     * Verifies that product is added to an existing cart
     * without creating a new cart.
     */
    @Test
    void addProductToCart_WithExistingCart_ShouldAddProductToCart() {
        AddToCartRequest request = new AddToCartRequest();
        request.setProductId(productId);
        request.setQuantity(1);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(cartRepository.findByUser(user)).thenReturn(Optional.of(cart));
        when(cartItemRepository.findByCartAndProduct(cart, product)).thenReturn(Optional.empty());
        when(cartItemRepository.save(any(CartItem.class))).thenReturn(cartItem);
        when(cartRepository.findById(cartId)).thenReturn(Optional.of(cart));
        when(cartItemRepository.findByCart(cart)).thenReturn(Arrays.asList(cartItem));

        CartResponse response = cartService.addProductToCart(userId, request);

        assertThat(response).isNotNull();
        verify(cartRepository, never()).save(any(Cart.class));
        verify(cartItemRepository, times(1)).save(any(CartItem.class));
    }

    /**
     * Test adding product that already exists in cart updates quantity
     * 
     * Verifies that adding an existing product increases its quantity
     * instead of creating a duplicate item.
     */
    @Test
    void addProductToCart_WithExistingProduct_ShouldUpdateQuantity() {
        AddToCartRequest request = new AddToCartRequest();
        request.setProductId(productId);
        request.setQuantity(3);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(cartRepository.findByUser(user)).thenReturn(Optional.of(cart));
        when(cartItemRepository.findByCartAndProduct(cart, product)).thenReturn(Optional.of(cartItem));
        when(cartItemRepository.save(any(CartItem.class))).thenReturn(cartItem);
        when(cartRepository.findById(cartId)).thenReturn(Optional.of(cart));
        when(cartItemRepository.findByCart(cart)).thenReturn(Arrays.asList(cartItem));

        CartResponse response = cartService.addProductToCart(userId, request);

        assertThat(response).isNotNull();
        verify(cartItemRepository, times(1)).save(any(CartItem.class));
    }

    /**
     * Test adding product with quantity exceeding available stock throws exception
     * 
     * Verifies that attempting to add more items than available
     * throws InvalidOperationException.
     */
    @Test
    void addProductToCart_WithExcessiveQuantity_ShouldThrowException() {
        AddToCartRequest request = new AddToCartRequest();
        request.setProductId(productId);
        request.setQuantity(20);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));

        assertThatThrownBy(() -> cartService.addProductToCart(userId, request))
                .isInstanceOf(InvalidOperationException.class)
                .hasMessageContaining("exceeds available quantity");
    }

    /**
     * Test adding inactive product throws exception
     * 
     * Verifies that attempting to add an inactive product
     * throws InvalidOperationException.
     */
    @Test
    void addProductToCart_WithInactiveProduct_ShouldThrowException() {
        AddToCartRequest request = new AddToCartRequest();
        request.setProductId(productId);
        request.setQuantity(1);

        product.setIsActive(false);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));

        assertThatThrownBy(() -> cartService.addProductToCart(userId, request))
                .isInstanceOf(InvalidOperationException.class)
                .hasMessageContaining("not available");
    }

    /**
     * Test adding product with non-existent user throws exception
     * 
     * Verifies that attempting to add product for non-existent user
     * throws ResourceNotFoundException.
     */
    @Test
    void addProductToCart_WithNonExistentUser_ShouldThrowException() {
        AddToCartRequest request = new AddToCartRequest();
        request.setProductId(productId);
        request.setQuantity(1);

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> cartService.addProductToCart(userId, request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("User");
    }

    /**
     * Test adding non-existent product throws exception
     * 
     * Verifies that attempting to add non-existent product
     * throws ResourceNotFoundException.
     */
    @Test
    void addProductToCart_WithNonExistentProduct_ShouldThrowException() {
        AddToCartRequest request = new AddToCartRequest();
        request.setProductId(productId);
        request.setQuantity(1);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(productRepository.findById(productId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> cartService.addProductToCart(userId, request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Product");
    }

    /**
     * Test successful update of cart item quantity
     * 
     * Verifies that cart item quantity can be updated successfully.
     */
    @Test
    void updateCartItem_WithValidQuantity_ShouldUpdateSuccessfully() {
        UpdateCartItemRequest request = new UpdateCartItemRequest();
        request.setQuantity(5);

        when(cartItemRepository.findByIdWithCartAndProduct(itemId)).thenReturn(Optional.of(cartItem));
        when(cartItemRepository.save(any(CartItem.class))).thenReturn(cartItem);
        when(cartRepository.findById(cartId)).thenReturn(Optional.of(cart));
        when(cartItemRepository.findByCart(cart)).thenReturn(Arrays.asList(cartItem));

        CartResponse response = cartService.updateCartItem(userId, itemId, request);

        assertThat(response).isNotNull();
        verify(cartItemRepository, times(1)).save(any(CartItem.class));
    }

    /**
     * Test updating cart item with excessive quantity throws exception
     * 
     * Verifies that updating quantity beyond available stock
     * throws InvalidOperationException.
     */
    @Test
    void updateCartItem_WithExcessiveQuantity_ShouldThrowException() {
        UpdateCartItemRequest request = new UpdateCartItemRequest();
        request.setQuantity(20);

        when(cartItemRepository.findByIdWithCartAndProduct(itemId)).thenReturn(Optional.of(cartItem));

        assertThatThrownBy(() -> cartService.updateCartItem(userId, itemId, request))
                .isInstanceOf(InvalidOperationException.class)
                .hasMessageContaining("exceeds available quantity");
    }

    /**
     * Test updating cart item belonging to different user throws exception
     * 
     * Verifies that users cannot update cart items that don't belong to them.
     */
    @Test
    void updateCartItem_WithDifferentUser_ShouldThrowException() {
        UUID differentUserId = UUID.randomUUID();
        UpdateCartItemRequest request = new UpdateCartItemRequest();
        request.setQuantity(3);

        when(cartItemRepository.findByIdWithCartAndProduct(itemId)).thenReturn(Optional.of(cartItem));

        assertThatThrownBy(() -> cartService.updateCartItem(differentUserId, itemId, request))
                .isInstanceOf(InvalidOperationException.class)
                .hasMessageContaining("does not belong to user");
    }

    /**
     * Test successful removal of cart item
     * 
     * Verifies that a cart item can be removed successfully
     * and cart remains if other items exist.
     */
    @Test
    void removeCartItem_WithRemainingItems_ShouldRemoveItemAndKeepCart() {
        when(cartItemRepository.findByIdWithCartAndProduct(itemId)).thenReturn(Optional.of(cartItem));
        doNothing().when(cartItemRepository).delete(cartItem);
        when(cartItemRepository.countByCart(cart)).thenReturn(1L);
        when(cartRepository.findById(cartId)).thenReturn(Optional.of(cart));
        when(cartItemRepository.findByCart(cart)).thenReturn(Arrays.asList());

        CartResponse response = cartService.removeCartItem(userId, itemId);

        assertThat(response).isNotNull();
        verify(cartItemRepository, times(1)).delete(cartItem);
        verify(cartRepository, never()).delete(cart);
    }

    /**
     * Test removing last cart item deletes the cart
     * 
     * Verifies that removing the last item from cart
     * automatically deletes the cart (auto-delete empty cart).
     */
    @Test
    void removeCartItem_WithLastItem_ShouldDeleteCart() {
        when(cartItemRepository.findByIdWithCartAndProduct(itemId)).thenReturn(Optional.of(cartItem));
        doNothing().when(cartItemRepository).delete(cartItem);
        when(cartItemRepository.countByCart(cart)).thenReturn(0L);
        doNothing().when(cartRepository).delete(cart);

        CartResponse response = cartService.removeCartItem(userId, itemId);

        assertThat(response).isNotNull();
        assertThat(response.getCartId()).isNull();
        assertThat(response.getTotalItems()).isEqualTo(0);
        verify(cartItemRepository, times(1)).delete(cartItem);
        verify(cartRepository, times(1)).delete(cart);
    }

    /**
     * Test removing cart item belonging to different user throws exception
     * 
     * Verifies that users cannot remove cart items that don't belong to them.
     */
    @Test
    void removeCartItem_WithDifferentUser_ShouldThrowException() {
        UUID differentUserId = UUID.randomUUID();

        when(cartItemRepository.findByIdWithCartAndProduct(itemId)).thenReturn(Optional.of(cartItem));

        assertThatThrownBy(() -> cartService.removeCartItem(differentUserId, itemId))
                .isInstanceOf(InvalidOperationException.class)
                .hasMessageContaining("does not belong to user");
    }

    /**
     * Test successful retrieval of user's cart
     * 
     * Verifies that a user can retrieve their cart with all items
     * and calculated totals.
     */
    @Test
    void getCart_WithValidUser_ShouldReturnCartWithItems() {
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(cartRepository.findByUserIdWithItems(userId)).thenReturn(Optional.of(cart));
        when(cartRepository.findById(cart.getId())).thenReturn(Optional.of(cart));
        when(cartItemRepository.findByCart(cart)).thenReturn(Arrays.asList(cartItem));

        CartResponse response = cartService.getCart(userId);

        assertThat(response).isNotNull();
        assertThat(response.getCartId()).isEqualTo(cartId);
        verify(cartRepository, times(1)).findByUserIdWithItems(userId);
    }

    /**
     * Test getting cart for non-existent user throws exception
     * 
     * Verifies that attempting to get cart for non-existent user
     * throws ResourceNotFoundException.
     */
    @Test
    void getCart_WithNonExistentUser_ShouldThrowException() {
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> cartService.getCart(userId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("User");
    }

    /**
     * Test getting cart when user has no cart throws exception
     * 
     * Verifies that attempting to get cart when user has no cart
     * throws ResourceNotFoundException.
     */
    @Test
    void getCart_WithNoCart_ShouldThrowException() {
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(cartRepository.findByUserIdWithItems(userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> cartService.getCart(userId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Cart not found");
    }

    /**
     * Test successful cart cleanup on logout
     * 
     * Verifies that cart is deleted when user logs out.
     */
    @Test
    void cleanupCartOnLogout_WithExistingCart_ShouldDeleteCart() {
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(cartRepository.findByUser(user)).thenReturn(Optional.of(cart));
        doNothing().when(cartRepository).delete(cart);

        cartService.cleanupCartOnLogout(userId);

        verify(cartRepository, times(1)).delete(cart);
    }

    /**
     * Test cart cleanup on logout when no cart exists
     * 
     * Verifies that cleanup handles gracefully when user has no cart.
     */
    @Test
    void cleanupCartOnLogout_WithNoCart_ShouldHandleGracefully() {
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(cartRepository.findByUser(user)).thenReturn(Optional.empty());

        cartService.cleanupCartOnLogout(userId);

        verify(cartRepository, never()).delete(any(Cart.class));
    }

    /**
     * Test cart cleanup for non-existent user throws exception
     * 
     * Verifies that attempting cleanup for non-existent user
     * throws ResourceNotFoundException.
     */
    @Test
    void cleanupCartOnLogout_WithNonExistentUser_ShouldThrowException() {
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> cartService.cleanupCartOnLogout(userId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("User");
    }
}