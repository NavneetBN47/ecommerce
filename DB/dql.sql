-- Shopping Cart System - Data Query Language (DQL)
-- Validation queries for tables and relationships
-- Version: 1.0
-- Date: $(date)

-- =====================================================
-- TABLE VALIDATION QUERIES
-- =====================================================

-- Query 1: Validate Users table structure and data
SELECT 
    'Users Table Validation' as validation_type,
    COUNT(*) as total_users,
    COUNT(CASE WHEN is_active = true THEN 1 END) as active_users,
    COUNT(CASE WHEN email IS NOT NULL AND email != '' THEN 1 END) as users_with_email,
    COUNT(CASE WHEN phone IS NOT NULL THEN 1 END) as users_with_phone,
    MIN(created_at) as earliest_user,
    MAX(created_at) as latest_user
FROM users;

-- Query 2: Validate Products table structure and data
SELECT 
    'Products Table Validation' as validation_type,
    COUNT(*) as total_products,
    COUNT(CASE WHEN is_active = true THEN 1 END) as active_products,
    COUNT(CASE WHEN stock_quantity > 0 THEN 1 END) as products_in_stock,
    COUNT(CASE WHEN stock_quantity = 0 THEN 1 END) as out_of_stock_products,
    AVG(price) as average_price,
    MIN(price) as min_price,
    MAX(price) as max_price,
    COUNT(DISTINCT category) as unique_categories
FROM products;

-- Query 3: Validate Carts table structure and data
SELECT 
    'Carts Table Validation' as validation_type,
    COUNT(*) as total_carts,
    COUNT(CASE WHEN status = 'active' THEN 1 END) as active_carts,
    COUNT(CASE WHEN status = 'abandoned' THEN 1 END) as abandoned_carts,
    COUNT(CASE WHEN status = 'expired' THEN 1 END) as expired_carts,
    AVG(total_amount) as average_cart_value,
    AVG(item_count) as average_items_per_cart
FROM carts;

-- Query 4: Validate Cart Items table structure and data
SELECT 
    'Cart Items Table Validation' as validation_type,
    COUNT(*) as total_cart_items,
    SUM(quantity) as total_quantity_in_carts,
    AVG(quantity) as average_quantity_per_item,
    COUNT(DISTINCT cart_id) as carts_with_items,
    COUNT(DISTINCT product_id) as unique_products_in_carts
FROM cart_items;

-- Query 5: Validate User Addresses table
SELECT 
    'User Addresses Table Validation' as validation_type,
    COUNT(*) as total_addresses,
    COUNT(CASE WHEN is_default = true THEN 1 END) as default_addresses,
    COUNT(CASE WHEN address_type = 'shipping' THEN 1 END) as shipping_addresses,
    COUNT(CASE WHEN address_type = 'billing' THEN 1 END) as billing_addresses,
    COUNT(DISTINCT user_id) as users_with_addresses
FROM user_addresses;

-- =====================================================
-- RELATIONSHIP VALIDATION QUERIES
-- =====================================================

-- Query 6: Validate User-Cart relationship
SELECT 
    'User-Cart Relationship' as validation_type,
    u.username,
    COUNT(c.cart_id) as total_carts,
    COUNT(CASE WHEN c.status = 'active' THEN 1 END) as active_carts,
    COALESCE(SUM(c.total_amount), 0) as total_cart_value
FROM users u
LEFT JOIN carts c ON u.user_id = c.user_id
GROUP BY u.user_id, u.username
ORDER BY total_cart_value DESC;

-- Query 7: Validate Cart-CartItem relationship and totals
SELECT 
    'Cart-CartItem Relationship' as validation_type,
    c.cart_id,
    c.status,
    c.total_amount as cart_total,
    c.item_count as cart_item_count,
    COALESCE(SUM(ci.total_price), 0) as calculated_total,
    COALESCE(SUM(ci.quantity), 0) as calculated_item_count,
    CASE 
        WHEN c.total_amount = COALESCE(SUM(ci.total_price), 0) THEN 'MATCH'
        ELSE 'MISMATCH'
    END as total_validation,
    CASE 
        WHEN c.item_count = COALESCE(SUM(ci.quantity), 0) THEN 'MATCH'
        ELSE 'MISMATCH'
    END as count_validation
FROM carts c
LEFT JOIN cart_items ci ON c.cart_id = ci.cart_id
GROUP BY c.cart_id, c.status, c.total_amount, c.item_count
ORDER BY c.created_at DESC;

