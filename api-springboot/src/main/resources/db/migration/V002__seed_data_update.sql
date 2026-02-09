-- Migration V002: Update Seed Data
-- Updates existing seed data to comply with new schema requirements

-- ============================================
-- 1. UPDATE EXISTING USERS WITH USERNAMES
-- ============================================

-- Generate unique usernames for existing users if not already set
UPDATE users 
SET username = LOWER(CONCAT(
    REPLACE(first_name, ' ', ''),
    '_',
    REPLACE(last_name, ' ', ''),
    '_',
    SUBSTRING(MD5(RANDOM()::TEXT), 1, 4)
))
WHERE username IS NULL OR username = '';

-- Ensure admin user has proper username
UPDATE users 
SET username = 'admin'
WHERE email = 'admin@ecommerce.com' AND username != 'admin';

-- ============================================
-- 2. SYNC PRODUCT INVENTORY
-- ============================================

-- Sync available_qty with product_inventory table
UPDATE products p
SET available_qty = pi.quantity_available
FROM product_inventory pi
WHERE p.product_id = pi.product_id;

-- ============================================
-- 3. CLEAN UP INVALID CARTS
-- ============================================

-- Remove any carts that violate 1:1 relationship
-- Keep only the most recent cart for each user
DELETE FROM shopping_carts
WHERE cart_id NOT IN (
    SELECT DISTINCT ON (user_id) cart_id
    FROM shopping_carts
    ORDER BY user_id, created_at DESC
);

-- Remove empty carts (carts with no items)
DELETE FROM shopping_carts
WHERE cart_id NOT IN (
    SELECT DISTINCT cart_id FROM cart_items
);

-- ============================================
-- 4. VALIDATE CART ITEMS
-- ============================================

-- Remove cart items with invalid quantities
DELETE FROM cart_items
WHERE quantity <= 0;

-- Remove duplicate cart items (keep the one with higher quantity)
DELETE FROM cart_items ci1
WHERE EXISTS (
    SELECT 1 FROM cart_items ci2
    WHERE ci1.cart_id = ci2.cart_id
    AND ci1.product_id = ci2.product_id
    AND ci1.cart_item_id < ci2.cart_item_id
);

-- Update unit_price to match current product price
UPDATE cart_items ci
SET unit_price = p.price
FROM products p
WHERE ci.product_id = p.product_id;

-- ============================================
-- 5. VALIDATION REPORT
-- ============================================

-- Generate validation report
DO $$
DECLARE
    user_count INTEGER;
    product_count INTEGER;
    cart_count INTEGER;
    cart_item_count INTEGER;
BEGIN
    SELECT COUNT(*) INTO user_count FROM users;
    SELECT COUNT(*) INTO product_count FROM products;
    SELECT COUNT(*) INTO cart_count FROM shopping_carts;
    SELECT COUNT(*) INTO cart_item_count FROM cart_items;
    
    RAISE NOTICE '=== Migration V002 Validation Report ===';
    RAISE NOTICE 'Total Users: %', user_count;
    RAISE NOTICE 'Total Products: %', product_count;
    RAISE NOTICE 'Total Active Carts: %', cart_count;
    RAISE NOTICE 'Total Cart Items: %', cart_item_count;
    RAISE NOTICE '=======================================';
END $$;

-- Migration completed successfully
RAISE NOTICE 'Migration V002 completed: Seed data updated successfully';