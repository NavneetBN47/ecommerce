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
 * JUnit test class for CategoryRepository.
 * Tests repository methods for Category entity operations including custom queries.
 * Uses in-memory database for testing without affecting production data.
 *
 * @author QA Automation Team
 * @version 1.0
 */
@DataJpaTest
@ActiveProfiles("test")
@DisplayName("Category Repository Tests")
class test_CategoryRepository {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private CategoryRepository categoryRepository;

    private Category rootCategory;
    private Category subCategory;
    private Category inactiveCategory;

    /**
     * Set up test data before each test method execution.
     * Creates and persists test categories with different states.
     */
    @BeforeEach
    void setUp() {
        rootCategory = new Category();
        rootCategory.setName("Electronics");
        rootCategory.setActive(true);
        rootCategory.setParentCategory(null);
        entityManager.persist(rootCategory);

        subCategory = new Category();
        subCategory.setName("Smartphones");
        subCategory.setActive(true);
        subCategory.setParentCategory(rootCategory);
        entityManager.persist(subCategory);

        inactiveCategory = new Category();
        inactiveCategory.setName("Inactive Category");
        inactiveCategory.setActive(false);
        inactiveCategory.setParentCategory(null);
        entityManager.persist(inactiveCategory);

        entityManager.flush();
    }

