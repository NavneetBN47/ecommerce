package com.ecommerce.service;

import com.ecommerce.dto.AddToCartRequestDTO;
import com.ecommerce.dto.CartDTO;
import com.ecommerce.dto.CartItemDTO;
import com.ecommerce.dto.UpdateCartItemRequestDTO;
import com.ecommerce.entity.Cart;
import com.ecommerce.entity.CartItem;
import com.ecommerce.entity.Product;
import com.ecommerce.entity.User;
import com.ecommerce.exception.BusinessRuleException;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Cart Service
 * Implements lazy cart creation and ephemeral cart lifecycle
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;

    /**
     * Add product to cart
     * Implements lazy cart creation
     */
    @Transactional
    public CartDTO addToCart(AddToCartRequestDTO request) {
        log.info("Adding product {} to cart for user {}", request.getProductId(), request.getUserId());
        
        // Validate user
        User user = userRepository.findById(request.getUserId())
            .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + request.getUserId()));
        
        // Validate product
        Product product = productRepository.findById(request.getProductId())
            .orElseThrow(() -> new ResourceNotFoundException("Product not found with ID: " + request.getProductId()));
        
        // Check product availability
        if (product.getAvailableQty() < request.getQuantity()) {
            throw new BusinessRuleException("Insufficient quantity available. Available: " + product.getAvailableQty());
        }
        
        // Lazy cart creation: Create cart only if it doesn't exist
        Cart cart = cartRepository.findByUserUserId(request.getUserId())
            .orElseGet(() -> {
                log.info("Creating new cart for user: {}", request.getUserId());
                Cart newCart = new Cart();
                newCart.setUser(user);
                return cartRepository.save(newCart);
            });
        
        // Check if product already in cart
        Optional<CartItem> existingItem = cartItemRepository.findByCartIdAndProductId(
            cart.getCartId(), 
            request.getProductId()
        );
        
        if (existingItem.isPresent()) {
            // Update quantity
            CartItem item = existingItem.get();
            int newQuantity = item.getQuantity() + request.getQuantity();
            
            if (product.getAvailableQty() < newQuantity) {
                throw new BusinessRuleException("Insufficient quantity available. Available: " + product.getAvailableQty());
            }
            
            item.setQuantity(newQuantity);
            cartItemRepository.save(item);
            log.info("Updated quantity for product {} in cart", request.getProductId());
        } else {
            // Add new item
            CartItem newItem = new CartItem();
            newItem.setCart(cart);
            newItem.setProduct(product);
            newItem.setQuantity(request.getQuantity());
            cartItemRepository.save(newItem);
            log.info("Added new product {} to cart", request.getProductId());
        }
        
        return getCartByUserId(request.getUserId());
    }

    /**
     * Update cart item quantity
     */
    @Transactional
    public CartDTO updateCartItem(UpdateCartItemRequestDTO request) {
        log.info("Updating cart item for user {} and product {}", request.getUserId(), request.getProductId());
        
        Cart cart = cartRepository.findByUserUserId(request.getUserId())
            .orElseThrow(() -> new ResourceNotFoundException("Cart not found for user: " + request.getUserId()));
        
        CartItem cartItem = cartItemRepository.findByCartIdAndProductId(
            cart.getCartId(), 
            request.getProductId()
        ).orElseThrow(() -> new ResourceNotFoundException("Product not found in cart"));
        
        Product product = cartItem.getProduct();
        
        if (product.getAvailableQty() < request.getQuantity()) {
            throw new BusinessRuleException("Insufficient quantity available. Available: " + product.getAvailableQty());
        }
        
        cartItem.setQuantity(request.getQuantity());
        cartItemRepository.save(cartItem);
        
        log.info("Cart item updated successfully");
        return getCartByUserId(request.getUserId());
    }

    /**
     * Remove product from cart
     * Auto-deletes cart if empty
     */
    @Transactional
    public CartDTO removeFromCart(Long userId, Long productId) {
        log.info("Removing product {} from cart for user {}", productId, userId);
        
        Cart cart = cartRepository.findByUserUserId(userId)
            .orElseThrow(() -> new ResourceNotFoundException("Cart not found for user: " + userId));
        
        cartItemRepository.deleteByCartIdAndProductId(cart.getCartId(), productId);
        
        // Check if cart is empty and delete if so
        Long itemCount = cartItemRepository.countByCartId(cart.getCartId());
        if (itemCount == 0) {
            log.info("Cart is empty, deleting cart for user: {}", userId);
            cartRepository.delete(cart);
            return new CartDTO(null, userId, new ArrayList<>(), BigDecimal.ZERO, 0, null);
        }
        
        log.info("Product removed from cart successfully");
        return getCartByUserId(userId);
    }

    /**
     * View cart items
     */
    @Transactional(readOnly = true)
    public CartDTO getCartByUserId(Long userId) {
        log.info("Fetching cart for user: {}", userId);
        
        Optional<Cart> cartOpt = cartRepository.findByUserUserId(userId);
        
        if (cartOpt.isEmpty()) {
            log.info("No cart found for user: {}", userId);
            return new CartDTO(null, userId, new ArrayList<>(), BigDecimal.ZERO, 0, null);
        }
        
        Cart cart = cartOpt.get();
        
        List<CartItemDTO> items = cart.getCartItems().stream()
            .map(this::convertToCartItemDTO)
            .collect(Collectors.toList());
        
        BigDecimal cartTotal = items.stream()
            .map(CartItemDTO::getItemTotal)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        CartDTO cartDTO = new CartDTO();
        cartDTO.setCartId(cart.getCartId());
        cartDTO.setUserId(userId);
        cartDTO.setItems(items);
        cartDTO.setCartTotal(cartTotal);
        cartDTO.setItemCount(items.size());
        cartDTO.setCreatedAt(cart.getCreatedAt());
        
        log.info("Cart retrieved with {} items, total: {}", items.size(), cartTotal);
        return cartDTO;
    }

    /**
     * Clear cart on logout
     * Deletes cart and all cart items
     */
    @Transactional
    public void clearCartOnLogout(Long userId) {
        log.info("Clearing cart for user on logout: {}", userId);
        
        Optional<Cart> cartOpt = cartRepository.findByUserUserId(userId);
        
        if (cartOpt.isPresent()) {
            Cart cart = cartOpt.get();
            cartItemRepository.deleteAllByCartId(cart.getCartId());
            cartRepository.delete(cart);
            log.info("Cart cleared successfully for user: {}", userId);
        } else {
            log.info("No cart to clear for user: {}", userId);
        }
    }

    /**
     * Convert CartItem entity to DTO
     */
    private CartItemDTO convertToCartItemDTO(CartItem cartItem) {
        CartItemDTO dto = new CartItemDTO();
        dto.setCartItemId(cartItem.getCartItemId());
        dto.setProductId(cartItem.getProduct().getProductId());
        dto.setProductName(cartItem.getProduct().getProductName());
        dto.setPrice(cartItem.getProduct().getPrice());
        dto.setQuantity(cartItem.getQuantity());
        dto.setItemTotal(cartItem.getProduct().getPrice().multiply(BigDecimal.valueOf(cartItem.getQuantity())));
        return dto;
    }
}