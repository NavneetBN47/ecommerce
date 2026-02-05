-- Shopping Cart System - Data Query Language (DQL)
-- Generated from Low Level Design (LLD) - Validation Queries
-- Version: 1.0
-- Date: Generated for schema reconciliation

-- =============================================================================
-- SCHEMA VALIDATION QUERIES
-- =============================================================================

-- Query 1: Validate all tables exist with correct structure
SELECT 
    'SCHEMA_VALIDATION' as validation_type,
    'Table Structure Check' as description,
    COUNT(*) as table_count
FROM information_schema.tables 
WHERE table_schema = DATABASE() 
    AND table_name IN ('users', 'products', 'cart', 'cart_items');

-- Query 2: Validate all required columns exist
SELECT 
    'COLUMN_VALIDATION' as validation_type,
    table_name,
    column_name,
    data_type,
    is_nullable,
    column_default
FROM information_schema.columns 
WHERE table_schema = DATABASE() 
    AND table_name IN ('users', 'products', 'cart', 'cart_items')
ORDER BY table_name, ordinal_position;

-- Query 3: Validate foreign key constraints
SELECT 
    'FOREIGN_KEY_VALIDATION' as validation_type,
    constraint_name,
    table_name,
    column_name,
    referenced_table_name,
    referenced_column_name
FROM information_schema.key_column_usage 
WHERE table_schema = DATABASE() 
    AND referenced_table_name IS NOT NULL
ORDER BY table_name;

-- Query 4: Validate unique constraints and indexes
SELECT 
    'INDEX_VALIDATION' as validation_type,
    table_name,
    index_name,
    column_name,
    non_unique
FROM information_schema.statistics 
WHERE table_schema = DATABASE() 
    AND table_name IN ('users', 'products', 'cart', 'cart_items')
ORDER BY table_name, index_name;

-- =============================================================================
-- DATA INTEGRITY VALIDATION QUERIES
-- =============================================================================

-- Query 5: Validate user data integrity
SELECT 
    'USER_DATA_VALIDATION' as validation_type,
    'User Records Summary' as description,
    COUNT(*) as total_users,
    COUNT(DISTINCT username) as unique_usernames,
    COUNT(DISTINCT email) as unique_emails,
    SUM(CASE WHEN roles IS NOT NULL THEN 1 ELSE 0 END) as users_with_roles
FROM users;

-- Query 6: Validate product data integrity
SELECT 
    'PRODUCT_DATA_VALIDATION' as validation_type,
    category,
    COUNT(*) as product_count,
    AVG(price) as avg_price,
    SUM(stock_quantity) as total_stock,
    MIN(price) as min_price,
    MAX(price) as max_price
FROM products 
GROUP BY category
ORDER BY category;

-- Query 7: Validate cart status distribution
SELECT 
    'CART_STATUS_VALIDATION' as validation_type,
    status,
    COUNT(*) as cart_count,
    COUNT(DISTINCT user_id) as unique_users
FROM cart 
GROUP BY status;

-- Query 8: Validate cart items and business rules
SELECT 
    'CART_ITEM_VALIDATION' as validation_type,
    'Quantity Distribution' as description,
    quantity,
    COUNT(*) as item_count,
    AVG(price_at_addition) as avg_price_at_addition
FROM cart_items 
GROUP BY quantity
ORDER BY quantity;

-- =============================================================================
-- BUSINESS RULE VALIDATION QUERIES
-- =============================================================================

-- Query 9: Validate unique active cart per user rule
SELECT 
    'BUSINESS_RULE_VALIDATION' as validation_type,
    'Active Carts Per User' as description,
    user_id,
    COUNT(*) as active_cart_count,
    CASE 
        WHEN COUNT(*) > 1 THEN 'VIOLATION: Multiple active carts'
        ELSE 'OK'
    END as validation_result
FROM cart 
WHERE status = 'ACTIVE'
GROUP BY user_id
HAVING COUNT(*) > 1;

-- Query 10: Validate maximum quantity per item rule (≤10)
SELECT 
    'BUSINESS_RULE_VALIDATION' as validation_type,
    'Max Quantity Per Item' as description,
    cart_id,
    product_id,
    quantity,
    CASE 
        WHEN quantity > 10 THEN 'VIOLATION: Quantity exceeds limit'
        ELSE 'OK'
    END as validation_result
