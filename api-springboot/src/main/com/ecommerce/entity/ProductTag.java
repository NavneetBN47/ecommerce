package com.ecommerce.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "product_tags", 
    uniqueConstraints = @UniqueConstraint(name = "uq_product_tag", columnNames = {"product_id", "tag"}),
    indexes = {
        @Index(name = "idx_product_tags_product", columnList = "product_id"),
        @Index(name = "idx_product_tags_tag", columnList = "tag")
    }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductTag {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "tag_id")
    private Long tagId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(name = "tag", nullable = false, length = 50)
    private String tag;
}