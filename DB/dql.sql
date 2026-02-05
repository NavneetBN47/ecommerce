-- Shopping Cart System Database Query Language (DQL)
-- Generated from Low-Level Design (LLD) as authoritative source
-- This script contains optimized queries for the shopping cart system

USE ecommerce_db;

-- =====================================================
-- PRODUCT QUERIES
-- =====================================================

-- Get all active products with category and inventory info
SELECT 
    p.product_id,
    p.name,
    p.description,
    p.sku,
    p.price,
    p.brand,
    p.color,
    p.size,
    c.name as category_name,
    i.quantity_available,
    pi.image_url as primary_image,
    COALESCE(AVG(r.rating), 0) as average_rating,
    COUNT(DISTINCT r.review_id) as review_count
FROM products p
JOIN categories c ON p.category_id = c.category_id
LEFT JOIN inventory i ON p.product_id = i.product_id
LEFT JOIN product_images pi ON p.product_id = pi.product_id AND pi.is_primary = TRUE
LEFT JOIN reviews r ON p.product_id = r.product_id AND r.is_approved = TRUE
WHERE p.is_active = TRUE AND c.is_active = TRUE
GROUP BY p.product_id
ORDER BY p.created_at DESC;

-- Search products by name or description
SELECT 
    p.product_id,
    p.name,
    p.description,
    p.price,
    p.brand,
    c.name as category_name,
    i.quantity_available,
    MATCH(p.name, p.description) AGAINST('smartphone' IN NATURAL LANGUAGE MODE) as relevance_score
FROM products p
JOIN categories c ON p.category_id = c.category_id
LEFT JOIN inventory i ON p.product_id = i.product_id
WHERE p.is_active = TRUE 
    AND c.is_active = TRUE
    AND MATCH(p.name, p.description) AGAINST('smartphone' IN NATURAL LANGUAGE MODE)
ORDER BY relevance_score DESC, p.name ASC;

-- Get products by category with pagination
SELECT 
    p.product_id,
    p.name,
    p.price,
    p.brand,
    i.quantity_available,
    pi.image_url as primary_image,
    COALESCE(AVG(r.rating), 0) as average_rating
FROM products p
JOIN categories c ON p.category_id = c.category_id
LEFT JOIN inventory i ON p.product_id = i.product_id
LEFT JOIN product_images pi ON p.product_id = pi.product_id AND pi.is_primary = TRUE
LEFT JOIN reviews r ON p.product_id = r.product_id AND r.is_approved = TRUE
WHERE p.is_active = TRUE 
    AND c.is_active = TRUE
    AND c.category_id = 1 -- Electronics category
GROUP BY p.product_id
ORDER BY p.name ASC
LIMIT 20 OFFSET 0;

-- Get product details with all images and reviews
SELECT 
    p.*,
    c.name as category_name,
    i.quantity_available,
    i.quantity_reserved,
    GROUP_CONCAT(DISTINCT CONCAT(pi.image_url, '|', pi.alt_text, '|', pi.sort_order) ORDER BY pi.sort_order) as images,
    COALESCE(AVG(r.rating), 0) as average_rating,
    COUNT(DISTINCT r.review_id) as review_count
FROM products p
JOIN categories c ON p.category_id = c.category_id
LEFT JOIN inventory i ON p.product_id = i.product_id
LEFT JOIN product_images pi ON p.product_id = pi.product_id
LEFT JOIN reviews r ON p.product_id = r.product_id AND r.is_approved = TRUE
WHERE p.product_id = 1
GROUP BY p.product_id;

-- Get featured products
SELECT 
    p.product_id,
    p.name,
    p.price,
    p.brand,
    c.name as category_name,
    pi.image_url as primary_image,
    COALESCE(AVG(r.rating), 0) as average_rating
FROM products p
JOIN categories c ON p.category_id = c.category_id
LEFT JOIN product_images pi ON p.product_id = pi.product_id AND pi.is_primary = TRUE
LEFT JOIN reviews r ON p.product_id = r.product_id AND r.is_approved = TRUE
WHERE p.is_active = TRUE 
    AND c.is_active = TRUE
    AND p.is_featured = TRUE
GROUP BY p.product_id
ORDER BY p.created_at DESC
LIMIT 10;

-- =====================================================
-- SHOPPING CART QUERIES
-- =====================================================

