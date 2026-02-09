package com.ecommerce.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.GenericGenerator;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * CartItem Entity - Represents an item in a shopping cart
 * 
 * Aligned with LLD specifications:
 * - id: UUID primary key
 * - cart_id: Foreign key to Cart
 * - product_id: Foreign key to Product
 * - quantity: Item quantity (must be > 0)
 * 
 * Business Rules:
 * - Quantity must be greater than 0
 * - One product can appear only once per cart (enforced by unique constraint)
 */
@Entity
@Table(name = "cart_items", 
    uniqueConstraints = {
        @UniqueConstraint(name = "cart_items_cart_product_unique", columnNames = {"cart_id", "product_id"})
    },
    indexes = {
        @Index(name = "idx_cart_items_cart_id", columnList = "cart_id"),
        @Index(name = "idx_cart_items_product_id", columnList = "product_id")
    }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CartItem {

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(name = "cart_item_id", updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cart_id", nullable = false)
    private Cart cart;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    @Column(name = "unit_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal unitPrice;

    /**
     * Validates that quantity is positive
     */
    @PrePersist
    @PreUpdate
    protected void validateQuantity() {
        if (quantity == null || quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than 0");
        }
    }

    /**
     * Calculate total price for this cart item
     */
    public BigDecimal getTotalPrice() {
        return unitPrice.multiply(BigDecimal.valueOf(quantity));
    }
}