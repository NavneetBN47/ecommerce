-- Cart Management Domain - Data Query Language (DQL)
-- Validation queries for all tables and relationships as per LLD

-- =====================================================
-- BASIC TABLE VALIDATION QUERIES
-- =====================================================

-- Validate Cart table structure and data
SELECT 'Cart Table Validation' as validation_type;
SELECT 
    cart_id,
    user_id,
    created_at,
    updated_at,
    CASE 
        WHEN cart_id IS NULL THEN 'FAIL: cart_id is NULL'
        WHEN user_id IS NULL THEN 'FAIL: user_id is NULL'
        WHEN created_at IS NULL THEN 'FAIL: created_at is NULL'
        WHEN updated_at IS NULL THEN 'FAIL: updated_at is NULL'
        ELSE 'PASS'
    END as validation_status
FROM carts
ORDER BY created_at DESC;

-- Validate CartItem table structure and data
SELECT 'CartItem Table Validation' as validation_type;
SELECT 
    item_id,
    cart_id,
    product_id,
    quantity,
    price,
    added_at,
    CASE 
        WHEN item_id IS NULL THEN 'FAIL: item_id is NULL'
        WHEN cart_id IS NULL THEN 'FAIL: cart_id is NULL'
        WHEN product_id IS NULL THEN 'FAIL: product_id is NULL'
        WHEN quantity IS NULL THEN 'FAIL: quantity is NULL'
        WHEN quantity < 1 THEN 'FAIL: quantity < 1'
        WHEN price IS NULL THEN 'FAIL: price is NULL'
        WHEN price <= 0 THEN 'FAIL: price <= 0'
        WHEN added_at IS NULL THEN 'FAIL: added_at is NULL'
        ELSE 'PASS'
    END as validation_status
FROM cart_items
ORDER BY added_at DESC;

-- =====================================================
-- RELATIONSHIP VALIDATION QUERIES
-- =====================================================

-- Validate Cart to CartItem relationship (1-to-many)
SELECT 'Cart-CartItem Relationship Validation' as validation_type;
SELECT 
    c.cart_id,
    c.user_id,
    COUNT(ci.item_id) as item_count,
    CASE 
        WHEN COUNT(ci.item_id) = 0 THEN 'Empty Cart'
        ELSE CONCAT('Cart has ', COUNT(ci.item_id), ' items')
    END as relationship_status
FROM carts c
LEFT JOIN cart_items ci ON c.cart_id = ci.cart_id
GROUP BY c.cart_id, c.user_id
ORDER BY c.created_at DESC;

-- Validate Foreign Key integrity - Cart to User
SELECT 'Cart-User Foreign Key Validation' as validation_type;
SELECT 
    c.cart_id,
    c.user_id,
    u.username,
    CASE 
        WHEN u.user_id IS NULL THEN 'FAIL: Invalid user_id reference'
        ELSE 'PASS: Valid user reference'
    END as fk_validation_status
FROM carts c
LEFT JOIN users u ON c.user_id = u.user_id
ORDER BY c.created_at DESC;

-- Validate Foreign Key integrity - CartItem to Product
SELECT 'CartItem-Product Foreign Key Validation' as validation_type;
SELECT 
    ci.item_id,
    ci.product_id,
    p.name as product_name,
    p.price as current_price,
    ci.price as cart_price,
    CASE 
        WHEN p.product_id IS NULL THEN 'FAIL: Invalid product_id reference'
        ELSE 'PASS: Valid product reference'
    END as fk_validation_status
FROM cart_items ci
LEFT JOIN products p ON ci.product_id = p.product_id
ORDER BY ci.added_at DESC;

-- =====================================================
-- CONSTRAINT VALIDATION QUERIES
-- =====================================================

-- Validate UUID format for all ID fields
SELECT 'UUID Format Validation' as validation_type;
SELECT 
    'carts' as table_name,
    cart_id,
    CASE 
        WHEN cart_id::text ~ '^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$' THEN 'PASS'
        ELSE 'FAIL: Invalid UUID format'
    END as uuid_validation
FROM carts
UNION ALL
SELECT 
    'cart_items' as table_name,
    item_id::text,
    CASE 
        WHEN item_id::text ~ '^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$' THEN 'PASS'
        ELSE 'FAIL: Invalid UUID format'
    END as uuid_validation
FROM cart_items;

