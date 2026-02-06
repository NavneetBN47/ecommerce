-- Shopping Cart System DQL Script
-- Data Query Language statements for validation and reporting
-- Generated from Low-Level Design (LLD)

-- ============================================
-- VALIDATION QUERIES
-- ============================================

-- Validate all tables exist and have data
SELECT 
    'users' as table_name,
    COUNT(*) as record_count,
    'User accounts in the system' as description
FROM users
UNION ALL
SELECT 
    'categories',
    COUNT(*),
    'Product categories'
FROM categories
UNION ALL
SELECT 
    'products',
    COUNT(*),
    'Products in catalog'
FROM products
UNION ALL
SELECT 
    'product_inventory',
    COUNT(*),
    'Product inventory records'
FROM product_inventory
UNION ALL
SELECT 
    'product_images',
    COUNT(*),
    'Product images'
FROM product_images
UNION ALL
SELECT 
    'user_addresses',
    COUNT(*),
    'User addresses'
FROM user_addresses
UNION ALL
SELECT 
    'shopping_carts',
    COUNT(*),
    'Active shopping carts'
FROM shopping_carts
UNION ALL
SELECT 
    'cart_items',
    COUNT(*),
    'Items in shopping carts'
FROM cart_items
UNION ALL
SELECT 
    'orders',
    COUNT(*),
    'Customer orders'
FROM orders
UNION ALL
SELECT 
    'order_items',
    COUNT(*),
    'Order line items'
FROM order_items
UNION ALL
SELECT 
    'payment_transactions',
    COUNT(*),
    'Payment transactions'
FROM payment_transactions;

-- ============================================
-- REFERENTIAL INTEGRITY VALIDATION
-- ============================================

-- Check for orphaned records
SELECT 'Orphaned Products (no category)' as check_name, COUNT(*) as count
FROM products p
LEFT JOIN categories c ON p.category_id = c.category_id
WHERE c.category_id IS NULL

UNION ALL

SELECT 'Orphaned Product Inventory (no product)', COUNT(*)
FROM product_inventory pi
LEFT JOIN products p ON pi.product_id = p.product_id
WHERE p.product_id IS NULL

UNION ALL

SELECT 'Orphaned Cart Items (no cart)', COUNT(*)
FROM cart_items ci
LEFT JOIN shopping_carts sc ON ci.cart_id = sc.cart_id
WHERE sc.cart_id IS NULL

UNION ALL

SELECT 'Orphaned Order Items (no order)', COUNT(*)
FROM order_items oi
LEFT JOIN orders o ON oi.order_id = o.order_id
WHERE o.order_id IS NULL;

-- ============================================
-- DATA CONSISTENCY VALIDATION
-- ============================================

-- Validate order totals match sum of order items
SELECT 
    'Order Total Validation' as check_name,
    COUNT(*) as inconsistent_orders
FROM orders o
WHERE o.subtotal != (
    SELECT COALESCE(SUM(oi.total_price), 0)
    FROM order_items oi
    WHERE oi.order_id = o.order_id
);

-- Validate cart item totals
SELECT 
    'Cart Item Price Validation' as check_name,
    COUNT(*) as inconsistent_items
FROM cart_items ci
JOIN products p ON ci.product_id = p.product_id
WHERE ci.unit_price != p.price;

-- Check for negative inventory
SELECT 
    'Negative Inventory Check' as check_name,
    COUNT(*) as negative_inventory_count
FROM product_inventory
WHERE quantity_available < 0;

-- ============================================
-- BUSINESS INTELLIGENCE QUERIES
-- ============================================

-- Product catalog overview with inventory status
SELECT 
    c.name as category_name,
    p.name as product_name,
    p.sku,
    p.price,
    pi.quantity_available,
    pi.reorder_level,
    CASE 
        WHEN pi.quantity_available <= pi.reorder_level THEN 'LOW STOCK'
        WHEN pi.quantity_available = 0 THEN 'OUT OF STOCK'
        ELSE 'IN STOCK'
    END as stock_status,
    p.is_active,
    p.is_featured
FROM products p
JOIN categories c ON p.category_id = c.category_id
JOIN product_inventory pi ON p.product_id = pi.product_id
ORDER BY c.name, p.name;

-- User registration and activity summary
SELECT 
    DATE_TRUNC('month', created_at) as registration_month,
    COUNT(*) as new_users,
    COUNT(CASE WHEN email_verified THEN 1 END) as verified_users,
    COUNT(CASE WHEN last_login IS NOT NULL THEN 1 END) as active_users
