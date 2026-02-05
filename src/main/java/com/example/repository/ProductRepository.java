package com.example.repository;

import com.example.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Product entity operations.
 */
@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    
    /**
     * Find product by SKU.
     */
    Optional<Product> findBySku(String sku);
    
    /**
     * Find all active products.
     */
    @Query("SELECT p FROM Product p WHERE p.active = true")
    Page<Product> findAllActiveProducts(Pageable pageable);
    
    /**
     * Search products by name (case-insensitive).
     */
    @Query("SELECT p FROM Product p WHERE LOWER(p.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) AND p.active = true")
    Page<Product> searchByName(@Param("searchTerm") String searchTerm, Pageable pageable);
    
    /**
     * Find products by category.
     */
    @Query("SELECT p FROM Product p WHERE p.category = :category AND p.active = true")
    Page<Product> findByCategory(@Param("category") String category, Pageable pageable);
    
    /**
     * Find featured products.
     */
    @Query("SELECT p FROM Product p WHERE p.featured = true AND p.active = true")
    List<Product> findFeaturedProducts();
    
    /**
     * Find products in stock.
     */
    @Query("SELECT p FROM Product p WHERE p.stockQuantity > 0 AND p.active = true")
    Page<Product> findInStockProducts(Pageable pageable);
    
    /**
     * Find products by brand.
     */
    @Query("SELECT p FROM Product p WHERE p.brand = :brand AND p.active = true")
    Page<Product> findByBrand(@Param("brand") String brand, Pageable pageable);
    
    /**
     * Advanced search with multiple criteria (case-insensitive).
     */
    @Query("SELECT p FROM Product p WHERE " +
           "(LOWER(p.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(p.description) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(p.category) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(p.brand) LIKE LOWER(CONCAT('%', :searchTerm, '%'))) AND " +
           "p.active = true")
    Page<Product> advancedSearch(@Param("searchTerm") String searchTerm, Pageable pageable);
    
    /**
     * Check if SKU exists.
     */
    boolean existsBySku(String sku);
}