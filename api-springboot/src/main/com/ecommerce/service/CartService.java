package com.ecommerce.service;

import com.ecommerce.dto.*;
import com.ecommerce.entity.Cart;
import com.ecommerce.entity.CartItem;
import com.ecommerce.entity.Product;
import com.ecommerce.exception.ResourceNotFoundException;
import com.ecommerce.repository.CartItemRepository;
import com.ecommerce.repository.CartRepository;
import com.ecommerce.repository.ProductRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Cart Service
 * Business logic for shopping cart operations
 * Implements lazy cart creation and auto-delete empty cart
 */
@Service
@Transactional
public class CartService {

    private static final Logger logger = LoggerFactory.getLogger(CartService.class);

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private CartItemRepository cartItemRepository;

    @Autowired
    private ProductRepository productRepository;

    /**
     * Add product to cart (lazy cart creation)
     * @param userId user ID
     * @param request add to cart request
     * @return cart response
     */
    public CartResponse addProductToCart(Long userId, AddToCartRequest request) {
        logger.info("Adding product {} to cart for user {}", request.getProductId(), userId);
        
        // Validate product exists
        Product product = productRepository.findById(request.getProductId())
            .orElseThrow(() -> {
                logger.error("Product not found: {}", request.getProductId());
                return new ResourceNotFoundException("Product not found");
            });

        // Validate quantity
        if (request.getQuantity() <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than 0");
        }

        // Lazy cart creation: find or create cart
        Cart cart = cartRepository.findByUserId(userId)
            .orElseGet(() -> {
                logger.info("Creating new cart for user {}", userId);
                Cart newCart = new Cart();
                newCart.setUserId(userId);
                return cartRepository.save(newCart);
            });

        // Check if product already in cart
        CartItem cartItem = cartItemRepository.findByCartIdAndProductId(cart.getId(), product.getId())
            .orElse(null);

        if (cartItem != null) {
            // Update existing cart item
            logger.info("Updating existing cart item quantity");
            cartItem.setQuantity(cartItem.getQuantity() + request.getQuantity());
        } else {
            // Add new cart item
            logger.info("Adding new cart item");
            cartItem = new CartItem();
            cartItem.setCart(cart);
            cartItem.setProduct(product);
            cartItem.setQuantity(request.getQuantity());
            cart.getItems().add(cartItem);
        }

        cartItemRepository.save(cartItem);
        logger.info("Product added to cart successfully");

        return getCartByUserId(userId);
    }

    /**
     * Update cart item quantity
     * @param userId user ID
     * @param itemId cart item ID
     * @param request update cart item request
     * @return cart response
     */
    public CartResponse updateCartItemQuantity(Long userId, Long itemId, UpdateCartItemRequest request) {
        logger.info("Updating cart item {} quantity for user {}", itemId, userId);
        
        // Validate quantity
        if (request.getQuantity() <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than 0");
        }

        CartItem cartItem = cartItemRepository.findById(itemId)
            .orElseThrow(() -> {
                logger.error("Cart item not found: {}", itemId);
                return new ResourceNotFoundException("Cart item not found");
            });

        // Verify cart belongs to user
        if (!cartItem.getCart().getUserId().equals(userId)) {
            throw new ResourceNotFoundException("Cart item not found");
        }

        cartItem.setQuantity(request.getQuantity());
        cartItemRepository.save(cartItem);
        logger.info("Cart item quantity updated successfully");

        return getCartByUserId(userId);
    }

    /**
     * Remove product from cart
     * Auto-deletes cart if empty
     * @param userId user ID
     * @param itemId cart item ID
     * @return cart response or null if cart deleted
     */
    public CartResponse removeProductFromCart(Long userId, Long itemId) {
        logger.info("Removing cart item {} for user {}", itemId, userId);
        
        CartItem cartItem = cartItemRepository.findById(itemId)
            .orElseThrow(() -> {
                logger.error("Cart item not found: {}", itemId);
                return new ResourceNotFoundException("Cart item not found");
            });

        // Verify cart belongs to user
        Cart cart = cartItem.getCart();
        if (!cart.getUserId().equals(userId)) {
            throw new ResourceNotFoundException("Cart item not found");
        }

        // Remove cart item
        cartItemRepository.delete(cartItem);
        logger.info("Cart item removed successfully");

        // Auto-delete cart if empty
        long itemCount = cartItemRepository.countByCartId(cart.getId());
        if (itemCount == 0) {
            logger.info("Cart is empty, auto-deleting cart for user {}", userId);
            cartRepository.delete(cart);
            return new CartResponse(null, List.of(), BigDecimal.ZERO);
        }

        return getCartByUserId(userId);
    }

    /**
     * View cart
     * @param userId user ID
     * @return cart response
     */
    @Transactional(readOnly = true)
    public CartResponse viewCart(Long userId) {
        logger.info("Viewing cart for user {}", userId);
        return getCartByUserId(userId);
    }

    /**
     * Delete cart by user ID (called on logout)
     * @param userId user ID
     */
    public void deleteCartByUserId(Long userId) {
        logger.info("Deleting cart for user {}", userId);
        cartRepository.findByUserId(userId).ifPresent(cart -> {
            cartItemRepository.deleteByCartId(cart.getId());
            cartRepository.delete(cart);
            logger.info("Cart deleted successfully for user {}", userId);
        });
    }

    /**
     * Get cart by user ID
     * @param userId user ID
     * @return cart response
     */
    private CartResponse getCartByUserId(Long userId) {
        Cart cart = cartRepository.findByUserId(userId)
            .orElseThrow(() -> {
                logger.error("Cart not found for user {}", userId);
                return new ResourceNotFoundException("Cart not found");
            });

        List<CartItemResponse> items = cart.getItems().stream()
            .map(this::mapToCartItemResponse)
            .collect(Collectors.toList());

        BigDecimal grandTotal = items.stream()
            .map(CartItemResponse::getTotal)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        CartResponse response = new CartResponse();
        response.setCartId(cart.getId());
        response.setItems(items);
        response.setGrandTotal(grandTotal);

        return response;
    }

    /**
     * Map CartItem entity to CartItemResponse DTO
     */
    private CartItemResponse mapToCartItemResponse(CartItem cartItem) {
        Product product = cartItem.getProduct();
        BigDecimal total = product.getPrice().multiply(BigDecimal.valueOf(cartItem.getQuantity()));

        CartItemResponse response = new CartItemResponse();
        response.setItemId(cartItem.getId());
        response.setProductId(product.getId());
        response.setName(product.getName());
        response.setQuantity(cartItem.getQuantity());
        response.setPrice(product.getPrice());
        response.setTotal(total);

        return response;
    }
}