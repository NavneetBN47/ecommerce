# Swagger/OpenAPI Integration Guide

## Overview

This guide provides comprehensive instructions for integrating and using Swagger/OpenAPI documentation in the E-commerce Platform microservices.

## Table of Contents

1. [Dependencies](#dependencies)
2. [Configuration](#configuration)
3. [Accessing Swagger UI](#accessing-swagger-ui)
4. [API Documentation Features](#api-documentation-features)
5. [Security Configuration](#security-configuration)
6. [Customization](#customization)
7. [Best Practices](#best-practices)
8. [Troubleshooting](#troubleshooting)

---

## Dependencies

### Maven Dependencies (Already Added)

All three microservices include the following dependency in their `pom.xml`:

```xml
<dependency>
    <groupId>org.springdoc</groupId>
    <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
    <version>2.3.0</version>
</dependency>
```

**What this provides:**
- Automatic OpenAPI 3.0 specification generation
- Swagger UI for interactive API testing
- Integration with Spring Boot 3.x
- Support for Spring Security JWT authentication

---

## Configuration

### Application Properties

Add the following to each service's `application.yml`:

```yaml
springdoc:
  api-docs:
    path: /api-docs
    enabled: true
  swagger-ui:
    path: /swagger-ui.html
    enabled: true
    operations-sorter: method
    tags-sorter: alpha
    try-it-out-enabled: true
    filter: true
    display-request-duration: true
  show-actuator: false
  default-consumes-media-type: application/json
  default-produces-media-type: application/json
```

### OpenAPI Configuration Classes

Each service has a dedicated `OpenApiConfiguration.java` class:

**User Management Service:**
- Location: `user-management-service/src/main/java/com/ecommerce/usermanagement/infrastructure/config/OpenApiConfiguration.java`
- Port: 8081

**Product Catalog Service:**
- Location: `product-catalog-service/src/main/java/com/ecommerce/productcatalog/infrastructure/config/OpenApiConfiguration.java`
- Port: 8082

**Shopping Cart Service:**
- Location: `shopping-cart-service/src/main/java/com/ecommerce/shoppingcart/infrastructure/config/OpenApiConfiguration.java`
- Port: 8083

---

## Accessing Swagger UI

### Development Environment

Once the services are running, access Swagger UI at:

| Service | Swagger UI URL | OpenAPI JSON |
|---------|---------------|-------------|
| User Management | http://localhost:8081/swagger-ui.html | http://localhost:8081/api-docs |
| Product Catalog | http://localhost:8082/swagger-ui.html | http://localhost:8082/api-docs |
| Shopping Cart | http://localhost:8083/swagger-ui.html | http://localhost:8083/api-docs |

### Production Environment

For production deployments:

```
https://api.ecommerce-platform.com/user-management/swagger-ui.html
https://api.ecommerce-platform.com/product-catalog/swagger-ui.html
https://api.ecommerce-platform.com/shopping-cart/swagger-ui.html
```

---

## API Documentation Features

### Implemented Annotations

#### Controller Level
```java
@Tag(name = "User Management", description = "APIs for user registration, authentication, and profile management")
```

#### Operation Level
```java
@Operation(
    summary = "Register a new user",
    description = "Creates a new user account with email, password, and profile information"
)
```

#### Response Documentation
```java
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
```

#### Parameter Documentation
```java
@Parameter(description = "User registration details", required = true)
```

#### Security Requirements
```java
@SecurityRequirement(name = "bearerAuth")
```

### Documented Endpoints

#### User Management Service (8 endpoints)
1. `POST /api/v1/users/register` - User registration
2. `POST /api/v1/users/login` - User authentication
3. `GET /api/v1/users/profile` - Get user profile
4. `PUT /api/v1/users/profile` - Update user profile
5. `POST /api/v1/users/password/change` - Change password
6. `POST /api/v1/users/password/reset` - Reset password
7. `GET /api/v1/users/validate/{userId}` - Validate user (internal)

#### Product Catalog Service (7 endpoints)
1. `GET /api/v1/products/search` - Search products
2. `GET /api/v1/products/{productId}` - Get product details
3. `GET /api/v1/products/category/{category}` - Get products by category
4. `GET /api/v1/products/featured` - Get featured products
5. `GET /api/v1/products/validate/{productId}` - Validate product (internal)
6. `POST /api/v1/products/check-stock` - Check stock availability

#### Shopping Cart Service (6 endpoints)
1. `GET /api/v1/cart` - Get user's cart
2. `POST /api/v1/cart/items` - Add item to cart
3. `PUT /api/v1/cart/items/{itemId}` - Update cart item
4. `DELETE /api/v1/cart/items/{itemId}` - Remove cart item
5. `DELETE /api/v1/cart` - Clear cart
6. `GET /api/v1/cart/summary` - Get cart summary

**Total: 21 documented endpoints**

---

## Security Configuration

### JWT Authentication in Swagger UI

1. **Obtain JWT Token:**
   - Navigate to User Management Service Swagger UI
   - Expand `POST /api/v1/users/login`
   - Click "Try it out"
   - Enter credentials:
     ```json
     {
       "email": "user@example.com",
       "password": "SecurePass123!"
     }
     ```
   - Execute and copy the `token` from response

2. **Authorize Swagger UI:**
   - Click the "Authorize" button (lock icon) at the top
   - Enter: `Bearer <your-token-here>`
   - Click "Authorize"
   - Click "Close"

3. **Test Protected Endpoints:**
   - All subsequent requests will include the JWT token
   - Token is valid for the duration specified in JWT configuration

### Security Scheme Configuration

```java
@SecurityScheme(
    name = "bearerAuth",
    description = "JWT Bearer Token Authentication",
    scheme = "bearer",
    type = SecuritySchemeType.HTTP,
    bearerFormat = "JWT",
    in = SecuritySchemeIn.HEADER
)
```

---

## Customization

### Customize API Information

Edit the `@OpenAPIDefinition` annotation in `OpenApiConfiguration.java`:

```java
@OpenAPIDefinition(
    info = @Info(
        title = "Your Custom Title",
        version = "2.0.0",
        description = "Your custom description",
        contact = @Contact(
            name = "Your Team",
            email = "your-email@example.com"
        )
    )
)
```

### Add Custom Servers

```java
servers = {
    @Server(
        url = "https://staging.example.com",
        description = "Staging Environment"
    )
}
```

### Exclude Endpoints from Documentation

Add to controller or method:

```java
@Hidden
public ResponseEntity<?> internalEndpoint() {
    // This endpoint won't appear in Swagger UI
}
```

---

## Best Practices

### 1. Comprehensive Documentation
- Always include `@Operation` with summary and description
- Document all possible response codes with `@ApiResponses`
- Provide examples for request/response bodies

### 2. Security
- Mark all protected endpoints with `@SecurityRequirement`
- Document authentication requirements clearly
- Never expose sensitive data in examples

### 3. Validation
- Use `@Valid` with request bodies
- Document validation constraints in schema
- Provide clear error messages

### 4. Versioning
- Include API version in URL path (`/api/v1/`)
- Update version in OpenAPI configuration when making breaking changes
- Maintain backward compatibility when possible

### 5. Performance
- Disable Swagger UI in production if not needed:
  ```yaml
  springdoc:
    swagger-ui:
      enabled: false
  ```
- Cache OpenAPI specification
- Use pagination for list endpoints

---

## Troubleshooting

### Issue: Swagger UI Not Loading

**Solution:**
1. Verify dependency is in `pom.xml`
2. Check application is running on correct port
3. Ensure no security configuration is blocking `/swagger-ui/**` and `/api-docs/**`
4. Check application logs for errors

### Issue: Endpoints Not Appearing

**Solution:**
1. Verify controller has `@RestController` annotation
2. Check request mapping paths
3. Ensure controller is in component scan path
4. Restart application

### Issue: JWT Authentication Not Working

**Solution:**
1. Verify token format: `Bearer <token>`
2. Check token expiration
3. Ensure Security Configuration allows Swagger endpoints:
   ```java
   .requestMatchers("/swagger-ui/**", "/api-docs/**").permitAll()
   ```

### Issue: Schema Not Displaying Correctly

**Solution:**
1. Add `@Schema` annotations to DTOs
2. Use proper Jackson annotations
3. Ensure DTOs are public classes
4. Check for circular references

---

## Migration Checklist

- [x] Add springdoc-openapi dependency to all services
- [x] Create OpenApiConfiguration class for each service
- [x] Add Swagger annotations to all controllers
- [x] Document all endpoints with @Operation
- [x] Add @ApiResponses for all response codes
- [x] Configure JWT security scheme
- [x] Add application.yml configuration
- [x] Test Swagger UI accessibility
- [x] Verify JWT authentication in Swagger UI
- [x] Generate OpenAPI specification file
- [x] Update security configuration to allow Swagger endpoints
- [x] Document all request/response schemas
- [x] Add parameter descriptions
- [x] Test all endpoints via Swagger UI
- [x] Create this migration guide

---

## Additional Resources

- [SpringDoc OpenAPI Documentation](https://springdoc.org/)
- [OpenAPI Specification](https://swagger.io/specification/)
- [Swagger UI Documentation](https://swagger.io/tools/swagger-ui/)
- [Spring Boot Integration Guide](https://springdoc.org/#spring-boot-3-support)

---

## Support

For issues or questions:
- Email: support@ecommerce-platform.com
- Documentation: https://docs.ecommerce-platform.com
- GitHub Issues: https://github.com/ecommerce-platform/issues

---

**Last Updated:** 2024-01-15  
**Version:** 1.0.0  
**Author:** E-commerce Platform Team
