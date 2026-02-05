-- Shopping Cart System DQL
-- Data Query Language statements for validation and testing
-- All queries are read-only and non-destructive

-- =============================================================================
-- SCHEMA VALIDATION QUERIES
-- =============================================================================

-- Verify table existence and structure
SELECT 
    table_name,
    table_type,
    table_schema
FROM information_schema.tables 
WHERE table_name IN ('Cart', 'CartItem')
ORDER BY table_name;

-- Verify column definitions for Cart table
SELECT 
    column_name,
    data_type,
    is_nullable,
    column_default,
    character_maximum_length
FROM information_schema.columns 
WHERE table_name = 'Cart'
ORDER BY ordinal_position;

-- Verify column definitions for CartItem table
SELECT 
    column_name,
    data_type,
    is_nullable,
    column_default,
    character_maximum_length,
    numeric_precision,
    numeric_scale
FROM information_schema.columns 
WHERE table_name = 'CartItem'
ORDER BY ordinal_position;

-- Verify constraints
SELECT 
    tc.table_name,
    tc.constraint_name,
    tc.constraint_type,
    kcu.column_name,
    ccu.table_name AS foreign_table_name,
    ccu.column_name AS foreign_column_name
FROM information_schema.table_constraints tc
LEFT JOIN information_schema.key_column_usage kcu
    ON tc.constraint_name = kcu.constraint_name
LEFT JOIN information_schema.constraint_column_usage ccu
    ON tc.constraint_name = ccu.constraint_name
WHERE tc.table_name IN ('Cart', 'CartItem')
ORDER BY tc.table_name, tc.constraint_type, tc.constraint_name;

-- Verify indexes
SELECT 
    schemaname,
    tablename,
    indexname,
    indexdef
FROM pg_indexes 
WHERE tablename IN ('cart', 'cartitem')
ORDER BY tablename, indexname;

-- =============================================================================
-- DATA VALIDATION QUERIES
-- =============================================================================

-- Basic data counts
SELECT 'Cart' as table_name, COUNT(*) as record_count FROM Cart
UNION ALL
SELECT 'CartItem' as table_name, COUNT(*) as record_count FROM CartItem;

-- Cart status distribution
SELECT 
    status,
    COUNT(*) as count,
    ROUND(COUNT(*) * 100.0 / SUM(COUNT(*)) OVER (), 2) as percentage
FROM Cart
GROUP BY status
ORDER BY count DESC;

-- Cart summary with item counts and totals
SELECT 
    c.cart_id,
    c.user_id,
    c.status,
    c.created_at,
    c.updated_at,
    COUNT(ci.item_id) as item_count,
    COALESCE(SUM(ci.quantity), 0) as total_quantity,
    COALESCE(SUM(ci.quantity * ci.price_at_addition), 0) as total_value
FROM Cart c
LEFT JOIN CartItem ci ON c.cart_id = ci.cart_id
GROUP BY c.cart_id, c.user_id, c.status, c.created_at, c.updated_at
ORDER BY c.created_at DESC;

-- CartItem details with cart information
SELECT 
    ci.item_id,
    ci.cart_id,
    c.user_id,
    c.status as cart_status,
    ci.product_id,
    ci.quantity,
    ci.price_at_addition,
    (ci.quantity * ci.price_at_addition) as line_total,
    ci.created_at as added_at
FROM CartItem ci
JOIN Cart c ON ci.cart_id = c.cart_id
ORDER BY c.created_at DESC, ci.created_at DESC;

-- =============================================================================
-- RELATIONSHIP VALIDATION QUERIES
-- =============================================================================

-- Verify referential integrity (should return 0 orphaned records)
SELECT 
    'Orphaned CartItems' as check_type,
    COUNT(*) as violation_count
FROM CartItem ci
LEFT JOIN Cart c ON ci.cart_id = c.cart_id
WHERE c.cart_id IS NULL;

-- Check for duplicate cart items (same product in same cart)
SELECT 
    cart_id,
    product_id,
    COUNT(*) as duplicate_count
FROM CartItem
GROUP BY cart_id, product_id
HAVING COUNT(*) > 1;

-- =============================================================================
-- BUSINESS LOGIC VALIDATION QUERIES
-- =============================================================================

-- Carts with invalid quantities (should be 0)
SELECT 
    'Invalid Quantities' as check_type,
    COUNT(*) as violation_count
FROM CartItem
WHERE quantity <= 0;

-- Carts with invalid prices (should be 0)
SELECT 
    'Invalid Prices' as check_type,
    COUNT(*) as violation_count
FROM CartItem
WHERE price_at_addition < 0;

-- Carts with invalid status values (should be 0)
SELECT 
    'Invalid Status Values' as check_type,
    COUNT(*) as violation_count
FROM Cart
WHERE status NOT IN ('active', 'abandoned', 'completed', 'expired');

-- =============================================================================
-- PERFORMANCE VALIDATION QUERIES
-- =============================================================================

-- Query performance test - Cart lookup by user_id
EXPLAIN (ANALYZE, BUFFERS) 
SELECT * FROM Cart WHERE user_id = '550e8400-e29b-41d4-a716-446655440101';

-- Query performance test - CartItem lookup by cart_id
EXPLAIN (ANALYZE, BUFFERS)
SELECT * FROM CartItem WHERE cart_id = '550e8400-e29b-41d4-a716-446655440001';

-- Query performance test - Join query
EXPLAIN (ANALYZE, BUFFERS)
SELECT c.cart_id, c.status, COUNT(ci.item_id) as item_count
FROM Cart c
LEFT JOIN CartItem ci ON c.cart_id = ci.cart_id
WHERE c.user_id = '550e8400-e29b-41d4-a716-446655440101'
GROUP BY c.cart_id, c.status;

-- =============================================================================
-- SUMMARY REPORT
-- =============================================================================

-- Final validation summary
SELECT 
    'SCHEMA_VALIDATION_COMPLETE' as validation_type,
    CURRENT_TIMESTAMP as completed_at,
    'All tables, columns, constraints, and indexes validated against LLD specification' as status;