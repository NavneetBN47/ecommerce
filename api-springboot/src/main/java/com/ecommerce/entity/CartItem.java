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
 * CartItem Entity - Individual items in a shopping cart
 */
@Entity
@Table(name = "cart_items", 
    uniqueConstraints = @UniqueConstraint(name = "uk_cart_product", columnNames = {"cart_id", "product_id"}),
    indexes = {
        @Index(name = "idx_cart_item_cart", columnList = "cart_id"),
        @Index(name = "idx_cart_item_product", columnList = "product_id")
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
    @JoinColumn(name = "cart_id", nullable = false, 
                foreignKey = @ForeignKey(name = "fk_cart_item_cart"))
    private Cart cart;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false, 
                foreignKey = @ForeignKey(name = "fk_cart_item_product"))
    private Product product;

    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    @Column(name = "price", nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @CreatedDate
    @Column(name = "added_at", nullable = false, updatable = false)
    private LocalDateTime addedAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /**
     * Calculate subtotal for this item
     */
    public BigDecimal getSubtotal() {
        return price.multiply(BigDecimal.valueOf(quantity));
    }
}