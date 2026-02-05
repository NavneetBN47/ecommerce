-- V003__add_cart_constraints.sql
-- Additional constraints and indexes for cart functionality

-- Add unique constraint to prevent duplicate cart items
ALTER TABLE cart_items 
ADD CONSTRAINT uk_cart_product UNIQUE (cart_id, product_id);

-- Add trigger to update cart totals (MySQL)
DELIMITER $$

CREATE TRIGGER update_cart_totals_after_insert
AFTER INSERT ON cart_items
FOR EACH ROW
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
    )
    WHERE id = NEW.cart_id;
END$$

CREATE TRIGGER update_cart_totals_after_update
AFTER UPDATE ON cart_items
FOR EACH ROW
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
    )
    WHERE id = NEW.cart_id;
END$$

CREATE TRIGGER update_cart_totals_after_delete
AFTER DELETE ON cart_items
FOR EACH ROW
BEGIN
    UPDATE carts 
    SET total_amount = (
        SELECT COALESCE(SUM(subtotal), 0) 
        FROM cart_items 
        WHERE cart_id = OLD.cart_id
    ),
    total_items = (
        SELECT COALESCE(SUM(quantity), 0) 
        FROM cart_items 
        WHERE cart_id = OLD.cart_id
    )
    WHERE id = OLD.cart_id;
END$$

DELIMITER ;

-- Add comment to document cart lifecycle
ALTER TABLE carts COMMENT = 'Shopping carts with lazy creation and auto-delete when empty';
ALTER TABLE cart_items COMMENT = 'Cart items with automatic subtotal calculation';