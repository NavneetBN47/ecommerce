package com.ecommerce.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Test class for OpenApiConfig
 * 
 * This test class verifies the OpenAPI/Swagger configuration,
 * ensuring proper API documentation setup with security schemes.
 * 
 * @author Shopping Cart System Team
 * @version 1.0.0
 */
@ExtendWith(MockitoExtension.class)
class test_OpenApiConfig {

    @InjectMocks
    private OpenApiConfig openApiConfig;

    private OpenAPI openAPI;

    /**
     * Setup method to initialize OpenAPI configuration before each test
     */
    @BeforeEach
    void setUp() {
        openAPI = openApiConfig.customOpenAPI();
    }

    /**
     * Test that customOpenAPI returns a non-null OpenAPI instance
     * 
     * Verifies that the configuration method properly initializes
     * the OpenAPI object.
     */
    @Test
    void customOpenAPI_ShouldReturnNonNullOpenAPI() {
        assertThat(openAPI).isNotNull();
    }

    /**
     * Test that API info is properly configured
     * 
     * Verifies that the API title, version, description, and contact
     * information are correctly set.
     */
    @Test
    void customOpenAPI_ShouldHaveCorrectApiInfo() {
        Info info = openAPI.getInfo();
        
        assertThat(info).isNotNull();
        assertThat(info.getTitle()).isEqualTo("Shopping Cart System API");
        assertThat(info.getVersion()).isEqualTo("1.0.0");
        assertThat(info.getDescription()).contains("RESTful API for Shopping Cart System");
        assertThat(info.getContact()).isNotNull();
        assertThat(info.getContact().getName()).isEqualTo("API Support");
        assertThat(info.getContact().getEmail()).isEqualTo("support@ecommerce.com");
    }

    /**
     * Test that security scheme is properly configured
     * 
     * Verifies that Bearer Authentication with JWT is correctly
     * configured in the OpenAPI specification.
     */
    @Test
    void customOpenAPI_ShouldHaveBearerAuthenticationSecurityScheme() {
        assertThat(openAPI.getComponents()).isNotNull();
        assertThat(openAPI.getComponents().getSecuritySchemes()).isNotNull();
        assertThat(openAPI.getComponents().getSecuritySchemes())
                .containsKey("Bearer Authentication");
        
        var securityScheme = openAPI.getComponents().getSecuritySchemes()
                .get("Bearer Authentication");
        assertThat(securityScheme.getType().toString()).isEqualTo("HTTP");
        assertThat(securityScheme.getScheme()).isEqualTo("bearer");
        assertThat(securityScheme.getBearerFormat()).isEqualTo("JWT");
    }

    /**
     * Test that security requirement is added to the API
     * 
     * Verifies that the Bearer Authentication security requirement
     * is properly added to all API endpoints.
     */
    @Test
    void customOpenAPI_ShouldHaveSecurityRequirement() {
        assertThat(openAPI.getSecurity()).isNotNull();
        assertThat(openAPI.getSecurity()).isNotEmpty();
        
        SecurityRequirement securityRequirement = openAPI.getSecurity().get(0);
        assertThat(securityRequirement.containsKey("Bearer Authentication")).isTrue();
    }
}