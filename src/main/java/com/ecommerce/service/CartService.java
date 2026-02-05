package com.ecommerce.service;

import com.ecommerce.dto.CartDTO;
import com.ecommerce.dto.CartItemDTO;
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

import java.util.List;
import java.util.stream.Collectors;

/**
 * Cart Service with lazy creation, auto-delete empty cart, and logout cleanup
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    /**
     * Get or create active cart for user (lazy creation)
     */
    @Transactional
    public CartDTO getOrCreateCart(Long userId) {
        log.info("Getting or creating cart for user: {}", userId);
        
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        Cart cart = cartRepository.findActiveCartByUserId(userId)
            .orElseGet(() -> {
                log.info("Creating new cart for user: {}", userId);
                Cart newCart = Cart.builder()
                    .user(user)
                    .status(Cart.CartStatus.ACTIVE)
                    .build();
                return cartRepository.save(newCart);
            });

        return convertToDTO(cart);
    }

    @Transactional(readOnly = true)
    public CartDTO getCartById(Long cartId) {
        log.info("Fetching cart by id: {}", cartId);
        Cart cart = cartRepository.findByIdWithItems(cartId)
            .orElseThrow(() -> new ResourceNotFoundException("Cart not found with id: " + cartId));
        return convertToDTO(cart);
    }

    /**
     * Add item to cart with quantity check
     */
    @Transactional
    public CartDTO addItemToCart(Long userId, CartItemDTO itemDTO) {
        log.info("Adding item to cart for user: {}, product: {}", userId, itemDTO.getProductId());
        
        Cart cart = cartRepository.findActiveCartByUserId(userId)
            .orElseGet(() -> {
                User user = userRepository.findById(userId)
                    .orElseThrow(() -> new ResourceNotFoundException("User not found"));
                Cart newCart = Cart.builder().user(user).status(Cart.CartStatus.ACTIVE).build();
                return cartRepository.save(newCart);
            });

        Product product = productRepository.findById(itemDTO.getProductId())
            .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + itemDTO.getProductId()));

        // Check stock availability
        if (!product.hasStock(itemDTO.getQuantity())) {
            throw new InsufficientStockException("Insufficient stock for product: " + product.getName());
        }

        // Check if item already exists in cart
        CartItem cartItem = cartItemRepository.findByCartIdAndProductId(cart.getId(), product.getId())
            .orElse(null);

        if (cartItem != null) {
            // Update quantity
            int newQuantity = cartItem.getQuantity() + itemDTO.getQuantity();
            if (!product.hasStock(newQuantity)) {
                throw new InsufficientStockException("Insufficient stock for product: " + product.getName());
            }
            cartItem.setQuantity(newQuantity);
            cartItem.calculateSubtotal();
        } else {
            // Create new cart item
            cartItem = CartItem.builder()
                .cart(cart)
                .product(product)
                .quantity(itemDTO.getQuantity())
                .price(product.getPrice())
                .build();
            cart.addItem(cartItem);
        }

        cartItemRepository.save(cartItem);
        cart.recalculateTotals();
        Cart savedCart = cartRepository.save(cart);
        
        log.info("Item added to cart successfully");
        return convertToDTO(savedCart);
    }

    /**
     * Update cart item quantity with stock check
     */
    @Transactional
    public CartDTO updateCartItem(Long cartId, Long itemId, Integer quantity) {
        log.info("Updating cart item: {}, new quantity: {}", itemId, quantity);
        
        CartItem cartItem = cartItemRepository.findById(itemId)
            .orElseThrow(() -> new ResourceNotFoundException("Cart item not found with id: " + itemId));

        if (!cartItem.getCart().getId().equals(cartId)) {
            throw new IllegalArgumentException("Cart item does not belong to specified cart");
        }

        Product product = cartItem.getProduct();
        if (!product.hasStock(quantity)) {
            throw new InsufficientStockException("Insufficient stock for product: " + product.getName());
        }

        cartItem.setQuantity(quantity);
        cartItem.calculateSubtotal();
        cartItemRepository.save(cartItem);

        Cart cart = cartItem.getCart();
        cart.recalculateTotals();
        Cart savedCart = cartRepository.save(cart);

        log.info("Cart item updated successfully");
        return convertToDTO(savedCart);
    }

    /**
     * Remove item from cart and auto-delete if empty
     */
    @Transactional
    public CartDTO removeItemFromCart(Long cartId, Long itemId) {
        log.info("Removing item from cart: {}", itemId);
        
        CartItem cartItem = cartItemRepository.findById(itemId)
            .orElseThrow(() -> new ResourceNotFoundException("Cart item not found with id: " + itemId));

        Cart cart = cartItem.getCart();
        cart.removeItem(cartItem);
        cartItemRepository.delete(cartItem);

        // Auto-delete cart if empty
        if (cart.isEmpty()) {
            log.info("Cart is empty, deleting cart: {}", cartId);
            cartRepository.delete(cart);
            return CartDTO.builder().id(cartId).build();
        }

        cart.recalculateTotals();
        Cart savedCart = cartRepository.save(cart);
        
        log.info("Item removed from cart successfully");
        return convertToDTO(savedCart);
    }

    /**
     * Clear cart and delete if empty
     */
    @Transactional
    public void clearCart(Long cartId) {
        log.info("Clearing cart: {}", cartId);
        
        Cart cart = cartRepository.findById(cartId)
            .orElseThrow(() -> new ResourceNotFoundException("Cart not found with id: " + cartId));

        cart.clearItems();
        cartItemRepository.deleteByCartId(cartId);
        
        // Auto-delete empty cart
        cartRepository.delete(cart);
        log.info("Cart cleared and deleted: {}", cartId);
    }

    /**
     * Cleanup empty carts on user logout
     */
    @Transactional
    public void cleanupEmptyCartsOnLogout(Long userId) {
        log.info("Cleaning up empty carts for user on logout: {}", userId);
        cartRepository.deleteEmptyCartsByUserId(userId);
        log.info("Empty carts cleaned up for user: {}", userId);
    }

    private CartDTO convertToDTO(Cart cart) {
        List<CartItemDTO> itemDTOs = cart.getItems().stream()
            .map(this::convertItemToDTO)
            .collect(Collectors.toList());

        return CartDTO.builder()
            .id(cart.getId())
            .userId(cart.getUser().getId())
            .items(itemDTOs)
            .status(cart.getStatus())
            .totalAmount(cart.getTotalAmount())
            .totalItems(cart.getTotalItems())
            .createdAt(cart.getCreatedAt())
            .updatedAt(cart.getUpdatedAt())
            .build();
    }

    private CartItemDTO convertItemToDTO(CartItem item) {
        return CartItemDTO.builder()
            .id(item.getId())
            .cartId(item.getCart().getId())
            .productId(item.getProduct().getId())
            .productName(item.getProduct().getName())
            .productSku(item.getProduct().getSku())
            .productImageUrl(item.getProduct().getImageUrl())
            .quantity(item.getQuantity())
            .price(item.getPrice())
            .subtotal(item.getSubtotal())
            .createdAt(item.getCreatedAt())
            .updatedAt(item.getUpdatedAt())
            .build();
    }
}