package com.ecommerce.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Cart Entity - Maps to carts table
 * Represents user shopping carts
 */
@Entity
@Table(name = "carts")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Cart {
    
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "cart_id", updatable = false, nullable = false)
    private UUID id;
    
    @Column(name = "user_id", nullable = false, unique = true)
    private UUID userId;
    
    @Column(name = "session_id")
    private String sessionId;
    
    @Column(name = "status", nullable = false, length = 20)
    private String status = "active";
    
    @Column(name = "total_amount", precision = 12, scale = 2)
    private BigDecimal totalAmount = BigDecimal.ZERO;
    
    @Column(name = "item_count")
    private Integer itemCount = 0;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @Column(name = "expires_at")
    private LocalDateTime expiresAt;
    
    @OneToMany(mappedBy = "cart", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<CartItem> items = new ArrayList<>();
    
    /**
     * Add item to cart
     */
    public void addItem(CartItem item) {
        items.add(item);
        item.setCart(this);
    }
    
    /**
     * Remove item from cart
     */
    public void removeItem(CartItem item) {
        items.remove(item);
        item.setCart(null);
    }
}