# COMPREHENSIVE BACKEND ENGINEERING SPECIFICATION
## Shopping Cart System - SCRUM-96
### "Implement Core Shopping Cart Backend Services Using Spring Boot MVC"

---

## EXECUTIVE SUMMARY

This document provides a complete backend engineering specification for implementing a shopping cart system using Java Spring Boot MVC architecture. The specification is derived from Jira story SCRUM-96 and includes all necessary artifacts for production-ready implementation.

**Project Scope:** Core backend services for user management, product search, and shopping cart operations with strict business rules and database-first validation.

**Key Highlights:**
- 11 RESTful API endpoints across 3 functional domains
- 4 core domain entities with complete attribute specifications
- 47 validation rules mapped across MVC layers
- Stateless authentication with session-independent cart lifecycle
- Auto-cleanup mechanisms for cart management
- Database-enforced constraints and business rules

**Technology Stack:** Java Spring Boot (MVC), RESTful APIs, Relational Database (JPA/Hibernate)

**Out of Scope:** Checkout, Payments, Inventory locking, Admin management, Password changes

---

## DETAILED ANALYSIS

### 1. FUNCTIONAL DOMAIN BREAKDOWN

#### 1.1 User Management Domain
**Purpose:** Handle user registration, authentication, and profile management

**Functional Requirements:**
- User registration with unique username constraint
- Stateless authentication (no session persistence at DB level)
- Profile viewing and updating (excluding password changes)
- Username immutability after creation

**Business Rules:**
- Username must be unique across all users
- User must exist before any cart operation
- Email format validation required
- Full name and email are updatable; username is not

#### 1.2 Product Catalog Domain
**Purpose:** Enable product discovery through search functionality

**Functional Requirements:**
- Keyword-based product search
- Case-insensitive search implementation
- Return product details: name, description, price, available quantity

**Business Rules:**
- Product must exist to be searchable
- Price is read-only from user perspective
- Search returns only existing products

#### 1.3 Shopping Cart Management Domain
**Purpose:** Manage user shopping cart lifecycle and operations

**Functional Requirements:**
- Lazy cart creation (only on first product addition)
- Add products to cart with quantity specification
- Update cart item quantities
- Remove individual products from cart
- View complete cart with totals
- Auto-delete empty carts
- Cart cleanup on user logout

**Business Rules:**
- One active cart per user maximum
- Cart cannot exist without at least one cart item
- Quantity must always be greater than zero
- Cart must not persist across logout sessions
- Empty cart triggers automatic deletion
- Cart items deleted before cart record deletion

---

### 2. DOMAIN ENTITIES AND ATTRIBUTES

#### 2.1 User Entity

```java
Entity: User
Table: users

Attributes:
- userId: Long (Primary Key, Auto-generated)
- username: String (Unique, Not Null, Immutable)
- password: String (Not Null, Encrypted)
- fullName: String (Not Null, Updatable)
- email: String (Not Null, Updatable, Email Format)
- createdDate: Timestamp (Not Null, Auto-generated)

Constraints:
- UNIQUE(username)
- NOT NULL on all fields
- Email format validation

Relationships:
- One-to-One with Cart (optional, user may not have cart)
```

#### 2.2 Product Entity

```java
Entity: Product
Table: products

Attributes:
- productId: Long (Primary Key, Auto-generated)
- name: String (Not Null)
- description: String (Nullable)
- price: BigDecimal (Not Null, Precision 10, Scale 2)
- availableQuantity: Integer (Not Null, >= 0)

Constraints:
- NOT NULL on productId, name, price, availableQuantity
- price >= 0
- availableQuantity >= 0

Relationships:
- One-to-Many with CartItem
```

#### 2.3 Cart Entity

```java
Entity: Cart
Table: carts

Attributes:
- cartId: Long (Primary Key, Auto-generated)
- userId: Long (Foreign Key to User, Unique, Not Null)
- createdDate: Timestamp (Not Null, Auto-generated)
- lastModifiedDate: Timestamp (Not Null, Auto-updated)

Constraints:
- UNIQUE(userId) - One cart per user
- FOREIGN KEY(userId) REFERENCES users(userId) ON DELETE CASCADE
- NOT NULL on all fields

Relationships:
- Many-to-One with User (mandatory)
- One-to-Many with CartItem (mandatory, at least one)
```

#### 2.4 CartItem Entity

```java
Entity: CartItem
Table: cart_items

Attributes:
- cartItemId: Long (Primary Key, Auto-generated)
- cartId: Long (Foreign Key to Cart, Not Null)
- productId: Long (Foreign Key to Product, Not Null)
- quantity: Integer (Not Null, > 0)
- addedDate: Timestamp (Not Null, Auto-generated)

Constraints:
- FOREIGN KEY(cartId) REFERENCES carts(cartId) ON DELETE CASCADE
- FOREIGN KEY(productId) REFERENCES products(productId)
- UNIQUE(cartId, productId) - One product per cart
- quantity > 0
- NOT NULL on all fields

Relationships:
- Many-to-One with Cart (mandatory)
- Many-to-One with Product (mandatory)
```

---

### 3. REST API CONTRACTS

#### 3.1 User Management APIs

##### API 1: User Sign-Up
```
Endpoint: POST /api/users/signup
Description: Register a new user account

Request Body:
{
  "username": "string (required, 3-50 chars, alphanumeric + underscore)",
  "password": "string (required, min 8 chars)",
  "fullName": "string (required, 1-100 chars)",
  "email": "string (required, valid email format)"
}

Success Response: 201 Created
{
  "userId": "long",
  "username": "string",
  "fullName": "string",
  "email": "string",
  "createdDate": "timestamp"
}

Error Responses:
- 400 Bad Request: Invalid input format or validation failure
  {
    "error": "VALIDATION_ERROR",
    "message": "Username already exists",
    "field": "username"
  }
- 409 Conflict: Username already exists
  {
    "error": "DUPLICATE_USERNAME",
    "message": "Username 'john_doe' is already taken"
  }

Validations:
- Username: required, unique, 3-50 chars, alphanumeric + underscore
- Password: required, min 8 chars
- Full Name: required, 1-100 chars
- Email: required, valid email format

Database Effects:
- INSERT into users table
- Auto-generate userId and createdDate
- Encrypt password before storage
```