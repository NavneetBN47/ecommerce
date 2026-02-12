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
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * JUnit 5 test class for CartService.
 * Tests all business logic for cart management operations.
 * Mocks repository layer to isolate service logic.
 *
 * @author QA Automation Team
 * @version 1.0
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("CartService Tests")
public class test_CartService {

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
     * Verifies that a new cart item is created and cart totals are updated.
     */
    @Test
    @DisplayName("Should add new item to cart successfully")
    void testAddItemToCart_NewItem_Success() {
        when(cartRepository.findByIdWithItems(testCartId)).thenReturn(Optional.of(testCart));
        when(productRepository.findById(testProductId)).thenReturn(Optional.of(testProduct));
        when(cartItemRepository.findByCartIdAndProductId(testCartId, testProductId))
                .thenReturn(Optional.empty());
        when(cartItemRepository.save(any(CartItem.class))).thenReturn(testCartItem);
        when(cartItemRepository.findByCartId(testCartId)).thenReturn(List.of(testCartItem));
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
     * Verifies that existing item quantity is incremented.
     */
    @Test
    @DisplayName("Should increment quantity when adding existing item")
    void testAddItemToCart_ExistingItem_Success() {
        when(cartRepository.findByIdWithItems(testCartId)).thenReturn(Optional.of(testCart));
        when(productRepository.findById(testProductId)).thenReturn(Optional.of(testProduct));
        when(cartItemRepository.findByCartIdAndProductId(testCartId, testProductId))
                .thenReturn(Optional.of(testCartItem));
        when(cartItemRepository.save(any(CartItem.class))).thenReturn(testCartItem);
        when(cartItemRepository.findByCartId(testCartId)).thenReturn(List.of(testCartItem));
        when(cartRepository.save(any(Cart.class))).thenReturn(testCart);
        when(cartHistoryRepository.save(any(CartHistory.class))).thenReturn(new CartHistory());

        CartDTO result = cartService.addItemToCart(testCartId, testAddItemRequest);

        assertNotNull(result);
        verify(cartItemRepository, times(1)).save(any(CartItem.class));
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
     * Test adding item when total quantity exceeds stock.
     * Verifies that InsufficientStockException is thrown for existing items.
     */
    @Test
    @DisplayName("Should throw InsufficientStockException when total quantity exceeds stock")
    void testAddItemToCart_ExistingItem_InsufficientStock() {
        testProduct.setStockQuantity(3);
        testCartItem.setQuantity(2);
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
     * Verifies that item is deleted and cart totals are updated.
     */
    @Test
    @DisplayName("Should remove item from cart successfully")
    void testRemoveItemFromCart_Success() {
        when(cartRepository.findByIdWithItems(testCartId)).thenReturn(Optional.of(testCart));
        when(cartItemRepository.findById(testItemId)).thenReturn(Optional.of(testCartItem));
        doNothing().when(cartItemRepository).delete(testCartItem);
        when(cartItemRepository.findByCartId(testCartId)).thenReturn(new ArrayList<>());
        when(cartRepository.save(any(Cart.class))).thenReturn(testCart);
        when(cartHistoryRepository.save(any(CartHistory.class))).thenReturn(new CartHistory());

        CartDTO result = cartService.removeItemFromCart(testCartId, testItemId);

        assertNotNull(result);
        assertEquals(testCartId, result.getCartId());
        verify(cartItemRepository, times(1)).delete(testCartItem);
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
     * Verifies that all items are removed and cart totals are reset.
     */
    @Test
    @DisplayName("Should cleanup cart successfully")
    void testCleanupCart_Success() {
        when(cartRepository.findById(testCartId)).thenReturn(Optional.of(testCart));
        doNothing().when(cartItemRepository).deleteByCartId(testCartId);
        when(cartRepository.save(any(Cart.class))).thenReturn(testCart);
        when(cartHistoryRepository.save(any(CartHistory.class))).thenReturn(new CartHistory());

        CartDTO result = cartService.cleanupCart(testCartId);

        assertNotNull(result);
        assertEquals(testCartId, result.getCartId());
        assertEquals(BigDecimal.ZERO, result.getTotalAmount());
        assertEquals(0, result.getTotalItems());
        verify(cartItemRepository, times(1)).deleteByCartId(testCartId);
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
     * Test successful cart view.
     * Verifies that cart with all items is returned.
     */
    @Test
    @DisplayName("Should view cart successfully")
    void testViewCart_Success() {
        List<CartItem> items = List.of(testCartItem);
        testCart.setItems(items);
        when(cartRepository.findByIdWithItems(testCartId)).thenReturn(Optional.of(testCart));
        when(cartItemRepository.findByCartId(testCartId)).thenReturn(items);
        when(productRepository.findById(testProductId)).thenReturn(Optional.of(testProduct));

        CartDTO result = cartService.viewCart(testCartId);

        assertNotNull(result);
        assertEquals(testCartId, result.getCartId());
        assertEquals(testUserId, result.getUserId());
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
     * Test creating new cart for user when none exists.
     * Verifies lazy cart creation functionality.
     */
    @Test
    @DisplayName("Should create new cart when user has no active cart")
    void testGetOrCreateCart_NewCart() {
        when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));
        when(cartRepository.findByUserIdAndCartStatus(testUserId, Cart.CartStatus.ACTIVE))
                .thenReturn(Optional.empty());
        when(cartRepository.save(any(Cart.class))).thenReturn(testCart);
        when(cartHistoryRepository.save(any(CartHistory.class))).thenReturn(new CartHistory());
        when(cartItemRepository.findByCartId(any())).thenReturn(new ArrayList<>());

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

        verify(cartRepository, never()).save(any());
    }

    /**
     * Test logout cleanup with empty cart.
     * Verifies that empty carts are marked as abandoned.
     */
    @Test
    @DisplayName("Should mark empty cart as abandoned during logout cleanup")
    void testLogoutCleanup_EmptyCart() {
        testCart.setItems(new ArrayList<>());
        when(cartRepository.findByUserId(testUserId)).thenReturn(List.of(testCart));
        when(cartRepository.save(any(Cart.class))).thenReturn(testCart);

        cartService.logoutCleanup(testUserId);

        verify(cartRepository, times(1)).save(any(Cart.class));
    }

    /**
     * Test logout cleanup with non-empty cart.
     * Verifies that non-empty carts are not marked as abandoned.
     */
    @Test
    @DisplayName("Should not mark non-empty cart as abandoned during logout cleanup")
    void testLogoutCleanup_NonEmptyCart() {
        testCart.setItems(List.of(testCartItem));
        when(cartRepository.findByUserId(testUserId)).thenReturn(List.of(testCart));

        cartService.logoutCleanup(testUserId);

        verify(cartRepository, never()).save(any(Cart.class));
    }

    /**
     * Test logout cleanup with no active carts.
     * Verifies that method handles users with no carts gracefully.
     */
    @Test
    @DisplayName("Should handle logout cleanup when user has no carts")
    void testLogoutCleanup_NoCarts() {
        when(cartRepository.findByUserId(testUserId)).thenReturn(new ArrayList<>());

        cartService.logoutCleanup(testUserId);

        verify(cartRepository, never()).save(any());
    }

    /**
     * Test logout cleanup with multiple carts.
     * Verifies that all empty carts are marked as abandoned.
     */
    @Test
    @DisplayName("Should mark all empty carts as abandoned during logout cleanup")
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

        when(cartRepository.findByUserId(testUserId)).thenReturn(List.of(emptyCart1, emptyCart2));
        when(cartRepository.save(any(Cart.class))).thenReturn(emptyCart1, emptyCart2);

        cartService.logoutCleanup(testUserId);

        verify(cartRepository, times(2)).save(any(Cart.class));
    }
}