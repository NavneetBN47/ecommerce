# COMPREHENSIVE BACKEND ENGINEERING SPECIFICATION PACKAGE
## SCRUM-96: Implement Core Shopping Cart Backend Services Using Spring Boot MVC

---

## 1. DOMAIN ENTITIES AND ATTRIBUTES MAPPING

### 1.1 User Entity
```java
@Entity
@Table(name = "users", uniqueConstraints = {
    @UniqueConstraint(columnNames = "username")
})
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, unique = true, length = 50)
    private String username;
    
    @Column(nullable = false, length = 255)
    private String password;
    
    @Column(nullable = false, length = 100)
    private String fullName;
    
    @Column(nullable = false, length = 100)
    private String email;
    
    @Column(nullable = false, updatable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date createdDate;
    
    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private Cart cart;
}
```

**Attributes:**
- `id` (Long, PK, Auto-increment): Unique identifier
- `username` (String, NOT NULL, UNIQUE, max 50): User's login name
- `password` (String, NOT NULL, max 255): Encrypted password
- `fullName` (String, NOT NULL, max 100): User's full name
- `email` (String, NOT NULL, max 100): User's email address
- `createdDate` (Timestamp, NOT NULL, immutable): Account creation timestamp

**Constraints:**
- Username must be unique
- All fields except id are mandatory
- Username is immutable after creation
- CreatedDate is set once at creation

---

### 1.2 Product Entity
```java
@Entity
@Table(name = "products")
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, length = 200)
    private String name;
    
    @Column(columnDefinition = "TEXT")
    private String description;
    
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;
    
    @Column(nullable = false)
    private Integer availableQuantity;
    
    @Version
    @Column(nullable = false)
    private Long version;
    
    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL)
    private List<CartItem> cartItems;
}
```

**Attributes:**
- `id` (Long, PK, Auto-increment): Unique identifier
- `name` (String, NOT NULL, max 200): Product name
- `description` (Text, nullable): Product description
- `price` (Decimal(10,2), NOT NULL): Product price
- `availableQuantity` (Integer, NOT NULL): Stock quantity
- `version` (Long, NOT NULL): Optimistic locking version

**Constraints:**
- Price must be non-negative
- Available quantity must be non-negative
- Product must exist before being added to cart
- Version column for optimistic locking to prevent concurrent modification issues

---

### 1.3 Cart Entity
```java
@Entity
@Table(name = "carts")
public class Cart {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @OneToOne
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;
    
    @Column(nullable = false, updatable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date createdDate;
    
    @OneToMany(mappedBy = "cart", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CartItem> cartItems = new ArrayList<>();
}
```

**Attributes:**
- `id` (Long, PK, Auto-increment): Unique identifier
- `user_id` (Long, FK to User, NOT NULL, UNIQUE): Owner of the cart
- `createdDate` (Timestamp, NOT NULL, immutable): Cart creation timestamp

**Constraints:**
- One cart per user (1:1 relationship)
- Cart must belong to exactly one user
- Cart cannot exist without at least one cart item
- Lazy creation: created only when first product is added
- Auto-deleted when last item is removed or on logout

---

### 1.4 CartItem Entity
```java
@Entity
@Table(name = "cart_items", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"cart_id", "product_id"})
})
public class CartItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "cart_id", nullable = false)
    private Cart cart;
    
    @ManyToOne
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;
    
    @Column(nullable = false)
    private Integer quantity;
    
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal priceAtAddition;
}
```

**Attributes:**
- `id` (Long, PK, Auto-increment): Unique identifier
- `cart_id` (Long, FK to Cart, NOT NULL): Parent cart
- `product_id` (Long, FK to Product, NOT NULL): Referenced product
- `quantity` (Integer, NOT NULL): Quantity of product
- `priceAtAddition` (Decimal(10,2), NOT NULL): Price snapshot when added

**Constraints:**
- Each cart item belongs to exactly one cart
- Quantity must be greater than zero
- Unique combination of cart_id and product_id (no duplicate products in same cart)
- Price is immutable after addition (snapshot)

---

## 6. TECHNICAL ARTIFACTS

### 6.1 Entity-Relationship Diagram (ERD)