-- Get user's active cart with items
SELECT 
    sc.cart_id,
    sc.total_amount,
    sc.total_items,
    sc.updated_at,
    ci.cart_item_id,
    ci.product_id,
    p.name as product_name,
    p.sku,
    ci.quantity,
    ci.unit_price,
    ci.total_price,
    pi.image_url as product_image,
    i.quantity_available
FROM shopping_carts sc
LEFT JOIN cart_items ci ON sc.cart_id = ci.cart_id
LEFT JOIN products p ON ci.product_id = p.product_id
LEFT JOIN product_images pi ON p.product_id = pi.product_id AND pi.is_primary = TRUE
LEFT JOIN inventory i ON p.product_id = i.product_id
WHERE sc.user_id = 1 
    AND sc.status = 'active'
ORDER BY ci.added_at DESC;

-- Get cart summary for user
SELECT 
    sc.cart_id,
    COUNT(ci.cart_item_id) as unique_items,
    SUM(ci.quantity) as total_quantity,
    SUM(ci.total_price) as subtotal,
    sc.updated_at
FROM shopping_carts sc
LEFT JOIN cart_items ci ON sc.cart_id = ci.cart_id
WHERE sc.user_id = 1 
    AND sc.status = 'active'
GROUP BY sc.cart_id;

-- Get abandoned carts (inactive for more than 24 hours)
SELECT 
    sc.cart_id,
    sc.user_id,
    u.email,
    u.first_name,
    u.last_name,
    sc.total_amount,
    sc.total_items,
    sc.updated_at,
    TIMESTAMPDIFF(HOUR, sc.updated_at, NOW()) as hours_since_update
FROM shopping_carts sc
JOIN users u ON sc.user_id = u.user_id
WHERE sc.status = 'active'
    AND sc.total_items > 0
    AND sc.updated_at < DATE_SUB(NOW(), INTERVAL 24 HOUR)
ORDER BY sc.updated_at ASC;

-- =====================================================
-- ORDER QUERIES
-- =====================================================

-- Get user's orders with summary
SELECT 
    o.order_id,
    o.order_number,
    o.status,
    o.total_amount,
    o.payment_status,
    o.created_at,
    o.shipped_at,
    o.delivered_at,
    COUNT(oi.order_item_id) as item_count,
    SUM(oi.quantity) as total_quantity
FROM orders o
LEFT JOIN order_items oi ON o.order_id = oi.order_id
WHERE o.user_id = 1
GROUP BY o.order_id
ORDER BY o.created_at DESC;

-- Get order details with items
SELECT 
    o.order_id,
    o.order_number,
    o.status,
    o.subtotal,
    o.tax_amount,
    o.shipping_amount,
    o.discount_amount,
    o.total_amount,
    o.payment_status,
    o.created_at,
    oi.product_id,
    p.name as product_name,
    p.sku,
    oi.quantity,
    oi.unit_price,
    oi.total_price,
    pi.image_url as product_image
FROM orders o
JOIN order_items oi ON o.order_id = oi.order_id
JOIN products p ON oi.product_id = p.product_id
LEFT JOIN product_images pi ON p.product_id = pi.product_id AND pi.is_primary = TRUE
WHERE o.order_id = 1
ORDER BY oi.order_item_id;

-- Get orders by status
SELECT 
    o.order_id,
    o.order_number,
    u.username,
    u.email,
    o.total_amount,
    o.created_at,
    COUNT(oi.order_item_id) as item_count
FROM orders o
JOIN users u ON o.user_id = u.user_id
LEFT JOIN order_items oi ON o.order_id = oi.order_id
WHERE o.status = 'pending'
GROUP BY o.order_id
ORDER BY o.created_at ASC;

-- Get sales summary by date range
SELECT 
    DATE(o.created_at) as order_date,
    COUNT(DISTINCT o.order_id) as order_count,
    SUM(o.total_amount) as total_sales,
    AVG(o.total_amount) as average_order_value,
    SUM(oi.quantity) as total_items_sold
FROM orders o
JOIN order_items oi ON o.order_id = oi.order_id
WHERE o.status NOT IN ('cancelled', 'refunded')
    AND o.created_at >= DATE_SUB(NOW(), INTERVAL 30 DAY)
GROUP BY DATE(o.created_at)
ORDER BY order_date DESC;

-- =====================================================
-- USER QUERIES
-- =====================================================

