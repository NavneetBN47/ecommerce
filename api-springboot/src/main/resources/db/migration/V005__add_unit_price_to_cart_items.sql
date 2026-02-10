-- Migration V005: Add unit_price to cart_items
-- Captures product price at time of adding to cart

-- Add unit_price column
ALTER TABLE cart_items ADD COLUMN IF NOT EXISTS unit_price DECIMAL(10,2);

-- Populate unit_price from products table for existing records
UPDATE cart_items ci
SET unit_price = p.price
FROM products p
WHERE ci.product_id = p.product_id AND ci.unit_price IS NULL;

-- Make unit_price NOT NULL and add check constraint
ALTER TABLE cart_items ALTER COLUMN unit_price SET NOT NULL;
ALTER TABLE cart_items ADD CONSTRAINT chk_unit_price_positive CHECK (unit_price >= 0);

-- Add comment
COMMENT ON COLUMN cart_items.unit_price IS 'Product price at time of adding to cart';