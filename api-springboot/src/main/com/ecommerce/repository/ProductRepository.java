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
 * Repository interface for Product entity operations
 * Implements case-insensitive search functionality
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
    List<Product> findByActiveTrue();

    /**
     * Find products by category (case-insensitive)
     */
    @Query("SELECT p FROM Product p WHERE LOWER(p.category) = LOWER(:category) AND p.active = true")
    List<Product> findByCategoryIgnoreCase(@Param("category") String category);

    /**
     * Search products by name (case-insensitive)
     */
    @Query("SELECT p FROM Product p WHERE LOWER(p.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) AND p.active = true")
    Page<Product> searchByNameIgnoreCase(@Param("searchTerm") String searchTerm, Pageable pageable);

    /**
     * Search products by name or description (case-insensitive)
     */
    @Query("SELECT p FROM Product p WHERE (LOWER(p.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
           "OR LOWER(p.description) LIKE LOWER(CONCAT('%', :searchTerm, '%'))) AND p.active = true")
    Page<Product> searchByNameOrDescriptionIgnoreCase(@Param("searchTerm") String searchTerm, Pageable pageable);

    /**
     * Find products with stock greater than specified quantity
     */
    @Query("SELECT p FROM Product p WHERE p.stockQuantity > :quantity AND p.active = true")
    List<Product> findByStockQuantityGreaterThan(@Param("quantity") Integer quantity);

    /**
     * Check if product has sufficient stock
     */
    @Query("SELECT CASE WHEN p.stockQuantity >= :quantity THEN true ELSE false END " +
           "FROM Product p WHERE p.id = :productId")
    boolean hasSufficientStock(@Param("productId") Long productId, @Param("quantity") Integer quantity);
}