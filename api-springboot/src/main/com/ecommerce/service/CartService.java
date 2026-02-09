package com.ecommerce.service;

import com.ecommerce.dto.AddToCartRequest;
import com.ecommerce.dto.CartItemResponse;
import com.ecommerce.dto.CartResponse;
import com.ecommerce.entity.Cart;
import com.ecommerce.entity.CartItem;
import com.ecommerce.entity.Product;
import com.ecommerce.exception.InvalidOperationException;
import com.ecommerce.exception.ResourceNotFoundException;
import com.ecommerce.repository.CartItemRepository;
import com.ecommerce.repository.CartRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CartService {
    
    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductService productService;
    
    @Transactional
    public CartResponse addProductToCart(UUID userId, AddToCartRequest request) {
        log.info("Adding product {} to cart for user {}", request.getProductId(), userId);
        
        // Validate product exists and get details
        Product product = productService.getProductById(request.getProductId());
        
        // Validate quantity available
        Integer availableQty = productService.getAvailableQuantity(request.getProductId());
        if (request.getQuantity() > availableQty) {
            throw new InvalidOperationException(
                String.format("Requested quantity %d exceeds available quantity %d", 
                    request.getQuantity(), availableQty));
        }
        
        // Get or create cart (lazy creation)
        Cart cart = cartRepository.findByUserId(userId)
            .orElseGet(() -> createCart(userId));
        
        // Check if product already in cart
        CartItem cartItem = cartItemRepository
            .findByCartIdAndProductId(cart.getId(), request.getProductId())
            .orElse(null);
        
        if (cartItem != null) {
            // Update existing cart item
            int newQuantity = cartItem.getQuantity() + request.getQuantity();
            if (newQuantity > availableQty) {
                throw new InvalidOperationException(
                    String.format("Total quantity %d exceeds available quantity %d", 
                        newQuantity, availableQty));
            }
            cartItem.setQuantity(newQuantity);
            cartItemRepository.save(cartItem);
            log.info("Updated cart item quantity to {}", newQuantity);
        } else {
            // Add new cart item
            cartItem = new CartItem();
            cartItem.setCart(cart);
            cartItem.setProduct(product);
            cartItem.setQuantity(request.getQuantity());
            cartItem.setUnitPrice(product.getPrice());
            cartItemRepository.save(cartItem);
            log.info("Added new item to cart");
        }
        
        return getCartResponse(userId);
    }
    
    @Transactional
    public CartResponse updateCartItem(UUID userId, UUID itemId, Integer quantity) {
        log.info("Updating cart item {} to quantity {} for user {}", itemId, quantity, userId);
        
        CartItem cartItem = cartItemRepository.findById(itemId)
            .orElseThrow(() -> new ResourceNotFoundException("Cart item", "id", itemId));
        
        // Verify cart belongs to user
        Cart cart = cartRepository.findByUserId(userId)
            .orElseThrow(() -> new ResourceNotFoundException("Cart not found for user"));
        
        if (!cartItem.getCart().getId().equals(cart.getId())) {
            throw new InvalidOperationException("Cart item does not belong to user");
        }
        
        // Validate quantity available
        Integer availableQty = productService.getAvailableQuantity(cartItem.getProduct().getId());
        if (quantity > availableQty) {
            throw new InvalidOperationException(
                String.format("Requested quantity %d exceeds available quantity %d", 
                    quantity, availableQty));
        }
        
        cartItem.setQuantity(quantity);
        cartItemRepository.save(cartItem);
        log.info("Cart item updated successfully");
        
        return getCartResponse(userId);
    }
    
    @Transactional
    public CartResponse removeCartItem(UUID userId, UUID itemId) {
        log.info("Removing cart item {} for user {}", itemId, userId);
        
        CartItem cartItem = cartItemRepository.findById(itemId)
            .orElseThrow(() -> new ResourceNotFoundException("Cart item", "id", itemId));
        
        // Verify cart belongs to user
        Cart cart = cartRepository.findByUserId(userId)
            .orElseThrow(() -> new ResourceNotFoundException("Cart not found for user"));
        
        if (!cartItem.getCart().getId().equals(cart.getId())) {
            throw new InvalidOperationException("Cart item does not belong to user");
        }
        
        cartItemRepository.delete(cartItem);
        log.info("Cart item removed successfully");
        
        // Check if cart is now empty and delete if so
        long itemCount = cartItemRepository.countByCartId(cart.getId());
        if (itemCount == 0) {
            cartRepository.delete(cart);
            log.info("Cart deleted as it is now empty");
            return null;
        }
        
        return getCartResponse(userId);
    }
    
    @Transactional(readOnly = true)
    public CartResponse getCart(UUID userId) {
        log.info("Fetching cart for user {}", userId);
        return getCartResponse(userId);
    }
    
    @Transactional
    public void clearCart(UUID userId) {
        log.info("Clearing cart for user {}", userId);
        cartRepository.deleteByUserId(userId);
        log.info("Cart cleared successfully");
    }
    
    private Cart createCart(UUID userId) {
        log.info("Creating new cart for user {}", userId);
        Cart cart = new Cart();
        cart.setUserId(userId);
        return cartRepository.save(cart);
    }
    
    private CartResponse getCartResponse(UUID userId) {
        Cart cart = cartRepository.findByUserId(userId)
            .orElseThrow(() -> new ResourceNotFoundException("Cart not found for user"));
        
        List<CartItemResponse> items = cart.getItems().stream()
            .map(this::mapToCartItemResponse)
            .collect(Collectors.toList());
        
        BigDecimal grandTotal = items.stream()
            .map(CartItemResponse::getTotal)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        return CartResponse.builder()
            .cartId(cart.getId())
            .items(items)
            .grandTotal(grandTotal)
            .build();
    }
    
    private CartItemResponse mapToCartItemResponse(CartItem item) {
        return CartItemResponse.builder()
            .itemId(item.getId())
            .productId(item.getProduct().getId())
            .name(item.getProduct().getName())
            .quantity(item.getQuantity())
            .price(item.getUnitPrice())
            .total(item.getTotal())
            .build();
    }
}