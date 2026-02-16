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
 * Tests repository methods for Category entity operations including custom queries.
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
        testCategory = new Category();
        testCategory.setName("Electronics");
        testCategory.setActive(true);

        parentCategory = new Category();
        parentCategory.setName("Parent Category");
        parentCategory.setActive(true);

        childCategory = new Category();
        childCategory.setName("Child Category");
        childCategory.setActive(true);
    }

    /**
     * Test finding a category by name when it exists.
     * Verifies that the repository correctly retrieves a category by its name.
     */
    @Test
    @DisplayName("Should find category by name when exists")
    void testFindByName_WhenExists() {
        // Given
        Category savedCategory = entityManager.persistAndFlush(testCategory);

        // When
        Optional<Category> result = categoryRepository.findByName("Electronics");

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getName()).isEqualTo("Electronics");
    }

    /**
     * Test finding a category by name when it does not exist.
     * Verifies that the repository returns empty Optional for non-existent category.
     */
    @Test
    @DisplayName("Should return empty when category name does not exist")
    void testFindByName_WhenNotExists() {
        // When
        Optional<Category> result = categoryRepository.findByName("NonExistent");

        // Then
        assertThat(result).isEmpty();
    }

    /**
     * Test finding all active categories.
     * Verifies that the repository correctly retrieves only active categories.
     */
    @Test
    @DisplayName("Should find all active categories")
    void testFindAllActive() {
        // Given
        entityManager.persistAndFlush(testCategory);
        Category inactiveCategory = new Category();
        inactiveCategory.setName("Inactive");
        inactiveCategory.setActive(false);
        entityManager.persistAndFlush(inactiveCategory);

        // When
        List<Category> activeCategories = categoryRepository.findAllActive();

        // Then
        assertThat(activeCategories).isNotEmpty();
        assertThat(activeCategories).allMatch(Category::isActive);
    }

    /**
     * Test finding all root categories (categories without parent).
     * Verifies that the repository correctly retrieves root categories.
     */
    @Test
    @DisplayName("Should find all root categories")
    void testFindAllRootCategories() {
        // Given
        Category rootCategory = entityManager.persistAndFlush(parentCategory);
        childCategory.setParentCategory(rootCategory);
        entityManager.persistAndFlush(childCategory);

        // When
        List<Category> rootCategories = categoryRepository.findAllRootCategories();

        // Then
        assertThat(rootCategories).isNotEmpty();
    }

    /**
     * Test finding subcategories by parent ID.
     * Verifies that the repository correctly retrieves child categories.
     */
    @Test
    @DisplayName("Should find subcategories by parent ID")
    void testFindSubCategories() {
        // Given
        Category savedParent = entityManager.persistAndFlush(parentCategory);
        childCategory.setParentCategory(savedParent);
        entityManager.persistAndFlush(childCategory);

        // When
        List<Category> subCategories = categoryRepository.findSubCategories(savedParent.getCategoryId());

        // Then
        assertThat(subCategories).isNotEmpty();
    }

    /**
     * Test finding subcategories when parent has no children.
     * Verifies that the repository returns empty list for parent without children.
     */
    @Test
    @DisplayName("Should return empty list when parent has no subcategories")
    void testFindSubCategories_WhenNoChildren() {
        // Given
        Category savedParent = entityManager.persistAndFlush(parentCategory);

        // When
        List<Category> subCategories = categoryRepository.findSubCategories(savedParent.getCategoryId());

        // Then
        assertThat(subCategories).isEmpty();
    }

    /**
     * Test saving a category.
     * Verifies that the repository correctly persists a category.
     */
    @Test
    @DisplayName("Should save category successfully")
    void testSave_Category() {
        // When
        Category savedCategory = categoryRepository.save(testCategory);

        // Then
        assertThat(savedCategory).isNotNull();
        assertThat(savedCategory.getCategoryId()).isNotNull();
        assertThat(savedCategory.getName()).isEqualTo("Electronics");
    }

    /**
     * Test finding a category by ID.
     * Verifies that the repository correctly retrieves a category by its ID.
     */
    @Test
    @DisplayName("Should find category by ID")
    void testFindById_WhenExists() {
        // Given
        Category savedCategory = entityManager.persistAndFlush(testCategory);

        // When
        Optional<Category> result = categoryRepository.findById(savedCategory.getCategoryId());

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getCategoryId()).isEqualTo(savedCategory.getCategoryId());
    }

    /**
     * Test deleting a category.
     * Verifies that the repository correctly deletes a category.
     */
    @Test
    @DisplayName("Should delete category successfully")
    void testDelete_Category() {
        // Given
        Category savedCategory = entityManager.persistAndFlush(testCategory);
        Long categoryId = savedCategory.getCategoryId();

        // When
        categoryRepository.delete(savedCategory);
        entityManager.flush();

        // Then
        Optional<Category> result = categoryRepository.findById(categoryId);
        assertThat(result).isEmpty();
    }

    /**
     * Test finding all categories.
     * Verifies that the repository correctly retrieves all categories.
     */
    @Test
    @DisplayName("Should find all categories")
    void testFindAll_Categories() {
        // Given
        entityManager.persistAndFlush(testCategory);
        entityManager.persistAndFlush(parentCategory);

        // When
        List<Category> allCategories = categoryRepository.findAll();

        // Then
        assertThat(allCategories).hasSizeGreaterThanOrEqualTo(2);
    }
}