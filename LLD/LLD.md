Low Level Design (LLD) for Spring Boot MVC Shopping Cart System

1. Executive Summary
This document details the Low Level Design (LLD) for a stateless Shopping Cart System built using Spring Boot MVC. The system is designed to provide RESTful APIs for user management, product catalog browsing, and shopping cart operations, ensuring scalability, maintainability, and security. The architecture leverages Spring Boot's MVC framework, JPA for persistence, and follows best practices for stateless session management.

2. Functional Domains
2.1 User Management
- Registration, authentication, and profile management
- Password encryption and validation
- Stateless JWT-based authentication

2.2 Product Catalog
- CRUD operations for products
- Product search and filtering
- Inventory tracking

2.3 Shopping Cart Management
- Add, update, remove items from cart
- View cart contents
- Stateless cart persistence (cart stored in DB, associated with user)

3. Domain Entities
3.1 User
- id: Long (PK, auto-generated)
- username: String (unique, not null, 3-50 chars)
- email: String (unique, not null, email format)
- password: String (hashed, not null)
- roles: Set<Role> (e.g., USER, ADMIN)
- createdAt: Timestamp
- updatedAt: Timestamp

3.2 Product
- id: Long (PK, auto-generated)
- name: String (not null, 3-100 chars)
- description: String (nullable, max 500 chars)
- price: BigDecimal (not null, >=0)
- stock: Integer (not null, >=0)
- createdAt: Timestamp
- updatedAt: Timestamp

3.3 Cart
- id: Long (PK, auto-generated)
- user: User (OneToOne, not null)
- items: List<CartItem> (OneToMany)
- createdAt: Timestamp
- updatedAt: Timestamp

3.4 CartItem
- id: Long (PK, auto-generated)
- cart: Cart (ManyToOne, not null)
- product: Product (ManyToOne, not null)
- quantity: Integer (not null, >=1)
- priceAtAddition: BigDecimal (not null)

4. Business Rules and Constraints
- Usernames and emails must be unique.
- Passwords must be hashed using BCrypt.
- Product stock cannot be negative.
- CartItem quantity must not exceed available product stock.
- Only authenticated users can manage their carts.
- Cart is cleared upon checkout.

5. REST API Contracts
5.1 User APIs
- POST /api/users/register
  - Request: {username, email, password}
  - Response: 201 Created / 400 Bad Request
- POST /api/users/login
  - Request: {username/email, password}
  - Response: 200 OK (JWT Token) / 401 Unauthorized
- GET /api/users/me
  - Auth: Bearer Token
  - Response: 200 OK (User profile)

5.2 Product APIs
- GET /api/products
  - Query: name, minPrice, maxPrice, page, size
  - Response: 200 OK (Paged products)
- GET /api/products/{id}
  - Response: 200 OK (Product) / 404 Not Found
- POST /api/products
  - Auth: ADMIN
  - Request: {name, description, price, stock}
  - Response: 201 Created / 400 Bad Request
- PUT /api/products/{id}
  - Auth: ADMIN
  - Request: {name, description, price, stock}
  - Response: 200 OK / 400 Bad Request / 404 Not Found
- DELETE /api/products/{id}
  - Auth: ADMIN
  - Response: 204 No Content / 404 Not Found

5.3 Cart APIs
- GET /api/cart
  - Auth: USER
  - Response: 200 OK (Cart details)
- POST /api/cart/items
  - Auth: USER
  - Request: {productId, quantity}
  - Response: 201 Created / 400 Bad Request / 409 Conflict
- PUT /api/cart/items/{itemId}
  - Auth: USER
  - Request: {quantity}
  - Response: 200 OK / 400 Bad Request / 404 Not Found
- DELETE /api/cart/items/{itemId}
  - Auth: USER
  - Response: 204 No Content / 404 Not Found
- POST /api/cart/checkout
  - Auth: USER
  - Response: 200 OK (Order confirmation) / 400 Bad Request

6. Validation Matrix
| Field         | Validation                               |
|---------------|------------------------------------------|
| username      | Unique, 3-50 chars, not null             |
| email         | Unique, valid email, not null             |
| password      | Min 8 chars, not null                    |
| product.name  | 3-100 chars, not null                    |
| product.price | >=0, not null                            |
| product.stock | >=0, not null                            |
| cartItem.qty  | >=1, <=product.stock, not null           |

7. Database Effects
- User registration inserts into users table.
- Product CRUD affects products table.
- Cart and CartItem tables track user carts and their contents.
- On checkout, cart items are removed and product stock is decremented.
- All operations are transactional to ensure consistency.

8. Deliverables
- Domain Model (UML class diagram)
- Sequence Diagrams for major flows (registration, add to cart, checkout)
- Low Level Design document (this file)

9. Implementation Guide
9.1 Project Structure
- com.example.shoppingcart
  - controller
  - service
  - repository
  - model (entities)
  - dto
  - security
  - exception
  - config

9.2 Security
- Use Spring Security with JWT for stateless authentication.
- Passwords stored as BCrypt hashes.
- Role-based access control for endpoints.

9.3 Persistence
- Spring Data JPA for ORM.
- Entities mapped as per section 3.
- Use @Transactional for service methods modifying data.

9.4 Service Layer Logic
- UserService: registration, authentication, profile
- ProductService: CRUD, search, filter
- CartService: add/update/remove item, view cart, checkout
- Validation and business rule enforcement in service layer

9.5 Exception Handling
- @ControllerAdvice for global exception mapping
- Custom exceptions: ResourceNotFound, ValidationException, ConflictException

9.6 DTOs and Mappers
- Use DTOs for API requests/responses
- MapStruct or manual mapping between entities and DTOs

9.7 API Documentation
- Swagger/OpenAPI for endpoint documentation

9.8 Testing
- Unit tests for services and controllers
- Integration tests for major flows
- MockMvc for API testing

10. Quality Assurance Report
- Code coverage >80% for services/controllers
- Static analysis (SonarQube/Checkstyle)
- Manual and automated API testing
- Security audit (OWASP Top 10)

11. Troubleshooting and Support
- Centralized logging (SLF4J/Logback)
- Exception trace IDs for error tracking
- API error responses with meaningful messages
- Monitoring endpoints (Spring Boot Actuator)

12. Future Considerations
- Order management and payment integration
- Multi-cart support (e.g., wishlists)
- Caching for product catalog
- Rate limiting and API quotas
- Internationalization (i18n)
- Microservices migration path