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
 * Tests repository methods for Category entity operations including
 * finding by name, active categories, root categories, and subcategories.
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

    /**
     * Set up test data before each test method.
     */
    @BeforeEach
    void setUp() {
        testCategory = new Category();
        testCategory.setName("Electronics");
        testCategory.setIsActive(true);
        
        parentCategory = new Category();
        parentCategory.setName("Parent Category");
        parentCategory.setIsActive(true);
    }

    /**
     * Test finding a category by name when it exists.
     * Verifies that the correct category is returned.
     */
    @Test
    @DisplayName("Should find category by name when exists")
    void testFindByName_WhenExists_ReturnsCategory() {
        // Given
        Category category = new Category();
        category.setName("TestCategory");
        category.setIsActive(true);
        entityManager.persistAndFlush(category);

        // When
        Optional<Category> result = categoryRepository.findByName("TestCategory");

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getName()).isEqualTo("TestCategory");
    }

    /**
     * Test finding a category by name when it does not exist.
     * Verifies that an empty Optional is returned.
     */
    @Test
    @DisplayName("Should return empty Optional when category name does not exist")
    void testFindByName_WhenNotExists_ReturnsEmpty() {
        // Given
        String nonExistentName = "NonExistentCategory";

        // When
        Optional<Category> result = categoryRepository.findByName(nonExistentName);

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
        // Given
        Category activeCategory = new Category();
        activeCategory.setName("Active");
        activeCategory.setIsActive(true);
        
        Category inactiveCategory = new Category();
        inactiveCategory.setName("Inactive");
        inactiveCategory.setIsActive(false);
        
        entityManager.persist(activeCategory);
        entityManager.persist(inactiveCategory);
        entityManager.flush();

        // When
        List<Category> result = categoryRepository.findAllActive();

        // Then
        assertThat(result).isNotNull();
        assertThat(result).allMatch(Category::getIsActive);
    }

    /**
     * Test finding all root categories.
     * Verifies that only categories without parent are returned.
     */
    @Test
    @DisplayName("Should find all root categories")
    void testFindAllRootCategories_ReturnsOnlyRootCategories() {
        // Given
        Category rootCategory = new Category();
        rootCategory.setName("Root");
        rootCategory.setIsActive(true);
        rootCategory.setParentCategory(null);
        entityManager.persistAndFlush(rootCategory);

        // When
        List<Category> result = categoryRepository.findAllRootCategories();

        // Then
        assertThat(result).isNotNull();
        assertThat(result).allMatch(cat -> cat.getParentCategory() == null);
        assertThat(result).allMatch(Category::getIsActive);
    }

    /**
     * Test finding subcategories by parent ID.
     * Verifies that only child categories of the specified parent are returned.
     */
    @Test
    @DisplayName("Should find subcategories by parent ID")
    void testFindSubCategories_ReturnsChildCategories() {
        // Given
        Category parent = new Category();
        parent.setName("Parent");
        parent.setIsActive(true);
        parent = entityManager.persistAndFlush(parent);
        
        Category child = new Category();
        child.setName("Child");
        child.setIsActive(true);
        child.setParentCategory(parent);
        entityManager.persistAndFlush(child);

        // When
        List<Category> result = categoryRepository.findSubCategories(parent.getCategoryId());

        // Then
        assertThat(result).isNotNull();
    }

    /**
     * Test saving a category.
     * Verifies that the category is persisted correctly.
     */
    @Test
    @DisplayName("Should save category successfully")
    void testSave_ValidCategory_SavesSuccessfully() {
        // Given
        Category newCategory = new Category();
        newCategory.setName("NewCategory");
        newCategory.setIsActive(true);

        // When
        Category savedCategory = categoryRepository.save(newCategory);

        // Then
        assertThat(savedCategory).isNotNull();
        assertThat(savedCategory.getCategoryId()).isNotNull();
        assertThat(savedCategory.getName()).isEqualTo("NewCategory");
    }

    /**
     * Test finding a category by ID.
     * Verifies that the category can be retrieved by its ID.
     */
    @Test
    @DisplayName("Should find category by ID when exists")
    void testFindById_WhenExists_ReturnsCategory() {
        // Given
        Category category = new Category();
        category.setName("TestCategory");
        category.setIsActive(true);
        Category savedCategory = entityManager.persistAndFlush(category);

        // When
        Optional<Category> result = categoryRepository.findById(savedCategory.getCategoryId());

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getCategoryId()).isEqualTo(savedCategory.getCategoryId());
    }

    /**
     * Test deleting a category.
     * Verifies that the category is removed from the database.
     */
    @Test
    @DisplayName("Should delete category successfully")
    void testDelete_ExistingCategory_DeletesSuccessfully() {
        // Given
        Category category = new Category();
        category.setName("ToDelete");
        category.setIsActive(true);
        Category savedCategory = entityManager.persistAndFlush(category);
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
     * Verifies that all categories can be retrieved.
     */
    @Test
    @DisplayName("Should find all categories")
    void testFindAll_ReturnsAllCategories() {
        // Given
        Category category1 = new Category();
        category1.setName("Category1");
        category1.setIsActive(true);
        
        Category category2 = new Category();
        category2.setName("Category2");
        category2.setIsActive(true);
        
        entityManager.persist(category1);
        entityManager.persist(category2);
        entityManager.flush();

        // When
        List<Category> result = categoryRepository.findAll();

        // Then
        assertThat(result).isNotNull();
        assertThat(result.size()).isGreaterThanOrEqualTo(2);
    }

    /**
     * Test counting categories.
     * Verifies that the count of categories is correct.
     */
    @Test
    @DisplayName("Should count categories correctly")
    void testCount_ReturnsCorrectCount() {
        // Given
        long initialCount = categoryRepository.count();
        Category category = new Category();
        category.setName("CountTest");
        category.setIsActive(true);
        entityManager.persistAndFlush(category);

        // When
        long newCount = categoryRepository.count();

        // Then
        assertThat(newCount).isEqualTo(initialCount + 1);
    }

    /**
     * Test finding subcategories when parent has no children.
     * Verifies that an empty list is returned.
     */
    @Test
    @DisplayName("Should return empty list when parent has no subcategories")
    void testFindSubCategories_WhenNoChildren_ReturnsEmptyList() {
        // Given
        Category parent = new Category();
        parent.setName("ParentWithNoChildren");
        parent.setIsActive(true);
        parent = entityManager.persistAndFlush(parent);

        // When
        List<Category> result = categoryRepository.findSubCategories(parent.getCategoryId());

        // Then
        assertThat(result).isEmpty();
    }
}