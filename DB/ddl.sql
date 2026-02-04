-- =========================
-- SCHEMA RECONCILIATION DDL
-- Adding missing fields and constraints from LLD
-- All changes are additive and non-destructive
-- =========================

-- Add unique constraint to users email if not exists
ALTER TABLE users ADD CONSTRAINT users_email_unique UNIQUE (email);

-- Add missing fields to products table
ALTER TABLE products ADD COLUMN is_active BOOLEAN DEFAULT true;

-- Add missing updated_at field to cart table
ALTER TABLE cart ADD COLUMN updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP;

-- Add missing price_at_addition field to cart_items table
ALTER TABLE cart_items ADD COLUMN price_at_addition DECIMAL(10,2);

-- Add check constraints for validation as per LLD
ALTER TABLE products ADD CONSTRAINT products_price_check CHECK (price >= 0);
ALTER TABLE products ADD CONSTRAINT products_stock_check CHECK (available_qty >= 0);
ALTER TABLE cart_items ADD CONSTRAINT cart_items_quantity_check CHECK (quantity > 0);

-- Add indexes for performance
CREATE INDEX IF NOT EXISTS idx_users_email ON users(email);
CREATE INDEX IF NOT EXISTS idx_cart_user_id ON cart(user_id);
CREATE INDEX IF NOT EXISTS idx_cart_items_cart_id ON cart_items(cart_id);
CREATE INDEX IF NOT EXISTS idx_cart_items_product_id ON cart_items(product_id);

-- Add trigger to update cart.updated_at when cart_items are modified
CREATE OR REPLACE FUNCTION update_cart_timestamp()
RETURNS TRIGGER AS $$
BEGIN
    UPDATE cart SET updated_at = CURRENT_TIMESTAMP WHERE cart_id = COALESCE(NEW.cart_id, OLD.cart_id);
    RETURN COALESCE(NEW, OLD);
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER cart_items_update_trigger
    AFTER INSERT OR UPDATE OR DELETE ON cart_items
    FOR EACH ROW
    EXECUTE FUNCTION update_cart_timestamp();