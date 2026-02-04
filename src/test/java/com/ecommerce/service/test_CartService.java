package com.ecommerce.service;

import com.ecommerce.dto.*;
import com.ecommerce.entity.*;
import com.ecommerce.exception.InsufficientStockException;
import com.ecommerce.exception.ResourceNotFoundException;
import com.ecommerce.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Comprehensive JUnit 5 test class for CartService.
 * Tests all public methods with proper mocking of repository dependencies.
 * Covers normal execution paths, edge cases, and exception scenarios.
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

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setUserId(1L);
        testUser.setUsername("testuser");

        testProduct = new Product();
        testProduct.setProductId(1L);
        testProduct.setProductName("Test Product");
        testProduct.setPrice(BigDecimal.valueOf(10.00));
        testProduct.setAvailableQty(100);
        testProduct.setIsActive(true);

        testCart = new Cart();
        testCart.setCartId(1L);
        testCart.setUser(testUser);

        testCartItem = new CartItem();
        testCartItem.setCartItemId(1L);
        testCartItem.setCart(testCart);
        testCartItem.setProduct(testProduct);
        testCartItem.setQuantity(2);
        testCartItem.setPriceAtAddition(BigDecimal.valueOf(10.00));
    }

    /**
     * Test successful cart retrieval with items.
     * Verifies that the service returns cart response with correct calculations.
     */
    @Test
    @DisplayName("Should get cart successfully with items")
    void testGetCart_WithItems() {
        // Given
        Long userId = 1L;
        testCart.setCartItems(Arrays.asList(testCartItem));
        
        when(cartRepository.findByUserIdWithItems(userId)).thenReturn(Optional.of(testCart));

        // When
        CartResponse result = cartService.getCart(userId);

        // Then
        assertNotNull(result);
        assertEquals(userId, result.getUserId());
        assertEquals(1L, result.getCartId());
        assertEquals(1, result.getItemCount());
        assertEquals(BigDecimal.valueOf(20.00), result.getCartTotal());
        assertEquals(1, result.getItems().size());
        verify(cartRepository, times(1)).findByUserIdWithItems(userId);
    }

    /**
     * Test cart retrieval when no cart exists.
     * Verifies that the service returns empty cart response.
     */
    @Test
    @DisplayName("Should return empty cart when no cart exists")
    void testGetCart_NoCart() {
        // Given
        Long userId = 1L;
        
        when(cartRepository.findByUserIdWithItems(userId)).thenReturn(Optional.empty());

        // When
        CartResponse result = cartService.getCart(userId);

        // Then
        assertNotNull(result);
        assertEquals(userId, result.getUserId());
        assertNull(result.getCartId());
        assertEquals(0, result.getItemCount());
        assertEquals(BigDecimal.ZERO, result.getCartTotal());
        assertTrue(result.getItems().isEmpty());
        verify(cartRepository, times(1)).findByUserIdWithItems(userId);
    }

    /**
     * Test successful addition of new cart item.
     * Verifies that the service creates new cart and cart item correctly.
     */
    @Test
    @DisplayName("Should add cart item successfully for new cart")
    void testAddCartItem_NewCart() {
        // Given
        Long userId = 1L;
        AddCartItemRequest request = new AddCartItemRequest();
        request.setProductId(1L);
        request.setQuantity(2);

        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));
        when(cartRepository.findByUser_UserId(userId)).thenReturn(Optional.empty());
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(cartRepository.save(any(Cart.class))).thenReturn(testCart);
        when(cartItemRepository.findByCart_CartIdAndProduct_ProductId(1L, 1L)).thenReturn(Optional.empty());
        when(cartItemRepository.save(any(CartItem.class))).thenReturn(testCartItem);

        // When
        CartItemResponse result = cartService.addCartItem(userId, request);

        // Then
        assertNotNull(result);
        assertEquals(1L, result.getCartItemId());
        assertEquals(1L, result.getProductId());
        assertEquals("Test Product", result.getProductName());
        assertEquals(2, result.getQuantity());
        assertEquals(BigDecimal.valueOf(20.00), result.getItemTotal());
        verify(productRepository, times(1)).findById(1L);
        verify(cartRepository, times(1)).save(any(Cart.class));
        verify(cartItemRepository, times(1)).save(any(CartItem.class));
    }

    /**
     * Test addition of cart item to existing cart.
     * Verifies that the service updates existing cart item quantity.
     */
    @Test
    @DisplayName("Should update existing cart item quantity")
    void testAddCartItem_ExistingItem() {
        // Given
        Long userId = 1L;
        AddCartItemRequest request = new AddCartItemRequest();
        request.setProductId(1L);
        request.setQuantity(3);

        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));
        when(cartRepository.findByUser_UserId(userId)).thenReturn(Optional.of(testCart));
        when(cartItemRepository.findByCart_CartIdAndProduct_ProductId(1L, 1L)).thenReturn(Optional.of(testCartItem));
        
        CartItem updatedCartItem = new CartItem();
        updatedCartItem.setCartItemId(1L);
        updatedCartItem.setCart(testCart);
        updatedCartItem.setProduct(testProduct);
        updatedCartItem.setQuantity(5); // 2 + 3
        updatedCartItem.setPriceAtAddition(BigDecimal.valueOf(10.00));
        
        when(cartItemRepository.save(any(CartItem.class))).thenReturn(updatedCartItem);

        // When
        CartItemResponse result = cartService.addCartItem(userId, request);

        // Then
        assertNotNull(result);
        assertEquals(5, result.getQuantity());
        assertEquals(BigDecimal.valueOf(50.00), result.getItemTotal());
        verify(cartItemRepository, times(1)).save(any(CartItem.class));
    }

    /**
     * Test addition of cart item with insufficient stock.
     * Verifies that the service throws InsufficientStockException.
     */
    @Test
    @DisplayName("Should throw exception for insufficient stock")
    void testAddCartItem_InsufficientStock() {
        // Given
        Long userId = 1L;
        AddCartItemRequest request = new AddCartItemRequest();
        request.setProductId(1L);
        request.setQuantity(200); // More than available

        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));

        // When & Then
        assertThrows(InsufficientStockException.class, () -> {
            cartService.addCartItem(userId, request);
        });
        
        verify(productRepository, times(1)).findById(1L);
        verify(cartRepository, never()).save(any(Cart.class));
    }

    /**
     * Test addition of cart item for non-existent product.
     * Verifies that the service throws ResourceNotFoundException.
     */
    @Test
    @DisplayName("Should throw exception for non-existent product")
    void testAddCartItem_ProductNotFound() {
        // Given
        Long userId = 1L;
        AddCartItemRequest request = new AddCartItemRequest();
        request.setProductId(999L);
        request.setQuantity(1);

        when(productRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(ResourceNotFoundException.class, () -> {
            cartService.addCartItem(userId, request);
        });
        
        verify(productRepository, times(1)).findById(999L);
    }

    /**
     * Test addition of cart item for inactive product.
     * Verifies that the service throws ResourceNotFoundException.
     */
    @Test
    @DisplayName("Should throw exception for inactive product")
    void testAddCartItem_InactiveProduct() {
        // Given
        Long userId = 1L;
        AddCartItemRequest request = new AddCartItemRequest();
        request.setProductId(1L);
        request.setQuantity(1);
        
        testProduct.setIsActive(false);

        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));

        // When & Then
        assertThrows(ResourceNotFoundException.class, () -> {
            cartService.addCartItem(userId, request);
        });
        
        verify(productRepository, times(1)).findById(1L);
    }

    /**
     * Test successful cart item update.
     * Verifies that the service updates cart item quantity correctly.
     */
    @Test
    @DisplayName("Should update cart item successfully")
    void testUpdateCartItem_Success() {
        // Given
        Long userId = 1L;
        Long cartItemId = 1L;
        UpdateCartItemRequest request = new UpdateCartItemRequest();
        request.setQuantity(5);

        when(cartItemRepository.findById(cartItemId)).thenReturn(Optional.of(testCartItem));
        
        CartItem updatedCartItem = new CartItem();
        updatedCartItem.setCartItemId(1L);
        updatedCartItem.setCart(testCart);
        updatedCartItem.setProduct(testProduct);
        updatedCartItem.setQuantity(5);
        updatedCartItem.setPriceAtAddition(BigDecimal.valueOf(10.00));
        
        when(cartItemRepository.save(any(CartItem.class))).thenReturn(updatedCartItem);

        // When
        CartItemResponse result = cartService.updateCartItem(userId, cartItemId, request);

        // Then
        assertNotNull(result);
        assertEquals(5, result.getQuantity());
        assertEquals(BigDecimal.valueOf(50.00), result.getItemTotal());
        verify(cartItemRepository, times(1)).findById(cartItemId);
        verify(cartItemRepository, times(1)).save(any(CartItem.class));
    }

    /**
     * Test cart item update with zero quantity.
     * Verifies that the service removes the item when quantity is zero.
     */
    @Test
    @DisplayName("Should remove cart item when quantity is zero")
    void testUpdateCartItem_ZeroQuantity() {
        // Given
        Long userId = 1L;
        Long cartItemId = 1L;
        UpdateCartItemRequest request = new UpdateCartItemRequest();
        request.setQuantity(0);

        when(cartItemRepository.findById(cartItemId)).thenReturn(Optional.of(testCartItem));
        when(cartItemRepository.countByCart_CartId(1L)).thenReturn(0L);

        // When
        CartItemResponse result = cartService.updateCartItem(userId, cartItemId, request);

        // Then
        assertNotNull(result);
        verify(cartItemRepository, times(1)).delete(testCartItem);
        verify(cartRepository, times(1)).deleteById(1L);
    }

    /**
     * Test cart item update with insufficient stock.
     * Verifies that the service throws InsufficientStockException.
     */
    @Test
    @DisplayName("Should throw exception when updating to quantity exceeding stock")
    void testUpdateCartItem_InsufficientStock() {
        // Given
        Long userId = 1L;
        Long cartItemId = 1L;
        UpdateCartItemRequest request = new UpdateCartItemRequest();
        request.setQuantity(200); // More than available

        when(cartItemRepository.findById(cartItemId)).thenReturn(Optional.of(testCartItem));

        // When & Then
        assertThrows(InsufficientStockException.class, () -> {
            cartService.updateCartItem(userId, cartItemId, request);
        });
        
        verify(cartItemRepository, times(1)).findById(cartItemId);
        verify(cartItemRepository, never()).save(any(CartItem.class));
    }

    /**
     * Test cart item update for wrong user.
     * Verifies that the service throws ResourceNotFoundException.
     */
    @Test
    @DisplayName("Should throw exception when updating cart item for wrong user")
    void testUpdateCartItem_WrongUser() {
        // Given
        Long userId = 2L; // Different user
        Long cartItemId = 1L;
        UpdateCartItemRequest request = new UpdateCartItemRequest();
        request.setQuantity(3);

        when(cartItemRepository.findById(cartItemId)).thenReturn(Optional.of(testCartItem));

        // When & Then
        assertThrows(ResourceNotFoundException.class, () -> {
            cartService.updateCartItem(userId, cartItemId, request);
        });
        
        verify(cartItemRepository, times(1)).findById(cartItemId);
    }

    /**
     * Test successful cart item removal.
     * Verifies that the service removes cart item correctly.
     */
    @Test
    @DisplayName("Should remove cart item successfully")
    void testRemoveCartItem_Success() {
        // Given
        Long userId = 1L;
        Long cartItemId = 1L;

        when(cartItemRepository.findById(cartItemId)).thenReturn(Optional.of(testCartItem));
        when(cartItemRepository.countByCart_CartId(1L)).thenReturn(1L); // Still has items

        // When
        CartItemResponse result = cartService.removeCartItem(userId, cartItemId);

        // Then
        assertNotNull(result);
        assertEquals(1L, result.getCartItemId());
        verify(cartItemRepository, times(1)).delete(testCartItem);
        verify(cartRepository, never()).deleteById(any()); // Cart should not be deleted
    }

    /**
     * Test cart item removal when it's the last item.
     * Verifies that the service removes both cart item and cart.
     */
    @Test
    @DisplayName("Should remove cart when removing last item")
    void testRemoveCartItem_LastItem() {
        // Given
        Long userId = 1L;
        Long cartItemId = 1L;

        when(cartItemRepository.findById(cartItemId)).thenReturn(Optional.of(testCartItem));
        when(cartItemRepository.countByCart_CartId(1L)).thenReturn(0L); // No items left

        // When
        CartItemResponse result = cartService.removeCartItem(userId, cartItemId);

        // Then
        assertNotNull(result);
        verify(cartItemRepository, times(1)).delete(testCartItem);
        verify(cartRepository, times(1)).deleteById(1L); // Cart should be deleted
    }

    /**
     * Test removal of non-existent cart item.
     * Verifies that the service throws ResourceNotFoundException.
     */
    @Test
    @DisplayName("Should throw exception when removing non-existent cart item")
    void testRemoveCartItem_NotFound() {
        // Given
        Long userId = 1L;
        Long cartItemId = 999L;

        when(cartItemRepository.findById(cartItemId)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(ResourceNotFoundException.class, () -> {
            cartService.removeCartItem(userId, cartItemId);
        });
        
        verify(cartItemRepository, times(1)).findById(cartItemId);
    }

    /**
     * Test successful cart clearing.
     * Verifies that the service clears all cart items and removes cart.
     */
    @Test
    @DisplayName("Should clear cart successfully")
    void testClearCart_Success() {
        // Given
        Long userId = 1L;

        when(cartRepository.findByUser_UserId(userId)).thenReturn(Optional.of(testCart));

        // When
        cartService.clearCart(userId);

        // Then
        verify(cartItemRepository, times(1)).deleteByCartId(1L);
        verify(cartRepository, times(1)).delete(testCart);
    }

    /**
     * Test clearing non-existent cart.
     * Verifies that the service handles non-existent cart gracefully.
     */
    @Test
    @DisplayName("Should handle clearing non-existent cart")
    void testClearCart_NoCart() {
        // Given
        Long userId = 1L;

        when(cartRepository.findByUser_UserId(userId)).thenReturn(Optional.empty());

        // When
        cartService.clearCart(userId);

        // Then
        verify(cartItemRepository, never()).deleteByCartId(any());
        verify(cartRepository, never()).delete(any());
    }
}