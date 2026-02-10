-- V003__add_cart_constraints.sql
-- Additional constraints and optimizations for cart functionality

-- Add trigger to update cart totals automatically
CREATE OR REPLACE FUNCTION update_cart_totals()
RETURNS TRIGGER AS $$
BEGIN
    UPDATE carts
    SET total_amount = (
        SELECT COALESCE(SUM(subtotal), 0)
        FROM cart_items
        WHERE cart_id = NEW.cart_id
    ),
    total_items = (
        SELECT COALESCE(SUM(quantity), 0)
        FROM cart_items
        WHERE cart_id = NEW.cart_id
    ),
    updated_at = CURRENT_TIMESTAMP
    WHERE id = NEW.cart_id;
    
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trigger_update_cart_totals
AFTER INSERT OR UPDATE OR DELETE ON cart_items
FOR EACH ROW
EXECUTE FUNCTION update_cart_totals();

-- Add trigger to update timestamps
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trigger_users_updated_at
BEFORE UPDATE ON users
FOR EACH ROW
EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER trigger_products_updated_at
BEFORE UPDATE ON products
FOR EACH ROW
EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER trigger_carts_updated_at
BEFORE UPDATE ON carts
FOR EACH ROW
EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER trigger_cart_items_updated_at
BEFORE UPDATE ON cart_items
FOR EACH ROW
EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER trigger_orders_updated_at
BEFORE UPDATE ON orders
FOR EACH ROW
EXECUTE FUNCTION update_updated_at_column();

COMMENT ON FUNCTION update_cart_totals() IS 'Automatically update cart totals when cart items change';
COMMENT ON FUNCTION update_updated_at_column() IS 'Automatically update updated_at timestamp';