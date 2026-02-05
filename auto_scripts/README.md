# E-Commerce Shopping Cart System - Backend API

## Overview

A production-ready RESTful API for an e-commerce shopping cart system built with Spring Boot 3.2, PostgreSQL, and JWT authentication. This system provides comprehensive user management, product catalog, and shopping cart functionality with enterprise-grade security and performance optimizations.

## Features

### Core Functionality
- **User Management**: Registration, authentication, and profile management
- **Product Catalog**: Browse, search (case-insensitive), and filter products by category
- **Shopping Cart**: 
  - Lazy cart creation (cart created only when first item is added)
  - Add/update/remove items with real-time stock validation
  - Automatic cart deletion when empty
  - Cart cleanup on user logout
  - Real-time total calculation
- **Category Management**: Organize products into categories
- **Stateless Authentication**: JWT-based authentication for scalability

### Technical Features
- RESTful API design with proper HTTP methods and status codes
- Comprehensive validation and error handling
- Database migration management with Flyway
- Optimized database queries with proper indexing
- Transaction management for data consistency
- Audit trails with automatic timestamp tracking
- CORS configuration for frontend integration

## Technology Stack

- **Framework**: Spring Boot 3.2.0
- **Language**: Java 17
- **Database**: PostgreSQL 15+
- **Security**: Spring Security 6 with JWT
- **ORM**: Spring Data JPA / Hibernate
- **Migration**: Flyway
- **Build Tool**: Maven
- **Additional Libraries**:
  - Lombok (boilerplate reduction)
  - JJWT (JWT token handling)
  - Jakarta Validation (input validation)

## Prerequisites

- Java 17 or higher
- Maven 3.8+
- PostgreSQL 15+
- Git

## Installation & Setup

### 1. Clone the Repository

```bash
git clone https://github.com/NavneetBN47/ecommerce.git
cd ecommerce/api-springboot
```

### 2. Database Setup

```bash
# Create PostgreSQL database
psql -U postgres
CREATE DATABASE ecommerce_db;
\q
```

### 3. Configure Application

Update `src/main/resources/application.yml` with your database credentials:

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/ecommerce_db
    username: your_username
    password: your_password
```

### 4. Build the Application

```bash
mvn clean install
```

### 5. Run Database Migrations

Migrations run automatically on application startup via Flyway.

### 6. Start the Application

```bash
mvn spring-boot:run
```

The API will be available at `http://localhost:8080`

## API Documentation

### Authentication Endpoints

#### Register User
```http
POST /api/auth/register
Content-Type: application/json

{
  "username": "john_doe",
  "email": "john@example.com",
  "password": "SecurePass@123",
  "fullName": "John Doe",
  "phone": "+1234567890",
  "address": "123 Main St, City, State"
}
```

#### Login
```http
POST /api/auth/login
Content-Type: application/json

{
  "usernameOrEmail": "john_doe",
  "password": "SecurePass@123"
}

Response:
{
  "success": true,
  "message": "Login successful",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "tokenType": "Bearer",
    "expiresIn": 86400000,
    "user": { ... }
  }
}
```

#### Logout
```http
POST /api/auth/logout
Authorization: Bearer <token>
```

### Product Endpoints

#### Get All Products (Paginated)
```http
GET /api/products?page=0&size=10&sortBy=productName&sortDir=ASC
```

#### Search Products (Case-Insensitive)
```http
GET /api/products/search?query=laptop&page=0&size=10
```

#### Get Products by Category
```http
GET /api/products/category/{categoryId}?page=0&size=10
```

#### Get Product by ID
```http
GET /api/products/{id}
```

### Cart Endpoints

#### Get Cart (Lazy Creation)
```http
GET /api/cart
Authorization: Bearer <token>
```

#### Add Item to Cart
```http
POST /api/cart/items
Authorization: Bearer <token>
Content-Type: application/json

{
  "productId": 1,
  "quantity": 2
}
```

#### Update Cart Item
```http
PUT /api/cart/items/{cartItemId}
Authorization: Bearer <token>
Content-Type: application/json

{
  "quantity": 3
}
```

#### Remove Item from Cart
```http
DELETE /api/cart/items/{cartItemId}
Authorization: Bearer <token>
```

