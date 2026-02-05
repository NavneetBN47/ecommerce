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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service layer for Cart operations
 * Implements lazy cart creation, auto-delete empty cart, and logout cleanup logic
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    @Value("${app.cart.auto-delete-empty:true}")
    private boolean autoDeleteEmptyCart;

    @Value("${app.cart.cleanup-on-logout:true}")
    private boolean cleanupOnLogout;

    /**
     * Get or create cart for user (lazy cart creation)
     */
    @Transactional
    public CartDTO getOrCreateCart(Long userId) {
        log.debug("Getting or creating cart for user ID: {}", userId);
        
        Cart cart = cartRepository.findByUserIdWithItems(userId)
            .orElseGet(() -> createCartForUser(userId));
        
        return mapToDTO(cart);
    }

    @Transactional(readOnly = true)
    public CartDTO getCartByUserId(Long userId) {
        log.debug("Fetching cart for user ID: {}", userId);
        
        Cart cart = cartRepository.findByUserIdWithItems(userId)
            .orElseThrow(() -> new ResourceNotFoundException("Cart", "userId", userId));
        
        return mapToDTO(cart);
    }

    /**
     * Add item to cart with quantity check
     */
    @Transactional
    public CartDTO addItemToCart(Long userId, Long productId, Integer quantity) {
        log.info("Adding product ID {} with quantity {} to cart for user ID: {}", productId, quantity, userId);
        
        // Get or create cart (lazy creation)
        Cart cart = cartRepository.findByUserIdWithItems(userId)
            .orElseGet(() -> createCartForUser(userId));
        
        // Validate product and stock
        Product product = productRepository.findById(productId)
            .orElseThrow(() -> new ResourceNotFoundException("Product", "id", productId));
        
        if (!product.hasStock(quantity)) {
            throw new InsufficientStockException(
                String.format("Insufficient stock for product '%s'. Available: %d, Requested: %d",
                    product.getName(), product.getStockQuantity(), quantity));
        }
        
        // Check if item already exists in cart
        CartItem existingItem = cart.findItemByProduct(product);
        
        if (existingItem != null) {
            // Update quantity
            int newQuantity = existingItem.getQuantity() + quantity;
            if (!product.hasStock(newQuantity)) {
                throw new InsufficientStockException(
                    String.format("Insufficient stock for product '%s'. Available: %d, Requested: %d",
                        product.getName(), product.getStockQuantity(), newQuantity));
            }
            existingItem.setQuantity(newQuantity);
            existingItem.updateSubtotal();
            cartItemRepository.save(existingItem);
        } else {
            // Create new cart item
            CartItem cartItem = CartItem.builder()
                .cart(cart)
                .product(product)
                .quantity(quantity)
                .unitPrice(product.getEffectivePrice())
                .build();
            cartItem.updateSubtotal();
            cart.addItem(cartItem);
            cartItemRepository.save(cartItem);
        }
        
        cart.recalculateTotals();
        Cart savedCart = cartRepository.save(cart);
        
        log.info("Item added to cart successfully for user ID: {}", userId);
        return mapToDTO(savedCart);
    }

    @Transactional
    public CartDTO updateCartItemQuantity(Long userId, Long productId, Integer quantity) {
        log.info("Updating cart item quantity for user ID: {}, product ID: {}, new quantity: {}", 
            userId, productId, quantity);
        
        Cart cart = cartRepository.findByUserIdWithItems(userId)
            .orElseThrow(() -> new ResourceNotFoundException("Cart", "userId", userId));
        
        Product product = productRepository.findById(productId)
            .orElseThrow(() -> new ResourceNotFoundException("Product", "id", productId));
        
        CartItem cartItem = cart.findItemByProduct(product);
        if (cartItem == null) {
            throw new ResourceNotFoundException("CartItem", "productId", productId);
        }
        
        if (quantity <= 0) {
            // Remove item if quantity is 0 or negative
            cart.removeItem(cartItem);
            cartItemRepository.delete(cartItem);
        } else {
            // Validate stock
            if (!product.hasStock(quantity)) {
                throw new InsufficientStockException(
                    String.format("Insufficient stock for product '%s'. Available: %d, Requested: %d",
                        product.getName(), product.getStockQuantity(), quantity));
            }
            cartItem.setQuantity(quantity);
            cartItem.updateSubtotal();
            cartItemRepository.save(cartItem);
        }
        
        cart.recalculateTotals();
        Cart savedCart = cartRepository.save(cart);
        
        // Auto-delete empty cart if enabled
        if (autoDeleteEmptyCart && savedCart.isEmpty()) {
            log.info("Auto-deleting empty cart for user ID: {}", userId);
            cartRepository.delete(savedCart);
            return CartDTO.builder().userId(userId).isEmpty(true).build();
        }
        
        log.info("Cart item quantity updated successfully for user ID: {}", userId);
        return mapToDTO(savedCart);
    }

    @Transactional
    public CartDTO removeItemFromCart(Long userId, Long productId) {
        log.info("Removing product ID {} from cart for user ID: {}", productId, userId);
        
        Cart cart = cartRepository.findByUserIdWithItems(userId)
            .orElseThrow(() -> new ResourceNotFoundException("Cart", "userId", userId));
        
        Product product = productRepository.findById(productId)
            .orElseThrow(() -> new ResourceNotFoundException("Product", "id", productId));
        
        CartItem cartItem = cart.findItemByProduct(product);
        if (cartItem == null) {
            throw new ResourceNotFoundException("CartItem", "productId", productId);
        }
        
        cart.removeItem(cartItem);
        cartItemRepository.delete(cartItem);
        cart.recalculateTotals();
        Cart savedCart = cartRepository.save(cart);
        
        // Auto-delete empty cart if enabled
        if (autoDeleteEmptyCart && savedCart.isEmpty()) {
            log.info("Auto-deleting empty cart for user ID: {}", userId);
            cartRepository.delete(savedCart);
            return CartDTO.builder().userId(userId).isEmpty(true).build();
        }
        
        log.info("Item removed from cart successfully for user ID: {}", userId);
        return mapToDTO(savedCart);
    }

    @Transactional
    public void clearCart(Long userId) {
        log.info("Clearing cart for user ID: {}", userId);
        
        Cart cart = cartRepository.findByUserIdWithItems(userId)
            .orElseThrow(() -> new ResourceNotFoundException("Cart", "userId", userId));
        
        cart.clearItems();
        cartItemRepository.deleteByCartId(cart.getId());
        
        // Auto-delete empty cart if enabled
        if (autoDeleteEmptyCart) {
            log.info("Auto-deleting empty cart for user ID: {}", userId);
            cartRepository.delete(cart);
        } else {
            cartRepository.save(cart);
        }
        
        log.info("Cart cleared successfully for user ID: {}", userId);
    }

    /**
     * Cleanup cart on user logout
     * Implements logout cleanup business rule
     */
    @Transactional
    public void cleanupCartOnLogout(Long userId) {
        if (!cleanupOnLogout) {
            log.debug("Cart cleanup on logout is disabled");
            return;
        }
        
        log.info("Cleaning up cart for user ID {} on logout", userId);
        
        cartRepository.findByUserId(userId).ifPresent(cart -> {
            if (cart.isEmpty()) {
                log.info("Deleting empty cart for user ID {} on logout", userId);
                cartRepository.delete(cart);
            }
        });
    }

    /**
     * Scheduled cleanup of abandoned empty carts
     */
    @Transactional
    public int cleanupAbandonedEmptyCarts() {
        if (!autoDeleteEmptyCart) {
            log.debug("Auto-delete empty cart is disabled");
            return 0;
        }
        
        log.info("Running cleanup for abandoned empty carts");
        int deletedCount = cartRepository.deleteEmptyCarts();
        log.info("Deleted {} abandoned empty carts", deletedCount);
        return deletedCount;
    }

    private Cart createCartForUser(Long userId) {
        log.info("Creating new cart for user ID: {}", userId);
        
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
        
        Cart cart = Cart.builder()
            .user(user)
            .build();
        
        return cartRepository.save(cart);
    }

    private CartDTO mapToDTO(Cart cart) {
        List<CartItemDTO> itemDTOs = cart.getItems().stream()
            .map(this::mapItemToDTO)
            .collect(Collectors.toList());
        
        return CartDTO.builder()
            .id(cart.getId())
            .userId(cart.getUser().getId())
            .items(itemDTOs)
            .totalItems(cart.getTotalItems())
            .totalPrice(cart.getTotalPrice())
            .isEmpty(cart.isEmpty())
            .createdAt(cart.getCreatedAt())
            .updatedAt(cart.getUpdatedAt())
            .build();
    }

    private CartItemDTO mapItemToDTO(CartItem item) {
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
            .availableStock(item.getProduct().getStockQuantity())
            .createdAt(item.getCreatedAt())
            .updatedAt(item.getUpdatedAt())
            .build();
    }
}