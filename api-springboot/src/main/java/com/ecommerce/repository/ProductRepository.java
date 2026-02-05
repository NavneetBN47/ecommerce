package com.ecommerce.repository;

import com.ecommerce.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for Product entity
 */
@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    /**
     * Find product by SKU
     */
    Optional<Product> findBySku(String sku);

    /**
     * Find all active products
     */
    Page<Product> findByIsActive(Boolean isActive, Pageable pageable);

    /**
     * Find products by category
     */
    Page<Product> findByCategoryCategoryIdAndIsActive(Long categoryId, Boolean isActive, Pageable pageable);

    /**
     * Search products by name (case-insensitive)
     */
    @Query("SELECT p FROM Product p WHERE LOWER(p.productName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) AND p.isActive = true")
    Page<Product> searchByProductName(@Param("searchTerm") String searchTerm, Pageable pageable);

    /**
     * Find products with stock available
     */
    @Query("SELECT p FROM Product p WHERE p.stockQuantity > 0 AND p.isActive = true")
    Page<Product> findAvailableProducts(Pageable pageable);

    /**
     * Check if SKU exists
     */
    boolean existsBySku(String sku);
}