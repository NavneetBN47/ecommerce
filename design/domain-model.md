# COMPLETE DOMAIN MODEL FOR SHOPPING CART BACKEND SERVICES
## Traceability Reference: SCRUM-96

---

## OUTPUT 1: UML CLASS DIAGRAM (PlantUML Syntax)

```plantuml
@startuml Shopping_Cart_Domain_Model

!define PII_COLOR #FFE4E1
!define SENSITIVE_COLOR #FFF8DC
!define AGGREGATE_ROOT #E6F3FF

' Stereotypes and Notes
note top of User
  <b>Aggregate Root: User Management</b>
  Security: PII fields encrypted at rest
  Password: bcrypt/PBKDF2 hashed
  Audit: Authentication & profile changes
end note

note top of Cart
  <b>Aggregate Root: Shopping Cart</b>
  Business Rules:
  - Lazy creation (on first item add)
  - Auto-delete when empty
  - Mandatory cleanup on logout
  - One active cart per user
end note

note top of Product
  <b>Aggregate Root: Product Catalog</b>
  Search: Case-insensitive
  Out of Scope: Inventory locking
end note

' User Entity (User Management Aggregate)
class User <<Entity>> <<AggregateRoot>> {
  - username : String {unique, not null, PII}
  - password : String {not null, hashed}
  - full_name : String {not null, PII}
  - email : String {not null, PII}
  - created_date : LocalDateTime {not null}
  --
  + getUsername() : String
  + getFullName() : String
  + getEmail() : String
  + getCreatedDate() : LocalDateTime
  + validatePassword(raw : String) : boolean
}

' Product Entity (Product Catalog Aggregate)
class Product <<Entity>> <<AggregateRoot>> {
  - product_id : Long {PK, auto-generated}
  - name : String {not null, indexed}
  - description : String
  - price : BigDecimal {not null, precision=10, scale=2}
  - available_quantity : Integer {not null, >= 0}
  --
  + getProductId() : Long
  + getName() : String
  + getDescription() : String
  + getPrice() : BigDecimal
  + getAvailableQuantity() : Integer
  + isAvailable() : boolean
  + updateQuantity(delta : Integer) : void
}

' Cart Entity (Shopping Cart Aggregate Root)
class Cart <<Entity>> <<AggregateRoot>> {
  - cart_id : Long {PK, auto-generated}
  - user_id : String {FK, unique, not null}
  - created_date : LocalDateTime {not null}
  --
  + getCartId() : Long
  + getUserId() : String
  + getCreatedDate() : LocalDateTime
  + addItem(product : Product, quantity : Integer) : CartItem
  + removeItem(cartItemId : Long) : void
  + updateItemQuantity(cartItemId : Long, quantity : Integer) : void
  + clearCart() : void
  + getTotalItems() : Integer
  + getTotalPrice() : BigDecimal
  + isEmpty() : boolean
}

' CartItem Entity (Child of Shopping Cart Aggregate)
class CartItem <<Entity>> {
  - cart_item_id : Long {PK, auto-generated}
  - cart_id : Long {FK, not null}
  - product_id : Long {FK, not null}
  - quantity : Integer {not null, > 0}
  --
  + getCartItemId() : Long
  + getCartId() : Long
  + getProductId() : Long
  + getQuantity() : Integer
  + setQuantity(quantity : Integer) : void
  + getSubtotal() : BigDecimal
  + validateQuantity() : boolean
}

' Relationships with Cardinalities
User "1" -- "0..1" Cart : has active cart >
Cart "1" *-- "1..*" CartItem : contains >
CartItem "0..*" --> "1" Product : references >

' Constraints
note right of User::username
  UNIQUE constraint
  PII - Encrypted at rest
end note

note right of User::password
  Hashed with bcrypt/PBKDF2
  Never returned in responses
end note

note right of CartItem::quantity
  CHECK constraint: quantity > 0
  Validated on insert/update
end note

note bottom of Cart
  Business Rule: Cannot exist
  without at least one CartItem
  (enforced at service layer)
end note

@enduml
```

---

## OUTPUT 2: JSON SCHEMA OF COMPLETE DOMAIN MODEL

