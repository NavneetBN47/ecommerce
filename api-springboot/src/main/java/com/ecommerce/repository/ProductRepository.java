package com.ecommerce.repository;

import com.ecommerce.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * Product Repository - Data access layer for Product entity
 */
@Repository
public interface ProductRepository extends JpaRepository<Product, UUID> {

    /**
     * Search products by keyword (case-insensitive)
     * Searches in product name and description
     * @param keyword the search keyword
     * @return list of matching products
     */
    @Query("SELECT p FROM Product p WHERE " +
           "LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(p.description) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Product> searchByKeyword(@Param("keyword") String keyword);

    /**
     * Find all active products
     * @return list of active products
     */
    List<Product> findByIsActiveTrue();

    /**
     * Find product by SKU
     * @param sku the product SKU
     * @return the product if found
     */
    Product findBySku(String sku);
}