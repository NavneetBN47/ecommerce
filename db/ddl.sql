-- SHOPPING CART BACKEND - DATA DEFINITION LANGUAGE (DDL)
-- Complete schema reflecting LLD specification as authoritative source
-- All tables, constraints, and relationships for User, Product, Cart, CartItem entities

-- USERS TABLE
-- Entity: User with id (PK, auto), username (unique, immutable), password (hashed), fullName, email, createdDate
CREATE TABLE IF NOT EXISTS users (
    user_id SERIAL PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    password VARCHAR(100) NOT NULL,
    full_name VARCHAR(100),
    email VARCHAR(100),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- PRODUCTS TABLE
-- Entity: Product with id (PK, auto), name, description, price, availableQuantity
CREATE TABLE IF NOT EXISTS products (
    product_id SERIAL PRIMARY KEY,
    product_name VARCHAR(100) NOT NULL,
    description VARCHAR(255),
    price DECIMAL(10,2) NOT NULL,
    available_qty INT NOT NULL CHECK (available_qty >= 0),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- CART TABLE
-- Entity: Cart with id (PK, auto), userId (FK → User), createdDate
-- Constraint: One active cart per user, belongs to one user
CREATE TABLE IF NOT EXISTS cart (
    cart_id SERIAL PRIMARY KEY,
    user_id INT UNIQUE NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_cart_user FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
);

-- CART ITEMS TABLE
-- Entity: CartItem with id (PK, auto), cartId (FK → Cart), productId (FK → Product), quantity
-- Constraint: Belongs to cart, product must exist, quantity > 0
CREATE TABLE IF NOT EXISTS cart_items (
    cart_item_id SERIAL PRIMARY KEY,
    cart_id INT NOT NULL,
    product_id INT NOT NULL,
    quantity INT NOT NULL CHECK (quantity > 0),
    added_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_cart_items_cart FOREIGN KEY (cart_id) REFERENCES cart(cart_id) ON DELETE CASCADE,
    CONSTRAINT fk_cart_items_product FOREIGN KEY (product_id) REFERENCES products(product_id),
    CONSTRAINT uk_cart_product UNIQUE (cart_id, product_id)
);

-- INDEXES for performance optimization
CREATE INDEX IF NOT EXISTS idx_users_username ON users(username);
CREATE INDEX IF NOT EXISTS idx_products_name ON products(product_name);
CREATE INDEX IF NOT EXISTS idx_cart_user ON cart(user_id);
CREATE INDEX IF NOT EXISTS idx_cart_items_cart ON cart_items(cart_id);
CREATE INDEX IF NOT EXISTS idx_cart_items_product ON cart_items(product_id);

-- COMMENTS for documentation
COMMENT ON TABLE users IS 'User management entity with unique username constraint';
COMMENT ON TABLE products IS 'Product catalog entity with immutable pricing';
COMMENT ON TABLE cart IS 'Shopping cart entity with one-to-one user relationship';
COMMENT ON TABLE cart_items IS 'Cart item entity with quantity validation and unique product per cart';
