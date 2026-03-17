package com.ecommerce.productcatalog.config;

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
        description = "RESTful API for browsing, searching, and retrieving product information in the E-commerce Platform",
        contact = @Contact(
            name = "E-commerce Platform Team",
            email = "support@ecommerce.com",
            url = "https://www.ecommerce.com"
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
            url = "https://api-dev.ecommerce.com",
            description = "Development Environment"
        ),
        @Server(
            url = "https://api.ecommerce.com",
            description = "Production Environment"
        )
    }
)
public class OpenApiConfiguration {
}