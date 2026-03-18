package com.ecommerce.usermanagement.infrastructure.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class SwaggerConfiguration {

    @Value("${server.port:8081}")
    private String serverPort;

    @Bean
    public OpenAPI userManagementOpenAPI() {
        return new OpenAPI()
            .info(new Info()
                .title("User Management Service API")
                .description("RESTful API for user registration, authentication, and profile management with JWT-based security (RSA-256). " +
                    "Implements BCrypt password hashing (cost factor 12), comprehensive audit logging, and GDPR-compliant data handling.")
                .version("v1.0.0")
                .contact(new Contact()
                    .name("E-Commerce Platform Team")
                    .email("support@ecommerce.com")
                    .url("https://ecommerce.com"))
                .license(new License()
                    .name("Apache 2.0")
                    .url("https://www.apache.org/licenses/LICENSE-2.0.html")))
            .servers(List.of(
                new Server()
                    .url("http://localhost:" + serverPort)
                    .description("Development Server"),
                new Server()
                    .url("https://api.ecommerce.com")
                    .description("Production Server")))
            .components(new Components()
                .addSecuritySchemes("bearerAuth", new SecurityScheme()
                    .type(SecurityScheme.Type.HTTP)
                    .scheme("bearer")
                    .bearerFormat("JWT")
                    .description("JWT authentication token (RSA-256 signed). Obtain via /api/v1/users/login endpoint.")));
    }
}