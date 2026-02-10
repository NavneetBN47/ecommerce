package com.ecommerce.service;

import com.ecommerce.dto.AddItemRequest;
import com.ecommerce.dto.CartDTO;
import com.ecommerce.dto.CartItemDTO;
import com.ecommerce.entity.*;
import com.ecommerce.exception.*;
import com.ecommerce.repository.*;
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
 * Service layer for cart management business logic
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final CartHistoryRepository cartHistoryRepository;

    /**
     * Add item to cart - implements lazy cart creation and quantity increment logic
     */
    @Transactional
    public CartDTO addItemToCart(UUID cartId, AddItemRequest request) {
        log.info("Adding item to cart: cartId={}, productId={}, quantity={}", 
                 cartId, request.getProductId(), request.getQuantity());

        // Validate cart exists
        Cart cart = cartRepository.findByIdWithItems(cartId)
                .orElseThrow(() -> new CartNotFoundException("Cart not found with id: " + cartId));

        // Validate product exists and has sufficient stock
        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new ProductNotFoundException("Product not found with id: " + request.getProductId()));

        if (!product.getIsActive()) {
            throw new ProductNotAvailableException("Product is not active: " + request.getProductId());
        }

        if (product.getStockQuantity() < request.getQuantity()) {
            throw new InsufficientStockException(
                    String.format("Insufficient stock for product %s. Available: %d, Requested: %d",
                            request.getProductId(), product.getStockQuantity(), request.getQuantity()));
        }

        // Check if item already exists in cart
        CartItem cartItem = cartItemRepository.findByCartIdAndProductId(cartId, request.getProductId())
                .orElse(null);

        if (cartItem != null) {
            // Update existing item quantity
            int newQuantity = cartItem.getQuantity() + request.getQuantity();
            if (product.getStockQuantity() < newQuantity) {
                throw new InsufficientStockException(
                        String.format("Insufficient stock for product %s. Available: %d, Requested total: %d",
                                request.getProductId(), product.getStockQuantity(), newQuantity));
            }
            cartItem.setQuantity(newQuantity);
            cartItemRepository.save(cartItem);
            log.info("Updated cart item quantity: itemId={}, newQuantity={}", cartItem.getCartItemId(), newQuantity);
            
            // Record history
            recordHistory(cartId, CartHistory.ActionType.ITEM_UPDATED, 
                         String.format("{\"productId\":\"%s\",\"quantity\":%d}", request.getProductId(), newQuantity),
                         cart.getUserId());
        } else {
            // Add new item to cart
            cartItem = new CartItem();
            cartItem.setCartId(cartId);
            cartItem.setProductId(request.getProductId());
            cartItem.setQuantity(request.getQuantity());
            cartItem.setUnitPrice(product.getPrice());
            cartItemRepository.save(cartItem);
            log.info("Added new cart item: itemId={}", cartItem.getCartItemId());
            
            // Record history
            recordHistory(cartId, CartHistory.ActionType.ITEM_ADDED,
                         String.format("{\"productId\":\"%s\",\"quantity\":%d}", request.getProductId(), request.getQuantity()),
                         cart.getUserId());
        }

        // Update cart totals
        updateCartTotals(cart);

        return convertToDTO(cart);
    }

    /**
     * Remove item from cart
     */
    @Transactional
    public CartDTO removeItemFromCart(UUID cartId, UUID itemId) {
        log.info("Removing item from cart: cartId={}, itemId={}", cartId, itemId);

        Cart cart = cartRepository.findByIdWithItems(cartId)
                .orElseThrow(() -> new CartNotFoundException("Cart not found with id: " + cartId));

        CartItem cartItem = cartItemRepository.findById(itemId)
                .orElseThrow(() -> new CartItemNotFoundException("Cart item not found with id: " + itemId));

        if (!cartItem.getCartId().equals(cartId)) {
            throw new CartItemNotFoundException("Item does not belong to this cart");
        }

        cartItemRepository.delete(cartItem);
        log.info("Removed cart item: itemId={}", itemId);

        // Record history
        recordHistory(cartId, CartHistory.ActionType.ITEM_REMOVED,
                     String.format("{\"itemId\":\"%s\",\"productId\":\"%s\"}", itemId, cartItem.getProductId()),
                     cart.getUserId());

        // Update cart totals
        updateCartTotals(cart);

        // Auto-delete empty cart if configured
        if (cart.getItems().isEmpty()) {
            log.info("Cart is empty after item removal, considering auto-delete");
        }

        return convertToDTO(cart);
    }

    /**
     * Cleanup cart - remove all items
     */
    @Transactional
    public CartDTO cleanupCart(UUID cartId) {
        log.info("Cleaning up cart: cartId={}", cartId);

        Cart cart = cartRepository.findById(cartId)
                .orElseThrow(() -> new CartNotFoundException("Cart not found with id: " + cartId));

        cartItemRepository.deleteByCartId(cartId);
        log.info("Removed all items from cart: cartId={}", cartId);

        // Record history
        recordHistory(cartId, CartHistory.ActionType.STATUS_CHANGED,
                     "{\"action\":\"cleanup\",\"itemsRemoved\":true}",
                     cart.getUserId());

        // Update cart totals
        cart.setTotalAmount(BigDecimal.ZERO);
        cart.setTotalItems(0);
        cartRepository.save(cart);

        return convertToDTO(cart);
    }

    /**
     * View cart with all items
     */
    @Transactional(readOnly = true)
    public CartDTO viewCart(UUID cartId) {
        log.info("Viewing cart: cartId={}", cartId);

        Cart cart = cartRepository.findByIdWithItems(cartId)
                .orElseThrow(() -> new CartNotFoundException("Cart not found with id: " + cartId));

        return convertToDTO(cart);
    }

    /**
     * Get or create active cart for user (lazy creation)
     */
    @Transactional
    public CartDTO getOrCreateCart(UUID userId) {
        log.info("Getting or creating cart for user: userId={}", userId);

        // Validate user exists
        userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + userId));

        // Find existing active cart or create new one
        Cart cart = cartRepository.findByUserIdAndCartStatus(userId, Cart.CartStatus.ACTIVE)
                .orElseGet(() -> {
                    Cart newCart = new Cart();
                    newCart.setUserId(userId);
                    newCart.setCartStatus(Cart.CartStatus.ACTIVE);
                    newCart.setExpiresAt(LocalDateTime.now().plusDays(30));
                    Cart savedCart = cartRepository.save(newCart);
                    
                    // Record history
                    recordHistory(savedCart.getCartId(), CartHistory.ActionType.CREATED,
                                 "{\"cartStatus\":\"active\"}", userId);
                    
                    log.info("Created new cart: cartId={}", savedCart.getCartId());
                    return savedCart;
                });

        return convertToDTO(cart);
    }

    /**
     * Logout cleanup - handle cart cleanup on user logout
     */
    @Transactional
    public void logoutCleanup(UUID userId) {
        log.info("Performing logout cleanup for user: userId={}", userId);

        List<Cart> activeCarts = cartRepository.findByUserId(userId).stream()
                .filter(cart -> cart.getCartStatus() == Cart.CartStatus.ACTIVE)
                .collect(Collectors.toList());

        for (Cart cart : activeCarts) {
            if (cart.getItems().isEmpty()) {
                cart.setCartStatus(Cart.CartStatus.ABANDONED);
                cartRepository.save(cart);
                log.info("Marked empty cart as abandoned: cartId={}", cart.getCartId());
            }
        }
    }

    /**
     * Update cart totals based on items
     */
    private void updateCartTotals(Cart cart) {
        List<CartItem> items = cartItemRepository.findByCartId(cart.getCartId());
        
        BigDecimal totalAmount = items.stream()
                .map(item -> item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        int totalItems = items.stream()
                .mapToInt(CartItem::getQuantity)
                .sum();

        cart.setTotalAmount(totalAmount);
        cart.setTotalItems(totalItems);
        cartRepository.save(cart);
        
        log.info("Updated cart totals: cartId={}, totalAmount={}, totalItems={}", 
                 cart.getCartId(), totalAmount, totalItems);
    }

    /**
     * Record cart history
     */
    private void recordHistory(UUID cartId, CartHistory.ActionType actionType, String actionDetails, UUID performedBy) {
        CartHistory history = new CartHistory();
        history.setCartId(cartId);
        history.setActionType(actionType);
        history.setActionDetails(actionDetails);
        history.setPerformedBy(performedBy);
        cartHistoryRepository.save(history);
    }

    /**
     * Convert Cart entity to DTO
     */
    private CartDTO convertToDTO(Cart cart) {
        CartDTO dto = new CartDTO();
        dto.setCartId(cart.getCartId());
        dto.setUserId(cart.getUserId());
        dto.setCartStatus(cart.getCartStatus().name());
        dto.setSessionId(cart.getSessionId());
        dto.setTotalAmount(cart.getTotalAmount());
        dto.setTotalItems(cart.getTotalItems());
        dto.setCurrencyCode(cart.getCurrencyCode());
        dto.setCreatedAt(cart.getCreatedAt());
        dto.setUpdatedAt(cart.getUpdatedAt());
        dto.setExpiresAt(cart.getExpiresAt());

        List<CartItem> items = cartItemRepository.findByCartId(cart.getCartId());
        List<CartItemDTO> itemDTOs = items.stream()
                .map(this::convertItemToDTO)
                .collect(Collectors.toList());
        dto.setItems(itemDTOs);

        return dto;
    }

    /**
     * Convert CartItem entity to DTO
     */
    private CartItemDTO convertItemToDTO(CartItem item) {
        CartItemDTO dto = new CartItemDTO();
        dto.setItemId(item.getCartItemId());
        dto.setProductId(item.getProductId());
        dto.setQuantity(item.getQuantity());
        dto.setUnitPrice(item.getUnitPrice());
        dto.setTotalPrice(item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity())));
        dto.setAddedAt(item.getAddedAt());

        // Fetch product name
        productRepository.findById(item.getProductId())
                .ifPresent(product -> dto.setProductName(product.getProductName()));

        return dto;
    }
}