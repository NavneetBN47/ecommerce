package com.ecommerce.productcatalog.infrastructure.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
    info = @Info(
        title = "Product Catalog Service API",
        version = "1.0.0",
        description = "RESTful API for product catalog management in the E-commerce Platform. " +
                      "This service provides product search with case-insensitive full-text search using PostgreSQL GIN indexes, " +
                      "product details retrieval, category-based filtering, and stock validation. " +
                      "All product data is cached in Redis with 10-minute TTL for optimal performance. " +
                      "Supports pagination, sorting, and filtering for efficient product discovery.",
        contact = @Contact(
            name = "E-commerce Platform Team",
            email = "support@ecommerce-platform.com",
            url = "https://ecommerce-platform.com"
        ),
        license = @License(
            name = "Apache 2.0",
            url = "https://www.apache.org/licenses/LICENSE-2.0.html"
        )
    ),
    servers = {
        @Server(
            url = "http://localhost:8082",
            description = "Development Server"
        ),
        @Server(
            url = "https://api-dev.ecommerce-platform.com/products",
            description = "Development Environment"
        ),
        @Server(
            url = "https://api.ecommerce-platform.com/products",
            description = "Production Environment"
        )
    }
)
@SecurityScheme(
    name = "bearerAuth",
    description = "JWT Bearer Token Authentication. Required for internal service-to-service communication.",
    scheme = "bearer",
    type = SecuritySchemeType.HTTP,
    bearerFormat = "JWT",
    in = SecuritySchemeIn.HEADER
)
public class OpenApiConfiguration {
}