# E-Commerce Platform - Microservices

## Overview

This is a comprehensive e-commerce platform built using Spring Boot microservices architecture. The platform consists of three core services:

1. **User Management Service** - Handles user registration, authentication, and profile management
2. **Product Catalog Service** - Manages product information and search functionality
3. **Shopping Cart Service** - Manages shopping cart operations

## Architecture

### Technology Stack

- **Framework**: Spring Boot 3.2.x
- **Language**: Java 17
- **Database**: PostgreSQL 15.x
- **Cache**: Redis 7.x
- **Security**: Spring Security 6.x with JWT
- **API Documentation**: SpringDoc OpenAPI 3.0
- **Build Tool**: Maven 3.9.x
- **Containerization**: Docker
- **Orchestration**: Kubernetes

### Design Patterns

- Domain-Driven Design (DDD)
- Repository Pattern
- Service Layer Pattern
- DTO Pattern
- Circuit Breaker Pattern (Resilience4j)

## Services

### 1. User Management Service (Port: 8081)

**Endpoints:**
- `POST /api/users/register` - Register new user
- `POST /api/users/login` - User authentication
- `GET /api/users/profile` - Get user profile (authenticated)
- `PUT /api/users/profile` - Update user profile (authenticated)
- `POST /api/users/change-password` - Change password (authenticated)
- `POST /api/users/reset-password` - Request password reset

**Features:**
- BCrypt password hashing (cost factor 12)
- JWT token generation and validation
- User profile management
- Audit logging for compliance

### 2. Product Catalog Service (Port: 8082)

**Endpoints:**
- `GET /api/products/search` - Search products by keyword
- `GET /api/products/{id}` - Get product details
- `GET /api/products/{id}/availability` - Check product availability
- `GET /api/products/category/{category}` - Get products by category

**Features:**
- Redis caching for performance
- Full-text search support
- Pagination support
- Product availability checking

### 3. Shopping Cart Service (Port: 8083)

**Endpoints:**
- `GET /api/cart` - Get user's cart (authenticated)
- `POST /api/cart/items` - Add item to cart (authenticated)
- `PUT /api/cart/items/{itemId}` - Update cart item (authenticated)
- `DELETE /api/cart/items/{itemId}` - Remove item from cart (authenticated)

**Features:**
- Lazy cart creation
- User and product validation via service clients
- Circuit breaker for resilience
- ABAC authorization

## Setup Instructions

### Prerequisites

- Java 17 or higher
- Maven 3.9.x or higher
- PostgreSQL 15.x
- Redis 7.x
- Docker (optional)
- Kubernetes (optional)

### Local Development Setup

#### 1. Database Setup

```bash
# Create PostgreSQL databases
psql -U postgres
CREATE DATABASE user_management_db;
CREATE DATABASE product_catalog_db;
CREATE DATABASE shopping_cart_db;
```

#### 2. Redis Setup

```bash
# Start Redis server
redis-server

# Or using Docker
docker run -d -p 6379:6379 redis:7-alpine
```

#### 3. Configure Application Properties

Update `application.yml` in each service with your database and Redis credentials:

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/{database_name}
    username: your_username
    password: your_password
  
  redis:
    host: localhost
    port: 6379

jwt:
  secret: your_jwt_secret_key_at_least_256_bits
  expiration: 86400000
```

#### 4. Build and Run Services

```bash
# Build all services
mvn clean install

# Run User Management Service
cd user-management-service
mvn spring-boot:run

# Run Product Catalog Service
cd product-catalog-service
mvn spring-boot:run

# Run Shopping Cart Service
cd shopping-cart-service
mvn spring-boot:run
```

### Docker Setup

```bash
# Build Docker images
docker build -t user-management-service:latest ./user-management-service
docker build -t product-catalog-service:latest ./product-catalog-service
docker build -t shopping-cart-service:latest ./shopping-cart-service

# Run using Docker Compose
docker-compose up -d
```

### Kubernetes Deployment

```bash
# Apply Kubernetes manifests
kubectl apply -f kubernetes/database-secrets.yaml
kubectl apply -f kubernetes/redis-secrets.yaml
kubectl apply -f kubernetes/jwt-secrets.yaml
kubectl apply -f kubernetes/user-management-deployment.yaml
kubectl apply -f kubernetes/product-catalog-deployment.yaml
kubectl apply -f kubernetes/shopping-cart-deployment.yaml
```

## API Documentation

### Swagger UI Access

- **User Management Service**: http://localhost:8081/swagger-ui.html
- **Product Catalog Service**: http://localhost:8082/swagger-ui.html
- **Shopping Cart Service**: http://localhost:8083/swagger-ui.html

### OpenAPI Specifications

- **User Management**: http://localhost:8081/api-docs
- **Product Catalog**: http://localhost:8082/api-docs
- **Shopping Cart**: http://localhost:8083/api-docs

## Testing

### Run Unit Tests

```bash
mvn test
```

### Run Integration Tests

```bash
mvn verify
```

### Test Coverage

```bash
mvn jacoco:report
```

Coverage reports will be available at `target/site/jacoco/index.html`

## Authentication Flow

1. **Register User**: `POST /api/users/register`
2. **Login**: `POST /api/users/login` - Returns JWT token
3. **Use Token**: Include token in Authorization header: `Bearer {token}`
4. **Access Protected Endpoints**: All authenticated endpoints require valid JWT token

### Example Authentication

```bash
# Register
curl -X POST http://localhost:8081/api/users/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "john_doe",
    "email": "john@example.com",
    "password": "SecurePass123!",
    "firstName": "John",
    "lastName": "Doe"
  }'

