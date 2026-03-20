# DOMAIN MODEL AND HIGH-LEVEL DESIGN DOCUMENT

## DOMAIN MODEL

### UML Class Diagram

```
┌─────────────────────────────────────┐
│                User                 │
├─────────────────────────────────────┤
│ - id: Long (PK)                     │
│ - username: String (UNIQUE, NOT NULL)│
│ - password: String (NOT NULL)       │
│ - fullName: String (NOT NULL)       │
│ - email: String (NOT NULL)          │
│ - createdDate: LocalDateTime        │
├─────────────────────────────────────┤
│ + signUp()                          │
│ + signIn()                          │
│ + viewProfile()                     │
│ + updateProfile()                   │
└─────────────────────────────────────┘
                    │
                    │ 1
                    │
                    │ 0..1
┌─────────────────────────────────────┐
│                Cart                 │
├─────────────────────────────────────┤
│ - id: Long (PK)                     │
│ - userId: Long (FK, NOT NULL)       │
│ - createdDate: LocalDateTime        │
├─────────────────────────────────────┤
│ + createCart()                      │
│ + addProduct()                      │
│ + removeProduct()                   │
│ + updateQuantity()                  │
│ + viewCart()                        │
│ + calculateTotal()                  │
│ + deleteCart()                      │
└─────────────────────────────────────┘
                    │
                    │ 1
                    │
                    │ 1..*
┌─────────────────────────────────────┐
│              CartItem               │
├─────────────────────────────────────┤
│ - id: Long (PK)                     │
│ - cartId: Long (FK, NOT NULL)       │
│ - productId: Long (FK, NOT NULL)    │
│ - quantity: Integer (NOT NULL, >0)  │
│ - addedDate: LocalDateTime          │
├─────────────────────────────────────┤
│ + updateQuantity()                  │
│ + calculateItemTotal()              │
│ + removeItem()                      │
└─────────────────────────────────────┘
                    │
                    │ *
                    │
                    │ 1
┌─────────────────────────────────────┐
│               Product               │
├─────────────────────────────────────┤
│ - id: Long (PK)                     │
│ - name: String (NOT NULL)           │
│ - description: String               │
│ - price: BigDecimal (NOT NULL)      │
│ - availableQuantity: Integer        │
│ - createdDate: LocalDateTime        │
├─────────────────────────────────────┤
│ + searchProducts()                  │
│ + getProductDetails()               │
│ + validateExistence()               │
└─────────────────────────────────────┘
```

### Entity Relationships

**User ↔ Cart (1:0..1)**
- One user can have at most one active cart
- Cart is created lazily when first product is added
- Cart is deleted on logout or when empty

**Cart ↔ CartItem (1:1..*)**
- One cart must have at least one cart item
- Cart is auto-deleted when last item is removed
- Cascade delete: removing cart removes all cart items

**CartItem ↔ Product (N:1)**
- Multiple cart items can reference the same product
- Product existence is validated before cart operations
- Products are read-only from cart operations perspective

## HIGH-LEVEL DESIGN DOCUMENT

### Architecture Overview

```
┌─────────────────────────────────────────────────────────────┐
│                    PRESENTATION LAYER                       │
├─────────────────────────────────────────────────────────────┤
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐         │
│  │    User     │  │   Product   │  │    Cart     │         │
│  │ Controller  │  │ Controller  │  │ Controller  │         │
│  └─────────────┘  └─────────────┘  └─────────────┘         │
└─────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────┐
│                    BUSINESS LOGIC LAYER                     │
├─────────────────────────────────────────────────────────────┤
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐         │
│  │    User     │  │   Product   │  │    Cart     │         │
│  │   Service   │  │   Service   │  │   Service   │         │
│  └─────────────┘  └─────────────┘  └─────────────┘         │
│                              │                              │
│  ┌─────────────────────────────────────────────────────┐   │
│  │           Security & Validation Service             │   │
│  └─────────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────┐
│                    DATA ACCESS LAYER                        │
├─────────────────────────────────────────────────────────────┤
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐         │
│  │    User     │  │   Product   │  │    Cart     │         │
│  │ Repository  │  │ Repository  │  │ Repository  │         │
│  └─────────────┘  └─────────────┘  └─────────────┘         │
│                                                             │
│  ┌─────────────────────────────────────────────────────┐   │
│  │              CartItem Repository                    │   │
│  └─────────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────┐
│                    DATABASE LAYER                           │
├─────────────────────────────────────────────────────────────┤
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐         │
│  │    users    │  │  products   │  │    carts    │         │
│  │    table    │  │    table    │  │    table    │         │
│  └─────────────┘  └─────────────┘  └─────────────┘         │
│                                                             │
│  ┌─────────────────────────────────────────────────────┐   │
│  │                cart_items table                     │   │
│  └─────────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────────┘
```

### Major Components

#### 1. Controller Layer
- **UserController**: Handles user registration, authentication, profile management
- **ProductController**: Manages product search and retrieval operations
- **CartController**: Manages cart operations including add, update, remove, view

