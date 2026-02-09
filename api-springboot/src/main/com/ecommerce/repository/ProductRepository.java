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
 * Repository interface for Product entity
 * Provides database operations for product catalog
 */
@Repository
public interface ProductRepository extends JpaRepository<Product, UUID> {
    
    /**
     * Find product by SKU
     * @param sku product SKU
     * @return Optional containing product if found
     */
    Optional<Product> findBySku(String sku);
    
    /**
     * Search products by keyword (case-insensitive)
     * Searches in name and description fields
     * @param keyword search term
     * @return list of matching products
     */
    @Query("SELECT p FROM Product p WHERE " +
           "LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(p.description) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Product> searchByKeyword(@Param("keyword") String keyword);
    
    /**
     * Find active products by category
     * @param categoryId category UUID
     * @return list of products in category
     */
    List<Product> findByCategoryIdAndIsActiveTrue(UUID categoryId);
    
    /**
     * Find all active products
     * @return list of active products
     */
    List<Product> findByIsActiveTrue();
    
    /**
     * Find featured products
     * @return list of featured products
     */
    List<Product> findByIsFeaturedTrueAndIsActiveTrue();
}