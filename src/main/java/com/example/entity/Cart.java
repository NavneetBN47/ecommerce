package com.example.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * Cart Entity representing user shopping carts
 * Implements lazy creation and auto-delete when empty
 */
@Entity
@Table(name = "carts", indexes = {
    @Index(name = "idx_cart_user", columnList = "user_id", unique = true)
})
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
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

    @OneToMany(mappedBy = "cart", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private Set<CartItem> items = new HashSet<>();

    /**
     * Add item to cart and recalculate totals
     */
    public void addItem(CartItem item) {
        items.add(item);
        item.setCart(this);
        recalculateTotals();
    }

    /**
     * Remove item from cart and recalculate totals
     */
    public void removeItem(CartItem item) {
        items.remove(item);
        item.setCart(null);
        recalculateTotals();
    }

    /**
     * Recalculate cart totals based on items
     */
    public void recalculateTotals() {
        this.totalAmount = items.stream()
            .map(CartItem::getSubtotal)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        this.totalItems = items.stream()
            .mapToInt(CartItem::getQuantity)
            .sum();
    }

    /**
     * Check if cart is empty
     */
    public boolean isEmpty() {
        return items == null || items.isEmpty();
    }

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
}