#### 2. Service Layer
- **UserService**: Business logic for user management and authentication
- **ProductService**: Business logic for product search and validation
- **CartService**: Complex business logic for cart lifecycle management
- **SecurityService**: Handles authentication, authorization, and input validation

#### 3. Repository Layer
- **UserRepository**: Data access for user operations with username uniqueness
- **ProductRepository**: Data access for product search with case-insensitive queries
- **CartRepository**: Data access for cart operations with user relationship
- **CartItemRepository**: Data access for cart item operations with cascade handling

#### 4. Database Schema
```sql
-- Users table
CREATE TABLE users (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    full_name VARCHAR(100) NOT NULL,
    email VARCHAR(100) NOT NULL,
    created_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Products table
CREATE TABLE products (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(200) NOT NULL,
    description TEXT,
    price DECIMAL(10,2) NOT NULL,
    available_quantity INTEGER DEFAULT 0,
    created_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Carts table
CREATE TABLE carts (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    created_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    UNIQUE KEY unique_user_cart (user_id)
);

-- Cart items table
CREATE TABLE cart_items (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    cart_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    quantity INTEGER NOT NULL CHECK (quantity > 0),
    added_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (cart_id) REFERENCES carts(id) ON DELETE CASCADE,
    FOREIGN KEY (product_id) REFERENCES products(id),
    UNIQUE KEY unique_cart_product (cart_id, product_id)
);
```

### Integration Points

#### 1. RESTful API Endpoints
```
User Management:
POST   /api/users/signup
POST   /api/users/signin
GET    /api/users/profile
PUT    /api/users/profile

Product Management:
GET    /api/products/search?keyword={keyword}
GET    /api/products/{id}

Cart Management:
POST   /api/cart/items
PUT    /api/cart/items/{itemId}
DELETE /api/cart/items/{itemId}
GET    /api/cart
DELETE /api/cart
```

#### 2. Database Integration
- **Connection Pool**: HikariCP for efficient database connections
- **ORM**: Spring Data JPA with Hibernate for object-relational mapping
- **Transaction Management**: Spring @Transactional for ACID compliance
- **Database Constraints**: Enforced at database level for data integrity

#### 3. Authentication Integration
- **Stateless Authentication**: JWT tokens or session-based validation
- **Password Security**: BCrypt hashing for password storage
- **Request Validation**: Input sanitization and validation at controller level

### Security & Compliance Features

#### 1. Enterprise Security Implementation

**Input Validation & Sanitization**
```java
@Component
public class SecurityValidationService {
    
    // Input validation with regex patterns
    public boolean validateUsername(String username) {
        return username.matches("^[a-zA-Z0-9_]{3,50}$");
    }
    
    // SQL injection prevention through parameterized queries
    // XSS prevention through output encoding
    // CSRF protection through Spring Security
}
```

**Encryption & Data Protection**
- **Data at Rest**: AES-256 encryption for sensitive fields
- **Data in Transit**: TLS 1.3 for all API communications
- **Password Hashing**: BCrypt with salt for password storage
- **Database Encryption**: Transparent Data Encryption (TDE) enabled

**Access Control**
```java
@Service
public class AuthorizationService {
    
    // Role-Based Access Control (RBAC)
    @PreAuthorize("hasRole('USER')")
    public Cart getUserCart(Long userId) {
        // Ensure users can only access their own cart
        return cartRepository.findByUserId(userId);
    }
    
    // Attribute-Based Access Control (ABAC)
    public boolean canModifyCart(User user, Cart cart) {
        return cart.getUserId().equals(user.getId());
    }
}
```

**Audit Logging**
```java
@Component
public class AuditLogger {
    
    @EventListener
    public void logCartOperation(CartOperationEvent event) {
        AuditLog log = AuditLog.builder()
            .userId(event.getUserId())
            .operation(event.getOperation())
            .timestamp(Instant.now())
            .ipAddress(event.getIpAddress())
            .userAgent(event.getUserAgent())
            .build();
        auditRepository.save(log);
    }
}
```

**Secrets Management**
- **Environment Variables**: Database credentials stored in environment variables
- **Key Vault Integration**: Integration with AWS Secrets Manager or Azure Key Vault
- **Configuration Encryption**: Spring Cloud Config with encryption support

#### 2. Compliance Features

**Data Retention & Privacy**
```java
@Service
public class DataRetentionService {
    
    // GDPR Article 17 - Right to be forgotten
    @Scheduled(cron = "0 0 2 * * ?") // Daily at 2 AM
    public void processDataDeletionRequests() {
        List<User> usersToDelete = userRepository.findUsersMarkedForDeletion();
        for (User user : usersToDelete) {
            deleteUserDataCompletely(user);
        }
    }
    
    // Data retention policy enforcement
    @Scheduled(cron = "0 0 3 * * ?") // Daily at 3 AM
    public void enforceDataRetention() {
        LocalDateTime cutoffDate = LocalDateTime.now().minusYears(7);
        auditRepository.deleteAuditLogsOlderThan(cutoffDate);
    }
}
```

