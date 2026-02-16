package com.ecommerce.repository;

import com.ecommerce.entity.Cart;
import com.ecommerce.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * JUnit 5 test class for CartRepository.
 * Tests repository methods for Cart entity operations including user-specific queries.
 * Uses @DataJpaTest for repository layer testing with in-memory database.
 * 
 * @author Test Generation Agent
 * @version 1.0
 */
@DataJpaTest
@ActiveProfiles("test")
@DisplayName("CartRepository Tests")
public class test_CartRepository {

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private TestEntityManager entityManager;

    private User testUser;
    private Cart testCart;
    private UUID userId;
    private UUID cartId;

    /**
     * Set up test data before each test method execution.
     * Creates and persists test entities for User and Cart.
     */
    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        cartId = UUID.randomUUID();

        testUser = new User();
        testUser.setId(userId);
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
        entityManager.persist(testUser);

        testCart = new Cart();
        testCart.setId(cartId);
        testCart.setUserId(userId);
        entityManager.persist(testCart);
        
        entityManager.flush();
    }

    /**
     * Test finding a Cart by user ID when it exists.
     * Verifies that the correct Cart is returned for the given user.
     */
    @Test
    @DisplayName("Should find Cart by user ID when exists")
    void testFindByUserId_WhenExists_ReturnsCart() {
        // When
        Optional<Cart> result = cartRepository.findByUserId(userId);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getUserId()).isEqualTo(userId);
        assertThat(result.get().getId()).isEqualTo(cartId);
    }

    /**
     * Test finding a Cart by user ID when it does not exist.
     * Verifies that an empty Optional is returned.
     */
    @Test
    @DisplayName("Should return empty Optional when Cart does not exist for user")
    void testFindByUserId_WhenNotExists_ReturnsEmpty() {
        // Given
        UUID nonExistentUserId = UUID.randomUUID();

        // When
        Optional<Cart> result = cartRepository.findByUserId(nonExistentUserId);

        // Then
        assertThat(result).isEmpty();
    }

    /**
     * Test deleting a Cart by user ID.
     * Verifies that the Cart is removed from the database.
     */
    @Test
    @DisplayName("Should delete Cart by user ID successfully")
    void testDeleteByUserId_Success() {
        // When
        cartRepository.deleteByUserId(userId);
        entityManager.flush();

        // Then
        Optional<Cart> result = cartRepository.findByUserId(userId);
        assertThat(result).isEmpty();
    }

    /**
     * Test deleting a Cart by user ID when no cart exists.
     * Verifies that no exception is thrown and operation completes successfully.
     */
    @Test
    @DisplayName("Should handle delete by user ID when Cart does not exist")
    void testDeleteByUserId_WhenNotExists_NoException() {
        // Given
        UUID nonExistentUserId = UUID.randomUUID();

        // When & Then - should not throw exception
        cartRepository.deleteByUserId(nonExistentUserId);
        entityManager.flush();
    }

    /**
     * Test saving a new Cart.
     * Verifies that the Cart is persisted correctly with all attributes.
     */
    @Test
    @DisplayName("Should save new Cart successfully")
    void testSave_NewCart_Success() {
        // Given
        UUID newUserId = UUID.randomUUID();
        User newUser = new User();
        newUser.setId(newUserId);
        newUser.setUsername("newuser");
        newUser.setEmail("newuser@example.com");
        entityManager.persist(newUser);

        Cart newCart = new Cart();
        newCart.setUserId(newUserId);

        // When
        Cart savedCart = cartRepository.save(newCart);

        // Then
        assertThat(savedCart).isNotNull();
        assertThat(savedCart.getId()).isNotNull();
        assertThat(savedCart.getUserId()).isEqualTo(newUserId);
    }

    /**
     * Test updating an existing Cart.
     * Verifies that Cart modifications are persisted correctly.
     */
    @Test
    @DisplayName("Should update existing Cart successfully")
    void testSave_UpdateCart_Success() {
        // Given
        testCart.setUserId(userId);

        // When
        Cart updatedCart = cartRepository.save(testCart);
        entityManager.flush();

        // Then
        assertThat(updatedCart.getId()).isEqualTo(cartId);
        assertThat(updatedCart.getUserId()).isEqualTo(userId);
    }

    /**
     * Test finding a Cart by ID.
     * Verifies that the correct Cart is retrieved by its ID.
     */
    @Test
    @DisplayName("Should find Cart by ID when exists")
    void testFindById_WhenExists_ReturnsCart() {
        // When
        Optional<Cart> result = cartRepository.findById(cartId);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(cartId);
        assertThat(result.get().getUserId()).isEqualTo(userId);
    }

    /**
     * Test finding a Cart by ID when it does not exist.
     * Verifies that an empty Optional is returned.
     */
    @Test
    @DisplayName("Should return empty Optional when Cart ID does not exist")
    void testFindById_WhenNotExists_ReturnsEmpty() {
        // Given
        UUID nonExistentCartId = UUID.randomUUID();

        // When
        Optional<Cart> result = cartRepository.findById(nonExistentCartId);

        // Then
        assertThat(result).isEmpty();
    }

    /**
     * Test deleting a Cart by ID.
     * Verifies that the Cart is removed from the database.
     */
    @Test
    @DisplayName("Should delete Cart by ID successfully")
    void testDeleteById_Success() {
        // When
        cartRepository.deleteById(cartId);
        entityManager.flush();

        // Then
        Optional<Cart> result = cartRepository.findById(cartId);
        assertThat(result).isEmpty();
    }

    /**
     * Test that multiple carts can exist but only one per user.
     * Verifies user-cart relationship integrity.
     */
    @Test
    @DisplayName("Should maintain one cart per user relationship")
    void testOneCartPerUser() {
        // Given
        UUID anotherUserId = UUID.randomUUID();
        User anotherUser = new User();
        anotherUser.setId(anotherUserId);
        anotherUser.setUsername("anotheruser");
        anotherUser.setEmail("another@example.com");
        entityManager.persist(anotherUser);

        Cart anotherCart = new Cart();
        anotherCart.setUserId(anotherUserId);
        entityManager.persist(anotherCart);
        entityManager.flush();

        // When
        Optional<Cart> userCart1 = cartRepository.findByUserId(userId);
        Optional<Cart> userCart2 = cartRepository.findByUserId(anotherUserId);

        // Then
        assertThat(userCart1).isPresent();
        assertThat(userCart2).isPresent();
        assertThat(userCart1.get().getUserId()).isEqualTo(userId);
        assertThat(userCart2.get().getUserId()).isEqualTo(anotherUserId);
        assertThat(userCart1.get().getId()).isNotEqualTo(userCart2.get().getId());
    }
}