-- Migration V002: Add Triggers for Timestamp Updates and Cart Totals

-- Function for updating timestamps
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ language 'plpgsql';

-- Apply update triggers
DROP TRIGGER IF EXISTS update_users_updated_at ON users;
CREATE TRIGGER update_users_updated_at 
    BEFORE UPDATE ON users 
    FOR EACH ROW 
    EXECUTE FUNCTION update_updated_at_column();

DROP TRIGGER IF EXISTS update_products_updated_at ON products;
CREATE TRIGGER update_products_updated_at 
    BEFORE UPDATE ON products 
    FOR EACH ROW 
    EXECUTE FUNCTION update_updated_at_column();

DROP TRIGGER IF EXISTS update_cart_updated_at ON cart;
CREATE TRIGGER update_cart_updated_at 
    BEFORE UPDATE ON cart 
    FOR EACH ROW 
    EXECUTE FUNCTION update_updated_at_column();

DROP TRIGGER IF EXISTS update_cart_item_updated_at ON cart_item;
CREATE TRIGGER update_cart_item_updated_at 
    BEFORE UPDATE ON cart_item 
    FOR EACH ROW 
    EXECUTE FUNCTION update_updated_at_column();

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
CREATE TRIGGER update_cart_totals_on_insert 
    AFTER INSERT ON cart_item 
    FOR EACH ROW 
    EXECUTE FUNCTION update_cart_totals();

DROP TRIGGER IF EXISTS update_cart_totals_on_update ON cart_item;
CREATE TRIGGER update_cart_totals_on_update 
    AFTER UPDATE ON cart_item 
    FOR EACH ROW 
    EXECUTE FUNCTION update_cart_totals();

DROP TRIGGER IF EXISTS update_cart_totals_on_delete ON cart_item;
CREATE TRIGGER update_cart_totals_on_delete 
    AFTER DELETE ON cart_item 
    FOR EACH ROW 
    EXECUTE FUNCTION update_cart_totals();