# Login
curl -X POST http://localhost:8081/api/users/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "john@example.com",
    "password": "SecurePass123!"
  }'

# Use token for authenticated requests
curl -X GET http://localhost:8081/api/users/profile \
  -H "Authorization: Bearer {your_jwt_token}"
```

## Troubleshooting

### Common Issues

#### 1. Database Connection Failed

**Problem**: Cannot connect to PostgreSQL database

**Solution**:
- Verify PostgreSQL is running: `pg_isready`
- Check database credentials in `application.yml`
- Ensure database exists: `psql -l`
- Check firewall settings

#### 2. Redis Connection Failed

**Problem**: Cannot connect to Redis

**Solution**:
- Verify Redis is running: `redis-cli ping`
- Check Redis host and port in `application.yml`
- Ensure Redis is accessible from application

#### 3. JWT Token Invalid

**Problem**: 401 Unauthorized error with valid token

**Solution**:
- Verify JWT secret is consistent across services
- Check token expiration time
- Ensure token format is correct: `Bearer {token}`
- Verify token is not expired

#### 4. Port Already in Use

**Problem**: Address already in use error

**Solution**:
```bash
# Find process using port
lsof -i :8081

# Kill process
kill -9 {PID}

# Or change port in application.yml
server:
  port: 8084
```

#### 5. Flyway Migration Failed

**Problem**: Database migration errors

**Solution**:
- Check migration scripts in `src/main/resources/db/migration`
- Verify database schema permissions
- Clean and rebuild: `mvn clean install -DskipTests`
- Reset Flyway: `mvn flyway:clean flyway:migrate`

#### 6. Service-to-Service Communication Failed

**Problem**: Shopping Cart cannot reach User/Product services

**Solution**:
- Verify all services are running
- Check service URLs in `application.yml`
- Ensure network connectivity between services
- Check circuit breaker configuration

### Preventive Measures

1. **Keep Dependencies Updated**: Regularly update Spring Boot and dependencies
2. **Monitor Logs**: Check application logs for warnings and errors
3. **Database Backups**: Regular database backups for data safety
4. **Health Checks**: Use Spring Boot Actuator endpoints for monitoring
5. **Load Testing**: Perform load testing before production deployment
6. **Security Audits**: Regular security audits and penetration testing

## Monitoring and Observability

### Health Checks

```bash
# User Management Service
curl http://localhost:8081/actuator/health

# Product Catalog Service
curl http://localhost:8082/actuator/health

# Shopping Cart Service
curl http://localhost:8083/actuator/health
```

### Metrics

```bash
# Prometheus metrics
curl http://localhost:8081/actuator/prometheus
```

### Logging

Logs are structured in JSON format and include:
- Request/Response details
- User actions (audit logs)
- Error traces
- Performance metrics

## Security Considerations

1. **Password Security**: BCrypt with cost factor 12
2. **JWT Security**: RSA-256 signing, short expiration times
3. **TLS/SSL**: Use TLS 1.3 in production
4. **Input Validation**: All inputs validated using Bean Validation
5. **SQL Injection**: Protected by JPA/Hibernate parameterized queries
6. **CORS**: Configure CORS policies for production
7. **Rate Limiting**: Implement rate limiting for API endpoints
8. **Secrets Management**: Use AWS Secrets Manager or HashiCorp Vault

## Performance Optimization

1. **Caching**: Redis caching for frequently accessed data
2. **Connection Pooling**: HikariCP for database connections
3. **Lazy Loading**: Lazy initialization of entities
4. **Pagination**: Paginated responses for large datasets
5. **Indexing**: Database indexes on frequently queried columns
6. **Circuit Breaker**: Resilience4j for fault tolerance

## Compliance

- **GDPR**: User data protection and right to deletion
- **SOC2**: Audit logging and access controls
- **ISO27001**: Information security management
- **OWASP Top 10**: Security best practices implemented

## Contributing

1. Fork the repository
2. Create feature branch: `git checkout -b feature/new-feature`
3. Commit changes: `git commit -am 'Add new feature'`
4. Push to branch: `git push origin feature/new-feature`
5. Submit pull request

## License

Apache License 2.0

## Support

For issues and questions:
- Email: support@ecommerce.com
- GitHub Issues: [Create Issue](https://github.com/ecommerce/platform/issues)
- Documentation: [Wiki](https://github.com/ecommerce/platform/wiki)

## Version History

- **1.0.0** (2024-01-15): Initial release
  - User Management Service
  - Product Catalog Service
  - Shopping Cart Service
  - JWT Authentication
  - Swagger Documentation
  - Kubernetes Deployment

---

**Generated by**: E-Commerce Platform Code Generation System
**Date**: 2024-01-15
**Version**: 1.0.0