```json
{
  "$schema": "http://json-schema.org/draft-07/schema#",
  "title": "Shopping Cart Backend Services Domain Model",
  "description": "Complete domain model schema for Shopping Cart system with security and compliance metadata",
  "version": "1.0.0",
  "traceabilityReference": "SCRUM-96",
  "definitions": {
    "User": {
      "type": "object",
      "description": "User entity - Aggregate Root for User Management bounded context",
      "aggregateRoot": true,
      "boundedContext": "UserManagement",
      "properties": {
        "username": {
          "type": "string",
          "description": "Unique username for authentication",
          "minLength": 3,
          "maxLength": 50,
          "pattern": "^[a-zA-Z0-9_-]+$",
          "constraints": ["UNIQUE", "NOT NULL"],
          "securityClassification": "PII",
          "encryptionRequired": true,
          "indexed": true,
          "primaryKey": true
        },
        "password": {
          "type": "string",
          "description": "Hashed password using bcrypt or PBKDF2",
          "minLength": 60,
          "maxLength": 255,
          "constraints": ["NOT NULL"],
          "securityClassification": "SENSITIVE",
          "hashingAlgorithm": "bcrypt",
          "writeOnly": true,
          "auditLog": false
        },
        "full_name": {
          "type": "string",
          "description": "User's full legal name",
          "minLength": 1,
          "maxLength": 100,
          "constraints": ["NOT NULL"],
          "securityClassification": "PII",
          "encryptionRequired": true,
          "gdprRelevant": true
        },
        "email": {
          "type": "string",
          "description": "User's email address",
          "format": "email",
          "maxLength": 255,
          "constraints": ["NOT NULL"],
          "securityClassification": "PII",
          "encryptionRequired": true,
          "gdprRelevant": true,
          "indexed": true
        },
        "created_date": {
          "type": "string",
          "format": "date-time",
          "description": "Timestamp when user account was created",
          "constraints": ["NOT NULL"],
          "defaultValue": "CURRENT_TIMESTAMP",
          "immutable": true
        }
      },
      "required": ["username", "password", "full_name", "email", "created_date"],
      "auditRequirements": {
        "events": ["AUTHENTICATION", "PROFILE_UPDATE", "ACCOUNT_CREATION", "ACCOUNT_DELETION"],
        "retentionPeriod": "7 years",
        "complianceFrameworks": ["GDPR", "CCPA"]
      }
    },
    "Product": {
      "type": "object",
      "description": "Product entity - Aggregate Root for Product Catalog bounded context",
      "aggregateRoot": true,
      "boundedContext": "ProductCatalog",
      "properties": {
        "product_id": {
          "type": "integer",
          "format": "int64",
          "description": "Unique product identifier",
          "constraints": ["PRIMARY KEY", "AUTO_INCREMENT"],
          "minimum": 1
        },
        "name": {
          "type": "string",
          "description": "Product name (case-insensitive search enabled)",
          "minLength": 1,
          "maxLength": 200,
          "constraints": ["NOT NULL"],
          "indexed": true,
          "searchable": true,
          "caseInsensitive": true
        },
        "description": {
          "type": "string",
          "description": "Detailed product description",
          "maxLength": 2000,
          "constraints": ["NULLABLE"]
        },
        "price": {
          "type": "number",
          "description": "Product price in decimal format",
          "format": "decimal",
          "precision": 10,
          "scale": 2,
          "minimum": 0.01,
          "constraints": ["NOT NULL"],
          "validationRule": "price > 0"
        },
        "available_quantity": {
          "type": "integer",
          "format": "int32",
          "description": "Current available quantity in inventory",
          "minimum": 0,
          "constraints": ["NOT NULL", "CHECK >= 0"],
          "defaultValue": 0
        }
      },
      "required": ["product_id", "name", "price", "available_quantity"],
      "businessRules": [
        "Case-insensitive product search required",
        "Inventory locking out of scope for this release"
      ]
    },
    "Cart": {
      "type": "object",
      "description": "Shopping Cart entity - Aggregate Root for Shopping Cart bounded context",
      "aggregateRoot": true,
      "boundedContext": "ShoppingCart",
      "properties": {
        "cart_id": {
          "type": "integer",
          "format": "int64",
          "description": "Unique cart identifier",
          "constraints": ["PRIMARY KEY", "AUTO_INCREMENT"],
          "minimum": 1
        },
        "user_id": {
          "type": "string",
          "description": "Foreign key reference to User.username",
          "minLength": 3,
          "maxLength": 50,
          "constraints": ["FOREIGN KEY", "UNIQUE", "NOT NULL"],
          "references": {
            "entity": "User",
            "field": "username",
            "onDelete": "CASCADE"
          },
          "indexed": true
        },
        "created_date": {
          "type": "string",
          "format": "date-time",
          "description": "Timestamp when cart was created",
          "constraints": ["NOT NULL"],
          "defaultValue": "CURRENT_TIMESTAMP",
          "immutable": true
        },
        "cartItems": {
          "type": "array",
          "description": "Collection of items in the cart",
          "items": {
            "$ref": "#/definitions/CartItem"
          },
          "minItems": 1,
          "relationship": "ONE_TO_MANY"
        }
      },
      "required": ["cart_id", "user_id", "created_date"],
      "businessRules": [
        "Lazy creation: Cart created only when first product is added",
        "Auto-delete: Cart automatically deleted when last item is removed",
        "Mandatory cleanup: Cart must be deleted on user logout",
        "One active cart per user enforced at application level",
        "Cart cannot exist without at least one CartItem"
      ],
      "auditRequirements": {
        "events": ["CART_CREATED", "CART_CLEARED", "CART_DELETED", "ITEM_ADDED", "ITEM_REMOVED", "ITEM_QUANTITY_UPDATED"],
        "retentionPeriod": "90 days",
        "complianceFrameworks": ["Internal Audit Policy"]
      }
    },
    "CartItem": {
      "type": "object",
      "description": "Cart Item entity - Child entity within Shopping Cart aggregate",
      "aggregateRoot": false,
      "boundedContext": "ShoppingCart",
      "parentAggregate": "Cart",
      "properties": {
        "cart_item_id": {
          "type": "integer",
          "format": "int64",
          "description": "Unique cart item identifier",
          "constraints": ["PRIMARY KEY", "AUTO_INCREMENT"],
          "minimum": 1
        },
        "cart_id": {
          "type": "integer",
          "format": "int64",
          "description": "Foreign key reference to Cart.cart_id",
          "constraints": ["FOREIGN KEY", "NOT NULL"],
          "references": {
            "entity": "Cart",
            "field": "cart_id",
            "onDelete": "CASCADE"
          },
          "indexed": true
        },
        "product_id": {
          "type": "integer",
          "format": "int64",
          "description": "Foreign key reference to Product.product_id",
          "constraints": ["FOREIGN KEY", "NOT NULL"],
          "references": {
            "entity": "Product",
            "field": "product_id",
            "onDelete": "RESTRICT"
          },
          "indexed": true
        },
        "quantity": {
          "type": "integer",
          "format": "int32",
          "description": "Quantity of product in cart",
          "minimum": 1,
          "constraints": ["NOT NULL", "CHECK > 0"],
          "validationRule": "quantity > 0"
        }
      },
      "required": ["cart_item_id", "cart_id", "product_id", "quantity"],
      "uniqueConstraints": [
        {
          "name": "uk_cart_product",
          "columns": ["cart_id", "product_id"],
          "description": "Prevent duplicate products in same cart"
        }
      ],
      "businessRules": [
        "Quantity must always be greater than 0",
        "If quantity becomes 0, item must be removed from cart"
      ]
    }
  },
  "relationships": [
    {
      "name": "User_Cart",
      "type": "ONE_TO_ONE",
      "from": {
        "entity": "User",
        "field": "username",
        "cardinality": "1"
      },
      "to": {
        "entity": "Cart",
        "field": "user_id",
        "cardinality": "0..1"
      },
      "description": "One user has at most one active cart",
      "bidirectional": true,
      "cascadeDelete": true
    },
    {
      "name": "Cart_User",
      "type": "MANY_TO_ONE",
      "from": {
        "entity": "Cart",
        "field": "user_id",
        "cardinality": "*"
      },
      "to": {
        "entity": "User",
        "field": "username",
        "cardinality": "1"
      },
      "description": "Each cart belongs to exactly one user",
      "bidirectional": true,
      "mandatory": true
    },
    {
      "name": "Cart_CartItem",
      "type": "ONE_TO_MANY",
      "from": {
        "entity": "Cart",
        "field": "cart_id",
        "cardinality": "1"
      },
      "to": {
        "entity": "CartItem",
        "field": "cart_id",
        "cardinality": "1..*"
      },
      "description": "Cart contains one or more cart items",
      "bidirectional": true,
      "cascadeDelete": true,
      "orphanRemoval": true
    },
    {
      "name": "CartItem_Cart",
      "type": "MANY_TO_ONE",
      "from": {
        "entity": "CartItem",
        "field": "cart_id",
        "cardinality": "*"
      },
      "to": {
        "entity": "Cart",
        "field": "cart_id",
        "cardinality": "1"
      },
      "description": "Each cart item belongs to exactly one cart",
      "bidirectional": true,
      "mandatory": true
    },
    {
      "name": "CartItem_Product",
      "type": "MANY_TO_ONE",
      "from": {
        "entity": "CartItem",
        "field": "product_id",
        "cardinality": "*"
      },
      "to": {
        "entity": "Product",
        "field": "product_id",
        "cardinality": "1"
      },
      "description": "Each cart item references exactly one product",
      "bidirectional": true,
      "mandatory": true
    },
    {
      "name": "Product_CartItem",
      "type": "ONE_TO_MANY",
      "from": {
        "entity": "Product",
        "field": "product_id",
        "cardinality": "1"
      },
      "to": {
        "entity": "CartItem",
        "field": "product_id",
        "cardinality": "*"
      },
      "description": "Product can be referenced in multiple cart items",
      "bidirectional": true,
      "cascadeDelete": false
    }
  ],
  "aggregates": [
    {
      "name": "UserManagement",
      "root": "User",
      "entities": ["User"],
      "description": "Manages user accounts and authentication"
    },
    {
      "name": "ProductCatalog",
      "root": "Product",
      "entities": ["Product"],
      "description": "Manages product information and availability"
    },
    {
      "name": "ShoppingCart",
      "root": "Cart",
      "entities": ["Cart", "CartItem"],
      "description": "Manages shopping cart and cart items as a consistency boundary"
    }
  ],
  "securityRequirements": {
    "authentication": "Stateless (JWT/OAuth2)",
    "sessionManagement": "No database sessions",
    "piiFields": ["User.username", "User.full_name", "User.email"],
    "encryptionAtRest": ["User.username", "User.full_name", "User.email"],
    "passwordHashing": {
      "algorithm": "bcrypt or PBKDF2",
      "minimumStrength": 10
    }
  },
  "complianceRequirements": {
    "frameworks": ["GDPR", "CCPA"],
    "dataRetention": {
      "auditLogs": "7 years",
      "cartData": "90 days after last activity",
      "userData": "Until account deletion + 30 days"
    },
    "rightToErasure": true,
    "dataPortability": true
  }
}
```

