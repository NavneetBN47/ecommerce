-- Data Query Language (DQL)
-- Cart Management System - Validation Queries
-- Generated from Low-Level Design (LLD) requirements

-- ============================================================================
-- VALIDATION QUERIES FOR ALL TABLES AND RELATIONSHIPS
-- ============================================================================

-- Query 1: Validate Users table structure and data
SELECT 'Users Table Validation' AS validation_type;
SELECT 
    COUNT(*) AS total_users,
    COUNT(CASE WHEN is_active = true THEN 1 END) AS active_users,
    COUNT(CASE WHEN email IS NOT NULL THEN 1 END) AS users_with_email,
    COUNT(CASE WHEN username IS NOT NULL THEN 1 END) AS users_with_username
FROM users;

-- Query 2: Validate Products table structure and data
SELECT 'Products Table Validation' AS validation_type;
SELECT 
    COUNT(*) AS total_products,
    COUNT(CASE WHEN is_active = true THEN 1 END) AS active_products,
    COUNT(CASE WHEN stock_quantity > 0 THEN 1 END) AS products_in_stock,
    COUNT(CASE WHEN price > 0 THEN 1 END) AS products_with_valid_price,
    AVG(price) AS average_price,
    SUM(stock_quantity) AS total_stock
FROM products;

-- Query 3: Validate Cart table structure and relationships
SELECT 'Cart Table Validation' AS validation_type;
SELECT 
    COUNT(*) AS total_carts,
    COUNT(CASE WHEN cart_status = 'active' THEN 1 END) AS active_carts,
    COUNT(CASE WHEN cart_status = 'abandoned' THEN 1 END) AS abandoned_carts,
    COUNT(CASE WHEN cart_status = 'converted' THEN 1 END) AS converted_carts,
    COUNT(CASE WHEN cart_status = 'expired' THEN 1 END) AS expired_carts,
    AVG(total_amount) AS average_cart_value,
    AVG(total_items) AS average_items_per_cart
FROM cart;

-- Query 4: Validate CartItem table and relationships
SELECT 'CartItem Table Validation' AS validation_type;
SELECT 
    COUNT(*) AS total_cart_items,
    COUNT(DISTINCT cart_id) AS carts_with_items,
    COUNT(DISTINCT product_id) AS unique_products_in_carts,
    SUM(quantity) AS total_quantity_across_all_carts,
    SUM(total_price) AS total_value_all_cart_items,
    AVG(quantity) AS average_quantity_per_item,
    AVG(unit_price) AS average_unit_price
FROM cart_item;

-- Query 5: Validate Cart History table
SELECT 'Cart History Validation' AS validation_type;
SELECT 
    COUNT(*) AS total_history_records,
    COUNT(DISTINCT cart_id) AS carts_with_history,
    COUNT(CASE WHEN action_type = 'created' THEN 1 END) AS cart_creation_events,
    COUNT(CASE WHEN action_type = 'item_added' THEN 1 END) AS item_added_events,
    COUNT(CASE WHEN action_type = 'item_removed' THEN 1 END) AS item_removed_events,
    COUNT(CASE WHEN action_type = 'status_changed' THEN 1 END) AS status_change_events
FROM cart_history;

-- ============================================================================
-- RELATIONSHIP VALIDATION QUERIES
-- ============================================================================

-- Query 6: Validate Cart-User relationship
SELECT 'Cart-User Relationship Validation' AS validation_type;
SELECT 
    u.username,
    u.email,
    COUNT(c.cart_id) AS total_carts,
    COUNT(CASE WHEN c.cart_status = 'active' THEN 1 END) AS active_carts,
    COALESCE(SUM(c.total_amount), 0) AS total_cart_value
FROM users u
LEFT JOIN cart c ON u.user_id = c.user_id
GROUP BY u.user_id, u.username, u.email
ORDER BY total_cart_value DESC;

-- Query 7: Validate CartItem-Cart relationship
SELECT 'CartItem-Cart Relationship Validation' AS validation_type;
SELECT 
    c.cart_id,
    c.cart_status,
    c.total_amount AS cart_total_amount,
    c.total_items AS cart_total_items,
    COUNT(ci.cart_item_id) AS actual_item_count,
    COALESCE(SUM(ci.total_price), 0) AS calculated_total_amount,
    COALESCE(SUM(ci.quantity), 0) AS calculated_total_items,
    CASE 
        WHEN c.total_amount = COALESCE(SUM(ci.total_price), 0) THEN 'MATCH'
        ELSE 'MISMATCH'
    END AS amount_validation,
    CASE 
        WHEN c.total_items = COALESCE(SUM(ci.quantity), 0) THEN 'MATCH'
        ELSE 'MISMATCH'
    END AS items_validation
FROM cart c
LEFT JOIN cart_item ci ON c.cart_id = ci.cart_id
GROUP BY c.cart_id, c.cart_status, c.total_amount, c.total_items
ORDER BY c.cart_id;

-- Query 8: Validate CartItem-Product relationship
SELECT 'CartItem-Product Relationship Validation' AS validation_type;
SELECT 
    p.product_name,
    p.sku,
    p.price AS current_price,
    p.stock_quantity,
    COUNT(ci.cart_item_id) AS times_in_cart,
    SUM(ci.quantity) AS total_quantity_in_carts,
    AVG(ci.unit_price) AS average_cart_price,
    CASE 
        WHEN p.price = AVG(ci.unit_price) THEN 'PRICE_MATCH'
        ELSE 'PRICE_VARIATION'
    END AS price_consistency
