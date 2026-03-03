# Low Level Design (LLD) – Shopping Cart Management System

## 1. Domain Modeling

### 1.1 Entities
- **User**: user_id, name, email, password_hash, address, created_at, updated_at
- **Product**: product_id, name, description, price, stock_quantity, category, created_at, updated_at
- **Cart**: cart_id, user_id, status (active/checked_out/abandoned), created_at, updated_at
- **CartItem**: cart_item_id, cart_id, product_id, quantity, price_at_addition, created_at, updated_at
- **Order**: order_id, user_id, cart_id, total_amount, status (pending/paid/shipped/delivered/cancelled), created_at, updated_at

### 1.2 Relationships
- User 1:N Cart
- Cart 1:N CartItem
- Product 1:N CartItem
- Cart 1:1 Order

---

## 2. REST API Contracts

### 2.1 Add Item to Cart
- **POST /api/carts/{cart_id}/items**
- Request Body:
  ```json
  {
    "product_id": "string",
    "quantity": integer
  }
  ```
- Response:
  - 201 Created
  - 400 Bad Request (invalid product/quantity)
  - 404 Not Found (cart or product not found)
  - 409 Conflict (insufficient stock)

### 2.2 Remove Item from Cart
- **DELETE /api/carts/{cart_id}/items/{cart_item_id}**
- Response:
  - 200 OK
  - 404 Not Found (cart or item not found)

### 2.3 Update Item Quantity
- **PATCH /api/carts/{cart_id}/items/{cart_item_id}**
- Request Body:
  ```json
  {
    "quantity": integer
  }
  ```
- Response:
  - 200 OK
  - 400 Bad Request (invalid quantity)
  - 404 Not Found (cart or item not found)
  - 409 Conflict (insufficient stock)

### 2.4 Get Cart
- **GET /api/carts/{cart_id}**
- Response:
  - 200 OK (cart details, items, total)
  - 404 Not Found

### 2.5 Checkout Cart
- **POST /api/carts/{cart_id}/checkout**
- Response:
  - 200 OK (order details)
  - 400 Bad Request (cart empty or invalid)
  - 404 Not Found (cart not found)
  - 409 Conflict (stock issues)

---

## 3. Validation Matrices

| Field         | Validation Rules                                       |
|---------------|--------------------------------------------------------|
| product_id    | Must exist, UUID format                                |
| quantity      | Integer, >0, <= product stock                          |
| cart_id       | Must exist, UUID format                                |
| cart_item_id  | Must exist, UUID format, belongs to cart               |
| user_id       | Must exist, UUID format                                |

Error codes and messages are standardized per API guidelines.

---

## 4. Business Logic

- **Add Item**: Validate cart and product existence, check stock, add or update CartItem, recalculate cart total.
- **Remove Item**: Validate cart and item, remove CartItem, recalculate cart total.
- **Update Quantity**: Validate cart, item, and stock, update quantity, recalculate cart total.
- **Checkout**: Validate cart (not empty, active), lock stock, create order, mark cart as checked_out, deduct stock, handle payment.
- **Concurrency**: Use optimistic locking on stock and cart rows to prevent race conditions.

---

## 5. Diagrams

### 5.1 Entity Relationship Diagram (ERD)

[ERD Diagram Placeholder: User --< Cart --< CartItem >-- Product, Cart -- Order]

### 5.2 Sequence Diagram – Add Item to Cart

1. Client → API: POST /api/carts/{cart_id}/items
2. API → DB: Validate cart, product, stock
3. API → DB: Insert/Update CartItem
4. API → DB: Update cart total
5. API → Client: 201 Created

---

## 6. Implementation Guidance

- Use RESTful conventions, idempotent operations where possible.
- Leverage ORM for entity relationships.
- Use transactions for multi-step operations (e.g., checkout).
- Implement input validation at controller and service layers.
- Use JWT or OAuth2 for authentication.
- Log all cart modifications with user and timestamp.
- Use pagination for cart item listings if large.

---

## 7. QA & Testing

- Unit tests for all service methods (add, remove, update, checkout).
- Integration tests for API endpoints.
- Load testing for concurrent cart modifications.
- Test cases for edge conditions (out-of-stock, invalid cart, etc).
- Security testing (auth, authorization, input validation).

---

## 8. Troubleshooting

- Log all failed cart operations with error codes and stack traces.
- Monitor stock levels and cart status transitions.
- Provide clear error messages to clients.
- Use alerts for repeated stock conflicts or checkout failures.

---

## 9. Future Considerations

- Support for guest carts (non-logged-in users).
- Multi-currency and localization support.
- Promotions and coupon codes integration.
- Cart expiration and recovery mechanisms.
- Event-driven architecture for stock updates and notifications.