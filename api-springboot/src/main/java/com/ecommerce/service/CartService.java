package com.ecommerce.service;

import com.ecommerce.dto.AddToCartRequest;
import com.ecommerce.dto.CartDTO;
import com.ecommerce.dto.CartItemDTO;
import com.ecommerce.dto.UpdateCartItemRequest;
import com.ecommerce.entity.Cart;
import com.ecommerce.entity.CartItem;
import com.ecommerce.entity.Product;
import com.ecommerce.entity.User;
import com.ecommerce.exception.InsufficientStockException;
import com.ecommerce.exception.ResourceNotFoundException;
import com.ecommerce.repository.CartItemRepository;
import com.ecommerce.repository.CartRepository;
import com.ecommerce.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for Cart operations
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final UserRepository userRepository;
    private final ProductService productService;

    /**
     * Get or create cart for user (lazy creation)
     */
    @Transactional
    public CartDTO getOrCreateCart(Long userId) {
        log.info("Getting or creating cart for user: {}", userId);

        Cart cart = cartRepository.findByUserUserId(userId)
            .orElseGet(() -> createNewCart(userId));

        return mapToDTO(cart);
    }

    /**
     * Add item to cart
     */
    @Transactional
    public CartDTO addItemToCart(Long userId, AddToCartRequest request) {
        log.info("Adding item to cart - User: {}, Product: {}, Quantity: {}", 
                 userId, request.getProductId(), request.getQuantity());

        // Get or create cart
        Cart cart = cartRepository.findByUserUserId(userId)
            .orElseGet(() -> createNewCart(userId));

        // Get product and validate stock
        Product product = productService.getProductEntityById(request.getProductId());
        
        if (!product.getIsActive()) {
            throw new ResourceNotFoundException("Product is not available: " + product.getProductId());
        }

        // Check if item already exists in cart
        CartItem cartItem = cartItemRepository
            .findByCartCartIdAndProductProductId(cart.getCartId(), product.getProductId())
            .orElse(null);

        if (cartItem != null) {
            // Update existing item
            int newQuantity = cartItem.getQuantity() + request.getQuantity();
            validateStock(product, newQuantity);
            cartItem.setQuantity(newQuantity);
        } else {
            // Create new cart item
            validateStock(product, request.getQuantity());
            cartItem = CartItem.builder()
                .cart(cart)
                .product(product)
                .quantity(request.getQuantity())
                .price(product.getPrice())
                .build();
            cart.getItems().add(cartItem);
        }

        cartItemRepository.save(cartItem);
        
        // Recalculate cart totals
        cart.recalculateTotals();
        cart = cartRepository.save(cart);

        log.info("Item added to cart successfully");
        return mapToDTO(cart);
    }

    /**
     * Update cart item quantity
     */
    @Transactional
    public CartDTO updateCartItem(Long userId, Long cartItemId, UpdateCartItemRequest request) {
        log.info("Updating cart item: {} for user: {}", cartItemId, userId);

        Cart cart = cartRepository.findByUserUserId(userId)
            .orElseThrow(() -> new ResourceNotFoundException("Cart not found for user: " + userId));

        CartItem cartItem = cartItemRepository.findById(cartItemId)
            .orElseThrow(() -> new ResourceNotFoundException("Cart item not found: " + cartItemId));

        // Verify cart item belongs to user's cart
        if (!cartItem.getCart().getCartId().equals(cart.getCartId())) {
            throw new ResourceNotFoundException("Cart item does not belong to user's cart");
        }

        // Validate stock
        validateStock(cartItem.getProduct(), request.getQuantity());

        cartItem.setQuantity(request.getQuantity());
        cartItemRepository.save(cartItem);

        // Recalculate cart totals
        cart.recalculateTotals();
        cart = cartRepository.save(cart);

        log.info("Cart item updated successfully");
        return mapToDTO(cart);
    }

    /**
     * Remove item from cart
     */
    @Transactional
    public CartDTO removeItemFromCart(Long userId, Long cartItemId) {
        log.info("Removing cart item: {} for user: {}", cartItemId, userId);

        Cart cart = cartRepository.findByUserUserId(userId)
            .orElseThrow(() -> new ResourceNotFoundException("Cart not found for user: " + userId));

        CartItem cartItem = cartItemRepository.findById(cartItemId)
            .orElseThrow(() -> new ResourceNotFoundException("Cart item not found: " + cartItemId));

        // Verify cart item belongs to user's cart
        if (!cartItem.getCart().getCartId().equals(cart.getCartId())) {
            throw new ResourceNotFoundException("Cart item does not belong to user's cart");
        }

        cart.getItems().remove(cartItem);
        cartItemRepository.delete(cartItem);

        // Recalculate cart totals
        cart.recalculateTotals();

        // Auto-delete cart if empty
        if (cart.isEmpty()) {
            log.info("Cart is empty, deleting cart: {}", cart.getCartId());
            cartRepository.delete(cart);
            return CartDTO.builder()
                .userId(userId)
                .status("EMPTY")
                .totalItems(0)
                .build();
        }

        cart = cartRepository.save(cart);
        log.info("Cart item removed successfully");
        return mapToDTO(cart);
    }

    /**
     * Clear all items from cart
     */
    @Transactional
    public void clearCart(Long userId) {
        log.info("Clearing cart for user: {}", userId);

        Cart cart = cartRepository.findByUserUserId(userId)
            .orElseThrow(() -> new ResourceNotFoundException("Cart not found for user: " + userId));

        cartItemRepository.deleteByCartCartId(cart.getCartId());
        cartRepository.delete(cart);

        log.info("Cart cleared successfully");
    }

    /**
     * Cleanup empty cart (called on logout)
     */
    @Transactional
    public void cleanupEmptyCart(Long userId) {
        log.info("Cleaning up empty cart for user: {}", userId);

        cartRepository.findByUserUserId(userId).ifPresent(cart -> {
            if (cart.isEmpty()) {
                log.info("Deleting empty cart: {}", cart.getCartId());
                cartRepository.delete(cart);
            }
        });
    }

    /**
     * Create new cart for user
     */
    private Cart createNewCart(Long userId) {
        log.info("Creating new cart for user: {}", userId);

        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));

        Cart cart = Cart.builder()
            .user(user)
            .status("ACTIVE")
            .build();

        return cartRepository.save(cart);
    }

    /**
     * Validate product stock availability
     */
    private void validateStock(Product product, int requestedQuantity) {
        if (product.getStockQuantity() < requestedQuantity) {
            throw new InsufficientStockException(
                String.format("Insufficient stock for product %s. Available: %d, Requested: %d",
                    product.getProductName(), product.getStockQuantity(), requestedQuantity)
            );
        }
    }

    /**
     * Map Cart entity to CartDTO
     */
    private CartDTO mapToDTO(Cart cart) {
        List<CartItemDTO> itemDTOs = cart.getItems().stream()
            .map(this::mapItemToDTO)
            .collect(Collectors.toList());

        return CartDTO.builder()
            .cartId(cart.getCartId())
            .userId(cart.getUser().getUserId())
            .status(cart.getStatus())
            .totalAmount(cart.getTotalAmount())
            .totalItems(cart.getTotalItems())
            .items(itemDTOs)
            .createdAt(cart.getCreatedAt())
            .updatedAt(cart.getUpdatedAt())
            .build();
    }

    /**
     * Map CartItem entity to CartItemDTO
     */
    private CartItemDTO mapItemToDTO(CartItem item) {
        return CartItemDTO.builder()
            .cartItemId(item.getCartItemId())
            .productId(item.getProduct().getProductId())
            .productName(item.getProduct().getProductName())
            .productImageUrl(item.getProduct().getImageUrl())
            .quantity(item.getQuantity())
            .price(item.getPrice())
            .subtotal(item.getSubtotal())
            .addedAt(item.getAddedAt())
            .updatedAt(item.getUpdatedAt())
            .build();
    }
}