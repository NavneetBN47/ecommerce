# Swagger/OpenAPI Integration Migration Guide

## Overview
This guide provides step-by-step instructions for integrating and using Swagger/OpenAPI documentation in the E-commerce Platform microservices.

## Prerequisites
- Java 17 or higher
- Maven 3.8+
- Spring Boot 3.2.0
- All microservices built and running

## Dependencies Required

The following dependency has been added to all service `pom.xml` files:

```xml
<dependency>
    <groupId>org.springdoc</groupId>
    <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
    <version>2.3.0</version>
</dependency>
```

## Configuration Steps

### Step 1: Verify Dependencies
Ensure the SpringDoc OpenAPI dependency is present in each service's `pom.xml`:
- user-management-service/pom.xml
- product-catalog-service/pom.xml
- shopping-cart-service/pom.xml

### Step 2: Update Application Properties

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
  default-consumes-media-type: application/json
  default-produces-media-type: application/json
```

### Step 3: OpenAPI Configuration Classes

OpenAPI configuration classes have been created for each service:

1. **User Management Service**: `com.ecommerce.usermanagement.config.OpenApiConfiguration`
2. **Product Catalog Service**: `com.ecommerce.productcatalog.config.OpenApiConfiguration`
3. **Shopping Cart Service**: `com.ecommerce.shoppingcart.config.OpenApiConfiguration`

These classes define:
- API metadata (title, version, description)
- Contact information
- License information
- Server URLs (development, staging, production)
- Security schemes (JWT Bearer authentication)

### Step 4: Controller Annotations

All controllers have been enhanced with comprehensive Swagger annotations:

- `@Tag`: Groups related endpoints
- `@Operation`: Describes endpoint purpose
- `@ApiResponses`: Documents all possible HTTP responses
- `@Parameter`: Describes request parameters
- `@SecurityRequirement`: Indicates authentication requirements

### Step 5: Build and Deploy

```bash
# Build each service
cd user-management-service
mvn clean install

cd ../product-catalog-service
mvn clean install

cd ../shopping-cart-service
mvn clean install
```

### Step 6: Start Services

```bash
# Start User Management Service (Port 8081)
java -jar user-management-service/target/user-management-service-1.0.0.jar

# Start Product Catalog Service (Port 8082)
java -jar product-catalog-service/target/product-catalog-service-1.0.0.jar

# Start Shopping Cart Service (Port 8083)
java -jar shopping-cart-service/target/shopping-cart-service-1.0.0.jar
```

## Accessing Swagger UI

### User Management Service
- **Swagger UI**: http://localhost:8081/swagger-ui.html
- **OpenAPI JSON**: http://localhost:8081/api-docs
- **OpenAPI YAML**: http://localhost:8081/api-docs.yaml

### Product Catalog Service
- **Swagger UI**: http://localhost:8082/swagger-ui.html
- **OpenAPI JSON**: http://localhost:8082/api-docs
- **OpenAPI YAML**: http://localhost:8082/api-docs.yaml

### Shopping Cart Service
- **Swagger UI**: http://localhost:8083/swagger-ui.html
- **OpenAPI JSON**: http://localhost:8083/api-docs
- **OpenAPI YAML**: http://localhost:8083/api-docs.yaml

## Using Swagger UI

### 1. Authentication
For endpoints requiring authentication:

1. First, call the `/api/v1/users/login` endpoint to obtain a JWT token
2. Click the "Authorize" button at the top of Swagger UI
3. Enter the token in the format: `Bearer <your-token>`
4. Click "Authorize" to apply the token to all subsequent requests

### 2. Testing Endpoints

1. Expand an endpoint by clicking on it
2. Click "Try it out"
3. Fill in required parameters
4. Click "Execute"
5. View the response below

### 3. Viewing Request/Response Schemas

- Scroll down to the "Schemas" section
- Click on any schema to view its structure
- Use these schemas as reference for request bodies

## Security Best Practices

### 1. Production Configuration

In production, disable Swagger UI by setting:

```yaml
springdoc:
  swagger-ui:
    enabled: false
  api-docs:
    enabled: false
```

Or use Spring profiles:

```yaml
spring:
  profiles: prod
springdoc:
  swagger-ui:
    enabled: false
