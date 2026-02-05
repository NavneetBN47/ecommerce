package com.ecommerce.service;

import com.ecommerce.dto.AddToCartRequest;
import com.ecommerce.dto.CartItemResponse;
import com.ecommerce.dto.CartResponse;
import com.ecommerce.dto.UpdateCartItemRequest;
import com.ecommerce.entity.Cart;
import com.ecommerce.entity.CartItem;
import com.ecommerce.entity.Product;
import com.ecommerce.exception.CartNotFoundException;
import com.ecommerce.exception.InvalidQuantityException;
import com.ecommerce.exception.ItemNotFoundException;
import com.ecommerce.exception.ProductNotFoundException;
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
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CartService {
    
    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;
    
    /**
     * Get user's active cart
     */
    @Transactional(readOnly = true)
    public CartResponse getCart(UUID userId) {
        log.info("Fetching cart for user ID: {}", userId);
        
        Cart cart = cartRepository.findByUserIdAndStatus(userId, "active")
                .orElseThrow(() -> new CartNotFoundException("Cart not found"));
        
        return mapToCartResponse(cart);
    }
    
    /**
     * Add product to cart (lazy cart creation)
     */
    @Transactional
    public CartResponse addProductToCart(UUID userId, AddToCartRequest request) {
        log.info("Adding product {} to cart for user {}", request.getProductId(), userId);
        
        // Validate quantity
        if (request.getQuantity() <= 0) {
            throw new InvalidQuantityException("Quantity must be greater than 0");
        }
        
        // Validate product exists
        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new ProductNotFoundException("Product not found"));
        
        // Get or create cart (lazy creation)
        Cart cart = cartRepository.findByUserIdAndStatus(userId, "active")
                .orElseGet(() -> createCart(userId));
        
        // Check if product already in cart
        CartItem existingItem = cartItemRepository
                .findByCartIdAndProductId(cart.getId(), product.getId())
                .orElse(null);
        
        if (existingItem != null) {
            // Update existing item quantity
            existingItem.setQuantity(existingItem.getQuantity() + request.getQuantity());
            existingItem.setUnitPrice(product.getPrice());
            cartItemRepository.save(existingItem);
            log.info("Updated existing cart item: {}", existingItem.getId());
        } else {
            // Create new cart item
            CartItem newItem = CartItem.builder()
                    .cart(cart)
                    .product(product)
                    .quantity(request.getQuantity())
                    .unitPrice(product.getPrice())
                    .build();
            cart.addItem(newItem);
            cartItemRepository.save(newItem);
            log.info("Added new cart item: {}", newItem.getId());
        }
        
        // Update cart totals
        updateCartTotals(cart);
        
        return mapToCartResponse(cart);
    }
    
    /**
     * Update cart item quantity
     */
    @Transactional
    public CartResponse updateCartItem(UUID userId, UUID itemId, UpdateCartItemRequest request) {
        log.info("Updating cart item {} for user {}", itemId, userId);
        
        // Validate quantity
        if (request.getQuantity() <= 0) {
            throw new InvalidQuantityException("Quantity must be greater than 0");
        }
        
        CartItem item = cartItemRepository.findById(itemId)
                .orElseThrow(() -> new ItemNotFoundException("Cart item not found"));
        
        // Verify item belongs to user's cart
        Cart cart = item.getCart();
        if (!cart.getUserId().equals(userId)) {
            throw new ItemNotFoundException("Cart item not found");
        }
        
        item.setQuantity(request.getQuantity());
        cartItemRepository.save(item);
        
        // Update cart totals
        updateCartTotals(cart);
        
        log.info("Cart item updated successfully: {}", itemId);
        
        return mapToCartResponse(cart);
    }
    
    /**
     * Remove item from cart (auto-delete empty cart)
     */
    @Transactional
    public CartResponse removeCartItem(UUID userId, UUID itemId) {
        log.info("Removing cart item {} for user {}", itemId, userId);
        
        CartItem item = cartItemRepository.findById(itemId)
                .orElseThrow(() -> new ItemNotFoundException("Cart item not found"));
        
        // Verify item belongs to user's cart
        Cart cart = item.getCart();
        if (!cart.getUserId().equals(userId)) {
            throw new ItemNotFoundException("Cart item not found");
        }
        
        cart.removeItem(item);
        cartItemRepository.delete(item);
        
        // Check if cart is now empty
        if (cart.getItems().isEmpty()) {
            log.info("Cart is empty, deleting cart: {}", cart.getId());
            cartRepository.delete(cart);
            throw new CartNotFoundException("Cart deleted (was empty)");
        }
        
        // Update cart totals
        updateCartTotals(cart);
        
        log.info("Cart item removed successfully: {}", itemId);
        
        return mapToCartResponse(cart);
    }
    
    /**
     * Delete user's cart (logout cleanup)
     */
    @Transactional
    public void deleteUserCart(UUID userId) {
        log.info("Deleting cart for user: {}", userId);
        
        cartRepository.findByUserId(userId).ifPresent(cart -> {
            cartItemRepository.deleteByCartId(cart.getId());
            cartRepository.delete(cart);
            log.info("Cart and items deleted for user: {}", userId);
        });
    }
    
    /**
     * Create new cart for user
     */
    private Cart createCart(UUID userId) {
        log.info("Creating new cart for user: {}", userId);
        
        Cart cart = Cart.builder()
                .userId(userId)
                .status("active")
                .totalAmount(BigDecimal.ZERO)
                .itemCount(0)
                .expiresAt(LocalDateTime.now().plusDays(30))
                .build();
        
        return cartRepository.save(cart);
    }
    
    /**
     * Update cart totals based on items
     */
    private void updateCartTotals(Cart cart) {
        BigDecimal total = cart.getItems().stream()
                .map(CartItem::getTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        int itemCount = cart.getItems().stream()
                .mapToInt(CartItem::getQuantity)
                .sum();
        
        cart.setTotalAmount(total);
        cart.setItemCount(itemCount);
        cartRepository.save(cart);
    }
    
    /**
     * Map Cart entity to CartResponse DTO
     */
    private CartResponse mapToCartResponse(Cart cart) {
        List<CartItemResponse> items = cart.getItems().stream()
                .map(this::mapToCartItemResponse)
                .collect(Collectors.toList());
        
        return CartResponse.builder()
                .cartId(cart.getId())
                .items(items)
                .grandTotal(cart.getTotalAmount())
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
                .description(item.getProduct().getDescription())
                .unitPrice(item.getUnitPrice())
                .quantity(item.getQuantity())
                .total(item.getTotalPrice())
                .build();
    }
}