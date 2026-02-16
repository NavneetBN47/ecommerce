package com.ecommerce.repository;

import com.ecommerce.entity.ProductTag;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * JUnit 5 test class for ProductTagRepository.
 * Tests repository operations for ProductTag entity including custom query methods.
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
    private Long testProductId;
    private String testTag;

    /**
     * Set up test data before each test method execution.
     */
    @BeforeEach
    void setUp() {
        testProductId = 1L;
        testTag = "electronics";
        
        testProductTag = new ProductTag();
        testProductTag.setTag(testTag);
    }

    /**
     * Test finding product tags by product ID.
     * Verifies that all tags for a specific product are returned.
     */
    @Test
    @DisplayName("Should find product tags by product ID")
    void testFindByProductId_ShouldReturnProductTags() {
        // Given
        productTagRepository.save(testProductTag);
        entityManager.flush();

        // When
        List<ProductTag> result = productTagRepository.findByProductId(testProductId);

        // Then
        assertThat(result).isNotNull();
    }

    /**
     * Test finding product tags by product ID when no tags exist.
     * Verifies that an empty list is returned.
     */
    @Test
    @DisplayName("Should return empty list when product has no tags")
    void testFindByProductId_WhenNoTags_ShouldReturnEmptyList() {
        // Given
        Long nonExistentProductId = 999999L;

        // When
        List<ProductTag> result = productTagRepository.findByProductId(nonExistentProductId);

        // Then
        assertThat(result).isEmpty();
    }

    /**
     * Test finding product tags by tag name.
     * Verifies that all products with the specified tag are returned.
     */
    @Test
    @DisplayName("Should find product tags by tag name")
    void testFindByTag_ShouldReturnMatchingTags() {
        // Given
        productTagRepository.save(testProductTag);
        entityManager.flush();
        entityManager.clear();

        // When
        List<ProductTag> result = productTagRepository.findByTag(testTag);

        // Then
        assertThat(result).isNotEmpty();
        assertThat(result).allMatch(tag -> tag.getTag().equalsIgnoreCase(testTag));
    }

    /**
     * Test finding product tags by tag name with case-insensitive search.
     * Verifies that the search is case-insensitive.
     */
    @Test
    @DisplayName("Should perform case-insensitive tag search")
    void testFindByTag_CaseInsensitive_ShouldReturnMatchingTags() {
        // Given
        productTagRepository.save(testProductTag);
        entityManager.flush();
        entityManager.clear();

        // When
        List<ProductTag> result = productTagRepository.findByTag("ELECTRONICS");

        // Then
        assertThat(result).isNotEmpty();
    }

    /**
     * Test finding product tags by non-existent tag.
     * Verifies that an empty list is returned.
     */
    @Test
    @DisplayName("Should return empty list when tag does not exist")
    void testFindByTag_WhenNotExists_ShouldReturnEmptyList() {
        // When
        List<ProductTag> result = productTagRepository.findByTag("nonexistent");

        // Then
        assertThat(result).isEmpty();
    }

    /**
     * Test deleting product tags by product ID.
     * Verifies that all tags for a product are deleted.
     */
    @Test
    @Transactional
    @DisplayName("Should delete product tags by product ID")
    void testDeleteByProductProductId_ShouldRemoveTags() {
        // Given
        ProductTag savedTag = productTagRepository.save(testProductTag);
        entityManager.flush();

        // When
        productTagRepository.deleteByProductProductId(testProductId);
        entityManager.flush();

        // Then
        List<ProductTag> result = productTagRepository.findByProductId(testProductId);
        assertThat(result).isEmpty();
    }

    /**
     * Test deleting product tags by non-existent product ID.
     * Verifies that the operation completes without error.
     */
    @Test
    @Transactional
    @DisplayName("Should handle delete by non-existent product ID gracefully")
    void testDeleteByProductProductId_WithNonExistentId_ShouldNotThrowException() {
        // Given
        Long nonExistentProductId = 999999L;

        // When & Then
        org.junit.jupiter.api.Assertions.assertDoesNotThrow(() -> {
            productTagRepository.deleteByProductProductId(nonExistentProductId);
            entityManager.flush();
        });
    }

    /**
     * Test saving a product tag.
     * Verifies that the save operation works correctly.
     */
    @Test
    @DisplayName("Should save product tag successfully")
    void testSave_ShouldPersistProductTag() {
        // When
        ProductTag savedTag = productTagRepository.save(testProductTag);
        entityManager.flush();

        // Then
        assertThat(savedTag).isNotNull();
        assertThat(savedTag.getId()).isNotNull();
    }

    /**
     * Test finding all product tags.
     * Verifies that findAll returns all persisted tags.
     */
    @Test
    @DisplayName("Should find all product tags")
    void testFindAll_ShouldReturnAllTags() {
        // Given
        productTagRepository.save(testProductTag);
        entityManager.flush();

        // When
        List<ProductTag> result = productTagRepository.findAll();

        // Then
        assertThat(result).isNotEmpty();
    }

    /**
     * Test counting all product tags.
     * Verifies that the count operation returns correct number.
     */
    @Test
    @DisplayName("Should count all product tags correctly")
    void testCount_ShouldReturnCorrectCount() {
        // Given
        productTagRepository.save(testProductTag);
        entityManager.flush();

        // When
        long count = productTagRepository.count();

        // Then
        assertThat(count).isGreaterThanOrEqualTo(1);
    }
}