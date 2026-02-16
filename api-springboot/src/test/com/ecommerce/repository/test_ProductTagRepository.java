package com.ecommerce.repository;

import com.ecommerce.entity.Product;
import com.ecommerce.entity.ProductTag;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * JUnit test class for ProductTagRepository.
 * Tests repository methods for ProductTag entity operations including custom queries.
 * Uses in-memory database for testing without affecting production data.
 *
 * @author QA Automation Team
 * @version 1.0
 */
@DataJpaTest
@ActiveProfiles("test")
@DisplayName("ProductTag Repository Tests")
class test_ProductTagRepository {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private ProductTagRepository productTagRepository;

    private Product testProduct;
    private ProductTag testTag1;
    private ProductTag testTag2;

    /**
     * Set up test data before each test method execution.
     * Creates and persists test products and tags.
     */
    @BeforeEach
    void setUp() {
        testProduct = new Product();
        testProduct.setProductId(UUID.randomUUID());
        testProduct.setName("Test Product");
        testProduct.setDescription("Test Description");
        testProduct.setPrice(BigDecimal.valueOf(99.99));
        entityManager.persist(testProduct);

        testTag1 = new ProductTag();
        testTag1.setProduct(testProduct);
        testTag1.setTag("electronics");
        entityManager.persist(testTag1);

        testTag2 = new ProductTag();
        testTag2.setProduct(testProduct);
        testTag2.setTag("featured");
        entityManager.persist(testTag2);

        entityManager.flush();
    }

    /**
     * Test finding ProductTags by product ID.
     * Verifies that the custom query returns all tags for a specific product.
     */
    @Test
    @DisplayName("Should find ProductTags by product ID")
    void testFindByProductId_Success() {
        // When
        List<ProductTag> result = productTagRepository.findByProductId(testProduct.getProductId());

        // Then
        assertThat(result).isNotEmpty();
        assertThat(result).hasSize(2);
        assertThat(result).extracting(ProductTag::getTag)
                .containsExactlyInAnyOrder("electronics", "featured");
    }

    /**
     * Test finding ProductTags with non-existent product ID.
     * Verifies that an empty list is returned when product has no tags.
     */
    @Test
    @DisplayName("Should return empty list when product ID has no tags")
    void testFindByProductId_NotFound() {
        // Given
        Long nonExistentProductId = 99999L;

        // When
        List<ProductTag> result = productTagRepository.findByProductId(nonExistentProductId);

        // Then
        assertThat(result).isEmpty();
    }

