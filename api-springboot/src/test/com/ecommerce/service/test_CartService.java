package com.ecommerce.service;

import com.ecommerce.dto.AddItemRequest;
import com.ecommerce.dto.CartDTO;
import com.ecommerce.entity.*;
import com.ecommerce.exception.*;
import com.ecommerce.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * JUnit 5 test class for CartService.
 * Tests all business logic for cart management including item operations,
 * cart lifecycle, validation, and transaction handling.
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

    @Mock
    private CartHistoryRepository cartHistoryRepository;

    @InjectMocks
    private CartService cartService;

    private UUID testCartId;
    private UUID testUserId;
    private UUID testProductId;
    private UUID testItemId;
    private Cart testCart;
    private Product testProduct;
    private CartItem testCartItem;
    private User testUser;
    private AddItemRequest testAddItemRequest;

    /**
     * Set up test data before each test execution.
     * Initializes common test entities and mock objects.
     */
    @BeforeEach
    void setUp() {
        testCartId = UUID.randomUUID();
        testUserId = UUID.randomUUID();
        testProductId = UUID.randomUUID();
        testItemId = UUID.randomUUID();

        // Initialize test Cart
        testCart = new Cart();
        testCart.setCartId(testCartId);
        testCart.setUserId(testUserId);
        testCart.setCartStatus(Cart.CartStatus.ACTIVE);
        testCart.setTotalAmount(BigDecimal.ZERO);
        testCart.setTotalItems(0);
        testCart.setCurrencyCode("USD");
        testCart.setCreatedAt(LocalDateTime.now());
        testCart.setUpdatedAt(LocalDateTime.now());
        testCart.setExpiresAt(LocalDateTime.now().plusDays(30));
        testCart.setItems(new ArrayList<>());

        // Initialize test Product
        testProduct = new Product();
        testProduct.setProductId(testProductId);
        testProduct.setProductName("Test Product");
        testProduct.setPrice(BigDecimal.valueOf(50.00));
        testProduct.setStockQuantity(100);
        testProduct.setIsActive(true);

        // Initialize test CartItem
        testCartItem = new CartItem();
        testCartItem.setCartItemId(testItemId);
        testCartItem.setCartId(testCartId);
        testCartItem.setProductId(testProductId);
        testCartItem.setQuantity(2);
        testCartItem.setUnitPrice(BigDecimal.valueOf(50.00));
        testCartItem.setAddedAt(LocalDateTime.now());

        // Initialize test User
        testUser = new User();
        testUser.setUserId(testUserId);
        testUser.setEmail("test@example.com");

        // Initialize test AddItemRequest
        testAddItemRequest = new AddItemRequest();
        testAddItemRequest.setProductId(testProductId);
        testAddItemRequest.setQuantity(2);
    }

    /**
     * Test successful addition of new item to cart.
     * Verifies that item is added, totals are updated, and history is recorded.
     */
    @Test
    @DisplayName("Should successfully add new item to cart")
    void testAddItemToCart_NewItem_Success() {
        when(cartRepository.findByIdWithItems(testCartId)).thenReturn(Optional.of(testCart));
        when(productRepository.findById(testProductId)).thenReturn(Optional.of(testProduct));
        when(cartItemRepository.findByCartIdAndProductId(testCartId, testProductId))
                .thenReturn(Optional.empty());
        when(cartItemRepository.save(any(CartItem.class))).thenReturn(testCartItem);
        when(cartItemRepository.findByCartId(testCartId)).thenReturn(Arrays.asList(testCartItem));
        when(cartRepository.save(any(Cart.class))).thenReturn(testCart);
        when(cartHistoryRepository.save(any(CartHistory.class))).thenReturn(new CartHistory());

        CartDTO result = cartService.addItemToCart(testCartId, testAddItemRequest);

        assertNotNull(result);
        assertEquals(testCartId, result.getCartId());
        verify(cartItemRepository, times(1)).save(any(CartItem.class));
        verify(cartRepository, times(1)).save(any(Cart.class));
        verify(cartHistoryRepository, times(1)).save(any(CartHistory.class));
    }

    /**
     * Test adding item to cart when item already exists.
     * Verifies that quantity is incremented instead of creating duplicate.
     */
    @Test
    @DisplayName("Should increment quantity when adding existing item")
    void testAddItemToCart_ExistingItem_IncrementQuantity() {
        testCartItem.setQuantity(3);
        when(cartRepository.findByIdWithItems(testCartId)).thenReturn(Optional.of(testCart));
        when(productRepository.findById(testProductId)).thenReturn(Optional.of(testProduct));
        when(cartItemRepository.findByCartIdAndProductId(testCartId, testProductId))
                .thenReturn(Optional.of(testCartItem));
        when(cartItemRepository.save(any(CartItem.class))).thenReturn(testCartItem);
        when(cartItemRepository.findByCartId(testCartId)).thenReturn(Arrays.asList(testCartItem));
        when(cartRepository.save(any(Cart.class))).thenReturn(testCart);
        when(cartHistoryRepository.save(any(CartHistory.class))).thenReturn(new CartHistory());

        CartDTO result = cartService.addItemToCart(testCartId, testAddItemRequest);

        assertNotNull(result);
        verify(cartItemRepository, times(1)).save(argThat(item -> 
            item.getQuantity() == 5 // 3 existing + 2 new
        ));
        verify(cartHistoryRepository, times(1)).save(any(CartHistory.class));
    }

    /**
     * Test adding item to non-existent cart.
     * Verifies that CartNotFoundException is thrown.
     */
    @Test
    @DisplayName("Should throw CartNotFoundException when cart does not exist")
    void testAddItemToCart_CartNotFound() {
        when(cartRepository.findByIdWithItems(testCartId)).thenReturn(Optional.empty());

        assertThrows(CartNotFoundException.class, () -> {
            cartService.addItemToCart(testCartId, testAddItemRequest);
        });

        verify(productRepository, never()).findById(any());
        verify(cartItemRepository, never()).save(any());
    }

    /**
     * Test adding non-existent product to cart.
     * Verifies that ProductNotFoundException is thrown.
     */
    @Test
    @DisplayName("Should throw ProductNotFoundException when product does not exist")
    void testAddItemToCart_ProductNotFound() {
        when(cartRepository.findByIdWithItems(testCartId)).thenReturn(Optional.of(testCart));
        when(productRepository.findById(testProductId)).thenReturn(Optional.empty());

        assertThrows(ProductNotFoundException.class, () -> {
            cartService.addItemToCart(testCartId, testAddItemRequest);
        });

        verify(cartItemRepository, never()).save(any());
    }

    /**
     * Test adding inactive product to cart.
     * Verifies that ProductNotAvailableException is thrown.
     */
    @Test
    @DisplayName("Should throw ProductNotAvailableException when product is inactive")
    void testAddItemToCart_ProductInactive() {
        testProduct.setIsActive(false);
        when(cartRepository.findByIdWithItems(testCartId)).thenReturn(Optional.of(testCart));
        when(productRepository.findById(testProductId)).thenReturn(Optional.of(testProduct));

        assertThrows(ProductNotAvailableException.class, () -> {
            cartService.addItemToCart(testCartId, testAddItemRequest);
        });

        verify(cartItemRepository, never()).save(any());
    }

    /**
     * Test adding item with insufficient stock.
     * Verifies that InsufficientStockException is thrown.
     */
    @Test
    @DisplayName("Should throw InsufficientStockException when stock is insufficient")
    void testAddItemToCart_InsufficientStock() {
        testProduct.setStockQuantity(1);
        testAddItemRequest.setQuantity(5);
        when(cartRepository.findByIdWithItems(testCartId)).thenReturn(Optional.of(testCart));
        when(productRepository.findById(testProductId)).thenReturn(Optional.of(testProduct));

        assertThrows(InsufficientStockException.class, () -> {
            cartService.addItemToCart(testCartId, testAddItemRequest);
        });

        verify(cartItemRepository, never()).save(any());
    }

    /**
     * Test adding item that would exceed stock when combined with existing quantity.
     * Verifies that InsufficientStockException is thrown.
     */
    @Test
    @DisplayName("Should throw InsufficientStockException when total quantity exceeds stock")
    void testAddItemToCart_ExceedsStockWithExisting() {
        testProduct.setStockQuantity(10);
        testCartItem.setQuantity(9);
        testAddItemRequest.setQuantity(2);
        when(cartRepository.findByIdWithItems(testCartId)).thenReturn(Optional.of(testCart));
        when(productRepository.findById(testProductId)).thenReturn(Optional.of(testProduct));
        when(cartItemRepository.findByCartIdAndProductId(testCartId, testProductId))
                .thenReturn(Optional.of(testCartItem));

        assertThrows(InsufficientStockException.class, () -> {
            cartService.addItemToCart(testCartId, testAddItemRequest);
        });

        verify(cartItemRepository, never()).save(any());
    }

    /**
     * Test successful removal of item from cart.
     * Verifies that item is deleted, totals are updated, and history is recorded.
     */
    @Test
    @DisplayName("Should successfully remove item from cart")
    void testRemoveItemFromCart_Success() {
        when(cartRepository.findByIdWithItems(testCartId)).thenReturn(Optional.of(testCart));
        when(cartItemRepository.findById(testItemId)).thenReturn(Optional.of(testCartItem));
        doNothing().when(cartItemRepository).delete(any(CartItem.class));
        when(cartItemRepository.findByCartId(testCartId)).thenReturn(new ArrayList<>());
        when(cartRepository.save(any(Cart.class))).thenReturn(testCart);
        when(cartHistoryRepository.save(any(CartHistory.class))).thenReturn(new CartHistory());

        CartDTO result = cartService.removeItemFromCart(testCartId, testItemId);

        assertNotNull(result);
        assertEquals(testCartId, result.getCartId());
        verify(cartItemRepository, times(1)).delete(testCartItem);
        verify(cartRepository, times(1)).save(any(Cart.class));
        verify(cartHistoryRepository, times(1)).save(any(CartHistory.class));
    }

    /**
     * Test removing item from non-existent cart.
     * Verifies that CartNotFoundException is thrown.
     */
    @Test
    @DisplayName("Should throw CartNotFoundException when removing from non-existent cart")
    void testRemoveItemFromCart_CartNotFound() {
        when(cartRepository.findByIdWithItems(testCartId)).thenReturn(Optional.empty());

        assertThrows(CartNotFoundException.class, () -> {
            cartService.removeItemFromCart(testCartId, testItemId);
        });

        verify(cartItemRepository, never()).delete(any());
    }

    /**
     * Test removing non-existent item from cart.
     * Verifies that CartItemNotFoundException is thrown.
     */
    @Test
    @DisplayName("Should throw CartItemNotFoundException when item does not exist")
    void testRemoveItemFromCart_ItemNotFound() {
        when(cartRepository.findByIdWithItems(testCartId)).thenReturn(Optional.of(testCart));
        when(cartItemRepository.findById(testItemId)).thenReturn(Optional.empty());

        assertThrows(CartItemNotFoundException.class, () -> {
            cartService.removeItemFromCart(testCartId, testItemId);
        });

        verify(cartItemRepository, never()).delete(any());
    }

    /**
     * Test removing item that belongs to different cart.
     * Verifies that CartItemNotFoundException is thrown.
     */
    @Test
    @DisplayName("Should throw CartItemNotFoundException when item belongs to different cart")
    void testRemoveItemFromCart_ItemBelongsToDifferentCart() {
        testCartItem.setCartId(UUID.randomUUID());
        when(cartRepository.findByIdWithItems(testCartId)).thenReturn(Optional.of(testCart));
        when(cartItemRepository.findById(testItemId)).thenReturn(Optional.of(testCartItem));

        assertThrows(CartItemNotFoundException.class, () -> {
            cartService.removeItemFromCart(testCartId, testItemId);
        });

        verify(cartItemRepository, never()).delete(any());
    }

    /**
     * Test successful cart cleanup.
     * Verifies that all items are removed and totals are reset.
     */
    @Test
    @DisplayName("Should successfully cleanup cart")
    void testCleanupCart_Success() {
        when(cartRepository.findById(testCartId)).thenReturn(Optional.of(testCart));
        doNothing().when(cartItemRepository).deleteByCartId(testCartId);
        when(cartRepository.save(any(Cart.class))).thenReturn(testCart);
        when(cartHistoryRepository.save(any(CartHistory.class))).thenReturn(new CartHistory());

        CartDTO result = cartService.cleanupCart(testCartId);

        assertNotNull(result);
        assertEquals(BigDecimal.ZERO, result.getTotalAmount());
        assertEquals(0, result.getTotalItems());
        verify(cartItemRepository, times(1)).deleteByCartId(testCartId);
        verify(cartRepository, times(1)).save(any(Cart.class));
        verify(cartHistoryRepository, times(1)).save(any(CartHistory.class));
    }

    /**
     * Test cleanup of non-existent cart.
     * Verifies that CartNotFoundException is thrown.
     */
    @Test
    @DisplayName("Should throw CartNotFoundException when cleaning up non-existent cart")
    void testCleanupCart_CartNotFound() {
        when(cartRepository.findById(testCartId)).thenReturn(Optional.empty());

        assertThrows(CartNotFoundException.class, () -> {
            cartService.cleanupCart(testCartId);
        });

        verify(cartItemRepository, never()).deleteByCartId(any());
    }

    /**
     * Test successful cart viewing.
     * Verifies that cart with all items is returned.
     */
    @Test
    @DisplayName("Should successfully view cart")
    void testViewCart_Success() {
        when(cartRepository.findByIdWithItems(testCartId)).thenReturn(Optional.of(testCart));
        when(cartItemRepository.findByCartId(testCartId)).thenReturn(Arrays.asList(testCartItem));
        when(productRepository.findById(testProductId)).thenReturn(Optional.of(testProduct));

        CartDTO result = cartService.viewCart(testCartId);

        assertNotNull(result);
        assertEquals(testCartId, result.getCartId());
        assertEquals(testUserId, result.getUserId());
        assertFalse(result.getItems().isEmpty());
        verify(cartRepository, times(1)).findByIdWithItems(testCartId);
    }

    /**
     * Test viewing non-existent cart.
     * Verifies that CartNotFoundException is thrown.
     */
    @Test
    @DisplayName("Should throw CartNotFoundException when viewing non-existent cart")
    void testViewCart_CartNotFound() {
        when(cartRepository.findByIdWithItems(testCartId)).thenReturn(Optional.empty());

        assertThrows(CartNotFoundException.class, () -> {
            cartService.viewCart(testCartId);
        });
    }

    /**
     * Test viewing empty cart.
     * Verifies that empty cart DTO is returned correctly.
     */
    @Test
    @DisplayName("Should successfully view empty cart")
    void testViewCart_EmptyCart() {
        when(cartRepository.findByIdWithItems(testCartId)).thenReturn(Optional.of(testCart));
        when(cartItemRepository.findByCartId(testCartId)).thenReturn(new ArrayList<>());

        CartDTO result = cartService.viewCart(testCartId);

        assertNotNull(result);
        assertTrue(result.getItems().isEmpty());
        assertEquals(0, result.getTotalItems());
    }

    /**
     * Test getting existing active cart for user.
     * Verifies that existing cart is returned without creating new one.
     */
    @Test
    @DisplayName("Should return existing active cart for user")
    void testGetOrCreateCart_ExistingCart() {
        when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));
        when(cartRepository.findByUserIdAndCartStatus(testUserId, Cart.CartStatus.ACTIVE))
                .thenReturn(Optional.of(testCart));
        when(cartItemRepository.findByCartId(testCartId)).thenReturn(new ArrayList<>());

        CartDTO result = cartService.getOrCreateCart(testUserId);

        assertNotNull(result);
        assertEquals(testCartId, result.getCartId());
        assertEquals(testUserId, result.getUserId());
        verify(cartRepository, never()).save(any(Cart.class));
    }

    /**
     * Test creating new cart for user without active cart.
     * Verifies that new cart is created and saved.
     */
    @Test
    @DisplayName("Should create new cart when user has no active cart")
    void testGetOrCreateCart_CreateNewCart() {
        when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));
        when(cartRepository.findByUserIdAndCartStatus(testUserId, Cart.CartStatus.ACTIVE))
                .thenReturn(Optional.empty());
        when(cartRepository.save(any(Cart.class))).thenReturn(testCart);
        when(cartItemRepository.findByCartId(any())).thenReturn(new ArrayList<>());
        when(cartHistoryRepository.save(any(CartHistory.class))).thenReturn(new CartHistory());

        CartDTO result = cartService.getOrCreateCart(testUserId);

        assertNotNull(result);
        assertEquals(testUserId, result.getUserId());
        verify(cartRepository, times(1)).save(any(Cart.class));
        verify(cartHistoryRepository, times(1)).save(any(CartHistory.class));
    }

    /**
     * Test get or create cart for non-existent user.
     * Verifies that UserNotFoundException is thrown.
     */
    @Test
    @DisplayName("Should throw UserNotFoundException when user does not exist")
    void testGetOrCreateCart_UserNotFound() {
        when(userRepository.findById(testUserId)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> {
            cartService.getOrCreateCart(testUserId);
        });

        verify(cartRepository, never()).findByUserIdAndCartStatus(any(), any());
        verify(cartRepository, never()).save(any());
    }

    /**
     * Test successful logout cleanup.
     * Verifies that empty active carts are marked as abandoned.
     */
    @Test
    @DisplayName("Should mark empty carts as abandoned during logout")
    void testLogoutCleanup_Success() {
        Cart emptyCart = new Cart();
        emptyCart.setCartId(UUID.randomUUID());
        emptyCart.setUserId(testUserId);
        emptyCart.setCartStatus(Cart.CartStatus.ACTIVE);
        emptyCart.setItems(new ArrayList<>());

        when(cartRepository.findByUserId(testUserId)).thenReturn(Arrays.asList(emptyCart));
        when(cartRepository.save(any(Cart.class))).thenReturn(emptyCart);

        cartService.logoutCleanup(testUserId);

        verify(cartRepository, times(1)).save(argThat(cart -> 
            cart.getCartStatus() == Cart.CartStatus.ABANDONED
        ));
    }

    /**
     * Test logout cleanup with non-empty cart.
     * Verifies that non-empty carts are not marked as abandoned.
     */
    @Test
    @DisplayName("Should not mark non-empty carts as abandoned during logout")
    void testLogoutCleanup_NonEmptyCart() {
        testCart.setItems(Arrays.asList(testCartItem));
        when(cartRepository.findByUserId(testUserId)).thenReturn(Arrays.asList(testCart));

        cartService.logoutCleanup(testUserId);

        verify(cartRepository, never()).save(any(Cart.class));
    }

    /**
     * Test logout cleanup with no active carts.
     * Verifies that cleanup handles empty list gracefully.
     */
    @Test
    @DisplayName("Should handle logout cleanup when user has no active carts")
    void testLogoutCleanup_NoActiveCarts() {
        Cart inactiveCart = new Cart();
        inactiveCart.setCartStatus(Cart.CartStatus.ABANDONED);
        when(cartRepository.findByUserId(testUserId)).thenReturn(Arrays.asList(inactiveCart));

        cartService.logoutCleanup(testUserId);

        verify(cartRepository, never()).save(any(Cart.class));
    }

    /**
     * Test logout cleanup with multiple carts.
     * Verifies that only empty active carts are marked as abandoned.
     */
    @Test
    @DisplayName("Should handle logout cleanup with multiple carts")
    void testLogoutCleanup_MultipleCarts() {
        Cart emptyCart1 = new Cart();
        emptyCart1.setCartId(UUID.randomUUID());
        emptyCart1.setUserId(testUserId);
        emptyCart1.setCartStatus(Cart.CartStatus.ACTIVE);
        emptyCart1.setItems(new ArrayList<>());

        Cart emptyCart2 = new Cart();
        emptyCart2.setCartId(UUID.randomUUID());
        emptyCart2.setUserId(testUserId);
        emptyCart2.setCartStatus(Cart.CartStatus.ACTIVE);
        emptyCart2.setItems(new ArrayList<>());

        Cart nonEmptyCart = new Cart();
        nonEmptyCart.setCartId(UUID.randomUUID());
        nonEmptyCart.setUserId(testUserId);
        nonEmptyCart.setCartStatus(Cart.CartStatus.ACTIVE);
        nonEmptyCart.setItems(Arrays.asList(testCartItem));

        when(cartRepository.findByUserId(testUserId))
                .thenReturn(Arrays.asList(emptyCart1, emptyCart2, nonEmptyCart));
        when(cartRepository.save(any(Cart.class))).thenAnswer(i -> i.getArguments()[0]);

        cartService.logoutCleanup(testUserId);

        verify(cartRepository, times(2)).save(argThat(cart -> 
            cart.getCartStatus() == Cart.CartStatus.ABANDONED
        ));
    }
}