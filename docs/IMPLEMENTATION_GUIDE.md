# Implementation Guide - Shopping Cart System

## Overview

This guide provides detailed implementation notes for the Shopping Cart System backend, covering design decisions, business logic implementation, and best practices followed.

## Design Decisions

### 1. Lazy Cart Creation

**Rationale**: Carts are created only when a user adds their first product, avoiding unnecessary database records.

**Implementation**:
```java
// In CartService.addProductToCart()
Cart cart = cartRepository.findByUser(user)
    .orElseGet(() -> {
        Cart newCart = new Cart();
        newCart.setUser(user);
        return cartRepository.save(newCart);
    });
```

**Benefits**:
- Reduced database storage
- Cleaner user experience
- Aligns with "cart cannot exist without items" rule

### 2. Auto-Delete Empty Carts

**Rationale**: When the last item is removed from a cart, the cart itself is deleted to maintain data integrity.

**Implementation**:
```java
// In CartService.removeCartItem()
cartItemRepository.delete(item);
cart = cartRepository.findById(cart.getId()).orElseThrow();
if (cart.getItems().isEmpty()) {
    cartRepository.delete(cart);
}
```

**Benefits**:
- Enforces "cart cannot exist without items" constraint
- Prevents orphaned cart records
- Simplifies cart lifecycle management

### 3. Cart Cleanup on Logout

**Rationale**: Stateless system design requires cart deletion when users log out.

**Implementation**:
```java
// In CartService.clearCartOnLogout()
Optional<Cart> cartOpt = cartRepository.findByUser(user);
if (cartOpt.isPresent()) {
    cartRepository.delete(cartOpt.get());
}
```

**Benefits**:
- Maintains stateless architecture
- Prevents cart accumulation
- Clear session boundaries

### 4. Stateless JWT Authentication

**Rationale**: No server-side session storage, all authentication state in JWT token.

**Implementation**:
- JWT contains userId and username
- Token validated on each request
- No database session table

**Benefits**:
- Horizontal scalability
- Reduced database load
- Simplified deployment

### 5. Cascade Delete for Cart Items

**Rationale**: When a cart is deleted, all associated items are automatically removed.

**Implementation**:
```java
@OneToMany(mappedBy = "cart", cascade = CascadeType.ALL, orphanRemoval = true)
private List<CartItem> items = new ArrayList<>();
```

**Database Constraint**:
```sql
FOREIGN KEY (cart_id) REFERENCES cart(cart_id) ON DELETE CASCADE
```

**Benefits**:
- Data consistency
- Prevents orphaned cart items
- Simplified deletion logic

## Business Logic Implementation

### User Management

#### Sign Up Flow
1. Validate input (username, password, email format)
2. Check username uniqueness
3. Hash password with BCrypt
4. Create user record
5. Return user profile (excluding password)

#### Login Flow
1. Validate credentials
2. Verify username exists
3. Compare password hash
4. Generate JWT token with 24-hour expiration
5. Return token and user info

#### Profile Update Flow
1. Extract userId from JWT token
2. Validate new email format
3. Update full_name and email only (username immutable)
4. Return updated profile

### Product Search

#### Search Logic
1. Validate keyword is not empty
2. Perform case-insensitive search on name and description
3. Return matching products with full details

**Query**:
```sql
SELECT * FROM products 
WHERE LOWER(name) LIKE LOWER('%keyword%') 
   OR LOWER(description) LIKE LOWER('%keyword%')
```

### Cart Operations

#### Add Product to Cart
1. Authenticate user via JWT
2. Validate product exists
3. Validate quantity > 0
4. Find or create cart for user
5. Check if product already in cart:
   - If yes: Increment quantity
   - If no: Create new cart item
6. Return updated cart with totals

#### Update Cart Item Quantity
1. Authenticate user
2. Validate new quantity > 0
3. Find cart item by ID and cart ownership
4. Update quantity
5. Return updated cart

#### Remove Cart Item
1. Authenticate user
2. Find cart item by ID and cart ownership
3. Delete cart item
4. Check if cart is now empty:
   - If yes: Delete cart
   - If no: Return updated cart

#### View Cart
1. Authenticate user
2. Find cart for user
3. Calculate item totals (price × quantity)
4. Calculate grand total (sum of item totals)
5. Return cart with items and totals

## Validation Rules

### User Validation

| Field       | Rule                                      | Error Message                          |
|-------------|-------------------------------------------|---------------------------------------|
| username    | 3-100 chars, not blank                    | Username must be between 3 and 100... |
| password    | Min 8 chars, not blank                    | Password must be at least 8 chars     |
| fullName    | Max 200 chars, not blank                  | Full name is required                 |
| email       | Valid email format, not blank             | Email must be valid                   |

### Product Validation

| Field       | Rule                                      | Error Message                          |
|-------------|-------------------------------------------|---------------------------------------|
| keyword     | Not empty                                 | Search keyword cannot be empty        |

### Cart Validation

| Field       | Rule                                      | Error Message                          |
|-------------|-------------------------------------------|---------------------------------------|
| productId   | Must exist in products table              | Product not found                     |
| quantity    | Integer > 0                               | Quantity must be greater than 0       |

## Database Constraints

### Uniqueness Constraints

```sql
-- Username must be unique
ALTER TABLE users ADD CONSTRAINT uk_users_username UNIQUE (username);

-- One cart per user
ALTER TABLE cart ADD CONSTRAINT uk_cart_user_id UNIQUE (user_id);

-- No duplicate products in same cart
ALTER TABLE cart_items ADD CONSTRAINT uk_cart_items_cart_product UNIQUE (cart_id, product_id);
```

### Check Constraints

