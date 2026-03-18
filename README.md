# E-Commerce Platform - Spring Boot Microservices

## Project Overview

A comprehensive e-commerce platform built with Spring Boot microservices architecture, featuring user management, product catalog, and shopping cart functionality with complete test coverage.

## Architecture

### Microservices

1. **User Management Service** (Port 8081)
   - User registration and authentication
   - JWT token-based security
   - Profile management

2. **Product Catalog Service** (Port 8082)
   - Product CRUD operations
   - Search and filtering
   - Category management
   - Caching support

3. **Shopping Cart Service** (Port 8083)
   - Cart management
   - Item operations
   - Total calculation

## Technology Stack

### Core Technologies
- **Java 17**
- **Spring Boot 3.2.0**
- **Spring Security** (JWT Authentication)
- **Spring Data JPA**
- **PostgreSQL 14+**
- **Maven 3.6+**

### Testing Stack
- **JUnit 5** - Testing framework
- **Mockito** - Mocking framework
- **Spring Boot Test** - Integration testing
- **MockMvc** - REST API testing
- **H2 Database** - In-memory test database

### Documentation
- **SpringDoc OpenAPI 3.0** (Swagger UI)
- **OpenAPI 3.0 Specification**

## Prerequisites

- Java 17 or higher
- Maven 3.6 or higher
- PostgreSQL 14 or higher
- Git

## Setup Instructions

### 1. Clone Repository

```bash
git clone https://github.com/NavneetBN47/ecommerce.git
cd ecommerce
```

### 2. Database Setup

```sql
-- Create databases for each service
CREATE DATABASE user_management_db;
CREATE DATABASE product_catalog_db;
CREATE DATABASE shopping_cart_db;

-- Create user (optional)
CREATE USER ecommerce_user WITH PASSWORD 'your_password';
GRANT ALL PRIVILEGES ON DATABASE user_management_db TO ecommerce_user;
GRANT ALL PRIVILEGES ON DATABASE product_catalog_db TO ecommerce_user;
GRANT ALL PRIVILEGES ON DATABASE shopping_cart_db TO ecommerce_user;
```

### 3. Configure Application Properties

Update `application.yml` in each service with your database credentials:

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/[database_name]
    username: ecommerce_user
    password: your_password
```

### 4. Build All Services

```bash
# Build all services
mvn clean install

# Or build individual services
cd user-management-service
mvn clean install

cd ../product-catalog-service
mvn clean install

cd ../shopping-cart-service
mvn clean install
```

### 5. Run Services

```bash
# Terminal 1 - User Management Service
cd user-management-service
mvn spring-boot:run

# Terminal 2 - Product Catalog Service
cd product-catalog-service
mvn spring-boot:run

# Terminal 3 - Shopping Cart Service
cd shopping-cart-service
mvn spring-boot:run
```

### 6. Verify Services

- User Management: http://localhost:8081/swagger-ui.html
- Product Catalog: http://localhost:8082/swagger-ui.html
- Shopping Cart: http://localhost:8083/swagger-ui.html

## Running Tests

### Run All Tests

```bash
# Run all tests across all services
mvn clean test

# Run tests with coverage report
mvn clean test jacoco:report
```

### Run Service-Specific Tests

```bash
# User Management Service tests
cd user-management-service
mvn test

# Product Catalog Service tests
cd product-catalog-service
mvn test

# Shopping Cart Service tests
cd shopping-cart-service
mvn test
```

### Run Specific Test Classes

```bash
# Run specific test class
mvn test -Dtest=UserControllerTest

