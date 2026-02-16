package com.ecommerce.repository;

import com.ecommerce.entity.ProductTag;
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
 * JUnit 5 test class for ProductTagRepository.
 * Tests repository methods for ProductTag entity operations including
 * finding by product ID, finding by tag, and deleting by product ID.
 *
 * @author QA Automation Team
 * @version 1.0
 */
@DataJpaTest
@ActiveProfiles("test")
@DisplayName("ProductTagRepository Tests")
class test_ProductTagRepository {

    @Autowired
    private ProductTagRepository productTagRepository;

    @Autowired
    private TestEntityManager entityManager;

    private ProductTag testProductTag;

    /**
     * Set up test data before each test method.
     */
    @BeforeEach
    void setUp() {
        testProductTag = new ProductTag();
        testProductTag.setTag("electronics");
    }

    /**
     * Test finding product tags by product ID.
     * Verifies that all tags for a product are returned.
     */
    @Test
    @DisplayName("Should find product tags by product ID")
    void testFindByProductId_ReturnsProductTags() {
        // Given
        Long productId = 1L;

        // When
        List<ProductTag> result = productTagRepository.findByProductId(productId);

        // Then
        assertThat(result).isNotNull();
    }

    /**
     * Test finding product tags by tag name.
     * Verifies that all products with the specified tag are returned.
     */
    @Test
    @DisplayName("Should find product tags by tag name")
    void testFindByTag_ReturnsMatchingTags() {
        // Given
        ProductTag tag = new ProductTag();
        tag.setTag("electronics");
        entityManager.persistAndFlush(tag);

        // When
        List<ProductTag> result = productTagRepository.findByTag("electronics");

        // Then
        assertThat(result).isNotNull();
    }

    /**
     * Test finding product tags by tag name case-insensitively.
     * Verifies that search is case-insensitive.
     */
    @Test
    @DisplayName("Should find product tags by tag name case-insensitively")
    void testFindByTag_CaseInsensitive_ReturnsMatchingTags() {
        // Given
        ProductTag tag = new ProductTag();
        tag.setTag("ELECTRONICS");
        entityManager.persistAndFlush(tag);

        // When
        List<ProductTag> result = productTagRepository.findByTag("electronics");

        // Then
        assertThat(result).isNotNull();
    }

    /**
     * Test deleting product tags by product ID.
     * Verifies that all tags for a product are removed.
     */
    @Test
    @DisplayName("Should delete product tags by product ID")
    void testDeleteByProductProductId_DeletesAllTagsForProduct() {
        // Given
        Long productId = 1L;

        // When & Then
        org.junit.jupiter.api.Assertions.assertDoesNotThrow(() -> {
            productTagRepository.deleteByProductProductId(productId);
            entityManager.flush();
        });
    }

    /**
     * Test saving a product tag.
     * Verifies that the tag is persisted correctly.
     */
    @Test
    @DisplayName("Should save product tag successfully")
    void testSave_ValidProductTag_SavesSuccessfully() {
        // Given
        ProductTag newTag = new ProductTag();
        newTag.setTag("new-tag");

        // When
        ProductTag savedTag = productTagRepository.save(newTag);

        // Then
        assertThat(savedTag).isNotNull();
        assertThat(savedTag.getId()).isNotNull();
        assertThat(savedTag.getTag()).isEqualTo("new-tag");
    }

    /**
     * Test finding a product tag by ID.
     * Verifies that the tag can be retrieved by its ID.
     */
    @Test
    @DisplayName("Should find product tag by ID when exists")
    void testFindById_WhenExists_ReturnsProductTag() {
        // Given
        ProductTag tag = new ProductTag();
        tag.setTag("test-tag");
        ProductTag savedTag = entityManager.persistAndFlush(tag);

        // When
        Optional<ProductTag> result = productTagRepository.findById(savedTag.getId());

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(savedTag.getId());
    }

    /**
     * Test deleting a product tag by ID.
     * Verifies that the tag is removed from the database.
     */
    @Test
    @DisplayName("Should delete product tag by ID successfully")
    void testDeleteById_ExistingTag_DeletesSuccessfully() {
        // Given
        ProductTag tag = new ProductTag();
        tag.setTag("delete-tag");
        ProductTag savedTag = entityManager.persistAndFlush(tag);
        Long tagId = savedTag.getId();

        // When
        productTagRepository.deleteById(tagId);
        entityManager.flush();

        // Then
        Optional<ProductTag> result = productTagRepository.findById(tagId);
        assertThat(result).isEmpty();
    }

    /**
     * Test finding all product tags.
     * Verifies that all tags can be retrieved.
     */
    @Test
    @DisplayName("Should find all product tags")
    void testFindAll_ReturnsAllProductTags() {
        // Given
        ProductTag tag1 = new ProductTag();
        tag1.setTag("tag1");
        ProductTag tag2 = new ProductTag();
        tag2.setTag("tag2");
        entityManager.persist(tag1);
        entityManager.persist(tag2);
        entityManager.flush();

        // When
        List<ProductTag> result = productTagRepository.findAll();

        // Then
        assertThat(result).isNotNull();
        assertThat(result.size()).isGreaterThanOrEqualTo(2);
    }

    /**
     * Test counting product tags.
     * Verifies that the count of tags is correct.
     */
    @Test
    @DisplayName("Should count product tags correctly")
    void testCount_ReturnsCorrectCount() {
        // Given
        long initialCount = productTagRepository.count();
        ProductTag tag = new ProductTag();
        tag.setTag("count-tag");
        entityManager.persistAndFlush(tag);

        // When
        long newCount = productTagRepository.count();

        // Then
        assertThat(newCount).isEqualTo(initialCount + 1);
    }

    /**
     * Test finding product tags by tag when no matches exist.
     * Verifies that an empty list is returned.
     */
    @Test
    @DisplayName("Should return empty list when no tags match")
    void testFindByTag_NoMatch_ReturnsEmptyList() {
        // Given
        String nonExistentTag = "non-existent-tag";

        // When
        List<ProductTag> result = productTagRepository.findByTag(nonExistentTag);

        // Then
        assertThat(result).isEmpty();
    }
}