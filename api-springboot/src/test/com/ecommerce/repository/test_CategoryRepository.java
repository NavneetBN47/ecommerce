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
 * Tests repository methods for Category entity operations including hierarchical queries.
 * Uses @DataJpaTest for repository layer testing with in-memory database.
 * 
 * @author Test Generation Agent
 * @version 1.0
 */
@DataJpaTest
@ActiveProfiles("test")
@DisplayName("CategoryRepository Tests")
public class test_CategoryRepository {

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private TestEntityManager entityManager;

    private Category rootCategory;
    private Category subCategory1;
    private Category subCategory2;
    private Category inactiveCategory;

    /**
     * Set up test data before each test method execution.
     * Creates and persists test categories with hierarchical structure.
     */
    @BeforeEach
    void setUp() {
        // Create root category
        rootCategory = new Category();
        rootCategory.setName("Electronics");
        rootCategory.setDescription("Electronic items");
        rootCategory.setIsActive(true);
        entityManager.persist(rootCategory);

        // Create sub-categories
        subCategory1 = new Category();
        subCategory1.setName("Laptops");
        subCategory1.setDescription("Laptop computers");
        subCategory1.setIsActive(true);
        subCategory1.setParentCategory(rootCategory);
        entityManager.persist(subCategory1);

        subCategory2 = new Category();
        subCategory2.setName("Smartphones");
        subCategory2.setDescription("Mobile phones");
        subCategory2.setIsActive(true);
        subCategory2.setParentCategory(rootCategory);
        entityManager.persist(subCategory2);

        // Create inactive category
        inactiveCategory = new Category();
        inactiveCategory.setName("Discontinued");
        inactiveCategory.setDescription("Discontinued items");
        inactiveCategory.setIsActive(false);
        entityManager.persist(inactiveCategory);
        
        entityManager.flush();
    }

