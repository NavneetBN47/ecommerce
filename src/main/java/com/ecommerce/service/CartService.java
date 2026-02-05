package com.ecommerce.service;

import com.ecommerce.dto.AddToCartRequest;
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

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for Cart operations
 * Implements lazy cart creation and auto-delete empty cart logic
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
    public CartDTO getOrCreateCart(Long userId) {
        log.debug("Getting or creating cart for user: {}", userId);
        
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));
        
        Cart cart = cartRepository.findByUser(user)
            .orElseGet(() -> {
                log.info("Creating new cart for user: {}", userId);
                Cart newCart = Cart.builder()
                    .user(user)
                    .build();
                return cartRepository.save(newCart);
            });
        
        return convertToDTO(cart);
    }
    
    /**
     * Add item to cart
     */
    public CartDTO addItemToCart(Long userId, AddToCartRequest request) {
        log.info("Adding item to cart for user: {}, product: {}, quantity: {}",
            userId, request.getProductId(), request.getQuantity());
        
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));
        
        Product product = productRepository.findById(request.getProductId())
            .orElseThrow(() -> new ResourceNotFoundException("Product not found with ID: " + request.getProductId()));
        
        // Check stock availability
        if (product.getStockQuantity() < request.getQuantity()) {
            throw new InsufficientStockException(
                String.format("Insufficient stock for product %s. Available: %d, Requested: %d",
                    product.getName(), product.getStockQuantity(), request.getQuantity()));
        }
        
        // Get or create cart (lazy creation)
        Cart cart = cartRepository.findByUser(user)
            .orElseGet(() -> {
                log.info("Creating new cart for user during add item: {}", userId);
                Cart newCart = Cart.builder()
                    .user(user)
                    .build();
                return cartRepository.save(newCart);
            });
        
        // Check if item already exists in cart
        CartItem cartItem = cartItemRepository.findByCartAndProduct(cart, product)
            .map(existingItem -> {
                log.debug("Updating existing cart item quantity");
                int newQuantity = existingItem.getQuantity() + request.getQuantity();
                
                // Check stock for new quantity
                if (product.getStockQuantity() < newQuantity) {
                    throw new InsufficientStockException(
                        String.format("Insufficient stock for product %s. Available: %d, Requested: %d",
                            product.getName(), product.getStockQuantity(), newQuantity));
                }
                
                existingItem.setQuantity(newQuantity);
                return existingItem;
            })
            .orElseGet(() -> {
                log.debug("Creating new cart item");
                return CartItem.builder()
                    .cart(cart)
                    .product(product)
                    .quantity(request.getQuantity())
                    .unitPrice(product.getPrice())
                    .build();
            });
        
        cartItemRepository.save(cartItem);
        cart.calculateTotal();
        cartRepository.save(cart);
        
        log.info("Item added to cart successfully");
        return convertToDTO(cart);
    }
    
    /**
     * Update cart item quantity
     */
    public CartDTO updateCartItem(Long userId, Long cartItemId, Integer quantity) {
        log.info("Updating cart item: {}, new quantity: {}", cartItemId, quantity);
        
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));
        
        Cart cart = cartRepository.findByUser(user)
            .orElseThrow(() -> new ResourceNotFoundException("Cart not found for user: " + userId));
        
        CartItem cartItem = cartItemRepository.findById(cartItemId)
            .orElseThrow(() -> new ResourceNotFoundException("Cart item not found with ID: " + cartItemId));
        
        if (!cartItem.getCart().getId().equals(cart.getId())) {
            throw new IllegalArgumentException("Cart item does not belong to user's cart");
        }
        
        Product product = cartItem.getProduct();
        
        // Check stock availability
        if (product.getStockQuantity() < quantity) {
            throw new InsufficientStockException(
                String.format("Insufficient stock for product %s. Available: %d, Requested: %d",
                    product.getName(), product.getStockQuantity(), quantity));
        }
        
        cartItem.setQuantity(quantity);
        cartItemRepository.save(cartItem);
        
        cart.calculateTotal();
        cartRepository.save(cart);
        
        log.info("Cart item updated successfully");
        return convertToDTO(cart);
    }
    
    /**
     * Remove item from cart
     */
    public CartDTO removeItemFromCart(Long userId, Long cartItemId) {
        log.info("Removing item from cart: {}", cartItemId);
        
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));
        
        Cart cart = cartRepository.findByUser(user)
            .orElseThrow(() -> new ResourceNotFoundException("Cart not found for user: " + userId));
        
        CartItem cartItem = cartItemRepository.findById(cartItemId)
            .orElseThrow(() -> new ResourceNotFoundException("Cart item not found with ID: " + cartItemId));
        
        if (!cartItem.getCart().getId().equals(cart.getId())) {
            throw new IllegalArgumentException("Cart item does not belong to user's cart");
        }
        
        cartItemRepository.delete(cartItem);
        
        cart.calculateTotal();
        
        // Auto-delete empty cart
        if (cart.isEmpty()) {
            log.info("Cart is empty, deleting cart for user: {}", userId);
            cartRepository.delete(cart);
            return CartDTO.builder()
                .userId(userId)
                .itemCount(0)
                .build();
        }
        
        cartRepository.save(cart);
        log.info("Item removed from cart successfully");
        return convertToDTO(cart);
    }
    
    /**
     * Clear cart
     */
    public void clearCart(Long userId) {
        log.info("Clearing cart for user: {}", userId);
        
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));
        
        Cart cart = cartRepository.findByUser(user)
            .orElseThrow(() -> new ResourceNotFoundException("Cart not found for user: " + userId));
        
        cartItemRepository.deleteByCart(cart);
        
        // Auto-delete empty cart
        log.info("Deleting empty cart for user: {}", userId);
        cartRepository.delete(cart);
    }
    
    /**
     * Cleanup cart on logout
     */
    public void cleanupCartOnLogout(Long userId) {
        log.info("Cleaning up cart on logout for user: {}", userId);
        
        cartRepository.findByUserId(userId).ifPresent(cart -> {
            if (cart.isEmpty()) {
                log.info("Deleting empty cart on logout for user: {}", userId);
                cartRepository.delete(cart);
            }
        });
    }
    
    /**
     * Convert Cart entity to DTO
     */
    private CartDTO convertToDTO(Cart cart) {
        List<CartItemDTO> itemDTOs = cart.getItems().stream()
            .map(this::convertItemToDTO)
            .collect(Collectors.toList());
        
        return CartDTO.builder()
            .id(cart.getId())
            .userId(cart.getUser().getId())
            .items(itemDTOs)
            .totalAmount(cart.getTotalAmount())
            .itemCount(itemDTOs.size())
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
            .unitPrice(item.getUnitPrice())
            .subtotal(item.getSubtotal())
            .createdAt(item.getCreatedAt())
            .updatedAt(item.getUpdatedAt())
            .build();
    }
}