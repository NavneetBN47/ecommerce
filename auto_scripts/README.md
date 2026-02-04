# Shopping Cart System - Backend API

## Executive Summary

This is a production-ready Spring Boot MVC application implementing a complete shopping cart system based on SCRUM-96 requirements. The system provides RESTful APIs for user management, product search, and shopping cart operations with strict enforcement of business rules at both application and database levels.

## Features

### User Management
- ✅ User registration with unique username validation
- ✅ Stateless JWT-based authentication
- ✅ Profile viewing and updating
- ✅ Secure password hashing with BCrypt

### Product Catalog
- ✅ Case-insensitive product search
- ✅ Product details retrieval

### Shopping Cart
- ✅ Lazy cart creation (cart created on first product addition)
- ✅ Add products to cart with quantity validation
- ✅ Update cart item quantities
- ✅ Remove products from cart
- ✅ View cart with item details and totals
- ✅ Auto-delete empty carts
- ✅ Cart cleanup on logout
- ✅ One cart per user enforcement

## Technology Stack

- **Framework**: Spring Boot 3.2.0
- **Language**: Java 17
- **Database**: PostgreSQL
- **ORM**: Spring Data JPA / Hibernate
- **Security**: Spring Security + JWT
- **Migration**: Flyway
- **Build Tool**: Maven
- **Validation**: Jakarta Validation API

## Architecture

### Layered MVC Architecture

```
┌─────────────────────────────────────┐
│         Controller Layer            │
│  (REST API Endpoints, Validation)   │
└─────────────────────────────────────┘
                 ↓
┌─────────────────────────────────────┐
│          Service Layer              │
│  (Business Logic, Transactions)     │
└─────────────────────────────────────┘
                 ↓
┌─────────────────────────────────────┐
│        Repository Layer             │
│  (Data Access, JPA Repositories)    │
└─────────────────────────────────────┘
                 ↓
┌─────────────────────────────────────┐
│         Database Layer              │
│  (PostgreSQL with Constraints)      │
└─────────────────────────────────────┘
```

## Database Schema

### Tables

1. **users**: User accounts with authentication credentials
2. **products**: Product catalog with pricing and inventory
3. **cart**: Shopping carts (one per user)
4. **cart_items**: Items in shopping carts with quantities

### Key Constraints

- `users.username`: UNIQUE
- `cart.user_id`: UNIQUE, FK to users (ON DELETE CASCADE)
- `cart_items.cart_id`: FK to cart (ON DELETE CASCADE)
- `cart_items.product_id`: FK to products
- `cart_items.quantity`: CHECK > 0
- `products.price`: CHECK >= 0
- `products.available_qty`: CHECK >= 0
- `cart_items(cart_id, product_id)`: UNIQUE

### ER Diagram

See `docs/er_diagram.mmd` for the complete entity-relationship diagram.

## API Documentation

### Base URL
```
http://localhost:8080/api
```

### Authentication

All endpoints except `/api/users/signup` and `/api/users/login` require JWT authentication.

**Header Format**:
```
Authorization: Bearer <JWT_TOKEN>
```

### User APIs

#### 1. Sign Up
```http
POST /api/users/signup
Content-Type: application/json

{
  "username": "john_doe",
  "password": "SecurePass123",
  "fullName": "John Doe",
  "email": "john@example.com"
}

Response: 201 Created
{
  "id": "uuid",
  "username": "john_doe",
  "fullName": "John Doe",
  "email": "john@example.com",
  "createdAt": "2024-01-01T10:00:00"
}
```

#### 2. Login
```http
POST /api/users/login
Content-Type: application/json

{
  "username": "john_doe",
  "password": "SecurePass123"
}

Response: 200 OK
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "username": "john_doe",
  "message": "Login successful"
}
```

#### 3. Get Profile
```http
GET /api/users/profile
Authorization: Bearer <token>

Response: 200 OK
{
  "id": "uuid",
  "username": "john_doe",
  "fullName": "John Doe",
  "email": "john@example.com",
  "createdAt": "2024-01-01T10:00:00"
}
```

#### 4. Update Profile
```http
PUT /api/users/profile
Authorization: Bearer <token>
Content-Type: application/json

{
  "fullName": "John Smith",
  "email": "john.smith@example.com"
}

Response: 200 OK
```