```mermaid
erDiagram
    USERS ||--o| CARTS : "has one"
    CARTS ||--|{ CART_ITEMS : "contains"
    PRODUCTS ||--|{ CART_ITEMS : "referenced by"
    
    USERS {
        BIGINT id PK "Auto-increment"
        VARCHAR(50) username UK "NOT NULL, UNIQUE"
        VARCHAR(255) password "NOT NULL"
        VARCHAR(100) full_name "NOT NULL"
        VARCHAR(100) email "NOT NULL"
        TIMESTAMP created_date "NOT NULL"
    }
    
    PRODUCTS {
        BIGINT id PK "Auto-increment"
        VARCHAR(200) name "NOT NULL"
        TEXT description "NULLABLE"
        DECIMAL(10,2) price "NOT NULL"
        INTEGER available_quantity "NOT NULL"
        BIGINT version "NOT NULL, Optimistic Lock"
    }
    
    CARTS {
        BIGINT id PK "Auto-increment"
        BIGINT user_id FK "NOT NULL, UNIQUE"
        TIMESTAMP created_date "NOT NULL"
    }
    
    CART_ITEMS {
        BIGINT id PK "Auto-increment"
        BIGINT cart_id FK "NOT NULL"
        BIGINT product_id FK "NOT NULL"
        INTEGER quantity "NOT NULL, > 0"
        DECIMAL(10,2) price_at_addition "NOT NULL"
    }
```

---

### 6.2 Add to Cart Sequence Diagram

```mermaid
sequenceDiagram
    participant Client
    participant CartController
    participant CartService
    participant ProductRepository
    participant CartRepository
    participant Database
    
    Client->>CartController: POST /api/carts/items<br/>{productId, quantity}
    activate CartController
    
    CartController->>CartService: addProductToCart(userId, productId, quantity)
    activate CartService
    
    Note over CartService: Validate Input Parameters
    
    CartService->>ProductRepository: findByIdWithLock(productId)
    activate ProductRepository
    ProductRepository->>Database: SELECT * FROM products<br/>WHERE id = ? FOR UPDATE
    activate Database
    Database-->>ProductRepository: Product Entity (with version)
    deactivate Database
    ProductRepository-->>CartService: Product
    deactivate ProductRepository
    
    alt Product Not Found
        CartService-->>CartController: throw ProductNotFoundException
        CartController-->>Client: 404 Not Found
    end
    
    Note over CartService: Check Stock Availability
    
    alt Insufficient Stock
        CartService-->>CartController: throw InsufficientStockException
        CartController-->>Client: 409 Conflict<br/>"Insufficient stock"
    end
    
    Note over CartService: Lazy Cart Creation Logic
    
    CartService->>CartRepository: findByUserId(userId)
    activate CartRepository
    CartRepository->>Database: SELECT * FROM carts<br/>WHERE user_id = ?
    activate Database
    Database-->>CartRepository: Cart or NULL
    deactivate Database
    CartRepository-->>CartService: Optional<Cart>
    deactivate CartRepository
    
    alt Cart Does Not Exist
        Note over CartService: Create New Cart (Lazy Creation)
        CartService->>CartService: cart = new Cart(user)
        CartService->>CartRepository: save(cart)
        activate CartRepository
        CartRepository->>Database: INSERT INTO carts<br/>(user_id, created_date)
        activate Database
        Database-->>CartRepository: Cart ID
        deactivate Database
        CartRepository-->>CartService: Saved Cart
        deactivate CartRepository
    end
    
    Note over CartService: Check if Product Already in Cart
    
    alt Product Already in Cart
        Note over CartService: Update Existing Cart Item Quantity
        CartService->>CartService: cartItem.quantity += quantity
    else Product Not in Cart
        Note over CartService: Create New Cart Item
        CartService->>CartService: cartItem = new CartItem(cart, product, quantity)
    end
    
    Note over CartService: Capture Price Snapshot
    CartService->>CartService: cartItem.priceAtAddition = product.price
    
    CartService->>CartRepository: save(cart)
    activate CartRepository
    CartRepository->>Database: UPDATE/INSERT cart_items
    activate Database
    
    alt Optimistic Locking Failure
        Database-->>CartRepository: OptimisticLockException<br/>(version mismatch)
        deactivate Database
        CartRepository-->>CartService: throw OptimisticLockException
        deactivate CartRepository
        CartService-->>CartController: throw ConcurrentModificationException
        CartController-->>Client: 409 Conflict<br/>"Product modified, retry"
    else Success
        Database-->>CartRepository: Success
        deactivate Database
        CartRepository-->>CartService: Updated Cart
        deactivate CartRepository
    end
    
    CartService-->>CartController: CartItemDTO
    deactivate CartService
    
    CartController-->>Client: 201 Created<br/>CartItemDTO
    deactivate CartController
```

---

### 6.3 Database Model (PostgreSQL DDL)

