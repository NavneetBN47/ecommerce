package com.ecommerce.service;

import com.ecommerce.dto.AddToCartRequest;
import com.ecommerce.dto.CartItemDTO;
import com.ecommerce.dto.CartSummaryResponse;
import com.ecommerce.dto.UpdateCartItemRequest;
import com.ecommerce.entity.CartItem;
import com.ecommerce.entity.Product;
import com.ecommerce.entity.ShoppingCart;
import com.ecommerce.exception.InsufficientStockException;
import com.ecommerce.exception.ResourceNotFoundException;
import com.ecommerce.repository.CartItemRepository;
import com.ecommerce.repository.CartRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for shopping cart management
 * Implements:
 * - Lazy cart creation (cart created only when first item is added)
 * - Auto-delete empty cart (cart deleted when last item is removed)
 * - Logout cleanup (cart deleted on logout)
 * - Quantity validation (check stock before operations)
 * - Totals calculation (subtotal, tax, grand total)
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductService productService;

    private static final BigDecimal TAX_RATE = new BigDecimal("0.10"); // 10% tax

    /**
     * Add item to cart with lazy cart creation
     * Business Rule: Cart is created lazily - only when first item is added
     * Business Rule: Validate stock before adding to cart
     */
    @Transactional
    public CartSummaryResponse addToCart(Long userId, AddToCartRequest request) {
        log.info("Adding product {} to cart for user {}", request.getProductId(), userId);

        // Get product and validate stock
        Product product = productService.getProductEntity(request.getProductId());
        validateStock(product, request.getQuantity());

        // Lazy cart creation - find or create cart
        ShoppingCart cart = cartRepository.findByUserId(userId)
                .orElseGet(() -> {
                    log.info("Creating new cart for user {}", userId);
                    ShoppingCart newCart = new ShoppingCart();
                    newCart.setUserId(userId);
                    return cartRepository.save(newCart);
                });

        // Check if product already in cart
        CartItem cartItem = cartItemRepository.findByCartIdAndProductId(cart.getCartId(), product.getProductId())
                .orElse(null);

        if (cartItem != null) {
            // Update existing cart item
            int newQuantity = cartItem.getQuantity() + request.getQuantity();
            validateStock(product, newQuantity);
            cartItem.setQuantity(newQuantity);
            log.info("Updated cart item quantity to {}", newQuantity);
        } else {
            // Create new cart item
            cartItem = new CartItem();
            cartItem.setCart(cart);
            cartItem.setProduct(product);
            cartItem.setQuantity(request.getQuantity());
            cartItem.setPriceAtAddition(product.getPrice());
            log.info("Created new cart item");
        }

        cartItemRepository.save(cartItem);
        return getCartSummary(userId);
    }

    /**
     * Update cart item quantity
     * Business Rule: Validate stock before updating quantity
     */
    @Transactional
    public CartSummaryResponse updateCartItem(Long userId, Long cartItemId, UpdateCartItemRequest request) {
        log.info("Updating cart item {} for user {}", cartItemId, userId);

        CartItem cartItem = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart item not found: " + cartItemId));

        // Verify cart belongs to user
        ShoppingCart cart = cartRepository.findById(cartItem.getCart().getCartId())
                .orElseThrow(() -> new ResourceNotFoundException("Cart not found"));
        
        if (!cart.getUserId().equals(userId)) {
            throw new ResourceNotFoundException("Cart item not found for this user");
        }

        // Validate stock
        Product product = cartItem.getProduct();
        validateStock(product, request.getQuantity());

        cartItem.setQuantity(request.getQuantity());
        cartItemRepository.save(cartItem);

        return getCartSummary(userId);
    }

    /**
     * Remove item from cart
     * Business Rule: Auto-delete empty cart when last item is removed
     */
    @Transactional
    public CartSummaryResponse removeFromCart(Long userId, Long cartItemId) {
        log.info("Removing cart item {} for user {}", cartItemId, userId);

        CartItem cartItem = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart item not found: " + cartItemId));

        ShoppingCart cart = cartRepository.findById(cartItem.getCart().getCartId())
                .orElseThrow(() -> new ResourceNotFoundException("Cart not found"));

        if (!cart.getUserId().equals(userId)) {
            throw new ResourceNotFoundException("Cart item not found for this user");
        }

        // Remove the cart item
        cartItemRepository.delete(cartItem);

        // Check if cart is now empty and auto-delete if so
        long remainingItems = cartItemRepository.countByCartId(cart.getCartId());
        if (remainingItems == 0) {
            log.info("Cart is empty, auto-deleting cart for user {}", userId);
            cartRepository.delete(cart);
            return new CartSummaryResponse(null, List.of(), 0, BigDecimal.ZERO, BigDecimal.ZERO, TAX_RATE, BigDecimal.ZERO);
        }

        return getCartSummary(userId);
    }

    /**
     * Get cart summary with totals calculation
     * Business Rule: Calculate subtotal, tax, and grand total
     */
    @Transactional(readOnly = true)
    public CartSummaryResponse getCartSummary(Long userId) {
        log.info("Getting cart summary for user {}", userId);

        ShoppingCart cart = cartRepository.findByUserId(userId).orElse(null);

        if (cart == null) {
            return new CartSummaryResponse(null, List.of(), 0, BigDecimal.ZERO, BigDecimal.ZERO, TAX_RATE, BigDecimal.ZERO);
        }

        List<CartItem> cartItems = cartItemRepository.findByCartId(cart.getCartId());

        if (cartItems.isEmpty()) {
            return new CartSummaryResponse(cart.getCartId(), List.of(), 0, BigDecimal.ZERO, BigDecimal.ZERO, TAX_RATE, BigDecimal.ZERO);
        }

        // Convert to DTOs and calculate totals
        List<CartItemDTO> itemDTOs = cartItems.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        BigDecimal subtotal = cartItems.stream()
                .map(CartItem::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal taxAmount = subtotal.multiply(TAX_RATE);
        BigDecimal grandTotal = subtotal.add(taxAmount);

        int totalItems = cartItems.stream()
                .mapToInt(CartItem::getQuantity)
                .sum();

        return new CartSummaryResponse(
                cart.getCartId(),
                itemDTOs,
                totalItems,
                subtotal,
                taxAmount,
                TAX_RATE,
                grandTotal
        );
    }

    /**
     * Clear cart (logout cleanup)
     * Business Rule: All cart data must be cleaned up when user logs out
     */
    @Transactional
    public void clearCart(Long userId) {
        log.info("Clearing cart for user {} (logout cleanup)", userId);

        ShoppingCart cart = cartRepository.findByUserId(userId).orElse(null);
        
        if (cart != null) {
            cartItemRepository.deleteByCartId(cart.getCartId());
            cartRepository.delete(cart);
            log.info("Cart cleared successfully for user {}", userId);
        }
    }

    /**
     * Validate product stock
     * Business Rule: Always check stock before cart operations
     */
    private void validateStock(Product product, int requestedQuantity) {
        if (requestedQuantity <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than 0");
        }

        if (!product.hasStock(requestedQuantity)) {
            throw new InsufficientStockException(
                    String.format("Insufficient stock for product '%s'. Available: %d, Requested: %d",
                            product.getProductName(), product.getStockQuantity(), requestedQuantity)
            );
        }
    }

    /**
     * Convert CartItem to DTO
     */
    private CartItemDTO convertToDTO(CartItem cartItem) {
        CartItemDTO dto = new CartItemDTO();
        dto.setCartItemId(cartItem.getCartItemId());
        dto.setProductId(cartItem.getProduct().getProductId());
        dto.setProductName(cartItem.getProduct().getProductName());
        dto.setProductImage(cartItem.getProduct().getImageUrl());
        dto.setPrice(cartItem.getPriceAtAddition());
        dto.setQuantity(cartItem.getQuantity());
        dto.setSubtotal(cartItem.getSubtotal());
        return dto;
    }
}