    /**
     * Test finding a Category by name when it exists.
     * Verifies that the correct Category is returned.
     */
    @Test
    @DisplayName("Should find Category by name when exists")
    void testFindByName_WhenExists_ReturnsCategory() {
        // When
        Optional<Category> result = categoryRepository.findByName("Electronics");

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getName()).isEqualTo("Electronics");
        assertThat(result.get().getDescription()).isEqualTo("Electronic items");
    }

    /**
     * Test finding a Category by name when it does not exist.
     * Verifies that an empty Optional is returned.
     */
    @Test
    @DisplayName("Should return empty Optional when Category name does not exist")
    void testFindByName_WhenNotExists_ReturnsEmpty() {
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
    void testFindAllActive_ReturnsOnlyActiveCategories() {
        // When
        List<Category> result = categoryRepository.findAllActive();

        // Then
        assertThat(result).isNotEmpty();
        assertThat(result).hasSize(3); // root + 2 subcategories
        assertThat(result).allMatch(Category::getIsActive);
        assertThat(result).extracting(Category::getName)
            .containsExactlyInAnyOrder("Electronics", "Laptops", "Smartphones");
    }

    /**
     * Test finding all active categories when none exist.
     * Verifies that an empty list is returned.
     */
    @Test
    @DisplayName("Should return empty list when no active categories exist")
    void testFindAllActive_WhenNoneActive_ReturnsEmpty() {
        // Given - deactivate all categories
        rootCategory.setIsActive(false);
        subCategory1.setIsActive(false);
        subCategory2.setIsActive(false);
        entityManager.flush();

        // When
        List<Category> result = categoryRepository.findAllActive();

        // Then
        assertThat(result).isEmpty();
    }

    /**
     * Test finding all root categories (categories without parent).
     * Verifies that only active root categories are returned.
     */
    @Test
    @DisplayName("Should find all root categories")
    void testFindAllRootCategories_ReturnsRootCategories() {
        // When
        List<Category> result = categoryRepository.findAllRootCategories();

        // Then
        assertThat(result).isNotEmpty();
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("Electronics");
        assertThat(result.get(0).getParentCategory()).isNull();
        assertThat(result.get(0).getIsActive()).isTrue();
    }

    /**
     * Test finding all root categories when none exist.
     * Verifies that an empty list is returned.
     */
    @Test
    @DisplayName("Should return empty list when no root categories exist")
    void testFindAllRootCategories_WhenNoneExist_ReturnsEmpty() {
        // Given - deactivate root category
        rootCategory.setIsActive(false);
        entityManager.flush();

        // When
        List<Category> result = categoryRepository.findAllRootCategories();

        // Then
        assertThat(result).isEmpty();
    }

    /**
     * Test finding subcategories by parent ID.
     * Verifies that all active subcategories of a parent are returned.
     */
    @Test
    @DisplayName("Should find subcategories by parent ID")
    void testFindSubCategories_ReturnsSubCategories() {
        // When
        List<Category> result = categoryRepository.findSubCategories(rootCategory.getCategoryId());

        // Then
        assertThat(result).isNotEmpty();
        assertThat(result).hasSize(2);
        assertThat(result).extracting(Category::getName)
            .containsExactlyInAnyOrder("Laptops", "Smartphones");
        assertThat(result).allMatch(cat -> cat.getParentCategory().getCategoryId().equals(rootCategory.getCategoryId()));
    }

    /**
     * Test finding subcategories when parent has no children.
     * Verifies that an empty list is returned.
     */
    @Test
    @DisplayName("Should return empty list when parent has no subcategories")
    void testFindSubCategories_WhenNoChildren_ReturnsEmpty() {
        // When
        List<Category> result = categoryRepository.findSubCategories(subCategory1.getCategoryId());

        // Then
        assertThat(result).isEmpty();
    }

    /**
     * Test finding subcategories with non-existent parent ID.
     * Verifies that an empty list is returned.
     */
    @Test
    @DisplayName("Should return empty list when parent ID does not exist")
    void testFindSubCategories_WhenParentNotExists_ReturnsEmpty() {
        // When
        List<Category> result = categoryRepository.findSubCategories(99999L);

        // Then
        assertThat(result).isEmpty();
    }

    /**
     * Test saving a new Category.
     * Verifies that the Category is persisted correctly.
     */
    @Test
    @DisplayName("Should save new Category successfully")
    void testSave_NewCategory_Success() {
        // Given
        Category newCategory = new Category();
        newCategory.setName("Tablets");
        newCategory.setDescription("Tablet devices");
        newCategory.setIsActive(true);
        newCategory.setParentCategory(rootCategory);

        // When
        Category savedCategory = categoryRepository.save(newCategory);

        // Then
        assertThat(savedCategory).isNotNull();
        assertThat(savedCategory.getCategoryId()).isNotNull();
        assertThat(savedCategory.getName()).isEqualTo("Tablets");
        assertThat(savedCategory.getParentCategory().getCategoryId()).isEqualTo(rootCategory.getCategoryId());
    }

    /**
     * Test updating an existing Category.
     * Verifies that Category modifications are persisted correctly.
     */
    @Test
    @DisplayName("Should update existing Category successfully")
    void testSave_UpdateCategory_Success() {
        // Given
        rootCategory.setDescription("Updated description");

        // When
        Category updatedCategory = categoryRepository.save(rootCategory);
        entityManager.flush();

        // Then
        assertThat(updatedCategory.getDescription()).isEqualTo("Updated description");
        assertThat(updatedCategory.getCategoryId()).isEqualTo(rootCategory.getCategoryId());
    }

    /**
     * Test deleting a Category by ID.
     * Verifies that the Category is removed from the database.
     */
    @Test
    @DisplayName("Should delete Category by ID successfully")
    void testDeleteById_Success() {
        // Given
        Long categoryId = inactiveCategory.getCategoryId();

        // When
        categoryRepository.deleteById(categoryId);
        entityManager.flush();

        // Then
        Optional<Category> result = categoryRepository.findById(categoryId);
        assertThat(result).isEmpty();
    }

    /**
     * Test finding a Category by ID.
     * Verifies that the correct Category is retrieved.
     */
    @Test
    @DisplayName("Should find Category by ID when exists")
    void testFindById_WhenExists_ReturnsCategory() {
        // When
        Optional<Category> result = categoryRepository.findById(rootCategory.getCategoryId());

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getCategoryId()).isEqualTo(rootCategory.getCategoryId());
        assertThat(result.get().getName()).isEqualTo("Electronics");
    }

    /**
     * Test that inactive categories are excluded from active queries.
     * Verifies filtering logic for active status.
     */
    @Test
    @DisplayName("Should exclude inactive categories from active queries")
    void testActiveQueriesExcludeInactiveCategories() {
        // When
        List<Category> activeCategories = categoryRepository.findAllActive();
        List<Category> rootCategories = categoryRepository.findAllRootCategories();

        // Then
        assertThat(activeCategories).doesNotContain(inactiveCategory);
        assertThat(rootCategories).doesNotContain(inactiveCategory);
    }
}