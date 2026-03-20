# HIGH-LEVEL DESIGN DOCUMENT

## Architecture Overview

```
┌─────────────────────────────────────────────────────────────┐
│                    PRESENTATION LAYER                       │
├─────────────────────────────────────────────────────────────┤
│  REST Controllers (Spring MVC)                              │
│  ┌─────────────┐ ┌─────────────┐ ┌─────────────┐          │
│  │UserController│ │CartController│ │ProductController│      │
│  └─────────────┘ └─────────────┘ └─────────────┘          │
└─────────────────────────────────────────────────────────────┘
                            │
                            ▼
┌─────────────────────────────────────────────────────────────┐
│                    BUSINESS LAYER                           │
├─────────────────────────────────────────────────────────────┤
│  Service Layer (Spring Services)                            │
│  ┌─────────────┐ ┌─────────────┐ ┌─────────────┐          │
│  │ UserService │ │ CartService │ │ProductService│          │
│  └─────────────┘ └─────────────┘ └─────────────┘          │
│                            │                                │
│  ┌─────────────────────────────────────────────────────┐   │
│  │           Security & Validation Layer               │   │
│  │  - Input Validation    - RBAC/ABAC                 │   │
│  │  - Output Filtering    - Audit Logging             │   │
│  └─────────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────────┘
                            │
                            ▼
┌─────────────────────────────────────────────────────────────┐
│                    DATA ACCESS LAYER                        │
├─────────────────────────────────────────────────────────────┤
│  Repository Layer (Spring Data JPA)                         │
│  ┌─────────────┐ ┌─────────────┐ ┌─────────────┐          │
│  │UserRepository│ │CartRepository│ │ProductRepository│      │
│  └─────────────┘ └─────────────┘ └─────────────┘          │
└─────────────────────────────────────────────────────────────┘
                            │
                            ▼
┌─────────────────────────────────────────────────────────────┐
│                    PERSISTENCE LAYER                        │
├─────────────────────────────────────────────────────────────┤
│  Database (MySQL/PostgreSQL)                                │
│  - Encrypted at Rest (AES-256)                             │
│  - Connection Pool with TLS 1.3                            │
│  - Database Constraints Enforcement                         │
└─────────────────────────────────────────────────────────────┘
```

## Major Components

### 1. User Management Component
**Responsibilities:**
- User registration and authentication
- Profile management (view/update)
- Stateless JWT token generation
- Password encryption (BCrypt)

**Key Classes:**
- `UserController`: REST endpoints for user operations
- `UserService`: Business logic for user management
- `UserRepository`: Data access for user entities
- `AuthenticationService`: JWT token management

### 2. Product Catalog Component
**Responsibilities:**
- Product search functionality
- Product information retrieval
- Case-insensitive search implementation

**Key Classes:**
- `ProductController`: REST endpoints for product operations
- `ProductService`: Business logic for product search
- `ProductRepository`: Data access with custom search queries

### 3. Shopping Cart Component
**Responsibilities:**
- Lazy cart creation
- Cart item management (add/update/remove)
- Cart total calculations
- Automatic cart cleanup

**Key Classes:**
- `CartController`: REST endpoints for cart operations
- `CartService`: Business logic for cart management
- `CartRepository`: Data access for cart entities
- `CartItemRepository`: Data access for cart items

### 4. Security Component
**Responsibilities:**
- Input validation and sanitization
- Output filtering and encoding
- Authentication and authorization
- Audit logging

**Key Classes:**
- `SecurityConfig`: Spring Security configuration
- `JwtAuthenticationFilter`: Token validation
- `AuditService`: Compliance logging
- `ValidationService`: Input validation

## Integration Points

### 1. External Integrations
- **Database Connection Pool**: HikariCP with encrypted connections
- **Logging System**: SLF4J with Logback for audit trails
- **Monitoring**: Spring Boot Actuator endpoints

### 2. Internal Service Communications
- **Synchronous**: Direct service method calls within JVM
- **Event-Driven**: Spring Application Events for audit logging
- **Transaction Management**: Spring @Transactional annotations

## Security Features

