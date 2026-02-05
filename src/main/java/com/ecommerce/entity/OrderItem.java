package com.ecommerce.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * OrderItem Entity representing individual items in an order
 * 
 * Business Rules:
 * - Denormalized product name and price for historical accuracy
 * - Subtotal = price * quantity
 * - Cascade delete with order
 * - Cannot delete product if in order (RESTRICT)
 */
@Entity
@Table(name = "order_items", indexes = {
    @Index(name = "idx_order", columnList = "order_id"),
    @Index(name = "idx_product", columnList = "product_id")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "order_item_id")
    private Long orderItemId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @Column(name = "order_id", insertable = false, updatable = false)
    private Long orderId;

    @Column(name = "product_id", nullable = false)
    private Long productId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", insertable = false, updatable = false)
    private Product product;

    @Column(name = "product_name", nullable = false, length = 200)
    private String productName;

    @Column(name = "price", nullable = false, precision = 12, scale = 2)
    private BigDecimal price;

    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    @Column(name = "subtotal", nullable = false, precision = 12, scale = 2)
    private BigDecimal subtotal;

    /**
     * Calculate and set subtotal
     */
    public void calculateSubtotal() {
        this.subtotal = price.multiply(BigDecimal.valueOf(quantity));
    }

    /**
     * Create OrderItem from CartItem
     */
    public static OrderItem fromCartItem(CartItem cartItem, Product product) {
        OrderItem orderItem = OrderItem.builder()
            .productId(cartItem.getProductId())
            .productName(product.getName())
            .price(product.getPrice())
            .quantity(cartItem.getQuantity())
            .build();
        orderItem.calculateSubtotal();
        return orderItem;
    }
}