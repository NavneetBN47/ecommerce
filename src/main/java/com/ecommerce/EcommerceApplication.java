package com.ecommerce;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Main Spring Boot Application Class for E-Commerce Backend
 * 
 * Features:
 * - RESTful API for e-commerce operations
 * - JWT-based authentication and authorization
 * - Shopping cart management with lifecycle handling
 * - Order processing with stock management
 * - Product catalog with search capabilities
 * - User management with role-based access control
 * 
 * @author E-Commerce Development Team
 * @version 1.0.0
 */
@SpringBootApplication
@EnableJpaAuditing
@EnableScheduling
public class EcommerceApplication {

    public static void main(String[] args) {
        SpringApplication.run(EcommerceApplication.class, args);
    }
}