-- Get user profile with addresses
SELECT 
    u.user_id,
    u.username,
    u.email,
    u.first_name,
    u.last_name,
    u.phone,
    u.created_at,
    u.last_login,
    ua.address_id,
    ua.address_type,
    ua.street_address,
    ua.city,
    ua.state,
    ua.postal_code,
    ua.country,
    ua.is_default
FROM users u
LEFT JOIN user_addresses ua ON u.user_id = ua.user_id
WHERE u.user_id = 1
ORDER BY ua.is_default DESC, ua.address_type;

-- Get user's order history summary
SELECT 
    u.user_id,
    u.username,
    u.email,
    COUNT(DISTINCT o.order_id) as total_orders,
    COALESCE(SUM(o.total_amount), 0) as total_spent,
    COALESCE(AVG(o.total_amount), 0) as average_order_value,
    MAX(o.created_at) as last_order_date,
    COUNT(DISTINCT r.review_id) as reviews_written
FROM users u
LEFT JOIN orders o ON u.user_id = o.user_id AND o.status NOT IN ('cancelled', 'refunded')
LEFT JOIN reviews r ON u.user_id = r.user_id
WHERE u.user_id = 1
GROUP BY u.user_id;

-- =====================================================
-- INVENTORY QUERIES
-- =====================================================

-- Get low stock products
SELECT 
    p.product_id,
    p.name,
    p.sku,
    c.name as category_name,
    i.quantity_available,
    i.quantity_reserved,
    i.reorder_level,
    i.warehouse_location
FROM products p
JOIN categories c ON p.category_id = c.category_id
JOIN inventory i ON p.product_id = i.product_id
WHERE p.is_active = TRUE
    AND i.quantity_available <= i.reorder_level
ORDER BY i.quantity_available ASC;

-- Get inventory summary by warehouse
SELECT 
    i.warehouse_location,
    COUNT(DISTINCT i.product_id) as product_count,
    SUM(i.quantity_available) as total_quantity,
    SUM(i.quantity_reserved) as total_reserved,
    SUM(i.quantity_available * p.cost_price) as inventory_value
FROM inventory i
JOIN products p ON i.product_id = p.product_id
WHERE p.is_active = TRUE
GROUP BY i.warehouse_location
ORDER BY inventory_value DESC;

-- =====================================================
-- REVIEW QUERIES
-- =====================================================

-- Get product reviews with user info
SELECT 
    r.review_id,
    r.rating,
    r.title,
    r.comment,
    r.is_verified_purchase,
    r.helpful_count,
    r.created_at,
    u.username,
    u.first_name
FROM reviews r
JOIN users u ON r.user_id = u.user_id
WHERE r.product_id = 1
    AND r.is_approved = TRUE
ORDER BY r.created_at DESC;

-- Get review statistics by product
SELECT 
    p.product_id,
    p.name,
    COUNT(r.review_id) as total_reviews,
    AVG(r.rating) as average_rating,
    SUM(CASE WHEN r.rating = 5 THEN 1 ELSE 0 END) as five_star_count,
    SUM(CASE WHEN r.rating = 4 THEN 1 ELSE 0 END) as four_star_count,
    SUM(CASE WHEN r.rating = 3 THEN 1 ELSE 0 END) as three_star_count,
    SUM(CASE WHEN r.rating = 2 THEN 1 ELSE 0 END) as two_star_count,
    SUM(CASE WHEN r.rating = 1 THEN 1 ELSE 0 END) as one_star_count
FROM products p
LEFT JOIN reviews r ON p.product_id = r.product_id AND r.is_approved = TRUE
WHERE p.product_id = 1
GROUP BY p.product_id;

-- =====================================================
-- COUPON QUERIES
-- =====================================================

-- Get active coupons
SELECT 
    coupon_id,
    code,
    name,
    description,
    discount_type,
    discount_value,
    minimum_order_amount,
    usage_limit,
    usage_count,
    valid_from,
    valid_until
FROM coupons
WHERE is_active = TRUE
    AND (valid_until IS NULL OR valid_until > NOW())
    AND (usage_limit IS NULL OR usage_count < usage_limit)
ORDER BY created_at DESC;

-- Get coupon usage history
SELECT 
    c.code,
    c.name,
    cu.discount_amount,
    cu.used_at,
    u.username,
    o.order_number
FROM coupon_usage cu
JOIN coupons c ON cu.coupon_id = c.coupon_id
JOIN users u ON cu.user_id = u.user_id
JOIN orders o ON cu.order_id = o.order_id
WHERE c.coupon_id = 1
ORDER BY cu.used_at DESC;

