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
import com.ecommerce.repository.CartItemRepository;
import com.ecommerce.repository.CartRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CartService {
    
    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final UserService userService;
    private final ProductService productService;
    
    @Transactional(readOnly = true)
    public CartResponse getCart(UUID userId) {
        log.info("Fetching cart for user ID: {}", userId);
        
        User user = userService.getUserById(userId);
        Cart cart = cartRepository.findByUser(user)
            .orElseThrow(() -> {
                log.warn("Cart not found for user: {}", userId);
                return new ResourceNotFoundException("Cart not found");
            });
        
        return mapToCartResponse(cart);
    }
    
    @Transactional
    public CartResponse addProductToCart(UUID userId, AddToCartRequest request) {
        log.info("Adding product {} to cart for user {}", request.getProductId(), userId);
        
        User user = userService.getUserById(userId);
        Product product = productService.getProductById(request.getProductId());
        
        // Lazy cart creation
        Cart cart = cartRepository.findByUser(user)
            .orElseGet(() -> {
                log.info("Creating new cart for user: {}", userId);
                Cart newCart = new Cart();
                newCart.setUser(user);
                return cartRepository.save(newCart);
            });
        
        // Check if product already exists in cart
        Optional<CartItem> existingItem = cartItemRepository.findByCartAndProduct(cart, product);
        
        if (existingItem.isPresent()) {
            // Update quantity
            CartItem item = existingItem.get();
            item.setQuantity(item.getQuantity() + request.getQuantity());
            cartItemRepository.save(item);
            log.info("Updated quantity for existing cart item: {}", item.getId());
        } else {
            // Add new item
            CartItem newItem = new CartItem();
            newItem.setCart(cart);
            newItem.setProduct(product);
            newItem.setQuantity(request.getQuantity());
            cartItemRepository.save(newItem);
            log.info("Added new item to cart: {}", newItem.getId());
        }
        
        // Refresh cart to get updated items
        cart = cartRepository.findById(cart.getId()).orElseThrow();
        return mapToCartResponse(cart);
    }
    
    @Transactional
    public CartResponse updateCartItem(UUID userId, UUID itemId, UpdateCartItemRequest request) {
        log.info("Updating cart item {} for user {}", itemId, userId);
        
        User user = userService.getUserById(userId);
        Cart cart = cartRepository.findByUser(user)
            .orElseThrow(() -> {
                log.warn("Cart not found for user: {}", userId);
                return new ResourceNotFoundException("Cart not found");
            });
        
        CartItem item = cartItemRepository.findByIdAndCartId(itemId, cart.getId())
            .orElseThrow(() -> {
                log.warn("Cart item not found: {}", itemId);
                return new ResourceNotFoundException("Cart item not found");
            });
        
        item.setQuantity(request.getQuantity());
        cartItemRepository.save(item);
        log.info("Cart item updated: {}", itemId);
        
        // Refresh cart
        cart = cartRepository.findById(cart.getId()).orElseThrow();
        return mapToCartResponse(cart);
    }
    
    @Transactional
    public void removeCartItem(UUID userId, UUID itemId) {
        log.info("Removing cart item {} for user {}", itemId, userId);
        
        User user = userService.getUserById(userId);
        Cart cart = cartRepository.findByUser(user)
            .orElseThrow(() -> {
                log.warn("Cart not found for user: {}", userId);
                return new ResourceNotFoundException("Cart not found");
            });
        
        CartItem item = cartItemRepository.findByIdAndCartId(itemId, cart.getId())
            .orElseThrow(() -> {
                log.warn("Cart item not found: {}", itemId);
                return new ResourceNotFoundException("Cart item not found");
            });
        
        cartItemRepository.delete(item);
        log.info("Cart item removed: {}", itemId);
        
        // Auto-delete cart if empty
        cart = cartRepository.findById(cart.getId()).orElseThrow();
        if (cart.getItems().isEmpty()) {
            cartRepository.delete(cart);
            log.info("Cart deleted as it became empty: {}", cart.getId());
        }
    }
    
    @Transactional
    public void clearCartOnLogout(UUID userId) {
        log.info("Clearing cart on logout for user: {}", userId);
        
        User user = userService.getUserById(userId);
        Optional<Cart> cartOpt = cartRepository.findByUser(user);
        
        if (cartOpt.isPresent()) {
            cartRepository.delete(cartOpt.get());
            log.info("Cart deleted on logout for user: {}", userId);
        } else {
            log.info("No cart found to delete for user: {}", userId);
        }
    }
    
    private CartResponse mapToCartResponse(Cart cart) {
        List<CartItemResponse> itemResponses = cart.getItems().stream()
            .map(this::mapToCartItemResponse)
            .collect(Collectors.toList());
        
        BigDecimal grandTotal = itemResponses.stream()
            .map(CartItemResponse::getItemTotal)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        return CartResponse.builder()
            .id(cart.getId())
            .userId(cart.getUser().getId())
            .createdAt(cart.getCreatedAt())
            .items(itemResponses)
            .grandTotal(grandTotal)
            .totalItems(itemResponses.size())
            .build();
    }
    
    private CartItemResponse mapToCartItemResponse(CartItem item) {
        BigDecimal itemTotal = item.getProduct().getPrice()
            .multiply(BigDecimal.valueOf(item.getQuantity()));
        
        return CartItemResponse.builder()
            .id(item.getId())
            .productId(item.getProduct().getId())
            .productName(item.getProduct().getName())
            .productPrice(item.getProduct().getPrice())
            .quantity(item.getQuantity())
            .itemTotal(itemTotal)
            .build();
    }
}