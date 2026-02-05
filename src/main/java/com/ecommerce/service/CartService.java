package com.ecommerce.service;

import com.ecommerce.dto.AddToCartRequest;
import com.ecommerce.dto.CartDTO;
import com.ecommerce.dto.CartItemDTO;
import com.ecommerce.entity.Cart;
import com.ecommerce.entity.CartItem;
import com.ecommerce.entity.Product;
import com.ecommerce.entity.User;
import com.ecommerce.exception.ResourceNotFoundException;
import com.ecommerce.exception.ValidationException;
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
import java.util.stream.Collectors;

/**
 * Service class for Cart operations
 * Implements lazy cart creation, auto-delete empty carts, and logout cleanup
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
     * Get or create active cart for user (lazy creation)
     */
    public CartDTO getOrCreateCart(Long userId) {
        log.info("Getting or creating cart for user ID: {}", userId);

        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));

        Cart cart = cartRepository.findActiveCartByUserId(userId)
            .orElseGet(() -> {
                log.info("Creating new cart for user ID: {}", userId);
                Cart newCart = Cart.builder()
                    .user(user)
                    .status(Cart.CartStatus.ACTIVE)
                    .totalAmount(BigDecimal.ZERO)
                    .totalItems(0)
                    .build();
                return cartRepository.save(newCart);
            });

        return convertToDTO(cart);
    }

    @Transactional(readOnly = true)
    public CartDTO getCartById(Long cartId) {
        log.info("Fetching cart by ID: {}", cartId);
        Cart cart = cartRepository.findById(cartId)
            .orElseThrow(() -> new ResourceNotFoundException("Cart not found with ID: " + cartId));
        return convertToDTO(cart);
    }

    @Transactional(readOnly = true)
    public CartDTO getActiveCart(Long userId) {
        log.info("Fetching active cart for user ID: {}", userId);
        Cart cart = cartRepository.findActiveCartByUserId(userId)
            .orElseThrow(() -> new ResourceNotFoundException("No active cart found for user ID: " + userId));
        return convertToDTO(cart);
    }

    /**
     * Add item to cart with quantity validation
     */
    public CartDTO addItemToCart(Long userId, AddToCartRequest request) {
        log.info("Adding item to cart for user ID: {}, product ID: {}, quantity: {}",
            userId, request.getProductId(), request.getQuantity());

        // Get or create cart (lazy creation)
        Cart cart = cartRepository.findActiveCartByUserId(userId)
            .orElseGet(() -> {
                User user = userRepository.findById(userId)
                    .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));
                Cart newCart = Cart.builder()
                    .user(user)
                    .status(Cart.CartStatus.ACTIVE)
                    .totalAmount(BigDecimal.ZERO)
                    .totalItems(0)
                    .build();
                return cartRepository.save(newCart);
            });

        Product product = productRepository.findById(request.getProductId())
            .orElseThrow(() -> new ResourceNotFoundException("Product not found with ID: " + request.getProductId()));

        // Validate stock availability
        if (product.getStockQuantity() < request.getQuantity()) {
            throw new ValidationException("Insufficient stock for product: " + product.getName() +
                ". Available: " + product.getStockQuantity() + ", Requested: " + request.getQuantity());
        }

        // Check if item already exists in cart
        CartItem cartItem = cartItemRepository.findByCartIdAndProductId(cart.getId(), product.getId())
            .orElse(null);

        if (cartItem != null) {
            // Update existing item quantity
            int newQuantity = cartItem.getQuantity() + request.getQuantity();
            if (product.getStockQuantity() < newQuantity) {
                throw new ValidationException("Insufficient stock for product: " + product.getName() +
                    ". Available: " + product.getStockQuantity() + ", Requested: " + newQuantity);
            }
            cartItem.setQuantity(newQuantity);
            cartItemRepository.save(cartItem);
        } else {
            // Create new cart item
            cartItem = CartItem.builder()
                .cart(cart)
                .product(product)
                .quantity(request.getQuantity())
                .price(product.getPrice())
                .build();
            cart.addItem(cartItem);
            cartItemRepository.save(cartItem);
        }

        cart.recalculateTotals();
        Cart savedCart = cartRepository.save(cart);

        log.info("Item added to cart successfully. Cart ID: {}, Total items: {}",
            savedCart.getId(), savedCart.getTotalItems());

        return convertToDTO(savedCart);
    }

    /**
     * Update cart item quantity with validation
     */
    public CartDTO updateCartItemQuantity(Long userId, Long cartItemId, Integer quantity) {
        log.info("Updating cart item ID: {} to quantity: {} for user ID: {}",
            cartItemId, quantity, userId);

        CartItem cartItem = cartItemRepository.findById(cartItemId)
            .orElseThrow(() -> new ResourceNotFoundException("Cart item not found with ID: " + cartItemId));

        Cart cart = cartItem.getCart();
        if (!cart.getUser().getId().equals(userId)) {
            throw new ValidationException("Cart item does not belong to user");
        }

        Product product = cartItem.getProduct();

        // Validate stock availability
        if (product.getStockQuantity() < quantity) {
            throw new ValidationException("Insufficient stock for product: " + product.getName() +
                ". Available: " + product.getStockQuantity() + ", Requested: " + quantity);
        }

        cartItem.setQuantity(quantity);
        cartItemRepository.save(cartItem);

        cart.recalculateTotals();
        Cart savedCart = cartRepository.save(cart);

        log.info("Cart item quantity updated successfully");

        return convertToDTO(savedCart);
    }

    /**
     * Remove item from cart and auto-delete if empty
     */
    public CartDTO removeItemFromCart(Long userId, Long cartItemId) {
        log.info("Removing cart item ID: {} for user ID: {}", cartItemId, userId);

        CartItem cartItem = cartItemRepository.findById(cartItemId)
            .orElseThrow(() -> new ResourceNotFoundException("Cart item not found with ID: " + cartItemId));

        Cart cart = cartItem.getCart();
        if (!cart.getUser().getId().equals(userId)) {
            throw new ValidationException("Cart item does not belong to user");
        }

        cart.removeItem(cartItem);
        cartItemRepository.delete(cartItem);

        // Auto-delete cart if empty
        if (cart.isEmpty()) {
            log.info("Cart is empty, deleting cart ID: {}", cart.getId());
            cartRepository.delete(cart);
            return null;
        }

        cart.recalculateTotals();
        Cart savedCart = cartRepository.save(cart);

        log.info("Cart item removed successfully");

        return convertToDTO(savedCart);
    }

    /**
     * Clear all items from cart and delete cart
     */
    public void clearCart(Long userId) {
        log.info("Clearing cart for user ID: {}", userId);

        Cart cart = cartRepository.findActiveCartByUserId(userId)
            .orElseThrow(() -> new ResourceNotFoundException("No active cart found for user ID: " + userId));

        cartItemRepository.deleteByCartId(cart.getId());
        cartRepository.delete(cart);

        log.info("Cart cleared and deleted successfully for user ID: {}", userId);
    }

    /**
     * Cleanup carts on user logout
     */
    public void cleanupOnLogout(Long userId) {
        log.info("Cleaning up carts on logout for user ID: {}", userId);

        // Delete all active carts for user
        cartRepository.deleteActiveCartsByUserId(userId);

        log.info("Logout cleanup completed for user ID: {}", userId);
    }

    /**
     * Delete empty carts for user
     */
    public void deleteEmptyCarts(Long userId) {
        log.info("Deleting empty carts for user ID: {}", userId);
        cartRepository.deleteEmptyCartsByUserId(userId);
        log.info("Empty carts deleted for user ID: {}", userId);
    }

    private CartDTO convertToDTO(Cart cart) {
        List<CartItemDTO> itemDTOs = cart.getItems().stream()
            .map(this::convertItemToDTO)
            .collect(Collectors.toList());

        return CartDTO.builder()
            .id(cart.getId())
            .userId(cart.getUser().getId())
            .username(cart.getUser().getUsername())
            .items(itemDTOs)
            .status(cart.getStatus())
            .totalAmount(cart.getTotalAmount())
            .totalItems(cart.getTotalItems())
            .createdAt(cart.getCreatedAt())
            .updatedAt(cart.getUpdatedAt())
            .build();
    }

    private CartItemDTO convertItemToDTO(CartItem item) {
        return CartItemDTO.builder()
            .id(item.getId())
            .cartId(item.getCart().getId())
            .productId(item.getProduct().getId())
            .productName(item.getProduct().getName())
            .productSku(item.getProduct().getSku())
            .quantity(item.getQuantity())
            .price(item.getPrice())
            .subtotal(item.getSubtotal())
            .createdAt(item.getCreatedAt())
            .updatedAt(item.getUpdatedAt())
            .build();
    }
}