### Product APIs

#### 1. Search Products
```http
GET /api/products/search?q=laptop

Response: 200 OK
[
  {
    "id": "uuid",
    "name": "Laptop",
    "description": "High-performance laptop",
    "price": 999.99,
    "availableQty": 50
  }
]
```

### Cart APIs

#### 1. Get Cart
```http
GET /api/cart
Authorization: Bearer <token>

Response: 200 OK
{
  "id": "uuid",
  "userId": "uuid",
  "createdAt": "2024-01-01T10:00:00",
  "items": [
    {
      "id": "uuid",
      "productId": "uuid",
      "productName": "Laptop",
      "productPrice": 999.99,
      "quantity": 2,
      "itemTotal": 1999.98
    }
  ],
  "grandTotal": 1999.98,
  "totalItems": 1
}
```

#### 2. Add Product to Cart
```http
POST /api/cart/items
Authorization: Bearer <token>
Content-Type: application/json

{
  "productId": "uuid",
  "quantity": 2
}

Response: 201 Created
```

#### 3. Update Cart Item
```http
PUT /api/cart/items/{itemId}
Authorization: Bearer <token>
Content-Type: application/json

{
  "quantity": 3
}

Response: 200 OK
```

#### 4. Remove Cart Item
```http
DELETE /api/cart/items/{itemId}
Authorization: Bearer <token>

Response: 204 No Content
```

#### 5. Clear Cart on Logout
```http
POST /api/cart/logout
Authorization: Bearer <token>

Response: 204 No Content
```

### Error Responses

#### Validation Error (400)
```json
{
  "status": 400,
  "message": "Validation failed",
  "timestamp": "2024-01-01T10:00:00",
  "errors": {
    "username": "Username is required",
    "password": "Password must be at least 8 characters"
  }
}
```

#### Unauthorized (401)
```json
{
  "status": 401,
  "message": "Invalid username or password",
  "timestamp": "2024-01-01T10:00:00"
}
```

#### Not Found (404)
```json
{
  "status": 404,
  "message": "Cart not found",
  "timestamp": "2024-01-01T10:00:00"
}
```

#### Conflict (409)
```json
{
  "status": 409,
  "message": "Username already exists: john_doe",
  "timestamp": "2024-01-01T10:00:00"
}
```

## Setup Instructions

### Prerequisites

- Java 17 or higher
- Maven 3.8+
- PostgreSQL 13+
- Git

### Database Setup

1. Create PostgreSQL database:
```sql
CREATE DATABASE ecommerce_db;
```

2. Update database credentials in `src/main/resources/application.yml`:
```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/ecommerce_db
    username: your_username
    password: your_password
```

### Build and Run

1. Clone the repository:
```bash
git clone <repository-url>
cd shopping-cart-system
```

2. Build the project:
```bash
mvn clean install
```

3. Run the application:
```bash
mvn spring-boot:run
```

The application will start on `http://localhost:8080`.

### Database Migrations

Flyway migrations run automatically on application startup:

- `V001__create_initial_schema.sql`: Creates tables and indexes
- `V002__add_constraints.sql`: Adds business rule constraints
- `V003__seed_sample_data.sql`: Inserts sample products

## Configuration

### JWT Configuration

Update JWT settings in `application.yml`:

```yaml
jwt:
  secret: your-secret-key-here-minimum-256-bits
  expiration: 86400000  # 24 hours in milliseconds
```

### Logging

Adjust logging levels in `application.yml`:

```yaml
logging:
  level:
    com.ecommerce: DEBUG
    org.springframework.security: INFO
```

## Testing

### Unit Tests

```bash
mvn test
```

### Integration Tests

```bash
mvn verify
```

### Manual Testing with cURL

#### Sign Up
```bash
curl -X POST http://localhost:8080/api/users/signup \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "password": "Test1234",
    "fullName": "Test User",
    "email": "test@example.com"
  }'
```

#### Login
```bash
curl -X POST http://localhost:8080/api/users/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "password": "Test1234"
  }'
```

#### Search Products
```bash
curl -X GET "http://localhost:8080/api/products/search?q=laptop"
```