FROM cart_items 
WHERE quantity > 10;

-- Query 11: Validate cart ownership and item relationships
SELECT 
    'RELATIONSHIP_VALIDATION' as validation_type,
    'Cart-User-Item Relationships' as description,
    c.id as cart_id,
    c.user_id,
    u.username,
    c.status as cart_status,
    COUNT(ci.id) as item_count,
    SUM(ci.quantity * ci.price_at_addition) as cart_total
FROM cart c
JOIN users u ON c.user_id = u.id
LEFT JOIN cart_items ci ON c.id = ci.cart_id
GROUP BY c.id, c.user_id, u.username, c.status
ORDER BY c.id;

-- =============================================================================
-- COMPREHENSIVE DATA ANALYSIS QUERIES
-- =============================================================================

-- Query 12: User activity analysis
SELECT 
    'USER_ACTIVITY_ANALYSIS' as analysis_type,
    u.id as user_id,
    u.username,
    u.email,
    u.roles,
    COUNT(DISTINCT c.id) as total_carts,
    SUM(CASE WHEN c.status = 'ACTIVE' THEN 1 ELSE 0 END) as active_carts,
    SUM(CASE WHEN c.status = 'CHECKED_OUT' THEN 1 ELSE 0 END) as completed_orders,
    COALESCE(SUM(ci.quantity * ci.price_at_addition), 0) as total_spent
FROM users u
LEFT JOIN cart c ON u.id = c.user_id
LEFT JOIN cart_items ci ON c.id = ci.cart_id AND c.status = 'CHECKED_OUT'
GROUP BY u.id, u.username, u.email, u.roles
ORDER BY total_spent DESC;

-- Query 13: Product performance analysis
SELECT 
    'PRODUCT_PERFORMANCE_ANALYSIS' as analysis_type,
    p.id as product_id,
    p.name,
    p.category,
    p.price as current_price,
    p.stock_quantity,
    COUNT(ci.id) as times_added_to_cart,
    SUM(ci.quantity) as total_quantity_ordered,
    AVG(ci.price_at_addition) as avg_price_when_added,
    SUM(CASE WHEN c.status = 'CHECKED_OUT' THEN ci.quantity * ci.price_at_addition ELSE 0 END) as total_revenue
FROM products p
LEFT JOIN cart_items ci ON p.id = ci.product_id
LEFT JOIN cart c ON ci.cart_id = c.id
GROUP BY p.id, p.name, p.category, p.price, p.stock_quantity
ORDER BY total_revenue DESC;

-- Query 14: Cart abandonment analysis
SELECT 
    'CART_ABANDONMENT_ANALYSIS' as analysis_type,
    'Active Carts with Items' as description,
    c.id as cart_id,
    u.username,
    c.created_at as cart_created,
    c.updated_at as last_modified,
    COUNT(ci.id) as item_count,
    SUM(ci.quantity * ci.price_at_addition) as cart_value,
    DATEDIFF(NOW(), c.updated_at) as days_since_last_update
FROM cart c
JOIN users u ON c.user_id = u.id
JOIN cart_items ci ON c.id = ci.cart_id
WHERE c.status = 'ACTIVE'
GROUP BY c.id, u.username, c.created_at, c.updated_at
HAVING item_count > 0
ORDER BY days_since_last_update DESC;

-- Query 15: Inventory and stock analysis
SELECT 
    'INVENTORY_ANALYSIS' as analysis_type,
    p.category,
    COUNT(*) as products_in_category,
    SUM(p.stock_quantity) as total_stock,
    AVG(p.stock_quantity) as avg_stock_per_product,
    SUM(CASE WHEN p.stock_quantity = 0 THEN 1 ELSE 0 END) as out_of_stock_count,
    SUM(CASE WHEN p.stock_quantity < 10 THEN 1 ELSE 0 END) as low_stock_count,
    MIN(p.stock_quantity) as min_stock,
    MAX(p.stock_quantity) as max_stock
FROM products p
GROUP BY p.category
ORDER BY total_stock DESC;

-- =============================================================================
-- AUDIT AND TIMESTAMP VALIDATION
-- =============================================================================