```sql
-- ============================================
-- SCRUM-96: Shopping Cart Database Schema
-- Database: PostgreSQL 14+
-- ============================================

-- Drop existing tables (for clean setup)
DROP TABLE IF EXISTS cart_items CASCADE;
DROP TABLE IF EXISTS carts CASCADE;
DROP TABLE IF EXISTS products CASCADE;
DROP TABLE IF EXISTS users CASCADE;

-- ============================================
-- Table: users
-- Description: Stores user account information
-- ============================================
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    full_name VARCHAR(100) NOT NULL,
    email VARCHAR(100) NOT NULL,
    created_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    -- Constraints
    CONSTRAINT chk_username_length CHECK (LENGTH(username) >= 3),
    CONSTRAINT chk_email_format CHECK (email ~* '^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}$')
);

-- Indexes for users table
CREATE UNIQUE INDEX idx_users_username ON users(username);
CREATE INDEX idx_users_email ON users(email);

COMMENT ON TABLE users IS 'Stores user account information';
COMMENT ON COLUMN users.id IS 'Primary key, auto-incrementing user identifier';
COMMENT ON COLUMN users.username IS 'Unique username for login, 3-50 characters';
COMMENT ON COLUMN users.password IS 'Encrypted password hash';
COMMENT ON COLUMN users.created_date IS 'Account creation timestamp, immutable';

-- ============================================
-- Table: products
-- Description: Stores product catalog information
-- ============================================
CREATE TABLE products (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(200) NOT NULL,
    description TEXT,
    price DECIMAL(10, 2) NOT NULL,
    available_quantity INTEGER NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,
    
    -- Constraints
    CONSTRAINT chk_price_positive CHECK (price >= 0),
    CONSTRAINT chk_quantity_non_negative CHECK (available_quantity >= 0),
    CONSTRAINT chk_name_not_empty CHECK (LENGTH(TRIM(name)) > 0)
);

-- Indexes for products table
CREATE INDEX idx_products_name ON products(name);
CREATE INDEX idx_products_price ON products(price);

COMMENT ON TABLE products IS 'Stores product catalog with inventory and pricing';
COMMENT ON COLUMN products.id IS 'Primary key, auto-incrementing product identifier';
COMMENT ON COLUMN products.price IS 'Product price with 2 decimal precision';
COMMENT ON COLUMN products.available_quantity IS 'Current stock quantity';
COMMENT ON COLUMN products.version IS 'Optimistic locking version for concurrent update control';

-- ============================================
-- Table: carts
-- Description: Stores shopping carts (one per user)
-- ============================================
CREATE TABLE carts (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE,
    created_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    -- Foreign Key Constraints
    CONSTRAINT fk_carts_user FOREIGN KEY (user_id) 
        REFERENCES users(id) 
        ON DELETE CASCADE
        ON UPDATE CASCADE
);

-- Indexes for carts table
CREATE UNIQUE INDEX idx_carts_user_id ON carts(user_id);

COMMENT ON TABLE carts IS 'Stores shopping carts with one-to-one relationship to users';
COMMENT ON COLUMN carts.id IS 'Primary key, auto-incrementing cart identifier';
COMMENT ON COLUMN carts.user_id IS 'Foreign key to users table, unique (one cart per user)';
COMMENT ON COLUMN carts.created_date IS 'Cart creation timestamp, immutable';

-- ============================================
-- Table: cart_items
-- Description: Stores individual items in shopping carts
-- ============================================
CREATE TABLE cart_items (
    id BIGSERIAL PRIMARY KEY,
    cart_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    quantity INTEGER NOT NULL,
    price_at_addition DECIMAL(10, 2) NOT NULL,
    
    -- Constraints
    CONSTRAINT chk_quantity_positive CHECK (quantity > 0),
    CONSTRAINT chk_price_at_addition_positive CHECK (price_at_addition >= 0),
    
    -- Foreign Key Constraints
    CONSTRAINT fk_cart_items_cart FOREIGN KEY (cart_id) 
        REFERENCES carts(id) 
        ON DELETE CASCADE
        ON UPDATE CASCADE,
    
    CONSTRAINT fk_cart_items_product FOREIGN KEY (product_id) 
        REFERENCES products(id) 
        ON DELETE CASCADE
        ON UPDATE CASCADE,
    
    -- Unique Constraint: No duplicate products in same cart
    CONSTRAINT uk_cart_product UNIQUE (cart_id, product_id)
);

-- Indexes for cart_items table
CREATE INDEX idx_cart_items_cart_id ON cart_items(cart_id);
CREATE INDEX idx_cart_items_product_id ON cart_items(product_id);
CREATE UNIQUE INDEX idx_cart_items_cart_product ON cart_items(cart_id, product_id);

COMMENT ON TABLE cart_items IS 'Stores individual line items within shopping carts';
COMMENT ON COLUMN cart_items.id IS 'Primary key, auto-incrementing cart item identifier';
COMMENT ON COLUMN cart_items.cart_id IS 'Foreign key to carts table';
COMMENT ON COLUMN cart_items.product_id IS 'Foreign key to products table';
COMMENT ON COLUMN cart_items.quantity IS 'Quantity of product in cart, must be > 0';
COMMENT ON COLUMN cart_items.price_at_addition IS 'Price snapshot when product was added to cart';

-- ============================================
-- Sample Data (Optional - for testing)
-- ============================================

-- Insert sample users
INSERT INTO users (username, password, full_name, email) VALUES
('john_doe', '$2a$10$encrypted_password_hash_1', 'John Doe', 'john.doe@example.com'),
('jane_smith', '$2a$10$encrypted_password_hash_2', 'Jane Smith', 'jane.smith@example.com');

-- Insert sample products
INSERT INTO products (name, description, price, available_quantity, version) VALUES
('Wireless Mouse', 'Ergonomic wireless mouse with USB receiver', 29.99, 150, 0),
('USB Keyboard', 'Mechanical keyboard with RGB lighting', 79.99, 75, 0),
('Monitor 24"', 'Full HD 1080p LED monitor', 199.99, 30, 0),
('Laptop Stand', 'Adjustable aluminum laptop stand', 39.99, 200, 0),
('Webcam HD', '1080p HD webcam with built-in microphone', 59.99, 100, 0);

-- ============================================
-- Performance Optimization Queries
-- ============================================

-- Analyze tables for query optimization
ANALYZE users;
ANALYZE products;
ANALYZE carts;
ANALYZE cart_items;

-- ============================================
-- Verification Queries
-- ============================================

-- Verify table creation
SELECT 
    table_name, 
    table_type
FROM 
    information_schema.tables
WHERE 
    table_schema = 'public'
    AND table_name IN ('users', 'products', 'carts', 'cart_items')
ORDER BY 
    table_name;

-- Verify foreign key constraints
SELECT
    tc.constraint_name,
    tc.table_name,
    kcu.column_name,
    ccu.table_name AS foreign_table_name,
    ccu.column_name AS foreign_column_name,
    rc.delete_rule,
    rc.update_rule
FROM
    information_schema.table_constraints AS tc
    JOIN information_schema.key_column_usage AS kcu
        ON tc.constraint_name = kcu.constraint_name
        AND tc.table_schema = kcu.table_schema
    JOIN information_schema.constraint_column_usage AS ccu
        ON ccu.constraint_name = tc.constraint_name
        AND ccu.table_schema = tc.table_schema
    JOIN information_schema.referential_constraints AS rc
        ON tc.constraint_name = rc.constraint_name
WHERE
    tc.constraint_type = 'FOREIGN KEY'
    AND tc.table_schema = 'public'
ORDER BY
    tc.table_name,
    tc.constraint_name;

-- Verify indexes
SELECT
    tablename,
    indexname,
    indexdef
FROM
    pg_indexes
WHERE
    schemaname = 'public'
    AND tablename IN ('users', 'products', 'carts', 'cart_items')
ORDER BY
    tablename,
    indexname;

-- ============================================
-- END OF SCHEMA DEFINITION
-- ============================================
```

