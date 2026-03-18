# Swagger/OpenAPI Integration Migration Guide

## Overview

This guide provides step-by-step instructions for integrating and using Swagger/OpenAPI documentation in the E-Commerce Platform microservices.

## Table of Contents

1. [Prerequisites](#prerequisites)
2. [Dependencies](#dependencies)
3. [Configuration](#configuration)
4. [Accessing Swagger UI](#accessing-swagger-ui)
5. [Security Configuration](#security-configuration)
6. [Best Practices](#best-practices)
7. [Troubleshooting](#troubleshooting)

---

## Prerequisites

Before integrating Swagger/OpenAPI, ensure you have:

- Java 17 or higher
- Maven 3.6 or higher
- Spring Boot 3.2.0
- PostgreSQL 14 or higher

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

This will download all required dependencies including SpringDoc OpenAPI.

---

## Configuration

### Step 3: Application Configuration

Each service has Swagger configuration in `application.yml`:

```yaml
springdoc:
  api-docs:
    path: /v3/api-docs
  swagger-ui:
    path: /swagger-ui.html
    enabled: true
    operations-sorter: method
    tags-sorter: alpha
  show-actuator: false
```

**Configuration Options:**

- `api-docs.path`: OpenAPI JSON/YAML endpoint
- `swagger-ui.path`: Swagger UI web interface path
- `swagger-ui.enabled`: Enable/disable Swagger UI
- `operations-sorter`: Sort operations by method (GET, POST, etc.)
- `tags-sorter`: Sort tags alphabetically
- `show-actuator`: Hide Spring Boot Actuator endpoints

### Step 4: OpenAPI Annotations

Each application class includes `@OpenAPIDefinition` annotation:

```java
@OpenAPIDefinition(
    info = @Info(
        title = "Service Name API",
        version = "1.0.0",
        description = "Service description",
        contact = @Contact(
            name = "E-Commerce Platform Team",
            email = "support@ecommerce.com"
        ),
        license = @License(
            name = "Apache 2.0",
            url = "https://www.apache.org/licenses/LICENSE-2.0.html"
        )
    ),
    servers = {
        @Server(url = "http://localhost:808X", description = "Development Server"),
        @Server(url = "https://api.ecommerce.com", description = "Production Server")
    }
)
```

### Step 5: Security Scheme Configuration

For services requiring authentication (User Management and Shopping Cart):

```java
@SecurityScheme(
    name = "bearerAuth",
    type = SecuritySchemeType.HTTP,
    scheme = "bearer",
    bearerFormat = "JWT",
    in = SecuritySchemeIn.HEADER
)
```

---

## Accessing Swagger UI

### Step 6: Start the Services

**User Management Service:**
```bash
cd user-management-service
mvn spring-boot:run
```
Access Swagger UI: http://localhost:8081/swagger-ui.html

**Product Catalog Service:**
```bash
cd product-catalog-service
mvn spring-boot:run
```
Access Swagger UI: http://localhost:8082/swagger-ui.html

**Shopping Cart Service:**
```bash
cd shopping-cart-service
mvn spring-boot:run
```
Access Swagger UI: http://localhost:8083/swagger-ui.html

### Step 7: Access OpenAPI Specification

Each service exposes OpenAPI specification at:

- JSON format: `http://localhost:808X/v3/api-docs`
- YAML format: `http://localhost:808X/v3/api-docs.yaml`

Replace `X` with the service port number (1, 2, or 3).

---

## Security Configuration

### Step 8: Configure Security for Swagger Endpoints

Swagger UI and API docs endpoints are publicly accessible. The security configuration in each service allows unauthenticated access:

```java
.authorizeHttpRequests(auth -> auth
    .requestMatchers(
        "/swagger-ui/**",
        "/v3/api-docs/**",
        "/swagger-resources/**",
        "/webjars/**"
    ).permitAll()
    .anyRequest().authenticated()
)
```

### Step 9: Testing Authenticated Endpoints

1. **Register a new user** via `/api/users/register`
2. **Login** via `/api/users/login` to get JWT token
3. **Click "Authorize" button** in Swagger UI
4. **Enter token** in format: `Bearer <your-jwt-token>`
5. **Test protected endpoints**

---

## Best Practices

### Step 10: API Documentation Standards

**Controller-Level Documentation:**
```java
@Tag(name = "Resource Name", description = "Resource description")
```

**Operation-Level Documentation:**
```java
@Operation(
    summary = "Short summary",
    description = "Detailed description"
)
```

**Response Documentation:**
```java
@ApiResponses(value = {
    @ApiResponse(
        responseCode = "200",
        description = "Success message",
        content = @Content(schema = @Schema(implementation = ResponseClass.class))
    ),
    @ApiResponse(
        responseCode = "400",
        description = "Error message",
        content = @Content
    )
})
```

**Parameter Documentation:**
```java
@Parameter(
    description = "Parameter description",
    required = true,
    example = "example value"
)
```

**Schema Documentation:**
```java
@Schema(description = "Field description", example = "example value")
private String fieldName;
```

### Step 11: Security Best Practices

1. **Never expose sensitive data** in API responses
2. **Use DTOs** instead of entities to control exposed fields
3. **Implement proper validation** on all request objects
4. **Use HTTPS** in production environments
5. **Implement rate limiting** to prevent abuse
6. **Add API versioning** for backward compatibility

---

## Troubleshooting

### Common Issues and Solutions

#### Issue 1: Swagger UI Not Loading

**Solution:**
- Verify service is running: `curl http://localhost:808X/actuator/health`
- Check application logs for errors
- Ensure `springdoc.swagger-ui.enabled=true` in configuration

#### Issue 2: 404 Error on Swagger Endpoints

**Solution:**
- Verify correct port number
- Check security configuration allows public access
- Ensure SpringDoc dependency is in classpath

#### Issue 3: Authentication Not Working

**Solution:**
- Verify JWT token is valid and not expired
- Check token format: `Bearer <token>`
- Ensure security configuration is correct

#### Issue 4: Missing API Endpoints

**Solution:**
- Verify controller has `@RestController` annotation
- Check `@RequestMapping` paths are correct
- Ensure controller is in component scan path

#### Issue 5: Schema Not Displaying Correctly

**Solution:**
- Add `@Schema` annotations to DTOs
- Use proper validation annotations
- Check for circular references in object graphs

---

## Additional Resources

### Official Documentation

- [SpringDoc OpenAPI Documentation](https://springdoc.org/)
- [OpenAPI Specification](https://swagger.io/specification/)
- [Swagger UI Documentation](https://swagger.io/tools/swagger-ui/)

### Example Requests

**Register User:**
```bash
curl -X POST http://localhost:8081/api/users/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "john_doe",
    "email": "john.doe@example.com",
    "password": "SecurePass123!",
    "firstName": "John",
    "lastName": "Doe"
  }'
```

**Get Products:**
```bash
curl -X GET "http://localhost:8082/api/products?page=0&size=20&sortBy=name"
```

**Add to Cart (with authentication):**
```bash
curl -X POST http://localhost:8083/api/cart/users/1/items \
  -H "Authorization: Bearer <your-jwt-token>" \
  -H "Content-Type: application/json" \
  -d '{
    "productId": 1,
    "quantity": 2
  }'
```

---

## Deployment Considerations

### Production Environment

1. **Disable Swagger UI in production** (optional):
   ```yaml
   springdoc:
     swagger-ui:
       enabled: false
   ```

2. **Secure API documentation endpoints**:
   - Use API Gateway for authentication
   - Implement IP whitelisting
   - Use VPN for internal access

3. **Use environment-specific configurations**:
   ```yaml
   spring:
     profiles:
       active: ${SPRING_PROFILE:dev}
   ```

4. **Configure CORS** for cross-origin requests:
   ```java
   @Configuration
   public class CorsConfig {
       @Bean
       public WebMvcConfigurer corsConfigurer() {
           return new WebMvcConfigurer() {
               @Override
               public void addCorsMappings(CorsRegistry registry) {
                   registry.addMapping("/api/**")
                       .allowedOrigins("https://yourdomain.com")
                       .allowedMethods("GET", "POST", "PUT", "DELETE");
               }
           };
       }
   }
   ```

---

## Conclusion

You have successfully integrated Swagger/OpenAPI documentation into the E-Commerce Platform microservices. The API documentation is now:

✅ **Fully documented** with comprehensive annotations
✅ **Interactive** via Swagger UI
✅ **Secure** with JWT authentication support
✅ **Standardized** following OpenAPI 3.0 specification
✅ **Production-ready** with proper security configurations

For questions or support, contact: support@ecommerce.com