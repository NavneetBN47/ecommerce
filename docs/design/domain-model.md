# DOMAIN MODEL

## UML Class Diagram

```
┌─────────────────────────────────────┐
│              User                   │
├─────────────────────────────────────┤
│ - userId: Long (PK)                 │
│ - username: String (UNIQUE, NOT NULL)│
│ - password: String (NOT NULL)       │
│ - fullName: String (NOT NULL)       │
│ - email: String (NOT NULL)          │
│ - createdDate: LocalDateTime        │
├─────────────────────────────────────┤
│ + register()                        │
│ + authenticate()                    │
│ + updateProfile()                   │
│ + viewProfile()                     │
└─────────────────────────────────────┘
                │
                │ 1
                │
                │ 0..1
┌─────────────────────────────────────┐
│             Cart                    │
├─────────────────────────────────────┤
│ - cartId: Long (PK)                 │
│ - userId: Long (FK, UNIQUE)         │
│ - createdDate: LocalDateTime        │
│ - lastModified: LocalDateTime       │
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
│           CartItem                  │
├─────────────────────────────────────┤
│ - cartItemId: Long (PK)             │
│ - cartId: Long (FK)                 │
│ - productId: Long (FK)              │
│ - quantity: Integer (CHECK > 0)     │
│ - addedDate: LocalDateTime          │
├─────────────────────────────────────┤
│ + addToCart()                       │
│ + updateQuantity()                  │
│ + removeFromCart()                  │
│ + calculateItemTotal()              │
└─────────────────────────────────────┘
                │
                │ *
                │
                │ 1
┌─────────────────────────────────────┐
│            Product                  │
├─────────────────────────────────────┤
│ - productId: Long (PK)              │
│ - name: String (NOT NULL)           │
│ - description: String               │
│ - price: BigDecimal (NOT NULL)      │
│ - availableQuantity: Integer        │
│ - createdDate: LocalDateTime        │
├─────────────────────────────────────┤
│ + searchProducts()                  │
│ + getProductDetails()               │
│ + checkAvailability()               │
└─────────────────────────────────────┘

┌─────────────────────────────────────┐
│         AuditLog                    │
├─────────────────────────────────────┤
│ - logId: Long (PK)                  │
│ - userId: Long                      │
│ - action: String                    │
│ - entityType: String                │
│ - entityId: Long                    │
│ - timestamp: LocalDateTime          │
│ - ipAddress: String                 │
│ - userAgent: String                 │
├─────────────────────────────────────┤
│ + logAction()                       │
│ + generateReport()                  │
└─────────────────────────────────────┘
```

## Entity Relationships

**User → Cart (1:0..1)**
- One user can have zero or one active cart
- Cart is created lazily when first product is added
- Cart is deleted on logout or when empty

**Cart → CartItem (1:1..*)**
- One cart must have at least one cart item
- Cart is auto-deleted when last item is removed
- Cascade delete enforced

**CartItem → Product (*:1)**
- Many cart items can reference one product
- Product existence validated before cart operations
- Foreign key constraint enforced

## Database Schema

```sql
-- Users Table
CREATE TABLE users (
    user_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    full_name_encrypted VARBINARY(255) NOT NULL,
    email_encrypted VARBINARY(255) NOT NULL,
    created_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_username (username)
);

-- Products Table
CREATE TABLE products (
    product_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    price DECIMAL(10,2) NOT NULL,
    available_quantity INT DEFAULT 0,
    created_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FULLTEXT INDEX idx_product_search (name, description)
);

-- Carts Table
CREATE TABLE carts (
    cart_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT UNIQUE NOT NULL,
    created_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    last_modified TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
);

-- Cart Items Table
CREATE TABLE cart_items (
    cart_item_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    cart_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    quantity INT NOT NULL CHECK (quantity > 0),
    added_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (cart_id) REFERENCES carts(cart_id) ON DELETE CASCADE,
    FOREIGN KEY (product_id) REFERENCES products(product_id),
    UNIQUE KEY unique_cart_product (cart_id, product_id)
);

-- Audit Log Table
CREATE TABLE audit_logs (
    log_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT,
    action VARCHAR(100) NOT NULL,
    entity_type VARCHAR(50),
    entity_id BIGINT,
    timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    ip_address VARCHAR(45),
    user_agent TEXT,
    INDEX idx_user_timestamp (user_id, timestamp),
    INDEX idx_action_timestamp (action, timestamp)
);
```