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
 * Tests all public methods including custom query methods.
 * Uses @DataJpaTest for repository layer testing with in-memory database.
 */
@DataJpaTest
@ActiveProfiles("test")
@DisplayName("ProductTagRepository Tests")
class test_ProductTagRepository {

    @Autowired
    private ProductTagRepository productTagRepository;

    @Autowired
    private TestEntityManager entityManager;

    private ProductTag testTag1;
    private ProductTag testTag2;
    private ProductTag testTag3;
    private Long testProductId;

    /**
     * Set up test data before each test method execution.
     */
    @BeforeEach
    void setUp() {
        testProductId = 1L;
        
        testTag1 = new ProductTag();
        testTag1.setTag("electronics");
        testTag1.setProduct(createMockProduct(testProductId));
        
        testTag2 = new ProductTag();
        testTag2.setTag("laptop");
        testTag2.setProduct(createMockProduct(testProductId));
        
        testTag3 = new ProductTag();
        testTag3.setTag("electronics");
        testTag3.setProduct(createMockProduct(2L));
    }

    /**
     * Test finding product tags by product ID.
     * Verifies that all tags for the specified product are returned.
     */
    @Test
    @DisplayName("Should find product tags by product ID")
    void testFindByProductId() {
        // Given
        entityManager.persist(testTag1);
        entityManager.persist(testTag2);
        entityManager.persist(testTag3);
        entityManager.flush();

        // When
        List<ProductTag> result = productTagRepository.findByProductId(testProductId);

        // Then
        assertThat(result).hasSize(2);
        assertThat(result).extracting(ProductTag::getTag)
            .containsExactlyInAnyOrder("electronics", "laptop");
    }

