# Swagger/OpenAPI Integration Guide

## Overview

This guide provides comprehensive instructions for integrating and using Swagger/OpenAPI documentation in the E-Commerce Platform microservices.

## Table of Contents

1. [Dependencies](#dependencies)
2. [Configuration](#configuration)
3. [Accessing Swagger UI](#accessing-swagger-ui)
4. [Security Configuration](#security-configuration)
5. [Customization](#customization)
6. [Best Practices](#best-practices)
7. [Troubleshooting](#troubleshooting)

## Dependencies

### Maven Dependencies

All service `pom.xml` files have been updated with the following Swagger dependency:

```xml
<dependency>
    <groupId>org.springdoc</groupId>
    <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
    <version>2.3.0</version>
</dependency>
```

### Dependency Details

- **springdoc-openapi-starter-webmvc-ui**: Provides OpenAPI 3.0 specification generation and Swagger UI integration for Spring Boot 3.x applications
- **Version**: 2.3.0 (latest stable as of January 2024)
- **Compatibility**: Spring Boot 3.2.x, Java 17+

## Configuration

### Application Properties

Add the following configuration to each service's `application.yml`:

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
  default-consumes-media-type: application/json
  default-produces-media-type: application/json
```

### Swagger Configuration Class

A centralized `SwaggerConfiguration.java` has been created in the common module:

**Location**: `src/main/java/com/ecommerce/common/config/SwaggerConfiguration.java`

**Key Features**:
- API metadata (title, description, version)
- Contact information
- License details
- Server configurations (dev, prod)
- JWT Bearer authentication scheme

### Service-Specific Configuration

Each service should import the common Swagger configuration:

```java
@Configuration
@Import(SwaggerConfiguration.class)
public class ServiceConfiguration {
    // Service-specific beans
}
```

## Accessing Swagger UI

### Development Environment

#### User Management Service
- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **OpenAPI JSON**: http://localhost:8080/api-docs
- **OpenAPI YAML**: http://localhost:8080/api-docs.yaml

#### Product Catalog Service
- **Swagger UI**: http://localhost:8081/swagger-ui.html
- **OpenAPI JSON**: http://localhost:8081/api-docs
- **OpenAPI YAML**: http://localhost:8081/api-docs.yaml

#### Shopping Cart Service
- **Swagger UI**: http://localhost:8082/swagger-ui.html
- **OpenAPI JSON**: http://localhost:8082/api-docs
- **OpenAPI YAML**: http://localhost:8082/api-docs.yaml

### Production Environment

- **Swagger UI**: https://api.ecommerce.com/swagger-ui.html
- **OpenAPI Spec**: https://api.ecommerce.com/api-docs

**Note**: In production, consider restricting Swagger UI access to authorized users only.

## Security Configuration

### JWT Authentication in Swagger UI

1. **Obtain JWT Token**:
   - Navigate to the "User Management" section
   - Execute the `POST /api/v1/users/login` endpoint
   - Copy the returned JWT token

2. **Authorize in Swagger UI**:
   - Click the "Authorize" button (lock icon) at the top right
   - Enter: `Bearer <your-jwt-token>`
   - Click "Authorize"
   - Click "Close"

3. **Test Protected Endpoints**:
   - All subsequent requests will include the Authorization header
   - Token is valid for 24 hours

### Security Annotations

Controllers use the following security annotations:

```java
@SecurityRequirement(name = "Bearer Authentication")
@PreAuthorize("isAuthenticated()")
public class SecureController {
    // Protected endpoints
}
```

### Hiding Sensitive Information

Use `@Parameter(hidden = true)` to hide sensitive parameters:

```java
public ResponseEntity<?> secureEndpoint(
    @Parameter(hidden = true) @RequestHeader("Authorization") String token) {
    // Implementation
}
```

## Customization

### Custom API Documentation

#### Controller-Level Documentation

```java
@Tag(name = "User Management", description = "APIs for user operations")
@RestController
@RequestMapping("/api/v1/users")
public class UserController {
    // Endpoints
}
```

#### Endpoint-Level Documentation

```java
@Operation(
    summary = "Register a new user",
    description = "Creates a new user account with secure password hashing"
)
@ApiResponses(value = {
    @ApiResponse(responseCode = "201", description = "User registered successfully",
        content = @Content(schema = @Schema(implementation = UserResponse.class))),
    @ApiResponse(responseCode = "400", description = "Invalid input",
        content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
})
@PostMapping("/register")
public ResponseEntity<UserResponse> register(@Valid @RequestBody UserRequest request) {
    // Implementation
}
```

#### Parameter Documentation

```java
public ResponseEntity<?> search(
    @Parameter(description = "Search keyword", example = "laptop") 
    @RequestParam String keyword) {
    // Implementation
}
```

### Schema Documentation

```java
@Schema(description = "User registration request")
public class UserRegistrationRequest {
    
    @Schema(description = "User email address", example = "user@example.com", required = true)
    @Email
    private String email;
    
    @Schema(description = "User password (min 8 characters)", example = "SecurePass123!", required = true)
    @Size(min = 8)
    private String password;
}
```

## Best Practices

### 1. Comprehensive Documentation

- Document all public endpoints
- Provide clear descriptions and examples
- Include all possible response codes
- Document request/response schemas

### 2. Security

- Never expose sensitive data in examples
- Use `@Parameter(hidden = true)` for internal parameters
- Implement proper authentication/authorization
- Restrict Swagger UI access in production

### 3. Versioning

- Include API version in the URL path (`/api/v1/`)
- Update OpenAPI version when making breaking changes
- Maintain backward compatibility when possible

### 4. Error Handling

- Document all error responses
- Use consistent error response format
- Include error codes and messages

### 5. Performance

- Cache OpenAPI specification
- Disable Swagger UI in production if not needed
- Use lazy initialization for Swagger beans

## Troubleshooting

### Issue: Swagger UI Not Loading

**Solution**:
1. Verify dependency is in `pom.xml`
2. Check `application.yml` configuration
3. Ensure no conflicting security rules
4. Clear browser cache
5. Check application logs for errors

### Issue: Endpoints Not Appearing

**Solution**:
1. Verify controller has `@RestController` annotation
2. Check request mapping paths
3. Ensure controller is in component scan path
4. Verify no `@Hidden` annotations on endpoints

### Issue: Authentication Not Working

**Solution**:
1. Verify JWT token format: `Bearer <token>`
2. Check token expiration
3. Ensure security configuration allows Swagger endpoints
4. Verify `@SecurityRequirement` annotation

### Issue: Schema Not Displaying Correctly

**Solution**:
1. Add `@Schema` annotations to DTOs
2. Ensure proper Jackson annotations
3. Check for circular references
4. Verify getter/setter methods exist

### Issue: 404 on Swagger UI Path

**Solution**:
1. Check `springdoc.swagger-ui.path` in `application.yml`
2. Verify Spring Boot version compatibility
3. Ensure no servlet context path conflicts
4. Check security configuration

## Migration Steps

### Step 1: Add Dependencies

Update all service `pom.xml` files with Swagger dependency (already completed).

### Step 2: Configure Application Properties

Add Swagger configuration to `application.yml` in each service:

```yaml
springdoc:
  api-docs:
    path: /api-docs
    enabled: true
  swagger-ui:
    path: /swagger-ui.html
    enabled: true
```

### Step 3: Update Security Configuration

Allow Swagger endpoints in security configuration:

```java
@Bean
public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    http
        .authorizeHttpRequests(auth -> auth
            .requestMatchers("/swagger-ui/**", "/api-docs/**").permitAll()
            .anyRequest().authenticated()
        );
    return http.build();
}
```

### Step 4: Add Annotations to Controllers

Update controllers with Swagger annotations (already completed for all controllers).

### Step 5: Test Documentation

1. Start each service
2. Navigate to Swagger UI URL
3. Verify all endpoints are documented
4. Test authentication flow
5. Validate request/response schemas

### Step 6: Generate OpenAPI Specification

Export OpenAPI specification for external use:

```bash
curl http://localhost:8080/api-docs > openapi.json
curl http://localhost:8080/api-docs.yaml > openapi.yaml
```

### Step 7: Deploy to Production

1. Update production `application.yml`
2. Configure security for Swagger UI
3. Update server URLs in OpenAPI config
4. Test in production environment

## Additional Resources

- [SpringDoc OpenAPI Documentation](https://springdoc.org/)
- [OpenAPI Specification](https://swagger.io/specification/)
- [Swagger UI Documentation](https://swagger.io/tools/swagger-ui/)
- [Spring Boot Security](https://spring.io/guides/gs/securing-web/)

## Support

For issues or questions:
- **Email**: navneet.bhargavan@ascendion.com
- **GitHub Issues**: https://github.com/NavneetBN47/ecommerce/issues
- **Documentation**: https://github.com/NavneetBN47/ecommerce/wiki

---

**Last Updated**: 2024-01-15
**Version**: 1.0.0
**Author**: E-Commerce Platform Team