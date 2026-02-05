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

/**
 * CartItem Entity representing individual items in a shopping cart
 * 
 * Business Rules:
 * - One product per cart (enforced by unique constraint)
 * - Quantity range: 1-99
 * - Price captured at time of addition (historical)
 * - Cascade delete with cart
 * - Cannot delete product if in cart (RESTRICT)
 */
@Entity
@Table(name = "cart_items",
    uniqueConstraints = @UniqueConstraint(name = "uk_cart_product", columnNames = {"cart_id", "product_id"}),
    indexes = {
        @Index(name = "idx_cart", columnList = "cart_id"),
        @Index(name = "idx_product", columnList = "product_id")
    }
)
@EntityListeners(AuditingEntityListener.class)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CartItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "cart_item_id")
    private Long cartItemId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cart_id", nullable = false)
    private Cart cart;

    @Column(name = "cart_id", insertable = false, updatable = false)
    private Long cartId;

    @Column(name = "product_id", nullable = false)
    private Long productId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", insertable = false, updatable = false)
    private Product product;

    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    @Column(name = "price_at_addition", nullable = false, precision = 12, scale = 2)
    private BigDecimal priceAtAddition;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /**
     * Calculate subtotal (price * quantity)
     */
    public BigDecimal getSubtotal() {
        return priceAtAddition.multiply(BigDecimal.valueOf(quantity));
    }

    /**
     * Validate quantity is within allowed range (1-99)
     */
    public void validateQuantity() {
        if (quantity < 1 || quantity > 99) {
            throw new IllegalArgumentException(
                String.format("Quantity must be between 1 and 99. Provided: %d", quantity)
            );
        }
    }

    /**
     * Update quantity with validation
     */
    public void updateQuantity(Integer newQuantity) {
        if (newQuantity < 1 || newQuantity > 99) {
            throw new IllegalArgumentException(
                String.format("Quantity must be between 1 and 99. Provided: %d", newQuantity)
            );
        }
        this.quantity = newQuantity;
    }
}