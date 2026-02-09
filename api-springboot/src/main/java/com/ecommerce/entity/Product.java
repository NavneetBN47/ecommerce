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
 * Product Entity - Represents a product in the catalog
 * 
 * Aligned with LLD specifications:
 * - id: UUID primary key
 * - name: Product name
 * - description: Product description
 * - price: Product price (>= 0)
 * - available_qty: Available quantity (>= 0)
 */
@Entity
@Table(name = "products", indexes = {
    @Index(name = "idx_products_name_lower", columnList = "name"),
    @Index(name = "idx_products_active", columnList = "is_active")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Product {

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(name = "product_id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "name", nullable = false, length = 255)
    private String name;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "price", nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Column(name = "available_qty", nullable = false)
    private Integer availableQty;

    @Column(name = "sku", unique = true, nullable = false, length = 100)
    private String sku;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    /**
     * Validates that price is non-negative
     */
    @PrePersist
    @PreUpdate
    protected void validatePrice() {
        if (price != null && price.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Price must be greater than or equal to 0");
        }
        if (availableQty != null && availableQty < 0) {
            throw new IllegalArgumentException("Available quantity must be greater than or equal to 0");
        }
    }
}