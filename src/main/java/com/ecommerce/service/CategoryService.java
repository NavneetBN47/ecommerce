package com.ecommerce.service;

import com.ecommerce.dto.CategoryDTO;
import com.ecommerce.entity.Category;
import com.ecommerce.exception.ResourceAlreadyExistsException;
import com.ecommerce.exception.ResourceNotFoundException;
import com.ecommerce.mapper.CategoryMapper;
import com.ecommerce.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service class for Category operations
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;

    /**
     * Create a new category
     */
    public CategoryDTO createCategory(CategoryDTO categoryDTO) {
        log.info("Creating new category: {}", categoryDTO.getName());

        // Check if category name already exists
        if (categoryRepository.existsByName(categoryDTO.getName())) {
            throw new ResourceAlreadyExistsException("Category already exists: " + categoryDTO.getName());
        }

        // Map DTO to entity
        Category category = categoryMapper.toEntity(categoryDTO);
        category.setActive(true);

        // Save category
        Category savedCategory = categoryRepository.save(category);
        log.info("Category created successfully: {}", savedCategory.getName());

        return categoryMapper.toDTO(savedCategory);
    }

    /**
     * Get category by ID
     */
    @Transactional(readOnly = true)
    public CategoryDTO getCategoryById(Long id) {
        log.debug("Fetching category by ID: {}", id);
        Category category = categoryRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Category not found with ID: " + id));
        return categoryMapper.toDTO(category);
    }

    /**
     * Get all active categories
     */
    @Transactional(readOnly = true)
    public List<CategoryDTO> getAllActiveCategories() {
        log.debug("Fetching all active categories");
        return categoryRepository.findByActiveTrue().stream()
            .map(categoryMapper::toDTO)
            .collect(Collectors.toList());
    }

    /**
     * Get all categories
     */
    @Transactional(readOnly = true)
    public List<CategoryDTO> getAllCategories() {
        log.debug("Fetching all categories");
        return categoryRepository.findAll().stream()
            .map(categoryMapper::toDTO)
            .collect(Collectors.toList());
    }

    /**
     * Update category
     */
    public CategoryDTO updateCategory(Long id, CategoryDTO categoryDTO) {
        log.info("Updating category with ID: {}", id);

        Category existingCategory = categoryRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Category not found with ID: " + id));

        // Check if name is being changed and if it already exists
        if (categoryDTO.getName() != null && !categoryDTO.getName().equals(existingCategory.getName())) {
            if (categoryRepository.existsByName(categoryDTO.getName())) {
                throw new ResourceAlreadyExistsException("Category already exists: " + categoryDTO.getName());
            }
            existingCategory.setName(categoryDTO.getName());
        }

        if (categoryDTO.getDescription() != null) {
            existingCategory.setDescription(categoryDTO.getDescription());
        }

        Category updatedCategory = categoryRepository.save(existingCategory);
        log.info("Category updated successfully: {}", updatedCategory.getName());

        return categoryMapper.toDTO(updatedCategory);
    }

    /**
     * Delete category
     */
    public void deleteCategory(Long id) {
        log.info("Deleting category with ID: {}", id);

        if (!categoryRepository.existsById(id)) {
            throw new ResourceNotFoundException("Category not found with ID: " + id);
        }

        categoryRepository.deleteById(id);
        log.info("Category deleted successfully with ID: {}", id);
    }
}