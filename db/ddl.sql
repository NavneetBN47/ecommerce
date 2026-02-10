-- Database Schema Definition (DDL)
-- Cart Management System - Additive Schema Changes
-- Generated from Low-Level Design (LLD) as authoritative source

-- Create extension for UUID generation if not exists
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- Users table (referenced by Cart)
CREATE TABLE IF NOT EXISTS users (
    user_id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    username VARCHAR(255) UNIQUE NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    first_name VARCHAR(255),
    last_name VARCHAR(255),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    is_active BOOLEAN DEFAULT true
);

-- Products table (referenced by CartItem)
CREATE TABLE IF NOT EXISTS products (
    product_id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    product_name VARCHAR(255) NOT NULL,
    product_description TEXT,
    price DECIMAL(10,2) NOT NULL CHECK (price >= 0),
    stock_quantity INTEGER NOT NULL DEFAULT 0 CHECK (stock_quantity >= 0),
    category_id UUID,
    sku VARCHAR(100) UNIQUE,
    is_active BOOLEAN DEFAULT true,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Cart table - Main entity for cart management
CREATE TABLE IF NOT EXISTS cart (
    cart_id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL,
    cart_status VARCHAR(50) NOT NULL DEFAULT 'active' CHECK (cart_status IN ('active', 'abandoned', 'converted', 'expired')),
    session_id VARCHAR(255),
    total_amount DECIMAL(10,2) DEFAULT 0.00 CHECK (total_amount >= 0),
    total_items INTEGER DEFAULT 0 CHECK (total_items >= 0),
    currency_code VARCHAR(3) DEFAULT 'USD',
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    expires_at TIMESTAMP WITH TIME ZONE DEFAULT (CURRENT_TIMESTAMP + INTERVAL '30 days'),
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
);

-- CartItem table - Items within a cart
CREATE TABLE IF NOT EXISTS cart_item (
    cart_item_id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    cart_id UUID NOT NULL,
    product_id UUID NOT NULL,
    quantity INTEGER NOT NULL DEFAULT 1 CHECK (quantity > 0),
    unit_price DECIMAL(10,2) NOT NULL CHECK (unit_price >= 0),
    total_price DECIMAL(10,2) GENERATED ALWAYS AS (quantity * unit_price) STORED,
    added_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (cart_id) REFERENCES cart(cart_id) ON DELETE CASCADE,
    FOREIGN KEY (product_id) REFERENCES products(product_id) ON DELETE CASCADE,
    UNIQUE(cart_id, product_id)
);

-- Cart History table for audit trail
CREATE TABLE IF NOT EXISTS cart_history (
    history_id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    cart_id UUID NOT NULL,
    action_type VARCHAR(50) NOT NULL CHECK (action_type IN ('created', 'item_added', 'item_removed', 'item_updated', 'status_changed', 'expired')),
    action_details JSONB,
    performed_by UUID,
    performed_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (cart_id) REFERENCES cart(cart_id) ON DELETE CASCADE,
    FOREIGN KEY (performed_by) REFERENCES users(user_id) ON DELETE SET NULL
);

-- Indexes for performance optimization
CREATE INDEX IF NOT EXISTS idx_cart_user_id ON cart(user_id);
CREATE INDEX IF NOT EXISTS idx_cart_status ON cart(cart_status);
CREATE INDEX IF NOT EXISTS idx_cart_session_id ON cart(session_id);
CREATE INDEX IF NOT EXISTS idx_cart_expires_at ON cart(expires_at);
CREATE INDEX IF NOT EXISTS idx_cart_item_cart_id ON cart_item(cart_id);
CREATE INDEX IF NOT EXISTS idx_cart_item_product_id ON cart_item(product_id);
CREATE INDEX IF NOT EXISTS idx_cart_history_cart_id ON cart_history(cart_id);
CREATE INDEX IF NOT EXISTS idx_cart_history_action_type ON cart_history(action_type);
CREATE INDEX IF NOT EXISTS idx_products_sku ON products(sku);
CREATE INDEX IF NOT EXISTS idx_products_category_id ON products(category_id);
CREATE INDEX IF NOT EXISTS idx_users_email ON users(email);
CREATE INDEX IF NOT EXISTS idx_users_username ON users(username);

-- Triggers for updating timestamps
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ language 'plpgsql';

-- Apply update triggers
DROP TRIGGER IF EXISTS update_users_updated_at ON users;
CREATE TRIGGER update_users_updated_at BEFORE UPDATE ON users FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

DROP TRIGGER IF EXISTS update_products_updated_at ON products;
CREATE TRIGGER update_products_updated_at BEFORE UPDATE ON products FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

DROP TRIGGER IF EXISTS update_cart_updated_at ON cart;
CREATE TRIGGER update_cart_updated_at BEFORE UPDATE ON cart FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

DROP TRIGGER IF EXISTS update_cart_item_updated_at ON cart_item;
CREATE TRIGGER update_cart_item_updated_at BEFORE UPDATE ON cart_item FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

-- Function to update cart totals
CREATE OR REPLACE FUNCTION update_cart_totals()
RETURNS TRIGGER AS $$
BEGIN
    UPDATE cart 
    SET 
        total_amount = (
            SELECT COALESCE(SUM(total_price), 0) 
            FROM cart_item 
            WHERE cart_id = COALESCE(NEW.cart_id, OLD.cart_id)
        ),
        total_items = (
            SELECT COALESCE(SUM(quantity), 0) 
            FROM cart_item 
            WHERE cart_id = COALESCE(NEW.cart_id, OLD.cart_id)
        )
    WHERE cart_id = COALESCE(NEW.cart_id, OLD.cart_id);
    
    RETURN COALESCE(NEW, OLD);
END;
$$ language 'plpgsql';

-- Triggers for cart total updates
DROP TRIGGER IF EXISTS update_cart_totals_on_insert ON cart_item;
CREATE TRIGGER update_cart_totals_on_insert AFTER INSERT ON cart_item FOR EACH ROW EXECUTE FUNCTION update_cart_totals();

DROP TRIGGER IF EXISTS update_cart_totals_on_update ON cart_item;
CREATE TRIGGER update_cart_totals_on_update AFTER UPDATE ON cart_item FOR EACH ROW EXECUTE FUNCTION update_cart_totals();

DROP TRIGGER IF EXISTS update_cart_totals_on_delete ON cart_item;
CREATE TRIGGER update_cart_totals_on_delete AFTER DELETE ON cart_item FOR EACH ROW EXECUTE FUNCTION update_cart_totals();

-- Comments for documentation
COMMENT ON TABLE cart IS 'Main cart entity storing user shopping cart information';
COMMENT ON TABLE cart_item IS 'Individual items within a shopping cart';
COMMENT ON TABLE cart_history IS 'Audit trail for cart operations and changes';
COMMENT ON COLUMN cart.cart_status IS 'Current status of the cart: active, abandoned, converted, expired';
COMMENT ON COLUMN cart.expires_at IS 'Timestamp when the cart expires and becomes inactive';
COMMENT ON COLUMN cart_item.total_price IS 'Computed column: quantity * unit_price';