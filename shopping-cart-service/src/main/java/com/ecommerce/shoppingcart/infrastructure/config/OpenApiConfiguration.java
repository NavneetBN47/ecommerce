package com.ecommerce.shoppingcart.infrastructure.config;

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
        title = "Shopping Cart Service API",
        version = "1.0.0",
        description = "RESTful API for shopping cart management in the E-commerce Platform. " +
                      "This service handles cart lifecycle operations including lazy cart creation, " +
                      "cart item CRUD operations with stock validation, and cart summary calculations. " +
                      "Implements ABAC (Attribute-Based Access Control) for cart ownership validation. " +
                      "Integrates with User Management and Product Catalog services via REST clients with circuit breaker pattern for resilience. " +
                      "All cart operations require JWT authentication and validate user ownership.",
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
            url = "http://localhost:8083",
            description = "Development Server"
        ),
        @Server(
            url = "https://api-dev.ecommerce-platform.com/cart",
            description = "Development Environment"
        ),
        @Server(
            url = "https://api.ecommerce-platform.com/cart",
            description = "Production Environment"
        )
    }
)
@SecurityScheme(
    name = "bearerAuth",
    description = "JWT Bearer Token Authentication. All cart operations require valid JWT token obtained from User Management Service.",
    scheme = "bearer",
    type = SecuritySchemeType.HTTP,
    bearerFormat = "JWT",
    in = SecuritySchemeIn.HEADER
)
public class OpenApiConfiguration {
}