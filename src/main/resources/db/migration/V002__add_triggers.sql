-- Migration V002: Add database triggers for automatic updates

-- Create function for updating timestamps
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ language 'plpgsql';

-- Drop existing triggers if they exist
DROP TRIGGER IF EXISTS update_users_updated_at ON users;
DROP TRIGGER IF EXISTS update_products_updated_at ON products;
DROP TRIGGER IF EXISTS update_carts_updated_at ON carts;
DROP TRIGGER IF EXISTS update_cart_items_updated_at ON cart_items;

-- Create triggers for automatic timestamp updates
CREATE TRIGGER update_users_updated_at 
    BEFORE UPDATE ON users
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

-- Create function to update cart totals
CREATE OR REPLACE FUNCTION update_cart_totals()
RETURNS TRIGGER AS $$
BEGIN
    UPDATE carts 
    SET 
        total_amount = (
            SELECT COALESCE(SUM(total_price), 0) 
            FROM cart_items 
            WHERE cart_id = COALESCE(NEW.cart_id, OLD.cart_id)
        ),
        item_count = (
            SELECT COALESCE(SUM(quantity), 0) 
            FROM cart_items 
            WHERE cart_id = COALESCE(NEW.cart_id, OLD.cart_id)
        )
    WHERE cart_id = COALESCE(NEW.cart_id, OLD.cart_id);
    
    RETURN COALESCE(NEW, OLD);
END;
$$ language 'plpgsql';

-- Drop existing cart total triggers if they exist
DROP TRIGGER IF EXISTS update_cart_totals_insert ON cart_items;
DROP TRIGGER IF EXISTS update_cart_totals_update ON cart_items;
DROP TRIGGER IF EXISTS update_cart_totals_delete ON cart_items;

-- Create triggers for cart total updates
CREATE TRIGGER update_cart_totals_insert 
    AFTER INSERT ON cart_items
    FOR EACH ROW 
    EXECUTE FUNCTION update_cart_totals();

CREATE TRIGGER update_cart_totals_update 
    AFTER UPDATE ON cart_items
    FOR EACH ROW 
    EXECUTE FUNCTION update_cart_totals();

CREATE TRIGGER update_cart_totals_delete 
    AFTER DELETE ON cart_items
    FOR EACH ROW 
    EXECUTE FUNCTION update_cart_totals();