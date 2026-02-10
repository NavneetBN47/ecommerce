package com.ecommerce.repository;

import com.ecommerce.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Product entity
 * Provides database operations for product management with case-insensitive search
 */
@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    /**
     * Find all active products
     */
    List<Product> findByIsActiveTrue();

    /**
     * Find active product by ID
     */
    Optional<Product> findByProductIdAndIsActiveTrue(Long productId);

    /**
     * Find products by category (case-insensitive)
     */
    List<Product> findByCategoryIgnoreCaseAndIsActiveTrue(String category);

    /**
     * Case-insensitive search across product name, description, and category
     * This implements the business requirement for case-insensitive product search
     */
    @Query("SELECT p FROM Product p WHERE p.isActive = true AND " +
           "(LOWER(p.productName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(p.description) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(p.category) LIKE LOWER(CONCAT('%', :searchTerm, '%')))")
    List<Product> searchProducts(@Param("searchTerm") String searchTerm);

    /**
     * Find products with stock available
     */
    @Query("SELECT p FROM Product p WHERE p.isActive = true AND p.stockQuantity > 0")
    List<Product> findProductsInStock();

    /**
     * Check if product has sufficient stock
     */
    @Query("SELECT CASE WHEN p.stockQuantity >= :quantity THEN true ELSE false END " +
           "FROM Product p WHERE p.productId = :productId")
    boolean hasStock(@Param("productId") Long productId, @Param("quantity") int quantity);
}