-- =========================
-- SCHEMA RECONCILIATION DML
-- Update existing data to match LLD requirements
-- =========================

-- Update existing cart_items to set price_at_addition from current product price
-- This ensures existing cart items have the required price_at_addition field populated
UPDATE cart_items 
SET price_at_addition = (
    SELECT price 
    FROM products 
    WHERE products.product_id = cart_items.product_id
)
WHERE price_at_addition IS NULL;

-- Update all existing carts to set updated_at to created_at for consistency
UPDATE cart 
SET updated_at = created_at 
WHERE updated_at IS NULL;

-- Ensure all products have is_active set to true (default for existing products)
UPDATE products 
SET is_active = true 
WHERE is_active IS NULL;