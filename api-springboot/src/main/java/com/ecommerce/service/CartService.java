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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service for shopping cart operations
 * Implements LLD cart management business logic including:
 * - Lazy cart creation
 * - Auto-delete empty cart
 * - Cart cleanup on logout
 */
@Service
@Transactional
public class CartService {

    private static final Logger logger = LoggerFactory.getLogger(CartService.class);

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private CartItemRepository cartItemRepository;

    @Autowired
    private ProductService productService;

    @Autowired
    private UserService userService;

    /**
     * Add product to cart with lazy cart creation
     * Implements LLD POST /api/cart/items
     */
    public CartResponse addProductToCart(UUID userId, AddToCartRequest request) {
        logger.info("Adding product {} to cart for user {}", request.getProductId(), userId);

        // Validate product exists
        Product product = productService.getProductById(request.getProductId());

        // Validate product is active
        if (!product.getIsActive()) {
            throw new ValidationException("Product is not available");
        }

        // Get or create cart (lazy creation per LLD)
        Cart cart = getOrCreateCart(userId);

        // Check if product already in cart
        Optional<CartItem> existingItem = cartItemRepository.findByCartAndProduct(cart, product);

        if (existingItem.isPresent()) {
            // Update quantity if product already in cart
            CartItem item = existingItem.get();
            item.setQuantity(item.getQuantity() + request.getQuantity());
            cartItemRepository.save(item);
            logger.info("Updated quantity for existing cart item: {}", item.getId());
        } else {
            // Add new item to cart
            CartItem newItem = new CartItem();
            newItem.setCart(cart);
            newItem.setProduct(product);
            newItem.setQuantity(request.getQuantity());
            newItem.setUnitPrice(product.getPrice());
            cartItemRepository.save(newItem);
            logger.info("Added new item to cart: {}", newItem.getId());
        }

        return getCartResponse(userId);
    }

    /**
     * Update cart item quantity
     * Implements LLD PUT /api/cart/items/{item_id}
     */
    public CartResponse updateCartItem(UUID userId, UUID itemId, UpdateCartItemRequest request) {
        logger.info("Updating cart item {} for user {}", itemId, userId);

        // Find cart item and validate ownership
        CartItem item = cartItemRepository.findByIdAndUserId(itemId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart item", "id", itemId));

        // Update quantity
        item.setQuantity(request.getQuantity());
        cartItemRepository.save(item);
        logger.info("Cart item {} updated with new quantity: {}", itemId, request.getQuantity());

        return getCartResponse(userId);
    }

    /**
     * Remove cart item with auto-delete empty cart
     * Implements LLD DELETE /api/cart/items/{item_id}
     */
    public CartResponse removeCartItem(UUID userId, UUID itemId) {
        logger.info("Removing cart item {} for user {}", itemId, userId);

        // Find cart item and validate ownership
        CartItem item = cartItemRepository.findByIdAndUserId(itemId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart item", "id", itemId));

        Cart cart = item.getCart();
        UUID cartId = cart.getId();

        // Remove item
        cartItemRepository.delete(item);
        logger.info("Cart item {} removed", itemId);

        // Check if cart is now empty and auto-delete per LLD
        long remainingItems = cartItemRepository.countByCartId(cartId);
        if (remainingItems == 0) {
            cartRepository.deleteById(cartId);
            logger.info("Cart {} auto-deleted as it became empty", cartId);
            return null; // Return null to indicate cart was deleted
        }

        return getCartResponse(userId);
    }

    /**
     * Get cart for user
     * Implements LLD GET /api/cart
     */
    @Transactional(readOnly = true)
    public CartResponse getCart(UUID userId) {
        logger.info("Fetching cart for user {}", userId);
        return getCartResponse(userId);
    }

    /**
     * Clear cart on logout
     * Implements LLD POST /api/logout cart cleanup
     */
    public void clearCartOnLogout(UUID userId) {
        logger.info("Clearing cart for user {} on logout", userId);

        // Delete all cart items first
        cartItemRepository.deleteByUserId(userId);

        // Delete cart
        cartRepository.deleteByUserId(userId);

        logger.info("Cart cleared successfully for user {}", userId);
    }

    /**
     * Get or create cart (lazy creation per LLD)
     */
    private Cart getOrCreateCart(UUID userId) {
        Optional<Cart> existingCart = cartRepository.findByUserId(userId);

        if (existingCart.isPresent()) {
            return existingCart.get();
        }

        // Create new cart (lazy creation)
        User user = userService.getUserById(userId);
        Cart newCart = new Cart();
        newCart.setUser(user);
        newCart.setExpiresAt(LocalDateTime.now().plusDays(30));
        newCart = cartRepository.save(newCart);
        logger.info("Created new cart {} for user {}", newCart.getId(), userId);

        return newCart;
    }

    /**
     * Build cart response DTO
     */
    @Transactional(readOnly = true)
    private CartResponse getCartResponse(UUID userId) {
        Optional<Cart> cartOpt = cartRepository.findByUserIdWithItems(userId);

        if (cartOpt.isEmpty()) {
            throw new ResourceNotFoundException("Cart not found for user");
        }

        Cart cart = cartOpt.get();
        List<CartItemResponse> items = cart.getItems().stream()
                .map(this::mapToCartItemResponse)
                .collect(Collectors.toList());

        BigDecimal grandTotal = items.stream()
                .map(CartItemResponse::getTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return CartResponse.builder()
                .cartId(cart.getId())
                .items(items)
                .grandTotal(grandTotal)
                .build();
    }

    /**
     * Map CartItem entity to CartItemResponse DTO
     */
    private CartItemResponse mapToCartItemResponse(CartItem item) {
        return CartItemResponse.builder()
                .itemId(item.getId())
                .productId(item.getProduct().getId())
                .name(item.getProduct().getName())
                .quantity(item.getQuantity())
                .price(item.getUnitPrice())
                .total(item.getTotalPrice())
                .build();
    }
}