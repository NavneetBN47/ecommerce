-- Shopping Cart System Data Query Language (DQL)
-- Validation queries and common business queries as per LLD specifications

-- =====================================================
-- SCHEMA VALIDATION QUERIES
-- =====================================================

-- Verify all required tables exist
SELECT 
    table_name,
    table_type
FROM information_schema.tables 
WHERE table_schema = 'public' 
    AND table_name IN ('users', 'products', 'cart', 'cart_items')
ORDER BY table_name;

-- Verify all required columns exist with correct data types
SELECT 
    table_name,
    column_name,
    data_type,
    is_nullable,
    column_default
FROM information_schema.columns 
WHERE table_schema = 'public' 
    AND table_name IN ('users', 'products', 'cart', 'cart_items')
ORDER BY table_name, ordinal_position;

-- Verify foreign key constraints
SELECT 
    tc.constraint_name,
    tc.table_name,
    kcu.column_name,
    ccu.table_name AS foreign_table_name,
    ccu.column_name AS foreign_column_name
FROM information_schema.table_constraints AS tc
JOIN information_schema.key_column_usage AS kcu
    ON tc.constraint_name = kcu.constraint_name
JOIN information_schema.constraint_column_usage AS ccu
    ON ccu.constraint_name = tc.constraint_name
WHERE tc.constraint_type = 'FOREIGN KEY'
    AND tc.table_schema = 'public'
ORDER BY tc.table_name;

-- Verify check constraints for business rules
SELECT 
    constraint_name,
    table_name,
    check_clause
FROM information_schema.check_constraints
WHERE constraint_schema = 'public'
ORDER BY table_name;

-- =====================================================
-- BUSINESS VALIDATION QUERIES
-- =====================================================

-- Verify cart status constraints (only ACTIVE or CHECKED_OUT)
SELECT 
    status,
    COUNT(*) as count
FROM cart
GROUP BY status
ORDER BY status;

-- Verify quantity constraints (1-10 per cart item)
SELECT 
    MIN(quantity) as min_quantity,
    MAX(quantity) as max_quantity,
    AVG(quantity) as avg_quantity
FROM cart_items;

-- Verify no duplicate products per cart
SELECT 
    cart_id,
    product_id,
    COUNT(*) as duplicate_count
FROM cart_items
GROUP BY cart_id, product_id
HAVING COUNT(*) > 1;

-- Verify price consistency (price_at_addition should be positive)
SELECT 
    COUNT(*) as total_items,
    COUNT(CASE WHEN price_at_addition <= 0 THEN 1 END) as invalid_prices
FROM cart_items;

-- =====================================================
-- COMMON BUSINESS QUERIES
-- =====================================================

-- Get user's active cart with items
SELECT 
    u.username,
    c.id as cart_id,
    c.status,
    p.name as product_name,
    ci.quantity,
    ci.price_at_addition,
    (ci.quantity * ci.price_at_addition) as line_total
FROM users u
JOIN cart c ON u.id = c.user_id
JOIN cart_items ci ON c.id = ci.cart_id
JOIN products p ON ci.product_id = p.id
WHERE c.status = 'ACTIVE'
ORDER BY u.username, p.name;

-- Get cart totals by user
SELECT 
    u.username,
    c.status,
    COUNT(ci.id) as total_items,
    SUM(ci.quantity) as total_quantity,
    SUM(ci.quantity * ci.price_at_addition) as cart_total
FROM users u
JOIN cart c ON u.id = c.user_id
LEFT JOIN cart_items ci ON c.id = ci.cart_id
GROUP BY u.id, u.username, c.id, c.status
ORDER BY u.username;

-- Get product inventory status
SELECT 
    p.name,
    p.category,
    p.stock_quantity,
    COALESCE(SUM(ci.quantity), 0) as quantity_in_active_carts,
    (p.stock_quantity - COALESCE(SUM(ci.quantity), 0)) as available_stock
FROM products p
LEFT JOIN cart_items ci ON p.id = ci.product_id
LEFT JOIN cart c ON ci.cart_id = c.id AND c.status = 'ACTIVE'
GROUP BY p.id, p.name, p.category, p.stock_quantity
ORDER BY p.category, p.name;

-- Get top products by quantity in carts
SELECT 
    p.name,
    p.category,
    SUM(ci.quantity) as total_quantity_in_carts,
    COUNT(DISTINCT ci.cart_id) as number_of_carts
FROM products p
JOIN cart_items ci ON p.id = ci.product_id
JOIN cart c ON ci.cart_id = c.id
WHERE c.status = 'ACTIVE'
GROUP BY p.id, p.name, p.category
ORDER BY total_quantity_in_carts DESC;

-- Get users without active carts
SELECT 
    u.id,
    u.username,
    u.email,
    u.created_at
FROM users u
LEFT JOIN cart c ON u.id = c.user_id AND c.status = 'ACTIVE'
WHERE c.id IS NULL
ORDER BY u.created_at DESC;

-- Get checkout history (checked out carts)
SELECT 
    u.username,
    c.id as cart_id,
    c.updated_at as checkout_date,
    COUNT(ci.id) as items_count,
    SUM(ci.quantity * ci.price_at_addition) as total_amount
FROM users u
JOIN cart c ON u.id = c.user_id
LEFT JOIN cart_items ci ON c.id = ci.cart_id
WHERE c.status = 'CHECKED_OUT'
GROUP BY u.id, u.username, c.id, c.updated_at
ORDER BY c.updated_at DESC;

-- =====================================================
-- DATA QUALITY CHECKS
-- =====================================================

-- Check for orphaned cart items (should be none due to FK constraints)
SELECT COUNT(*) as orphaned_cart_items
FROM cart_items ci
LEFT JOIN cart c ON ci.cart_id = c.id
WHERE c.id IS NULL;

-- Check for products with zero or negative stock
SELECT 
    name,
    stock_quantity
FROM products
WHERE stock_quantity <= 0
ORDER BY stock_quantity;

-- Check for users with invalid email formats (basic check)
SELECT 
    username,
    email
FROM users
WHERE email NOT LIKE '%@%.%'
ORDER BY username;

-- Check for cart items exceeding business rule limits
SELECT 
    ci.id,
    ci.cart_id,
    ci.product_id,
    ci.quantity
FROM cart_items ci
WHERE ci.quantity > 10 OR ci.quantity <= 0
ORDER BY ci.quantity DESC;
