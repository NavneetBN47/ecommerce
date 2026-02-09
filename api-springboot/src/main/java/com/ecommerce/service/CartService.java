package com.ecommerce.service;

import com.ecommerce.dto.AddToCartRequest;
import com.ecommerce.dto.CartItemResponse;
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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Cart Service - Business logic for shopping cart management
 * 
 * Implements:
 * - Lazy cart creation on first add
 * - Auto-delete empty cart
 * - Cart cleanup on logout
 * - One cart per user (1:1 relationship)
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    /**
     * Add product to cart (lazy cart creation)
     */
    @Transactional
    public CartResponse addProductToCart(UUID userId, AddToCartRequest request) {
        log.info("Adding product {} to cart for user {}", request.getProductId(), userId);

        // Get user
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        // Get product
        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", request.getProductId()));

        // Validate product is active
        if (!product.getIsActive()) {
            throw new InvalidOperationException("Product is not available");
        }

        // Validate quantity
        if (request.getQuantity() > product.getAvailableQty()) {
            throw new InvalidOperationException(
                    String.format("Requested quantity %d exceeds available quantity %d", 
                            request.getQuantity(), product.getAvailableQty()));
        }

        // Get or create cart (lazy creation)
        Cart cart = cartRepository.findByUser(user)
                .orElseGet(() -> {
                    log.info("Creating new cart for user {}", userId);
                    Cart newCart = Cart.builder()
                            .user(user)
                            .build();
                    return cartRepository.save(newCart);
                });

        // Check if product already in cart
        Optional<CartItem> existingItem = cartItemRepository.findByCartAndProduct(cart, product);

        if (existingItem.isPresent()) {
            // Update quantity
            CartItem item = existingItem.get();
            int newQuantity = item.getQuantity() + request.getQuantity();
            
            if (newQuantity > product.getAvailableQty()) {
                throw new InvalidOperationException(
                        String.format("Total quantity %d exceeds available quantity %d", 
                                newQuantity, product.getAvailableQty()));
            }
            
            item.setQuantity(newQuantity);
            cartItemRepository.save(item);
            log.info("Updated cart item quantity to {}", newQuantity);
        } else {
            // Add new item
            CartItem newItem = CartItem.builder()
                    .cart(cart)
                    .product(product)
                    .quantity(request.getQuantity())
                    .unitPrice(product.getPrice())
                    .build();
            cartItemRepository.save(newItem);
            log.info("Added new item to cart");
        }

        return getCartResponse(cart.getId());
    }

    /**
     * Update cart item quantity
     */
    @Transactional
    public CartResponse updateCartItem(UUID userId, UUID itemId, UpdateCartItemRequest request) {
        log.info("Updating cart item {} for user {}", itemId, userId);

        // Get cart item with cart and product
        CartItem cartItem = cartItemRepository.findByIdWithCartAndProduct(itemId)
                .orElseThrow(() -> new ResourceNotFoundException("CartItem", "id", itemId));

        // Verify cart belongs to user
        if (!cartItem.getCart().getUser().getId().equals(userId)) {
            throw new InvalidOperationException("Cart item does not belong to user");
        }

        // Validate quantity
        if (request.getQuantity() > cartItem.getProduct().getAvailableQty()) {
            throw new InvalidOperationException(
                    String.format("Requested quantity %d exceeds available quantity %d", 
                            request.getQuantity(), cartItem.getProduct().getAvailableQty()));
        }

        cartItem.setQuantity(request.getQuantity());
        cartItemRepository.save(cartItem);
        log.info("Cart item updated successfully");

        return getCartResponse(cartItem.getCart().getId());
    }

    /**
     * Remove item from cart (auto-delete cart if empty)
     */
    @Transactional
    public CartResponse removeCartItem(UUID userId, UUID itemId) {
        log.info("Removing cart item {} for user {}", itemId, userId);

        // Get cart item with cart
        CartItem cartItem = cartItemRepository.findByIdWithCartAndProduct(itemId)
                .orElseThrow(() -> new ResourceNotFoundException("CartItem", "id", itemId));

        // Verify cart belongs to user
        if (!cartItem.getCart().getUser().getId().equals(userId)) {
            throw new InvalidOperationException("Cart item does not belong to user");
        }

        Cart cart = cartItem.getCart();
        UUID cartId = cart.getId();

        // Remove item
        cartItemRepository.delete(cartItem);
        log.info("Cart item removed successfully");

        // Check if cart is now empty and delete if so
        long itemCount = cartItemRepository.countByCart(cart);
        if (itemCount == 0) {
            log.info("Cart is empty, deleting cart {}", cartId);
            cartRepository.delete(cart);
            return CartResponse.builder()
                    .cartId(null)
                    .items(List.of())
                    .grandTotal(BigDecimal.ZERO)
                    .totalItems(0)
                    .build();
        }

        return getCartResponse(cartId);
    }

    /**
     * Get user's cart
     */
    @Transactional(readOnly = true)
    public CartResponse getCart(UUID userId) {
        log.info("Fetching cart for user {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        Cart cart = cartRepository.findByUserIdWithItems(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart not found for user"));

        return getCartResponse(cart.getId());
    }

    /**
     * Cleanup cart on logout
     */
    @Transactional
    public void cleanupCartOnLogout(UUID userId) {
        log.info("Cleaning up cart for user {} on logout", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        Optional<Cart> cart = cartRepository.findByUser(user);
        if (cart.isPresent()) {
            cartRepository.delete(cart.get());
            log.info("Cart deleted successfully for user {}", userId);
        } else {
            log.info("No cart found for user {}", userId);
        }
    }

    /**
     * Build cart response with items and totals
     */
    private CartResponse getCartResponse(UUID cartId) {
        Cart cart = cartRepository.findById(cartId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart", "id", cartId));

        List<CartItem> items = cartItemRepository.findByCart(cart);

        List<CartItemResponse> itemResponses = items.stream()
                .map(this::mapToCartItemResponse)
                .collect(Collectors.toList());

        BigDecimal grandTotal = items.stream()
                .map(CartItem::getTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return CartResponse.builder()
                .cartId(cart.getId())
                .items(itemResponses)
                .grandTotal(grandTotal)
                .totalItems(items.size())
                .build();
    }

    /**
     * Map CartItem entity to CartItemResponse DTO
     */
    private CartItemResponse mapToCartItemResponse(CartItem item) {
        return CartItemResponse.builder()
                .id(item.getId())
                .productId(item.getProduct().getId())
                .productName(item.getProduct().getName())
                .quantity(item.getQuantity())
                .unitPrice(item.getUnitPrice())
                .totalPrice(item.getTotalPrice())
                .build();
    }
}