    /**
     * Test finding ProductTags by tag name.
     * Verifies that the custom query returns all products with a specific tag.
     */
    @Test
    @DisplayName("Should find ProductTags by tag name")
    void testFindByTag_Success() {
        // When
        List<ProductTag> result = productTagRepository.findByTag("electronics");

        // Then
        assertThat(result).isNotEmpty();
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTag()).isEqualToIgnoringCase("electronics");
    }

    /**
     * Test finding ProductTags by tag name case-insensitively.
     * Verifies that the search is case-insensitive.
     */
    @Test
    @DisplayName("Should find ProductTags by tag name case-insensitively")
    void testFindByTag_CaseInsensitive() {
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
     * Test finding ProductTags with non-existent tag.
     * Verifies that an empty list is returned when tag doesn't exist.
     */
    @Test
    @DisplayName("Should return empty list when tag does not exist")
    void testFindByTag_NotFound() {
        // When
        List<ProductTag> result = productTagRepository.findByTag("nonexistent");

        // Then
        assertThat(result).isEmpty();
    }

    /**
     * Test deleting ProductTags by product ID.
     * Verifies that all tags for a product are deleted.
     */
    @Test
    @DisplayName("Should delete ProductTags by product ID")
    @Transactional
    void testDeleteByProductProductId_Success() {
        // Given
        Long productId = testProduct.getProductId();

        // When
        productTagRepository.deleteByProductProductId(productId);
        entityManager.flush();

        // Then
        List<ProductTag> result = productTagRepository.findByProductId(productId);
        assertThat(result).isEmpty();
    }

    /**
     * Test deleting ProductTags with non-existent product ID.
     * Verifies that the delete operation handles non-existent product gracefully.
     */
    @Test
    @DisplayName("Should handle delete by non-existent product ID gracefully")
    @Transactional
    void testDeleteByProductProductId_NotFound() {
        // Given
        Long nonExistentProductId = 99999L;
        long initialCount = productTagRepository.count();

        // When
        productTagRepository.deleteByProductProductId(nonExistentProductId);
        entityManager.flush();

        // Then
        long finalCount = productTagRepository.count();
        assertThat(finalCount).isEqualTo(initialCount);
    }

    /**
     * Test saving a new ProductTag.
     * Verifies that the repository can persist a new ProductTag entity.
     */
    @Test
    @DisplayName("Should save a new ProductTag successfully")
    void testSave_NewTag() {
        // Given
        ProductTag newTag = new ProductTag();
        newTag.setProduct(testProduct);
        newTag.setTag("new-arrival");

        // When
        ProductTag savedTag = productTagRepository.save(newTag);

        // Then
        assertThat(savedTag).isNotNull();
        assertThat(savedTag.getId()).isNotNull();
        assertThat(savedTag.getTag()).isEqualTo("new-arrival");
    }

    /**
     * Test updating an existing ProductTag.
     * Verifies that the repository can update ProductTag properties.
     */
    @Test
    @DisplayName("Should update existing ProductTag successfully")
    void testSave_UpdateTag() {
        // Given
        testTag1.setTag("updated-electronics");

        // When
        ProductTag updatedTag = productTagRepository.save(testTag1);

        // Then
        assertThat(updatedTag.getTag()).isEqualTo("updated-electronics");
    }

    /**
     * Test finding a ProductTag by ID.
     * Verifies that the repository can retrieve a ProductTag by its primary key.
     */
    @Test
    @DisplayName("Should find ProductTag by ID")
    void testFindById_Success() {
        // When
        Optional<ProductTag> result = productTagRepository.findById(testTag1.getId());

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(testTag1.getId());
    }

    /**
     * Test deleting a ProductTag.
     * Verifies that the repository can delete a ProductTag entity.
     */
    @Test
    @DisplayName("Should delete ProductTag successfully")
    void testDelete_Success() {
        // Given
        Long tagId = testTag1.getId();

        // When
        productTagRepository.delete(testTag1);
        entityManager.flush();

        // Then
        Optional<ProductTag> result = productTagRepository.findById(tagId);
        assertThat(result).isEmpty();
    }

    /**
     * Test counting all ProductTags.
     * Verifies that the repository can count the total number of ProductTags.
     */
    @Test
    @DisplayName("Should count all ProductTags")
    void testCount_Success() {
        // When
        long count = productTagRepository.count();

        // Then
        assertThat(count).isEqualTo(2);
    }

    /**
     * Test checking if ProductTag exists by ID.
     * Verifies that the repository can check existence of a ProductTag.
     */
    @Test
    @DisplayName("Should return true when ProductTag exists")
    void testExistsById_True() {
        // When
        boolean exists = productTagRepository.existsById(testTag1.getId());

        // Then
        assertThat(exists).isTrue();
    }

    /**
     * Test checking if ProductTag exists with non-existent ID.
     * Verifies that the repository returns false for non-existent ProductTag.
     */
    @Test
    @DisplayName("Should return false when ProductTag does not exist")
    void testExistsById_False() {
        // Given
        Long nonExistentId = 99999L;

        // When
        boolean exists = productTagRepository.existsById(nonExistentId);

        // Then
        assertThat(exists).isFalse();
    }

    /**
     * Test finding tags for multiple products.
     * Verifies that tags are correctly associated with their respective products.
     */
    @Test
    @DisplayName("Should find tags for multiple products")
    void testFindByProductId_MultipleProducts() {
        // Given
        Product product2 = new Product();
        product2.setProductId(UUID.randomUUID());
        product2.setName("Product 2");
        product2.setDescription("Description 2");
        product2.setPrice(BigDecimal.valueOf(149.99));
        entityManager.persist(product2);

        ProductTag tag3 = new ProductTag();
        tag3.setProduct(product2);
        tag3.setTag("premium");
        entityManager.persist(tag3);
        entityManager.flush();

        // When
        List<ProductTag> tagsProduct1 = productTagRepository.findByProductId(testProduct.getProductId());
        List<ProductTag> tagsProduct2 = productTagRepository.findByProductId(product2.getProductId());

        // Then
        assertThat(tagsProduct1).hasSize(2);
        assertThat(tagsProduct2).hasSize(1);
        assertThat(tagsProduct2.get(0).getTag()).isEqualTo("premium");
    }

    /**
     * Test finding multiple products with the same tag.
     * Verifies that the same tag can be associated with multiple products.
     */
    @Test
    @DisplayName("Should find multiple products with the same tag")
    void testFindByTag_MultipleProducts() {
        // Given
        Product product2 = new Product();
        product2.setProductId(UUID.randomUUID());
        product2.setName("Product 2");
        product2.setDescription("Description 2");
        product2.setPrice(BigDecimal.valueOf(149.99));
        entityManager.persist(product2);

        ProductTag tag3 = new ProductTag();
        tag3.setProduct(product2);
        tag3.setTag("electronics");
        entityManager.persist(tag3);
        entityManager.flush();

        // When
        List<ProductTag> result = productTagRepository.findByTag("electronics");

        // Then
        assertThat(result).hasSize(2);
    }

    /**
     * Test saving ProductTag with empty tag.
     * Verifies that the repository can handle edge case of empty tag.
     */
    @Test
    @DisplayName("Should save ProductTag with empty tag")
    void testSave_EmptyTag() {
        // Given
        ProductTag emptyTag = new ProductTag();
        emptyTag.setProduct(testProduct);
        emptyTag.setTag("");

        // When
        ProductTag savedTag = productTagRepository.save(emptyTag);

        // Then
        assertThat(savedTag).isNotNull();
        assertThat(savedTag.getTag()).isEmpty();
    }

    /**
     * Test finding all ProductTags.
     * Verifies that the repository can retrieve all ProductTags.
     */
    @Test
    @DisplayName("Should find all ProductTags")
    void testFindAll_Success() {
        // When
        List<ProductTag> result = productTagRepository.findAll();

        // Then
        assertThat(result).hasSize(2);
        assertThat(result).extracting(ProductTag::getTag)
                .containsExactlyInAnyOrder("electronics", "featured");
    }
}