-- =========================
-- SCHEMA RECONCILIATION DML
-- Data updates to support LLD requirements
-- =========================

-- Update existing cart_items to populate price_at_addition from current product prices
UPDATE cart_items 
SET price_at_addition = p.price
FROM products p
WHERE cart_items.product_id = p.product_id 
AND cart_items.price_at_addition IS NULL;

-- Update cart updated_at timestamps for existing records
UPDATE cart 
SET updated_at = created_at 
WHERE updated_at IS NULL;

-- Ensure all products have is_active set to true for existing records
UPDATE products 
SET is_active = TRUE 
WHERE is_active IS NULL;

-- Populate name column for existing products
UPDATE products 
SET name = product_name 
WHERE name IS NULL AND product_name IS NOT NULL;

-- Populate stock_quantity for existing products
UPDATE products 
SET stock_quantity = available_qty 
WHERE stock_quantity IS NULL AND available_qty IS NOT NULL;

-- Set default email values for users without email (if any)
UPDATE users 
SET email = CONCAT(username, '@example.com') 
WHERE email IS NULL OR email = '';

-- Generate password_hash from existing password (simple copy for compatibility)
-- In production, this should be properly hashed
UPDATE users 
SET password_hash = password 
WHERE password_hash IS NULL AND password IS NOT NULL;