# E-commerce Shopping Cart API Documentation

## Overview
This document provides comprehensive API documentation for the E-commerce Shopping Cart Backend System.

## Base URL
```
http://localhost:8080/api
```

## Authentication
All protected endpoints require a Bearer token in the Authorization header:
```
Authorization: Bearer <token>
```

## User APIs

### 1. Register User
**Endpoint:** `POST /api/users/register`

**Request Body:**
```json
{
  "username": "john_doe",
  "password": "password123",
  "fullName": "John Doe",
  "email": "john@example.com"
}
```

**Response:** `201 Created`
```json
{
  "userId": 1,
  "username": "john_doe",
  "fullName": "John Doe",
  "email": "john@example.com",
  "createdAt": "2024-01-01T10:00:00"
}
```

### 2. Login
**Endpoint:** `POST /api/users/login`

**Request Body:**
```json
{
  "username": "john_doe",
  "password": "password123"
}
```

**Response:** `200 OK`
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "userId": 1,
  "username": "john_doe"
}
```

### 3. Logout
**Endpoint:** `POST /api/users/logout`

**Headers:** `Authorization: Bearer <token>`

**Response:** `204 No Content`

**Note:** Clears user's cart on logout.

### 4. Get User Profile
**Endpoint:** `GET /api/users/profile`

**Headers:** `Authorization: Bearer <token>`

**Response:** `200 OK`
```json
{
  "userId": 1,
  "username": "john_doe",
  "fullName": "John Doe",
  "email": "john@example.com",
  "createdAt": "2024-01-01T10:00:00"
}
```

### 5. Update User Profile
**Endpoint:** `PATCH /api/users/profile`

**Headers:** `Authorization: Bearer <token>`

**Request Body:**
```json
{
  "fullName": "John Updated",
  "email": "john.updated@example.com"
}
```

**Response:** `200 OK`

## Product APIs

### 1. Search Products
**Endpoint:** `GET /api/products`

**Query Parameters:**
- `search` (optional): Search term for product name
- `page` (optional): Page number (0-indexed)
- `size` (optional): Page size

**Example:** `GET /api/products?search=laptop&page=0&size=10`

**Response:** `200 OK`
```json
[
  {
    "productId": 1,
    "productName": "Laptop",
    "description": "High performance laptop",
    "price": 1200.00,
    "availableQty": 10,
    "isActive": true
  }
]
```

### 2. Get Product by ID
**Endpoint:** `GET /api/products/{id}`

**Response:** `200 OK`
```json
{
  "productId": 1,
  "productName": "Laptop",
  "description": "High performance laptop",
  "price": 1200.00,
  "availableQty": 10,
  "isActive": true
}
```

## Cart APIs

### 1. Get Cart
**Endpoint:** `GET /api/cart`

**Headers:** `Authorization: Bearer <token>`

**Response:** `200 OK`
```json
{
  "cartId": 1,
  "userId": 1,
  "items": [
    {
      "cartItemId": 1,
      "productId": 1,
      "productName": "Laptop",
      "price": 1200.00,
      "quantity": 2,
      "itemTotal": 2400.00
    }
  ],
  "cartTotal": 2400.00,
  "itemCount": 1
}
```

### 2. Add Item to Cart
**Endpoint:** `POST /api/cart/items`

**Headers:** `Authorization: Bearer <token>`

**Request Body:**
```json
{
  "productId": 1,
  "quantity": 2
}
```

**Response:** `201 Created`
```json
{
  "cartItemId": 1,
  "productId": 1,
  "productName": "Laptop",
  "price": 1200.00,
  "quantity": 2,
  "itemTotal": 2400.00
}
```

**Business Rules:**
- Cart is created lazily on first item addition
- If item already exists, quantity is incremented
- Stock availability is validated
- Product must be active

### 3. Update Cart Item
**Endpoint:** `PATCH /api/cart/items/{id}`

**Headers:** `Authorization: Bearer <token>`

**Request Body:**
```json
{
  "quantity": 3
}
```

**Response:** `200 OK`

**Business Rules:**
- If quantity is 0, item is removed
- Stock availability is validated
- Cart is deleted if last item is removed

### 4. Remove Cart Item
**Endpoint:** `DELETE /api/cart/items/{id}`

**Headers:** `Authorization: Bearer <token>`

**Response:** `204 No Content`

**Business Rules:**
- Cart is automatically deleted if last item is removed

## Error Responses

### 400 Bad Request
```json
{
  "field": "error message"
}
```

### 401 Unauthorized
```json
{
  "status": 401,
  "message": "Invalid token",
  "timestamp": "2024-01-01T10:00:00"
}
```

### 404 Not Found
```json
{
  "status": 404,
  "message": "Resource not found",
  "timestamp": "2024-01-01T10:00:00"
}
```

### 409 Conflict
```json
{
  "status": 409,
  "message": "Insufficient stock for product",
  "timestamp": "2024-01-01T10:00:00"
}
```

### 500 Internal Server Error
```json
{
  "status": 500,
  "message": "An unexpected error occurred",
  "timestamp": "2024-01-01T10:00:00"
}
```

## Business Logic

### Cart Lifecycle
1. **Lazy Creation**: Cart is created only when first product is added
2. **Auto-Delete**: Cart is automatically deleted when last item is removed
3. **Logout Cleanup**: Cart is cleared on user logout

### Stock Management
- All cart operations validate product stock availability
- Quantity updates check against current stock levels
- Products must be active to be added to cart

### Price Tracking
- Product price is captured at time of addition (`price_at_addition`)
- Cart totals use captured price, not current product price

## Testing

### Sample Test Flow
```bash
# 1. Register user
curl -X POST http://localhost:8080/api/users/register \
  -H "Content-Type: application/json" \
  -d '{"username":"test","password":"password123","fullName":"Test User","email":"test@example.com"}'

# 2. Login
curl -X POST http://localhost:8080/api/users/login \
  -H "Content-Type: application/json" \
  -d '{"username":"test","password":"password123"}'

# 3. Search products
curl http://localhost:8080/api/products?search=laptop

# 4. Add to cart
curl -X POST http://localhost:8080/api/cart/items \
  -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json" \
  -d '{"productId":1,"quantity":2}'

# 5. View cart
curl http://localhost:8080/api/cart \
  -H "Authorization: Bearer <token>"

# 6. Logout
curl -X POST http://localhost:8080/api/users/logout \
  -H "Authorization: Bearer <token>"
```