---

## 7. IMPLEMENTATION NOTES

### 7.1 Optimistic Locking Implementation
- The `version` column in the `products` table enables JPA's optimistic locking mechanism
- When a product is updated, JPA automatically increments the version
- If two transactions try to update the same product simultaneously, the second will fail with `OptimisticLockException`
- The application should catch this exception and return a 409 Conflict response with a retry message

### 7.2 Lazy Cart Creation
- Carts are not created during user registration
- A cart is created only when the user adds the first product
- This approach reduces database overhead for users who never use the shopping cart feature
- The sequence diagram explicitly shows this lazy creation logic

### 7.3 Cascade Delete Behavior
- When a user is deleted, their cart is automatically deleted (ON DELETE CASCADE)
- When a cart is deleted, all cart items are automatically deleted (ON DELETE CASCADE)
- When a product is deleted, all cart items referencing it are deleted (business decision - alternative: prevent deletion if in carts)

### 7.4 Index Strategy
- Primary keys automatically have indexes
- Foreign keys have explicit indexes for join performance
- Unique constraints (username, cart-product combination) have unique indexes
- Additional indexes on frequently queried columns (email, product name, price)

### 7.5 Data Integrity
- CHECK constraints enforce business rules at the database level
- NOT NULL constraints prevent missing required data
- UNIQUE constraints prevent duplicate entries
- Foreign key constraints maintain referential integrity

---

**Document Version:** 2.0  
**Last Updated:** 2024-01-15  
**Status:** Enhanced with Technical Artifacts  
**Author:** Backend Engineering Team  
**Reviewers:** Technical Lead, Database Administrator, QA Lead

---

*End of Enhanced Low Level Design Document*