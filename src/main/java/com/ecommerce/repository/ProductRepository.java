package com.ecommerce.repository;

import com.ecommerce.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for Product entity
 * Provides database access methods for product operations
 */
@Repository
public interface ProductRepository extends JpaRepository<Product, UUID> {

    /**
     * Find product by SKU
     * @param sku the product SKU
     * @return Optional containing product if found
     */
    Optional<Product> findBySku(String sku);

    /**
     * Search products by keyword (case-insensitive)
     * Searches in name and description fields
     * @param keyword the search keyword
     * @return List of matching products
     */
    @Query("SELECT p FROM Product p WHERE " +
           "LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(p.description) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Product> searchByKeyword(@Param("keyword") String keyword);

    /**
     * Find all active products
     * @return List of active products
     */
    List<Product> findByIsActiveTrue();

    /**
     * Find products by category
     * @param category the product category
     * @return List of products in category
     */
    List<Product> findByCategory(String category);
}