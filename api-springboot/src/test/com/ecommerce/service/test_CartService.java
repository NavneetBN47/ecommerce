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
 * Test class for CartService
 * Tests shopping cart operations including add, update, remove, and retrieve functionality
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
    private UUID cartItemId;
    private User testUser;
    private Product testProduct;
    private Cart testCart;
    private CartItem testCartItem;

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
     * Test adding a new product to cart successfully
     */
    @Test
    void testAddProductToCart_NewProduct_Success() {
        AddToCartRequest request = AddToCartRequest.builder()
            .productId(productId)
            .quantity(2)
            .build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(productRepository.findById(productId)).thenReturn(Optional.of(testProduct));
        when(cartRepository.findByUserId(userId)).thenReturn(Optional.of(testCart));
        when(cartItemRepository.findByCartIdAndProductId(cartId, productId)).thenReturn(Optional.empty());
        when(cartItemRepository.save(any(CartItem.class))).thenReturn(testCartItem);

        CartResponse response = cartService.addProductToCart(userId, request);

        assertNotNull(response);
        assertEquals(cartId, response.getCartId());
        verify(userRepository).findById(userId);
        verify(productRepository).findById(productId);
        verify(cartItemRepository).save(any(CartItem.class));
    }

    /**
     * Test adding product to cart when cart doesn't exist (lazy creation)
     */
    @Test
    void testAddProductToCart_CreateNewCart_Success() {
        AddToCartRequest request = AddToCartRequest.builder()
            .productId(productId)
            .quantity(1)
            .build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(productRepository.findById(productId)).thenReturn(Optional.of(testProduct));
        when(cartRepository.findByUserId(userId)).thenReturn(Optional.empty());
        when(cartRepository.save(any(Cart.class))).thenReturn(testCart);
        when(cartItemRepository.findByCartIdAndProductId(any(), any())).thenReturn(Optional.empty());
        when(cartItemRepository.save(any(CartItem.class))).thenReturn(testCartItem);

        CartResponse response = cartService.addProductToCart(userId, request);

        assertNotNull(response);
        verify(cartRepository).save(any(Cart.class));
        verify(cartItemRepository).save(any(CartItem.class));
    }

    /**
     * Test adding existing product to cart (quantity update)
     */
    @Test
    void testAddProductToCart_ExistingProduct_UpdateQuantity() {
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

        CartResponse response = cartService.addProductToCart(userId, request);

        assertNotNull(response);
        assertEquals(5, testCartItem.getQuantity());
        verify(cartItemRepository).save(testCartItem);
    }

    /**
     * Test adding product with invalid quantity (zero)
     */
    @Test
    void testAddProductToCart_ZeroQuantity_ThrowsException() {
        AddToCartRequest request = AddToCartRequest.builder()
            .productId(productId)
            .quantity(0)
            .build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(productRepository.findById(productId)).thenReturn(Optional.of(testProduct));

        ValidationException exception = assertThrows(ValidationException.class, () -> {
            cartService.addProductToCart(userId, request);
        });

        assertEquals("Quantity must be greater than 0", exception.getMessage());
    }

    /**
     * Test adding product with negative quantity
     */
    @Test
    void testAddProductToCart_NegativeQuantity_ThrowsException() {
        AddToCartRequest request = AddToCartRequest.builder()
            .productId(productId)
            .quantity(-1)
            .build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(productRepository.findById(productId)).thenReturn(Optional.of(testProduct));

        ValidationException exception = assertThrows(ValidationException.class, () -> {
            cartService.addProductToCart(userId, request);
        });

        assertEquals("Quantity must be greater than 0", exception.getMessage());
    }

    /**
     * Test adding product when user not found
     */
    @Test
    void testAddProductToCart_UserNotFound_ThrowsException() {
        AddToCartRequest request = AddToCartRequest.builder()
            .productId(productId)
            .quantity(1)
            .build();

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            cartService.addProductToCart(userId, request);
        });

        assertEquals("User not found", exception.getMessage());
    }

    /**
     * Test adding product when product not found
     */
    @Test
    void testAddProductToCart_ProductNotFound_ThrowsException() {
        AddToCartRequest request = AddToCartRequest.builder()
            .productId(productId)
            .quantity(1)
            .build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(productRepository.findById(productId)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            cartService.addProductToCart(userId, request);
        });

        assertEquals("Product not found", exception.getMessage());
    }

    /**
     * Test updating cart item quantity successfully
     */
    @Test
    void testUpdateCartItem_Success() {
        UpdateCartItemRequest request = UpdateCartItemRequest.builder()
            .quantity(5)
            .build();

        testCart.getItems().add(testCartItem);

        when(cartRepository.findByUserId(userId)).thenReturn(Optional.of(testCart));
        when(cartItemRepository.findById(cartItemId)).thenReturn(Optional.of(testCartItem));
        when(cartItemRepository.save(any(CartItem.class))).thenReturn(testCartItem);

        CartResponse response = cartService.updateCartItem(userId, cartItemId, request);

        assertNotNull(response);
        assertEquals(5, testCartItem.getQuantity());
        verify(cartItemRepository).save(testCartItem);
    }

    /**
     * Test updating cart item with invalid quantity
     */
    @Test
    void testUpdateCartItem_InvalidQuantity_ThrowsException() {
        UpdateCartItemRequest request = UpdateCartItemRequest.builder()
            .quantity(0)
            .build();

        when(cartRepository.findByUserId(userId)).thenReturn(Optional.of(testCart));
        when(cartItemRepository.findById(cartItemId)).thenReturn(Optional.of(testCartItem));

        ValidationException exception = assertThrows(ValidationException.class, () -> {
            cartService.updateCartItem(userId, cartItemId, request);
        });

        assertEquals("Quantity must be greater than 0", exception.getMessage());
    }

    /**
     * Test updating cart item that doesn't belong to user
     */
    @Test
    void testUpdateCartItem_ItemNotBelongToUser_ThrowsException() {
        UpdateCartItemRequest request = UpdateCartItemRequest.builder()
            .quantity(5)
            .build();

        Cart anotherCart = Cart.builder()
            .id(UUID.randomUUID())
            .user(testUser)
            .build();

        when(cartRepository.findByUserId(userId)).thenReturn(Optional.of(testCart));
        when(cartItemRepository.findById(cartItemId)).thenReturn(Optional.of(testCartItem));
        testCartItem.setCart(anotherCart);

        ValidationException exception = assertThrows(ValidationException.class, () -> {
            cartService.updateCartItem(userId, cartItemId, request);
        });

        assertEquals("Cart item does not belong to user", exception.getMessage());
    }

    /**
     * Test removing cart item successfully
     */
    @Test
    void testRemoveCartItem_Success() {
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

        CartResponse response = cartService.removeCartItem(userId, cartItemId);

        assertNotNull(response);
        verify(cartItemRepository).delete(testCartItem);
        verify(cartRepository, never()).delete(any());
    }

    /**
     * Test removing last cart item (cart auto-deletion)
     */
    @Test
    void testRemoveCartItem_LastItem_DeleteCart() {
        testCart.getItems().add(testCartItem);

        when(cartRepository.findByUserId(userId)).thenReturn(Optional.of(testCart));
        when(cartItemRepository.findById(cartItemId)).thenReturn(Optional.of(testCartItem));
        doNothing().when(cartItemRepository).delete(testCartItem);
        doNothing().when(cartRepository).delete(testCart);

        CartResponse response = cartService.removeCartItem(userId, cartItemId);

        assertNull(response);
        verify(cartItemRepository).delete(testCartItem);
        verify(cartRepository).delete(testCart);
    }

    /**
     * Test getting cart successfully
     */
    @Test
    void testGetCart_Success() {
        testCart.getItems().add(testCartItem);

        when(cartRepository.findByUserId(userId)).thenReturn(Optional.of(testCart));

        CartResponse response = cartService.getCart(userId);

        assertNotNull(response);
        assertEquals(cartId, response.getCartId());
        assertFalse(response.getItems().isEmpty());
        verify(cartRepository).findByUserId(userId);
    }

    /**
     * Test getting cart when cart not found
     */
    @Test
    void testGetCart_NotFound_ThrowsException() {
        when(cartRepository.findByUserId(userId)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            cartService.getCart(userId);
        });

        assertEquals("Cart not found", exception.getMessage());
    }

    /**
     * Test clearing cart successfully
     */
    @Test
    void testClearCart_Success() {
        doNothing().when(cartRepository).deleteByUserId(userId);

        cartService.clearCart(userId);

        verify(cartRepository).deleteByUserId(userId);
    }

    /**
     * Test cart total calculation
     */
    @Test
    void testGetCart_CalculatesTotalCorrectly() {
        CartItem item1 = CartItem.builder()
            .id(UUID.randomUUID())
            .cart(testCart)
            .product(Product.builder().id(UUID.randomUUID()).name("Product 1").price(new BigDecimal("10.00")).build())
            .quantity(2)
            .build();

        CartItem item2 = CartItem.builder()
            .id(UUID.randomUUID())
            .cart(testCart)
            .product(Product.builder().id(UUID.randomUUID()).name("Product 2").price(new BigDecimal("15.50")).build())
            .quantity(3)
            .build();

        testCart.getItems().add(item1);
        testCart.getItems().add(item2);

        when(cartRepository.findByUserId(userId)).thenReturn(Optional.of(testCart));

        CartResponse response = cartService.getCart(userId);

        assertNotNull(response);
        assertEquals(new BigDecimal("66.50"), response.getGrandTotal());
    }
}