#### Clear Cart
```http
DELETE /api/cart
Authorization: Bearer <token>
```

### Category Endpoints

#### Get All Categories
```http
GET /api/categories
```

#### Get Category by ID
```http
GET /api/categories/{id}
```

## Business Logic Implementation

### Cart Management

1. **Lazy Cart Creation**: Cart is created only when the first item is added
2. **Stock Validation**: Real-time validation of product availability before adding to cart
3. **Automatic Deletion**: Empty carts are automatically deleted when:
   - Last item is removed
   - User logs out with an empty cart
4. **Total Calculation**: Cart totals (amount and item count) are recalculated automatically on any cart modification
5. **Duplicate Prevention**: Unique constraint on (cart_id, product_id) prevents duplicate items

### Product Search

- Case-insensitive search using SQL LOWER() function
- Indexed product_name column for performance
- Pagination support for large result sets

### Security

- Stateless JWT authentication (no server-side sessions)
- BCrypt password hashing
- Token expiration (24 hours default)
- CORS configuration for frontend integration
- Method-level security with @PreAuthorize

## Database Schema

### Tables

1. **users**: User accounts and profiles
2. **categories**: Product categories
3. **products**: Product catalog
4. **carts**: Shopping carts (one per user)
5. **cart_items**: Items in shopping carts

### Key Relationships

- Users (1) ←→ (0..1) Carts
- Carts (1) ←→ (0..*) Cart Items
- Products (1) ←→ (0..*) Cart Items
- Categories (1) ←→ (0..*) Products

### Constraints

- Unique: username, email, SKU, (cart_id, product_id)
- Foreign Keys: All relationships with appropriate CASCADE rules
- Check Constraints: price >= 0, stock_quantity >= 0, quantity > 0
- Indexes: All foreign keys and frequently queried columns

## Testing

### Test User Credentials

```
Username: testuser
Email: test@example.com
Password: Test@123
```

### Sample Products

The database is pre-populated with sample products across multiple categories for testing.

## Configuration

### Environment Profiles

- **dev**: Development environment (application-dev.yml)
- **prod**: Production environment (application-prod.yml)

Switch profiles:
```bash
mvn spring-boot:run -Dspring-boot.run.profiles=prod
```

### Key Configuration Properties

```yaml
app:
  jwt:
    secret: <your-secret-key>
    expiration-ms: 86400000  # 24 hours

spring:
  datasource:
    hikari:
      maximum-pool-size: 10
      minimum-idle: 5
```

## Maintenance

### Database Migrations

Migrations are located in `src/main/resources/db/migration/`:

- V001: Initial schema
- V002: Sample data
- V003: Triggers for automatic timestamps

To create a new migration:
```
V004__description.sql
```

### Backup & Restore

```bash
# Backup
pg_dump -U postgres ecommerce_db > backup.sql

# Restore
psql -U postgres ecommerce_db < backup.sql
```

## Troubleshooting

See [TROUBLESHOOTING.md](docs/TROUBLESHOOTING.md) for common issues and solutions.

## Performance Optimization

1. **Database Indexing**: All foreign keys and frequently queried columns are indexed
2. **Connection Pooling**: HikariCP with optimized settings
3. **Query Optimization**: 
   - Lazy loading for relationships
   - Fetch joins where appropriate
   - Pagination for large result sets
4. **Caching**: Consider adding Redis for session management in production

## Security Best Practices

1. Change default JWT secret in production
2. Use environment variables for sensitive configuration
3. Enable HTTPS in production
4. Implement rate limiting for API endpoints
5. Regular security audits and dependency updates

## Future Enhancements

1. Order management and checkout process
2. Payment gateway integration
3. Email notifications
4. Product reviews and ratings
5. Wishlist functionality
6. Admin dashboard
7. Analytics and reporting
8. Redis caching layer
9. Elasticsearch for advanced product search
10. Microservices architecture

## Contributing

Please read CONTRIBUTING.md for details on our code of conduct and the process for submitting pull requests.

## License

This project is licensed under the MIT License - see the LICENSE file for details.

## Support

For support, email support@example.com or create an issue in the GitHub repository.

## Authors

- Shopping Cart Team
- Version: 1.0.0
- Last Updated: 2025-02-05