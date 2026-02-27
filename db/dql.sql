-- SHOPPING CART BACKEND - DATA QUERY LANGUAGE (DQL)
-- Validation and operational SELECT queries reflecting LLD requirements
-- User authentication, profile management, product search, cart operations

-- USER AUTHENTICATION AND PROFILE QUERIES

-- Sign-in validation query
-- Validates username and returns user details for authentication
SELECT 
    user_id,
    username,
    password,
    full_name,
    email,
    created_at
FROM users 
WHERE username = ? AND password = ?;

-- User profile query by username
-- Returns user profile information (excluding password)
SELECT 
    user_id,
    username,
    full_name,
    email,
    created_at
FROM users 
WHERE username = ?;

-- User profile query by ID
-- Returns user profile information for authenticated sessions
SELECT 
    user_id,
    username,
    full_name,
    email,
    created_at
FROM users 
WHERE user_id = ?;

-- Check username availability
-- Validates unique username constraint during registration
SELECT COUNT(*) as username_exists
FROM users 
WHERE username = ?;

-- PRODUCT CATALOG QUERIES

-- Product search with case-insensitive matching
-- Implements case-insensitive product search as per LLD requirement
SELECT 
    product_id,
    product_name,
    description,
    price,
    available_qty,
    created_at
FROM products 
WHERE LOWER(product_name) LIKE LOWER(CONCAT('%', ?, '%'))
   OR LOWER(description) LIKE LOWER(CONCAT('%', ?, '%'))
ORDER BY product_name;

-- Get all products
-- Returns complete product catalog
SELECT 
    product_id,
    product_name,
    description,
    price,
    available_qty,
    created_at
FROM products 
ORDER BY product_name;

-- Get product by ID
-- Returns specific product details
SELECT 
    product_id,
    product_name,
    description,
    price,
    available_qty,
    created_at
FROM products 
WHERE product_id = ?;

-- Check product availability
-- Validates product exists and has sufficient quantity
SELECT 
    product_id,
    product_name,
    available_qty
FROM products 
WHERE product_id = ? AND available_qty >= ?;

-- CART MANAGEMENT QUERIES

-- Check cart existence for user
-- Determines if user has an active cart
SELECT 
    cart_id,
    user_id,
    created_at
FROM cart 
WHERE user_id = ?;

-- Get user cart with items
-- Returns complete cart view with product details
SELECT 
    c.cart_id,
    c.user_id,
    c.created_at as cart_created,
    ci.cart_item_id,
    ci.product_id,
    ci.quantity,
    ci.added_at,
    p.product_name,
    p.description,
    p.price,
    (ci.quantity * p.price) as item_total
FROM cart c
JOIN cart_items ci ON c.cart_id = ci.cart_id
JOIN products p ON ci.product_id = p.product_id
WHERE c.user_id = ?
ORDER BY ci.added_at DESC;

-- Get cart items count
-- Returns total number of items in user's cart
SELECT 
    COALESCE(SUM(ci.quantity), 0) as total_items
FROM cart c
JOIN cart_items ci ON c.cart_id = ci.cart_id
WHERE c.user_id = ?;

-- Get cart grand total
-- Calculates total price of all items in cart
SELECT 
    COALESCE(SUM(ci.quantity * p.price), 0.00) as grand_total
FROM cart c
JOIN cart_items ci ON c.cart_id = ci.cart_id
JOIN products p ON ci.product_id = p.product_id
WHERE c.user_id = ?;

-- Get specific cart item
-- Returns details of a specific item in user's cart
SELECT 
    ci.cart_item_id,
    ci.cart_id,
    ci.product_id,
    ci.quantity,
    ci.added_at,
    p.product_name,
    p.price,
    (ci.quantity * p.price) as item_total
FROM cart c
JOIN cart_items ci ON c.cart_id = ci.cart_id
JOIN products p ON ci.product_id = p.product_id
WHERE c.user_id = ? AND ci.product_id = ?;

-- VALIDATION AND AUDIT QUERIES

-- Validate user exists before cart operations
-- Ensures user must exist before cart operation as per LLD constraint
SELECT user_id, username
FROM users 
WHERE user_id = ?;

-- Validate product exists before adding to cart
-- Ensures product must exist as per LLD constraint
SELECT product_id, product_name, price, available_qty
FROM products 
WHERE product_id = ?;

-- Check for empty carts (should not exist per LLD)
-- Audit query to ensure no empty carts exist
SELECT 
    c.cart_id,
    c.user_id,
    c.created_at,
    COUNT(ci.cart_item_id) as item_count
FROM cart c
LEFT JOIN cart_items ci ON c.cart_id = ci.cart_id
GROUP BY c.cart_id, c.user_id, c.created_at
HAVING COUNT(ci.cart_item_id) = 0;

-- Get cart summary for all users
-- Administrative query for cart status overview
SELECT 
    u.username,
    c.cart_id,
    c.created_at,
    COUNT(ci.cart_item_id) as total_items,
    COALESCE(SUM(ci.quantity * p.price), 0.00) as cart_total
FROM users u
LEFT JOIN cart c ON u.user_id = c.user_id
LEFT JOIN cart_items ci ON c.cart_id = ci.cart_id
LEFT JOIN products p ON ci.product_id = p.product_id
GROUP BY u.user_id, u.username, c.cart_id, c.created_at
ORDER BY u.username;

-- Performance monitoring queries
-- Check for potential performance issues

-- Users without carts (normal state)
SELECT 
    u.user_id,
    u.username,
    u.created_at
FROM users u
LEFT JOIN cart c ON u.user_id = c.user_id
WHERE c.cart_id IS NULL;

-- Products never added to cart
SELECT 
    p.product_id,
    p.product_name,
    p.price
FROM products p
LEFT JOIN cart_items ci ON p.product_id = ci.product_id
WHERE ci.product_id IS NULL;

-- Most popular products in carts
SELECT 
    p.product_id,
    p.product_name,
    COUNT(ci.cart_item_id) as times_added,
    SUM(ci.quantity) as total_quantity
FROM products p
JOIN cart_items ci ON p.product_id = ci.product_id
GROUP BY p.product_id, p.product_name
ORDER BY times_added DESC, total_quantity DESC;
