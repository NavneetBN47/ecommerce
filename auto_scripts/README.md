# Shopping Cart System Backend API

## Executive Summary

This project implements a complete Spring Boot MVC application for a Shopping Cart System based on the provided Low-Level Design (LLD) specification. The system supports user management, product search, and shopping cart operations with strict enforcement of business rules.

### Key Features Implemented

- **User Management**: Sign-up, login (stateless), profile view/update
- **Product Catalog**: Case-insensitive product search
- **Shopping Cart**: Lazy cart creation, add/update/remove items, view cart, auto-delete empty cart, logout cleanup
- **Security**: JWT-based stateless authentication, BCrypt password hashing
- **Database**: PostgreSQL with Flyway migrations, comprehensive schema with constraints
- **API Documentation**: Swagger/OpenAPI integration

## Architecture

### Technology Stack

- **Framework**: Spring Boot 3.2.0
- **Language**: Java 17
- **Database**: PostgreSQL
- **ORM**: Spring Data JPA / Hibernate
- **Security**: Spring Security with JWT
- **Migration**: Flyway
- **Documentation**: Springdoc OpenAPI
- **Build Tool**: Maven

### Project Structure

```
api-springboot/
├── src/main/
│   ├── java/com/ecommerce/
│   │   ├── ShoppingCartApplication.java
│   │   ├── controller/
│   │   │   ├── UserController.java
│   │   │   ├── ProductController.java
│   │   │   ├── CartController.java
│   │   │   └── LogoutController.java
│   │   ├── service/
│   │   │   ├── UserService.java
│   │   │   ├── ProductService.java
│   │   │   └── CartService.java
│   │   ├── repository/
│   │   │   ├── UserRepository.java
│   │   │   ├── ProductRepository.java
│   │   │   ├── CartRepository.java
│   │   │   ├── CartItemRepository.java
│   │   │   └── ProductInventoryRepository.java
│   │   ├── entity/
│   │   │   ├── User.java
│   │   │   ├── Product.java
│   │   │   ├── Category.java
│   │   │   ├── ProductInventory.java
│   │   │   ├── Cart.java
│   │   │   └── CartItem.java
│   │   ├── dto/
│   │   │   ├── SignUpRequest.java
│   │   │   ├── LoginRequest.java
│   │   │   ├── UserResponse.java
│   │   │   ├── ProfileUpdateRequest.java
│   │   │   ├── ProductResponse.java
│   │   │   ├── AddToCartRequest.java
│   │   │   ├── UpdateCartItemRequest.java
│   │   │   ├── CartItemResponse.java
│   │   │   ├── CartResponse.java
│   │   │   └── ApiResponse.java
│   │   ├── exception/
│   │   │   ├── ResourceNotFoundException.java
│   │   │   ├── ResourceAlreadyExistsException.java
│   │   │   ├── ValidationException.java
│   │   │   ├── UnauthorizedException.java
│   │   │   └── GlobalExceptionHandler.java
│   │   ├── security/
│   │   │   ├── JwtTokenProvider.java
│   │   │   └── JwtAuthenticationFilter.java
│   │   └── config/
│   │       ├── SecurityConfig.java
│   │       └── OpenApiConfig.java
│   └── resources/
│       ├── application.yml
│       └── db/migration/
│           ├── V002__add_username_to_users.sql
│           ├── V003__add_cart_constraints.sql
│           └── V004__add_product_search_indexes.sql
└── pom.xml
```

## Setup Instructions

### Prerequisites

- Java 17 or higher
- Maven 3.6+
- PostgreSQL 12+
- Git

### Database Setup

1. Create PostgreSQL database:
```sql
CREATE DATABASE ecommerce;
```

2. Update database credentials in `application.yml`:
```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/ecommerce
    username: your_username
    password: your_password
```

### Application Setup

1. Clone the repository:
```bash
git clone <repository-url>
cd api-springboot
```

2. Build the project:
```bash
mvn clean install
```

3. Run Flyway migrations (automatic on startup):
```bash
mvn flyway:migrate
```

4. Run the application:
```bash
mvn spring-boot:run
```

The application will start on `http://localhost:8080/api`