-- Query 8: Validate Product-CartItem relationship
SELECT 
    'Product-CartItem Relationship' as validation_type,
    p.name as product_name,
    p.sku,
    p.price as current_price,
    p.stock_quantity,
    COUNT(ci.cart_item_id) as times_in_cart,
    SUM(ci.quantity) as total_quantity_in_carts,
    AVG(ci.unit_price) as average_cart_price,
    CASE 
        WHEN p.price = AVG(ci.unit_price) THEN 'MATCH'
        ELSE 'PRICE_CHANGED'
    END as price_validation
FROM products p
LEFT JOIN cart_items ci ON p.product_id = ci.product_id
GROUP BY p.product_id, p.name, p.sku, p.price, p.stock_quantity
HAVING COUNT(ci.cart_item_id) > 0
ORDER BY total_quantity_in_carts DESC;

-- Query 9: Validate User-Address relationship
SELECT 
    'User-Address Relationship' as validation_type,
    u.username,
    u.email,
    COUNT(ua.address_id) as total_addresses,
    COUNT(CASE WHEN ua.address_type = 'shipping' THEN 1 END) as shipping_addresses,
    COUNT(CASE WHEN ua.address_type = 'billing' THEN 1 END) as billing_addresses,
    COUNT(CASE WHEN ua.is_default = true THEN 1 END) as default_addresses,
    CASE 
        WHEN COUNT(CASE WHEN ua.is_default = true THEN 1 END) = 1 THEN 'VALID'
        WHEN COUNT(CASE WHEN ua.is_default = true THEN 1 END) = 0 THEN 'NO_DEFAULT'
        ELSE 'MULTIPLE_DEFAULTS'
    END as default_validation
FROM users u
LEFT JOIN user_addresses ua ON u.user_id = ua.user_id
GROUP BY u.user_id, u.username, u.email
ORDER BY u.username;

-- =====================================================
-- BUSINESS LOGIC VALIDATION QUERIES
-- =====================================================

-- Query 10: Cart abandonment analysis
SELECT 
    'Cart Abandonment Analysis' as analysis_type,
    status,
    COUNT(*) as cart_count,
    AVG(total_amount) as avg_cart_value,
    AVG(item_count) as avg_items,
    AVG(EXTRACT(EPOCH FROM (CURRENT_TIMESTAMP - created_at))/3600) as avg_age_hours
FROM carts
GROUP BY status
ORDER BY cart_count DESC;

-- Query 11: Product popularity and stock analysis
SELECT 
    'Product Popularity Analysis' as analysis_type,
    p.name,
    p.category,
    p.price,
    p.stock_quantity,
    COALESCE(SUM(ci.quantity), 0) as total_in_carts,
    COUNT(DISTINCT ci.cart_id) as unique_carts,
    CASE 
        WHEN p.stock_quantity = 0 THEN 'OUT_OF_STOCK'
        WHEN p.stock_quantity < 10 THEN 'LOW_STOCK'
        WHEN p.stock_quantity < 50 THEN 'MEDIUM_STOCK'
        ELSE 'HIGH_STOCK'
    END as stock_level
FROM products p
LEFT JOIN cart_items ci ON p.product_id = ci.product_id
WHERE p.is_active = true
GROUP BY p.product_id, p.name, p.category, p.price, p.stock_quantity
ORDER BY total_in_carts DESC, unique_carts DESC;

-- Query 12: User engagement analysis
SELECT 
    'User Engagement Analysis' as analysis_type,
    u.username,
    u.created_at as user_since,
    COUNT(DISTINCT c.cart_id) as total_carts_created,
    COUNT(DISTINCT CASE WHEN c.status = 'active' THEN c.cart_id END) as active_carts,
    COALESCE(SUM(c.total_amount), 0) as total_cart_value,
    COALESCE(MAX(c.updated_at), u.created_at) as last_cart_activity,
    EXTRACT(EPOCH FROM (CURRENT_TIMESTAMP - COALESCE(MAX(c.updated_at), u.created_at)))/86400 as days_since_last_activity
FROM users u
LEFT JOIN carts c ON u.user_id = c.user_id
WHERE u.is_active = true
GROUP BY u.user_id, u.username, u.created_at
ORDER BY total_cart_value DESC;

-- =====================================================
-- DATA INTEGRITY VALIDATION QUERIES
-- =====================================================

-- Query 13: Check for orphaned records
SELECT 
    'Orphaned Records Check' as validation_type,
    'cart_items_without_cart' as issue_type,
    COUNT(*) as count
FROM cart_items ci
WHERE NOT EXISTS (SELECT 1 FROM carts c WHERE c.cart_id = ci.cart_id)

UNION ALL

SELECT 
    'Orphaned Records Check',
    'cart_items_without_product',
    COUNT(*)
FROM cart_items ci
WHERE NOT EXISTS (SELECT 1 FROM products p WHERE p.product_id = ci.product_id)

UNION ALL

SELECT 
    'Orphaned Records Check',
    'carts_without_user',
    COUNT(*)
