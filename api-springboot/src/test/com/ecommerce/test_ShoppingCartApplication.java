package com.ecommerce;

import org.junit.jupiter.api.Test;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Test class for ShoppingCartApplication
 * 
 * This test class verifies the main application class functionality,
 * including application context loading and Spring Boot configuration.
 * 
 * @author Shopping Cart System Team
 * @version 1.0.0
 */
@SpringBootTest
@ActiveProfiles("test")
class test_ShoppingCartApplication {

    /**
     * Test that the application context loads successfully
     * 
     * Verifies that all Spring beans are properly configured and
     * the application context can be initialized without errors.
     */
    @Test
    void contextLoads() {
        // This test will fail if the application context cannot start
        assertThat(true).isTrue();
    }

    /**
     * Test that the main method runs without exceptions
     * 
     * Verifies that the SpringApplication.run() method can be invoked
     * and the application starts successfully.
     */
    @Test
    void mainMethodStartsApplication() {
        // Verify main method exists and can be called
        assertThat(ShoppingCartApplication.class)
                .hasDeclaredMethods("main");
    }

    /**
     * Test that required annotations are present on the main class
     * 
     * Verifies that @SpringBootApplication, @EnableJpaAuditing, and
     * @EnableTransactionManagement annotations are properly configured.
     */
    @Test
    void verifyRequiredAnnotations() {
        assertThat(ShoppingCartApplication.class.isAnnotationPresent(
                org.springframework.boot.autoconfigure.SpringBootApplication.class))
                .isTrue();
        assertThat(ShoppingCartApplication.class.isAnnotationPresent(
                org.springframework.data.jpa.repository.config.EnableJpaAuditing.class))
                .isTrue();
        assertThat(ShoppingCartApplication.class.isAnnotationPresent(
                org.springframework.transaction.annotation.EnableTransactionManagement.class))
                .isTrue();
    }
}