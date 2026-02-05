package com.ecommerce.service;

import com.ecommerce.dto.*;
import com.ecommerce.entity.Cart;
import com.ecommerce.entity.CartItem;
import com.ecommerce.entity.Product;
import com.ecommerce.exception.BusinessException;
import com.ecommerce.exception.ErrorCode;
import com.ecommerce.repository.CartItemRepository;
import com.ecommerce.repository.CartRepository;
import com.ecommerce.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service for shopping cart operations
 * Implements business logic for cart management
 * Business Rules:
 * - Lazy cart creation (cart created when first item added)
 * - One active cart per user
 * - Cart auto-deleted when empty
 * - Cart and items deleted on logout
 * - Quantity must be > 0
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;

    private static final String ACTIVE_STATUS = "active";

    /**
     * Get user's active cart
     * @param userId the user ID
     * @return cart response
     */
    @Transactional(readOnly = true)
    public CartResponseDTO getCart(UUID userId) {
        log.info("Fetching cart for user: {}", userId);

        Cart cart = cartRepository.findByUserIdAndStatus(userId, ACTIVE_STATUS)
                .orElseThrow(() -> {
                    log.error("Cart not found for user: {}", userId);
                    return new BusinessException(ErrorCode.CART_NOT_FOUND, "Cart not found");
                });

        return buildCartResponse(cart);
    }

    /**
     * Add product to cart
     * Business Rule: Lazy cart creation - cart created if doesn't exist
     * @param userId the user ID
     * @param addToCartDTO product and quantity to add
     * @return updated cart response
     */
    @Transactional
    public CartResponseDTO addProductToCart(UUID userId, AddToCartDTO addToCartDTO) {
        log.info("Adding product {} to cart for user: {}", addToCartDTO.getProductId(), userId);

        // Verify product exists
        Product product = productRepository.findById(addToCartDTO.getProductId())
                .orElseThrow(() -> {
                    log.error("Product not found: {}", addToCartDTO.getProductId());
                    return new BusinessException(ErrorCode.PRODUCT_NOT_FOUND, "Product not found");
                });

        // Validate quantity
        if (addToCartDTO.getQuantity() <= 0) {
            log.error("Invalid quantity: {}", addToCartDTO.getQuantity());
            throw new BusinessException(ErrorCode.INVALID_QUANTITY, 
                "Quantity must be greater than 0");
        }

        // Get or create cart (lazy creation)
        Cart cart = cartRepository.findByUserIdAndStatus(userId, ACTIVE_STATUS)
                .orElseGet(() -> createCart(userId));

        // Check if product already in cart
        CartItem existingItem = cartItemRepository
                .findByCartIdAndProductId(cart.getCartId(), product.getProductId())
                .orElse(null);

        if (existingItem != null) {
            // Update existing item quantity
            existingItem.setQuantity(existingItem.getQuantity() + addToCartDTO.getQuantity());
            existingItem.calculateTotalPrice();
            cartItemRepository.save(existingItem);
            log.info("Updated cart item quantity: {}", existingItem.getCartItemId());
        } else {
            // Create new cart item
            CartItem newItem = new CartItem();
            newItem.setCartId(cart.getCartId());
            newItem.setProductId(product.getProductId());
            newItem.setQuantity(addToCartDTO.getQuantity());
            newItem.setUnitPrice(product.getPrice());
            newItem.calculateTotalPrice();
            cartItemRepository.save(newItem);
            log.info("Added new cart item: {}", newItem.getCartItemId());
        }

        // Recalculate cart totals
        updateCartTotals(cart);

        return buildCartResponse(cart);
    }

    /**
     * Update cart item quantity
     * @param userId the user ID
     * @param itemId the cart item ID
     * @param updateDTO new quantity
     * @return updated cart response
     */
    @Transactional
    public CartResponseDTO updateCartItem(UUID userId, UUID itemId, UpdateCartItemDTO updateDTO) {
        log.info("Updating cart item {} for user: {}", itemId, userId);

        // Validate quantity
        if (updateDTO.getQuantity() <= 0) {
            log.error("Invalid quantity: {}", updateDTO.getQuantity());
            throw new BusinessException(ErrorCode.INVALID_QUANTITY, 
                "Quantity must be greater than 0");
        }

        CartItem cartItem = cartItemRepository.findById(itemId)
                .orElseThrow(() -> {
                    log.error("Cart item not found: {}", itemId);
                    return new BusinessException(ErrorCode.ITEM_NOT_FOUND, "Cart item not found");
                });

        // Verify cart belongs to user
        Cart cart = cartRepository.findById(cartItem.getCartId())
                .orElseThrow(() -> new BusinessException(ErrorCode.CART_NOT_FOUND, "Cart not found"));

        if (!cart.getUserId().equals(userId)) {
            log.error("Cart does not belong to user: {}", userId);
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "Unauthorized access to cart");
        }

        // Update quantity
        cartItem.setQuantity(updateDTO.getQuantity());
        cartItem.calculateTotalPrice();
        cartItemRepository.save(cartItem);
        log.info("Cart item updated: {}", itemId);

        // Recalculate cart totals
        updateCartTotals(cart);

        return buildCartResponse(cart);
    }

    /**
     * Remove cart item
     * Business Rule: Cart auto-deleted if last item removed
     * @param userId the user ID
     * @param itemId the cart item ID
     * @return updated cart response or null if cart deleted
     */
    @Transactional
    public CartResponseDTO removeCartItem(UUID userId, UUID itemId) {
        log.info("Removing cart item {} for user: {}", itemId, userId);

        CartItem cartItem = cartItemRepository.findById(itemId)
                .orElseThrow(() -> {
                    log.error("Cart item not found: {}", itemId);
                    return new BusinessException(ErrorCode.ITEM_NOT_FOUND, "Cart item not found");
                });

        // Verify cart belongs to user
        Cart cart = cartRepository.findById(cartItem.getCartId())
                .orElseThrow(() -> new BusinessException(ErrorCode.CART_NOT_FOUND, "Cart not found"));

        if (!cart.getUserId().equals(userId)) {
            log.error("Cart does not belong to user: {}", userId);
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "Unauthorized access to cart");
        }

        // Remove item
        cartItemRepository.delete(cartItem);
        log.info("Cart item removed: {}", itemId);

        // Check if cart is now empty
        long itemCount = cartItemRepository.countByCartId(cart.getCartId());
        if (itemCount == 0) {
            // Auto-delete empty cart
            cartRepository.delete(cart);
            log.info("Empty cart auto-deleted: {}", cart.getCartId());
            return null; // Return null to indicate cart was deleted
        }

        // Recalculate cart totals
        updateCartTotals(cart);

        return buildCartResponse(cart);
    }

    /**
     * Logout and cleanup cart
     * Business Rule: Cart and all items deleted on logout
     * @param userId the user ID
     */
    @Transactional
    public void logoutAndCleanupCart(UUID userId) {
        log.info("Logging out and cleaning up cart for user: {}", userId);

        Cart cart = cartRepository.findByUserIdAndStatus(userId, ACTIVE_STATUS)
                .orElse(null);

        if (cart != null) {
            // Delete all cart items
            cartItemRepository.deleteByCartId(cart.getCartId());
            // Delete cart
            cartRepository.delete(cart);
            log.info("Cart and items deleted for user: {}", userId);
        } else {
            log.info("No active cart found for user: {}", userId);
        }
    }

    /**
     * Create new cart for user
     * @param userId the user ID
     * @return created cart
     */
    private Cart createCart(UUID userId) {
        log.info("Creating new cart for user: {}", userId);

        Cart cart = new Cart();
        cart.setUserId(userId);
        cart.setStatus(ACTIVE_STATUS);
        cart.setTotalAmount(BigDecimal.ZERO);
        cart.setItemCount(0);
        cart.setExpiresAt(LocalDateTime.now().plusDays(30));

        cart = cartRepository.save(cart);
        log.info("Cart created: {}", cart.getCartId());

        return cart;
    }

    /**
     * Update cart totals based on items
     * @param cart the cart to update
     */
    private void updateCartTotals(Cart cart) {
        List<CartItem> items = cartItemRepository.findByCartId(cart.getCartId());

        BigDecimal totalAmount = items.stream()
                .map(CartItem::getTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        int itemCount = items.stream()
                .mapToInt(CartItem::getQuantity)
                .sum();

        cart.setTotalAmount(totalAmount);
        cart.setItemCount(itemCount);
        cartRepository.save(cart);

        log.info("Cart totals updated: {} items, total: {}", itemCount, totalAmount);
    }

    /**
     * Build cart response DTO
     * @param cart the cart entity
     * @return cart response DTO
     */
    private CartResponseDTO buildCartResponse(Cart cart) {
        List<CartItem> items = cartItemRepository.findByCartId(cart.getCartId());

        List<CartItemResponseDTO> itemDTOs = items.stream()
                .map(item -> CartItemResponseDTO.builder()
                        .itemId(item.getCartItemId())
                        .productId(item.getProductId())
                        .name(item.getProduct().getName())
                        .description(item.getProduct().getDescription())
                        .unitPrice(item.getUnitPrice())
                        .quantity(item.getQuantity())
                        .total(item.getTotalPrice())
                        .build())
                .collect(Collectors.toList());

        return CartResponseDTO.builder()
                .cartId(cart.getCartId())
                .items(itemDTOs)
                .grandTotal(cart.getTotalAmount())
                .itemCount(cart.getItemCount())
                .build();
    }
}