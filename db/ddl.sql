-- DDL Script for Shopping Cart System (SCRUM-96)
-- Generated from Low-Level Design specification
-- UUID-based schema with all constraints and relationships

-- Enable UUID extension if not already enabled
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- Users table with UUID primary key
CREATE TABLE IF NOT EXISTS users_v2 (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    username VARCHAR(255) NOT NULL UNIQUE, -- immutable and unique as per LLD
    password VARCHAR(255) NOT NULL, -- hashed password
    fullName VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL,
    createdDate TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Products table with UUID primary key
CREATE TABLE IF NOT EXISTS products_v2 (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    name VARCHAR(255) NOT NULL,
    description TEXT,
    price DECIMAL(10,2) NOT NULL CHECK (price >= 0),
    availableQuantity INTEGER NOT NULL CHECK (availableQuantity >= 0)
);

-- Cart table with UUID primary key and unique user constraint
CREATE TABLE IF NOT EXISTS carts_v2 (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    userId UUID NOT NULL UNIQUE, -- One active cart per user
    createdDate TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (userId) REFERENCES users_v2(id) ON DELETE CASCADE
);

-- CartItem table with UUID primary key and constraints
CREATE TABLE IF NOT EXISTS cart_items_v2 (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    cartId UUID NOT NULL,
    productId UUID NOT NULL,
    quantity INTEGER NOT NULL CHECK (quantity > 0), -- quantity must be > 0
    FOREIGN KEY (cartId) REFERENCES carts_v2(id) ON DELETE CASCADE, -- cascade delete
    FOREIGN KEY (productId) REFERENCES products_v2(id) ON DELETE RESTRICT,
    UNIQUE(cartId, productId) -- prevent duplicate products in same cart
);

-- Indexes for performance
CREATE INDEX IF NOT EXISTS idx_users_v2_username ON users_v2(username);
CREATE INDEX IF NOT EXISTS idx_users_v2_email ON users_v2(email);
CREATE INDEX IF NOT EXISTS idx_carts_v2_userId ON carts_v2(userId);
CREATE INDEX IF NOT EXISTS idx_cart_items_v2_cartId ON cart_items_v2(cartId);
CREATE INDEX IF NOT EXISTS idx_cart_items_v2_productId ON cart_items_v2(productId);

-- Function to auto-delete empty carts
CREATE OR REPLACE FUNCTION delete_empty_carts()
RETURNS TRIGGER AS $$
BEGIN
    -- Delete cart if no items remain
    DELETE FROM carts_v2 
    WHERE id = OLD.cartId 
    AND NOT EXISTS (
        SELECT 1 FROM cart_items_v2 
        WHERE cartId = OLD.cartId
    );
    RETURN OLD;
END;
$$ LANGUAGE plpgsql;

-- Trigger to auto-delete empty carts when cart items are removed
CREATE TRIGGER IF NOT EXISTS trigger_delete_empty_carts
    AFTER DELETE ON cart_items_v2
    FOR EACH ROW
    EXECUTE FUNCTION delete_empty_carts();

-- Function to prevent cart creation for seed users (if needed)
CREATE OR REPLACE FUNCTION prevent_seed_user_carts()
RETURNS TRIGGER AS $$
BEGIN
    -- Add logic here if seed users need to be identified
    -- For now, allowing all users to have carts
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Comments for documentation
COMMENT ON TABLE users_v2 IS 'User accounts with immutable usernames';
COMMENT ON TABLE products_v2 IS 'Product catalog with inventory tracking';
COMMENT ON TABLE carts_v2 IS 'Shopping carts - one per user, auto-deleted when empty';
COMMENT ON TABLE cart_items_v2 IS 'Cart items with cascade delete from parent cart';
COMMENT ON COLUMN users_v2.username IS 'Immutable unique identifier for user';
COMMENT ON COLUMN carts_v2.userId IS 'One-to-one relationship with user';
COMMENT ON COLUMN cart_items_v2.quantity IS 'Must be greater than 0';
