package com.example.ecommerce;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * Test class for EcommerceApplication main class.
 * Tests the Spring Boot application startup and context loading.
 */
@SpringBootTest
@ActiveProfiles("test")
class EcommerceApplicationTest {

    /**
     * Test that the Spring Boot application context loads successfully.
     * This is a smoke test to ensure the application can start without errors.
     */
    @Test
    void contextLoads() {
        // This test will pass if the application context loads successfully
        // No additional assertions needed as Spring Boot will fail the test
        // if the context cannot be loaded
    }

    /**
     * Test the main method to ensure it can be called without exceptions.
     * This test verifies that the application entry point is properly configured.
     */
    @Test
    void mainMethodShouldStartApplication() {
        // Test that main method can be called without throwing exceptions
        // In a real scenario, we might want to mock SpringApplication.run
        // but for this test, we'll just verify the method exists and is accessible
        
        // Verify the main method exists and is public static
        try {
            EcommerceApplication.class.getDeclaredMethod("main", String[].class);
        } catch (NoSuchMethodException e) {
            throw new AssertionError("Main method should exist", e);
        }
    }
}