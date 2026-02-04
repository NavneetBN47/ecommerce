-- =========================
-- SCHEMA RECONCILIATION DDL
-- Adding missing fields and constraints to align with LLD
-- All changes are additive and non-destructive
-- =========================

-- Enable UUID extension if not already enabled
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- =========================
-- USERS TABLE ENHANCEMENTS
-- =========================

-- Add UUID column for LLD compatibility (keeping existing SERIAL for backward compatibility)
ALTER TABLE users ADD COLUMN IF NOT EXISTS id UUID DEFAULT uuid_generate_v4();

-- Add unique constraint on email as required by LLD
ALTER TABLE users ADD CONSTRAINT IF NOT EXISTS unique_user_email UNIQUE (email);

-- Add password_hash column (keeping existing password for backward compatibility)
ALTER TABLE users ADD COLUMN IF NOT EXISTS password_hash VARCHAR(255);

-- Make email NOT NULL if it isn't already
ALTER TABLE users ALTER COLUMN email SET NOT NULL;

-- =========================
-- PRODUCTS TABLE ENHANCEMENTS
-- =========================

-- Add UUID column for LLD compatibility
ALTER TABLE products ADD COLUMN IF NOT EXISTS id UUID DEFAULT uuid_generate_v4();

-- Add name column (aliasing product_name for LLD compatibility)
ALTER TABLE products ADD COLUMN IF NOT EXISTS name VARCHAR(100);

-- Add stock_quantity column (aliasing available_qty for LLD compatibility)
ALTER TABLE products ADD COLUMN IF NOT EXISTS stock_quantity INT;

-- Add is_active column as required by LLD
ALTER TABLE products ADD COLUMN IF NOT EXISTS is_active BOOLEAN DEFAULT TRUE;

-- Add check constraint for price >= 0
ALTER TABLE products ADD CONSTRAINT IF NOT EXISTS check_price_non_negative CHECK (price >= 0);

-- Add check constraint for stock_quantity >= 0
ALTER TABLE products ADD CONSTRAINT IF NOT EXISTS check_stock_non_negative CHECK (stock_quantity >= 0);

-- Update existing data to populate new columns
UPDATE products SET name = product_name WHERE name IS NULL;
UPDATE products SET stock_quantity = available_qty WHERE stock_quantity IS NULL;

-- =========================
-- CART TABLE ENHANCEMENTS
-- =========================

-- Add UUID column for LLD compatibility
ALTER TABLE cart ADD COLUMN IF NOT EXISTS id UUID DEFAULT uuid_generate_v4();

-- Add updated_at timestamp as required by LLD
ALTER TABLE cart ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP;

-- Create trigger to auto-update updated_at timestamp
CREATE OR REPLACE FUNCTION update_cart_timestamp()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS trigger_cart_updated_at ON cart;
CREATE TRIGGER trigger_cart_updated_at
    BEFORE UPDATE ON cart
    FOR EACH ROW
    EXECUTE FUNCTION update_cart_timestamp();

-- =========================
-- CART_ITEMS TABLE ENHANCEMENTS
-- =========================

-- Add UUID column for LLD compatibility
ALTER TABLE cart_items ADD COLUMN IF NOT EXISTS id UUID DEFAULT uuid_generate_v4();

-- Add price_at_addition column as required by LLD
ALTER TABLE cart_items ADD COLUMN IF NOT EXISTS price_at_addition DECIMAL(10,2);

-- Add check constraint for quantity > 0
ALTER TABLE cart_items ADD CONSTRAINT IF NOT EXISTS check_quantity_positive CHECK (quantity > 0);

-- Add check constraint for price_at_addition >= 0
ALTER TABLE cart_items ADD CONSTRAINT IF NOT EXISTS check_price_at_addition_non_negative CHECK (price_at_addition >= 0);

-- =========================
-- INDEXES FOR PERFORMANCE
-- =========================

-- Add indexes on UUID columns for better performance
CREATE INDEX IF NOT EXISTS idx_users_uuid ON users(id);
CREATE INDEX IF NOT EXISTS idx_products_uuid ON products(id);
CREATE INDEX IF NOT EXISTS idx_cart_uuid ON cart(id);
CREATE INDEX IF NOT EXISTS idx_cart_items_uuid ON cart_items(id);

-- Add index on email for faster lookups
CREATE INDEX IF NOT EXISTS idx_users_email ON users(email);

-- Add index on product name for search functionality
CREATE INDEX IF NOT EXISTS idx_products_name ON products(name);

-- =========================
-- COMMENTS FOR DOCUMENTATION
-- =========================

COMMENT ON COLUMN users.id IS 'UUID identifier for LLD compatibility';
COMMENT ON COLUMN users.password_hash IS 'Hashed password for LLD compatibility';
COMMENT ON COLUMN products.id IS 'UUID identifier for LLD compatibility';
COMMENT ON COLUMN products.name IS 'Product name for LLD compatibility';
COMMENT ON COLUMN products.stock_quantity IS 'Available stock quantity for LLD compatibility';
COMMENT ON COLUMN products.is_active IS 'Product active status for LLD compatibility';
COMMENT ON COLUMN cart.id IS 'UUID identifier for LLD compatibility';
COMMENT ON COLUMN cart.updated_at IS 'Last update timestamp for LLD compatibility';
COMMENT ON COLUMN cart_items.id IS 'UUID identifier for LLD compatibility';
COMMENT ON COLUMN cart_items.price_at_addition IS 'Product price when added to cart for LLD compatibility';