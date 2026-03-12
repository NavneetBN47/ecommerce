package com.ecommerce.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Cart Entity
 * Represents a user's shopping cart (1:1 with User)
 * Lazy creation: Created only when first product is added
 * Ephemeral: Deleted when empty or on logout
 */
@Entity
@Table(name = "cart")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Cart {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "cart_id")
    private Long cartId;

    @OneToOne
    @JoinColumn(name = "user_id", unique = true, nullable = false)
    private User user;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "cart", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CartItem> cartItems = new ArrayList<>();

    /**
     * Add item to cart
     */
    public void addItem(CartItem item) {
        cartItems.add(item);
        item.setCart(this);
    }

    /**
     * Remove item from cart
     */
    public void removeItem(CartItem item) {
        cartItems.remove(item);
        item.setCart(null);
    }

    /**
     * Check if cart is empty
     */
    public boolean isEmpty() {
        return cartItems == null || cartItems.isEmpty();
    }
}