-- Validate quantity constraints (>= 1)
SELECT 'Quantity Constraint Validation' as validation_type;
SELECT 
    item_id,
    quantity,
    CASE 
        WHEN quantity >= 1 THEN 'PASS: quantity >= 1'
        ELSE 'FAIL: quantity < 1'
    END as quantity_validation
FROM cart_items
ORDER BY added_at DESC;

-- Validate price constraints (> 0)
SELECT 'Price Constraint Validation' as validation_type;
SELECT 
    item_id,
    price,
    CASE 
        WHEN price > 0 THEN 'PASS: price > 0'
        ELSE 'FAIL: price <= 0'
    END as price_validation
FROM cart_items
ORDER BY added_at DESC;

-- =====================================================
-- COMPREHENSIVE CART QUERIES
-- =====================================================

-- Get complete cart information with items
SELECT 'Complete Cart Information' as query_type;
SELECT 
    c.cart_id,
    u.username,
    u.email,
    c.created_at as cart_created,
    c.updated_at as cart_updated,
    ci.item_id,
    p.name as product_name,
    ci.quantity,
    ci.price as item_price,
    (ci.quantity * ci.price) as item_total,
    ci.added_at as item_added
FROM carts c
JOIN users u ON c.user_id = u.user_id
LEFT JOIN cart_items ci ON c.cart_id = ci.cart_id
LEFT JOIN products p ON ci.product_id = p.product_id
ORDER BY c.created_at DESC, ci.added_at DESC;

-- Cart summary with totals
SELECT 'Cart Summary with Totals' as query_type;
SELECT 
    c.cart_id,
    u.username,
    COUNT(ci.item_id) as total_items,
    COALESCE(SUM(ci.quantity), 0) as total_quantity,
    COALESCE(SUM(ci.quantity * ci.price), 0) as cart_total,
    c.created_at,
    c.updated_at
FROM carts c
JOIN users u ON c.user_id = u.user_id
LEFT JOIN cart_items ci ON c.cart_id = ci.cart_id
GROUP BY c.cart_id, u.username, c.created_at, c.updated_at
ORDER BY c.created_at DESC;

-- =====================================================
-- DATA INTEGRITY CHECKS
-- =====================================================

-- Check for orphaned cart items (items without valid cart)
SELECT 'Orphaned Cart Items Check' as validation_type;
SELECT 
    ci.item_id,
    ci.cart_id,
    'FAIL: Orphaned cart item' as integrity_status
FROM cart_items ci
LEFT JOIN carts c ON ci.cart_id = c.cart_id
WHERE c.cart_id IS NULL;

-- Check for carts without users
SELECT 'Carts Without Users Check' as validation_type;
SELECT 
    c.cart_id,
    c.user_id,
    'FAIL: Cart without valid user' as integrity_status
FROM carts c
LEFT JOIN users u ON c.user_id = u.user_id
WHERE u.user_id IS NULL;

-- Check for cart items without products
SELECT 'Cart Items Without Products Check' as validation_type;
SELECT 
    ci.item_id,
    ci.product_id,
    'FAIL: Cart item without valid product' as integrity_status
FROM cart_items ci
LEFT JOIN products p ON ci.product_id = p.product_id
WHERE p.product_id IS NULL;

-- =====================================================
-- TIMESTAMP VALIDATION
-- =====================================================

-- Validate timestamp consistency
SELECT 'Timestamp Consistency Validation' as validation_type;
SELECT 
    c.cart_id,
    c.created_at,
    c.updated_at,
    CASE 
        WHEN c.updated_at >= c.created_at THEN 'PASS: updated_at >= created_at'
        ELSE 'FAIL: updated_at < created_at'
    END as timestamp_validation
FROM carts c
ORDER BY c.created_at DESC;

-- Validate cart item timestamps against cart timestamps
SELECT 'Cart Item Timestamp Validation' as validation_type;
SELECT 
    ci.item_id,
    c.cart_id,
    c.created_at as cart_created,
    ci.added_at as item_added,
    CASE 
        WHEN ci.added_at >= c.created_at THEN 'PASS: item added after cart created'
        ELSE 'FAIL: item added before cart created'
    END as timestamp_validation
FROM cart_items ci
JOIN carts c ON ci.cart_id = c.cart_id
ORDER BY ci.added_at DESC;

-- End of DQL validation queries
