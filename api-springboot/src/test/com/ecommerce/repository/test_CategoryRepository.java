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
 * Tests all repository methods including custom query methods for category operations.
 * Uses @DataJpaTest for repository layer testing with in-memory database.
 *
 * @author QA Automation Team
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
    private Category subCategory;
    private Category inactiveCategory;

    /**
     * Set up test data before each test method execution.
     * Creates test categories including root, sub, and inactive categories.
     */
    @BeforeEach
    void setUp() {
        rootCategory = new Category();
        rootCategory.setName("Electronics");
        rootCategory.setDescription("Electronic items");
        rootCategory.setIsActive(true);
        rootCategory.setParentCategory(null);
        entityManager.persist(rootCategory);

        subCategory = new Category();
        subCategory.setName("Laptops");
        subCategory.setDescription("Laptop computers");
        subCategory.setIsActive(true);
        subCategory.setParentCategory(rootCategory);
        entityManager.persist(subCategory);

        inactiveCategory = new Category();
        inactiveCategory.setName("Obsolete");
        inactiveCategory.setDescription("Inactive category");
        inactiveCategory.setIsActive(false);
        inactiveCategory.setParentCategory(null);
        entityManager.persist(inactiveCategory);

        entityManager.flush();
    }

    /**
     * Test finding a category by name.
     * Verifies that the correct category is retrieved by its name.
     */
    @Test
    @DisplayName("Should find category by name")
    void testFindByName_Success() {
        Optional<Category> result = categoryRepository.findByName("Electronics");

        assertThat(result).isPresent();
        assertThat(result.get().getName()).isEqualTo("Electronics");
        assertThat(result.get().getIsActive()).isTrue();
    }

    /**
     * Test finding a category with non-existent name.
     * Verifies that an empty Optional is returned when name doesn't exist.
     */
    @Test
    @DisplayName("Should return empty when category name not found")
    void testFindByName_NotFound() {
        Optional<Category> result = categoryRepository.findByName("NonExistent");

        assertThat(result).isEmpty();
    }

    /**
     * Test finding all active categories.
     * Verifies that only active categories are retrieved.
     */
    @Test
    @DisplayName("Should find all active categories")
    void testFindAllActive() {
        List<Category> activeCategories = categoryRepository.findAllActive();

        assertThat(activeCategories).isNotEmpty();
        assertThat(activeCategories).hasSize(2);
        assertThat(activeCategories).allMatch(Category::getIsActive);
        assertThat(activeCategories).extracting(Category::getName)
            .containsExactlyInAnyOrder("Electronics", "Laptops");
    }

    /**
     * Test finding all active categories when none exist.
     * Verifies that an empty list is returned when no active categories exist.
     */
    @Test
    @DisplayName("Should return empty list when no active categories")
    void testFindAllActive_NoActiveCategories() {
        categoryRepository.deleteAll();
        entityManager.flush();

        Category inactive = new Category();
        inactive.setName("Inactive");
        inactive.setIsActive(false);
        entityManager.persist(inactive);
        entityManager.flush();

        List<Category> activeCategories = categoryRepository.findAllActive();

        assertThat(activeCategories).isEmpty();
    }

    /**
     * Test finding all root categories.
     * Verifies that only root-level active categories are retrieved.
     */
    @Test
    @DisplayName("Should find all root categories")
    void testFindAllRootCategories() {
        List<Category> rootCategories = categoryRepository.findAllRootCategories();

        assertThat(rootCategories).isNotEmpty();
        assertThat(rootCategories).hasSize(1);
        assertThat(rootCategories.get(0).getName()).isEqualTo("Electronics");
        assertThat(rootCategories.get(0).getParentCategory()).isNull();
    }

    /**
     * Test finding all root categories when none exist.
     * Verifies that an empty list is returned when no root categories exist.
     */
    @Test
    @DisplayName("Should return empty list when no root categories")
    void testFindAllRootCategories_NoRootCategories() {
        categoryRepository.deleteAll();
        entityManager.flush();

        Category parent = new Category();
        parent.setName("Parent");
        parent.setIsActive(true);
        entityManager.persist(parent);

        Category child = new Category();
        child.setName("Child");
        child.setIsActive(true);
        child.setParentCategory(parent);
        entityManager.persist(child);
        entityManager.flush();

        List<Category> rootCategories = categoryRepository.findAllRootCategories();

        assertThat(rootCategories).hasSize(1);
        assertThat(rootCategories.get(0).getName()).isEqualTo("Parent");
    }

    /**
     * Test finding subcategories by parent ID.
     * Verifies that all active subcategories of a parent are retrieved.
     */
    @Test
    @DisplayName("Should find subcategories by parent ID")
    void testFindSubCategories() {
        List<Category> subCategories = categoryRepository.findSubCategories(rootCategory.getCategoryId());

        assertThat(subCategories).isNotEmpty();
        assertThat(subCategories).hasSize(1);
        assertThat(subCategories.get(0).getName()).isEqualTo("Laptops");
        assertThat(subCategories.get(0).getParentCategory().getCategoryId())
            .isEqualTo(rootCategory.getCategoryId());
    }

    /**
     * Test finding subcategories with non-existent parent ID.
     * Verifies that an empty list is returned when parent ID doesn't exist.
     */
    @Test
    @DisplayName("Should return empty list when parent ID not found")
    void testFindSubCategories_ParentNotFound() {
        List<Category> subCategories = categoryRepository.findSubCategories(999L);

        assertThat(subCategories).isEmpty();
    }

    /**
     * Test finding subcategories when parent has no children.
     * Verifies that an empty list is returned when parent has no subcategories.
     */
    @Test
    @DisplayName("Should return empty list when parent has no subcategories")
    void testFindSubCategories_NoChildren() {
        List<Category> subCategories = categoryRepository.findSubCategories(subCategory.getCategoryId());

        assertThat(subCategories).isEmpty();
    }

    /**
     * Test saving a new category.
     * Verifies that a category can be successfully persisted.
     */
    @Test
    @DisplayName("Should save new category successfully")
    void testSaveCategory() {
        Category newCategory = new Category();
        newCategory.setName("Books");
        newCategory.setDescription("Book items");
        newCategory.setIsActive(true);

        Category saved = categoryRepository.save(newCategory);

        assertThat(saved).isNotNull();
        assertThat(saved.getCategoryId()).isNotNull();
        assertThat(saved.getName()).isEqualTo("Books");
    }

    /**
     * Test updating an existing category.
     * Verifies that category details can be updated.
     */
    @Test
    @DisplayName("Should update existing category")
    void testUpdateCategory() {
        rootCategory.setDescription("Updated description");
        Category updated = categoryRepository.save(rootCategory);

        assertThat(updated.getDescription()).isEqualTo("Updated description");
        assertThat(updated.getCategoryId()).isEqualTo(rootCategory.getCategoryId());
    }

    /**
     * Test deleting a category by ID.
     * Verifies that a category can be successfully deleted.
     */
    @Test
    @DisplayName("Should delete category by ID")
    void testDeleteCategory() {
        Long categoryId = inactiveCategory.getCategoryId();
        categoryRepository.deleteById(categoryId);
        entityManager.flush();

        Optional<Category> result = categoryRepository.findById(categoryId);
        assertThat(result).isEmpty();
    }

    /**
     * Test finding all categories.
     * Verifies that all categories can be retrieved.
     */
    @Test
    @DisplayName("Should find all categories")
    void testFindAll() {
        List<Category> allCategories = categoryRepository.findAll();

        assertThat(allCategories).hasSize(3);
    }
}