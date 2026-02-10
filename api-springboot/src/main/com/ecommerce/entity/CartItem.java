package com.ecommerce.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * CartItem Entity representing cart_items table
 * Manages individual items in shopping cart
 */
@Entity
@Table(name = "cart_items", 
    uniqueConstraints = @UniqueConstraint(name = "unique_cart_product", columnNames = {"cart_id", "product_id"}),
    indexes = {
        @Index(name = "idx_cart_id", columnList = "cart_id"),
        @Index(name = "idx_product_id", columnList = "product_id")
    }
)
@EntityListeners(AuditingEntityListener.class)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CartItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "cart_item_id")
    private Long cartItemId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cart_id", nullable = false, foreignKey = @ForeignKey(name = "fk_cart_item_cart"))
    private ShoppingCart cart;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "product_id", nullable = false, foreignKey = @ForeignKey(name = "fk_cart_item_product"))
    private Product product;

    @NotNull(message = "Quantity is required")
    @Min(value = 1, message = "Quantity must be at least 1")
    @Column(name = "quantity", nullable = false)
    private Integer quantity = 1;

    @NotNull(message = "Price at addition is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Price must be greater than 0")
    @Digits(integer = 8, fraction = 2, message = "Price must have at most 8 integer digits and 2 decimal places")
    @Column(name = "price_at_addition", nullable = false, precision = 10, scale = 2)
    private BigDecimal priceAtAddition;

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
        if (quantity == null) {
            quantity = 1;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    /**
     * Calculate item subtotal
     */
    public BigDecimal getSubtotal() {
        return priceAtAddition.multiply(BigDecimal.valueOf(quantity));
    }
}