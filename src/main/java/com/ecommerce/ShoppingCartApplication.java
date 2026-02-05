package com.ecommerce;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * Main Spring Boot Application for Shopping Cart System
 * Implements SCRUM-96 requirements
 */
@SpringBootApplication
@EnableJpaAuditing
@EnableTransactionManagement
public class ShoppingCartApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(ShoppingCartApplication.class, args);
    }
}