    /**
     * Test finding a Category by name.
     * Verifies that the custom query method returns the correct Category.
     */
    @Test
    @DisplayName("Should find Category by name")
    void testFindByName_Success() {
        // When
        Optional<Category> result = categoryRepository.findByName("Electronics");

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getName()).isEqualTo("Electronics");
        assertThat(result.get().isActive()).isTrue();
    }

    /**
     * Test finding a Category with non-existent name.
     * Verifies that the method returns empty Optional when category doesn't exist.
     */
    @Test
    @DisplayName("Should return empty Optional when category name does not exist")
    void testFindByName_NotFound() {
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
    void testFindAllActive_Success() {
        // When
        List<Category> result = categoryRepository.findAllActive();

        // Then
        assertThat(result).isNotEmpty();
        assertThat(result).hasSize(2);
        assertThat(result).allMatch(Category::isActive);
        assertThat(result).extracting(Category::getName)
                .containsExactlyInAnyOrder("Electronics", "Smartphones");
    }

    /**
     * Test finding all active categories when none exist.
     * Verifies that an empty list is returned when no active categories exist.
     */
    @Test
    @DisplayName("Should return empty list when no active categories exist")
    void testFindAllActive_Empty() {
        // Given
        categoryRepository.deleteAll();
        entityManager.flush();

        // When
        List<Category> result = categoryRepository.findAllActive();

        // Then
        assertThat(result).isEmpty();
    }

    /**
     * Test finding all root categories.
     * Verifies that only active categories without parent are returned.
     */
    @Test
    @DisplayName("Should find all root categories")
    void testFindAllRootCategories_Success() {
        // When
        List<Category> result = categoryRepository.findAllRootCategories();

        // Then
        assertThat(result).isNotEmpty();
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("Electronics");
        assertThat(result.get(0).getParentCategory()).isNull();
        assertThat(result.get(0).isActive()).isTrue();
    }

    /**
     * Test finding subcategories by parent ID.
     * Verifies that all active subcategories of a parent are returned.
     */
    @Test
    @DisplayName("Should find subcategories by parent ID")
    void testFindSubCategories_Success() {
        // When
        List<Category> result = categoryRepository.findSubCategories(rootCategory.getCategoryId());

        // Then
        assertThat(result).isNotEmpty();
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("Smartphones");
        assertThat(result.get(0).getParentCategory().getCategoryId())
                .isEqualTo(rootCategory.getCategoryId());
    }

    /**
     * Test finding subcategories with non-existent parent ID.
     * Verifies that an empty list is returned when parent doesn't exist.
     */
    @Test
    @DisplayName("Should return empty list when parent ID does not exist")
    void testFindSubCategories_NotFound() {
        // Given
        Long nonExistentParentId = 99999L;

        // When
        List<Category> result = categoryRepository.findSubCategories(nonExistentParentId);

        // Then
        assertThat(result).isEmpty();
    }

    /**
     * Test finding subcategories when parent has no children.
     * Verifies that an empty list is returned when parent has no subcategories.
     */
    @Test
    @DisplayName("Should return empty list when parent has no subcategories")
    void testFindSubCategories_NoChildren() {
        // When
        List<Category> result = categoryRepository.findSubCategories(subCategory.getCategoryId());

        // Then
        assertThat(result).isEmpty();
    }

    /**
     * Test saving a new Category.
     * Verifies that the repository can persist a new Category entity.
     */
    @Test
    @DisplayName("Should save a new Category successfully")
    void testSave_NewCategory() {
        // Given
        Category newCategory = new Category();
        newCategory.setName("Laptops");
        newCategory.setActive(true);
        newCategory.setParentCategory(rootCategory);

        // When
        Category savedCategory = categoryRepository.save(newCategory);

        // Then
        assertThat(savedCategory).isNotNull();
        assertThat(savedCategory.getCategoryId()).isNotNull();
        assertThat(savedCategory.getName()).isEqualTo("Laptops");
        assertThat(savedCategory.isActive()).isTrue();
    }

    /**
     * Test updating an existing Category.
     * Verifies that the repository can update Category properties.
     */
    @Test
    @DisplayName("Should update existing Category successfully")
    void testSave_UpdateCategory() {
        // Given
        rootCategory.setName("Updated Electronics");

        // When
        Category updatedCategory = categoryRepository.save(rootCategory);

        // Then
        assertThat(updatedCategory.getName()).isEqualTo("Updated Electronics");
    }

    /**
     * Test finding a Category by ID.
     * Verifies that the repository can retrieve a Category by its primary key.
     */
    @Test
    @DisplayName("Should find Category by ID")
    void testFindById_Success() {
        // When
        Optional<Category> result = categoryRepository.findById(rootCategory.getCategoryId());

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getCategoryId()).isEqualTo(rootCategory.getCategoryId());
    }

    /**
     * Test deleting a Category.
     * Verifies that the repository can delete a Category entity.
     */
    @Test
    @DisplayName("Should delete Category successfully")
    void testDelete_Success() {
        // Given
        Long categoryId = inactiveCategory.getCategoryId();

        // When
        categoryRepository.delete(inactiveCategory);
        entityManager.flush();

        // Then
        Optional<Category> result = categoryRepository.findById(categoryId);
        assertThat(result).isEmpty();
    }

    /**
     * Test counting all Categories.
     * Verifies that the repository can count the total number of Categories.
     */
    @Test
    @DisplayName("Should count all Categories")
    void testCount_Success() {
        // When
        long count = categoryRepository.count();

        // Then
        assertThat(count).isEqualTo(3);
    }

    /**
     * Test checking if Category exists by ID.
     * Verifies that the repository can check existence of a Category.
     */
    @Test
    @DisplayName("Should return true when Category exists")
    void testExistsById_True() {
        // When
        boolean exists = categoryRepository.existsById(rootCategory.getCategoryId());

        // Then
        assertThat(exists).isTrue();
    }

    /**
     * Test checking if Category exists with non-existent ID.
     * Verifies that the repository returns false for non-existent Category.
     */
    @Test
    @DisplayName("Should return false when Category does not exist")
    void testExistsById_False() {
        // Given
        Long nonExistentId = 99999L;

        // When
        boolean exists = categoryRepository.existsById(nonExistentId);

        // Then
        assertThat(exists).isFalse();
    }

    /**
     * Test finding category by name case sensitivity.
     * Verifies that the search is case-sensitive.
     */
    @Test
    @DisplayName("Should be case-sensitive when finding by name")
    void testFindByName_CaseSensitive() {
        // When
        Optional<Category> result = categoryRepository.findByName("electronics");

        // Then
        assertThat(result).isEmpty();
    }

    /**
     * Test finding inactive categories are excluded from active list.
     * Verifies that inactive categories are not returned in findAllActive.
     */
    @Test
    @DisplayName("Should exclude inactive categories from active list")
    void testFindAllActive_ExcludesInactive() {
        // When
        List<Category> result = categoryRepository.findAllActive();

        // Then
        assertThat(result).noneMatch(c -> c.getName().equals("Inactive Category"));
    }

    /**
     * Test creating a multi-level category hierarchy.
     * Verifies that categories can have multiple levels of nesting.
     */
    @Test
    @DisplayName("Should support multi-level category hierarchy")
    void testMultiLevelHierarchy() {
        // Given
        Category level2 = new Category();
        level2.setName("Android Phones");
        level2.setActive(true);
        level2.setParentCategory(subCategory);
        entityManager.persist(level2);
        entityManager.flush();

        // When
        List<Category> subCategoriesLevel1 = categoryRepository.findSubCategories(rootCategory.getCategoryId());
        List<Category> subCategoriesLevel2 = categoryRepository.findSubCategories(subCategory.getCategoryId());

        // Then
        assertThat(subCategoriesLevel1).hasSize(1);
        assertThat(subCategoriesLevel2).hasSize(1);
        assertThat(subCategoriesLevel2.get(0).getName()).isEqualTo("Android Phones");
    }
}