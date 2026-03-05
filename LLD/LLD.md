Low Level Design (LLD) Document for Shopping Cart System (Spring Boot MVC)

1. Introduction
This Low Level Design (LLD) document provides a detailed technical blueprint for implementing the Shopping Cart System backend using Java Spring Boot MVC. It translates the high-level engineering specification into concrete components, data models, service interfaces, validation logic, and REST API mappings. This LLD is intended for backend engineers and architects to ensure a consistent, secure, and maintainable implementation.

2. Architecture Overview
The Shopping Cart System is a stateless, RESTful backend application. It enforces strict business rules, statelessness, and data validation at both service and database layers. The system is composed of the following core modules:
- User Management
- Product Catalog
- Shopping Cart Management
- Security & Authentication
- Validation & Error Handling
- Persistence Layer (JPA/Hibernate)

2.1 Component Diagram (Textual Description)
- API Layer: Spring MVC Controllers expose REST endpoints for users, products, and cart operations.
- Service Layer: Business logic, validation, and domain rules enforcement.
- Persistence Layer: JPA repositories for User, Product, Cart, and CartItem entities.
- Security Layer: Stateless authentication (e.g., JWT or session token), password hashing.
- Database: Relational DB (e.g., PostgreSQL/MySQL) with schema enforcing constraints.

3. Data Model Design

3.1 Entity-Relationship Diagram (Textual Description)
- User (1) --- (1) Cart
- Cart (1) --- (N) CartItem
- CartItem (N) --- (1) Product

3.2 Entity Definitions

User
- id: UUID (PK, auto-generated)
- username: String (unique, immutable)
- password: String (hashed)
- fullName: String
- email: String (valid email)
- createdAt: DateTime (auto-generated)

Product
- id: UUID (PK, auto-generated)
- name: String (required)
- description: String (optional)
- price: Decimal (required, immutable)
- availableQty: Integer (>= 0)

Cart
- id: UUID (PK, auto-generated)
- userId: UUID (FK → User, unique)
- createdAt: DateTime (auto-generated)

CartItem
- id: UUID (PK, auto-generated)
- cartId: UUID (FK → Cart)
- productId: UUID (FK → Product)
- quantity: Integer (> 0)
- price: Decimal (snapshotted from Product)

3.3 Database Constraints
- User.username: UNIQUE, NOT NULL
- Product.price: NOT NULL, immutable after creation
- Cart.userId: UNIQUE, NOT NULL
- CartItem.quantity: > 0
- CartItem.price: NOT NULL
- Foreign keys with ON DELETE CASCADE for Cart and CartItem

4. API Layer Design

4.1 Controller Classes
- UserController: Handles /api/users endpoints
- ProductController: Handles /api/products endpoints
- CartController: Handles /api/cart endpoints

4.2 Endpoint Mapping
- POST /api/users/signup → UserController.signup()
- POST /api/users/signin → UserController.signin()
- GET /api/users/profile → UserController.getProfile()
- PUT /api/users/profile → UserController.updateProfile()
- GET /api/products/search → ProductController.searchProducts()
- POST /api/cart/items → CartController.addItem()
- PUT /api/cart/items/{itemId} → CartController.updateItem()
- DELETE /api/cart/items/{itemId} → CartController.removeItem()
- GET /api/cart → CartController.getCart()
- POST /api/cart/logout → CartController.logoutAndCleanup()

4.3 Request/Response DTOs
- UserSignupRequest, UserResponse, UserProfileUpdateRequest
- ProductResponse
- CartItemRequest, CartResponse, CartItemResponse

5. Service Layer Design

5.1 UserService
- signup(UserSignupRequest): UserResponse
- signin(UserSigninRequest): UserResponse
- getProfile(userId): UserResponse
- updateProfile(userId, UserProfileUpdateRequest): UserResponse

5.2 ProductService
- searchProducts(query): List<ProductResponse>

5.3 CartService
- addItem(userId, CartItemRequest): CartResponse
- updateItem(userId, itemId, CartItemUpdateRequest): CartResponse
- removeItem(userId, itemId): CartResponse or null
- getCart(userId): CartResponse
- logoutAndCleanup(userId): void

5.4 ValidationService
- validateUsernameUnique(username)
- validateEmail(email)
- validateProductExists(productId)
- validateQuantity(quantity)
- validateCartOwnership(userId, cartId)
- validateCartItemOwnership(userId, itemId)

6. Persistence Layer Design

6.1 JPA Entities
- UserEntity
- ProductEntity
- CartEntity
- CartItemEntity

6.2 Repository Interfaces
- UserRepository
- ProductRepository
- CartRepository
- CartItemRepository

