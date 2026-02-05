# E-Commerce Application

## Overview

Production-ready Spring Boot MVC E-Commerce Application with comprehensive features including user management, product catalog, shopping cart with lazy creation and auto-delete, order processing, and address management.

## Features

### Core Functionality
- **User Management**: Registration, authentication, profile management
- **Product Catalog**: Product browsing, case-insensitive search, category filtering
- **Shopping Cart**: 
  - Lazy cart creation (created only when first item added)
  - Auto-delete when empty (after removing last item)
  - Logout cleanup (cart deleted on user logout)
  - Real-time total calculation
- **Order Processing**: Cart-to-order conversion, order tracking, cancellation
- **Address Management**: Multiple addresses per user, default address support

### Technical Features
- RESTful API with comprehensive endpoints
- Stateless JWT authentication
- Input validation with detailed error messages
- Global exception handling
- Database migration with Flyway
- API documentation with Swagger/OpenAPI
- Comprehensive logging
- Transaction management
- Optimized database queries with proper indexing

## Technology Stack

- **Framework**: Spring Boot 3.2.0
- **Language**: Java 17
- **Database**: MySQL 8.0
- **ORM**: Spring Data JPA / Hibernate
- **Migration**: Flyway
- **Security**: Spring Security with JWT
- **Validation**: Jakarta Validation
- **Mapping**: MapStruct
- **Documentation**: SpringDoc OpenAPI
- **Build Tool**: Maven

## Prerequisites

- Java 17 or higher
- Maven 3.8+
- MySQL 8.0+
- Git

## Setup Instructions

### 1. Clone Repository

```bash
git clone <repository-url>
cd ecommerce-application
```

### 2. Database Setup

Create MySQL database:

```sql
CREATE DATABASE ecommerce_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE USER 'ecommerce_user'@'localhost' IDENTIFIED BY 'your_password';
GRANT ALL PRIVILEGES ON ecommerce_db.* TO 'ecommerce_user'@'localhost';
FLUSH PRIVILEGES;
```

### 3. Configuration

Update `src/main/resources/application.yml`:

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/ecommerce_db
    username: ecommerce_user
    password: your_password
```

Or use environment variables:

```bash
export DB_USERNAME=ecommerce_user
export DB_PASSWORD=your_password
export JWT_SECRET=your_secret_key_here
```

### 4. Build Application

```bash
mvn clean install
```

### 5. Run Application

```bash
mvn spring-boot:run
```

Or run the JAR:

```bash
java -jar target/ecommerce-application-1.0.0.jar
```

Application will start on `http://localhost:8080/api`

## API Documentation

Access Swagger UI at: `http://localhost:8080/api/swagger-ui.html`

API Docs JSON: `http://localhost:8080/api/api-docs`

## API Endpoints

### User Management
- `POST /api/users/register` - Register new user
- `GET /api/users/{id}` - Get user by ID
- `PUT /api/users/{id}` - Update user
- `DELETE /api/users/{id}` - Delete user

### Product Management
- `GET /api/products` - Get all active products
- `GET /api/products/{id}` - Get product by ID
- `GET /api/products/search?query={term}` - Search products (case-insensitive)
- `POST /api/products` - Create product
- `PUT /api/products/{id}` - Update product
- `DELETE /api/products/{id}` - Delete product

### Cart Management
- `GET /api/carts/user/{userId}` - Get or create cart (lazy creation)
- `POST /api/carts/user/{userId}/items` - Add item to cart
- `PUT /api/carts/user/{userId}/items/{itemId}` - Update item quantity
- `DELETE /api/carts/user/{userId}/items/{itemId}` - Remove item (auto-delete cart if empty)
- `DELETE /api/carts/user/{userId}` - Clear cart
- `POST /api/carts/user/{userId}/logout` - Delete cart on logout

### Order Management
- `POST /api/orders/user/{userId}` - Create order from cart
- `GET /api/orders/{id}` - Get order by ID
- `GET /api/orders/user/{userId}` - Get user orders
- `POST /api/orders/{id}/cancel` - Cancel order

### Category Management
- `GET /api/categories` - Get all categories
- `POST /api/categories` - Create category
- `PUT /api/categories/{id}` - Update category

### Address Management
- `POST /api/addresses/user/{userId}` - Create address
- `GET /api/addresses/user/{userId}` - Get user addresses
- `PUT /api/addresses/{id}` - Update address
- `DELETE /api/addresses/{id}` - Delete address

## Database Schema