```

### 2. API Gateway Integration

For production deployments with an API Gateway:

1. Aggregate all service OpenAPI specs at the gateway level
2. Expose a single Swagger UI endpoint through the gateway
3. Implement rate limiting on documentation endpoints
4. Use authentication for accessing Swagger UI

### 3. Sensitive Data Protection

- Password fields are marked with `format: password` to hide values
- JWT tokens are not logged in examples
- Sensitive response fields are excluded from documentation

## Customization Options

### Custom Swagger UI Theme

Add to `application.yml`:

```yaml
springdoc:
  swagger-ui:
    syntaxHighlight:
      theme: monokai
```

### Custom API Groups

Create multiple API documentation groups:

```java
@Bean
public GroupedOpenApi publicApi() {
    return GroupedOpenApi.builder()
            .group("public")
            .pathsToMatch("/api/v1/**")
            .build();
}

@Bean
public GroupedOpenApi adminApi() {
    return GroupedOpenApi.builder()
            .group("admin")
            .pathsToMatch("/api/v1/admin/**")
            .build();
}
```

### Custom Operation Sorting

```yaml
springdoc:
  swagger-ui:
    operationsSorter: alpha  # or method
    tagsSorter: alpha
```

## Troubleshooting

### Issue: Swagger UI Not Loading

**Solution**:
1. Verify the service is running: `curl http://localhost:8081/actuator/health`
2. Check if SpringDoc dependency is in classpath
3. Verify `springdoc.swagger-ui.enabled=true` in application.yml
4. Check for port conflicts

### Issue: Endpoints Not Appearing

**Solution**:
1. Ensure controllers are in the component scan path
2. Verify `@RestController` annotation is present
3. Check if endpoints are properly mapped with `@RequestMapping`
4. Restart the service

### Issue: Authentication Not Working

**Solution**:
1. Verify JWT token is valid and not expired
2. Ensure token format is: `Bearer <token>`
3. Check if security configuration allows Swagger endpoints
4. Add Swagger paths to security whitelist:

```java
@Bean
public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    http.authorizeHttpRequests(auth -> auth
        .requestMatchers("/swagger-ui/**", "/api-docs/**").permitAll()
        .anyRequest().authenticated()
    );
    return http.build();
}
```

### Issue: CORS Errors

**Solution**:
Add CORS configuration:

```java
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
```

## Integration with CI/CD

### Generate OpenAPI Spec During Build

Add to `pom.xml`:

```xml
<plugin>
    <groupId>org.springdoc</groupId>
    <artifactId>springdoc-openapi-maven-plugin</artifactId>
    <version>1.4</version>
    <executions>
        <execution>
            <goals>
                <goal>generate</goal>
            </goals>
        </execution>
    </executions>
    <configuration>
        <apiDocsUrl>http://localhost:8081/api-docs</apiDocsUrl>
        <outputFileName>openapi.json</outputFileName>
        <outputDir>${project.build.directory}</outputDir>
    </configuration>
</plugin>
```

### Validate OpenAPI Spec

Use Swagger CLI:

```bash
npm install -g @apidevtools/swagger-cli
swagger-cli validate swagger/openapi.yaml
```

## Monitoring and Analytics

### Track API Usage

Integrate with Spring Boot Actuator:

```yaml
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,prometheus
  metrics:
    export:
      prometheus:
        enabled: true
```

### Custom Metrics

```java
@Component
public class ApiMetrics {
    private final MeterRegistry meterRegistry;
    
    public ApiMetrics(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }
    
    public void recordApiCall(String endpoint) {
        meterRegistry.counter("api.calls", "endpoint", endpoint).increment();
    }
}
```

## Additional Resources

- [SpringDoc OpenAPI Documentation](https://springdoc.org/)
- [OpenAPI Specification](https://swagger.io/specification/)
- [Swagger UI Documentation](https://swagger.io/tools/swagger-ui/)
- [Spring Boot Documentation](https://spring.io/projects/spring-boot)

## Support

For issues or questions:
- Email: support@ecommerce.com
- Documentation: https://docs.ecommerce.com
- GitHub Issues: https://github.com/ecommerce/platform/issues

## Version History

- **1.0.0** (2024-01-15): Initial Swagger integration
  - Added SpringDoc OpenAPI dependency
  - Created OpenAPI configuration classes
  - Enhanced all controllers with Swagger annotations
  - Generated comprehensive OpenAPI specification
  - Created migration guide
