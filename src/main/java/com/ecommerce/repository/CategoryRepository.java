package com.ecommerce.repository;

import com.ecommerce.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Category entity
 */
@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

    Optional<Category> findByName(String name);

    boolean existsByName(String name);

    @Query("SELECT c FROM Category c WHERE c.status = 'ACTIVE'")
    List<Category> findAllActiveCategories();

    @Query("SELECT c FROM Category c WHERE c.parentCategory IS NULL AND c.status = 'ACTIVE'")
    List<Category> findRootCategories();

    @Query("SELECT c FROM Category c WHERE c.parentCategory.id = :parentId AND c.status = 'ACTIVE'")
    List<Category> findSubCategories(Long parentId);
}