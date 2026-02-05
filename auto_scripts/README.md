# E-Commerce Shopping Cart API

A production-ready, stateless shopping cart system built with Spring Boot 3.2, implementing RESTful APIs for user authentication, product management, cart operations, and order processing.

## Table of Contents

- [Features](#features)
- [Technology Stack](#technology-stack)
- [Architecture](#architecture)
- [Getting Started](#getting-started)
- [API Documentation](#api-documentation)
- [Database Schema](#database-schema)
- [Security](#security)
- [Testing](#testing)
- [Deployment](#deployment)
- [Quality Metrics](#quality-metrics)
- [Troubleshooting](#troubleshooting)
- [Contributing](#contributing)

## Features

### Core Functionality

- **User Management**
  - User registration with validation
  - Stateless JWT-based authentication
  - Password encryption with BCrypt

- **Product Management**
  - Advanced product search with filters
  - Category and brand filtering
  - Price range filtering
  - Pagination and sorting
  - Stock management

- **Shopping Cart**
  - Add/update/remove items
  - Real-time cart total calculation
  - Stock validation
  - Cart lifecycle management (ACTIVE, CHECKED_OUT, ABANDONED)

- **Order Processing**
  - Checkout with address and payment details
  - Order history with pagination
  - Order tracking by order number
  - Automatic stock deduction

### Technical Features

- Stateless architecture with JWT tokens
- RESTful API design
- Comprehensive validation
- Global exception handling
- Database migration with Flyway
- Transaction management
- Optimized database queries with indexes
- Cascading deletes and referential integrity

## Technology Stack

- **Framework**: Spring Boot 3.2.0
- **Language**: Java 17
- **Database**: MySQL 8.0
- **ORM**: Spring Data JPA / Hibernate
- **Security**: Spring Security + JWT
- **Migration**: Flyway
- **Build Tool**: Maven
- **Additional Libraries**:
  - Lombok (boilerplate reduction)
  - JJWT (JWT handling)
  - Jackson (JSON processing)

## Architecture

### Layered Architecture

```
┌─────────────────────────────────────┐
│         Controllers                 │  REST API Layer
├─────────────────────────────────────┤
│         Services                    │  Business Logic Layer
├─────────────────────────────────────┤
│         Repositories                │  Data Access Layer
├─────────────────────────────────────┤
│         Entities                    │  Domain Model
└─────────────────────────────────────┘
```

### Package Structure

```
com.ecommerce
├── config              # Configuration classes
├── controller          # REST controllers
├── dto                 # Data Transfer Objects
├── entity              # JPA entities
├── exception           # Custom exceptions
├── repository          # Data repositories
├── security            # Security components
└── service             # Business logic
```

## Getting Started

### Prerequisites

- Java 17 or higher
- Maven 3.6+
- MySQL 8.0+
- Git

### Installation

1. **Clone the repository**

```bash
git clone https://github.com/NavneetBN47/ecommerce.git
cd ecommerce
```

2. **Configure Database**

Create a MySQL database:

```sql
CREATE DATABASE ecommerce_db;
```

Update `src/main/resources/application.properties`:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/ecommerce_db
spring.datasource.username=your_username
spring.datasource.password=your_password
```

3. **Build the project**

```bash
mvn clean install
```

4. **Run the application**

```bash
mvn spring-boot:run
```

The application will start on `http://localhost:8080`

### Quick Start with Docker (Optional)

```bash
docker-compose up -d
```

## API Documentation

### Authentication Endpoints

#### Register User

```http
POST /api/auth/register
Content-Type: application/json

{
  "email": "user@example.com",
  "password": "SecurePass123!",
  "firstName": "John",
  "lastName": "Doe",
  "phone": "1234567890"
}
```

#### Login

```http
POST /api/auth/login
Content-Type: application/json

{
  "email": "user@example.com",
  "password": "SecurePass123!"
}
```

**Response:**
```json
{
  "success": true,
  "data": {
    "token": "eyJhbGciOiJIUzUxMiJ9...",
    "tokenType": "Bearer",
    "userId": 1,
    "email": "user@example.com",
    "firstName": "John",
    "lastName": "Doe"
  }
}
```

### Product Endpoints

#### Search Products

```http
POST /api/products/search
Content-Type: application/json

{
  "keyword": "laptop",
  "category": "Electronics",
  "minPrice": 100,
  "maxPrice": 2000,
  "page": 0,
  "size": 20,
  "sortBy": "price",
  "sortDirection": "ASC"
}
```

#### Get Product by ID

```http
GET /api/products/{id}
```

### Cart Endpoints

#### Add to Cart

```http
POST /api/cart/add
Authorization: Bearer {token}
Content-Type: application/json

{
  "productId": 1,
  "quantity": 2
}
```

#### Get Cart

```http
GET /api/cart
Authorization: Bearer {token}
```

#### Update Cart Item

```http
PUT /api/cart/items/{itemId}
Authorization: Bearer {token}
Content-Type: application/json

{
  "quantity": 3
}
```

#### Remove Cart Item

```http
DELETE /api/cart/items/{itemId}
Authorization: Bearer {token}
```

### Order Endpoints

#### Checkout

```http
POST /api/orders/checkout
Authorization: Bearer {token}
Content-Type: application/json

{
  "shippingAddress": "123 Main St, City, State 12345",
  "billingAddress": "123 Main St, City, State 12345",
  "paymentMethod": "CREDIT_CARD"
}
```

#### Get User Orders

```http
GET /api/orders?page=0&size=10
Authorization: Bearer {token}
```

## Database Schema

See [db/diagrams/er_diagram.md](db/diagrams/er_diagram.md) for the complete ER diagram.

### Key Tables

- **users**: User account information
- **products**: Product catalog
- **carts**: Shopping cart instances
- **cart_items**: Items in shopping carts
- **orders**: Completed orders
- **order_items**: Items in orders

### Migration Scripts

Database migrations are managed by Flyway:

- `V1__Initial_Schema.sql`: Creates all tables with constraints
- `V2__Sample_Data.sql`: Inserts sample data for testing

## Security

### Authentication Flow

1. User registers or logs in
2. Server generates JWT token
3. Client includes token in Authorization header
4. Server validates token on each request

### Password Security

- Passwords are hashed using BCrypt with strength 10
- Never stored in plain text
- Validation enforces strong password requirements

### JWT Configuration

- Token expiration: 24 hours (configurable)
- Secret key: Configured in application.properties
- Stateless authentication (no session storage)

## Testing

### Run Tests

```bash
mvn test
```

### Test Coverage

```bash
mvn jacoco:report
```

View coverage report at `target/site/jacoco/index.html`

## Deployment

### Production Checklist

- [ ] Update database credentials
- [ ] Set strong JWT secret
- [ ] Configure production profile
- [ ] Enable HTTPS
- [ ] Set up monitoring
- [ ] Configure logging
- [ ] Set up backup strategy

### Environment Variables

```bash
export DATABASE_URL=jdbc:mysql://prod-db:3306/ecommerce_db
export DATABASE_USERNAME=prod_user
export DATABASE_PASSWORD=secure_password
export JWT_SECRET=your_production_secret_key
```

### Build for Production

```bash
mvn clean package -DskipTests
java -jar target/ecommerce-api-1.0.0.jar --spring.profiles.active=prod
```

## Quality Metrics

### Code Quality

- **Lines of Code**: ~3,500
- **Test Coverage**: Target 80%+
- **Cyclomatic Complexity**: < 10 per method
- **Code Duplication**: < 5%

### Performance Metrics

- **API Response Time**: < 200ms (p95)
- **Database Query Time**: < 50ms (p95)
- **Concurrent Users**: 1000+
- **Throughput**: 500 req/sec

### Database Optimization

- Indexed columns for frequent queries
- Optimized JOIN operations
- Connection pooling configured
- Query result caching

## Troubleshooting

See [docs/TROUBLESHOOTING.md](docs/TROUBLESHOOTING.md) for detailed troubleshooting guide.

### Common Issues

#### Database Connection Failed

**Problem**: Application fails to connect to MySQL

**Solution**:
1. Verify MySQL is running: `sudo systemctl status mysql`
2. Check credentials in application.properties
3. Ensure database exists: `SHOW DATABASES;`
4. Check firewall rules

#### JWT Token Invalid

**Problem**: 401 Unauthorized error

**Solution**:
1. Verify token is included in Authorization header
2. Check token format: `Bearer {token}`
3. Ensure token hasn't expired
4. Verify JWT secret matches

#### Insufficient Stock Error

**Problem**: Cannot add item to cart

**Solution**:
1. Check product stock quantity
2. Verify product is active
3. Review cart item quantity

## Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

### Code Style

- Follow Java naming conventions
- Use Lombok annotations
- Write meaningful commit messages
- Add JavaDoc for public methods
- Include unit tests for new features

## License

This project is licensed under the MIT License - see the LICENSE file for details.

## Contact

- **Project Repository**: https://github.com/NavneetBN47/ecommerce
- **Issue Tracker**: https://github.com/NavneetBN47/ecommerce/issues

## Acknowledgments

- Spring Boot team for the excellent framework
- MySQL team for the robust database
- Open source community for various libraries
