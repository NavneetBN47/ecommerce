-- Triggers Migration
-- Version: V002
-- Description: Create triggers for automatic timestamp updates and cart totals

-- Create function for automatic timestamp updates
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ language 'plpgsql';

-- Apply update triggers to relevant tables
CREATE TRIGGER update_users_updated_at BEFORE UPDATE ON users
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_products_updated_at BEFORE UPDATE ON products
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_carts_updated_at BEFORE UPDATE ON carts
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_cart_items_updated_at BEFORE UPDATE ON cart_items
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_addresses_updated_at BEFORE UPDATE ON user_addresses
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

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

-- Apply cart total update triggers
CREATE TRIGGER update_cart_totals_insert AFTER INSERT ON cart_items
    FOR EACH ROW EXECUTE FUNCTION update_cart_totals();

CREATE TRIGGER update_cart_totals_update AFTER UPDATE ON cart_items
    FOR EACH ROW EXECUTE FUNCTION update_cart_totals();

CREATE TRIGGER update_cart_totals_delete AFTER DELETE ON cart_items
    FOR EACH ROW EXECUTE FUNCTION update_cart_totals();