    /**
     * Test finding product tags by product ID when no tags exist.
     * Verifies that an empty list is returned.
     */
    @Test
    @DisplayName("Should return empty list when no tags exist for product ID")
    void testFindByProductId_WhenNoTags() {
        // Given
        Long nonExistentProductId = 999L;

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
    void testFindByTag() {
        // Given
        entityManager.persist(testTag1);
        entityManager.persist(testTag2);
        entityManager.persist(testTag3);
        entityManager.flush();

        // When
        List<ProductTag> result = productTagRepository.findByTag("electronics");

        // Then
        assertThat(result).hasSize(2);
        assertThat(result).allMatch(tag -> tag.getTag().equalsIgnoreCase("electronics"));
    }

    /**
     * Test finding product tags by tag name with case-insensitive search.
     * Verifies that search is case-insensitive.
     */
    @Test
    @DisplayName("Should find product tags by tag name case-insensitively")
    void testFindByTag_CaseInsensitive() {
        // Given
        entityManager.persist(testTag1);
        entityManager.flush();

        // When
        List<ProductTag> resultLower = productTagRepository.findByTag("electronics");
        List<ProductTag> resultUpper = productTagRepository.findByTag("ELECTRONICS");
        List<ProductTag> resultMixed = productTagRepository.findByTag("ElEcTrOnIcS");

        // Then
        assertThat(resultLower).hasSize(1);
        assertThat(resultUpper).hasSize(1);
        assertThat(resultMixed).hasSize(1);
    }

    /**
     * Test finding product tags by tag name when tag doesn't exist.
     * Verifies that an empty list is returned.
     */
    @Test
    @DisplayName("Should return empty list when tag not found")
    void testFindByTag_WhenNotExists() {
        // Given
        entityManager.persist(testTag1);
        entityManager.flush();

        // When
        List<ProductTag> result = productTagRepository.findByTag("nonexistent");

        // Then
        assertThat(result).isEmpty();
    }

    /**
     * Test deleting product tags by product ID.
     * Verifies that all tags for the specified product are removed.
     */
    @Test
    @DisplayName("Should delete product tags by product ID")
    void testDeleteByProductProductId() {
        // Given
        entityManager.persist(testTag1);
        entityManager.persist(testTag2);
        entityManager.persist(testTag3);
        entityManager.flush();
        entityManager.clear();

        // When
        productTagRepository.deleteByProductProductId(testProductId);
        entityManager.flush();

        // Then
        List<ProductTag> remainingTags = productTagRepository.findByProductId(testProductId);
        assertThat(remainingTags).isEmpty();
        
        // Verify tags for other products still exist
        List<ProductTag> otherProductTags = productTagRepository.findByProductId(2L);
        assertThat(otherProductTags).hasSize(1);
    }

    /**
     * Test deleting product tags by product ID when no tags exist.
     * Verifies that no exception is thrown.
     */
    @Test
    @DisplayName("Should handle delete by product ID when no tags exist")
    void testDeleteByProductProductId_WhenNoTags() {
        // Given
        Long nonExistentProductId = 999L;

        // When & Then - should not throw exception
        productTagRepository.deleteByProductProductId(nonExistentProductId);
        entityManager.flush();
    }

    /**
     * Test saving a new product tag.
     * Verifies that the tag is persisted with generated ID.
     */
    @Test
    @DisplayName("Should save new product tag successfully")
    void testSave_NewTag() {
        // When
        ProductTag savedTag = productTagRepository.save(testTag1);

        // Then
        assertThat(savedTag).isNotNull();
        assertThat(savedTag.getId()).isNotNull();
        assertThat(savedTag.getTag()).isEqualTo("electronics");
    }

    /**
     * Test updating an existing product tag.
     * Verifies that changes are persisted correctly.
     */
    @Test
    @DisplayName("Should update existing product tag successfully")
    void testSave_UpdateTag() {
        // Given
        ProductTag savedTag = entityManager.persist(testTag1);
        entityManager.flush();
        Long savedId = savedTag.getId();

        // When
        savedTag.setTag("updated-tag");
        ProductTag updatedTag = productTagRepository.save(savedTag);

        // Then
        assertThat(updatedTag.getId()).isEqualTo(savedId);
        assertThat(updatedTag.getTag()).isEqualTo("updated-tag");
    }

    /**
     * Test finding product tag by ID.
     * Verifies that the correct tag is retrieved.
     */
    @Test
    @DisplayName("Should find product tag by ID when exists")
    void testFindById_WhenExists() {
        // Given
        ProductTag savedTag = entityManager.persist(testTag1);
        entityManager.flush();

        // When
        Optional<ProductTag> result = productTagRepository.findById(savedTag.getId());

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(savedTag.getId());
    }

    /**
     * Test finding product tag by ID when it doesn't exist.
     * Verifies that an empty Optional is returned.
     */
    @Test
    @DisplayName("Should return empty Optional when tag not found by ID")
    void testFindById_WhenNotExists() {
        // Given
        Long nonExistentId = 999L;

        // When
        Optional<ProductTag> result = productTagRepository.findById(nonExistentId);

        // Then
        assertThat(result).isEmpty();
    }

    /**
     * Test deleting a product tag by ID.
     * Verifies that the tag is removed from the database.
     */
    @Test
    @DisplayName("Should delete product tag by ID successfully")
    void testDeleteById() {
        // Given
        ProductTag savedTag = entityManager.persist(testTag1);
        entityManager.flush();
        Long savedId = savedTag.getId();

        // When
        productTagRepository.deleteById(savedId);
        entityManager.flush();

        // Then
        Optional<ProductTag> result = productTagRepository.findById(savedId);
        assertThat(result).isEmpty();
    }

    /**
     * Test finding all product tags.
     * Verifies that all persisted tags are retrieved.
     */
    @Test
    @DisplayName("Should find all product tags")
    void testFindAll() {
        // Given
        entityManager.persist(testTag1);
        entityManager.persist(testTag2);
        entityManager.persist(testTag3);
        entityManager.flush();

        // When
        List<ProductTag> allTags = productTagRepository.findAll();

        // Then
        assertThat(allTags).hasSize(3);
    }

    /**
     * Test checking if product tag exists by ID.
     * Verifies the existence check returns correct boolean value.
     */
    @Test
    @DisplayName("Should return true when tag exists by ID")
    void testExistsById_WhenExists() {
        // Given
        ProductTag savedTag = entityManager.persist(testTag1);
        entityManager.flush();

        // When
        boolean exists = productTagRepository.existsById(savedTag.getId());

        // Then
        assertThat(exists).isTrue();
    }

    /**
     * Test checking if product tag exists by ID when it doesn't.
     * Verifies the existence check returns false.
     */
    @Test
    @DisplayName("Should return false when tag does not exist by ID")
    void testExistsById_WhenNotExists() {
        // Given
        Long nonExistentId = 999L;

        // When
        boolean exists = productTagRepository.existsById(nonExistentId);

        // Then
        assertThat(exists).isFalse();
    }

    /**
     * Helper method to create a mock product for testing.
     */
    private com.ecommerce.entity.Product createMockProduct(Long productId) {
        com.ecommerce.entity.Product product = new com.ecommerce.entity.Product();
        product.setProductId(java.util.UUID.randomUUID());
        return entityManager.persist(product);
    }
}