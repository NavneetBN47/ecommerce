package com.ecommerce.repository;

import com.ecommerce.entity.Category;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * JUnit 5 test class for CategoryRepository.
 * Tests repository operations for Category entity including custom query methods.
 * 
 * @author QA Automation Team
 * @version 1.0
 */
@DataJpaTest
@ActiveProfiles("test")
@DisplayName("CategoryRepository Tests")
class test_CategoryRepository {

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private TestEntityManager entityManager;

    private Category testCategory;
    private Category parentCategory;
    private Category childCategory;

    /**
     * Set up test data before each test method execution.
     */
    @BeforeEach
    void setUp() {
        testCategory = new Category();
        testCategory.setName("Electronics");
        testCategory.setIsActive(true);
        
        parentCategory = new Category();
        parentCategory.setName("Parent Category");
        parentCategory.setIsActive(true);
        
        childCategory = new Category();
        childCategory.setName("Child Category");
        childCategory.setIsActive(true);
    }

    /**
     * Test finding a category by name when it exists.
     * Verifies that the custom query method returns the correct category.
     */
    @Test
    @DisplayName("Should find category by name when exists")
    void testFindByName_WhenExists_ShouldReturnCategory() {
        // Given
        Category savedCategory = categoryRepository.save(testCategory);
        entityManager.flush();
        entityManager.clear();

        // When
        Optional<Category> result = categoryRepository.findByName("Electronics");

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getName()).isEqualTo("Electronics");
    }

    /**
     * Test finding a category by name when it does not exist.
     * Verifies that the method returns an empty Optional.
     */
    @Test
    @DisplayName("Should return empty when category name does not exist")
    void testFindByName_WhenNotExists_ShouldReturnEmpty() {
        // When
        Optional<Category> result = categoryRepository.findByName("NonExistent");

        // Then
        assertThat(result).isEmpty();
    }

    /**
     * Test finding all active categories.
     * Verifies that only active categories are returned.
     */
    @Test
    @DisplayName("Should find all active categories")
    void testFindAllActive_ShouldReturnOnlyActiveCategories() {
        // Given
        Category activeCategory = new Category();
        activeCategory.setName("Active");
        activeCategory.setIsActive(true);
        
        Category inactiveCategory = new Category();
        inactiveCategory.setName("Inactive");
        inactiveCategory.setIsActive(false);
        
        categoryRepository.save(activeCategory);
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
     * Verifies that only categories without parent are returned.
     */
    @Test
    @DisplayName("Should find all root categories")
    void testFindAllRootCategories_ShouldReturnCategoriesWithoutParent() {
        // Given
        Category rootCategory = new Category();
        rootCategory.setName("Root");
        rootCategory.setIsActive(true);
        rootCategory.setParentCategory(null);
        
        categoryRepository.save(rootCategory);
        entityManager.flush();

        // When
        List<Category> result = categoryRepository.findAllRootCategories();

        // Then
        assertThat(result).isNotEmpty();
        assertThat(result).allMatch(cat -> cat.getParentCategory() == null);
    }

    /**
     * Test finding subcategories by parent ID.
     * Verifies that only child categories of the specified parent are returned.
     */
    @Test
    @DisplayName("Should find subcategories by parent ID")
    void testFindSubCategories_ShouldReturnChildCategories() {
        // Given
        Category savedParent = categoryRepository.save(parentCategory);
        entityManager.flush();
        
        childCategory.setParentCategory(savedParent);
        categoryRepository.save(childCategory);
        entityManager.flush();

        // When
        List<Category> result = categoryRepository.findSubCategories(savedParent.getCategoryId());

        // Then
        assertThat(result).isNotEmpty();
    }

    /**
     * Test finding subcategories when parent has no children.
     * Verifies that an empty list is returned.
     */
    @Test
    @DisplayName("Should return empty list when parent has no subcategories")
    void testFindSubCategories_WhenNoChildren_ShouldReturnEmptyList() {
        // Given
        Category savedParent = categoryRepository.save(parentCategory);
        entityManager.flush();

        // When
        List<Category> result = categoryRepository.findSubCategories(savedParent.getCategoryId());

        // Then
        assertThat(result).isEmpty();
    }

    /**
     * Test finding subcategories with non-existent parent ID.
     * Verifies that an empty list is returned.
     */
    @Test
    @DisplayName("Should return empty list for non-existent parent ID")
    void testFindSubCategories_WithNonExistentParentId_ShouldReturnEmptyList() {
        // When
        List<Category> result = categoryRepository.findSubCategories(999999L);

        // Then
        assertThat(result).isEmpty();
    }

    /**
     * Test saving a category.
     * Verifies that the save operation works correctly.
     */
    @Test
    @DisplayName("Should save category successfully")
    void testSave_ShouldPersistCategory() {
        // When
        Category savedCategory = categoryRepository.save(testCategory);
        entityManager.flush();

        // Then
        assertThat(savedCategory).isNotNull();
        assertThat(savedCategory.getCategoryId()).isNotNull();
    }

    /**
     * Test finding a category by ID.
     * Verifies that findById returns the correct category.
     */
    @Test
    @DisplayName("Should find category by ID when exists")
    void testFindById_WhenExists_ShouldReturnCategory() {
        // Given
        Category savedCategory = categoryRepository.save(testCategory);
        entityManager.flush();
        Long savedId = savedCategory.getCategoryId();

        // When
        Optional<Category> result = categoryRepository.findById(savedId);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getCategoryId()).isEqualTo(savedId);
    }

    /**
     * Test deleting a category.
     * Verifies that the delete operation works correctly.
     */
    @Test
    @DisplayName("Should delete category successfully")
    void testDelete_ShouldRemoveCategory() {
        // Given
        Category savedCategory = categoryRepository.save(testCategory);
        entityManager.flush();
        Long savedId = savedCategory.getCategoryId();

        // When
        categoryRepository.delete(savedCategory);
        entityManager.flush();

        // Then
        Optional<Category> result = categoryRepository.findById(savedId);
        assertThat(result).isEmpty();
    }
}