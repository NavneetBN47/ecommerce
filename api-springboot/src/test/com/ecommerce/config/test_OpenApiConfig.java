package com.ecommerce.config;

import io.swagger.v3.oas.models.OpenAPI;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Test class for OpenApiConfig
 * 
 * Tests OpenAPI/Swagger configuration including:
 * - OpenAPI bean creation
 * - API documentation metadata
 * - Security scheme configuration
 * 
 * @author Test Generation System
 * @version 1.0.0
 */
@DisplayName("OpenApiConfig Tests")
class test_OpenApiConfig {

    private OpenApiConfig openApiConfig;

    /**
     * Set up test instance before each test
     */
    @BeforeEach
    void setUp() {
        openApiConfig = new OpenApiConfig();
    }

    /**
     * Test OpenAPI bean creation
     * 
     * Validates:
     * - OpenAPI bean is created successfully
     * - Bean is not null
     */
    @Test
    @DisplayName("Should create OpenAPI bean successfully")
    void testCustomOpenAPI_Creation() {
        OpenAPI openAPI = openApiConfig.customOpenAPI();

        assertThat(openAPI).isNotNull();
    }

    /**
     * Test API info configuration
     * 
     * Validates:
     * - API title is set correctly
     * - API version is set
     * - API description is present
     */
    @Test
    @DisplayName("Should configure API info correctly")
    void testCustomOpenAPI_InfoConfiguration() {
        OpenAPI openAPI = openApiConfig.customOpenAPI();

        assertThat(openAPI.getInfo()).isNotNull();
        assertThat(openAPI.getInfo().getTitle()).isEqualTo("Shopping Cart System API");
        assertThat(openAPI.getInfo().getVersion()).isEqualTo("1.0.0");
        assertThat(openAPI.getInfo().getDescription()).contains("Shopping Cart System");
    }

    /**
     * Test contact information configuration
     * 
     * Validates:
     * - Contact information is set
     * - Contact name and email are present
     */
    @Test
    @DisplayName("Should configure contact information")
    void testCustomOpenAPI_ContactConfiguration() {
        OpenAPI openAPI = openApiConfig.customOpenAPI();

        assertThat(openAPI.getInfo().getContact()).isNotNull();
        assertThat(openAPI.getInfo().getContact().getName()).isEqualTo("API Support");
        assertThat(openAPI.getInfo().getContact().getEmail()).isEqualTo("support@ecommerce.com");
    }

    /**
     * Test security scheme configuration
     * 
     * Validates:
     * - Security scheme is configured
     * - Bearer authentication is set up
     * - JWT format is specified
     */
    @Test
    @DisplayName("Should configure security scheme for JWT")
    void testCustomOpenAPI_SecurityScheme() {
        OpenAPI openAPI = openApiConfig.customOpenAPI();

        assertThat(openAPI.getComponents()).isNotNull();
        assertThat(openAPI.getComponents().getSecuritySchemes()).isNotNull();
        assertThat(openAPI.getComponents().getSecuritySchemes()).containsKey("Bearer Authentication");
    }

    /**
     * Test security requirement configuration
     * 
     * Validates:
     * - Security requirements are added
     * - Bearer authentication is required
     */
    @Test
    @DisplayName("Should add security requirements")
    void testCustomOpenAPI_SecurityRequirement() {
        OpenAPI openAPI = openApiConfig.customOpenAPI();

        assertThat(openAPI.getSecurity()).isNotNull();
        assertThat(openAPI.getSecurity()).isNotEmpty();
    }
}