6.3 Transactional Boundaries
- All cart operations are transactional (Spring @Transactional)
- Deleting a cart cascades to CartItems

7. Security & Authentication
- Stateless authentication (JWT or session token in HTTP header)
- Passwords hashed with bcrypt or Argon2
- User identity extracted from token for all authenticated endpoints
- No session state persisted in DB
- On logout, cart and cart items for user are deleted

8. Validation & Error Handling
- All input validated at controller and service layers
- Error codes and messages per Validation Matrix
- Exceptions mapped to HTTP status codes (e.g., 400, 401, 404, 409)
- Global exception handler for consistent error responses

9. Business Rule Enforcement
- One active cart per user; enforced at DB (unique constraint) and service
- Cart cannot exist without items; auto-delete empty cart
- Cart deleted on logout
- Product price immutable after creation
- CartItem price snapshotted at add/update
- All product and user references validated before operations

10. Cart Lifecycle Management
- Cart created lazily on first addItem
- Cart auto-deleted if last item removed
- Cart deleted on logout
- No cart persists across user logout or empty state

11. Implementation Guidelines
- Use Spring Boot 3.x, Java 17+
- Use Spring Data JPA for persistence
- Use Bean Validation (javax.validation) for DTOs
- Use Lombok for boilerplate reduction
- Use MapStruct for DTO mapping
- Use Spring Security for authentication
- Use @Transactional for service methods
- Write unit and integration tests for all modules
- Document all APIs with OpenAPI/Swagger

12. Non-Functional Requirements
- Stateless backend; no session state in DB
- Secure password storage
- Input validation and sanitization
- Consistent error handling
- Performance: Indexes on username, product name, cart userId
- Scalability: Horizontally scalable stateless services

13. Out-of-Scope Features
- No checkout, payments, inventory locking, admin, password reset/change, cart persistence across sessions

14. Sample Database Schema (DDL)

CREATE TABLE users (
  id UUID PRIMARY KEY,
  username VARCHAR(50) UNIQUE NOT NULL,
  password VARCHAR(255) NOT NULL,
  full_name VARCHAR(100) NOT NULL,
  email VARCHAR(100) NOT NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE products (
  id UUID PRIMARY KEY,
  name VARCHAR(100) NOT NULL,
  description TEXT,
  price DECIMAL(12,2) NOT NULL,
  available_qty INTEGER NOT NULL CHECK (available_qty >= 0)
);

CREATE TABLE carts (
  id UUID PRIMARY KEY,
  user_id UUID UNIQUE NOT NULL REFERENCES users(id) ON DELETE CASCADE,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE cart_items (
  id UUID PRIMARY KEY,
  cart_id UUID NOT NULL REFERENCES carts(id) ON DELETE CASCADE,
  product_id UUID NOT NULL REFERENCES products(id),
  quantity INTEGER NOT NULL CHECK (quantity > 0),
  price DECIMAL(12,2) NOT NULL
);

15. Validation Matrix
| Field | Rule | Layer | Error Code |
|-------|------|-------|------------|
| username | Unique, required, immutable | DB, Service | 409, 400 |
| password | Required, hashed | Service | 400 |
| email | Valid email, required | Service | 400 |
| fullName | Required | Service | 400 |
| productId | Exists | Service, DB | 400, 404 |
| quantity | > 0 | Service, DB | 400 |
| cartId | Exists, one per user | Service, DB | 404 |
| itemId | Exists | Service, DB | 404 |
| price | Immutable | DB | 400 |

16. Sequence Diagrams (Textual Description)

16.1 Add Item to Cart
User → CartController.addItem() → CartService.addItem() → CartRepository.findByUserId() → ProductRepository.findById() → CartItemRepository.save() → CartRepository.save() → CartResponse → User

16.2 Remove Item from Cart
User → CartController.removeItem() → CartService.removeItem() → CartItemRepository.deleteById() → If cart empty: CartRepository.delete() → CartResponse or 204 → User

16.3 Logout and Cart Cleanup
User → CartController.logoutAndCleanup() → CartService.logoutAndCleanup() → CartRepository.deleteByUserId() → 204 → User

17. Error Handling Strategy
- All errors returned as JSON with code, message, and details
- Validation errors: 400 Bad Request
- Auth errors: 401 Unauthorized
- Not found: 404 Not Found
- Conflict: 409 Conflict

18. Testing Guidelines
- Unit tests for all service and controller logic
- Integration tests for API and DB interactions
- Test all validation and error scenarios
- Mock external dependencies

19. Documentation & Deliverables
- This LLD document (to be committed in docs/LLD/shopping-cart-system-lld.md)
- OpenAPI/Swagger API documentation
- Entity and API diagrams (as above)
- Test cases and coverage reports

End of LLD Document.