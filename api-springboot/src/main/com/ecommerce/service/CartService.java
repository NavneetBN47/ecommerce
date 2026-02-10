package com.ecommerce.service;

import com.ecommerce.dto.AddToCartRequest;
import com.ecommerce.dto.CartItemResponse;
import com.ecommerce.dto.CartResponse;
import com.ecommerce.dto.UpdateCartItemRequest;
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
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CartService {
    
    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    
    @Transactional
    public CartResponse addProductToCart(UUID userId, AddToCartRequest request) {
        log.info("Adding product {} to cart for user {}", request.getProductId(), userId);
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        
        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));
        
        if (request.getQuantity() <= 0) {
            throw new ValidationException("Quantity must be greater than 0");
        }
        
        // Lazy cart creation
        Cart cart = cartRepository.findByUserId(userId)
                .orElseGet(() -> {
                    log.info("Creating new cart for user {}", userId);
                    Cart newCart = Cart.builder()
                            .user(user)
                            .build();
                    return cartRepository.save(newCart);
                });
        
        // Check if product already in cart
        CartItem existingItem = cartItemRepository.findByCartIdAndProductId(cart.getId(), product.getId())
                .orElse(null);
        
        if (existingItem != null) {
            log.info("Updating existing cart item quantity");
            existingItem.setQuantity(existingItem.getQuantity() + request.getQuantity());
            cartItemRepository.save(existingItem);
        } else {
            log.info("Adding new item to cart");
            CartItem newItem = CartItem.builder()
                    .cart(cart)
                    .product(product)
                    .quantity(request.getQuantity())
                    .build();
            cart.addItem(newItem);
            cartItemRepository.save(newItem);
        }
        
        log.info("Product added to cart successfully");
        return getCartResponse(cart);
    }
    
    @Transactional
    public CartResponse updateCartItem(UUID userId, UUID itemId, UpdateCartItemRequest request) {
        log.info("Updating cart item {} for user {}", itemId, userId);
        
        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart not found"));
        
        CartItem cartItem = cartItemRepository.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart item not found"));
        
        if (!cartItem.getCart().getId().equals(cart.getId())) {
            throw new ValidationException("Cart item does not belong to user");
        }
        
        if (request.getQuantity() <= 0) {
            throw new ValidationException("Quantity must be greater than 0");
        }
        
        cartItem.setQuantity(request.getQuantity());
        cartItemRepository.save(cartItem);
        
        log.info("Cart item updated successfully");
        return getCartResponse(cart);
    }
    
    @Transactional
    public CartResponse removeCartItem(UUID userId, UUID itemId) {
        log.info("Removing cart item {} for user {}", itemId, userId);
        
        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart not found"));
        
        CartItem cartItem = cartItemRepository.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart item not found"));
        
        if (!cartItem.getCart().getId().equals(cart.getId())) {
            throw new ValidationException("Cart item does not belong to user");
        }
        
        cart.removeItem(cartItem);
        cartItemRepository.delete(cartItem);
        
        // Auto-delete cart if empty
        if (cart.getItems().isEmpty()) {
            log.info("Cart is empty, deleting cart");
            cartRepository.delete(cart);
            return null;
        }
        
        log.info("Cart item removed successfully");
        return getCartResponse(cart);
    }
    
    @Transactional(readOnly = true)
    public CartResponse getCart(UUID userId) {
        log.info("Fetching cart for user {}", userId);
        
        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart not found"));
        
        return getCartResponse(cart);
    }
    
    @Transactional
    public void clearCart(UUID userId) {
        log.info("Clearing cart for user {}", userId);
        cartRepository.deleteByUserId(userId);
        log.info("Cart cleared successfully");
    }
    
    private CartResponse getCartResponse(Cart cart) {
        List<CartItemResponse> items = cart.getItems().stream()
                .map(item -> {
                    BigDecimal itemTotal = item.getProduct().getPrice()
                            .multiply(BigDecimal.valueOf(item.getQuantity()));
                    return CartItemResponse.builder()
                            .itemId(item.getId())
                            .productId(item.getProduct().getId())
                            .name(item.getProduct().getName())
                            .quantity(item.getQuantity())
                            .price(item.getProduct().getPrice())
                            .total(itemTotal)
                            .build();
                })
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
}