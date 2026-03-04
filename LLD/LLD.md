Low Level Design (LLD) for Shopping Cart Management Feature

1. Introduction
This document provides the Low Level Design (LLD) for the Shopping Cart Management feature as per Jira story SCRUM-343. The LLD covers domain entities, API contracts, validation rules, architectural diagrams, MVC mapping, business logic, security considerations, and test scenarios.

2. Domain Entities

2.1 Customer
- customerId: UUID
- name: String
- email: String
- passwordHash: String
- createdAt: Timestamp
- updatedAt: Timestamp

2.2 Product
- productId: UUID
- name: String
- description: String
- price: Decimal
- stockQuantity: Integer
- isActive: Boolean
- createdAt: Timestamp
- updatedAt: Timestamp

2.3 Cart
- cartId: UUID
- customerId: UUID (FK)
- createdAt: Timestamp
- updatedAt: Timestamp

2.4 CartItem
- cartItemId: UUID
- cartId: UUID (FK)
- productId: UUID (FK)
- quantity: Integer
- unitPrice: Decimal
- createdAt: Timestamp
- updatedAt: Timestamp

3. REST API Contracts

3.1 Add to Cart
- Endpoint: POST /api/v1/cart/items
- Request Body:
  {
    "productId": "UUID",
    "quantity": Integer
  }
- Response:
  201 Created
  {
    "cartItemId": "UUID",
    "cartId": "UUID",
    "productId": "UUID",
    "quantity": Integer,
    "unitPrice": Decimal
  }
- Error Codes:
  400, 401, 404, 409

3.2 View Cart
- Endpoint: GET /api/v1/cart
- Response:
  200 OK
  {
    "cartId": "UUID",
    "customerId": "UUID",
    "items": [
      {
        "cartItemId": "UUID",
        "productId": "UUID",
        "name": "String",
        "quantity": Integer,
        "unitPrice": Decimal,
        "totalPrice": Decimal
      }
    ],
    "totalAmount": Decimal
  }
- Error Codes:
  401, 404

3.3 Update Cart Item Quantity
- Endpoint: PATCH /api/v1/cart/items/{cartItemId}
- Request Body:
  {
    "quantity": Integer
  }
- Response:
  200 OK
  {
    "cartItemId": "UUID",
    "productId": "UUID",
    "quantity": Integer,
    "unitPrice": Decimal
  }
- Error Codes:
  400, 401, 404, 409

3.4 Remove Item from Cart
- Endpoint: DELETE /api/v1/cart/items/{cartItemId}
- Response:
  204 No Content
- Error Codes:
  401, 404

4. Validation Matrix & Error Codes

| Operation         | Validation Rule                                   | Error Code | Description                       |
|------------------|---------------------------------------------------|------------|-----------------------------------|
| Add to Cart      | Product exists and is active                      | 404        | Product not found                 |
|                  | Quantity > 0                                      | 400        | Invalid quantity                  |
|                  | Stock available                                   | 409        | Insufficient stock                |
|                  | Authenticated user                                | 401        | Unauthorized                      |
| View Cart        | Authenticated user                                | 401        | Unauthorized                      |
|                  | Cart exists for user                              | 404        | Cart not found                    |
| Update Quantity  | CartItem exists and belongs to user               | 404        | Cart item not found               |
|                  | Quantity > 0                                      | 400        | Invalid quantity                  |
|                  | Stock available                                   | 409        | Insufficient stock                |
|                  | Authenticated user                                | 401        | Unauthorized                      |
| Remove Item      | CartItem exists and belongs to user               | 404        | Cart item not found               |
|                  | Authenticated user                                | 401        | Unauthorized                      |

5. Architectural Diagrams

5.1 Class Diagram (Mermaid)
classDiagram
    Customer <|-- Cart
    Cart "1" o-- "*" CartItem
    CartItem "*" --> "1" Product
    class Customer {
        UUID customerId
        String name
        String email
        String passwordHash
        Timestamp createdAt
        Timestamp updatedAt
    }
    class Product {
        UUID productId
        String name
        String description
        Decimal price
        Integer stockQuantity
        Boolean isActive
        Timestamp createdAt
        Timestamp updatedAt
    }
    class Cart {
        UUID cartId
        UUID customerId
        Timestamp createdAt
        Timestamp updatedAt
    }
    class CartItem {
        UUID cartItemId
        UUID cartId
        UUID productId
        Integer quantity
        Decimal unitPrice
        Timestamp createdAt
        Timestamp updatedAt
    }

5.2 Sequence Diagram (Add to Cart)
sequenceDiagram
    participant User
    participant API
    participant AuthService
    participant CartService
    participant ProductService
    participant CartRepository
    participant CartItemRepository
    User->>API: POST /api/v1/cart/items
    API->>AuthService: Validate JWT
    AuthService-->>API: UserId
    API->>ProductService: Get Product(productId)
    ProductService-->>API: Product Details
    API->>CartService: Get or Create Cart(userId)
    CartService-->>API: Cart
    API->>CartItemRepository: Add CartItem(cartId, productId, quantity)
    CartItemRepository-->>API: CartItem
    API-->>User: 201 Created (CartItem)

6. MVC Mapping & Business Logic Sequencing

6.1 Controller (CartController)
- Receives HTTP requests, validates input, invokes service layer.
- Handles authentication via JWT middleware.

6.2 Service (CartService)
- addCartItem(userId, productId, quantity):
    1. Validate product exists and is active.
    2. Validate stock availability.
    3. Get or create cart for user.
    4. Add or update CartItem.
    5. Update cart updatedAt timestamp.
- getCart(userId):
    1. Retrieve cart and items for user.
    2. Calculate total amount.
- updateCartItemQuantity(userId, cartItemId, quantity):
    1. Validate cart item ownership.
    2. Validate quantity and stock.
    3. Update quantity.
    4. Update cart updatedAt timestamp.
- removeCartItem(userId, cartItemId):
    1. Validate cart item ownership.
    2. Remove cart item.
    3. Update cart updatedAt timestamp.

6.3 Repository
- CartRepository: CRUD operations for Cart.
- CartItemRepository: CRUD operations for CartItem.
- ProductRepository: Read operations for Product.

7. Security Considerations
- All endpoints require JWT authentication.
- Cart and CartItem operations are scoped to the authenticated user.
- Input validation and sanitization to prevent injection attacks.
- Rate limiting on cart modification endpoints to prevent abuse.
- Sensitive data (e.g., product price) is not accepted from client, always sourced from Product entity.
- Audit logging for cart modification events.

8. Test Scenarios

8.1 Add to Cart
- Add valid product with sufficient stock.
- Add product with insufficient stock (expect 409).
- Add inactive product (expect 404).
- Add with invalid quantity (expect 400).
- Add without authentication (expect 401).

8.2 View Cart
- View cart with items.
- View empty cart.
- View cart without authentication (expect 401).

8.3 Update Cart Item Quantity
- Update quantity to valid value.
- Update quantity to zero or negative (expect 400).
- Update quantity exceeding stock (expect 409).
- Update non-existent cart item (expect 404).
- Update without authentication (expect 401).

8.4 Remove Item from Cart
- Remove existing cart item.
- Remove non-existent cart item (expect 404).
- Remove without authentication (expect 401).

9. Conclusion
This LLD provides a comprehensive technical specification for the Shopping Cart Management feature, ensuring robust, secure, and maintainable implementation aligned with enterprise standards.