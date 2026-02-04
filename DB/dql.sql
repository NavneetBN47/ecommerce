-- =========================
-- SCHEMA RECONCILIATION VALIDATION QUERIES
-- Verify schema correctness after reconciliation
-- =========================

-- Verify users table has email unique constraint
SELECT 
    constraint_name,
    constraint_type
FROM information_schema.table_constraints 
WHERE table_name = 'users' 
    AND constraint_type = 'UNIQUE' 
    AND constraint_name = 'users_email_unique';

-- Verify products table has is_active column
SELECT 
    column_name,
    data_type,
    column_default
FROM information_schema.columns 
WHERE table_name = 'products' 
    AND column_name = 'is_active';

-- Verify cart table has updated_at column
SELECT 
    column_name,
    data_type,
    column_default
FROM information_schema.columns 
WHERE table_name = 'cart' 
    AND column_name = 'updated_at';

-- Verify cart_items table has price_at_addition column
SELECT 
    column_name,
    data_type
FROM information_schema.columns 
WHERE table_name = 'cart_items' 
    AND column_name = 'price_at_addition';

-- Verify check constraints are in place
SELECT 
    constraint_name,
    check_clause
FROM information_schema.check_constraints 
WHERE constraint_name IN (
    'products_price_check',
    'products_stock_check', 
    'cart_items_quantity_check'
);

-- Verify indexes are created
SELECT 
    indexname,
    tablename
FROM pg_indexes 
WHERE tablename IN ('users', 'cart', 'cart_items')
    AND indexname IN (
        'idx_users_email',
        'idx_cart_user_id',
        'idx_cart_items_cart_id',
        'idx_cart_items_product_id'
    );

-- Verify trigger function exists
SELECT 
    routine_name,
    routine_type
FROM information_schema.routines 
WHERE routine_name = 'update_cart_timestamp';

-- Verify trigger exists
SELECT 
    trigger_name,
    event_manipulation,
    event_object_table
FROM information_schema.triggers 
WHERE trigger_name = 'cart_items_update_trigger';

-- Test data integrity - ensure no null values in required fields after updates
SELECT 'cart_items_price_validation' as test_name, COUNT(*) as null_count
FROM cart_items 
WHERE price_at_addition IS NULL
UNION ALL
SELECT 'cart_updated_at_validation' as test_name, COUNT(*) as null_count
FROM cart 
WHERE updated_at IS NULL
UNION ALL
SELECT 'products_is_active_validation' as test_name, COUNT(*) as null_count
FROM products 
WHERE is_active IS NULL;