# Run specific test method
mvn test -Dtest=UserControllerTest#testRegisterUser_Success
```

### View Test Coverage Report

After running tests with JaCoCo:

```bash
# Coverage report location
open target/site/jacoco/index.html
```

## API Documentation

### Swagger UI Access

- **User Management API**: http://localhost:8081/swagger-ui.html
- **Product Catalog API**: http://localhost:8082/swagger-ui.html
- **Shopping Cart API**: http://localhost:8083/swagger-ui.html

### OpenAPI Specification

- **User Management**: http://localhost:8081/v3/api-docs
- **Product Catalog**: http://localhost:8082/v3/api-docs
- **Shopping Cart**: http://localhost:8083/v3/api-docs

## Usage Examples

### 1. User Registration

```bash
curl -X POST http://localhost:8081/api/users/register \
  -H "Content-Type: application/json" \
  -d '{
    "email": "user@example.com",
    "password": "SecurePass123!",
    "firstName": "John",
    "lastName": "Doe"
  }'
```

**Response:**
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "email": "user@example.com",
  "userId": 1
}
```

### 2. User Login

```bash
curl -X POST http://localhost:8081/api/users/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "user@example.com",
    "password": "SecurePass123!"
  }'
```

### 3. Create Product (Admin)

```bash
curl -X POST http://localhost:8082/api/products \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -d '{
    "name": "Laptop",
    "description": "High-performance laptop",
    "price": 999.99,
    "category": "Electronics",
    "stockQuantity": 50
  }'
```

### 4. Add Item to Cart

```bash
curl -X POST http://localhost:8083/api/cart/add \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -d '{
    "userId": 1,
    "productId": 1,
    "quantity": 2
  }'
```

### 5. Get Cart

```bash
curl -X GET http://localhost:8083/api/cart/1 \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

## Testing with Swagger UI

### Step-by-Step Guide

1. **Access Swagger UI**
   - Navigate to http://localhost:8081/swagger-ui.html

2. **Register a User**
   - Expand `POST /api/users/register`
   - Click "Try it out"
   - Enter user details
   - Click "Execute"
   - Copy the JWT token from response

3. **Authenticate**
   - Click "Authorize" button (top right)
   - Enter: `Bearer YOUR_JWT_TOKEN`
   - Click "Authorize"

4. **Test Protected Endpoints**
   - All authenticated endpoints now accessible
   - Test product creation, cart operations, etc.

## Project Structure

```
ecommerce/
├── user-management-service/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/
│   │   │   │   └── com/ecommerce/usermanagement/
│   │   │   │       ├── controller/
│   │   │   │       ├── service/
│   │   │   │       ├── repository/
│   │   │   │       ├── entity/
│   │   │   │       ├── dto/
│   │   │   │       └── config/
│   │   │   └── resources/
│   │   │       └── application.yml
│   │   └── test/
│   │       ├── java/
│   │       │   └── com/ecommerce/usermanagement/
│   │       │       ├── controller/
│   │       │       ├── service/
│   │       │       └── repository/
│   │       └── resources/
│   │           └── application-test.yml
│   └── pom.xml
├── product-catalog-service/
│   └── [similar structure]
├── shopping-cart-service/
│   └── [similar structure]
├── docs/
│   ├── swagger/
│   │   └── openapi.yaml
│   ├── TEST_COVERAGE_REPORT.md
│   ├── SWAGGER_MIGRATION_GUIDE.md
│   └── SWAGGER_INTEGRATION_REPORT.md
└── README.md
```

## Test Coverage

### Overall Metrics

- **Total Endpoints:** 16
- **Endpoints Tested:** 16 (100%)
- **Total Test Cases:** 60+
- **Test Success Rate:** 100%
- **Code Coverage:** 85%+

### Service-Wise Coverage

| Service | Endpoints | Test Cases | Coverage |
|---------|-----------|------------|----------|
| User Management | 4 | 22 | 100% |
| Product Catalog | 7 | 21 | 100% |
| Shopping Cart | 5 | 17 | 100% |

### Test Types

- **Unit Tests:** Controller, Service, Repository layers
- **Integration Tests:** End-to-end API testing
- **Security Tests:** Authentication and authorization
- **Validation Tests:** Input validation and error handling

For detailed coverage report, see [TEST_COVERAGE_REPORT.md](docs/TEST_COVERAGE_REPORT.md)

## Troubleshooting

### Common Issues

#### 1. Port Already in Use

**Problem:** Service fails to start with "Port already in use" error

**Solution:**
```bash
# Find process using port
lsof -i :8081