FROM carts c
WHERE NOT EXISTS (SELECT 1 FROM users u WHERE u.user_id = c.user_id)

UNION ALL

SELECT 
    'Orphaned Records Check',
    'addresses_without_user',
    COUNT(*)
FROM user_addresses ua
WHERE NOT EXISTS (SELECT 1 FROM users u WHERE u.user_id = ua.user_id);

-- Query 14: Check for constraint violations
SELECT 
    'Constraint Violations Check' as validation_type,
    'negative_prices' as issue_type,
    COUNT(*) as count
FROM products
WHERE price <= 0

UNION ALL

SELECT 
    'Constraint Violations Check',
    'negative_stock',
    COUNT(*)
FROM products
WHERE stock_quantity < 0

UNION ALL

SELECT 
    'Constraint Violations Check',
    'negative_cart_totals',
    COUNT(*)
FROM carts
WHERE total_amount < 0

UNION ALL

SELECT 
    'Constraint Violations Check',
    'zero_quantity_cart_items',
    COUNT(*)
FROM cart_items
WHERE quantity <= 0;

-- Query 15: Check for duplicate records
SELECT 
    'Duplicate Records Check' as validation_type,
    'duplicate_emails' as issue_type,
    COUNT(*) - COUNT(DISTINCT email) as count
FROM users

UNION ALL

SELECT 
    'Duplicate Records Check',
    'duplicate_usernames',
    COUNT(*) - COUNT(DISTINCT username)
FROM users

UNION ALL

SELECT 
    'Duplicate Records Check',
    'duplicate_skus',
    COUNT(*) - COUNT(DISTINCT sku)
FROM products

UNION ALL

SELECT 
    'Duplicate Records Check',
    'duplicate_cart_product_combinations',
    COUNT(*) - COUNT(DISTINCT (cart_id, product_id))
FROM cart_items;

-- =====================================================
-- PERFORMANCE VALIDATION QUERIES
-- =====================================================

-- Query 16: Index usage validation (explain plans)
EXPLAIN (ANALYZE, BUFFERS) 
SELECT u.username, c.total_amount 
FROM users u 
JOIN carts c ON u.user_id = c.user_id 
WHERE u.email = 'john.doe@example.com';

EXPLAIN (ANALYZE, BUFFERS)
SELECT p.name, p.price 
FROM products p 
WHERE p.category = 'Electronics' AND p.is_active = true;

EXPLAIN (ANALYZE, BUFFERS)
SELECT ci.quantity, p.name 
FROM cart_items ci 
JOIN products p ON ci.product_id = p.product_id 
WHERE ci.cart_id = (SELECT cart_id FROM carts LIMIT 1);

-- =====================================================
-- SUMMARY VALIDATION REPORT
-- =====================================================

-- Query 17: Overall system health summary
WITH system_stats AS (
    SELECT 
        'users' as table_name,
        COUNT(*) as record_count,
        COUNT(CASE WHEN is_active = true THEN 1 END) as active_count
    FROM users
    
    UNION ALL
    
    SELECT 
        'products',
        COUNT(*),
        COUNT(CASE WHEN is_active = true THEN 1 END)
    FROM products
    
    UNION ALL
    
    SELECT 
        'carts',
        COUNT(*),
        COUNT(CASE WHEN status = 'active' THEN 1 END)
    FROM carts
    
    UNION ALL
    
    SELECT 
        'cart_items',
        COUNT(*),
        COUNT(*)
    FROM cart_items
    
    UNION ALL
    
    SELECT 
        'user_addresses',
        COUNT(*),
        COUNT(CASE WHEN is_default = true THEN 1 END)
    FROM user_addresses
)
SELECT 
    'System Health Summary' as report_type,
    table_name,
    record_count,
    active_count,
    ROUND((active_count::DECIMAL / NULLIF(record_count, 0)) * 100, 2) as active_percentage
FROM system_stats
ORDER BY record_count DESC;

-- Query 18: Final validation status
SELECT 
    'Final Validation Status' as status_type,
    CASE 
        WHEN EXISTS (SELECT 1 FROM users WHERE email IS NULL OR email = '') THEN 'FAILED: Users with invalid emails'
        WHEN EXISTS (SELECT 1 FROM products WHERE price <= 0) THEN 'FAILED: Products with invalid prices'
        WHEN EXISTS (SELECT 1 FROM carts WHERE total_amount < 0) THEN 'FAILED: Carts with negative totals'
        WHEN EXISTS (SELECT 1 FROM cart_items WHERE quantity <= 0) THEN 'FAILED: Cart items with invalid quantities'
        ELSE 'PASSED: All validations successful'
    END as validation_result,
    CURRENT_TIMESTAMP as validation_timestamp;

-- DQL validation queries complete