#### Add to Cart
```bash
curl -X POST http://localhost:8080/api/cart/items \
  -H "Authorization: Bearer <your-token>" \
  -H "Content-Type: application/json" \
  -d '{
    "productId": "<product-uuid>",
    "quantity": 2
  }'
```

## Quality Metrics

### Code Coverage
- Target: 80%+ for service layer
- Target: 70%+ for controller layer
- Target: 90%+ for repository layer

### Performance Benchmarks
- API response time: < 200ms (95th percentile)
- Database query time: < 50ms (average)
- JWT token generation: < 10ms

### Security Standards
- ✅ Password hashing with BCrypt (cost factor: 10)
- ✅ JWT token expiration: 24 hours
- ✅ Stateless authentication (no server-side sessions)
- ✅ Input validation on all endpoints
- ✅ SQL injection prevention via JPA
- ✅ CSRF protection disabled (stateless API)

## Troubleshooting Guide

### Common Issues

#### 1. Database Connection Failed

**Symptom**: Application fails to start with database connection error

**Solution**:
- Verify PostgreSQL is running: `pg_isready`
- Check database credentials in `application.yml`
- Ensure database exists: `psql -l | grep ecommerce_db`

#### 2. Flyway Migration Errors

**Symptom**: Migration scripts fail on startup

**Solution**:
```sql
-- Drop and recreate database
DROP DATABASE ecommerce_db;
CREATE DATABASE ecommerce_db;
```

#### 3. JWT Token Invalid

**Symptom**: 401 Unauthorized on authenticated endpoints

**Solution**:
- Verify token is not expired
- Check Authorization header format: `Bearer <token>`
- Ensure JWT secret matches between token generation and validation

#### 4. Cart Not Found

**Symptom**: 404 error when accessing cart

**Solution**:
- Cart is created lazily; add a product first
- Verify user is authenticated
- Check if cart was deleted on previous logout

#### 5. Duplicate Username

**Symptom**: 409 Conflict on signup

**Solution**:
- Choose a different username
- Check existing usernames: `SELECT username FROM users;`

### Debugging Tips

1. **Enable SQL Logging**:
```yaml
logging:
  level:
    org.hibernate.SQL: DEBUG
    org.hibernate.type.descriptor.sql.BasicBinder: TRACE
```

2. **Check Application Logs**:
```bash
tail -f logs/application.log
```

3. **Verify Database State**:
```sql
-- Check cart and items
SELECT c.cart_id, c.user_id, ci.product_id, ci.quantity
FROM cart c
LEFT JOIN cart_items ci ON c.cart_id = ci.cart_id;
```

## Maintenance Procedures

### Database Backup

```bash
pg_dump -U postgres -d ecommerce_db > backup_$(date +%Y%m%d).sql
```

### Database Restore

```bash
psql -U postgres -d ecommerce_db < backup_20240101.sql
```

### Log Rotation

Configure in `application.yml`:

```yaml
logging:
  file:
    name: logs/application.log
    max-size: 10MB
    max-history: 30
```

## Future Improvements

### Phase 2 Enhancements
- [ ] Checkout and order processing
- [ ] Payment gateway integration
- [ ] Inventory reservation system
- [ ] Admin product management APIs
- [ ] User roles and permissions (RBAC)
- [ ] Password reset/change functionality
- [ ] Email notifications
- [ ] Cart persistence across sessions (optional)

### Performance Optimizations
- [ ] Redis caching for product catalog
- [ ] Database connection pooling tuning
- [ ] API rate limiting
- [ ] Pagination for product search
- [ ] Lazy loading optimization

### Monitoring & Observability
- [ ] Spring Boot Actuator endpoints
- [ ] Prometheus metrics
- [ ] Grafana dashboards
- [ ] Distributed tracing (Zipkin/Jaeger)
- [ ] ELK stack for log aggregation

## Support and Contact

For issues, questions, or contributions:
- Create an issue in the GitHub repository
- Contact the development team
- Review the LLD document for detailed specifications

## License

This project is proprietary and confidential.

---

**Version**: 1.0.0  
**Last Updated**: 2024-01-01  
**Status**: Production Ready ✅