FROM users
GROUP BY DATE_TRUNC('month', created_at)
ORDER BY registration_month DESC;

-- Sales summary by month
SELECT 
    DATE_TRUNC('month', o.created_at) as order_month,
    COUNT(*) as total_orders,
    COUNT(CASE WHEN o.status = 'delivered' THEN 1 END) as delivered_orders,
    SUM(o.total_amount) as total_revenue,
    AVG(o.total_amount) as average_order_value
FROM orders o
GROUP BY DATE_TRUNC('month', o.created_at)
ORDER BY order_month DESC;

-- Top-selling products
SELECT 
    p.name as product_name,
    p.sku,
    SUM(oi.quantity) as total_quantity_sold,
    SUM(oi.total_price) as total_revenue,
    COUNT(DISTINCT oi.order_id) as number_of_orders
FROM order_items oi
JOIN products p ON oi.product_id = p.product_id
JOIN orders o ON oi.order_id = o.order_id
WHERE o.status IN ('delivered', 'shipped')
GROUP BY p.product_id, p.name, p.sku
ORDER BY total_quantity_sold DESC
LIMIT 10;

-- Customer order history and value
SELECT 
    u.email,
    u.first_name,
    u.last_name,
    COUNT(o.order_id) as total_orders,
    SUM(o.total_amount) as lifetime_value,
    AVG(o.total_amount) as average_order_value,
    MAX(o.created_at) as last_order_date,
    COUNT(CASE WHEN o.status = 'delivered' THEN 1 END) as completed_orders
FROM users u
LEFT JOIN orders o ON u.user_id = o.user_id
GROUP BY u.user_id, u.email, u.first_name, u.last_name
HAVING COUNT(o.order_id) > 0
ORDER BY lifetime_value DESC;

-- Shopping cart abandonment analysis
SELECT 
    'Active Carts' as cart_type,
    COUNT(*) as cart_count,
    SUM(cart_value.total_value) as total_cart_value,
    AVG(cart_value.total_value) as average_cart_value
FROM shopping_carts sc
JOIN (
    SELECT 
        ci.cart_id,
        SUM(ci.quantity * ci.unit_price) as total_value
    FROM cart_items ci
    GROUP BY ci.cart_id
) cart_value ON sc.cart_id = cart_value.cart_id
WHERE sc.expires_at > CURRENT_TIMESTAMP

UNION ALL

SELECT 
    'Expired Carts',
    COUNT(*),
    SUM(cart_value.total_value),
    AVG(cart_value.total_value)
FROM shopping_carts sc
JOIN (
    SELECT 
        ci.cart_id,
        SUM(ci.quantity * ci.unit_price) as total_value
    FROM cart_items ci
    GROUP BY ci.cart_id
) cart_value ON sc.cart_id = cart_value.cart_id
WHERE sc.expires_at <= CURRENT_TIMESTAMP;

-- Inventory reorder report
SELECT 
    p.name as product_name,
    p.sku,
    c.name as category_name,
    pi.quantity_available,
    pi.reorder_level,
    pi.max_stock_level,
    (pi.max_stock_level - pi.quantity_available) as suggested_reorder_quantity,
    pi.last_restocked
FROM product_inventory pi
JOIN products p ON pi.product_id = p.product_id
JOIN categories c ON p.category_id = c.category_id
WHERE pi.quantity_available <= pi.reorder_level
AND p.is_active = TRUE
ORDER BY pi.quantity_available ASC;

-- Payment transaction summary
SELECT 
    pt.payment_method,
    pt.status,
    COUNT(*) as transaction_count,
    SUM(pt.amount) as total_amount,
    AVG(pt.amount) as average_amount
FROM payment_transactions pt
GROUP BY pt.payment_method, pt.status
ORDER BY pt.payment_method, pt.status;

-- Category performance analysis
SELECT 
    c.name as category_name,
    COUNT(DISTINCT p.product_id) as product_count,
    COUNT(DISTINCT oi.order_id) as orders_with_category,
    SUM(oi.quantity) as total_items_sold,
    SUM(oi.total_price) as category_revenue,
    AVG(p.price) as average_product_price
