package com.ecommerce.repository;

import com.ecommerce.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

    Optional<Category> findByName(String name);

    @Query("SELECT c FROM Category c WHERE c.isActive = true")
    List<Category> findAllActive();

    @Query("SELECT c FROM Category c WHERE c.parentCategory IS NULL AND c.isActive = true")
    List<Category> findAllRootCategories();

    @Query("SELECT c FROM Category c WHERE c.parentCategory.categoryId = :parentId AND c.isActive = true")
    List<Category> findSubCategories(Long parentId);
}