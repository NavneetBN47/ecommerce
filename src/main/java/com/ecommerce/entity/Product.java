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
 * Product Entity representing items in the product catalog
 * 
 * Business Rules:
 * - Price must be positive
 * - Stock quantity cannot be negative
 * - Soft delete using isActive flag
 * - Optimistic locking with version field
 * - Full-text search on name and description
 */
@Entity
@Table(name = "products", indexes = {
    @Index(name = "idx_category", columnList = "category"),
    @Index(name = "idx_name", columnList = "name"),
    @Index(name = "idx_price", columnList = "price"),
    @Index(name = "idx_stock", columnList = "stock_quantity"),
    @Index(name = "idx_active", columnList = "is_active")
})
@EntityListeners(AuditingEntityListener.class)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "product_id")
    private Long productId;

    @Column(name = "name", nullable = false, length = 200)
    private String name;

    @Column(name = "description", nullable = false, columnDefinition = "TEXT")
    private String description;

    @Column(name = "price", nullable = false, precision = 12, scale = 2)
    private BigDecimal price;

    @Column(name = "stock_quantity", nullable = false)
    @Builder.Default
    private Integer stockQuantity = 0;

    @Column(name = "category", nullable = false, length = 50)
    private String category;

    @Column(name = "image_url", length = 500)
    private String imageUrl;

    @Column(name = "is_active")
    @Builder.Default
    private Boolean isActive = true;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Version
    @Column(name = "version")
    @Builder.Default
    private Integer version = 0;

    /**
     * Check if product is in stock
     */
    public boolean isInStock() {
        return isActive && stockQuantity > 0;
    }

    /**
     * Check if sufficient stock is available
     */
    public boolean hasStock(Integer quantity) {
        return isActive && stockQuantity >= quantity;
    }

    /**
     * Deduct stock quantity (with validation)
     */
    public void deductStock(Integer quantity) {
        if (!hasStock(quantity)) {
            throw new IllegalStateException(
                String.format("Insufficient stock for product %s. Available: %d, Requested: %d",
                    name, stockQuantity, quantity)
            );
        }
        this.stockQuantity -= quantity;
    }

    /**
     * Restore stock quantity (for order cancellation)
     */
    public void restoreStock(Integer quantity) {
        this.stockQuantity += quantity;
    }

    /**
     * Soft delete product
     */
    public void softDelete() {
        this.isActive = false;
    }
}