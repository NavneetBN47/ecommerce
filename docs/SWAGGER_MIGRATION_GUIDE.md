# Swagger/OpenAPI Integration Migration Guide

## Overview

This guide provides step-by-step instructions for integrating Swagger/OpenAPI documentation into the E-Commerce Platform microservices.

## Table of Contents

1. [Prerequisites](#prerequisites)
2. [Dependencies](#dependencies)
3. [Configuration](#configuration)
4. [Accessing Swagger UI](#accessing-swagger-ui)
5. [Security Configuration](#security-configuration)
6. [Customization](#customization)
7. [Troubleshooting](#troubleshooting)

---

## Prerequisites

- Java 17 or higher
- Maven 3.8+
- Spring Boot 3.2.0
- All three microservices deployed and running

---

## Dependencies

### Step 1: Add Springdoc OpenAPI Dependency

The following dependency has been added to all three service `pom.xml` files:

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
- `springdoc-openapi-starter-webmvc-ui` (Swagger UI + OpenAPI 3.0)
- `swagger-annotations` (API documentation annotations)
- `swagger-models` (OpenAPI schema models)

---

## Configuration

### Step 3: Application Configuration

Add the following to each service's `application.yml`:

#### User Management Service (`application.yml`)

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

#### Product Catalog Service (`application.yml`)

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
```

#### Shopping Cart Service (`application.yml`)

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

### Step 4: Swagger Configuration Classes

Swagger configuration classes have been created for each service:

- **User Management**: `com.ecommerce.usermanagement.infrastructure.config.SwaggerConfiguration`
- **Product Catalog**: `com.ecommerce.productcatalog.infrastructure.config.SwaggerConfiguration`
- **Shopping Cart**: `com.ecommerce.shoppingcart.infrastructure.config.SwaggerConfiguration`

These classes define:
- API metadata (title, description, version)
- Contact information
- License details
- Server URLs (dev and prod)
- Security schemes (JWT Bearer authentication)

---

## Accessing Swagger UI

### Step 5: Start Services

Start each microservice:

```bash
# User Management Service
cd user-management-service
mvn spring-boot:run

# Product Catalog Service
cd product-catalog-service
mvn spring-boot:run

# Shopping Cart Service
cd shopping-cart-service
mvn spring-boot:run
```

### Step 6: Access Swagger UI

Once services are running, access Swagger UI at:

| Service | Swagger UI URL | OpenAPI JSON |
|---------|---------------|-------------|
| User Management | http://localhost:8081/swagger-ui.html | http://localhost:8081/api-docs |
| Product Catalog | http://localhost:8082/swagger-ui.html | http://localhost:8082/api-docs |
| Shopping Cart | http://localhost:8083/swagger-ui.html | http://localhost:8083/api-docs |

### Step 7: Download OpenAPI Specifications

You can download the OpenAPI 3.0 specification files:

```bash
# User Management Service
curl http://localhost:8081/api-docs -o user-management-openapi.json

# Product Catalog Service
curl http://localhost:8082/api-docs -o product-catalog-openapi.json

# Shopping Cart Service
curl http://localhost:8083/api-docs -o shopping-cart-openapi.json
```

YAML versions are also available in the `/swagger` directory of the repository.

---

## Security Configuration

### Step 8: JWT Authentication in Swagger UI

For services requiring authentication (User Management and Shopping Cart):

1. **Obtain JWT Token**:
   - Navigate to User Management Service Swagger UI
   - Execute `POST /api/v1/users/login` with valid credentials
   - Copy the `accessToken` from the response

2. **Authorize in Swagger UI**:
   - Click the **"Authorize"** button (lock icon) at the top right
   - Enter: `Bearer <your-access-token>`
   - Click **"Authorize"**
   - Click **"Close"**

3. **Test Authenticated Endpoints**:
   - All subsequent requests will include the JWT token
   - Token is valid for 1 hour (configurable)

### Step 9: Security Annotations

Controllers use the following security annotations:

```java
@SecurityRequirement(name = "bearerAuth")
@PreAuthorize("isAuthenticated()")
public ResponseEntity<UserProfileResponse> getUserProfile(
    @RequestAttribute("userId") Long userId
) {
    // Implementation
}
```

---

## Customization

### Step 10: Customize API Documentation

You can customize Swagger annotations in controllers:

#### Operation-Level Customization

```java
@Operation(
    summary = "Get user profile",
    description = "Retrieves the authenticated user's profile information",
    tags = {"User Management"}
)
@ApiResponses(value = {
    @ApiResponse(
        responseCode = "200",
        description = "Profile retrieved successfully",
        content = @Content(schema = @Schema(implementation = UserProfileResponse.class))
    ),
    @ApiResponse(
        responseCode = "401",
        description = "Unauthorized"
    )
})
```

#### Parameter Documentation

```java
@Parameter(
    description = "Unique product identifier",
    required = true,
    example = "12345"
)
@PathVariable Long productId
```

#### Schema Customization

```java
@Schema(
    description = "User registration request",
    example = "{\"email\":\"user@example.com\",\"password\":\"SecurePass123!\"}"
)
public class UserRegistrationRequest {
    @Schema(description = "User email address", example = "user@example.com")
    private String email;
    
    @Schema(description = "User password (min 8 chars)", example = "SecurePass123!")
    private String password;
}
```

### Step 11: Environment-Specific Configuration

For production environments, you may want to disable Swagger UI:

```yaml
# application-prod.yml
springdoc:
  swagger-ui:
    enabled: false
  api-docs:
    enabled: false
```

---

## Troubleshooting

### Issue 1: Swagger UI Not Loading

**Symptoms**: 404 error when accessing `/swagger-ui.html`

**Solutions**:
1. Verify `springdoc-openapi-starter-webmvc-ui` dependency is in `pom.xml`
2. Run `mvn clean install` to download dependencies
3. Check `application.yml` for correct Swagger configuration
4. Ensure service is running on the correct port

### Issue 2: JWT Authentication Not Working

**Symptoms**: 401 Unauthorized errors for authenticated endpoints

**Solutions**:
1. Verify JWT token is valid (not expired)
2. Ensure token is prefixed with `Bearer ` in Swagger UI
3. Check `SecurityConfiguration` allows Swagger endpoints:
   ```java
   .requestMatchers("/swagger-ui/**", "/api-docs/**").permitAll()
   ```

### Issue 3: Missing Endpoints in Swagger UI

**Symptoms**: Some endpoints not appearing in Swagger UI

**Solutions**:
1. Verify controller has `@RestController` annotation
2. Check `@RequestMapping` path is correct
3. Ensure controller is in a package scanned by Spring Boot
4. Add `@Tag` annotation to controller for grouping

### Issue 4: OpenAPI JSON Generation Fails

**Symptoms**: Error when accessing `/api-docs`

**Solutions**:
1. Check for circular references in DTOs
2. Verify all DTOs have proper Jackson annotations
3. Review application logs for schema generation errors
4. Ensure all `@Schema` annotations are valid

---

## Validation Checklist

- [ ] All three services have Swagger UI accessible
- [ ] OpenAPI JSON/YAML files can be downloaded
- [ ] JWT authentication works in Swagger UI
- [ ] All endpoints are documented with proper annotations
- [ ] Request/response schemas are complete
- [ ] Security requirements are properly configured
- [ ] Error responses are documented
- [ ] Examples are provided for all DTOs

---

## Additional Resources

- [Springdoc OpenAPI Documentation](https://springdoc.org/)
- [OpenAPI 3.0 Specification](https://swagger.io/specification/)
- [Swagger Annotations Guide](https://github.com/swagger-api/swagger-core/wiki/Swagger-2.X---Annotations)
- [JWT Authentication Best Practices](https://jwt.io/introduction)

---

## Support

For issues or questions:
- Email: support@ecommerce.com
- GitHub Issues: https://github.com/ecommerce-platform/issues
- Documentation: https://docs.ecommerce.com

---

**Last Updated**: 2024-01-15  
**Version**: 1.0.0  
**Author**: E-Commerce Platform Team