# Kill process
kill -9 <PID>

# Or change port in application.yml
server:
  port: 8091
```

#### 2. Database Connection Failed

**Problem:** "Connection refused" or "Authentication failed"

**Solution:**
- Verify PostgreSQL is running: `pg_isready`
- Check database credentials in `application.yml`
- Ensure database exists: `psql -l`
- Check PostgreSQL logs: `/var/log/postgresql/`

#### 3. JWT Token Invalid

**Problem:** "Invalid JWT token" or "Token expired"

**Solution:**
- Register new user to get fresh token
- Check token expiration time in configuration
- Verify JWT secret is consistent across services

#### 4. Tests Failing

**Problem:** Tests fail with various errors

**Solution:**
```bash
# Clean and rebuild
mvn clean install

# Run tests with debug logging
mvn test -X

# Check H2 database configuration
cat src/test/resources/application-test.yml
```

#### 5. Swagger UI Not Loading

**Problem:** 404 error when accessing Swagger UI

**Solution:**
- Verify service is running: `curl http://localhost:8081/actuator/health`
- Check SpringDoc dependency in `pom.xml`
- Verify security configuration allows Swagger endpoints
- Clear browser cache

### Preventive Measures

1. **Regular Dependency Updates**
   ```bash
   mvn versions:display-dependency-updates
   ```

2. **Database Backup**
   ```bash
   pg_dump -U ecommerce_user user_management_db > backup.sql
   ```

3. **Log Monitoring**
   - Check application logs regularly
   - Set up log aggregation (ELK stack)
   - Configure alerts for errors

4. **Health Checks**
   ```bash
   # Check service health
   curl http://localhost:8081/actuator/health
   curl http://localhost:8082/actuator/health
   curl http://localhost:8083/actuator/health
   ```

## Security Considerations

### Production Deployment

1. **Environment Variables**
   - Never commit sensitive data to Git
   - Use environment variables for secrets
   - Use secret management tools (Vault, AWS Secrets Manager)

2. **HTTPS Configuration**
   - Enable HTTPS in production
   - Use valid SSL certificates
   - Configure HSTS headers

3. **Database Security**
   - Use strong passwords
   - Enable SSL for database connections
   - Implement connection pooling
   - Regular security patches

4. **API Security**
   - Implement rate limiting
   - Enable CORS properly
   - Use API gateway
   - Implement request validation

5. **Monitoring**
   - Set up application monitoring (Prometheus, Grafana)
   - Enable security audit logging
   - Implement intrusion detection

## Performance Optimization

### Caching

- Product catalog uses Redis caching
- Configure cache TTL appropriately
- Monitor cache hit rates

### Database

- Use connection pooling (HikariCP)
- Implement database indexing
- Regular query optimization
- Use read replicas for scaling

### Load Balancing

- Deploy multiple instances
- Use load balancer (Nginx, HAProxy)
- Implement circuit breakers
- Configure health checks

## Contributing

### Development Workflow

1. Fork the repository
2. Create feature branch: `git checkout -b feature/your-feature`
3. Commit changes: `git commit -am 'Add feature'`
4. Push to branch: `git push origin feature/your-feature`
5. Submit pull request

### Code Standards

- Follow Java coding conventions
- Write unit tests for new features
- Update documentation
- Run tests before committing: `mvn test`
- Use meaningful commit messages

## License

This project is licensed under the MIT License.

## Support

For issues and questions:
- Create GitHub issue
- Email: navneet.bhargavan@ascendion.com
- Documentation: See `/docs` folder

## Changelog

### Version 1.0.0 (2024-01-15)

- Initial release
- User Management Service
- Product Catalog Service
- Shopping Cart Service
- Complete test coverage (100%)
- Swagger/OpenAPI documentation
- JWT authentication
- PostgreSQL integration

---

**Last Updated:** 2024-01-15  
**Version:** 1.0.0  
**Status:** Production Ready ✅
