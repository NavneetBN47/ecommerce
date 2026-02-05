package com.example.repository;

import com.example.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository interface for Product entity
 */
@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    Optional<Product> findBySku(String sku);

    Optional<Product> findByIdAndIsDeletedFalse(Long id);

    Optional<Product> findBySkuAndIsDeletedFalse(String sku);

    Page<Product> findByIsDeletedFalse(Pageable pageable);

    Page<Product> findByCategoryAndIsDeletedFalse(String category, Pageable pageable);

    @Query("SELECT p FROM Product p WHERE LOWER(p.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
           "AND p.isDeleted = false")
    Page<Product> searchByNameCaseInsensitive(@Param("searchTerm") String searchTerm, Pageable pageable);

    @Query("SELECT p FROM Product p WHERE (LOWER(p.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
           "OR LOWER(p.description) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
           "OR LOWER(p.category) LIKE LOWER(CONCAT('%', :searchTerm, '%'))) " +
           "AND p.isDeleted = false")
    Page<Product> searchProducts(@Param("searchTerm") String searchTerm, Pageable pageable);
}