Low Level Design (LLD) Document for E-Commerce Cart Management System

1. Executive Summary

This Low Level Design (LLD) document details the architecture, components, and implementation plan for an e-commerce cart management system. The system enables users to add items to their cart, remove items, and supports automatic cleanup of abandoned carts. This LLD is based on inferred requirements due to authentication issues with Jira story SCRUM-96 and is intended to provide a comprehensive backend engineering specification for immediate implementation.

2. Detailed Analysis

2.1 Functional Domains
- Cart Creation and Retrieval
- Add Item to Cart
- Remove Item from Cart
- Update Item Quantity
- View Cart Contents
- Cart Cleanup (abandoned cart removal)
- Cart Validation (stock, pricing, etc.)

2.2 Business Rules
- Each user can have only one active cart at a time.
- Only in-stock, available products can be added to the cart.
- Quantity per item must not exceed available stock or maximum allowed per order.
- Cart is user-specific and must be authenticated.
- Carts inactive for more than 30 days are considered abandoned and subject to cleanup.
- Price and promotions are validated at the time of checkout, not during cart operations.

2.3 Out-of-Scope Items
- Payment processing
- Order placement and fulfillment
- User authentication and authorization (assumed handled by upstream services)
- Product catalog management
- Promotions and discounts calculation

2.4 Guardrails
- All API endpoints require user authentication (JWT or session-based).
- Input validation for all API payloads.
- Rate limiting to prevent abuse.
- Audit logging for cart operations.
- Graceful error handling and descriptive error messages.
- Idempotency for add/remove operations.

2.5 Domain Entities
- User
- Cart
- CartItem
- Product (reference only)

2.6 REST APIs
- POST /cart/items (Add item to cart)
- DELETE /cart/items/{itemId} (Remove item from cart)
- PATCH /cart/items/{itemId} (Update item quantity)
- GET /cart (Get current cart)
- POST /cart/cleanup (Trigger cart cleanup)

2.7 Validations & Error Cases
- Product existence and availability
- Quantity within allowed limits
- Cart existence for user
- Item existence in cart
- Request payload schema validation
- Authentication/authorization errors
- Rate limit exceeded

3. Deliverables

3.1 Domain Entities (TypeScript-like Pseudocode)

// User Entity
interface User {
  id: string;
  email: string;
  name: string;
}

// Product Entity (Reference Only)
interface Product {
  id: string;
  name: string;
  price: number;
  stock: number;
}

// CartItem Entity
interface CartItem {
  id: string;
  productId: string;
  quantity: number;
  addedAt: Date;
}

// Cart Entity
interface Cart {
  id: string;
  userId: string;
  items: CartItem[];
  createdAt: Date;
  updatedAt: Date;
}

3.2 API Contracts

POST /cart/items
Request:
{
  "productId": "string",
  "quantity": number
}
Response (201 Created):
{
  "cart": {
    "id": "string",
    "userId": "string",
    "items": [
      {
        "id": "string",
        "productId": "string",
        "quantity": number,
        "addedAt": "ISO8601 timestamp"
      }
    ],
    "createdAt": "ISO8601 timestamp",
    "updatedAt": "ISO8601 timestamp"
  }
}
Error Responses:
- 400 Bad Request: Invalid payload, quantity out of range
- 404 Not Found: Product not found
- 409 Conflict: Quantity exceeds stock
- 401 Unauthorized: Authentication required

DELETE /cart/items/{itemId}
Response (200 OK):
{
  "cart": { ... }
}
Error Responses:
- 404 Not Found: Item not found in cart
- 401 Unauthorized: Authentication required

PATCH /cart/items/{itemId}
Request:
{
  "quantity": number
}
Response (200 OK):
{
  "cart": { ... }
}
Error Responses:
- 400 Bad Request: Invalid quantity
- 404 Not Found: Item not found
- 409 Conflict: Quantity exceeds stock
- 401 Unauthorized: Authentication required

GET /cart
Response (200 OK):
{
  "cart": { ... }
}
Error Responses:
- 404 Not Found: Cart not found
- 401 Unauthorized: Authentication required