-- =====================================================
-- ANALYTICS QUERIES
-- =====================================================

-- Top selling products
SELECT 
    p.product_id,
    p.name,
    p.brand,
    c.name as category_name,
    SUM(oi.quantity) as total_sold,
    SUM(oi.total_price) as total_revenue,
    COUNT(DISTINCT oi.order_id) as order_count
FROM products p
JOIN categories c ON p.category_id = c.category_id
JOIN order_items oi ON p.product_id = oi.product_id
JOIN orders o ON oi.order_id = o.order_id
WHERE o.status NOT IN ('cancelled', 'refunded')
    AND o.created_at >= DATE_SUB(NOW(), INTERVAL 30 DAY)
GROUP BY p.product_id
ORDER BY total_sold DESC
LIMIT 10;

-- Customer lifetime value
SELECT 
    u.user_id,
    u.username,
    u.email,
    u.created_at as registration_date,
    COUNT(DISTINCT o.order_id) as total_orders,
    SUM(o.total_amount) as lifetime_value,
    AVG(o.total_amount) as average_order_value,
    DATEDIFF(NOW(), u.created_at) as days_as_customer,
    SUM(o.total_amount) / NULLIF(DATEDIFF(NOW(), u.created_at), 0) as daily_value
FROM users u
LEFT JOIN orders o ON u.user_id = o.user_id AND o.status NOT IN ('cancelled', 'refunded')
GROUP BY u.user_id
HAVING total_orders > 0
ORDER BY lifetime_value DESC
LIMIT 20;

-- Monthly sales trend
SELECT 
    YEAR(o.created_at) as year,
    MONTH(o.created_at) as month,
    MONTHNAME(o.created_at) as month_name,
    COUNT(DISTINCT o.order_id) as order_count,
    SUM(o.total_amount) as total_sales,
    AVG(o.total_amount) as average_order_value,
    COUNT(DISTINCT o.user_id) as unique_customers
FROM orders o
WHERE o.status NOT IN ('cancelled', 'refunded')
    AND o.created_at >= DATE_SUB(NOW(), INTERVAL 12 MONTH)
GROUP BY YEAR(o.created_at), MONTH(o.created_at)
ORDER BY year DESC, month DESC;

-- Category performance
SELECT 
    c.category_id,
    c.name as category_name,
    COUNT(DISTINCT p.product_id) as product_count,
    COUNT(DISTINCT oi.order_id) as order_count,
    SUM(oi.quantity) as total_quantity_sold,
    SUM(oi.total_price) as total_revenue,
    AVG(oi.unit_price) as average_price
FROM categories c
JOIN products p ON c.category_id = p.category_id
JOIN order_items oi ON p.product_id = oi.product_id
JOIN orders o ON oi.order_id = o.order_id
WHERE o.status NOT IN ('cancelled', 'refunded')
    AND o.created_at >= DATE_SUB(NOW(), INTERVAL 30 DAY)
GROUP BY c.category_id
ORDER BY total_revenue DESC;

-- =====================================================
-- PERFORMANCE OPTIMIZATION QUERIES
-- =====================================================

-- Query to analyze table sizes and row counts
SELECT 
    TABLE_NAME,
    TABLE_ROWS,
    ROUND(((DATA_LENGTH + INDEX_LENGTH) / 1024 / 1024), 2) AS 'Size (MB)',
    ROUND((DATA_LENGTH / 1024 / 1024), 2) AS 'Data Size (MB)',
    ROUND((INDEX_LENGTH / 1024 / 1024), 2) AS 'Index Size (MB)'
FROM information_schema.TABLES
WHERE TABLE_SCHEMA = 'ecommerce_db'
ORDER BY (DATA_LENGTH + INDEX_LENGTH) DESC;

-- Query to check index usage
SELECT 
    TABLE_NAME,
    INDEX_NAME,
    COLUMN_NAME,
    CARDINALITY,
    SUB_PART,
    NULLABLE
FROM information_schema.STATISTICS
WHERE TABLE_SCHEMA = 'ecommerce_db'
ORDER BY TABLE_NAME, INDEX_NAME, SEQ_IN_INDEX;

-- Query performance analysis complete
-- All major query patterns covered
-- Optimized for performance with proper indexing
-- Includes analytics and reporting queries