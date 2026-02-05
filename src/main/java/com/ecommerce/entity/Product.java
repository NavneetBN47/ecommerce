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
import java.util.UUID;

/**
 * Product Entity - Maps to products table
 * Represents products available in the catalog
 */
@Entity
@Table(name = "products")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Product {
    
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "product_id", updatable = false, nullable = false)
    private UUID id;
    
    @Column(name = "name", nullable = false)
    private String name;
    
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;
    
    @Column(name = "price", nullable = false, precision = 10, scale = 2)
    private BigDecimal price;
    
    @Column(name = "category", nullable = false, length = 100)
    private String category;
    
    @Column(name = "brand", length = 100)
    private String brand;
    
    @Column(name = "sku", unique = true, nullable = false, length = 100)
    private String sku;
    
    @Column(name = "stock_quantity", nullable = false)
    private Integer stockQuantity = 0;
    
    @Column(name = "weight", precision = 8, scale = 3)
    private BigDecimal weight;
    
    @Column(name = "is_active")
    private Boolean isActive = true;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    /**
     * Computed property for available quantity (LLD compatibility)
     */
    @Transient
    public Integer getAvailableQty() {
        return stockQuantity;
    }
    
    @Transient
    public void setAvailableQty(Integer qty) {
        this.stockQuantity = qty;
    }
}