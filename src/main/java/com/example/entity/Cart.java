package com.example.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Cart entity representing a user's shopping cart.
 * Implements lazy creation and auto-delete when empty.
 */
@Entity
@Table(name = "carts", indexes = {
    @Index(name = "idx_cart_user", columnList = "user_id", unique = true)
})
@EntityListeners(AuditingEntityListener.class)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Cart {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;
    
    @OneToMany(mappedBy = "cart", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<CartItem> items = new ArrayList<>();
    
    @Column(name = "total_amount", precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal totalAmount = BigDecimal.ZERO;
    
    @Column(name = "total_items")
    @Builder.Default
    private Integer totalItems = 0;
    
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    /**
     * Add item to cart or update quantity if already exists.
     */
    public void addItem(CartItem item) {
        if (items == null) {
            items = new ArrayList<>();
        }
        
        // Check if product already exists in cart
        CartItem existingItem = items.stream()
            .filter(i -> i.getProduct().getId().equals(item.getProduct().getId()))
            .findFirst()
            .orElse(null);
        
        if (existingItem != null) {
            existingItem.setQuantity(existingItem.getQuantity() + item.getQuantity());
            existingItem.updateSubtotal();
        } else {
            items.add(item);
            item.setCart(this);
        }
        
        recalculateTotals();
    }
    
    /**
     * Remove item from cart.
     */
    public void removeItem(CartItem item) {
        items.remove(item);
        item.setCart(null);
        recalculateTotals();
    }
    
    /**
     * Clear all items from cart.
     */
    public void clearItems() {
        items.clear();
        recalculateTotals();
    }
    
    /**
     * Recalculate cart totals.
     */
    public void recalculateTotals() {
        if (items == null || items.isEmpty()) {
            totalAmount = BigDecimal.ZERO;
            totalItems = 0;
        } else {
            totalAmount = items.stream()
                .map(CartItem::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
            totalItems = items.stream()
                .mapToInt(CartItem::getQuantity)
                .sum();
        }
    }
    
    /**
     * Check if cart is empty.
     */
    public boolean isEmpty() {
        return items == null || items.isEmpty();
    }
}