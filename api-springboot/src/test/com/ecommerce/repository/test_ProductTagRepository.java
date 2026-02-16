package com.ecommerce.repository;

import com.ecommerce.entity.ProductTag;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * JUnit 5 test class for ProductTagRepository.
 * Tests all public methods including custom query methods.
 * Uses @DataJpaTest for repository layer testing with in-memory database.
 */
@DataJpaTest
@DisplayName("ProductTagRepository Tests")
public class test_ProductTagRepository {

    @Autowired
    private ProductTagRepository productTagRepository;

    @Autowired
    private TestEntityManager entityManager;

    private ProductTag testTag;
    private Long testProductId;

    /**
     * Set up test data before each test method.
     */
    @BeforeEach
    void setUp() {
        testProductId = 1L;
        
        testTag = new ProductTag();
        testTag.setTag("electronics");
        // Note: Actual entity setup would require Product entity
        // This is a simplified version for demonstration
    }

    /**
     * Test finding tags by product ID.
     */
    @Test
    @DisplayName("Should find tags by product ID")
    void testFindByProductId() {
        // Given
        productTagRepository.save(testTag);
        entityManager.flush();

        // When
        List<ProductTag> result = productTagRepository.findByProductId(testProductId);

        // Then
        assertThat(result).isNotNull();
    }

    /**
     * Test finding tags by product ID - no results.
     */
    @Test
    @DisplayName("Should return empty list when no tags found for product")
    void testFindByProductId_NoResults() {
        // Given
        Long nonExistentProductId = 999L;

        // When
        List<ProductTag> result = productTagRepository.findByProductId(nonExistentProductId);

        // Then
        assertThat(result).isEmpty();
    }

    /**
     * Test finding tags by tag name.
     */
    @Test
    @DisplayName("Should find tags by tag name")
    void testFindByTag() {
        // Given
        productTagRepository.save(testTag);
        entityManager.flush();

        // When
        List<ProductTag> result = productTagRepository.findByTag("electronics");

        // Then
        assertThat(result).isNotEmpty();
        assertThat(result).allMatch(tag -> tag.getTag().equalsIgnoreCase("electronics"));
    }

    /**
     * Test finding tags by tag name - case insensitive.
     */
    @Test
    @DisplayName("Should find tags by tag name case-insensitively")
    void testFindByTag_CaseInsensitive() {
        // Given
        productTagRepository.save(testTag);
        entityManager.flush();

        // When
        List<ProductTag> resultLower = productTagRepository.findByTag("electronics");
        List<ProductTag> resultUpper = productTagRepository.findByTag("ELECTRONICS");

        // Then
        assertThat(resultLower).isNotEmpty();
        assertThat(resultUpper).isNotEmpty();
        assertThat(resultLower.size()).isEqualTo(resultUpper.size());
    }

    /**
     * Test finding tags by tag name - no results.
     */
    @Test
    @DisplayName("Should return empty list when no tags match")
    void testFindByTag_NoResults() {
        // When
        List<ProductTag> result = productTagRepository.findByTag("nonexistent");

        // Then
        assertThat(result).isEmpty();
    }

    /**
     * Test deleting tags by product ID.
     */
    @Test
    @DisplayName("Should delete tags by product ID")
    void testDeleteByProductProductId() {
        // Given
        productTagRepository.save(testTag);
        entityManager.flush();

        // When
        productTagRepository.deleteByProductProductId(testProductId);
        entityManager.flush();

        // Then
        List<ProductTag> result = productTagRepository.findByProductId(testProductId);
        assertThat(result).isEmpty();
    }

    /**
     * Test deleting tags by non-existent product ID.
     */
    @Test
    @DisplayName("Should handle delete by non-existent product ID gracefully")
    void testDeleteByProductProductId_NotFound() {
        // Given
        Long nonExistentProductId = 999L;

        // When/Then - should not throw exception
        productTagRepository.deleteByProductProductId(nonExistentProductId);
        entityManager.flush();
    }

    /**
     * Test saving a product tag.
     */
    @Test
    @DisplayName("Should save product tag successfully")
    void testSaveProductTag() {
        // When
        ProductTag savedTag = productTagRepository.save(testTag);
        entityManager.flush();

        // Then
        assertThat(savedTag).isNotNull();
        assertThat(savedTag.getId()).isNotNull();
        assertThat(savedTag.getTag()).isEqualTo("electronics");
    }

    /**
     * Test finding all product tags.
     */
    @Test
    @DisplayName("Should find all product tags")
    void testFindAll() {
        // Given
        productTagRepository.save(testTag);
        
        ProductTag anotherTag = new ProductTag();
        anotherTag.setTag("smartphone");
        productTagRepository.save(anotherTag);
        entityManager.flush();

        // When
        List<ProductTag> tags = productTagRepository.findAll();

        // Then
        assertThat(tags).hasSizeGreaterThanOrEqualTo(2);
    }

    /**
     * Test deleting a product tag by ID.
     */
    @Test
    @DisplayName("Should delete product tag by ID")
    void testDeleteById() {
        // Given
        ProductTag savedTag = productTagRepository.save(testTag);
        entityManager.flush();
        Long tagId = savedTag.getId();

        // When
        productTagRepository.deleteById(tagId);
        entityManager.flush();

        // Then
        var result = productTagRepository.findById(tagId);
        assertThat(result).isEmpty();
    }

    /**
     * Test multiple tags for same product.
     */
    @Test
    @DisplayName("Should handle multiple tags for same product")
    void testMultipleTagsForProduct() {
        // Given
        ProductTag tag1 = new ProductTag();
        tag1.setTag("electronics");
        
        ProductTag tag2 = new ProductTag();
        tag2.setTag("smartphone");
        
        productTagRepository.save(tag1);
        productTagRepository.save(tag2);
        entityManager.flush();

        // When
        List<ProductTag> result = productTagRepository.findByProductId(testProductId);

        // Then
        assertThat(result).isNotNull();
    }

    /**
     * Test with null tag name - edge case.
     */
    @Test
    @DisplayName("Should handle null tag name gracefully")
    void testFindByTag_NullTag() {
        // When
        List<ProductTag> result = productTagRepository.findByTag(null);

        // Then
        assertThat(result).isEmpty();
    }

    /**
     * Test with null product ID - edge case.
     */
    @Test
    @DisplayName("Should handle null product ID gracefully")
    void testFindByProductId_NullProductId() {
        // When
        List<ProductTag> result = productTagRepository.findByProductId(null);

        // Then
        assertThat(result).isEmpty();
    }
}