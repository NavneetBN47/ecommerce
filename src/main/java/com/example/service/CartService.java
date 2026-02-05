package com.example.service;

import com.example.dto.*;
import com.example.entity.*;
import com.example.exception.*;
import com.example.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service class for Cart operations
 * Implements lazy cart creation and auto-delete empty cart logic
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
     * Get or create cart for user (lazy creation)
     */
    @Transactional(readOnly = true)
    public CartDTO getCart(Long userId) {
        log.info("Fetching cart for user: {}", userId);
        
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        Cart cart = cartRepository.findByUserIdWithItems(userId)
            .orElse(null);

        if (cart == null) {
            log.info("No cart found for user: {}, returning empty cart response", userId);
            return CartDTO.builder()
                .userId(userId)
                .totalAmount(BigDecimal.ZERO)
                .totalItems(0)
                .items(List.of())
                .build();
        }

        return convertToDTO(cart);
    }

    /**
     * Add item to cart (creates cart if not exists)
     */
    @Transactional
    public CartDTO addItemToCart(Long userId, AddToCartRequest request) {
        log.info("Adding item to cart for user: {}, product: {}, quantity: {}", 
            userId, request.getProductId(), request.getQuantity());

        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        Product product = productRepository.findByIdAndIsDeletedFalse(request.getProductId())
            .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + request.getProductId()));

        // Check stock availability
        if (product.getStockQuantity() < request.getQuantity()) {
            throw new InsufficientStockException(
                String.format("Insufficient stock for product %s. Available: %d, Requested: %d",
                    product.getName(), product.getStockQuantity(), request.getQuantity()));
        }

        // Get or create cart (lazy creation)
        Cart cart = cartRepository.findByUserIdWithItems(userId)
            .orElseGet(() -> {
                log.info("Creating new cart for user: {}", userId);
                Cart newCart = Cart.builder()
                    .user(user)
                    .totalAmount(BigDecimal.ZERO)
                    .totalItems(0)
                    .build();
                return cartRepository.save(newCart);
            });

        // Check if item already exists in cart
        CartItem existingItem = cartItemRepository.findByCartIdAndProductId(cart.getId(), product.getId())
            .orElse(null);

        if (existingItem != null) {
            // Update existing item
            int newQuantity = existingItem.getQuantity() + request.getQuantity();
            
            if (product.getStockQuantity() < newQuantity) {
                throw new InsufficientStockException(
                    String.format("Insufficient stock for product %s. Available: %d, Requested total: %d",
                        product.getName(), product.getStockQuantity(), newQuantity));
            }
            
            existingItem.setQuantity(newQuantity);
            existingItem.calculateSubtotal();
            cartItemRepository.save(existingItem);
        } else {
            // Create new cart item
            CartItem newItem = CartItem.builder()
                .cart(cart)
                .product(product)
                .quantity(request.getQuantity())
                .unitPrice(product.getPrice())
                .build();
            newItem.calculateSubtotal();
            cart.addItem(newItem);
            cartItemRepository.save(newItem);
        }

        // Recalculate cart totals
        cart.recalculateTotals();
        cart = cartRepository.save(cart);

        log.info("Item added to cart successfully for user: {}", userId);
        return convertToDTO(cart);
    }

    /**
     * Update cart item quantity
     */
    @Transactional
    public CartDTO updateCartItem(Long userId, Long itemId, UpdateCartItemRequest request) {
        log.info("Updating cart item: {} for user: {}, new quantity: {}", itemId, userId, request.getQuantity());

        Cart cart = cartRepository.findByUserIdWithItems(userId)
            .orElseThrow(() -> new ResourceNotFoundException("Cart not found for user: " + userId));

        CartItem cartItem = cartItemRepository.findById(itemId)
            .orElseThrow(() -> new ResourceNotFoundException("Cart item not found with id: " + itemId));

        if (!cartItem.getCart().getId().equals(cart.getId())) {
            throw new UnauthorizedException("Cart item does not belong to user");
        }

        Product product = cartItem.getProduct();
        if (product.getStockQuantity() < request.getQuantity()) {
            throw new InsufficientStockException(
                String.format("Insufficient stock for product %s. Available: %d, Requested: %d",
                    product.getName(), product.getStockQuantity(), request.getQuantity()));
        }

        cartItem.setQuantity(request.getQuantity());
        cartItem.calculateSubtotal();
        cartItemRepository.save(cartItem);

        cart.recalculateTotals();
        cart = cartRepository.save(cart);

        log.info("Cart item updated successfully");
        return convertToDTO(cart);
    }

    /**
     * Remove item from cart (auto-delete cart if empty)
     */
    @Transactional
    public CartDTO removeItemFromCart(Long userId, Long itemId) {
        log.info("Removing cart item: {} for user: {}", itemId, userId);

        Cart cart = cartRepository.findByUserIdWithItems(userId)
            .orElseThrow(() -> new ResourceNotFoundException("Cart not found for user: " + userId));

        CartItem cartItem = cartItemRepository.findById(itemId)
            .orElseThrow(() -> new ResourceNotFoundException("Cart item not found with id: " + itemId));

        if (!cartItem.getCart().getId().equals(cart.getId())) {
            throw new UnauthorizedException("Cart item does not belong to user");
        }

        cart.removeItem(cartItem);
        cartItemRepository.delete(cartItem);

        // Auto-delete cart if empty
        if (cart.isEmpty()) {
            log.info("Cart is empty, deleting cart for user: {}", userId);
            cartRepository.delete(cart);
            return CartDTO.builder()
                .userId(userId)
                .totalAmount(BigDecimal.ZERO)
                .totalItems(0)
                .items(List.of())
                .build();
        }

        cart.recalculateTotals();
        cart = cartRepository.save(cart);

        log.info("Cart item removed successfully");
        return convertToDTO(cart);
    }

    /**
     * Clear entire cart
     */
    @Transactional
    public void clearCart(Long userId) {
        log.info("Clearing cart for user: {}", userId);
        
        Cart cart = cartRepository.findByUserId(userId)
            .orElse(null);

        if (cart != null) {
            cartRepository.delete(cart);
            log.info("Cart cleared successfully for user: {}", userId);
        }
    }

    /**
     * Cleanup cart on logout
     */
    @Transactional
    public void cleanupCartOnLogout(Long userId) {
        log.info("Cleaning up cart on logout for user: {}", userId);
        clearCart(userId);
    }

    /**
     * Convert Cart entity to DTO
     */
    private CartDTO convertToDTO(Cart cart) {
        List<CartItemDTO> itemDTOs = cart.getItems().stream()
            .map(this::convertItemToDTO)
            .collect(Collectors.toList());

        return CartDTO.builder()
            .cartId(cart.getId())
            .userId(cart.getUser().getId())
            .totalAmount(cart.getTotalAmount())
            .totalItems(cart.getTotalItems())
            .items(itemDTOs)
            .createdAt(cart.getCreatedAt())
            .updatedAt(cart.getUpdatedAt())
            .build();
    }

    /**
     * Convert CartItem entity to DTO
     */
    private CartItemDTO convertItemToDTO(CartItem item) {
        return CartItemDTO.builder()
            .itemId(item.getId())
            .productId(item.getProduct().getId())
            .productName(item.getProduct().getName())
            .productSku(item.getProduct().getSku())
            .quantity(item.getQuantity())
            .unitPrice(item.getUnitPrice())
            .subtotal(item.getSubtotal())
            .stockAvailable(item.getProduct().getStockQuantity())
            .build();
    }
}