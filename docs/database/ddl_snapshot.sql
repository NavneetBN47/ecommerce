-- ============================================================================
-- E-COMMERCE SHOPPING CART SYSTEM - COMPLETE DDL SNAPSHOT
-- ============================================================================
-- Version: 1.0.0
-- Date: 2025-02-05
-- Description: Complete database schema definition with all tables, 
--              constraints, indexes, and triggers
-- ============================================================================

-- Drop existing tables (for clean installation)
DROP TABLE IF EXISTS cart_items CASCADE;
DROP TABLE IF EXISTS carts CASCADE;
DROP TABLE IF EXISTS products CASCADE;
DROP TABLE IF EXISTS categories CASCADE;
DROP TABLE IF EXISTS users CASCADE;

-- ============================================================================
-- USERS TABLE
-- ============================================================================
CREATE TABLE users (
    user_id BIGSERIAL PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    email VARCHAR(100) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    full_name VARCHAR(100),
    phone VARCHAR(20),
    address TEXT,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_user_email ON users(email);
CREATE INDEX idx_user_username ON users(username);

COMMENT ON TABLE users IS 'Stores registered user information';
COMMENT ON COLUMN users.password_hash IS 'BCrypt hashed password';

-- ============================================================================
-- CATEGORIES TABLE
-- ============================================================================
CREATE TABLE categories (
    category_id BIGSERIAL PRIMARY KEY,
    category_name VARCHAR(100) NOT NULL UNIQUE,
    description TEXT,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_category_name ON categories(category_name);

COMMENT ON TABLE categories IS 'Product categories for organization';

-- ============================================================================
-- PRODUCTS TABLE
-- ============================================================================
CREATE TABLE products (
    product_id BIGSERIAL PRIMARY KEY,
    product_name VARCHAR(200) NOT NULL,
    description TEXT,
    sku VARCHAR(50) UNIQUE,
    price DECIMAL(10, 2) NOT NULL CHECK (price >= 0),
    stock_quantity INTEGER NOT NULL DEFAULT 0 CHECK (stock_quantity >= 0),
    category_id BIGINT,
    image_url VARCHAR(500),
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_product_category FOREIGN KEY (category_id) 
        REFERENCES categories(category_id) ON DELETE SET NULL
);

CREATE INDEX idx_product_name ON products(product_name);
CREATE INDEX idx_product_category ON products(category_id);
CREATE INDEX idx_product_sku ON products(sku);

COMMENT ON TABLE products IS 'Product catalog with pricing and inventory';
COMMENT ON COLUMN products.stock_quantity IS 'Available inventory count';

-- ============================================================================
-- CARTS TABLE
-- ============================================================================
CREATE TABLE carts (
    cart_id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    total_amount DECIMAL(10, 2) DEFAULT 0.00,
    total_items INTEGER DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_cart_user FOREIGN KEY (user_id) 
        REFERENCES users(user_id) ON DELETE CASCADE
);

CREATE INDEX idx_cart_user ON carts(user_id);
CREATE INDEX idx_cart_status ON carts(status);

COMMENT ON TABLE carts IS 'Shopping carts for users';
COMMENT ON COLUMN carts.status IS 'Cart status: ACTIVE, CHECKED_OUT, ABANDONED';

-- ============================================================================
-- CART_ITEMS TABLE
-- ============================================================================
CREATE TABLE cart_items (
    cart_item_id BIGSERIAL PRIMARY KEY,
    cart_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    quantity INTEGER NOT NULL CHECK (quantity > 0),
    price DECIMAL(10, 2) NOT NULL CHECK (price >= 0),
    added_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_cart_item_cart FOREIGN KEY (cart_id) 
        REFERENCES carts(cart_id) ON DELETE CASCADE,
    CONSTRAINT fk_cart_item_product FOREIGN KEY (product_id) 
        REFERENCES products(product_id) ON DELETE CASCADE,
    CONSTRAINT uk_cart_product UNIQUE (cart_id, product_id)
);

CREATE INDEX idx_cart_item_cart ON cart_items(cart_id);
CREATE INDEX idx_cart_item_product ON cart_items(product_id);

COMMENT ON TABLE cart_items IS 'Individual items in shopping carts';
COMMENT ON COLUMN cart_items.price IS 'Price at the time of adding to cart';

-- ============================================================================
-- TRIGGERS FOR AUTOMATIC TIMESTAMP UPDATES
-- ============================================================================

CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER update_users_updated_at
    BEFORE UPDATE ON users
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_categories_updated_at
    BEFORE UPDATE ON categories
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_products_updated_at
    BEFORE UPDATE ON products
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_carts_updated_at
    BEFORE UPDATE ON carts
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_cart_items_updated_at
    BEFORE UPDATE ON cart_items
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

-- ============================================================================
-- END OF DDL SNAPSHOT
-- ============================================================================