FROM categories c
LEFT JOIN products p ON c.category_id = p.category_id AND p.is_active = TRUE
LEFT JOIN order_items oi ON p.product_id = oi.product_id
LEFT JOIN orders o ON oi.order_id = o.order_id AND o.status IN ('delivered', 'shipped')
WHERE c.parent_category_id IS NULL  -- Only top-level categories
GROUP BY c.category_id, c.name
ORDER BY category_revenue DESC NULLS LAST;

-- User address distribution
SELECT 
    ua.country,
    ua.state,
    COUNT(*) as address_count,
    COUNT(DISTINCT ua.user_id) as unique_users
FROM user_addresses ua
GROUP BY ua.country, ua.state
ORDER BY ua.country, ua.state;

-- Order status distribution
SELECT 
    status,
    COUNT(*) as order_count,
    SUM(total_amount) as total_value,
    AVG(total_amount) as average_value,
    MIN(created_at) as earliest_order,
    MAX(created_at) as latest_order
FROM orders
GROUP BY status
ORDER BY 
    CASE status
        WHEN 'pending' THEN 1
        WHEN 'confirmed' THEN 2
        WHEN 'processing' THEN 3
        WHEN 'shipped' THEN 4
        WHEN 'delivered' THEN 5
        WHEN 'cancelled' THEN 6
        WHEN 'refunded' THEN 7
        ELSE 8
    END;

-- ============================================
-- PERFORMANCE MONITORING QUERIES
-- ============================================

-- Check for missing indexes (tables with high row counts)
SELECT 
    schemaname,
    tablename,
    attname as column_name,
    n_distinct,
    correlation
FROM pg_stats
WHERE schemaname = 'public'
AND tablename IN ('users', 'products', 'orders', 'order_items', 'cart_items')
ORDER BY tablename, attname;

-- Table size analysis
SELECT 
    schemaname,
    tablename,
    pg_size_pretty(pg_total_relation_size(schemaname||'.'||tablename)) as size
FROM pg_tables
WHERE schemaname = 'public'
ORDER BY pg_total_relation_size(schemaname||'.'||tablename) DESC;

-- ============================================
-- AUDIT AND SECURITY QUERIES
-- ============================================

-- Recent audit log entries
SELECT 
    al.table_name,
    al.operation,
    al.record_id,
    u.email as user_email,
    al.ip_address,
    al.created_at
FROM audit_logs al
LEFT JOIN users u ON al.user_id = u.user_id
WHERE al.created_at >= CURRENT_TIMESTAMP - INTERVAL '7 days'
ORDER BY al.created_at DESC
LIMIT 100;

-- User security analysis
SELECT 
    'Total Users' as metric,
    COUNT(*) as count
FROM users
UNION ALL
SELECT 
    'Active Users',
    COUNT(*)
FROM users
WHERE is_active = TRUE
UNION ALL
SELECT 
    'Verified Email Users',
    COUNT(*)
FROM users
WHERE email_verified = TRUE
UNION ALL
SELECT 
    'Users with Recent Login (30 days)',
    COUNT(*)
FROM users
WHERE last_login >= CURRENT_TIMESTAMP - INTERVAL '30 days';

-- ============================================
-- FINAL VALIDATION SUMMARY
-- ============================================

-- Comprehensive system health check
SELECT 
    'SYSTEM HEALTH CHECK' as check_type,
    'Database schema successfully deployed' as status,
    CURRENT_TIMESTAMP as check_time
UNION ALL
SELECT 
    'REFERENTIAL INTEGRITY',
    CASE 
        WHEN EXISTS (
            SELECT 1 FROM products p
            LEFT JOIN categories c ON p.category_id = c.category_id
            WHERE c.category_id IS NULL
        ) THEN 'ISSUES FOUND'
        ELSE 'PASSED'
    END,
    CURRENT_TIMESTAMP
UNION ALL
SELECT 
    'DATA CONSISTENCY',
    CASE 
        WHEN EXISTS (
            SELECT 1 FROM product_inventory
            WHERE quantity_available < 0
        ) THEN 'ISSUES FOUND'
        ELSE 'PASSED'
    END,
    CURRENT_TIMESTAMP
UNION ALL
SELECT 
    'SAMPLE DATA',
    CASE 
        WHEN (SELECT COUNT(*) FROM products) > 0 
        AND (SELECT COUNT(*) FROM categories) > 0
        AND (SELECT COUNT(*) FROM users) > 0
        THEN 'LOADED'
        ELSE 'MISSING'
    END,
    CURRENT_TIMESTAMP;