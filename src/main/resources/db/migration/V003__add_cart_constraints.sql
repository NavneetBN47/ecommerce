-- V003__add_cart_constraints.sql
-- Additional constraints and optimizations for cart management

-- Add constraint to ensure cart status is valid
ALTER TABLE carts ADD CONSTRAINT chk_cart_status 
    CHECK (status IN ('ACTIVE', 'CHECKED_OUT', 'ABANDONED'));

-- Add constraint to ensure order status is valid
ALTER TABLE orders ADD CONSTRAINT chk_order_status 
    CHECK (status IN ('PENDING', 'CONFIRMED', 'PROCESSING', 'SHIPPED', 'DELIVERED', 'CANCELLED'));

-- Add trigger to update updated_at timestamp for users
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ language 'plpgsql';

CREATE TRIGGER update_users_updated_at BEFORE UPDATE ON users
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_products_updated_at BEFORE UPDATE ON products
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_carts_updated_at BEFORE UPDATE ON carts
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_cart_items_updated_at BEFORE UPDATE ON cart_items
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_orders_updated_at BEFORE UPDATE ON orders
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

-- Add index for better query performance on cart status and user
CREATE INDEX idx_cart_user_status ON carts(user_id, status);

-- Add index for order queries
CREATE INDEX idx_order_created_at ON orders(created_at DESC);

-- Add comment for cart auto-delete feature
COMMENT ON TABLE carts IS 'Shopping carts for users. Empty carts are automatically deleted.';
COMMENT ON COLUMN carts.status IS 'Cart status: ACTIVE (in use), CHECKED_OUT (converted to order), ABANDONED (inactive)';