---

## OUTPUT 3: ENTITY-ATTRIBUTE-RELATIONSHIP TABLE

### 3.1 ENTITY DEFINITIONS

| Entity Name | Aggregate Root | Bounded Context | Primary Key | Description |
|-------------|----------------|-----------------|-------------|-------------|
| User | Yes | User Management | username (String) | Represents system users with authentication credentials and profile information |
| Product | Yes | Product Catalog | product_id (Long) | Represents products available for purchase in the catalog |
| Cart | Yes | Shopping Cart | cart_id (Long) | Represents a user's active shopping cart |
| CartItem | No (Child of Cart) | Shopping Cart | cart_item_id (Long) | Represents individual product entries within a shopping cart |

### 3.2 ATTRIBUTE SPECIFICATIONS

#### User Entity Attributes

| Attribute Name | Data Type | Constraints | Security Classification | Indexed | Description |
|----------------|-----------|-------------|------------------------|---------|-------------|
| username | String (VARCHAR 50) | PRIMARY KEY, UNIQUE, NOT NULL | PII - Encrypted | Yes | Unique username for authentication |
| password | String (VARCHAR 255) | NOT NULL | SENSITIVE - Hashed (bcrypt) | No | Hashed password, never returned in responses |
| full_name | String (VARCHAR 100) | NOT NULL | PII - Encrypted | No | User's full legal name |
| email | String (VARCHAR 255) | NOT NULL | PII - Encrypted | Yes | User's email address (GDPR/CCPA relevant) |
| created_date | LocalDateTime | NOT NULL, DEFAULT CURRENT_TIMESTAMP | None | No | Account creation timestamp (immutable) |

