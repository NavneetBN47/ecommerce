-- V003__add_cart_cleanup_trigger.sql
-- Trigger for automatic cart cleanup when empty

DELIMITER $$

CREATE TRIGGER trg_cart_item_after_delete
AFTER DELETE ON cart_items
FOR EACH ROW
BEGIN
    DECLARE item_count INT;
    
    -- Count remaining items in the cart
    SELECT COUNT(*) INTO item_count
    FROM cart_items
    WHERE cart_id = OLD.cart_id;
    
    -- If no items remain, delete the cart (auto-cleanup)
    IF item_count = 0 THEN
        DELETE FROM carts WHERE id = OLD.cart_id;
    ELSE
        -- Update cart totals
        UPDATE carts c
        SET 
            total_items = (SELECT COUNT(*) FROM cart_items WHERE cart_id = c.id),
            total_amount = (SELECT COALESCE(SUM(subtotal), 0) FROM cart_items WHERE cart_id = c.id),
            updated_at = CURRENT_TIMESTAMP
        WHERE c.id = OLD.cart_id;
    END IF;
END$$

DELIMITER ;