-- Database Schema Reconciliation - DML Changes
-- Date: 2026-02-04
-- Purpose: Data manipulation statements for schema reconciliation

-- Clean up any invalid data before applying constraints

-- Remove products with negative prices (if any)
DELETE FROM products WHERE price < 0;

-- Remove products with negative available quantities (if any)
DELETE FROM products WHERE available_qty < 0;

-- Remove cart items with zero or negative quantities (if any)
DELETE FROM cart_items WHERE quantity <= 0;

-- Remove duplicate cart_items entries (keep the one with highest quantity)
DELETE FROM cart_items
WHERE cart_item_id NOT IN (
    SELECT MAX(cart_item_id)
    FROM cart_items
    GROUP BY cart_id, product_id
);

-- Verify data integrity after cleanup
SELECT 'Data cleanup completed' AS status;
