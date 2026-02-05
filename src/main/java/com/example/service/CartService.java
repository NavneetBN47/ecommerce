package com.example.service;

import com.example.dto.AddToCartRequest;
import com.example.dto.CartDTO;
import com.example.dto.CartItemDTO;
import com.example.entity.Cart;
import com.example.entity.CartItem;
import com.example.entity.Product;
import com.example.entity.User;
import com.example.exception.InsufficientStockException;
import com.example.exception.ResourceNotFoundException;
import com.example.repository.CartItemRepository;
import com.example.repository.CartRepository;
import com.example.repository.ProductRepository;
import com.example.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.stream.Collectors;

/**
 * Service class for Cart entity operations.
 * Implements lazy cart creation and auto-delete when empty.
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
     * Get or create cart for user (lazy creation).
     */
    @Transactional(readOnly = true)
    public CartDTO getCartByUserId(Long userId) {
        log.debug("Fetching cart for user: {}", userId);
        
        Cart cart = cartRepository.findByUserIdWithItems(userId)
            .orElse(null);
        
        if (cart == null || cart.isEmpty()) {
            log.debug("Cart is empty or doesn't exist for user: {}", userId);
            return CartDTO.builder()
                .userId(userId)
                .items(java.util.Collections.emptyList())
                .totalAmount(java.math.BigDecimal.ZERO)
                .totalItems(0)
                .build();
        }
        
        return convertToDTO(cart);
    }
    
    /**
     * Add item to cart (creates cart if doesn't exist).
     */
    public CartDTO addItemToCart(Long userId, AddToCartRequest request) {
        log.info("Adding item to cart for user: {}", userId);
        
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));
        
        Product product = productRepository.findById(request.getProductId())
            .orElseThrow(() -> new ResourceNotFoundException("Product not found with ID: " + request.getProductId()));
        
        // Check stock availability
        if (!product.hasSufficientStock(request.getQuantity())) {
            throw new InsufficientStockException(
                "Insufficient stock for product: " + product.getName() + 
                ". Available: " + product.getStockQuantity() + ", Requested: " + request.getQuantity()
            );
        }
        
        // Get or create cart (lazy creation)
        Cart cart = cartRepository.findByUser(user)
            .orElseGet(() -> {
                log.info("Creating new cart for user: {}", userId);
                Cart newCart = Cart.builder()
                    .user(user)
                    .build();
                return cartRepository.save(newCart);
            });
        
        // Check if product already exists in cart
        CartItem existingItem = cartItemRepository.findByCartAndProduct(cart, product)
            .orElse(null);
        
        if (existingItem != null) {
            // Update quantity
            int newQuantity = existingItem.getQuantity() + request.getQuantity();
            if (!product.hasSufficientStock(newQuantity)) {
                throw new InsufficientStockException(
                    "Insufficient stock for product: " + product.getName() + 
                    ". Available: " + product.getStockQuantity() + ", Requested: " + newQuantity
                );
            }
            existingItem.setQuantity(newQuantity);
            existingItem.updateSubtotal();
            cartItemRepository.save(existingItem);
        } else {
            // Add new item
            CartItem newItem = CartItem.builder()
                .cart(cart)
                .product(product)
                .quantity(request.getQuantity())
                .unitPrice(product.getEffectivePrice())
                .build();
            newItem.updateSubtotal();
            cartItemRepository.save(newItem);
            cart.getItems().add(newItem);
        }
        
        // Recalculate totals
        cart.recalculateTotals();
        cart = cartRepository.save(cart);
        
        log.info("Item added to cart successfully for user: {}", userId);
        return convertToDTO(cart);
    }
    
    /**
     * Update cart item quantity.
     */
    public CartDTO updateCartItem(Long userId, Long itemId, Integer quantity) {
        log.info("Updating cart item: {} for user: {}", itemId, userId);
        
        Cart cart = cartRepository.findByUserIdWithItems(userId)
            .orElseThrow(() -> new ResourceNotFoundException("Cart not found for user: " + userId));
        
        CartItem item = cartItemRepository.findById(itemId)
            .orElseThrow(() -> new ResourceNotFoundException("Cart item not found with ID: " + itemId));
        
        // Verify item belongs to user's cart
        if (!item.getCart().getId().equals(cart.getId())) {
            throw new IllegalArgumentException("Cart item does not belong to user's cart");
        }
        
        // Check stock availability
        if (!item.getProduct().hasSufficientStock(quantity)) {
            throw new InsufficientStockException(
                "Insufficient stock for product: " + item.getProduct().getName() + 
                ". Available: " + item.getProduct().getStockQuantity() + ", Requested: " + quantity
            );
        }
        
        item.setQuantity(quantity);
        item.updateSubtotal();
        cartItemRepository.save(item);
        
        cart.recalculateTotals();
        cart = cartRepository.save(cart);
        
        log.info("Cart item updated successfully: {}", itemId);
        return convertToDTO(cart);
    }
    
    /**
     * Remove item from cart.
     */
    public CartDTO removeItemFromCart(Long userId, Long itemId) {
        log.info("Removing item from cart: {} for user: {}", itemId, userId);
        
        Cart cart = cartRepository.findByUserIdWithItems(userId)
            .orElseThrow(() -> new ResourceNotFoundException("Cart not found for user: " + userId));
        
        CartItem item = cartItemRepository.findById(itemId)
            .orElseThrow(() -> new ResourceNotFoundException("Cart item not found with ID: " + itemId));
        
        // Verify item belongs to user's cart
        if (!item.getCart().getId().equals(cart.getId())) {
            throw new IllegalArgumentException("Cart item does not belong to user's cart");
        }
        
        cart.getItems().remove(item);
        cartItemRepository.delete(item);
        
        cart.recalculateTotals();
        
        // Auto-delete empty cart
        if (cart.isEmpty()) {
            log.info("Cart is empty, deleting cart for user: {}", userId);
            cartRepository.delete(cart);
            return CartDTO.builder()
                .userId(userId)
                .items(java.util.Collections.emptyList())
                .totalAmount(java.math.BigDecimal.ZERO)
                .totalItems(0)
                .build();
        }
        
        cart = cartRepository.save(cart);
        log.info("Item removed from cart successfully: {}", itemId);
        
        return convertToDTO(cart);
    }
    
    /**
     * Clear cart (delete all items and cart).
     */
    public void clearCart(Long userId) {
        log.info("Clearing cart for user: {}", userId);
        
        Cart cart = cartRepository.findByUserIdWithItems(userId)
            .orElse(null);
        
        if (cart != null) {
            cartRepository.delete(cart);
            log.info("Cart cleared successfully for user: {}", userId);
        }
    }
    
    /**
     * Cleanup cart on logout.
     */
    public void cleanupCartOnLogout(Long userId) {
        log.info("Cleaning up cart on logout for user: {}", userId);
        
        Cart cart = cartRepository.findByUserIdWithItems(userId)
            .orElse(null);
        
        if (cart != null && cart.isEmpty()) {
            log.info("Deleting empty cart on logout for user: {}", userId);
            cartRepository.delete(cart);
        }
    }
    
    /**
     * Convert Cart entity to DTO.
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
     * Convert CartItem entity to DTO.
     */
    private CartItemDTO convertItemToDTO(CartItem item) {
        return CartItemDTO.builder()
            .id(item.getId())
            .productId(item.getProduct().getId())
            .productName(item.getProduct().getName())
            .productSku(item.getProduct().getSku())
            .productImageUrl(item.getProduct().getImageUrl())
            .quantity(item.getQuantity())
            .unitPrice(item.getUnitPrice())
            .subtotal(item.getSubtotal())
            .createdAt(item.getCreatedAt())
            .updatedAt(item.getUpdatedAt())
            .build();
    }
}