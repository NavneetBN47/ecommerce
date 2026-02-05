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
 * Repository interface for Product entity
 */
@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    
    /**
     * Find product by SKU
     */
    Optional<Product> findBySku(String sku);
    
    /**
     * Find active products
     */
    List<Product> findByActiveTrue();
    
    /**
     * Find products by category
     */
    List<Product> findByCategory(String category);
    
    /**
     * Find active products by category
     */
    List<Product> findByCategoryAndActiveTrue(String category);
    
    /**
     * Case-insensitive product search by name
     */
    @Query("SELECT p FROM Product p WHERE LOWER(p.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) AND p.active = true")
    Page<Product> searchByNameIgnoreCase(@Param("searchTerm") String searchTerm, Pageable pageable);
    
    /**
     * Case-insensitive product search by name or description
     */
    @Query("SELECT p FROM Product p WHERE (LOWER(p.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
           "OR LOWER(p.description) LIKE LOWER(CONCAT('%', :searchTerm, '%'))) AND p.active = true")
    Page<Product> searchByNameOrDescriptionIgnoreCase(@Param("searchTerm") String searchTerm, Pageable pageable);
    
    /**
     * Find products with stock quantity greater than specified amount
     */
    List<Product> findByStockQuantityGreaterThanAndActiveTrue(Integer quantity);
    
    /**
     * Check if SKU exists
     */
    boolean existsBySku(String sku);
}