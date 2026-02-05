# Shopping Cart System - Backend Engineering Specification (SCRUM-96)

## Executive Summary

Complete Spring Boot MVC application for Shopping Cart System with:
- ✅ User management (registration, authentication, profile)
- ✅ Product catalog with case-insensitive search
- ✅ Shopping cart operations (lazy creation, auto-delete, logout cleanup)
- ✅ JWT-based stateless authentication
- ✅ PostgreSQL database with Flyway migrations
- ✅ Comprehensive business rule enforcement
- ✅ RESTful API with validation and error handling

---

## Table of Contents

1. [Architecture Overview](#architecture-overview)
2. [Technology Stack](#technology-stack)
3. [Setup Instructions](#setup-instructions)
4. [Database Configuration](#database-configuration)
5. [API Documentation](#api-documentation)
6. [Business Rules](#business-rules)
7. [Testing Guide](#testing-guide)
8. [Troubleshooting](#troubleshooting)
9. [Deployment](#deployment)
10. [Future Enhancements](#future-enhancements)

---

## Architecture Overview

### MVC Architecture

```
┌─────────────────────────────────────────────────────────┐
│                    Controller Layer                      │
│  (REST Endpoints, Request/Response Handling)            │
└─────────────────────────────────────────────────────────┘
                          ↓
┌─────────────────────────────────────────────────────────┐
│                     Service Layer                        │
│  (Business Logic, Validation, Transactions)             │
└─────────────────────────────────────────────────────────┘
                          ↓
┌─────────────────────────────────────────────────────────┐
│                   Repository Layer                       │
│  (Data Access, JPA, Database Operations)                │
└─────────────────────────────────────────────────────────┘
                          ↓
┌─────────────────────────────────────────────────────────┐
│                  PostgreSQL Database                     │
└─────────────────────────────────────────────────────────┘
```

### Package Structure

```
com.ecommerce/
├── controller/          # REST Controllers
│   ├── UserController
│   ├── ProductController
│   ├── CartController
│   └── AuthController
├── service/            # Business Logic
│   ├── UserService
│   ├── ProductService
│   └── CartService
├── repository/         # Data Access
│   ├── UserRepository
│   ├── ProductRepository
│   ├── CartRepository
│   └── CartItemRepository
├── entity/            # JPA Entities
│   ├── User
│   ├── Product
│   ├── Cart
│   ├── CartItem
│   └── UserAddress
├── dto/               # Data Transfer Objects
├── exception/         # Exception Handling
├── security/          # JWT & Security Config
└── config/            # Application Configuration
```

---

## Technology Stack

- **Java**: 17
- **Spring Boot**: 3.2.0
- **Spring Data JPA**: Database access
- **Spring Security**: Authentication & Authorization
- **PostgreSQL**: 14+
- **Flyway**: Database migrations
- **JWT**: Stateless authentication
- **Lombok**: Boilerplate reduction
- **Maven**: Build tool
- **Swagger/OpenAPI**: API documentation

---

## Setup Instructions

### Prerequisites

1. **Java 17** or higher
2. **Maven 3.8+**
3. **PostgreSQL 14+**
4. **Git**

### Step 1: Clone Repository

```bash
git clone https://github.com/NavneetBN47/ecommerce.git
cd ecommerce
git checkout feature_2026-02-05-14-37-56
```

### Step 2: Database Setup

```bash
# Create database
psql -U postgres
CREATE DATABASE ecommerce_db;
\q

# Run DDL script
psql -U postgres -d ecommerce_db -f DB/ddl.sql

# Run DML script (seed data)
psql -U postgres -d ecommerce_db -f DB/dml.sql
```

### Step 3: Configure Application

Edit `src/main/resources/application.properties`:

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/ecommerce_db
spring.datasource.username=postgres
spring.datasource.password=your_password

jwt.secret=your_secret_key_here
jwt.expiration=86400000
```

### Step 4: Build Application

```bash
mvn clean install
```

### Step 5: Run Application

```bash
mvn spring-boot:run
```

Application will start on `http://localhost:8080`

---

## Database Configuration

### Schema Overview

- **users**: User accounts and authentication
- **products**: Product catalog
- **carts**: Shopping carts (one active per user)
- **cart_items**: Items in carts
- **user_addresses**: User shipping/billing addresses
- **product_categories**: Product categorization

### Migration Scripts

- `V001__initial_schema.sql`: Create tables, constraints, indexes
- `V002__create_triggers.sql`: Automatic timestamp updates, cart totals

### Key Constraints

1. **Username**: Unique, immutable, min 3 characters
2. **Email**: Unique, valid format
3. **Cart**: One active cart per user
4. **Cart Item**: Quantity > 0, unique product per cart
5. **Product**: Price > 0, stock >= 0

---

## API Documentation

### Base URL

```
http://localhost:8080/api
```

### Authentication

All endpoints except `/users/signup`, `/users/login`, and `/products` require JWT token:

```
Authorization: Bearer <token>
```

### User Management

#### 1. Register User

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
  "createdAt": "2024-01-01T00:00:00Z",
  "token": "jwt_token"
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
  "id": "uuid",
  "username": "john_doe",
  "fullName": "John Doe",
  "email": "john@example.com",
  "createdAt": "2024-01-01T00:00:00Z",
  "token": "jwt_token"
}
```

#### 3. Get Profile

```http
GET /api/users/me
Authorization: Bearer <token>

Response: 200 OK
{
  "id": "uuid",
  "username": "john_doe",
  "fullName": "John Doe",
  "email": "john@example.com",
  "createdAt": "2024-01-01T00:00:00Z"
}
```

#### 4. Update Profile

```http
PUT /api/users/me
Authorization: Bearer <token>
Content-Type: application/json

{
  "fullName": "John Updated Doe",
  "email": "john.updated@example.com"
}

Response: 200 OK
```

### Product Catalog

#### 5. Search Products

```http
GET /api/products?search=laptop

Response: 200 OK
[
  {
    "id": "uuid",
    "name": "Wireless Bluetooth Headphones",
    "description": "High-quality wireless headphones",
    "price": 199.99,
    "availableQty": 50,
    "category": "Electronics",
    "brand": "TechBrand",
    "sku": "TB-WH-001"
  }
]
```

### Shopping Cart

#### 6. Get Cart

```http
GET /api/cart
Authorization: Bearer <token>

Response: 200 OK
{
  "cartId": "uuid",
  "items": [
    {
      "itemId": "uuid",
      "productId": "uuid",
      "name": "Product Name",
      "description": "Product Description",
      "unitPrice": 199.99,
      "quantity": 2,
      "total": 399.98
    }
  ],
  "grandTotal": 399.98,
  "itemCount": 2
}
```

#### 7. Add to Cart

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

#### 8. Update Cart Item

```http
PUT /api/cart/items/{itemId}
Authorization: Bearer <token>
Content-Type: application/json

{
  "quantity": 3
}

Response: 200 OK
```

#### 9. Remove Cart Item

```http
DELETE /api/cart/items/{itemId}
Authorization: Bearer <token>

Response: 200 OK
```

#### 10. Logout (Cart Cleanup)

```http
POST /api/logout
Authorization: Bearer <token>

Response: 200 OK
{
  "message": "Logged out. Cart deleted."
}
```

### Error Responses

```json
{
  "errorCode": "USERNAME_EXISTS",
  "message": "Username already exists",
  "details": "Username 'john_doe' is already taken",
  "timestamp": "2024-01-01T00:00:00Z",
  "path": "/api/users/signup"
}
```

---

## Business Rules

### User Management

1. ✅ Username must be unique and immutable
2. ✅ Email must be unique and valid format
3. ✅ Password stored as BCrypt hash
4. ✅ Stateless authentication via JWT

### Product Catalog

1. ✅ Case-insensitive keyword search in name and description
2. ✅ Price must be > 0
3. ✅ Stock quantity must be >= 0

### Shopping Cart

1. ✅ **Lazy Cart Creation**: Cart created when first item added
2. ✅ **One Active Cart Per User**: Only one active cart allowed
3. ✅ **Auto-Delete Empty Cart**: Cart deleted when last item removed
4. ✅ **Logout Cleanup**: Cart and all items deleted on logout
5. ✅ **Quantity Validation**: Quantity must be > 0
6. ✅ **Product Existence**: Product must exist before adding to cart
7. ✅ **Cart Totals**: Automatically calculated via database triggers

---

## Testing Guide

### Manual Testing with cURL

#### 1. Register User

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

#### 2. Login

```bash
curl -X POST http://localhost:8080/api/users/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "password": "Test1234"
  }'
```

#### 3. Search Products

```bash
curl -X GET "http://localhost:8080/api/products?search=headphones"
```

#### 4. Add to Cart

```bash
curl -X POST http://localhost:8080/api/cart/items \
  -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json" \
  -d '{
    "productId": "<product_uuid>",
    "quantity": 2
  }'
```

### Swagger UI

Access interactive API documentation:

```
http://localhost:8080/swagger-ui.html
```

---

## Troubleshooting

### Common Issues

#### 1. Database Connection Failed

**Error**: `Connection refused`

**Solution**:
- Verify PostgreSQL is running: `pg_isready`
- Check connection details in `application.properties`
- Ensure database exists: `psql -l`

#### 2. Flyway Migration Failed

**Error**: `Migration checksum mismatch`

**Solution**:
```bash
# Clean Flyway history
psql -U postgres -d ecommerce_db
DELETE FROM flyway_schema_history;
\q

# Restart application
mvn spring-boot:run
```

#### 3. JWT Token Invalid

**Error**: `401 Unauthorized`

**Solution**:
- Verify token in Authorization header: `Bearer <token>`
- Check token expiration (default 24 hours)
- Login again to get new token

#### 4. Cart Not Found

**Error**: `404 CART_NOT_FOUND`

**Solution**:
- Cart is created lazily when first item added
- Add product to cart first: `POST /api/cart/items`

#### 5. Product Not Found

**Error**: `404 PRODUCT_NOT_FOUND`

**Solution**:
- Verify product exists: `GET /api/products`
- Check product UUID is correct
- Run DML script to seed products

---

## Deployment

### Production Build

```bash
mvn clean package -DskipTests
```

### Docker Deployment

```dockerfile
FROM openjdk:17-jdk-slim
WORKDIR /app
COPY target/shopping-cart-system-1.0.0.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
```

```bash
docker build -t shopping-cart-system .
docker run -p 8080:8080 shopping-cart-system
```

### Environment Variables

```bash
export SPRING_DATASOURCE_URL=jdbc:postgresql://prod-db:5432/ecommerce_db
export SPRING_DATASOURCE_USERNAME=prod_user
export SPRING_DATASOURCE_PASSWORD=prod_password
export JWT_SECRET=production_secret_key
```

---

## Future Enhancements

1. **Checkout Flow**: Order creation, payment processing
2. **Inventory Management**: Stock reservation, low stock alerts
3. **Admin Features**: Product management, user management
4. **Password Management**: Reset, change password
5. **User Roles**: Admin, customer, guest
6. **Cart Persistence**: Save cart across sessions
7. **Wishlist**: Save products for later
8. **Product Reviews**: Ratings and reviews
9. **Order History**: View past orders
10. **Email Notifications**: Order confirmation, shipping updates

---

## Quality Metrics

- ✅ **100% API Coverage**: All LLD endpoints implemented
- ✅ **Business Rules Enforced**: All validation rules in place
- ✅ **Database Constraints**: FK, UK, CHECK constraints applied
- ✅ **Error Handling**: Standardized error responses
- ✅ **Security**: JWT authentication, password hashing
- ✅ **Logging**: Comprehensive logging at all layers
- ✅ **Documentation**: Complete API and setup documentation

---

## Support

For issues or questions:
- Review this README
- Check Troubleshooting section
- Review API documentation in Swagger UI
- Check application logs: `logs/spring-boot-application.log`

---

**Version**: 1.0.0  
**Last Updated**: 2024-01-01  
**Status**: ✅ Production Ready
