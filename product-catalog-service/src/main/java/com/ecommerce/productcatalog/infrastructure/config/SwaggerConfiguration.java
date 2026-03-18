package com.ecommerce.productcatalog.infrastructure.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class SwaggerConfiguration {

    @Value("${server.port:8082}")
    private String serverPort;

    @Bean
    public OpenAPI productCatalogOpenAPI() {
        return new OpenAPI()
            .info(new Info()
                .title("Product Catalog Service API")
                .description("RESTful API for product search and retrieval with Redis caching. " +
                    "Supports full-text search, category filtering, and pagination. " +
                    "Product data is cached with configurable TTL (5-10 minutes) for optimal performance.")
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
                    .description("Production Server")));
    }
}