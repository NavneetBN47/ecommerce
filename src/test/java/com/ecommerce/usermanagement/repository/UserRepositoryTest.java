package com.ecommerce.usermanagement.repository;

import com.ecommerce.usermanagement.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class UserRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private UserRepository userRepository;

    private User user;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setEmail("test@example.com");
        user.setPassword("encoded-password");
        user.setFirstName("John");
        user.setLastName("Doe");
    }

    @Test
    @DisplayName("Find By Email - Success")
    void testFindByEmail_Success() {
        entityManager.persist(user);
        entityManager.flush();

        Optional<User> found = userRepository.findByEmail("test@example.com");

        assertTrue(found.isPresent());
        assertEquals("test@example.com", found.get().getEmail());
    }

    @Test
    @DisplayName("Find By Email - Not Found")
    void testFindByEmail_NotFound() {
        Optional<User> found = userRepository.findByEmail("nonexistent@example.com");

        assertFalse(found.isPresent());
    }

    @Test
    @DisplayName("Exists By Email - True")
    void testExistsByEmail_True() {
        entityManager.persist(user);
        entityManager.flush();

        boolean exists = userRepository.existsByEmail("test@example.com");

        assertTrue(exists);
    }

    @Test
    @DisplayName("Exists By Email - False")
    void testExistsByEmail_False() {
        boolean exists = userRepository.existsByEmail("nonexistent@example.com");

        assertFalse(exists);
    }
}