```sql
-- Product price must be non-negative
ALTER TABLE products ADD CONSTRAINT chk_products_price_non_negative CHECK (price >= 0);

-- Product quantity must be non-negative
ALTER TABLE products ADD CONSTRAINT chk_products_available_qty_non_negative CHECK (available_qty >= 0);

-- Cart item quantity must be positive
ALTER TABLE cart_items ADD CONSTRAINT chk_cart_items_quantity_positive CHECK (quantity > 0);
```

### Foreign Key Constraints

```sql
-- Cart belongs to user (cascade delete)
ALTER TABLE cart ADD CONSTRAINT fk_cart_user 
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE;

-- Cart item belongs to cart (cascade delete)
ALTER TABLE cart_items ADD CONSTRAINT fk_cart_items_cart 
    FOREIGN KEY (cart_id) REFERENCES cart(cart_id) ON DELETE CASCADE;

-- Cart item references product
ALTER TABLE cart_items ADD CONSTRAINT fk_cart_items_product 
    FOREIGN KEY (product_id) REFERENCES products(product_id);
```

## Error Handling

### Exception Hierarchy

```
RuntimeException
├── ResourceNotFoundException (404)
├── DuplicateResourceException (409)
├── InvalidCredentialsException (401)
└── IllegalArgumentException (400)
```

### Global Exception Handler

All exceptions are caught by `GlobalExceptionHandler` and converted to standardized error responses:

```json
{
  "status": 404,
  "message": "Cart not found",
  "timestamp": "2024-01-01T10:00:00"
}
```

### Validation Errors

Validation failures return detailed field-level errors:

```json
{
  "status": 400,
  "message": "Validation failed",
  "timestamp": "2024-01-01T10:00:00",
  "errors": {
    "username": "Username is required",
    "email": "Email must be valid"
  }
}
```

## Security Implementation

### Password Security

- **Hashing Algorithm**: BCrypt with cost factor 10
- **Salt**: Automatically generated per password
- **Storage**: Only password hash stored, never plaintext

### JWT Security

- **Algorithm**: HS256 (HMAC with SHA-256)
- **Secret Key**: Minimum 256 bits (configurable)
- **Expiration**: 24 hours (configurable)
- **Claims**: userId, username, issuedAt, expiration

### API Security

- **Public Endpoints**: `/api/users/signup`, `/api/users/login`
- **Protected Endpoints**: All others require valid JWT
- **CSRF**: Disabled (stateless API)
- **CORS**: Configurable per environment

## Transaction Management

### Service Layer Transactions

All write operations are wrapped in transactions:

```java
@Transactional
public CartResponse addProductToCart(UUID userId, AddToCartRequest request) {
    // All operations in single transaction
    // Rollback on any exception
}
```

### Read-Only Transactions

Read operations use read-only transactions for optimization:

```java
@Transactional(readOnly = true)
public CartResponse getCart(UUID userId) {
    // Read-only, no write lock
}
```

## Logging Strategy

### Log Levels

- **DEBUG**: Method entry/exit, detailed flow
- **INFO**: Business events (user registered, cart created)
- **WARN**: Validation failures, not-found scenarios
- **ERROR**: Exceptions, system errors

### Log Format

```
2024-01-01 10:00:00 INFO  [UserService] User registered successfully: john_doe
2024-01-01 10:00:05 WARN  [CartService] Cart not found for user: uuid-123
2024-01-01 10:00:10 ERROR [GlobalExceptionHandler] Unexpected error: ...
```

## Performance Considerations

### Database Indexes

```sql
CREATE INDEX idx_cart_user_id ON cart(user_id);
CREATE INDEX idx_cart_items_cart_id ON cart_items(cart_id);
CREATE INDEX idx_cart_items_product_id ON cart_items(product_id);
```

### Query Optimization

- Use `@EntityGraph` for eager loading when needed
- Avoid N+1 queries with proper fetch strategies
- Use pagination for large result sets (future enhancement)

### Connection Pooling

HikariCP (default in Spring Boot) with recommended settings:

```yaml
spring:
  datasource:
    hikari:
      maximum-pool-size: 10
      minimum-idle: 5
      connection-timeout: 30000
```

## Testing Strategy

### Unit Tests

- Test service layer business logic in isolation
- Mock repository dependencies
- Cover all validation scenarios
- Test exception handling

### Integration Tests

- Test full request-response cycle
- Use test database (H2 or TestContainers)
- Verify database constraints
- Test transaction rollback scenarios

### API Tests

- Test all endpoints with valid/invalid inputs
- Verify HTTP status codes
- Test authentication/authorization
- Validate response formats

## Deployment Checklist

### Pre-Deployment

- [ ] Update database credentials
- [ ] Configure JWT secret (production-grade)
- [ ] Set appropriate log levels
- [ ] Configure CORS for frontend domain
- [ ] Review and adjust connection pool settings
- [ ] Run all tests
- [ ] Perform security audit

### Post-Deployment

- [ ] Verify database migrations applied
- [ ] Check application health endpoint
- [ ] Monitor logs for errors
- [ ] Test critical user flows
- [ ] Verify JWT token generation/validation
- [ ] Check database connection pool metrics

## Monitoring and Observability

### Key Metrics

- API response times (p50, p95, p99)
- Error rates by endpoint
- Database query performance
- JWT token generation time
- Active database connections

### Health Checks

```yaml
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics
  health:
    db:
      enabled: true
```

### Alerts

- High error rate (> 5%)
- Slow response times (> 500ms p95)
- Database connection pool exhaustion
- High memory/CPU usage

## Conclusion

This implementation follows Spring Boot best practices, enforces business rules at multiple levels (application + database), and provides a solid foundation for future enhancements. The stateless architecture ensures scalability, while comprehensive validation and error handling provide a robust user experience.