### Tables
- **users**: User accounts and authentication
- **categories**: Product categories
- **products**: Product catalog
- **addresses**: User shipping/billing addresses
- **carts**: Shopping carts (lazy created, auto-deleted)
- **cart_items**: Items in shopping carts
- **orders**: Customer orders
- **order_items**: Items in orders

### Key Relationships
- User 1:1 Cart (lazy, auto-delete)
- User 1:N Addresses
- User 1:N Orders
- Category 1:N Products
- Cart 1:N CartItems
- Order 1:N OrderItems
- Product N:M CartItems
- Product N:M OrderItems

## Business Rules

### Cart Management
1. **Lazy Creation**: Cart is created only when user adds first item
2. **Auto-Delete**: Cart is automatically deleted when:
   - Last item is removed
   - User explicitly clears cart
   - User logs out
3. **Stock Validation**: Quantity checks before adding/updating items
4. **Real-time Totals**: Cart total recalculated on every change

### Product Search
- Case-insensitive search on product name and description
- Active products only in search results
- Pagination support for large result sets

### Order Processing
1. Stock validation before order creation
2. Stock reduction on order confirmation
3. Stock restoration on order cancellation
4. Cart cleared after successful order creation

## Testing

### Sample User Credentials
- Username: `testuser`
- Password: `Test@1234`
- Email: `test@example.com`

### Run Tests

```bash
mvn test
```

## Troubleshooting

### Common Issues

#### Database Connection Failed
**Symptom**: Application fails to start with database connection error

**Solutions**:
1. Verify MySQL is running: `sudo systemctl status mysql`
2. Check database exists: `SHOW DATABASES;`
3. Verify credentials in `application.yml`
4. Check firewall settings

#### Migration Errors
**Symptom**: Flyway migration fails

**Solutions**:
1. Check migration scripts in `src/main/resources/db/migration`
2. Verify database user has proper privileges
3. Clean and rebuild: `mvn clean install`
4. Reset Flyway: `mvn flyway:clean flyway:migrate`

#### Port Already in Use
**Symptom**: Port 8080 already in use

**Solutions**:
1. Change port in `application.yml`: `server.port: 8081`
2. Kill process using port: `kill -9 $(lsof -t -i:8080)`

#### Cart Not Auto-Deleting
**Symptom**: Empty cart remains in database

**Solutions**:
1. Verify cascade settings in entity relationships
2. Check transaction management
3. Review cart service implementation
4. Check database constraints

## Performance Optimization

### Database
- Proper indexing on frequently queried columns
- Optimized JOIN queries with fetch strategies
- Connection pooling with HikariCP
- Query result caching where appropriate

### Application
- Lazy loading for associations
- Batch processing for bulk operations
- Async processing for non-critical operations
- Stateless architecture for horizontal scaling

## Security

### Implemented Measures
- Password encryption with BCrypt
- JWT-based stateless authentication
- CORS configuration
- SQL injection prevention with JPA
- Input validation and sanitization
- Secure session management

### Best Practices
- Never commit secrets to repository
- Use environment variables for sensitive data
- Regular security updates
- Implement rate limiting for production
- Enable HTTPS in production

## Monitoring

### Health Check
`GET /api/actuator/health`

### Logs
Logs are written to:
- Console: Real-time application logs
- File: `logs/ecommerce-application.log`

### Metrics
Access metrics at: `/api/actuator/metrics`

## Deployment

### Production Checklist
- [ ] Update `application.yml` for production database
- [ ] Set strong JWT secret key
- [ ] Enable HTTPS
- [ ] Configure proper CORS origins
- [ ] Set up database backups
- [ ] Configure log aggregation
- [ ] Set up monitoring and alerting
- [ ] Implement rate limiting
- [ ] Review and harden security settings
- [ ] Set up CI/CD pipeline

### Docker Deployment

Create `Dockerfile`:

```dockerfile
FROM openjdk:17-jdk-slim
WORKDIR /app
COPY target/ecommerce-application-1.0.0.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
```

Build and run:

```bash
docker build -t ecommerce-app .
docker run -p 8080:8080 \
  -e DB_USERNAME=user \
  -e DB_PASSWORD=pass \
  -e JWT_SECRET=secret \
  ecommerce-app
```

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
- Create GitHub issue
- Email: support@ecommerce.com
- Documentation: See `/docs` directory

## Version History

### v1.0.0 (2024)
- Initial release
- Complete user management
- Product catalog with search
- Shopping cart with lazy creation and auto-delete
- Order processing
- Address management
- RESTful API
- Swagger documentation
- Database migrations
- Comprehensive error handling

---

**Generated by Backend Automation Agent**
**Production-Ready Spring Boot MVC E-Commerce Application**