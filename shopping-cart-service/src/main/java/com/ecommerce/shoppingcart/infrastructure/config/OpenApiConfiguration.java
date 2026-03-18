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
        description = "RESTful API for shopping cart management in the E-Commerce platform",
        contact = @Contact(
            name = "E-Commerce Platform Team",
            email = "support@ecommerce.com",
            url = "https://ecommerce.com/support"
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
            url = "https://api-dev.ecommerce.com/cart",
            description = "Development Environment"
        ),
        @Server(
            url = "https://api.ecommerce.com/cart",
            description = "Production Environment"
        )
    }
)
@SecurityScheme(
    name = "bearerAuth",
    description = "JWT authentication token",
    scheme = "bearer",
    type = SecuritySchemeType.HTTP,
    bearerFormat = "JWT",
    in = SecuritySchemeIn.HEADER
)
public class OpenApiConfiguration {
}