**Consent Management**
```java
@Entity
public class UserConsent {
    private Long userId;
    private ConsentType consentType;
    private boolean granted;
    private LocalDateTime consentDate;
    private String ipAddress;
    private String legalBasis;
}

@Service
public class ConsentService {
    
    public boolean hasValidConsent(Long userId, ConsentType type) {
        return consentRepository.findValidConsent(userId, type).isPresent();
    }
    
    public void recordConsent(Long userId, ConsentType type, boolean granted) {
        UserConsent consent = new UserConsent(userId, type, granted);
        consentRepository.save(consent);
    }
}
```

**Data Lineage & Tracking**
```java
@Component
public class DataLineageTracker {
    
    @EventListener
    public void trackDataMovement(DataAccessEvent event) {
        DataLineage lineage = DataLineage.builder()
            .sourceTable(event.getSourceTable())
            .targetTable(event.getTargetTable())
            .operation(event.getOperation())
            .userId(event.getUserId())
            .timestamp(Instant.now())
            .dataClassification(event.getDataClassification())
            .build();
        lineageRepository.save(lineage);
    }
}
```

**Compliance Reporting**
```java
@Service
public class ComplianceReportingService {
    
    // SOC2 Type II compliance reporting
    public ComplianceReport generateSOC2Report(LocalDate startDate, LocalDate endDate) {
        return ComplianceReport.builder()
            .reportType("SOC2_TYPE_II")
            .period(DateRange.of(startDate, endDate))
            .securityControls(getSecurityControlsStatus())
            .auditTrail(getAuditTrailSummary(startDate, endDate))
            .accessControls(getAccessControlsSummary())
            .build();
    }
    
    // GDPR compliance reporting
    public GDPRComplianceReport generateGDPRReport() {
        return GDPRComplianceReport.builder()
            .dataProcessingActivities(getDataProcessingActivities())
            .consentRecords(getConsentSummary())
            .dataBreachIncidents(getDataBreachIncidents())
            .dataSubjectRequests(getDataSubjectRequestsSummary())
            .build();
    }
}
```

### Error Handling & Resilience Patterns

#### 1. Retry Mechanisms
```java
@Service
public class CartService {
    
    @Retryable(value = {DataAccessException.class}, maxAttempts = 3, backoff = @Backoff(delay = 1000))
    public Cart addProductToCart(Long userId, Long productId, Integer quantity) {
        try {
            return performCartOperation(userId, productId, quantity);
        } catch (OptimisticLockingFailureException e) {
            log.warn("Optimistic locking failure, retrying cart operation", e);
            throw e; // Will trigger retry
        }
    }
    
    @Recover
    public Cart recoverFromCartOperation(DataAccessException ex, Long userId, Long productId, Integer quantity) {
        log.error("Failed to add product to cart after retries", ex);
        throw new CartOperationException("Unable to add product to cart. Please try again later.");
    }
}
```

#### 2. Circuit Breaker Pattern
```java
@Component
public class ProductSearchService {
    
    @CircuitBreaker(name = "productSearch", fallbackMethod = "fallbackProductSearch")
    public List<Product> searchProducts(String keyword) {
        return productRepository.findByNameContainingIgnoreCase(keyword);
    }
    
    public List<Product> fallbackProductSearch(String keyword, Exception ex) {
        log.warn("Product search circuit breaker activated", ex);
        return getCachedSearchResults(keyword);
    }
}
```

#### 3. Comprehensive Logging
```java
@Component
public class ApplicationLogger {
    
    @EventListener
    public void logSecurityEvent(SecurityEvent event) {
        if (event.isSuspicious()) {
            securityLogger.warn("Suspicious activity detected: {}", event);
            alertingService.sendSecurityAlert(event);
        }
    }
    
    @EventListener
    public void logBusinessEvent(BusinessEvent event) {
        businessLogger.info("Business operation: {} by user: {}", 
            event.getOperation(), event.getUserId());
    }
    
    @EventListener
    public void logErrorEvent(ErrorEvent event) {
        errorLogger.error("Application error: {} in component: {}", 
            event.getError(), event.getComponent(), event.getException());
    }
}
```

### Data Flow Architecture

#### 1. User Registration Flow
```
Client Request → UserController.signUp() → UserService.createUser() → 
SecurityService.validateInput() → SecurityService.hashPassword() → 
UserRepository.save() → Database Constraint Validation → Response
```

#### 2. Cart Operation Flow
```
Client Request → CartController.addProduct() → AuthenticationFilter → 
CartService.addProductToCart() → ProductService.validateProduct() → 
CartRepository.findByUserId() → [Create Cart if not exists] → 
CartItemRepository.save() → AuditLogger.logCartOperation() → Response
```

#### 3. Logout Flow
```
Client Request → UserController.logout() → CartService.deleteUserCart() → 
CartItemRepository.deleteByCartId() → CartRepository.deleteByUserId() → 
SessionService.invalidateSession() → AuditLogger.logLogout() → Response
```