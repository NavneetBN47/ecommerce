package com.ecommerce.repository;

import com.ecommerce.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Product Repository
 * Handles database operations for Product entity
 */
@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    /**
     * Case-insensitive product search by name
     */
    @Query("SELECT p FROM Product p WHERE LOWER(p.productName) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    List<Product> searchByProductName(@Param("searchTerm") String searchTerm);

    /**
     * Find products with available quantity greater than zero
     */
    List<Product> findByAvailableQtyGreaterThan(Integer quantity);
}