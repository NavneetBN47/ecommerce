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
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.stream.Collectors;

/**
 * Service layer for Cart operations
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
    private final ModelMapper modelMapper;
    
    /**
     * Get or create cart for user (lazy creation)
     */
    @Transactional
    public CartDTO getOrCreateCart(Long userId) {
        log.info("Getting or creating cart for user: {}", userId);
        
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
        
        Cart cart = cartRepository.findByUserIdWithItems(userId)
            .orElseGet(() -> {
                log.info("Creating new cart for user: {}", userId);
                Cart newCart = new Cart();
                newCart.setUser(user);
                return cartRepository.save(newCart);
            });
        
        return mapToDTO(cart);
    }
    
    /**
     * Add item to cart
     */
    @Transactional
    public CartDTO addItemToCart(Long userId, CartItemDTO cartItemDTO) {
        log.info("Adding item to cart for user: {}, product: {}", userId, cartItemDTO.getProductId());
        
        // Get or create cart
        Cart cart = cartRepository.findByUserIdWithItems(userId)
            .orElseGet(() -> {
                User user = userRepository.findById(userId)
                    .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
                Cart newCart = new Cart();
                newCart.setUser(user);
                return cartRepository.save(newCart);
            });
        
        // Get product
        Product product = productRepository.findById(cartItemDTO.getProductId())
            .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + cartItemDTO.getProductId()));
        
        // Check stock availability
        if (product.getStock() < cartItemDTO.getQuantity()) {
            throw new InsufficientStockException("Insufficient stock for product: " + product.getName() + 
                ". Available: " + product.getStock() + ", Requested: " + cartItemDTO.getQuantity());
        }
        
        // Check if item already exists in cart
        CartItem cartItem = cartItemRepository.findByCartIdAndProductId(cart.getId(), product.getId())
            .orElse(null);
        
        if (cartItem != null) {
            // Update quantity
            int newQuantity = cartItem.getQuantity() + cartItemDTO.getQuantity();
            
            // Check stock for new quantity
            if (product.getStock() < newQuantity) {
                throw new InsufficientStockException("Insufficient stock for product: " + product.getName() + 
                    ". Available: " + product.getStock() + ", Requested: " + newQuantity);
            }
            
            cartItem.setQuantity(newQuantity);
            cartItemRepository.save(cartItem);
        } else {
            // Create new cart item
            cartItem = new CartItem();
            cartItem.setCart(cart);
            cartItem.setProduct(product);
            cartItem.setQuantity(cartItemDTO.getQuantity());
            cartItem.setPrice(product.getPrice());
            cart.addItem(cartItem);
            cartItemRepository.save(cartItem);
        }
        
        // Recalculate totals
        cart.recalculateTotals();
        cart = cartRepository.save(cart);
        
        log.info("Item added to cart successfully");
        return mapToDTO(cart);
    }
    
    /**
     * Update cart item quantity
     */
    @Transactional
    public CartDTO updateCartItem(Long userId, Long cartItemId, Integer quantity) {
        log.info("Updating cart item: {}, quantity: {}", cartItemId, quantity);
        
        Cart cart = cartRepository.findByUserIdWithItems(userId)
            .orElseThrow(() -> new ResourceNotFoundException("Cart not found for user: " + userId));
        
        CartItem cartItem = cartItemRepository.findById(cartItemId)
            .orElseThrow(() -> new ResourceNotFoundException("Cart item not found with id: " + cartItemId));
        
        // Verify cart item belongs to user's cart
        if (!cartItem.getCart().getId().equals(cart.getId())) {
            throw new ResourceNotFoundException("Cart item does not belong to user's cart");
        }
        
        Product product = cartItem.getProduct();
        
        // Check stock availability
        if (product.getStock() < quantity) {
            throw new InsufficientStockException("Insufficient stock for product: " + product.getName() + 
                ". Available: " + product.getStock() + ", Requested: " + quantity);
        }
        
        cartItem.setQuantity(quantity);
        cartItemRepository.save(cartItem);
        
        // Recalculate totals
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
        log.info("Removing item from cart: {}", cartItemId);
        
        Cart cart = cartRepository.findByUserIdWithItems(userId)
            .orElseThrow(() -> new ResourceNotFoundException("Cart not found for user: " + userId));
        
        CartItem cartItem = cartItemRepository.findById(cartItemId)
            .orElseThrow(() -> new ResourceNotFoundException("Cart item not found with id: " + cartItemId));
        
        // Verify cart item belongs to user's cart
        if (!cartItem.getCart().getId().equals(cart.getId())) {
            throw new ResourceNotFoundException("Cart item does not belong to user's cart");
        }
        
        cart.removeItem(cartItem);
        cartItemRepository.delete(cartItem);
        
        // Check if cart is empty and delete if so
        if (cart.isEmpty()) {
            log.info("Cart is empty, deleting cart for user: {}", userId);
            cartRepository.delete(cart);
            return new CartDTO();
        }
        
        // Recalculate totals
        cart.recalculateTotals();
        cart = cartRepository.save(cart);
        
        log.info("Item removed from cart successfully");
        return mapToDTO(cart);
    }
    
    /**
     * Clear cart
     */
    @Transactional
    public void clearCart(Long userId) {
        log.info("Clearing cart for user: {}", userId);
        
        Cart cart = cartRepository.findByUserId(userId)
            .orElseThrow(() -> new ResourceNotFoundException("Cart not found for user: " + userId));
        
        cartRepository.delete(cart);
        log.info("Cart cleared successfully");
    }
    
    /**
     * Cleanup empty cart (called on logout)
     */
    @Transactional
    public void cleanupEmptyCart(Long userId) {
        log.info("Cleaning up empty cart for user: {}", userId);
        
        cartRepository.findByUserId(userId).ifPresent(cart -> {
            if (cart.isEmpty()) {
                log.info("Deleting empty cart for user: {}", userId);
                cartRepository.delete(cart);
            }
        });
    }
    
    /**
     * Map Cart entity to DTO
     */
    private CartDTO mapToDTO(Cart cart) {
        CartDTO cartDTO = new CartDTO();
        cartDTO.setId(cart.getId());
        cartDTO.setUserId(cart.getUser().getId());
        cartDTO.setTotalAmount(cart.getTotalAmount());
        cartDTO.setTotalItems(cart.getTotalItems());
        cartDTO.setCreatedAt(cart.getCreatedAt());
        cartDTO.setUpdatedAt(cart.getUpdatedAt());
        
        cartDTO.setItems(cart.getItems().stream()
            .map(this::mapItemToDTO)
            .collect(Collectors.toList()));
        
        return cartDTO;
    }
    
    /**
     * Map CartItem entity to DTO
     */
    private CartItemDTO mapItemToDTO(CartItem cartItem) {
        CartItemDTO dto = new CartItemDTO();
        dto.setId(cartItem.getId());
        dto.setCartId(cartItem.getCart().getId());
        dto.setProductId(cartItem.getProduct().getId());
        dto.setProductName(cartItem.getProduct().getName());
        dto.setProductSku(cartItem.getProduct().getSku());
        dto.setQuantity(cartItem.getQuantity());
        dto.setPrice(cartItem.getPrice());
        dto.setSubtotal(cartItem.getSubtotal());
        dto.setCreatedAt(cartItem.getCreatedAt());
        dto.setUpdatedAt(cartItem.getUpdatedAt());
        return dto;
    }
}