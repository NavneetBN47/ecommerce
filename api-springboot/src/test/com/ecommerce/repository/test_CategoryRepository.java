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
 * Tests all public methods including custom query methods.
 * Uses @DataJpaTest for repository layer testing with in-memory database.
 */
@DataJpaTest
@ActiveProfiles("test")
@DisplayName("CategoryRepository Tests")
class test_CategoryRepository {

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private TestEntityManager entityManager;

    private Category parentCategory;
    private Category childCategory;
    private Category inactiveCategory;

    /**
     * Set up test data before each test method execution.
     */
    @BeforeEach
    void setUp() {
        parentCategory = new Category();
        parentCategory.setName("Electronics");
        parentCategory.setDescription("Electronic items");
        parentCategory.setActive(true);
        
        childCategory = new Category();
        childCategory.setName("Laptops");
        childCategory.setDescription("Laptop computers");
        childCategory.setActive(true);
        
        inactiveCategory = new Category();
        inactiveCategory.setName("Obsolete");
        inactiveCategory.setDescription("Obsolete items");
        inactiveCategory.setActive(false);
    }

    /**
     * Test finding category by name when category exists.
     * Verifies that the correct category is returned.
     */
    @Test
    @DisplayName("Should find category by name when exists")
    void testFindByName_WhenExists() {
        // Given
        entityManager.persist(parentCategory);
        entityManager.flush();

        // When
        Optional<Category> result = categoryRepository.findByName("Electronics");

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getName()).isEqualTo("Electronics");
        assertThat(result.get().getDescription()).isEqualTo("Electronic items");
    }

    /**
     * Test finding category by name when category does not exist.
     * Verifies that an empty Optional is returned.
     */
    @Test
    @DisplayName("Should return empty Optional when category not found by name")
    void testFindByName_WhenNotExists() {
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
    void testFindAllActive() {
        // Given
        entityManager.persist(parentCategory);
        entityManager.persist(childCategory);
        entityManager.persist(inactiveCategory);
        entityManager.flush();

        // When
        List<Category> result = categoryRepository.findAllActive();

        // Then
        assertThat(result).hasSize(2);
        assertThat(result).allMatch(Category::isActive);
        assertThat(result).extracting(Category::getName)
            .containsExactlyInAnyOrder("Electronics", "Laptops");
    }

    /**
     * Test finding all active categories when none exist.
     * Verifies that an empty list is returned.
     */
    @Test
    @DisplayName("Should return empty list when no active categories exist")
    void testFindAllActive_WhenNoneExist() {
        // Given
        entityManager.persist(inactiveCategory);
        entityManager.flush();

        // When
        List<Category> result = categoryRepository.findAllActive();

        // Then
        assertThat(result).isEmpty();
    }

    /**
     * Test finding all root categories (categories without parent).
     * Verifies that only root active categories are returned.
     */
    @Test
    @DisplayName("Should find all root categories")
    void testFindAllRootCategories() {
        // Given
        Category savedParent = entityManager.persist(parentCategory);
        childCategory.setParentCategory(savedParent);
        entityManager.persist(childCategory);
        entityManager.flush();

        // When
        List<Category> result = categoryRepository.findAllRootCategories();

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("Electronics");
        assertThat(result.get(0).getParentCategory()).isNull();
    }

    /**
     * Test finding all root categories when none exist.
     * Verifies that an empty list is returned.
     */
    @Test
    @DisplayName("Should return empty list when no root categories exist")
    void testFindAllRootCategories_WhenNoneExist() {
        // Given
        Category savedParent = entityManager.persist(parentCategory);
        childCategory.setParentCategory(savedParent);
        entityManager.persist(childCategory);
        
        // Make parent inactive
        savedParent.setActive(false);
        entityManager.persist(savedParent);
        entityManager.flush();

        // When
        List<Category> result = categoryRepository.findAllRootCategories();

        // Then
        assertThat(result).isEmpty();
    }

    /**
     * Test finding subcategories by parent ID.
     * Verifies that only active child categories of the specified parent are returned.
     */
    @Test
    @DisplayName("Should find subcategories by parent ID")
    void testFindSubCategories() {
        // Given
        Category savedParent = entityManager.persist(parentCategory);
        childCategory.setParentCategory(savedParent);
        entityManager.persist(childCategory);
        
        Category anotherChild = new Category();
        anotherChild.setName("Smartphones");
        anotherChild.setDescription("Mobile phones");
        anotherChild.setActive(true);
        anotherChild.setParentCategory(savedParent);
        entityManager.persist(anotherChild);
        entityManager.flush();

        // When
        List<Category> result = categoryRepository.findSubCategories(savedParent.getCategoryId());

        // Then
        assertThat(result).hasSize(2);
        assertThat(result).extracting(Category::getName)
            .containsExactlyInAnyOrder("Laptops", "Smartphones");
        assertThat(result).allMatch(cat -> cat.getParentCategory().getCategoryId().equals(savedParent.getCategoryId()));
    }

    /**
     * Test finding subcategories when parent has no children.
     * Verifies that an empty list is returned.
     */
    @Test
    @DisplayName("Should return empty list when parent has no subcategories")
    void testFindSubCategories_WhenNoChildren() {
        // Given
        Category savedParent = entityManager.persist(parentCategory);
        entityManager.flush();

        // When
        List<Category> result = categoryRepository.findSubCategories(savedParent.getCategoryId());

        // Then
        assertThat(result).isEmpty();
    }

    /**
     * Test finding subcategories excludes inactive children.
     * Verifies that only active subcategories are returned.
     */
    @Test
    @DisplayName("Should exclude inactive subcategories")
    void testFindSubCategories_ExcludesInactive() {
        // Given
        Category savedParent = entityManager.persist(parentCategory);
        childCategory.setParentCategory(savedParent);
        entityManager.persist(childCategory);
        
        Category inactiveChild = new Category();
        inactiveChild.setName("Obsolete Laptops");
        inactiveChild.setDescription("Old laptops");
        inactiveChild.setActive(false);
        inactiveChild.setParentCategory(savedParent);
        entityManager.persist(inactiveChild);
        entityManager.flush();

        // When
        List<Category> result = categoryRepository.findSubCategories(savedParent.getCategoryId());

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("Laptops");
    }

    /**
     * Test saving a new category.
     * Verifies that the category is persisted with generated ID.
     */
    @Test
    @DisplayName("Should save new category successfully")
    void testSave_NewCategory() {
        // When
        Category savedCategory = categoryRepository.save(parentCategory);

        // Then
        assertThat(savedCategory).isNotNull();
        assertThat(savedCategory.getCategoryId()).isNotNull();
        assertThat(savedCategory.getName()).isEqualTo("Electronics");
    }

    /**
     * Test updating an existing category.
     * Verifies that changes are persisted correctly.
     */
    @Test
    @DisplayName("Should update existing category successfully")
    void testSave_UpdateCategory() {
        // Given
        Category savedCategory = entityManager.persist(parentCategory);
        entityManager.flush();
        Long savedId = savedCategory.getCategoryId();

        // When
        savedCategory.setDescription("Updated description");
        Category updatedCategory = categoryRepository.save(savedCategory);

        // Then
        assertThat(updatedCategory.getCategoryId()).isEqualTo(savedId);
        assertThat(updatedCategory.getDescription()).isEqualTo("Updated description");
    }

    /**
     * Test finding category by ID.
     * Verifies that the correct category is retrieved.
     */
    @Test
    @DisplayName("Should find category by ID when exists")
    void testFindById_WhenExists() {
        // Given
        Category savedCategory = entityManager.persist(parentCategory);
        entityManager.flush();

        // When
        Optional<Category> result = categoryRepository.findById(savedCategory.getCategoryId());

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getCategoryId()).isEqualTo(savedCategory.getCategoryId());
    }

    /**
     * Test deleting a category by ID.
     * Verifies that the category is removed from the database.
     */
    @Test
    @DisplayName("Should delete category by ID successfully")
    void testDeleteById() {
        // Given
        Category savedCategory = entityManager.persist(parentCategory);
        entityManager.flush();
        Long savedId = savedCategory.getCategoryId();

        // When
        categoryRepository.deleteById(savedId);
        entityManager.flush();

        // Then
        Optional<Category> result = categoryRepository.findById(savedId);
        assertThat(result).isEmpty();
    }

    /**
     * Test finding all categories.
     * Verifies that all persisted categories are retrieved.
     */
    @Test
    @DisplayName("Should find all categories")
    void testFindAll() {
        // Given
        entityManager.persist(parentCategory);
        entityManager.persist(childCategory);
        entityManager.persist(inactiveCategory);
        entityManager.flush();

        // When
        List<Category> allCategories = categoryRepository.findAll();

        // Then
        assertThat(allCategories).hasSize(3);
    }
}