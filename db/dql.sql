-- DQL Script for Shopping Cart System
-- Validation queries for all tables and relationships
-- Queries to verify business rules and data integrity

-- 1. Validate Users table
SELECT 'Users Table Validation' AS validation_type;
SELECT 
    COUNT(*) as total_users,
    COUNT(DISTINCT username) as unique_usernames,
    COUNT(DISTINCT email) as unique_emails,
    SUM(CASE WHEN is_active = TRUE THEN 1 ELSE 0 END) as active_users
FROM users;

-- Check for duplicate usernames (should be 0)
SELECT 'Duplicate Username Check' AS validation_type;
SELECT username, COUNT(*) as count
FROM users 
GROUP BY username 
HAVING COUNT(*) > 1;

-- 2. Validate Products table
SELECT 'Products Table Validation' AS validation_type;
SELECT 
    COUNT(*) as total_products,
    COUNT(DISTINCT category) as unique_categories,
    SUM(CASE WHEN is_active = TRUE THEN 1 ELSE 0 END) as active_products,
    SUM(CASE WHEN price > 0 THEN 1 ELSE 0 END) as products_with_valid_price,
    SUM(CASE WHEN stock_quantity >= 0 THEN 1 ELSE 0 END) as products_with_valid_stock
FROM products;

-- Check products with invalid price or stock (should be 0)
SELECT 'Invalid Product Data Check' AS validation_type;
SELECT product_id, product_name, price, stock_quantity
FROM products 
WHERE price < 0 OR stock_quantity < 0;

-- 3. Validate Carts table
SELECT 'Carts Table Validation' AS validation_type;
SELECT 
    COUNT(*) as total_carts,
    COUNT(DISTINCT user_id) as users_with_carts,
    SUM(CASE WHEN is_active = TRUE THEN 1 ELSE 0 END) as active_carts
FROM carts;

-- Check for users with multiple active carts (should be 0)
SELECT 'Multiple Active Carts Check' AS validation_type;
SELECT user_id, COUNT(*) as active_cart_count
FROM carts 
WHERE is_active = TRUE 
GROUP BY user_id 
HAVING COUNT(*) > 1;

-- 4. Validate Cart Items table
SELECT 'Cart Items Table Validation' AS validation_type;
SELECT 
    COUNT(*) as total_cart_items,
    COUNT(DISTINCT cart_id) as carts_with_items,
    COUNT(DISTINCT product_id) as unique_products_in_carts,
    SUM(CASE WHEN quantity > 0 THEN 1 ELSE 0 END) as items_with_valid_quantity,
    SUM(CASE WHEN unit_price >= 0 THEN 1 ELSE 0 END) as items_with_valid_price
FROM cart_items;

-- Check for cart items with invalid quantity (should be 0)
SELECT 'Invalid Cart Item Data Check' AS validation_type;
SELECT cart_item_id, cart_id, product_id, quantity, unit_price
FROM cart_items 
WHERE quantity <= 0 OR unit_price < 0;

-- 5. Validate Relationships
SELECT 'Relationship Validation' AS validation_type;

-- Check for orphaned carts (carts without valid users)
SELECT 'Orphaned Carts Check' AS validation_type;
SELECT c.cart_id, c.user_id
FROM carts c
LEFT JOIN users u ON c.user_id = u.user_id
WHERE u.user_id IS NULL;

-- Check for orphaned cart items (items without valid carts or products)
SELECT 'Orphaned Cart Items Check' AS validation_type;
SELECT ci.cart_item_id, ci.cart_id, ci.product_id
FROM cart_items ci
LEFT JOIN carts c ON ci.cart_id = c.cart_id
LEFT JOIN products p ON ci.product_id = p.product_id
WHERE c.cart_id IS NULL OR p.product_id IS NULL;

-- 6. Business Rule Validations
-- Check for empty active carts (carts without items - should be handled by application)
SELECT 'Empty Active Carts Check' AS validation_type;
SELECT c.cart_id, c.user_id, u.username
FROM carts c
JOIN users u ON c.user_id = u.user_id
LEFT JOIN cart_items ci ON c.cart_id = ci.cart_id
WHERE c.is_active = TRUE AND ci.cart_id IS NULL;

-- 7. Summary Statistics
SELECT 'System Summary Statistics' AS validation_type;
SELECT 
    (SELECT COUNT(*) FROM users) as total_users,
    (SELECT COUNT(*) FROM products) as total_products,
    (SELECT COUNT(*) FROM carts) as total_carts,
    (SELECT COUNT(*) FROM cart_items) as total_cart_items,
    (SELECT COUNT(*) FROM carts WHERE is_active = TRUE) as active_carts,
    (SELECT COUNT(DISTINCT user_id) FROM carts WHERE is_active = TRUE) as users_with_active_carts;

-- 8. Data Integrity Final Check
SELECT 'Data Integrity Final Check' AS validation_type;
SELECT 
    'PASS' as status,
    'All validation queries completed successfully' as message
WHERE NOT EXISTS (
    SELECT 1 FROM users GROUP BY username HAVING COUNT(*) > 1
) AND NOT EXISTS (
    SELECT 1 FROM products WHERE price < 0 OR stock_quantity < 0
) AND NOT EXISTS (
    SELECT 1 FROM carts WHERE is_active = TRUE GROUP BY user_id HAVING COUNT(*) > 1
) AND NOT EXISTS (
    SELECT 1 FROM cart_items WHERE quantity <= 0 OR unit_price < 0
);
