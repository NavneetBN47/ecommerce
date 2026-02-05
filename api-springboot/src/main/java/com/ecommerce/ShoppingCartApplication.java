package com.ecommerce;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * Main Spring Boot Application for Shopping Cart System
 * 
 * @author Shopping Cart Team
 * @version 1.0.0
 */
@SpringBootApplication
@EnableJpaAuditing
public class ShoppingCartApplication {

    public static void main(String[] args) {
        SpringApplication.run(ShoppingCartApplication.class, args);
    }
}