package com.ecommerce.productcatalog.infrastructure.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
    info = @Info(
        title = "Product Catalog Service API",
        version = "1.0.0",
        description = "RESTful API for product search, retrieval, and catalog management in the E-Commerce platform",
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
            url = "http://localhost:8081",
            description = "Development Server"
        ),
        @Server(
            url = "https://api-dev.ecommerce.com/products",
            description = "Development Environment"
        ),
        @Server(
            url = "https://api.ecommerce.com/products",
            description = "Production Environment"
        )
    }
)
public class OpenApiConfiguration {
}