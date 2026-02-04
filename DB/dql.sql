-- =========================
-- SCHEMA RECONCILIATION VALIDATION QUERIES
-- Queries to verify schema changes are applied correctly
-- =========================

-- Verify UUID extension is enabled
SELECT 
    extname as extension_name,
    extversion as version
FROM pg_extension 
WHERE extname = 'uuid-ossp';

-- Verify users table has all required LLD columns
SELECT 
    column_name,
    data_type,
    is_nullable,
    column_default
FROM information_schema.columns 
WHERE table_name = 'users' 
AND column_name IN ('id', 'email', 'password_hash', 'created_at')
ORDER BY column_name;

-- Verify products table has all required LLD columns
SELECT 
    column_name,
    data_type,
    is_nullable,
    column_default
FROM information_schema.columns 
WHERE table_name = 'products' 
AND column_name IN ('id', 'name', 'price', 'stock_quantity', 'is_active')
ORDER BY column_name;

-- Verify cart table has all required LLD columns
SELECT 
    column_name,
    data_type,
    is_nullable,
    column_default
FROM information_schema.columns 
WHERE table_name = 'cart' 
AND column_name IN ('id', 'user_id', 'created_at', 'updated_at')
ORDER BY column_name;

-- Verify cart_items table has all required LLD columns
SELECT 
    column_name,
    data_type,
    is_nullable,
    column_default
FROM information_schema.columns 
WHERE table_name = 'cart_items' 
AND column_name IN ('id', 'cart_id', 'product_id', 'quantity', 'price_at_addition')
ORDER BY column_name;

-- Verify unique constraints are in place
SELECT 
    tc.constraint_name,
    tc.table_name,
    kcu.column_name
FROM information_schema.table_constraints tc
JOIN information_schema.key_column_usage kcu 
    ON tc.constraint_name = kcu.constraint_name
WHERE tc.constraint_type = 'UNIQUE'
AND tc.table_name IN ('users', 'products', 'cart', 'cart_items')
ORDER BY tc.table_name, tc.constraint_name;

-- Verify check constraints are in place
SELECT 
    tc.constraint_name,
    tc.table_name,
    cc.check_clause
FROM information_schema.table_constraints tc
JOIN information_schema.check_constraints cc 
    ON tc.constraint_name = cc.constraint_name
WHERE tc.constraint_type = 'CHECK'
AND tc.table_name IN ('users', 'products', 'cart', 'cart_items')
ORDER BY tc.table_name, tc.constraint_name;

-- Verify foreign key constraints are intact
SELECT 
    tc.constraint_name,
    tc.table_name,
    kcu.column_name,
    ccu.table_name AS foreign_table_name,
    ccu.column_name AS foreign_column_name
FROM information_schema.table_constraints tc
JOIN information_schema.key_column_usage kcu 
    ON tc.constraint_name = kcu.constraint_name
JOIN information_schema.constraint_column_usage ccu 
    ON ccu.constraint_name = tc.constraint_name
WHERE tc.constraint_type = 'FOREIGN KEY'
AND tc.table_name IN ('cart', 'cart_items')
ORDER BY tc.table_name, tc.constraint_name;

-- Verify indexes are created
SELECT 
    indexname,
    tablename,
    indexdef
FROM pg_indexes 
WHERE tablename IN ('users', 'products', 'cart', 'cart_items')
AND indexname LIKE 'idx_%'
ORDER BY tablename, indexname;

-- Verify trigger is created for cart updated_at
SELECT 
    trigger_name,
    event_manipulation,
    event_object_table,
    action_timing
FROM information_schema.triggers 
WHERE event_object_table = 'cart'
AND trigger_name = 'trigger_cart_updated_at';

-- Sample data verification - check that UUID columns are populated
SELECT 
    'users' as table_name,
    COUNT(*) as total_records,
    COUNT(id) as records_with_uuid
FROM users
UNION ALL
SELECT 
    'products' as table_name,
    COUNT(*) as total_records,
    COUNT(id) as records_with_uuid
FROM products
UNION ALL
SELECT 
    'cart' as table_name,
    COUNT(*) as total_records,
    COUNT(id) as records_with_uuid
FROM cart
UNION ALL
SELECT 
    'cart_items' as table_name,
    COUNT(*) as total_records,
    COUNT(id) as records_with_uuid
FROM cart_items;

-- Verify data integrity after updates
SELECT 
    'products_with_name' as check_name,
    COUNT(*) as count
FROM products 
WHERE name IS NOT NULL
UNION ALL
SELECT 
    'products_with_stock_quantity' as check_name,
    COUNT(*) as count
FROM products 
WHERE stock_quantity IS NOT NULL
UNION ALL
SELECT 
    'cart_items_with_price_at_addition' as check_name,
    COUNT(*) as count
FROM cart_items 
WHERE price_at_addition IS NOT NULL
UNION ALL
SELECT 
    'users_with_email' as check_name,
    COUNT(*) as count
FROM users 
WHERE email IS NOT NULL AND email != '';

-- Final validation - LLD compliance check
SELECT 
    'Schema reconciliation validation complete' as status,
    CURRENT_TIMESTAMP as validated_at;