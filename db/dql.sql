-- DQL SCRIPT FOR SHOPPING CART SYSTEM
-- Contains read (SELECT) queries for validation and application use
-- Generated from Low Level Design (LLD)

-- ========================================
-- VALIDATION QUERIES FOR ALL TABLES
-- ========================================

-- Validate users table structure and data
SELECT 'Users Table Validation' as validation_type;
SELECT 
    COUNT(*) as total_users,
    COUNT(CASE WHEN roles IS NOT NULL THEN 1 END) as users_with_roles,
    COUNT(CASE WHEN email IS NOT NULL THEN 1 END) as users_with_email,
    COUNT(CASE WHEN created_at IS NOT NULL THEN 1 END) as users_with_created_at,
    COUNT(CASE WHEN updated_at IS NOT NULL THEN 1 END) as users_with_updated_at
FROM users;

-- Validate products table structure and data
SELECT 'Products Table Validation' as validation_type;
SELECT 
    COUNT(*) as total_products,
    COUNT(CASE WHEN category IS NOT NULL THEN 1 END) as products_with_category,
    COUNT(CASE WHEN price > 0 THEN 1 END) as products_with_valid_price,
    COUNT(CASE WHEN available_qty >= 0 THEN 1 END) as products_with_valid_stock,
    COUNT(CASE WHEN updated_at IS NOT NULL THEN 1 END) as products_with_updated_at
FROM products;

-- Validate cart table structure and data
SELECT 'Cart Table Validation' as validation_type;
SELECT 
    COUNT(*) as total_carts,
    COUNT(CASE WHEN status IS NOT NULL THEN 1 END) as carts_with_status,
    COUNT(CASE WHEN status = 'ACTIVE' THEN 1 END) as active_carts,
    COUNT(CASE WHEN status = 'CHECKED_OUT' THEN 1 END) as checked_out_carts,
    COUNT(CASE WHEN updated_at IS NOT NULL THEN 1 END) as carts_with_updated_at
FROM cart;

-- Validate cart_items table structure and data
SELECT 'Cart Items Table Validation' as validation_type;
SELECT 
    COUNT(*) as total_cart_items,
    COUNT(CASE WHEN price_at_addition IS NOT NULL THEN 1 END) as items_with_price_at_addition,
    COUNT(CASE WHEN quantity > 0 AND quantity <= 10 THEN 1 END) as items_with_valid_quantity,
    COUNT(CASE WHEN created_at IS NOT NULL THEN 1 END) as items_with_created_at,
    COUNT(CASE WHEN updated_at IS NOT NULL THEN 1 END) as items_with_updated_at
FROM cart_items;

-- ========================================
-- RELATIONSHIP VALIDATION QUERIES
-- ========================================

-- Validate foreign key relationships
SELECT 'Foreign Key Validation' as validation_type;

-- Check cart -> users relationship
SELECT 
    'Cart-Users FK' as relationship,
    COUNT(*) as total_carts,
    COUNT(u.user_id) as valid_user_references
FROM cart c
LEFT JOIN users u ON c.user_id = u.user_id;

-- Check cart_items -> cart relationship
SELECT 
    'CartItems-Cart FK' as relationship,
    COUNT(*) as total_cart_items,
    COUNT(c.cart_id) as valid_cart_references
FROM cart_items ci
LEFT JOIN cart c ON ci.cart_id = c.cart_id;

-- Check cart_items -> products relationship
SELECT 
    'CartItems-Products FK' as relationship,
    COUNT(*) as total_cart_items,
    COUNT(p.product_id) as valid_product_references
FROM cart_items ci
LEFT JOIN products p ON ci.product_id = p.product_id;

-- ========================================
-- BUSINESS RULE VALIDATION QUERIES
-- ========================================

-- Validate unique active cart per user
SELECT 'Unique Active Cart Validation' as validation_type;
SELECT 
    user_id,
    COUNT(*) as active_carts_count
FROM cart 
WHERE status = 'ACTIVE'
GROUP BY user_id
HAVING COUNT(*) > 1;

-- Validate quantity limits (max 10 per product per cart)
SELECT 'Quantity Limit Validation' as validation_type;
SELECT 
    cart_id,
    product_id,
    quantity
FROM cart_items
WHERE quantity > 10;

-- ========================================
-- APPLICATION QUERIES
-- ========================================

-- Get user profile with role information
CREATE OR REPLACE VIEW user_profiles AS
SELECT 
    user_id,
    username,
    full_name,
    email,
    roles,
    created_at,
    updated_at
FROM users
ORDER BY created_at DESC;

