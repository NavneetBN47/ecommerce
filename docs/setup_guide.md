# Shopping Cart System - Setup Guide

## Prerequisites

- Java 17 or higher
- Maven 3.8+
- PostgreSQL 14+
- Git

## Database Setup

1. **Create Database**
```sql
CREATE DATABASE ecommerce;
```

2. **Configure Database Connection**

Edit `application.yml` or set environment variables:
```bash
export DB_USERNAME=your_db_user
export DB_PASSWORD=your_db_password
```

3. **Run Migrations**

Migrations will run automatically on application startup using Flyway.

## Build and Run

1. **Clone Repository**
```bash
git clone https://github.com/NavneetBN47/ecommerce.git
cd ecommerce
```

2. **Build Project**
```bash
cd api-springboot
mvn clean install
```

3. **Run Application**
```bash
mvn spring-boot:run
```

Application will start on `http://localhost:8080`

## API Endpoints

### User Management
- `POST /api/users/signup` - Register new user
- `POST /api/users/login` - User login
- `GET /api/users/profile` - Get user profile (requires X-User-Id header)
- `PUT /api/users/profile` - Update profile (requires X-User-Id header)

### Product Catalog
- `GET /api/products/search?keyword=...` - Search products

### Shopping Cart
- `POST /api/cart/items` - Add product to cart (requires X-User-Id header)
- `PUT /api/cart/items/{itemId}` - Update cart item quantity (requires X-User-Id header)
- `DELETE /api/cart/items/{itemId}` - Remove cart item (requires X-User-Id header)
- `GET /api/cart` - View cart (requires X-User-Id header)
- `POST /api/cart/logout` - Clear cart on logout (requires X-User-Id header)

## Testing

### Sample Requests

**Signup**
```bash
curl -X POST http://localhost:8080/api/users/signup \
  -H "Content-Type: application/json" \
  -d '{
    "username": "johndoe",
    "password": "password123",
    "fullName": "John Doe",
    "email": "john.doe@example.com"
  }'
```

**Login**
```bash
curl -X POST http://localhost:8080/api/users/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "johndoe",
    "password": "password123"
  }'
```

**Search Products**
```bash
curl -X GET "http://localhost:8080/api/products/search?keyword=laptop"
```

**Add to Cart**
```bash
curl -X POST http://localhost:8080/api/cart/items \
  -H "Content-Type: application/json" \
  -H "X-User-Id: <user-id-from-login>" \
  -d '{
    "productId": "<product-id>",
    "quantity": 2
  }'
```

## Configuration

### Application Properties

Key configurations in `application.yml`:

- `spring.datasource.url` - Database connection URL
- `spring.jpa.hibernate.ddl-auto` - Set to `validate` for production
- `app.jwt.secret` - JWT secret key (set via environment variable)
- `app.cart.expiration-days` - Cart expiration (currently not used, carts deleted on logout)

## Troubleshooting

### Common Issues

1. **Database Connection Failed**
   - Verify PostgreSQL is running
   - Check database credentials
   - Ensure database exists

2. **Migration Errors**
   - Check Flyway migration scripts in `src/main/resources/db/migration`
   - Verify database schema state
   - Review application logs

3. **Port Already in Use**
   - Change port in `application.yml`: `server.port=8081`
   - Or set environment variable: `SERVER_PORT=8081`

## Security Notes

- Passwords are hashed using BCrypt
- Authentication is stateless (no session storage)
- Use `X-User-Id` header for authenticated requests (in production, use JWT tokens)
- Never commit sensitive credentials to version control

## Next Steps

- Implement JWT-based authentication
- Add comprehensive unit and integration tests
- Set up CI/CD pipeline
- Configure production database
- Add API documentation (Swagger/OpenAPI)