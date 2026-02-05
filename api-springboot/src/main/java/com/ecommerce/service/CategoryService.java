package com.ecommerce.service;

import com.ecommerce.dto.CategoryDTO;
import com.ecommerce.entity.Category;
import com.ecommerce.exception.DuplicateResourceException;
import com.ecommerce.exception.ResourceNotFoundException;
import com.ecommerce.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for Category operations
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CategoryService {

    private final CategoryRepository categoryRepository;

    /**
     * Create a new category
     */
    @Transactional
    public CategoryDTO createCategory(CategoryDTO categoryDTO) {
        log.info("Creating new category: {}", categoryDTO.getCategoryName());

        if (categoryRepository.existsByCategoryName(categoryDTO.getCategoryName())) {
            throw new DuplicateResourceException("Category already exists: " + categoryDTO.getCategoryName());
        }

        Category category = Category.builder()
            .categoryName(categoryDTO.getCategoryName())
            .description(categoryDTO.getDescription())
            .isActive(true)
            .build();

        category = categoryRepository.save(category);
        log.info("Category created successfully: {}", category.getCategoryId());

        return mapToDTO(category);
    }

    /**
     * Get all active categories
     */
    @Transactional(readOnly = true)
    public List<CategoryDTO> getAllActiveCategories() {
        return categoryRepository.findByIsActive(true).stream()
            .map(this::mapToDTO)
            .collect(Collectors.toList());
    }

    /**
     * Get category by ID
     */
    @Transactional(readOnly = true)
    public CategoryDTO getCategoryById(Long categoryId) {
        Category category = categoryRepository.findById(categoryId)
            .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + categoryId));
        return mapToDTO(category);
    }

    /**
     * Update category
     */
    @Transactional
    public CategoryDTO updateCategory(Long categoryId, CategoryDTO categoryDTO) {
        log.info("Updating category: {}", categoryId);

        Category category = categoryRepository.findById(categoryId)
            .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + categoryId));

        if (!category.getCategoryName().equals(categoryDTO.getCategoryName()) &&
            categoryRepository.existsByCategoryName(categoryDTO.getCategoryName())) {
            throw new DuplicateResourceException("Category name already exists: " + categoryDTO.getCategoryName());
        }

        category.setCategoryName(categoryDTO.getCategoryName());
        category.setDescription(categoryDTO.getDescription());

        category = categoryRepository.save(category);
        log.info("Category updated successfully: {}", categoryId);

        return mapToDTO(category);
    }

    /**
     * Delete category (soft delete)
     */
    @Transactional
    public void deleteCategory(Long categoryId) {
        log.info("Deleting category: {}", categoryId);

        Category category = categoryRepository.findById(categoryId)
            .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + categoryId));

        category.setIsActive(false);
        categoryRepository.save(category);

        log.info("Category deleted successfully: {}", categoryId);
    }

    /**
     * Map Category entity to CategoryDTO
     */
    private CategoryDTO mapToDTO(Category category) {
        return CategoryDTO.builder()
            .categoryId(category.getCategoryId())
            .categoryName(category.getCategoryName())
            .description(category.getDescription())
            .isActive(category.getIsActive())
            .createdAt(category.getCreatedAt())
            .updatedAt(category.getUpdatedAt())
            .build();
    }
}