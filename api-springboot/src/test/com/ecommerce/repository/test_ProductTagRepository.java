package com.ecommerce.repository;

import com.ecommerce.entity.ProductTag;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * JUnit 5 test class for ProductTagRepository.
 * Tests repository methods for ProductTag entity operations including custom queries.
 * Uses @DataJpaTest for repository layer testing with in-memory database.
 */
@DataJpaTest
@DisplayName("ProductTagRepository Tests")
public class test_ProductTagRepository {

    @Autowired
    private ProductTagRepository productTagRepository;

    @Autowired
    private TestEntityManager entityManager;

    private ProductTag testProductTag;
    private Long testProductId;

    /**
     * Set up test data before each test method.
     */
    @BeforeEach
    void setUp() {
        testProductId = 1L;
        testProductTag = new ProductTag();
        testProductTag.setTag("electronics");
    }

    /**
     * Test finding product tags by product ID.
     * Verifies that the repository correctly retrieves tags for a specific product.
     */
    @Test
    @DisplayName("Should find product tags by product ID")
    void testFindByProductId() {
        // Given
        entityManager.persistAndFlush(testProductTag);

        // When
        List<ProductTag> results = productTagRepository.findByProductId(testProductId);

        // Then
        assertThat(results).isNotNull();
    }

    /**
     * Test finding product tags by product ID when no tags exist.
     * Verifies that the repository returns empty list for product without tags.
     */
    @Test
    @DisplayName("Should return empty list when product has no tags")
    void testFindByProductId_WhenNoTags() {
        // When
        List<ProductTag> results = productTagRepository.findByProductId(999L);

        // Then
        assertThat(results).isEmpty();
    }

    /**
     * Test finding product tags by tag name.
     * Verifies that the repository correctly retrieves tags by tag name.
     */
    @Test
    @DisplayName("Should find product tags by tag name")
    void testFindByTag() {
        // Given
        entityManager.persistAndFlush(testProductTag);

        // When
        List<ProductTag> results = productTagRepository.findByTag("electronics");

        // Then
        assertThat(results).isNotEmpty();
        assertThat(results).allMatch(tag -> tag.getTag().equalsIgnoreCase("electronics"));
    }

    /**
     * Test finding product tags by tag name case-insensitively.
     * Verifies that the tag search is case-insensitive.
     */
    @Test
    @DisplayName("Should find product tags case-insensitively")
    void testFindByTag_CaseInsensitive() {
        // Given
        entityManager.persistAndFlush(testProductTag);

        // When
        List<ProductTag> results = productTagRepository.findByTag("ELECTRONICS");

        // Then
        assertThat(results).isNotEmpty();
    }

    /**
     * Test finding product tags by non-existent tag.
     * Verifies that the repository returns empty list for non-existent tag.
     */
    @Test
    @DisplayName("Should return empty list when tag does not exist")
    void testFindByTag_WhenNotExists() {
        // When
        List<ProductTag> results = productTagRepository.findByTag("nonexistent");

        // Then
        assertThat(results).isEmpty();
    }

    /**
     * Test deleting product tags by product ID.
     * Verifies that the repository correctly deletes all tags for a product.
     */
    @Test
    @Transactional
    @DisplayName("Should delete product tags by product ID")
    void testDeleteByProductProductId() {
        // Given
        ProductTag savedTag = entityManager.persistAndFlush(testProductTag);

        // When
        productTagRepository.deleteByProductProductId(testProductId);
        entityManager.flush();

        // Then
        List<ProductTag> results = productTagRepository.findByProductId(testProductId);
        assertThat(results).isEmpty();
    }

    /**
     * Test saving a product tag.
     * Verifies that the repository correctly persists a product tag.
     */
    @Test
    @DisplayName("Should save product tag successfully")
    void testSave_ProductTag() {
        // When
        ProductTag savedTag = productTagRepository.save(testProductTag);

        // Then
        assertThat(savedTag).isNotNull();
        assertThat(savedTag.getId()).isNotNull();
        assertThat(savedTag.getTag()).isEqualTo("electronics");
    }

    /**
     * Test finding all product tags.
     * Verifies that the repository correctly retrieves all product tags.
     */
    @Test
    @DisplayName("Should find all product tags")
    void testFindAll_ProductTags() {
        // Given
        entityManager.persistAndFlush(testProductTag);
        ProductTag anotherTag = new ProductTag();
        anotherTag.setTag("gadgets");
        entityManager.persistAndFlush(anotherTag);

        // When
        List<ProductTag> allTags = productTagRepository.findAll();

        // Then
        assertThat(allTags).hasSizeGreaterThanOrEqualTo(2);
    }

    /**
     * Test deleting a product tag.
     * Verifies that the repository correctly deletes a product tag.
     */
    @Test
    @DisplayName("Should delete product tag successfully")
    void testDelete_ProductTag() {
        // Given
        ProductTag savedTag = entityManager.persistAndFlush(testProductTag);
        Long tagId = savedTag.getId();

        // When
        productTagRepository.delete(savedTag);
        entityManager.flush();

        // Then
        var result = productTagRepository.findById(tagId);
        assertThat(result).isEmpty();
    }

    /**
     * Test counting product tags.
     * Verifies that the repository correctly counts product tags.
     */
    @Test
    @DisplayName("Should count product tags")
    void testCount_ProductTags() {
        // Given
        entityManager.persistAndFlush(testProductTag);
        ProductTag anotherTag = new ProductTag();
        anotherTag.setTag("technology");
        entityManager.persistAndFlush(anotherTag);

        // When
        long count = productTagRepository.count();

        // Then
        assertThat(count).isGreaterThanOrEqualTo(2);
    }
}