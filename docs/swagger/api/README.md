# Ecommerce API Documentation

This directory contains the Swagger/OpenAPI specification for the Ecommerce API.

## Files

- `ecommerce_swagger.json` - Complete OpenAPI 3.0.3 specification for the E-commerce Shopping Cart Backend System

## API Overview

The Ecommerce API provides a complete backend system for an e-commerce shopping cart application with the following features:

### Authentication
- User registration and login
- JWT-based authentication
- Secure logout functionality

### User Management
- User profile retrieval and updates
- Secure user data handling

### Product Catalog
- Product listing with pagination and filtering
- Product details retrieval
- Admin-only product management (create, update, delete)

### Shopping Cart
- Add/remove items from cart
- Update item quantities
- Cart management and clearing

### Order Management
- Order creation from cart
- Order history and tracking
- Order cancellation

## Security

The API uses JWT Bearer token authentication for protected endpoints. Public endpoints include:
- User registration
- User login
- Product listing and details

## Usage

Import the `ecommerce_swagger.json` file into your preferred API documentation tool or client generator:

- Swagger UI
- Postman
- Insomnia
- OpenAPI Generator

## Validation

The specification includes comprehensive validation rules:
- Request body validation
- Parameter validation
- Response schema definitions
- Error response handling

## Version

- API Version: 1.0.0
- OpenAPI Version: 3.0.3