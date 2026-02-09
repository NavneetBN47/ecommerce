package com.ecommerce.repository;

import com.ecommerce.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * Repository for Product entity
 * Provides database access for product operations per LLD
 */
@Repository
public interface ProductRepository extends JpaRepository<Product, UUID> {

    /**
     * Case-insensitive product search by keyword in name or description
     * Implements LLD product search requirement
     */
    @Query("SELECT p FROM Product p WHERE " +
           "LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(p.description) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Product> searchByKeyword(@Param("keyword") String keyword);

    /**
     * Find active products only
     */
    @Query("SELECT p FROM Product p WHERE p.isActive = true")
    List<Product> findAllActive();

    /**
     * Case-insensitive search for active products only
     */
    @Query("SELECT p FROM Product p WHERE p.isActive = true AND " +
           "(LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(p.description) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    List<Product> searchActiveByKeyword(@Param("keyword") String keyword);
}