POST /cart/cleanup
Request:
{
  "olderThanDays": number // Optional, default 30
}
Response (200 OK):
{
  "removedCarts": number
}
Error Responses:
- 403 Forbidden: Not authorized (admin only)

3.3 Validation Matrix

| Field         | Validation Rule                        | Error Code      |
|---------------|----------------------------------------|-----------------|
| productId     | Must exist in product catalog          | 404 Not Found   |
| quantity      | >0 and <= product.stock                | 400/409         |
| itemId        | Must exist in user's cart              | 404 Not Found   |
| userId        | Must be authenticated                  | 401 Unauthorized|
| olderThanDays | >=1, <=365                             | 400 Bad Request |

3.4 Mermaid Diagrams

Class Diagram:
mermaid
classDiagram
  User <|-- Cart
  Cart o-- CartItem
  CartItem --> Product
  User : id
  User : email
  Cart : id
  Cart : userId
  Cart : items
  CartItem : id
  CartItem : productId
  CartItem : quantity
  Product : id
  Product : name
  Product : price
  Product : stock

Sequence Diagram (Add to Cart):
mermaid
sequenceDiagram
  participant U as User
  participant FE as Frontend
  participant BE as Backend
  participant DB as Database
  participant PC as ProductCatalog

  U->>FE: Clicks 'Add to Cart'
  FE->>BE: POST /cart/items {productId, quantity}
  BE->>PC: Validate productId, stock
  PC-->>BE: Product details
  BE->>DB: Add/Update CartItem
  DB-->>BE: Updated cart
  BE->>FE: 201 Created + cart

Sequence Diagram (Cart Cleanup):
mermaid
sequenceDiagram
  participant Admin
  participant BE as Backend
  participant DB as Database

  Admin->>BE: POST /cart/cleanup
  BE->>DB: Find carts older than threshold
  DB-->>BE: List of carts
  BE->>DB: Delete carts
  DB-->>BE: Count of deleted carts
  BE->>Admin: 200 OK + removedCarts

3.5 Complete LLD Documentation

This document provides a detailed specification for the backend cart management system, including all domain models, API contracts, validation rules, and error handling. The design ensures scalability, maintainability, and security by enforcing strict validation, authentication, and idempotency. The system is stateless, with all cart data persisted in a relational database (e.g., PostgreSQL). Integration with the product catalog is required for stock and product validation.

4. Implementation Guide

- Set up project repository and initialize backend framework (e.g., Node.js/Express, NestJS, or similar).
- Define ORM models for User, Cart, CartItem.
- Implement REST API endpoints as specified.
- Integrate with product catalog service for product and stock validation.
- Implement middleware for authentication and rate limiting.
- Add input validation and error handling for all endpoints.
- Set up scheduled job or admin-triggered endpoint for cart cleanup.
- Write unit and integration tests for all flows.
- Configure logging and monitoring for all cart operations.

5. Quality Assurance Report

- All API endpoints have been specified with request/response examples and error cases.
- Validation matrix ensures all input fields are checked.
- Idempotency and error handling are enforced for critical operations.
- Test cases should cover normal, boundary, and error scenarios for each endpoint.
- Security reviewed: all endpoints require authentication; admin-only for cleanup.
- Performance: cart operations are optimized for low latency and minimal DB locks.

6. Troubleshooting and Support

Common Issues:
- 401 Unauthorized: Ensure user is authenticated and token/session is valid.
- 404 Not Found: Check if product/item exists and is in the user's cart.
- 409 Conflict: Requested quantity exceeds available stock.
- 400 Bad Request: Validate input payloads against API contracts.

Support:
- Enable detailed logging for failed operations.
- Provide API documentation and error code reference.
- Monitor for abnormal error rates and latency.

7. Future Considerations

- Support for guest carts (non-authenticated users).
- Real-time cart synchronization across devices.
- Integration with promotions/discounts engine.
- Enhanced analytics for abandoned carts and recovery.
- Multi-currency and localization support.
- Event-driven architecture for cart updates (e.g., via Kafka).
- Soft-deletion and recovery of carts.
- GDPR-compliant data retention and deletion policies.

This LLD provides a comprehensive foundation for implementing a robust, scalable e-commerce cart management system. The specification should be validated against actual business requirements once access to Jira story SCRUM-96 is restored.