#### Product Entity Attributes

| Attribute Name | Data Type | Constraints | Security Classification | Indexed | Description |
|----------------|-----------|-------------|------------------------|---------|-------------|
| product_id | Long (BIGINT) | PRIMARY KEY, AUTO_INCREMENT | None | Yes | Unique product identifier |
| name | String (VARCHAR 200) | NOT NULL | None | Yes | Product name (case-insensitive search) |
| description | String (VARCHAR 2000) | NULLABLE | None | No | Detailed product description |
| price | BigDecimal (DECIMAL 10,2) | NOT NULL, CHECK > 0 | None | No | Product price (must be positive) |
| available_quantity | Integer (INT) | NOT NULL, CHECK >= 0, DEFAULT 0 | None | No | Current inventory quantity |

#### Cart Entity Attributes

| Attribute Name | Data Type | Constraints | Security Classification | Indexed | Description |
|----------------|-----------|-------------|------------------------|---------|-------------|
| cart_id | Long (BIGINT) | PRIMARY KEY, AUTO_INCREMENT | None | Yes | Unique cart identifier |
| user_id | String (VARCHAR 50) | FOREIGN KEY (User.username), UNIQUE, NOT NULL | None | Yes | Reference to owning user (one cart per user) |
| created_date | LocalDateTime | NOT NULL, DEFAULT CURRENT_TIMESTAMP | None | No | Cart creation timestamp (immutable) |

