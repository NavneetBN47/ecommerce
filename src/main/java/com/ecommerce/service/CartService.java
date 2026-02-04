package com.ecommerce.service;

import com.ecommerce.dto.AddToCartRequest;
import com.ecommerce.dto.CartItemResponse;
import com.ecommerce.dto.CartResponse;
import com.ecommerce.dto.ProductResponse;
import com.ecommerce.entity.Cart;
import com.ecommerce.entity.CartItem;
import com.ecommerce.entity.Product;
import com.ecommerce.entity.User;
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
    private final UserService userService;
    private final ProductService productService;

    @Transactional(readOnly = true)
    public CartResponse getCart(UUID userId) {
        log.info("Fetching cart for user ID: {}", userId);
        
        User user = userService.getUserById(userId);
        Cart cart = cartRepository.findByUser(user)
                .orElseThrow(() -> {
                    log.error("Cart not found for user: {}", userId);
                    return new ResourceNotFoundException("Cart not found");
                });

        return mapToCartResponse(cart);
    }

    @Transactional
    public CartResponse addProductToCart(UUID userId, AddToCartRequest request) {
        log.info("Adding product {} to cart for user {}", request.getProductId(), userId);
        
        User user = userService.getUserById(userId);
        Product product = productService.getProductById(request.getProductId());

        if (request.getQuantity() <= 0) {
            log.error("Invalid quantity: {}", request.getQuantity());
            throw new InvalidOperationException("Quantity must be greater than 0");
        }

        // Lazy cart creation
        Cart cart = cartRepository.findByUser(user)
                .orElseGet(() -> {
                    log.info("Creating new cart for user: {}", userId);
                    Cart newCart = new Cart();
                    newCart.setUser(user);
                    return cartRepository.save(newCart);
                });

        // Check if product already exists in cart
        CartItem cartItem = cartItemRepository.findByCartAndProduct(cart, product)
                .orElse(null);

        if (cartItem != null) {
            // Update existing item quantity
            cartItem.setQuantity(cartItem.getQuantity() + request.getQuantity());
            log.info("Updated cart item quantity to: {}", cartItem.getQuantity());
        } else {
            // Add new item
            cartItem = new CartItem();
            cartItem.setCart(cart);
            cartItem.setProduct(product);
            cartItem.setQuantity(request.getQuantity());
            cart.addItem(cartItem);
            log.info("Added new cart item");
        }

        cartItemRepository.save(cartItem);
        log.info("Product added to cart successfully");

        return mapToCartResponse(cart);
    }

    @Transactional
    public CartResponse updateCartItemQuantity(UUID userId, UUID itemId, Integer quantity) {
        log.info("Updating cart item {} quantity to {} for user {}", itemId, quantity, userId);
        
        User user = userService.getUserById(userId);
        Cart cart = cartRepository.findByUser(user)
                .orElseThrow(() -> {
                    log.error("Cart not found for user: {}", userId);
                    return new ResourceNotFoundException("Cart not found");
                });

        if (quantity <= 0) {
            log.error("Invalid quantity: {}", quantity);
            throw new InvalidOperationException("Quantity must be greater than 0");
        }

        CartItem cartItem = cartItemRepository.findByIdAndCartId(itemId, cart.getId())
                .orElseThrow(() -> {
                    log.error("Cart item not found: {}", itemId);
                    return new ResourceNotFoundException("Cart item not found");
                });

        cartItem.setQuantity(quantity);
        cartItemRepository.save(cartItem);
        log.info("Cart item quantity updated successfully");

        return mapToCartResponse(cart);
    }

    @Transactional
    public void removeCartItem(UUID userId, UUID itemId) {
        log.info("Removing cart item {} for user {}", itemId, userId);
        
        User user = userService.getUserById(userId);
        Cart cart = cartRepository.findByUser(user)
                .orElseThrow(() -> {
                    log.error("Cart not found for user: {}", userId);
                    return new ResourceNotFoundException("Cart not found");
                });

        CartItem cartItem = cartItemRepository.findByIdAndCartId(itemId, cart.getId())
                .orElseThrow(() -> {
                    log.error("Cart item not found: {}", itemId);
                    return new ResourceNotFoundException("Cart item not found");
                });

        cart.removeItem(cartItem);
        cartItemRepository.delete(cartItem);
        log.info("Cart item removed successfully");

        // Auto-delete empty cart
        if (cart.getItems().isEmpty()) {
            log.info("Cart is empty, deleting cart for user: {}", userId);
            cartRepository.delete(cart);
        }
    }

    @Transactional
    public void clearCartOnLogout(UUID userId) {
        log.info("Clearing cart on logout for user: {}", userId);
        
        User user = userService.getUserById(userId);
        cartRepository.findByUser(user).ifPresent(cart -> {
            log.info("Deleting cart and items for user: {}", userId);
            cartRepository.delete(cart);
        });
    }

    private CartResponse mapToCartResponse(Cart cart) {
        List<CartItemResponse> items = cart.getItems().stream()
                .map(this::mapToCartItemResponse)
                .collect(Collectors.toList());

        BigDecimal grandTotal = items.stream()
                .map(CartItemResponse::getItemTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return CartResponse.builder()
                .id(cart.getId())
                .userId(cart.getUser().getId())
                .items(items)
                .grandTotal(grandTotal)
                .createdAt(cart.getCreatedAt())
                .build();
    }

    private CartItemResponse mapToCartItemResponse(CartItem cartItem) {
        Product product = cartItem.getProduct();
        BigDecimal itemTotal = product.getPrice().multiply(BigDecimal.valueOf(cartItem.getQuantity()));

        ProductResponse productResponse = ProductResponse.builder()
                .id(product.getId())
                .name(product.getName())
                .description(product.getDescription())
                .price(product.getPrice())
                .availableQty(product.getAvailableQty())
                .build();

        return CartItemResponse.builder()
                .id(cartItem.getId())
                .product(productResponse)
                .quantity(cartItem.getQuantity())
                .itemTotal(itemTotal)
                .build();
    }
}