package com.ecommerce.repository;

import com.ecommerce.entity.Category;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * JUnit 5 test class for CategoryRepository.
 * Tests all public methods including custom query methods.
 * Uses @DataJpaTest for repository layer testing with in-memory database.
 */
@DataJpaTest
@DisplayName("CategoryRepository Tests")
public class test_CategoryRepository {

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private TestEntityManager entityManager;

    private Category testCategory;
    private Category parentCategory;
    private Category childCategory;

    /**
     * Set up test data before each test method.
     */
    @BeforeEach
    void setUp() {
        parentCategory = new Category();
        parentCategory.setName("Electronics");
        parentCategory.setIsActive(true);
        
        testCategory = new Category();
        testCategory.setName("Smartphones");
        testCategory.setIsActive(true);
        
        childCategory = new Category();
        childCategory.setName("Android Phones");
        childCategory.setIsActive(true);
    }

    /**
     * Test finding category by name - success case.
     */
    @Test
    @DisplayName("Should find category by name")
    void testFindByName_Success() {
        // Given
        Category savedCategory = categoryRepository.save(testCategory);
        entityManager.flush();

        // When
        Optional<Category> result = categoryRepository.findByName("Smartphones");

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getName()).isEqualTo("Smartphones");
    }

    /**
     * Test finding category by name - not found case.
     */
    @Test
    @DisplayName("Should return empty when category not found by name")
    void testFindByName_NotFound() {
        // When
        Optional<Category> result = categoryRepository.findByName("NonExistentCategory");

        // Then
        assertThat(result).isEmpty();
    }

    /**
     * Test finding all active categories.
     */
    @Test
    @DisplayName("Should find all active categories")
    void testFindAllActive() {
        // Given
        categoryRepository.save(testCategory);
        
        Category inactiveCategory = new Category();
        inactiveCategory.setName("Inactive");
        inactiveCategory.setIsActive(false);
        categoryRepository.save(inactiveCategory);
        entityManager.flush();

        // When
        List<Category> result = categoryRepository.findAllActive();

        // Then
        assertThat(result).isNotEmpty();
        assertThat(result).allMatch(Category::getIsActive);
    }

    /**
     * Test finding all root categories.
     */
    @Test
    @DisplayName("Should find all root categories")
    void testFindAllRootCategories() {
        // Given
        Category rootCategory = categoryRepository.save(parentCategory);
        entityManager.flush();

        // When
        List<Category> result = categoryRepository.findAllRootCategories();

        // Then
        assertThat(result).isNotEmpty();
    }

    /**
     * Test finding subcategories by parent ID.
     */
    @Test
    @DisplayName("Should find subcategories by parent ID")
    void testFindSubCategories() {
        // Given
        Category savedParent = categoryRepository.save(parentCategory);
        childCategory.setParentCategory(savedParent);
        categoryRepository.save(childCategory);
        entityManager.flush();

        // When
        List<Category> result = categoryRepository.findSubCategories(savedParent.getCategoryId());

        // Then
        assertThat(result).isNotEmpty();
    }

    /**
     * Test finding subcategories with no children.
     */
    @Test
    @DisplayName("Should return empty list when no subcategories exist")
    void testFindSubCategories_NoChildren() {
        // Given
        Category savedParent = categoryRepository.save(parentCategory);
        entityManager.flush();

        // When
        List<Category> result = categoryRepository.findSubCategories(savedParent.getCategoryId());

        // Then
        assertThat(result).isEmpty();
    }

    /**
     * Test saving a category.
     */
    @Test
    @DisplayName("Should save category successfully")
    void testSaveCategory() {
        // When
        Category savedCategory = categoryRepository.save(testCategory);
        entityManager.flush();

        // Then
        assertThat(savedCategory).isNotNull();
        assertThat(savedCategory.getCategoryId()).isNotNull();
        assertThat(savedCategory.getName()).isEqualTo("Smartphones");
    }

    /**
     * Test finding category by ID.
     */
    @Test
    @DisplayName("Should find category by ID")
    void testFindById_Success() {
        // Given
        Category savedCategory = categoryRepository.save(testCategory);
        entityManager.flush();

        // When
        Optional<Category> result = categoryRepository.findById(savedCategory.getCategoryId());

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getCategoryId()).isEqualTo(savedCategory.getCategoryId());
    }

    /**
     * Test deleting a category.
     */
    @Test
    @DisplayName("Should delete category successfully")
    void testDeleteCategory() {
        // Given
        Category savedCategory = categoryRepository.save(testCategory);
        entityManager.flush();
        Long categoryId = savedCategory.getCategoryId();

        // When
        categoryRepository.deleteById(categoryId);
        entityManager.flush();

        // Then
        Optional<Category> result = categoryRepository.findById(categoryId);
        assertThat(result).isEmpty();
    }

    /**
     * Test finding all categories.
     */
    @Test
    @DisplayName("Should find all categories")
    void testFindAll() {
        // Given
        categoryRepository.save(testCategory);
        categoryRepository.save(parentCategory);
        entityManager.flush();

        // When
        List<Category> categories = categoryRepository.findAll();

        // Then
        assertThat(categories).hasSizeGreaterThanOrEqualTo(2);
    }

    /**
     * Test with null name - edge case.
     */
    @Test
    @DisplayName("Should handle null name gracefully")
    void testFindByName_NullName() {
        // When
        Optional<Category> result = categoryRepository.findByName(null);

        // Then
        assertThat(result).isEmpty();
    }

    /**
     * Test finding active categories when none exist.
     */
    @Test
    @DisplayName("Should return empty list when no active categories exist")
    void testFindAllActive_NoActiveCategories() {
        // Given
        Category inactiveCategory = new Category();
        inactiveCategory.setName("Inactive");
        inactiveCategory.setIsActive(false);
        categoryRepository.save(inactiveCategory);
        entityManager.flush();

        // When
        List<Category> result = categoryRepository.findAllActive();

        // Then
        assertThat(result).isEmpty();
    }
}