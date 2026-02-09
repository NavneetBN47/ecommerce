package com.ecommerce.repository;

import com.ecommerce.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository interface for Category entity
 */
@Repository
public interface CategoryRepository extends JpaRepository<Category, UUID> {
    
    /**
     * Find category by name
     * @param name category name
     * @return Optional containing category if found
     */
    Optional<Category> findByName(String name);
    
    /**
     * Find all active categories
     * @return list of active categories
     */
    List<Category> findByIsActiveTrueOrderBySortOrder();
    
    /**
     * Find top-level categories (no parent)
     * @return list of parent categories
     */
    List<Category> findByParentCategoryIsNullAndIsActiveTrueOrderBySortOrder();
}