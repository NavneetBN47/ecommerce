package com.ecommerce.service;

import com.ecommerce.dto.*;
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
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CartService {
    
    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    
    @Transactional(readOnly = true)
    public CartResponse getCart(Long userId) {
        log.info("Fetching cart for user: {}", userId);
        
        Cart cart = cartRepository.findByUserIdWithItems(userId).orElse(null);
        
        if (cart == null) {
            log.info("No cart found for user: {}", userId);
            return CartResponse.builder()
                .userId(userId)
                .items(List.of())
                .cartTotal(BigDecimal.ZERO)
                .itemCount(0)
                .build();
        }
        
        return mapToCartResponse(cart);
    }
    
    @Transactional
    public CartItemResponse addCartItem(Long userId, AddCartItemRequest request) {
        log.info("Adding item to cart for user: {}, product: {}, quantity: {}", 
            userId, request.getProductId(), request.getQuantity());
        
        Product product = productRepository.findById(request.getProductId())
            .orElseThrow(() -> new ResourceNotFoundException("Product not found: " + request.getProductId()));
        
        if (!product.getIsActive()) {
            throw new ResourceNotFoundException("Product is not active: " + request.getProductId());
        }
        
        if (product.getAvailableQty() < request.getQuantity()) {
            throw new InsufficientStockException("Insufficient stock for product: " + product.getProductName());
        }
        
        Cart cart = cartRepository.findByUser_UserId(userId).orElseGet(() -> {
            log.info("Creating new cart for user: {}", userId);
            User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));
            Cart newCart = new Cart();
            newCart.setUser(user);
            return cartRepository.save(newCart);
        });
        
        CartItem cartItem = cartItemRepository.findByCart_CartIdAndProduct_ProductId(
            cart.getCartId(), request.getProductId()).orElse(null);
        
        if (cartItem != null) {
            int newQuantity = cartItem.getQuantity() + request.getQuantity();
            if (product.getAvailableQty() < newQuantity) {
                throw new InsufficientStockException("Insufficient stock for product: " + product.getProductName());
            }
            cartItem.setQuantity(newQuantity);
            log.info("Updated existing cart item quantity to: {}", newQuantity);
        } else {
            cartItem = new CartItem();
            cartItem.setCart(cart);
            cartItem.setProduct(product);
            cartItem.setQuantity(request.getQuantity());
            cartItem.setPriceAtAddition(product.getPrice());
            log.info("Created new cart item");
        }
        
        CartItem savedItem = cartItemRepository.save(cartItem);
        
        return mapToCartItemResponse(savedItem);
    }
    
    @Transactional
    public CartItemResponse updateCartItem(Long userId, Long cartItemId, UpdateCartItemRequest request) {
        log.info("Updating cart item: {} for user: {}, new quantity: {}", 
            cartItemId, userId, request.getQuantity());
        
        CartItem cartItem = cartItemRepository.findById(cartItemId)
            .orElseThrow(() -> new ResourceNotFoundException("Cart item not found: " + cartItemId));
        
        if (!cartItem.getCart().getUser().getUserId().equals(userId)) {
            throw new ResourceNotFoundException("Cart item not found for user");
        }
        
        if (request.getQuantity() == 0) {
            log.info("Quantity is 0, removing cart item: {}", cartItemId);
            return removeCartItem(userId, cartItemId);
        }
        
        Product product = cartItem.getProduct();
        if (product.getAvailableQty() < request.getQuantity()) {
            throw new InsufficientStockException("Insufficient stock for product: " + product.getProductName());
        }
        
        cartItem.setQuantity(request.getQuantity());
        CartItem updatedItem = cartItemRepository.save(cartItem);
        
        return mapToCartItemResponse(updatedItem);
    }
    
    @Transactional
    public CartItemResponse removeCartItem(Long userId, Long cartItemId) {
        log.info("Removing cart item: {} for user: {}", cartItemId, userId);
        
        CartItem cartItem = cartItemRepository.findById(cartItemId)
            .orElseThrow(() -> new ResourceNotFoundException("Cart item not found: " + cartItemId));
        
        if (!cartItem.getCart().getUser().getUserId().equals(userId)) {
            throw new ResourceNotFoundException("Cart item not found for user");
        }
        
        Long cartId = cartItem.getCart().getCartId();
        CartItemResponse response = mapToCartItemResponse(cartItem);
        
        cartItemRepository.delete(cartItem);
        
        long itemCount = cartItemRepository.countByCart_CartId(cartId);
        if (itemCount == 0) {
            log.info("Cart is empty, deleting cart: {}", cartId);
            cartRepository.deleteById(cartId);
        }
        
        return response;
    }
    
    @Transactional
    public void clearCart(Long userId) {
        log.info("Clearing cart for user: {}", userId);
        
        Cart cart = cartRepository.findByUser_UserId(userId).orElse(null);
        
        if (cart != null) {
            cartItemRepository.deleteByCartId(cart.getCartId());
            cartRepository.delete(cart);
            log.info("Cart cleared for user: {}", userId);
        }
    }
    
    private CartResponse mapToCartResponse(Cart cart) {
        List<CartItemResponse> items = cart.getCartItems().stream()
            .map(this::mapToCartItemResponse)
            .collect(Collectors.toList());
        
        BigDecimal total = items.stream()
            .map(CartItemResponse::getItemTotal)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        return CartResponse.builder()
            .cartId(cart.getCartId())
            .userId(cart.getUser().getUserId())
            .items(items)
            .cartTotal(total)
            .itemCount(items.size())
            .build();
    }
    
    private CartItemResponse mapToCartItemResponse(CartItem cartItem) {
        BigDecimal itemTotal = cartItem.getPriceAtAddition()
            .multiply(BigDecimal.valueOf(cartItem.getQuantity()));
        
        return CartItemResponse.builder()
            .cartItemId(cartItem.getCartItemId())
            .productId(cartItem.getProduct().getProductId())
            .productName(cartItem.getProduct().getProductName())
            .price(cartItem.getPriceAtAddition())
            .quantity(cartItem.getQuantity())
            .itemTotal(itemTotal)
            .build();
    }
}
