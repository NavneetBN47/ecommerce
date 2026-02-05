package com.ecommerce.entity;

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
 * Cart Entity representing user shopping carts
 * 
 * Business Rules:
 * - One ACTIVE cart per user at any time
 * - Cart status: ACTIVE or CHECKED_OUT
 * - CHECKED_OUT carts are historical records
 * - Abandoned carts (30+ days inactive) are auto-cleared
 * - Cart persists across sessions until checkout
 */
@Entity
@Table(name = "carts", indexes = {
    @Index(name = "idx_user_status", columnList = "user_id, status"),
    @Index(name = "idx_updated", columnList = "updated_at")
})
@EntityListeners(AuditingEntityListener.class)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Cart {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "cart_id")
    private Long cartId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", insertable = false, updatable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private CartStatus status = CartStatus.ACTIVE;

    @OneToMany(mappedBy = "cart", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<CartItem> items = new ArrayList<>();

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /**
     * Calculate total amount of all items in cart
     */
    public BigDecimal getTotalAmount() {
        return items.stream()
            .map(CartItem::getSubtotal)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * Get total number of items in cart
     */
    public Integer getItemCount() {
        return items.stream()
            .mapToInt(CartItem::getQuantity)
            .sum();
    }

    /**
     * Add item to cart or update quantity if already exists
     */
    public void addItem(CartItem item) {
        CartItem existingItem = items.stream()
            .filter(i -> i.getProductId().equals(item.getProductId()))
            .findFirst()
            .orElse(null);

        if (existingItem != null) {
            existingItem.setQuantity(existingItem.getQuantity() + item.getQuantity());
        } else {
            item.setCart(this);
            items.add(item);
        }
    }

    /**
     * Remove item from cart
     */
    public void removeItem(CartItem item) {
        items.remove(item);
        item.setCart(null);
    }

    /**
     * Clear all items from cart
     */
    public void clearItems() {
        items.clear();
    }

    /**
     * Mark cart as checked out
     */
    public void checkout() {
        this.status = CartStatus.CHECKED_OUT;
    }

    /**
     * Check if cart is active
     */
    public boolean isActive() {
        return status == CartStatus.ACTIVE;
    }

    /**
     * Check if cart is empty
     */
    public boolean isEmpty() {
        return items.isEmpty();
    }

    /**
     * Check if cart is abandoned (inactive for 30+ days)
     */
    public boolean isAbandoned(int abandonedDays) {
        return updatedAt.isBefore(LocalDateTime.now().minusDays(abandonedDays));
    }

    public enum CartStatus {
        ACTIVE, CHECKED_OUT
    }
}