FROM products p
LEFT JOIN cart_item ci ON p.product_id = ci.product_id
GROUP BY p.product_id, p.product_name, p.sku, p.price, p.stock_quantity
HAVING COUNT(ci.cart_item_id) > 0
ORDER BY total_quantity_in_carts DESC;

-- ============================================================================
-- DATA INTEGRITY AND CONSTRAINT VALIDATION
-- ============================================================================

-- Query 9: Check for orphaned records
SELECT 'Orphaned Records Check' AS validation_type;

-- Check for carts without valid users
SELECT 'Carts without valid users' AS check_type, COUNT(*) AS count
FROM cart c
LEFT JOIN users u ON c.user_id = u.user_id
WHERE u.user_id IS NULL

UNION ALL

-- Check for cart items without valid carts
SELECT 'Cart items without valid carts' AS check_type, COUNT(*) AS count
FROM cart_item ci
LEFT JOIN cart c ON ci.cart_id = c.cart_id
WHERE c.cart_id IS NULL

UNION ALL

-- Check for cart items without valid products
SELECT 'Cart items without valid products' AS check_type, COUNT(*) AS count
FROM cart_item ci
LEFT JOIN products p ON ci.product_id = p.product_id
WHERE p.product_id IS NULL

UNION ALL

-- Check for cart history without valid carts
SELECT 'Cart history without valid carts' AS check_type, COUNT(*) AS count
FROM cart_history ch
LEFT JOIN cart c ON ch.cart_id = c.cart_id
WHERE c.cart_id IS NULL;

-- Query 10: Validate business rules and constraints
SELECT 'Business Rules Validation' AS validation_type;

-- Check for negative quantities
SELECT 'Items with negative quantity' AS rule_check, COUNT(*) AS violations
FROM cart_item
WHERE quantity <= 0

UNION ALL

-- Check for negative prices
SELECT 'Items with negative unit price' AS rule_check, COUNT(*) AS violations
FROM cart_item
WHERE unit_price < 0

UNION ALL

-- Check for products with negative stock
SELECT 'Products with negative stock' AS rule_check, COUNT(*) AS violations
FROM products
WHERE stock_quantity < 0

UNION ALL

-- Check for products with negative price
SELECT 'Products with negative price' AS rule_check, COUNT(*) AS violations
FROM products
WHERE price < 0

UNION ALL

-- Check for carts with invalid status
SELECT 'Carts with invalid status' AS rule_check, COUNT(*) AS violations
FROM cart
WHERE cart_status NOT IN ('active', 'abandoned', 'converted', 'expired');

-- ============================================================================
-- PERFORMANCE AND INDEX VALIDATION
-- ============================================================================

-- Query 11: Check index usage and performance
SELECT 'Index Usage Validation' AS validation_type;
SELECT 
    schemaname,
    tablename,
    indexname,
    idx_scan AS index_scans,
    idx_tup_read AS tuples_read,
    idx_tup_fetch AS tuples_fetched
FROM pg_stat_user_indexes
WHERE schemaname = 'public'
  AND tablename IN ('cart', 'cart_item', 'cart_history', 'users', 'products')
ORDER BY tablename, indexname;

-- ============================================================================
-- SUMMARY VALIDATION REPORT
-- ============================================================================

-- Query 12: Overall system health summary
SELECT 'System Health Summary' AS validation_type;
SELECT 
    'Total Users' AS metric,
    COUNT(*)::TEXT AS value
FROM users

UNION ALL

SELECT 
    'Total Products' AS metric,
    COUNT(*)::TEXT AS value
FROM products

UNION ALL

SELECT 
    'Total Active Carts' AS metric,
    COUNT(*)::TEXT AS value
FROM cart
WHERE cart_status = 'active'

UNION ALL

SELECT 
    'Total Cart Items' AS metric,
    COUNT(*)::TEXT AS value
FROM cart_item

UNION ALL

SELECT 
    'Total Cart Value (USD)' AS metric,
    ROUND(SUM(total_amount), 2)::TEXT AS value
FROM cart
WHERE cart_status = 'active'

UNION ALL

SELECT 
    'Average Cart Value (USD)' AS metric,
    ROUND(AVG(total_amount), 2)::TEXT AS value
FROM cart
WHERE cart_status = 'active' AND total_amount > 0

UNION ALL

SELECT 
    'Total History Records' AS metric,
    COUNT(*)::TEXT AS value
FROM cart_history;

-- Query 13: Data consistency final check
SELECT 'Final Consistency Check' AS validation_type;
WITH consistency_check AS (
    SELECT 
        c.cart_id,
        c.total_amount AS stored_amount,
        c.total_items AS stored_items,
        COALESCE(SUM(ci.total_price), 0) AS calculated_amount,
        COALESCE(SUM(ci.quantity), 0) AS calculated_items
    FROM cart c
    LEFT JOIN cart_item ci ON c.cart_id = ci.cart_id
    GROUP BY c.cart_id, c.total_amount, c.total_items
)
SELECT 
    COUNT(*) AS total_carts_checked,
    COUNT(CASE WHEN stored_amount = calculated_amount THEN 1 END) AS amount_consistent_carts,
    COUNT(CASE WHEN stored_items = calculated_items THEN 1 END) AS items_consistent_carts,
    COUNT(CASE WHEN stored_amount != calculated_amount THEN 1 END) AS amount_inconsistent_carts,
    COUNT(CASE WHEN stored_items != calculated_items THEN 1 END) AS items_inconsistent_carts
FROM consistency_check;

-- End of validation queries
SELECT 'Validation Complete' AS status, CURRENT_TIMESTAMP AS completed_at;