### 1. Authentication & Authorization
```java
// JWT Token Configuration
@Configuration
public class SecurityConfig {
    - JWT tokens with 24-hour expiration
    - RSA-256 signing algorithm
    - Stateless session management
    - Role-based access control (USER role)
}
```

### 2. Data Protection
- **Encryption at Rest**: AES-256 for sensitive data (email, full name)
- **Encryption in Transit**: TLS 1.3 for all communications
- **Password Security**: BCrypt with salt rounds = 12
- **Input Validation**: JSR-303 Bean Validation
- **Output Filtering**: XSS protection, SQL injection prevention

### 3. Audit & Compliance
```java
@Component
public class AuditService {
    - User action logging (login, logout, cart operations)
    - Data access logging with timestamps
    - IP address and user agent tracking
    - Compliance report generation
}
```

## Compliance Features

### 1. Data Privacy (GDPR/CCPA)
- **Data Minimization**: Cart deletion on logout
- **Consent Management**: User registration implies consent
- **Data Retention**: 30-day audit log retention
- **Right to Deletion**: User account deletion cascade

### 2. Data Lineage
- **Audit Trail**: Complete action history
- **Data Flow Tracking**: Request-response correlation IDs
- **Change Tracking**: Entity modification timestamps

### 3. Compliance Reporting
```java
@Service
public class ComplianceService {
    - generateDataAccessReport()
    - generateUserActivityReport()
    - generateDataRetentionReport()
    - exportAuditLogs()
}
```

## Data Flow Architecture

### 1. User Registration Flow
```
Client → UserController → ValidationService → UserService 
→ PasswordEncoder → UserRepository → Database → AuditService
```

### 2. Cart Operations Flow
```
Client → CartController → AuthenticationFilter → CartService 
→ ProductService → CartRepository → Database → AuditService
```

### 3. Product Search Flow
```
Client → ProductController → ValidationService → ProductService 
→ ProductRepository → Database (Case-insensitive search)
```

## Error Handling & Resilience

### 1. Circuit Breaker Pattern
```java
@Component
public class DatabaseCircuitBreaker {
    - Failure threshold: 5 consecutive failures
    - Timeout: 10 seconds
    - Half-open retry interval: 30 seconds
}
```

### 2. Retry Mechanism
```java
@Retryable(value = {DataAccessException.class}, maxAttempts = 3)
public class RepositoryService {
    - Exponential backoff: 1s, 2s, 4s
    - Database connection retry
    - Transaction rollback on failure
}
```

### 3. Error Response Format
```json
{
    "timestamp": "2024-06-20T15:30:00Z",
    "status": 400,
    "error": "Bad Request",
    "message": "Validation failed",
    "path": "/api/cart/add",
    "correlationId": "req-12345"
}
```

## API Endpoints Specification

### User Management APIs
```
POST /api/users/register
POST /api/users/login
GET /api/users/profile
PUT /api/users/profile
POST /api/users/logout
```

### Product APIs
```
GET /api/products/search?keyword={keyword}
GET /api/products/{productId}
```

### Cart Management APIs
```
POST /api/cart/add
PUT /api/cart/update
DELETE /api/cart/remove/{productId}
GET /api/cart/view
DELETE /api/cart/clear
```

## Architecture Quality Metrics

**Design Principles:**
- ✅ Separation of Concerns: Clear layer separation
- ✅ Single Responsibility: Each component has focused responsibility
- ✅ Dependency Inversion: Interface-based dependencies
- ✅ Open/Closed Principle: Extensible design patterns

**Performance Considerations:**
- ✅ Database Indexing: Optimized queries with proper indexes
- ✅ Connection Pooling: HikariCP for efficient database connections
- ✅ Lazy Loading: Cart creation only when needed
- ✅ Caching Strategy: Prepared for Redis integration

**Maintainability:**
- ✅ Clear Documentation: Comprehensive design documentation
- ✅ Consistent Naming: Standard Java naming conventions
- ✅ Modular Design: Loosely coupled, highly cohesive modules
- ✅ Test-Friendly: Dependency injection enables easy testing