-- Get product catalog with availability
CREATE OR REPLACE VIEW product_catalog AS
SELECT 
    product_id,
    product_name,
    description,
    price,
    available_qty,
    category,
    CASE 
        WHEN available_qty > 0 THEN 'In Stock'
        ELSE 'Out of Stock'
    END as availability_status,
    created_at,
    updated_at
FROM products
ORDER BY category, product_name;

-- Get active cart details for a user
CREATE OR REPLACE VIEW active_cart_details AS
SELECT 
    c.cart_id,
    c.user_id,
    u.username,
    ci.cart_item_id,
    p.product_id,
    p.product_name,
    p.description,
    ci.quantity,
    ci.price_at_addition,
    (ci.quantity * ci.price_at_addition) as item_total,
    p.available_qty as current_stock,
    ci.created_at as added_at,
    ci.updated_at as last_modified
FROM cart c
JOIN users u ON c.user_id = u.user_id
JOIN cart_items ci ON c.cart_id = ci.cart_id
JOIN products p ON ci.product_id = p.product_id
WHERE c.status = 'ACTIVE'
ORDER BY c.cart_id, ci.created_at;

-- Get cart summary for active carts
CREATE OR REPLACE VIEW cart_summary AS
SELECT 
    c.cart_id,
    c.user_id,
    u.username,
    COUNT(ci.cart_item_id) as total_items,
    SUM(ci.quantity) as total_quantity,
    SUM(ci.quantity * ci.price_at_addition) as total_amount,
    c.created_at as cart_created,
    c.updated_at as last_updated
FROM cart c
JOIN users u ON c.user_id = u.user_id
LEFT JOIN cart_items ci ON c.cart_id = ci.cart_id
WHERE c.status = 'ACTIVE'
GROUP BY c.cart_id, c.user_id, u.username, c.created_at, c.updated_at
ORDER BY c.updated_at DESC;

-- Get order history (checked out carts)
CREATE OR REPLACE VIEW order_history AS
SELECT 
    c.cart_id as order_id,
    c.user_id,
    u.username,
    u.full_name,
    COUNT(ci.cart_item_id) as total_items,
    SUM(ci.quantity) as total_quantity,
    SUM(ci.quantity * ci.price_at_addition) as order_total,
    c.created_at as order_date,
    c.updated_at as checkout_date
FROM cart c
JOIN users u ON c.user_id = u.user_id
LEFT JOIN cart_items ci ON c.cart_id = ci.cart_id
WHERE c.status = 'CHECKED_OUT'
GROUP BY c.cart_id, c.user_id, u.username, u.full_name, c.created_at, c.updated_at
ORDER BY c.updated_at DESC;

-- Get product popularity (based on cart additions)
CREATE OR REPLACE VIEW product_popularity AS
SELECT 
    p.product_id,
    p.product_name,
    p.category,
    COUNT(ci.cart_item_id) as times_added_to_cart,
    SUM(ci.quantity) as total_quantity_requested,
    AVG(ci.quantity) as avg_quantity_per_addition,
    p.available_qty as current_stock
FROM products p
LEFT JOIN cart_items ci ON p.product_id = ci.product_id
GROUP BY p.product_id, p.product_name, p.category, p.available_qty
ORDER BY times_added_to_cart DESC, total_quantity_requested DESC;

-- ========================================
-- SAMPLE QUERIES FOR TESTING
-- ========================================

-- Get all users
SELECT * FROM user_profiles LIMIT 10;

-- Get all products by category
SELECT * FROM product_catalog WHERE category = 'Electronics' LIMIT 10;

-- Get active cart for specific user
SELECT * FROM active_cart_details WHERE username = 'john_doe';

-- Get cart summary for all active carts
SELECT * FROM cart_summary;

-- Get order history
SELECT * FROM order_history LIMIT 10;

-- Get product popularity report
SELECT * FROM product_popularity LIMIT 10;

-- Search products by name or category
SELECT * FROM product_catalog 
WHERE product_name ILIKE '%laptop%' OR category ILIKE '%electronics%'
ORDER BY price DESC;

-- Get low stock products
SELECT * FROM product_catalog 
WHERE available_qty < 20
ORDER BY available_qty ASC;

-- Get users with active carts
SELECT DISTINCT u.username, u.full_name, u.email
FROM users u
JOIN cart c ON u.user_id = c.user_id
WHERE c.status = 'ACTIVE';

-- Get total sales by product (from checked out carts)
SELECT 
    p.product_name,
    p.category,
    SUM(ci.quantity) as total_sold,
    SUM(ci.quantity * ci.price_at_addition) as total_revenue
FROM products p
JOIN cart_items ci ON p.product_id = ci.product_id
JOIN cart c ON ci.cart_id = c.cart_id
WHERE c.status = 'CHECKED_OUT'
GROUP BY p.product_id, p.product_name, p.category
ORDER BY total_revenue DESC;