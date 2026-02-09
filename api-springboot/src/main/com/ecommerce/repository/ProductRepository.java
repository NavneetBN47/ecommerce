package com.ecommerce.repository;

import com.ecommerce.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Product Repository
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
    List<Product> findByIsActiveTrue();

    /**
     * Find products by category
     */
    List<Product> findByCategoryAndIsActiveTrue(String category);

    /**
     * Case-insensitive search by product name
     */
    @Query("SELECT p FROM Product p WHERE LOWER(p.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) AND p.isActive = true")
    List<Product> searchByName(@Param("searchTerm") String searchTerm);

    /**
     * Case-insensitive search by name or category
     */
    @Query("SELECT p FROM Product p WHERE (LOWER(p.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR LOWER(p.category) LIKE LOWER(CONCAT('%', :searchTerm, '%'))) AND p.isActive = true")
    List<Product> searchByNameOrCategory(@Param("searchTerm") String searchTerm);

    /**
     * Find products with sufficient stock
     */
    @Query("SELECT p FROM Product p WHERE p.stockQuantity >= :minQuantity AND p.isActive = true")
    List<Product> findByStockQuantityGreaterThanEqual(@Param("minQuantity") Integer minQuantity);

    /**
     * Check if SKU exists
     */
    boolean existsBySku(String sku);
}