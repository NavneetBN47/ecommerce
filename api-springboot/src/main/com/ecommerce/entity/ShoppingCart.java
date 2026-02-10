package com.ecommerce.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * ShoppingCart Entity representing shopping_carts table
 * Manages user shopping cart with lazy creation and auto-deletion
 */
@Entity
@Table(name = "shopping_carts", indexes = {
    @Index(name = "idx_user_id", columnList = "user_id")
})
@EntityListeners(AuditingEntityListener.class)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ShoppingCart {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "cart_id")
    private Long cartId;

    @NotNull(message = "User ID is required")
    @Column(name = "user_id", nullable = false)
    private Long userId;

    @OneToMany(mappedBy = "cart", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<CartItem> cartItems = new ArrayList<>();

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (updatedAt == null) {
            updatedAt = LocalDateTime.now();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    /**
     * Check if cart is empty
     */
    public boolean isEmpty() {
        return cartItems == null || cartItems.isEmpty();
    }

    /**
     * Add cart item
     */
    public void addItem(CartItem item) {
        if (cartItems == null) {
            cartItems = new ArrayList<>();
        }
        cartItems.add(item);
        item.setCart(this);
    }

    /**
     * Remove cart item
     */
    public void removeItem(CartItem item) {
        if (cartItems != null) {
            cartItems.remove(item);
            item.setCart(null);
        }
    }

    /**
     * Clear all items
     */
    public void clearItems() {
        if (cartItems != null) {
            cartItems.clear();
        }
    }
}