### API Documentation

Access Swagger UI at: `http://localhost:8080/api/swagger-ui.html`

## API Endpoints

### User Management

#### Sign Up
```http
POST /api/users/signup
Content-Type: application/json

{
  "username": "johndoe",
  "password": "password123",
  "fullName": "John Doe",
  "email": "john@example.com"
}
```

#### Login
```http
POST /api/users/login
Content-Type: application/json

{
  "username": "johndoe",
  "password": "password123"
}
```

#### Get Profile
```http
GET /api/users/profile
Authorization: Bearer <jwt-token>
```

#### Update Profile
```http
PUT /api/users/profile
Authorization: Bearer <jwt-token>
Content-Type: application/json

{
  "fullName": "John Updated Doe",
  "email": "john.updated@example.com"
}
```

### Product Catalog

#### Search Products
```http
GET /api/products/search?keyword=laptop
```

### Shopping Cart

#### Add to Cart
```http
POST /api/cart/items
Authorization: Bearer <jwt-token>
Content-Type: application/json

{
  "productId": "<product-uuid>",
  "quantity": 2
}
```

#### Update Cart Item
```http
PUT /api/cart/items/{itemId}
Authorization: Bearer <jwt-token>
Content-Type: application/json

{
  "quantity": 3
}
```

#### Remove Cart Item
```http
DELETE /api/cart/items/{itemId}
Authorization: Bearer <jwt-token>
```

#### Get Cart
```http
GET /api/cart
Authorization: Bearer <jwt-token>
```

#### Logout (with cart cleanup)
```http
POST /api/logout
Authorization: Bearer <jwt-token>
```

## Business Rules Implemented

### User Management
- Username is unique and immutable
- Password is hashed using BCrypt
- Email must be valid format
- Full name is required

### Product Catalog
- Product search is case-insensitive
- Only active products are returned in search
- Product price is immutable via user actions

### Shopping Cart
- **Lazy Cart Creation**: Cart is created only when first item is added
- **One Cart Per User**: Each user can have only one active cart
- **Auto-Delete Empty Cart**: Cart is automatically deleted when last item is removed
- **Logout Cleanup**: Cart and all items are deleted on logout
- **Quantity Validation**: Cart item quantity must be > 0
- **No Cart Persistence**: Carts do not persist across logout

## Database Schema

### Schema Reconciliation

The following migrations were created to reconcile LLD with existing database:

1. **V002__add_username_to_users.sql**: Adds `username` field to users table (LLD requirement)
2. **V003__add_cart_constraints.sql**: Adds one-cart-per-user constraint and quantity validation
3. **V004__add_product_search_indexes.sql**: Adds indexes for case-insensitive product search

### Entity Relationship Diagram

See `docs/er_diagram.mmd` for complete ER diagram in Mermaid format.

**Key Relationships:**
- User → Cart (1:1)
- Cart → CartItem (1:N)
- CartItem → Product (N:1)
- Product → Category (N:1)
- Product → ProductInventory (1:1)

## Security

### Authentication

- **Stateless JWT**: No session data stored in database
- **Token Expiration**: 24 hours (configurable)
- **Password Hashing**: BCrypt with strength 12

### Authorization

- Public endpoints: `/users/signup`, `/users/login`, `/products/search`
- Protected endpoints: All cart operations, profile management, logout
- User can only access their own cart and profile

## Testing

### Sample Test Scenarios

1. **User Registration and Login**
   - Register new user
   - Login with credentials
   - Verify JWT token is returned

2. **Product Search**
   - Search with various keywords
   - Verify case-insensitive matching
   - Verify only active products returned

3. **Cart Operations**
   - Add product to cart (lazy creation)
   - Update cart item quantity
   - Remove cart item
   - Verify cart auto-deletion when empty

4. **Logout Cleanup**
   - Add items to cart
   - Logout
   - Verify cart and items are deleted

5. **Negative Tests**
   - Duplicate username registration
   - Invalid credentials login
   - Unauthorized cart access
   - Invalid product ID
   - Quantity <= 0

## Quality Metrics

### Code Coverage
- All LLD requirements implemented
- Complete MVC layering (Controller → Service → Repository)
- Comprehensive exception handling
- Input validation at all layers

