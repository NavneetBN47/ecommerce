package com.ecommerce.repository;

import com.ecommerce.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for Category entity
 */
@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

    /**
     * Find category by name
     */
    Optional<Category> findByCategoryName(String categoryName);

    /**
     * Find all active categories
     */
    List<Category> findByIsActive(Boolean isActive);

    /**
     * Check if category name exists
     */
    boolean existsByCategoryName(String categoryName);
}