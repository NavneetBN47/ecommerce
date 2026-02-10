package com.ecommerce.service;

import com.ecommerce.dto.AddToCartRequestDTO;
import com.ecommerce.dto.CartDTO;
import com.ecommerce.dto.CartItemDTO;
import com.ecommerce.dto.ProductDTO;
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

import java.math.BigDecimal;
import java.util.stream.Collectors;

/**
 * Service class for Cart entity operations
 * Implements lazy cart creation, auto-delete empty cart, and logout cleanup
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
        
        Cart cart = cartRepository.findByUserIdWithItems(userId)
            .orElseGet(() -> {
                log.info("Creating new cart for user: {}", userId);
                User user = userRepository.findById(userId)
                    .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));
                
                Cart newCart = Cart.builder()
                    .user(user)
                    .totalAmount(BigDecimal.ZERO)
                    .totalItems(0)
                    .build();
                return cartRepository.save(newCart);
            });

        return mapToDTO(cart);
    }

    /**
     * Add item to cart
     */
    public CartDTO addItemToCart(Long userId, AddToCartRequestDTO request) {
        log.info("Adding item to cart for user: {}, product: {}, quantity: {}", 
            userId, request.getProductId(), request.getQuantity());

        // Get or create cart
        Cart cart = cartRepository.findByUserIdWithItems(userId)
            .orElseGet(() -> {
                User user = userRepository.findById(userId)
                    .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));
                return cartRepository.save(Cart.builder()
                    .user(user)
                    .totalAmount(BigDecimal.ZERO)
                    .totalItems(0)
                    .build());
            });

        // Get product
        Product product = productRepository.findById(request.getProductId())
            .orElseThrow(() -> new ResourceNotFoundException("Product not found with ID: " + request.getProductId()));

        // Check stock availability
        if (!productRepository.hasSufficientStock(product.getId(), request.getQuantity())) {
            throw new InsufficientStockException(
                String.format("Insufficient stock for product: %s. Available: %d, Requested: %d",
                    product.getName(), product.getStockQuantity(), request.getQuantity()));
        }

        // Check if item already exists in cart
        CartItem cartItem = cartItemRepository.findByCartIdAndProductId(cart.getId(), product.getId())
            .orElse(null);

        if (cartItem != null) {
            // Update existing item quantity
            int newQuantity = cartItem.getQuantity() + request.getQuantity();
            if (!productRepository.hasSufficientStock(product.getId(), newQuantity)) {
                throw new InsufficientStockException(
                    String.format("Insufficient stock for product: %s. Available: %d, Requested: %d",
                        product.getName(), product.getStockQuantity(), newQuantity));
            }
            cartItem.setQuantity(newQuantity);
            cartItem.calculateSubtotal();
            cartItemRepository.save(cartItem);
        } else {
            // Add new item to cart
            cartItem = CartItem.builder()
                .cart(cart)
                .product(product)
                .quantity(request.getQuantity())
                .price(product.getPrice())
                .build();
            cartItem.calculateSubtotal();
            cart.addItem(cartItem);
            cartItemRepository.save(cartItem);
        }

        // Recalculate cart totals
        cart.recalculateTotals();
        cart = cartRepository.save(cart);

        log.info("Item added to cart successfully for user: {}", userId);
        return mapToDTO(cart);
    }

    /**
     * Update cart item quantity
     */
    public CartDTO updateCartItemQuantity(Long userId, Long cartItemId, Integer quantity) {
        log.info("Updating cart item: {} quantity to: {} for user: {}", cartItemId, quantity, userId);

        Cart cart = cartRepository.findByUserIdWithItems(userId)
            .orElseThrow(() -> new ResourceNotFoundException("Cart not found for user: " + userId));

        CartItem cartItem = cart.getItems().stream()
            .filter(item -> item.getId().equals(cartItemId))
            .findFirst()
            .orElseThrow(() -> new ResourceNotFoundException("Cart item not found with ID: " + cartItemId));

        // Check stock availability
        if (!productRepository.hasSufficientStock(cartItem.getProduct().getId(), quantity)) {
            throw new InsufficientStockException(
                String.format("Insufficient stock for product: %s. Available: %d, Requested: %d",
                    cartItem.getProduct().getName(), cartItem.getProduct().getStockQuantity(), quantity));
        }

        cartItem.setQuantity(quantity);
        cartItem.calculateSubtotal();
        cartItemRepository.save(cartItem);

        // Recalculate cart totals
        cart.recalculateTotals();
        cart = cartRepository.save(cart);

        log.info("Cart item updated successfully for user: {}", userId);
        return mapToDTO(cart);
    }

    /**
     * Remove item from cart
     */
    public CartDTO removeItemFromCart(Long userId, Long cartItemId) {
        log.info("Removing cart item: {} for user: {}", cartItemId, userId);

        Cart cart = cartRepository.findByUserIdWithItems(userId)
            .orElseThrow(() -> new ResourceNotFoundException("Cart not found for user: " + userId));

        CartItem cartItem = cart.getItems().stream()
            .filter(item -> item.getId().equals(cartItemId))
            .findFirst()
            .orElseThrow(() -> new ResourceNotFoundException("Cart item not found with ID: " + cartItemId));

        cart.removeItem(cartItem);
        cartItemRepository.delete(cartItem);

        // Auto-delete empty cart
        if (cart.isEmpty()) {
            log.info("Cart is empty, deleting cart for user: {}", userId);
            cartRepository.delete(cart);
            return CartDTO.builder()
                .userId(userId)
                .totalAmount(BigDecimal.ZERO)
                .totalItems(0)
                .build();
        }

        // Recalculate cart totals
        cart.recalculateTotals();
        cart = cartRepository.save(cart);

        log.info("Cart item removed successfully for user: {}", userId);
        return mapToDTO(cart);
    }

    /**
     * Clear cart (for logout cleanup)
     */
    public void clearCart(Long userId) {
        log.info("Clearing cart for user: {}", userId);
        cartRepository.deleteByUserId(userId);
        log.info("Cart cleared successfully for user: {}", userId);
    }

    /**
     * Get cart by user ID
     */
    @Transactional(readOnly = true)
    public CartDTO getCartByUserId(Long userId) {
        log.debug("Fetching cart for user: {}", userId);
        Cart cart = cartRepository.findByUserIdWithItems(userId)
            .orElse(null);
        
        if (cart == null) {
            return CartDTO.builder()
                .userId(userId)
                .totalAmount(BigDecimal.ZERO)
                .totalItems(0)
                .build();
        }
        
        return mapToDTO(cart);
    }

    /**
     * Map Cart entity to CartDTO
     */
    private CartDTO mapToDTO(Cart cart) {
        return CartDTO.builder()
            .id(cart.getId())
            .userId(cart.getUser().getId())
            .items(cart.getItems().stream()
                .map(this::mapCartItemToDTO)
                .collect(Collectors.toList()))
            .totalAmount(cart.getTotalAmount())
            .totalItems(cart.getTotalItems())
            .createdAt(cart.getCreatedAt())
            .updatedAt(cart.getUpdatedAt())
            .build();
    }

    /**
     * Map CartItem entity to CartItemDTO
     */
    private CartItemDTO mapCartItemToDTO(CartItem cartItem) {
        return CartItemDTO.builder()
            .id(cartItem.getId())
            .productId(cartItem.getProduct().getId())
            .product(mapProductToDTO(cartItem.getProduct()))
            .quantity(cartItem.getQuantity())
            .price(cartItem.getPrice())
            .subtotal(cartItem.getSubtotal())
            .createdAt(cartItem.getCreatedAt())
            .updatedAt(cartItem.getUpdatedAt())
            .build();
    }

    /**
     * Map Product entity to ProductDTO
     */
    private ProductDTO mapProductToDTO(Product product) {
        return ProductDTO.builder()
            .id(product.getId())
            .name(product.getName())
            .description(product.getDescription())
            .sku(product.getSku())
            .price(product.getPrice())
            .stockQuantity(product.getStockQuantity())
            .category(product.getCategory())
            .imageUrl(product.getImageUrl())
            .build();
    }
}