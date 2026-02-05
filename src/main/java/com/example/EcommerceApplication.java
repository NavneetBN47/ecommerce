package com.example;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * Main Spring Boot Application class for E-Commerce system.
 * Enables JPA auditing and transaction management.
 */
@SpringBootApplication
@EnableJpaAuditing
@EnableTransactionManagement
public class EcommerceApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(EcommerceApplication.class, args);
    }
}