-- =========================================================
-- VALIDATION QUERIES FOR SHOPPING CART SYSTEM SCHEMA
-- =========================================================
-- Queries to verify schema correctness and constraint enforcement

-- Verify all required tables exist
SELECT 
    table_name,
    table_type
FROM information_schema.tables 
WHERE table_schema = 'public' 
    AND table_name IN ('users', 'products', 'cart', 'cart_items')
ORDER BY table_name;

-- Verify users table structure and constraints
SELECT 
    column_name,
    data_type,
    is_nullable,
    column_default
FROM information_schema.columns 
WHERE table_name = 'users' 
    AND table_schema = 'public'
ORDER BY ordinal_position;

-- Verify products table structure and constraints
SELECT 
    column_name,
    data_type,
    is_nullable,
    column_default
FROM information_schema.columns 
WHERE table_name = 'products' 
    AND table_schema = 'public'
ORDER BY ordinal_position;

-- Verify cart table structure and constraints
SELECT 
    column_name,
    data_type,
    is_nullable,
    column_default
FROM information_schema.columns 
WHERE table_name = 'cart' 
    AND table_schema = 'public'
ORDER BY ordinal_position;

-- Verify cart_items table structure and constraints
SELECT 
    column_name,
    data_type,
    is_nullable,
    column_default
FROM information_schema.columns 
WHERE table_name = 'cart_items' 
    AND table_schema = 'public'
ORDER BY ordinal_position;

-- Verify foreign key constraints exist
SELECT 
    tc.constraint_name,
    tc.table_name,
    kcu.column_name,
    ccu.table_name AS foreign_table_name,
    ccu.column_name AS foreign_column_name
FROM information_schema.table_constraints AS tc 
JOIN information_schema.key_column_usage AS kcu
    ON tc.constraint_name = kcu.constraint_name
    AND tc.table_schema = kcu.table_schema
JOIN information_schema.constraint_column_usage AS ccu
    ON ccu.constraint_name = tc.constraint_name
    AND ccu.table_schema = tc.table_schema
WHERE tc.constraint_type = 'FOREIGN KEY' 
    AND tc.table_name IN ('cart', 'cart_items');

-- Verify unique constraints
SELECT 
    tc.constraint_name,
    tc.table_name,
    kcu.column_name
FROM information_schema.table_constraints AS tc 
JOIN information_schema.key_column_usage AS kcu
    ON tc.constraint_name = kcu.constraint_name
    AND tc.table_schema = kcu.table_schema
WHERE tc.constraint_type = 'UNIQUE' 
    AND tc.table_name IN ('users', 'cart', 'cart_items');

-- Verify check constraints
SELECT 
    tc.constraint_name,
    tc.table_name,
    cc.check_clause
FROM information_schema.table_constraints AS tc 
JOIN information_schema.check_constraints AS cc
    ON tc.constraint_name = cc.constraint_name
WHERE tc.constraint_type = 'CHECK' 
    AND tc.table_name IN ('products', 'cart_items');

-- Test data integrity - verify no invalid data exists
-- Check for negative prices
SELECT COUNT(*) as negative_price_count
FROM products 
WHERE price < 0;

-- Check for negative available quantities
SELECT COUNT(*) as negative_qty_count
FROM products 
WHERE available_qty < 0;

-- Check for zero or negative cart item quantities
SELECT COUNT(*) as invalid_quantity_count
FROM cart_items 
WHERE quantity <= 0;

-- Verify referential integrity
-- Check for orphaned cart records
SELECT COUNT(*) as orphaned_carts
FROM cart c
LEFT JOIN users u ON c.user_id = u.user_id
WHERE u.user_id IS NULL;

-- Check for orphaned cart_items records
SELECT COUNT(*) as orphaned_cart_items
FROM cart_items ci
LEFT JOIN cart c ON ci.cart_id = c.cart_id
WHERE c.cart_id IS NULL;

-- Check for cart_items referencing non-existent products
SELECT COUNT(*) as invalid_product_refs
FROM cart_items ci
LEFT JOIN products p ON ci.product_id = p.product_id
WHERE p.product_id IS NULL;