### Database Integrity
- All foreign key constraints enforced
- Unique constraints on username, email, user-cart relationship
- Check constraints on quantity, price
- Indexes for performance optimization

### API Compliance
- All LLD API contracts implemented
- Proper HTTP status codes (200, 201, 204, 400, 401, 404, 409)
- Consistent response format
- Comprehensive error messages

## Troubleshooting Guide

### Common Issues

#### 1. Database Connection Error
**Symptom**: Application fails to start with connection refused error

**Solution**:
- Verify PostgreSQL is running
- Check database credentials in `application.yml`
- Ensure database `ecommerce` exists

#### 2. Migration Script Errors
**Symptom**: Flyway migration fails

**Solution**:
- Check migration scripts in `src/main/resources/db/migration/`
- Verify migration order (V002, V003, V004)
- Check Flyway schema history: `SELECT * FROM flyway_schema_history;`

#### 3. JWT Token Invalid
**Symptom**: 401 Unauthorized on protected endpoints

**Solution**:
- Verify token is included in Authorization header: `Bearer <token>`
- Check token expiration (24 hours default)
- Verify JWT secret is configured in `application.yml`

#### 4. Cart Not Found
**Symptom**: 404 error when accessing cart

**Solution**:
- Cart is created lazily - add item first
- Cart is deleted on logout - login again
- Cart is auto-deleted when empty - add items

#### 5. Duplicate Username Error
**Symptom**: 409 Conflict on user registration

**Solution**:
- Username must be unique
- Choose different username

#### 6. Product Search Returns Empty
**Symptom**: No products found in search

**Solution**:
- Verify sample data is loaded (DML script)
- Check product is active (`is_active = true`)
- Try different search keywords

### Preventive Measures

1. **Regular Database Backups**: Schedule automated backups
2. **Monitoring**: Set up application and database monitoring
3. **Logging**: Review application logs regularly
4. **Testing**: Run integration tests before deployment
5. **Documentation**: Keep API documentation updated

## Recommendations

### Best Practices

1. **Environment Variables**: Use environment variables for sensitive configuration
2. **Connection Pooling**: Configure HikariCP for optimal performance
3. **Caching**: Consider Redis for product catalog caching
4. **Rate Limiting**: Implement rate limiting for public endpoints
5. **CORS**: Configure CORS for frontend integration

### Future Improvements

1. **Checkout Flow**: Implement order creation and payment processing
2. **Inventory Management**: Add inventory reservation and locking
3. **Admin Panel**: Create admin APIs for product management
4. **Cart Persistence**: Optional cart persistence across sessions
5. **Email Verification**: Implement email verification flow
6. **Password Reset**: Add password reset functionality
7. **Role-Based Access**: Implement user roles and permissions
8. **Audit Logging**: Enhanced audit trail for all operations
9. **Performance Optimization**: Add database query optimization
10. **Horizontal Scaling**: Prepare for horizontal scaling with session management

### Optimization Suggestions

1. **Database Indexing**: Review and optimize indexes based on query patterns
2. **Query Optimization**: Use query hints and explain plans
3. **Lazy Loading**: Optimize JPA fetch strategies
4. **Batch Operations**: Implement batch inserts/updates where applicable
5. **API Response Compression**: Enable GZIP compression

## Maintenance Procedures

### Regular Tasks

1. **Database Maintenance**
   - Weekly: Analyze and vacuum tables
   - Monthly: Review and optimize slow queries
   - Quarterly: Review and update indexes

2. **Application Maintenance**
   - Weekly: Review application logs
   - Monthly: Update dependencies
   - Quarterly: Security audit

3. **Monitoring**
   - Daily: Check application health
   - Weekly: Review performance metrics
   - Monthly: Capacity planning review

## Support and Contact

For issues, questions, or contributions:
- **Email**: support@ecommerce.com
- **Documentation**: See `docs/` directory
- **API Docs**: http://localhost:8080/api/swagger-ui.html

## License

This project is proprietary and confidential.

---

**Version**: 1.0.0  
**Last Updated**: 2024  
**Author**: Backend Engineering Team