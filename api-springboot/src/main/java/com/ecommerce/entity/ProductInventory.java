package com.ecommerce.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Product inventory entity for stock management
 * Implements available_qty from LLD
 */
@Entity
@Table(name = "product_inventory")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class ProductInventory {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "inventory_id", updatable = false, nullable = false)
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false, unique = true)
    private Product product;

    @Column(name = "quantity_available", nullable = false)
    private Integer quantityAvailable = 0;

    @Column(name = "quantity_reserved", nullable = false)
    private Integer quantityReserved = 0;

    @Column(name = "reorder_level")
    private Integer reorderLevel = 10;

    @Column(name = "max_stock_level")
    private Integer maxStockLevel;

    @Column(name = "last_restocked")
    private LocalDateTime lastRestocked;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}