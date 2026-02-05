package com.ecommerce.service;

import com.ecommerce.dto.CartDTO;
import com.ecommerce.dto.CartItemDTO;
import com.ecommerce.entity.Cart;
import com.ecommerce.entity.CartItem;
import com.ecommerce.entity.Product;
import com.ecommerce.entity.User;
import com.ecommerce.exception.InsufficientStockException;
import com.ecommerce.exception.ResourceNotFoundException;
import com.ecommerce.mapper.CartMapper;
import com.ecommerce.repository.CartItemRepository;
import com.ecommerce.repository.CartRepository;
import com.ecommerce.repository.ProductRepository;
import com.ecommerce.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service class for Cart operations
 * Implements lazy cart creation and auto-delete when empty
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final CartMapper cartMapper;

    /**
     * Get or create cart for user (lazy creation)
     */
    public CartDTO getOrCreateCart(Long userId) {
        log.debug("Getting or creating cart for user ID: {}", userId);

        // Verify user exists
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));

        // Try to find existing cart
        Cart cart = cartRepository.findByUserId(userId)
            .orElseGet(() -> {
                // Create new cart if not exists (lazy creation)
                log.info("Creating new cart for user ID: {}", userId);
                Cart newCart = Cart.builder()
                    .user(user)
                    .build();
                return cartRepository.save(newCart);
            });

        return cartMapper.toDTO(cart);
    }

    /**
     * Add item to cart
     */
    public CartDTO addItemToCart(Long userId, CartItemDTO cartItemDTO) {
        log.info("Adding item to cart for user ID: {}", userId);

        // Get or create cart
        Cart cart = cartRepository.findByUserId(userId)
            .orElseGet(() -> {
                User user = userRepository.findById(userId)
                    .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));
                Cart newCart = Cart.builder().user(user).build();
                return cartRepository.save(newCart);
            });

        // Get product
        Product product = productRepository.findById(cartItemDTO.getProductId())
            .orElseThrow(() -> new ResourceNotFoundException("Product not found with ID: " + cartItemDTO.getProductId()));

        // Check stock availability
        if (!product.hasStock(cartItemDTO.getQuantity())) {
            throw new InsufficientStockException("Insufficient stock for product: " + product.getName());
        }

        // Check if item already exists in cart
        CartItem cartItem = cartItemRepository.findByCartIdAndProductId(cart.getId(), product.getId())
            .orElse(null);

        if (cartItem != null) {
            // Update quantity if item exists
            int newQuantity = cartItem.getQuantity() + cartItemDTO.getQuantity();
            if (!product.hasStock(newQuantity)) {
                throw new InsufficientStockException("Insufficient stock for product: " + product.getName());
            }
            cartItem.updateQuantity(newQuantity);
            log.info("Updated cart item quantity for product: {}", product.getName());
        } else {
            // Create new cart item
            cartItem = CartItem.builder()
                .cart(cart)
                .product(product)
                .quantity(cartItemDTO.getQuantity())
                .price(product.getPrice())
                .build();
            cart.addItem(cartItem);
            log.info("Added new item to cart: {}", product.getName());
        }

        cartItemRepository.save(cartItem);
        cart.recalculateTotal();
        Cart savedCart = cartRepository.save(cart);

        return cartMapper.toDTO(savedCart);
    }

    /**
     * Update cart item quantity
     */
    public CartDTO updateCartItemQuantity(Long userId, Long cartItemId, Integer quantity) {
        log.info("Updating cart item quantity for user ID: {}", userId);

        Cart cart = cartRepository.findByUserId(userId)
            .orElseThrow(() -> new ResourceNotFoundException("Cart not found for user ID: " + userId));

        CartItem cartItem = cartItemRepository.findById(cartItemId)
            .orElseThrow(() -> new ResourceNotFoundException("Cart item not found with ID: " + cartItemId));

        // Verify cart item belongs to user's cart
        if (!cartItem.getCart().getId().equals(cart.getId())) {
            throw new IllegalArgumentException("Cart item does not belong to user's cart");
        }

        // Check stock availability
        if (!cartItem.getProduct().hasStock(quantity)) {
            throw new InsufficientStockException("Insufficient stock for product: " + cartItem.getProduct().getName());
        }

        // Update quantity
        cartItem.updateQuantity(quantity);
        cartItemRepository.save(cartItem);

        cart.recalculateTotal();
        Cart savedCart = cartRepository.save(cart);

        return cartMapper.toDTO(savedCart);
    }

    /**
     * Remove item from cart
     */
    public CartDTO removeItemFromCart(Long userId, Long cartItemId) {
        log.info("Removing item from cart for user ID: {}", userId);

        Cart cart = cartRepository.findByUserId(userId)
            .orElseThrow(() -> new ResourceNotFoundException("Cart not found for user ID: " + userId));

        CartItem cartItem = cartItemRepository.findById(cartItemId)
            .orElseThrow(() -> new ResourceNotFoundException("Cart item not found with ID: " + cartItemId));

        // Verify cart item belongs to user's cart
        if (!cartItem.getCart().getId().equals(cart.getId())) {
            throw new IllegalArgumentException("Cart item does not belong to user's cart");
        }

        cart.removeItem(cartItem);
        cartItemRepository.delete(cartItem);

        // Auto-delete cart if empty
        if (cart.isEmpty()) {
            log.info("Cart is empty, deleting cart for user ID: {}", userId);
            cartRepository.delete(cart);
            return CartDTO.builder().userId(userId).build();
        }

        cart.recalculateTotal();
        Cart savedCart = cartRepository.save(cart);

        return cartMapper.toDTO(savedCart);
    }

    /**
     * Clear cart (remove all items)
     */
    public void clearCart(Long userId) {
        log.info("Clearing cart for user ID: {}", userId);

        Cart cart = cartRepository.findByUserId(userId)
            .orElseThrow(() -> new ResourceNotFoundException("Cart not found for user ID: " + userId));

        cartItemRepository.deleteByCartId(cart.getId());
        
        // Auto-delete cart when cleared
        cartRepository.delete(cart);
        log.info("Cart cleared and deleted for user ID: {}", userId);
    }

    /**
     * Get cart by user ID
     */
    @Transactional(readOnly = true)
    public CartDTO getCartByUserId(Long userId) {
        log.debug("Fetching cart for user ID: {}", userId);

        Cart cart = cartRepository.findByUserId(userId)
            .orElseThrow(() -> new ResourceNotFoundException("Cart not found for user ID: " + userId));

        return cartMapper.toDTO(cart);
    }

    /**
     * Delete cart on logout (cleanup)
     */
    public void deleteCartOnLogout(Long userId) {
        log.info("Deleting cart on logout for user ID: {}", userId);

        cartRepository.findByUserId(userId).ifPresent(cart -> {
            cartRepository.delete(cart);
            log.info("Cart deleted on logout for user ID: {}", userId);
        });
    }
}