#### CartItem Entity Attributes

| Attribute Name | Data Type | Constraints | Security Classification | Indexed | Description |
|----------------|-----------|-------------|------------------------|---------|-------------|
| cart_item_id | Long (BIGINT) | PRIMARY KEY, AUTO_INCREMENT | None | Yes | Unique cart item identifier |
| cart_id | Long (BIGINT) | FOREIGN KEY (Cart.cart_id), NOT NULL | None | Yes | Reference to parent cart |
| product_id | Long (BIGINT) | FOREIGN KEY (Product.product_id), NOT NULL | None | Yes | Reference to product |
| quantity | Integer (INT) | NOT NULL, CHECK > 0 | None | No | Quantity of product (must be positive) |

### 3.3 RELATIONSHIP SPECIFICATIONS

| Relationship Name | From Entity | To Entity | Cardinality | Type | Cascade Behavior | Description |
|-------------------|-------------|-----------|-------------|------|------------------|-------------|
| User_Has_Cart | User | Cart | 1 : 0..1 | ONE-TO-ONE | CASCADE DELETE | One user has at most one active cart |
| Cart_Belongs_To_User | Cart | User | * : 1 | MANY-TO-ONE | RESTRICT | Each cart belongs to exactly one user |
| Cart_Contains_Items | Cart | CartItem | 1 : 1..* | ONE-TO-MANY | CASCADE DELETE, ORPHAN REMOVAL | Cart contains one or more items (business rule enforced) |
| CartItem_In_Cart | CartItem | Cart | * : 1 | MANY-TO-ONE | RESTRICT | Each cart item belongs to exactly one cart |
| CartItem_References_Product | CartItem | Product | * : 1 | MANY-TO-ONE | RESTRICT | Each cart item references exactly one product |
| Product_In_CartItems | Product | CartItem | 1 : * | ONE-TO-MANY | NO CASCADE | Product can appear in multiple cart items |

### 3.4 CONSTRAINTS AND VALIDATION RULES

| Constraint Type | Entity | Field(s) | Rule | Enforcement Level |
|-----------------|--------|----------|------|-------------------|
| UNIQUE | User | username | Username must be unique across all users | Database |
| CHECK | CartItem | quantity | quantity > 0 | Database |
| CHECK | Product | price | price > 0 | Database |
| CHECK | Product | available_quantity | available_quantity >= 0 | Database |
| FOREIGN KEY | Cart | user_id → User.username | Referential integrity | Database |
| FOREIGN KEY | CartItem | cart_id → Cart.cart_id | Referential integrity | Database |
| FOREIGN KEY | CartItem | product_id → Product.product_id | Referential integrity | Database |
| UNIQUE COMPOSITE | CartItem | (cart_id, product_id) | No duplicate products in same cart | Database |
| BUSINESS RULE | Cart | cartItems collection | Cart cannot exist without at least one item | Application |
| BUSINESS RULE | User | cart relationship | One active cart per user maximum | Application |
| BUSINESS RULE | Cart | lifecycle | Lazy creation on first item add | Application |
| BUSINESS RULE | Cart | lifecycle | Auto-delete when last item removed | Application |
| BUSINESS RULE | Cart | lifecycle | Mandatory deletion on user logout | Application |

### 3.5 INDEX SPECIFICATIONS

| Index Name | Entity | Column(s) | Type | Purpose |
|------------|--------|-----------|------|---------|