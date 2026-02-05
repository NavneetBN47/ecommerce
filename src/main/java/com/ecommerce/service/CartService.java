package com.ecommerce.service;

import com.ecommerce.dto.request.AddToCartRequest;
import com.ecommerce.dto.request.UpdateCartItemRequest;
import com.ecommerce.dto.response.CartItemResponse;
import com.ecommerce.dto.response.CartResponse;
import com.ecommerce.entity.CartItem;
import com.ecommerce.entity.Product;
import com.ecommerce.entity.ShoppingCart;
import com.ecommerce.entity.User;
import com.ecommerce.exception.BusinessException;
import com.ecommerce.exception.InsufficientStockException;
import com.ecommerce.exception.ResourceNotFoundException;
import com.ecommerce.repository.CartItemRepository;
import com.ecommerce.repository.ShoppingCartRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for shopping cart management operations
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CartService {

    private final ShoppingCartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductService productService;
    private final UserService userService;

    @Value("${app.cart.max-items:50}")
    private int maxCartItems;

    @Value("${app.cart.max-quantity-per-item:999}")
    private int maxQuantityPerItem;

    @Transactional
    public CartResponse addToCart(AddToCartRequest request) {
        User currentUser = userService.getCurrentUser();
        log.info("Adding product {} to cart for user {}", request.getProductId(), currentUser.getId());

        Product product = productService.getProductEntityById(request.getProductId());

        // Validate product availability
        if (!product.isInStock()) {
            throw new BusinessException("Product is out of stock");
        }

        if (!product.hasStock(request.getQuantity())) {
            throw new InsufficientStockException(
                product.getName(), request.getQuantity(), product.getStockQuantity());
        }

        // Get or create active cart
        ShoppingCart cart = cartRepository.findActiveCartByUserId(currentUser.getId())
            .orElseGet(() -> createNewCart(currentUser));

        // Check max items limit
        if (cart.getItems().size() >= maxCartItems) {
            throw new BusinessException("Cart has reached maximum capacity of " + maxCartItems + " items");
        }

        // Check if product already in cart
        CartItem existingItem = cartItemRepository
            .findByCartIdAndProductId(cart.getId(), product.getId())
            .orElse(null);

        if (existingItem != null) {
            // Update quantity
            int newQuantity = existingItem.getQuantity() + request.getQuantity();
            if (newQuantity > maxQuantityPerItem) {
                throw new BusinessException(
                    "Quantity exceeds maximum allowed per item: " + maxQuantityPerItem);
            }
            if (!product.hasStock(newQuantity)) {
                throw new InsufficientStockException(
                    product.getName(), newQuantity, product.getStockQuantity());
            }
            existingItem.setQuantity(newQuantity);
            cartItemRepository.save(existingItem);
        } else {
            // Add new item
            CartItem newItem = CartItem.builder()
                .cart(cart)
                .product(product)
                .quantity(request.getQuantity())
                .price(product.getPrice())
                .build();
            cartItemRepository.save(newItem);
            cart.addItem(newItem);
        }

        cart = cartRepository.save(cart);
        log.info("Product added to cart successfully");

        return mapToCartResponse(cart);
    }

    @Transactional(readOnly = true)
    public CartResponse getCart() {
        User currentUser = userService.getCurrentUser();
        log.info("Fetching cart for user {}", currentUser.getId());

        ShoppingCart cart = cartRepository.findActiveCartByUserIdWithItems(currentUser.getId())
            .orElseThrow(() -> new ResourceNotFoundException("Active cart not found"));

        return mapToCartResponse(cart);
    }

    @Transactional
    public CartResponse updateCartItem(Long itemId, UpdateCartItemRequest request) {
        User currentUser = userService.getCurrentUser();
        log.info("Updating cart item {} for user {}", itemId, currentUser.getId());

        CartItem cartItem = cartItemRepository.findById(itemId)
            .orElseThrow(() -> new ResourceNotFoundException("CartItem", "id", itemId));

        // Verify cart belongs to current user
        if (!cartItem.getCart().getUser().getId().equals(currentUser.getId())) {
            throw new BusinessException("Cart item does not belong to current user");
        }

        Product product = cartItem.getProduct();
        if (!product.hasStock(request.getQuantity())) {
            throw new InsufficientStockException(
                product.getName(), request.getQuantity(), product.getStockQuantity());
        }

        cartItem.setQuantity(request.getQuantity());
        cartItemRepository.save(cartItem);

        ShoppingCart cart = cartRepository.findById(cartItem.getCart().getId())
            .orElseThrow(() -> new ResourceNotFoundException("Cart not found"));

        log.info("Cart item updated successfully");
        return mapToCartResponse(cart);
    }

    @Transactional
    public void removeCartItem(Long itemId) {
        User currentUser = userService.getCurrentUser();
        log.info("Removing cart item {} for user {}", itemId, currentUser.getId());

        CartItem cartItem = cartItemRepository.findById(itemId)
            .orElseThrow(() -> new ResourceNotFoundException("CartItem", "id", itemId));

        if (!cartItem.getCart().getUser().getId().equals(currentUser.getId())) {
            throw new BusinessException("Cart item does not belong to current user");
        }

        ShoppingCart cart = cartItem.getCart();
        cart.removeItem(cartItem);
        cartItemRepository.delete(cartItem);

        // If cart is empty after removal, mark as inactive
        if (cart.isEmpty()) {
            cart.setStatus(ShoppingCart.CartStatus.INACTIVE);
            cartRepository.save(cart);
        }

        log.info("Cart item removed successfully");
    }

    @Transactional
    public void clearCart() {
        User currentUser = userService.getCurrentUser();
        log.info("Clearing cart for user {}", currentUser.getId());

        ShoppingCart cart = cartRepository.findActiveCartByUserId(currentUser.getId())
            .orElseThrow(() -> new ResourceNotFoundException("Active cart not found"));

        cart.clearItems();
        cartItemRepository.deleteByCartId(cart.getId());
        cart.setStatus(ShoppingCart.CartStatus.INACTIVE);
        cartRepository.save(cart);

        log.info("Cart cleared successfully");
    }

    @Transactional
    public void clearCartOnLogout(Long userId) {
        log.info("Clearing cart on logout for user {}", userId);
        cartRepository.findActiveCartByUserId(userId).ifPresent(cart -> {
            cart.clearItems();
            cartItemRepository.deleteByCartId(cart.getId());
            cart.setStatus(ShoppingCart.CartStatus.INACTIVE);
            cartRepository.save(cart);
            log.info("Cart cleared on logout for user {}", userId);
        });
    }

    private ShoppingCart createNewCart(User user) {
        log.info("Creating new cart for user {}", user.getId());
        ShoppingCart cart = ShoppingCart.builder()
            .user(user)
            .status(ShoppingCart.CartStatus.ACTIVE)
            .build();
        return cartRepository.save(cart);
    }

    private CartResponse mapToCartResponse(ShoppingCart cart) {
        List<CartItemResponse> items = cart.getItems().stream()
            .map(this::mapToCartItemResponse)
            .collect(Collectors.toList());

        return CartResponse.builder()
            .id(cart.getId())
            .userId(cart.getUser().getId())
            .status(cart.getStatus().name())
            .items(items)
            .total(cart.calculateTotal())
            .totalItems(cart.getTotalItems())
            .createdAt(cart.getCreatedAt())
            .updatedAt(cart.getUpdatedAt())
            .build();
    }

    private CartItemResponse mapToCartItemResponse(CartItem item) {
        return CartItemResponse.builder()
            .id(item.getId())
            .productId(item.getProduct().getId())
            .productName(item.getProduct().getName())
            .productSku(item.getProduct().getSku())
            .productImageUrl(item.getProduct().getImageUrl())
            .quantity(item.getQuantity())
            .price(item.getPrice())
            .subtotal(item.getSubtotal())
            .inStock(item.getProduct().isInStock())
            .addedAt(item.getCreatedAt())
            .build();
    }
}