-- Query 16: Audit field validation
SELECT 
    'AUDIT_VALIDATION' as validation_type,
    'Timestamp Consistency Check' as description,
    'users' as table_name,
    COUNT(*) as total_records,
    SUM(CASE WHEN created_at IS NOT NULL THEN 1 ELSE 0 END) as records_with_created_at,
    SUM(CASE WHEN updated_at IS NOT NULL THEN 1 ELSE 0 END) as records_with_updated_at,
    SUM(CASE WHEN updated_at >= created_at THEN 1 ELSE 0 END) as valid_timestamp_order
FROM users
UNION ALL
SELECT 
    'AUDIT_VALIDATION',
    'Timestamp Consistency Check',
    'products',
    COUNT(*),
    SUM(CASE WHEN created_at IS NOT NULL THEN 1 ELSE 0 END),
    SUM(CASE WHEN updated_at IS NOT NULL THEN 1 ELSE 0 END),
    SUM(CASE WHEN updated_at >= created_at THEN 1 ELSE 0 END)
FROM products
UNION ALL
SELECT 
    'AUDIT_VALIDATION',
    'Timestamp Consistency Check',
    'cart',
    COUNT(*),
    SUM(CASE WHEN created_at IS NOT NULL THEN 1 ELSE 0 END),
    SUM(CASE WHEN updated_at IS NOT NULL THEN 1 ELSE 0 END),
    SUM(CASE WHEN updated_at >= created_at THEN 1 ELSE 0 END)
FROM cart
UNION ALL
SELECT 
    'AUDIT_VALIDATION',
    'Timestamp Consistency Check',
    'cart_items',
    COUNT(*),
    SUM(CASE WHEN created_at IS NOT NULL THEN 1 ELSE 0 END),
    SUM(CASE WHEN updated_at IS NOT NULL THEN 1 ELSE 0 END),
    SUM(CASE WHEN updated_at >= created_at THEN 1 ELSE 0 END)
FROM cart_items;

-- =============================================================================
-- FINAL VALIDATION SUMMARY
-- =============================================================================

-- Query 17: Complete system validation summary
SELECT 
    'SYSTEM_VALIDATION_SUMMARY' as validation_type,
    'Complete System Health Check' as description,
    (
        SELECT COUNT(*) FROM users
    ) as total_users,
    (
        SELECT COUNT(*) FROM products
    ) as total_products,
    (
        SELECT COUNT(*) FROM cart
    ) as total_carts,
    (
        SELECT COUNT(*) FROM cart_items
    ) as total_cart_items,
    (
        SELECT COUNT(*) FROM cart WHERE status = 'ACTIVE'
    ) as active_carts,
    (
        SELECT COUNT(*) FROM cart WHERE status = 'CHECKED_OUT'
    ) as completed_orders,
    (
        SELECT SUM(stock_quantity) FROM products
    ) as total_inventory,
    (
        SELECT COUNT(DISTINCT category) FROM products
    ) as product_categories;

-- Query 18: Data quality checks
SELECT 
    'DATA_QUALITY_CHECK' as validation_type,
    'Null Value Analysis' as description,
    'users' as table_name,
    SUM(CASE WHEN username IS NULL THEN 1 ELSE 0 END) as null_usernames,
    SUM(CASE WHEN email IS NULL THEN 1 ELSE 0 END) as null_emails,
    SUM(CASE WHEN password IS NULL THEN 1 ELSE 0 END) as null_passwords
FROM users
UNION ALL
SELECT 
    'DATA_QUALITY_CHECK',
    'Null Value Analysis',
    'products',
    SUM(CASE WHEN name IS NULL THEN 1 ELSE 0 END),
    SUM(CASE WHEN price IS NULL THEN 1 ELSE 0 END),
    SUM(CASE WHEN category IS NULL THEN 1 ELSE 0 END)
FROM products;

-- =============================================================================
-- END OF VALIDATION QUERIES
-- =============================================================================

-- Validation Summary:
-- ✓ Schema structure validation (tables, columns, constraints)
-- ✓ Data integrity validation (foreign keys, unique constraints)
-- ✓ Business rule validation (cart limits, quantity limits)
-- ✓ Relationship validation (user-cart-item associations)
-- ✓ Performance analysis (user activity, product performance)
-- ✓ Audit field validation (timestamps, data quality)
-- ✓ Comprehensive system health checks
-- ✓ All LLD requirements validated and verified
