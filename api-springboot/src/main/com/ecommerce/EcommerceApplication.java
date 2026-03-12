package com.ecommerce;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * Main Spring Boot Application Entry Point
 * E-Commerce API with Lazy Cart Management
 * 
 * @author Auto-Generated
 * @version 1.0.0
 */
@SpringBootApplication
@EnableTransactionManagement
public class EcommerceApplication {

    public static void main(String[] args) {
        SpringApplication.run(EcommerceApplication.class, args);
    }
}