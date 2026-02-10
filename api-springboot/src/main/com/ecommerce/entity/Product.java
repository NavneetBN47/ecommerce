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
 * Product Entity representing products table
 * Manages product catalog with inventory tracking
 */
@Entity
@Table(name = "products", indexes = {
    @Index(name = "idx_product_name", columnList = "product_name"),
    @Index(name = "idx_category", columnList = "category"),
    @Index(name = "idx_price", columnList = "price")
})
@EntityListeners(AuditingEntityListener.class)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "product_id")
    private Long productId;

    @NotBlank(message = "Product name is required")
    @Size(max = 255, message = "Product name must not exceed 255 characters")
    @Column(name = "product_name", nullable = false, length = 255)
    private String productName;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Size(max = 100, message = "Category must not exceed 100 characters")
    @Column(name = "category", length = 100)
    private String category;

    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Price must be greater than 0")
    @Digits(integer = 8, fraction = 2, message = "Price must have at most 8 integer digits and 2 decimal places")
    @Column(name = "price", nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @NotNull(message = "Stock quantity is required")
    @Min(value = 0, message = "Stock quantity cannot be negative")
    @Column(name = "stock_quantity", nullable = false)
    private Integer stockQuantity = 0;

    @Size(max = 500, message = "Image URL must not exceed 500 characters")
    @Column(name = "image_url", length = 500)
    private String imageUrl;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (updatedAt == null) {
            updatedAt = LocalDateTime.now();
        }
        if (isActive == null) {
            isActive = true;
        }
        if (stockQuantity == null) {
            stockQuantity = 0;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    /**
     * Check if product has sufficient stock
     */
    public boolean hasStock(int quantity) {
        return this.stockQuantity >= quantity;
    }

    /**
     * Reduce stock quantity
     */
    public void reduceStock(int quantity) {
        if (!hasStock(quantity)) {
            throw new IllegalStateException("Insufficient stock");
        }
        this.stockQuantity -= quantity;
    }

    /**
     * Increase stock quantity
     */
    public void increaseStock(int quantity) {
        this.stockQuantity += quantity;
    }
}