package com.ecommerce.service;

import com.ecommerce.dto.CartDTO;
import com.ecommerce.dto.CartItemDTO;
import com.ecommerce.entity.Cart;
import com.ecommerce.entity.CartItem;
import com.ecommerce.entity.Product;
import com.ecommerce.entity.User;
import com.ecommerce.exception.InsufficientStockException;
import com.ecommerce.exception.ResourceNotFoundException;
import com.ecommerce.repository.CartItemRepository;
import com.ecommerce.repository.CartRepository;
import com.ecommerce.repository.ProductRepository;
import com.ecommerce.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.stream.Collectors;

/**
 * Cart Service - Handles cart-related business logic
 * Implements lazy cart creation and auto-delete empty cart
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;

    /**
     * Get or create cart for user (lazy creation)
     */
    @Transactional(readOnly = true)
    public CartDTO getCart(Long userId) {
        log.info("Fetching cart for user ID: {}", userId);

        Cart cart = cartRepository.findByUserId(userId)
            .orElse(null);

        if (cart == null) {
            log.info("No cart found for user ID: {}", userId);
            return CartDTO.builder()
                .userId(userId)
                .build();
        }

        return convertToDTO(cart);
    }

    /**
     * Add item to cart (creates cart if not exists)
     */
    public CartDTO addItemToCart(Long userId, CartItemDTO cartItemDTO) {
        log.info("Adding item to cart for user ID: {}", userId);

        // Validate user
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));

        // Validate product
        Product product = productRepository.findById(cartItemDTO.getProductId())
            .orElseThrow(() -> new ResourceNotFoundException("Product not found with ID: " + cartItemDTO.getProductId()));

        // Check stock availability
        if (!product.hasSufficientStock(cartItemDTO.getQuantity())) {
            throw new InsufficientStockException("Insufficient stock for product: " + product.getName());
        }

        // Get or create cart (lazy creation)
        Cart cart = cartRepository.findByUserId(userId)
            .orElseGet(() -> {
                log.info("Creating new cart for user ID: {}", userId);
                Cart newCart = Cart.builder()
                    .user(user)
                    .build();
                return cartRepository.save(newCart);
            });

        // Check if product already in cart
        CartItem existingItem = cartItemRepository.findByCartIdAndProductId(cart.getId(), product.getId())
            .orElse(null);

        if (existingItem != null) {
            // Update quantity
            int newQuantity = existingItem.getQuantity() + cartItemDTO.getQuantity();
            if (!product.hasSufficientStock(newQuantity)) {
                throw new InsufficientStockException("Insufficient stock for product: " + product.getName());
            }
            existingItem.setQuantity(newQuantity);
            existingItem.calculateSubtotal();
            cartItemRepository.save(existingItem);
        } else {
            // Add new item
            CartItem cartItem = CartItem.builder()
                .cart(cart)
                .product(product)
                .quantity(cartItemDTO.getQuantity())
                .price(product.getPrice())
                .build();
            cart.addItem(cartItem);
            cartItemRepository.save(cartItem);
        }

        cart.recalculateTotals();
        Cart savedCart = cartRepository.save(cart);
        log.info("Item added to cart successfully");

        return convertToDTO(savedCart);
    }

    /**
     * Update cart item quantity
     */
    public CartDTO updateCartItem(Long userId, Long itemId, Integer quantity) {
        log.info("Updating cart item ID: {} for user ID: {}", itemId, userId);

        Cart cart = cartRepository.findByUserId(userId)
            .orElseThrow(() -> new ResourceNotFoundException("Cart not found for user ID: " + userId));

        CartItem cartItem = cartItemRepository.findById(itemId)
            .orElseThrow(() -> new ResourceNotFoundException("Cart item not found with ID: " + itemId));

        // Validate cart ownership
        if (!cartItem.getCart().getId().equals(cart.getId())) {
            throw new IllegalArgumentException("Cart item does not belong to user's cart");
        }

        // Check stock availability
        if (!cartItem.getProduct().hasSufficientStock(quantity)) {
            throw new InsufficientStockException("Insufficient stock for product: " + cartItem.getProduct().getName());
        }

        cartItem.setQuantity(quantity);
        cartItem.calculateSubtotal();
        cartItemRepository.save(cartItem);

        cart.recalculateTotals();
        Cart savedCart = cartRepository.save(cart);
        log.info("Cart item updated successfully");

        return convertToDTO(savedCart);
    }

    /**
     * Remove item from cart
     */
    public CartDTO removeItemFromCart(Long userId, Long itemId) {
        log.info("Removing cart item ID: {} for user ID: {}", itemId, userId);

        Cart cart = cartRepository.findByUserId(userId)
            .orElseThrow(() -> new ResourceNotFoundException("Cart not found for user ID: " + userId));

        CartItem cartItem = cartItemRepository.findById(itemId)
            .orElseThrow(() -> new ResourceNotFoundException("Cart item not found with ID: " + itemId));

        // Validate cart ownership
        if (!cartItem.getCart().getId().equals(cart.getId())) {
            throw new IllegalArgumentException("Cart item does not belong to user's cart");
        }

        cart.removeItem(cartItem);
        cartItemRepository.delete(cartItem);

        // Auto-delete empty cart
        if (cart.isEmpty()) {
            log.info("Cart is empty, deleting cart for user ID: {}", userId);
            cartRepository.delete(cart);
            return CartDTO.builder()
                .userId(userId)
                .build();
        }

        cart.recalculateTotals();
        Cart savedCart = cartRepository.save(cart);
        log.info("Cart item removed successfully");

        return convertToDTO(savedCart);
    }

    /**
     * Clear cart
     */
    public void clearCart(Long userId) {
        log.info("Clearing cart for user ID: {}", userId);

        Cart cart = cartRepository.findByUserId(userId)
            .orElse(null);

        if (cart != null) {
            cartRepository.delete(cart);
            log.info("Cart cleared successfully");
        }
    }

    /**
     * Convert Cart entity to DTO
     */
    private CartDTO convertToDTO(Cart cart) {
        return CartDTO.builder()
            .id(cart.getId())
            .userId(cart.getUser().getId())
            .items(cart.getItems().stream()
                .map(this::convertItemToDTO)
                .collect(Collectors.toList()))
            .totalAmount(cart.getTotalAmount())
            .totalItems(cart.getTotalItems())
            .createdAt(cart.getCreatedAt())
            .updatedAt(cart.getUpdatedAt())
            .build();
    }

    /**
     * Convert CartItem entity to DTO
     */
    private CartItemDTO convertItemToDTO(CartItem item) {
        return CartItemDTO.builder()
            .id(item.getId())
            .cartId(item.getCart().getId())
            .productId(item.getProduct().getId())
            .productName(item.getProduct().getName())
            .productSku(item.getProduct().getSku())
            .productImageUrl(item.getProduct().getImageUrl())
            .quantity(item.getQuantity())
            .price(item.getPrice())
            .subtotal(item.getSubtotal())
            .createdAt(item.getCreatedAt())
            .updatedAt(item.getUpdatedAt())
            .build();
    }
}