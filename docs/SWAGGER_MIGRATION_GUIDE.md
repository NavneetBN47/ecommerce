# Swagger/OpenAPI Integration Migration Guide

## Overview

This guide provides step-by-step instructions for integrating and using Swagger/OpenAPI documentation in the E-Commerce Platform microservices.

## Table of Contents

1. [Prerequisites](#prerequisites)
2. [Dependencies](#dependencies)
3. [Configuration](#configuration)
4. [Accessing Swagger UI](#accessing-swagger-ui)
5. [Security Configuration](#security-configuration)
6. [Customization](#customization)
7. [Best Practices](#best-practices)
8. [Troubleshooting](#troubleshooting)

---

## Prerequisites

- Java 17 or higher
- Maven 3.9.x
- Spring Boot 3.2.0
- Running PostgreSQL instance
- Running Redis instance (for Product Catalog Service)

---

## Dependencies

### Step 1: Add SpringDoc OpenAPI Dependency

All three microservices already include the SpringDoc OpenAPI dependency in their `pom.xml`:

```xml
<dependency>
    <groupId>org.springdoc</groupId>
    <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
    <version>2.3.0</version>
</dependency>
```

### Step 2: Verify Maven Dependencies

Run the following command in each service directory:

```bash
mvn clean install
```

This will download all required dependencies including:
- `springdoc-openapi-starter-webmvc-ui`
- `swagger-annotations`
- `swagger-models`
- `swagger-ui`

---

## Configuration

### Step 3: Application Configuration

Add the following configuration to `application.yml` in each service:

```yaml
springdoc:
  api-docs:
    path: /api-docs
    enabled: true
  swagger-ui:
    path: /swagger-ui.html
    enabled: true
    operationsSorter: method
    tagsSorter: alpha
    displayRequestDuration: true
    filter: true
  show-actuator: false
```

### Step 4: OpenAPI Configuration Class

Each service includes an `OpenApiConfiguration` class:

**User Management Service:**
- Location: `src/main/java/com/ecommerce/usermanagement/infrastructure/config/OpenApiConfiguration.java`
- Includes JWT bearer authentication scheme

**Product Catalog Service:**
- Location: `src/main/java/com/ecommerce/productcatalog/infrastructure/config/OpenApiConfiguration.java`
- Public API (no authentication required)

**Shopping Cart Service:**
- Location: `src/main/java/com/ecommerce/shoppingcart/infrastructure/config/OpenApiConfiguration.java`
- Includes JWT bearer authentication scheme

---

## Accessing Swagger UI

### Step 5: Start the Services

**User Management Service (Port 8080):**
```bash
cd user-management-service
mvn spring-boot:run
```

**Product Catalog Service (Port 8081):**
```bash
cd product-catalog-service
mvn spring-boot:run
```

**Shopping Cart Service (Port 8082):**
```bash
cd shopping-cart-service
mvn spring-boot:run
```

### Step 6: Access Swagger UI

**User Management Service:**
- Swagger UI: http://localhost:8080/swagger-ui.html
- OpenAPI JSON: http://localhost:8080/api-docs
- OpenAPI YAML: http://localhost:8080/api-docs.yaml

**Product Catalog Service:**
- Swagger UI: http://localhost:8081/swagger-ui.html
- OpenAPI JSON: http://localhost:8081/api-docs
- OpenAPI YAML: http://localhost:8081/api-docs.yaml

**Shopping Cart Service:**
- Swagger UI: http://localhost:8082/swagger-ui.html
- OpenAPI JSON: http://localhost:8082/api-docs
- OpenAPI YAML: http://localhost:8082/api-docs.yaml

---

## Security Configuration

### Step 7: JWT Authentication in Swagger UI

For services requiring authentication (User Management and Shopping Cart):

1. **Register a User:**
   - Navigate to User Management Service Swagger UI
   - Expand `POST /api/v1/users/register`
   - Click "Try it out"
   - Enter user details
   - Execute the request

2. **Login to Get JWT Token:**
   - Expand `POST /api/v1/users/login`
   - Click "Try it out"
   - Enter credentials
   - Execute the request
   - Copy the `token` value from the response

3. **Authorize in Swagger UI:**
   - Click the "Authorize" button (lock icon) at the top right
   - Enter: `Bearer <your-jwt-token>`
   - Click "Authorize"
   - Click "Close"

4. **Test Protected Endpoints:**
   - All subsequent requests will include the JWT token
   - Try accessing `GET /api/v1/users/profile`

### Step 8: Security Annotations

Protected endpoints use the `@SecurityRequirement` annotation:

```java
@GetMapping("/profile")
@SecurityRequirement(name = "bearerAuth")
@Operation(summary = "Get user profile")
public ResponseEntity<UserProfileResponse> getUserProfile(Authentication authentication) {
    // Implementation
}
```

---

## Customization

### Step 9: Customize API Documentation

**Controller-Level Tags:**
```java
@Tag(name = "User Management", description = "APIs for user registration, authentication, and profile management")
public class UserController {
    // Controller methods
}
```

**Operation-Level Documentation:**
```java
@Operation(
    summary = "Register a new user",
    description = "Creates a new user account with email, password, and profile information"
)
@ApiResponses(value = {
    @ApiResponse(
        responseCode = "201",
        description = "User successfully registered",
        content = @Content(schema = @Schema(implementation = UserRegistrationResponse.class))
    ),
    @ApiResponse(
        responseCode = "400",
        description = "Invalid input data",
        content = @Content(schema = @Schema(implementation = ErrorResponse.class))
    )
})
public ResponseEntity<UserRegistrationResponse> registerUser(@Valid @RequestBody UserRegistrationRequest request) {
    // Implementation
}
```

**Schema Documentation:**
```java
@Schema(description = "User registration request")
public class UserRegistrationRequest {
    
    @Schema(description = "User's email address", example = "john.doe@example.com")
    private String email;
    
    @Schema(description = "User's password (min 8 characters)", example = "SecureP@ss123")
    private String password;
}
```

### Step 10: Environment-Specific Configuration

**Development Environment:**
```yaml
springdoc:
  swagger-ui:
    enabled: true
```

**Production Environment:**
```yaml
springdoc:
  swagger-ui:
    enabled: false  # Disable Swagger UI in production
  api-docs:
    enabled: true   # Keep API docs for internal tools
```

---

## Best Practices

### 1. **Comprehensive Documentation**
- Document all endpoints with clear summaries and descriptions
- Include example values for request/response schemas
- Document all possible response codes

### 2. **Security**
- Never expose sensitive data in examples
- Disable Swagger UI in production environments
- Use API keys or OAuth2 for production API documentation access

### 3. **Validation**
- Use `@Valid` annotation for request validation
- Document validation constraints in schema descriptions
- Include validation error responses

### 4. **Versioning**
- Include API version in the URL path (`/api/v1/...`)
- Document breaking changes in the API description
- Maintain backward compatibility when possible

### 5. **Error Handling**
- Use consistent error response structure
- Document all error scenarios
- Include error codes and messages

---

## Troubleshooting

### Issue 1: Swagger UI Not Loading

**Solution:**
```yaml
# Check application.yml
springdoc:
  swagger-ui:
    enabled: true
    path: /swagger-ui.html
```

**Verify URL:**
- Correct: `http://localhost:8080/swagger-ui.html`
- Incorrect: `http://localhost:8080/swagger-ui/`

### Issue 2: 401 Unauthorized on Protected Endpoints

**Solution:**
1. Ensure JWT token is valid and not expired
2. Check token format: `Bearer <token>`
3. Verify Spring Security configuration allows Swagger endpoints:

```java
@Bean
public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    http
        .authorizeHttpRequests(auth -> auth
            .requestMatchers("/swagger-ui/**", "/api-docs/**").permitAll()
            .anyRequest().authenticated()
        );
    return http.build();
}
```

### Issue 3: Missing Annotations

**Solution:**
Ensure all required dependencies are in `pom.xml`:
```xml
<dependency>
    <groupId>org.springdoc</groupId>
    <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
    <version>2.3.0</version>
</dependency>
```

### Issue 4: CORS Errors

**Solution:**
Add CORS configuration:
```java
@Configuration
public class CorsConfiguration {
    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/api/**")
                    .allowedOrigins("http://localhost:3000")
                    .allowedMethods("GET", "POST", "PUT", "DELETE")
                    .allowedHeaders("*")
                    .allowCredentials(true);
            }
        };
    }
}
```

### Issue 5: OpenAPI Spec Not Generated

**Solution:**
1. Check that `@RestController` is used (not `@Controller`)
2. Verify `@RequestMapping` is present on controller class
3. Ensure methods have proper HTTP method annotations (`@GetMapping`, `@PostMapping`, etc.)

---

## Additional Resources

- **SpringDoc OpenAPI Documentation:** https://springdoc.org/
- **OpenAPI Specification:** https://swagger.io/specification/
- **Swagger UI Documentation:** https://swagger.io/tools/swagger-ui/
- **Spring Boot Documentation:** https://spring.io/projects/spring-boot

---

## Support

For issues or questions:
- Email: support@ecommerce.com
- Documentation: https://ecommerce.com/docs
- GitHub Issues: https://github.com/ecommerce/platform/issues

---

**Last Updated:** 2024-01-15  
**Version:** 1.0.0  
**Author:** E-Commerce Platform Team