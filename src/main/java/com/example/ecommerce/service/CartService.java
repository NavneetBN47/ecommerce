package com.example.ecommerce.service;

import com.example.ecommerce.dto.AddToCartRequest;
import com.example.ecommerce.dto.CartItemResponse;
import com.example.ecommerce.dto.CartResponse;
import com.example.ecommerce.dto.UpdateCartItemRequest;
import com.example.ecommerce.entity.Cart;
import com.example.ecommerce.entity.CartItem;
import com.example.ecommerce.entity.Product;
import com.example.ecommerce.entity.User;
import com.example.ecommerce.exception.BadRequestException;
import com.example.ecommerce.exception.ResourceNotFoundException;
import com.example.ecommerce.repository.CartItemRepository;
import com.example.ecommerce.repository.CartRepository;
import com.example.ecommerce.repository.ProductRepository;
import com.example.ecommerce.repository.UserRepository;
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

    @Transactional(readOnly = true)
    public CartResponse getCart(UUID userId) {
        log.info("Fetching cart for user ID: {}", userId);
        
        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> {
                    log.error("Cart not found for user ID: {}", userId);
                    return new ResourceNotFoundException("Cart not found");
                });

        return mapToCartResponse(cart);
    }

    @Transactional
    public CartResponse addProductToCart(UUID userId, AddToCartRequest request) {
        log.info("Adding product {} to cart for user ID: {}", request.getProductId(), userId);
        
        // Validate user exists
        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.error("User not found with ID: {}", userId);
                    return new ResourceNotFoundException("User not found");
                });

        // Validate product exists
        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> {
                    log.error("Product not found with ID: {}", request.getProductId());
                    return new ResourceNotFoundException("Product not found");
                });

        // Validate quantity
        if (request.getQuantity() <= 0) {
            log.error("Invalid quantity: {}", request.getQuantity());
            throw new BadRequestException("Quantity must be greater than 0");
        }

        // Find or create cart (lazy creation)
        Cart cart = cartRepository.findByUserId(userId)
                .orElseGet(() -> {
                    log.info("Creating new cart for user ID: {}", userId);
                    Cart newCart = new Cart();
                    newCart.setUser(user);
                    return cartRepository.save(newCart);
                });

        // Check if product already in cart
        CartItem existingItem = cartItemRepository.findByCartIdAndProductId(cart.getId(), product.getId())
                .orElse(null);

        if (existingItem != null) {
            // Update existing item quantity
            existingItem.setQuantity(existingItem.getQuantity() + request.getQuantity());
            cartItemRepository.save(existingItem);
            log.info("Updated existing cart item quantity to: {}", existingItem.getQuantity());
        } else {
            // Add new item
            CartItem newItem = new CartItem();
            newItem.setCart(cart);
            newItem.setProduct(product);
            newItem.setQuantity(request.getQuantity());
            cart.addItem(newItem);
            cartItemRepository.save(newItem);
            log.info("Added new product to cart");
        }

        return mapToCartResponse(cart);
    }

    @Transactional
    public CartResponse updateCartItemQuantity(UUID userId, UUID cartItemId, UpdateCartItemRequest request) {
        log.info("Updating cart item {} quantity for user ID: {}", cartItemId, userId);
        
        // Validate quantity
        if (request.getQuantity() <= 0) {
            log.error("Invalid quantity: {}", request.getQuantity());
            throw new BadRequestException("Quantity must be greater than 0");
        }

        // Find cart
        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> {
                    log.error("Cart not found for user ID: {}", userId);
                    return new ResourceNotFoundException("Cart not found");
                });

        // Find cart item
        CartItem cartItem = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> {
                    log.error("Cart item not found with ID: {}", cartItemId);
                    return new ResourceNotFoundException("Cart item not found");
                });

        // Validate cart item belongs to user's cart
        if (!cartItem.getCart().getId().equals(cart.getId())) {
            log.error("Cart item {} does not belong to user's cart", cartItemId);
            throw new BadRequestException("Cart item does not belong to your cart");
        }

        cartItem.setQuantity(request.getQuantity());
        cartItemRepository.save(cartItem);
        log.info("Cart item quantity updated to: {}", request.getQuantity());

        return mapToCartResponse(cart);
    }

    @Transactional
    public void removeCartItem(UUID userId, UUID cartItemId) {
        log.info("Removing cart item {} for user ID: {}", cartItemId, userId);
        
        // Find cart
        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> {
                    log.error("Cart not found for user ID: {}", userId);
                    return new ResourceNotFoundException("Cart not found");
                });

        // Find cart item
        CartItem cartItem = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> {
                    log.error("Cart item not found with ID: {}", cartItemId);
                    return new ResourceNotFoundException("Cart item not found");
                });

        // Validate cart item belongs to user's cart
        if (!cartItem.getCart().getId().equals(cart.getId())) {
            log.error("Cart item {} does not belong to user's cart", cartItemId);
            throw new BadRequestException("Cart item does not belong to your cart");
        }

        cart.removeItem(cartItem);
        cartItemRepository.delete(cartItem);
        log.info("Cart item removed successfully");

        // Auto-delete cart if empty
        long itemCount = cartItemRepository.countByCartId(cart.getId());
        if (itemCount == 0) {
            log.info("Cart is empty, deleting cart for user ID: {}", userId);
            cartRepository.delete(cart);
        }
    }

    @Transactional
    public void clearCartOnLogout(UUID userId) {
        log.info("Clearing cart on logout for user ID: {}", userId);
        
        cartRepository.findByUserId(userId).ifPresent(cart -> {
            log.info("Deleting cart and items for user ID: {}", userId);
            cartRepository.delete(cart);
        });
    }

    private CartResponse mapToCartResponse(Cart cart) {
        List<CartItemResponse> itemResponses = cart.getItems().stream()
                .map(this::mapToCartItemResponse)
                .collect(Collectors.toList());

        BigDecimal grandTotal = itemResponses.stream()
                .map(CartItemResponse::getItemTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new CartResponse(
                cart.getId(),
                cart.getUser().getId(),
                cart.getCreatedAt(),
                itemResponses,
                grandTotal,
                itemResponses.size()
        );
    }

    private CartItemResponse mapToCartItemResponse(CartItem item) {
        BigDecimal itemTotal = item.getProduct().getPrice()
                .multiply(BigDecimal.valueOf(item.getQuantity()));

        return new CartItemResponse(
                item.getId(),
                item.getProduct().getId(),
                item.getProduct().getName(),
                item.getProduct().getPrice(),
                item.getQuantity(),
                itemTotal
        );
    }
}