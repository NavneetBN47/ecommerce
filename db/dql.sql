-- =========================================================
-- DQL SCRIPT FOR ECOMMERCE BROWNFIELD APPLICATION
-- PostgreSQL Compatible
-- =========================================================
-- Contains ONLY read (SELECT) queries
-- =========================================================


-- ---------------------------------------------------------
-- 1. USER SIGN-IN (LOGIN)
-- ---------------------------------------------------------

SELECT
    user_id,
    username,
    full_name
FROM users
WHERE username = ? AND password = ?;


-- ---------------------------------------------------------
-- 2. FETCH USER PROFILE
-- ---------------------------------------------------------

SELECT
    user_id,
    username,
    full_name,
    email,
    created_at
FROM users
WHERE user_id = ?;


-- ---------------------------------------------------------
-- 3. PRODUCT SEARCH
-- ---------------------------------------------------------

SELECT
    product_id,
    product_name,
    description,
    price,
    available_qty
FROM products
WHERE product_name ILIKE '%' || ? || '%';


-- ---------------------------------------------------------
-- 4. CHECK IF CART EXISTS FOR USER
-- ---------------------------------------------------------
-- Used before lazy cart creation
-- ---------------------------------------------------------

SELECT cart_id
FROM cart
WHERE user_id = ?;


-- ---------------------------------------------------------
-- 5. VIEW CART ITEMS
-- ---------------------------------------------------------

SELECT
    p.product_id,
    p.product_name,
    p.price,
    ci.quantity,
    (p.price * ci.quantity) AS item_total
FROM cart c
JOIN cart_items ci ON c.cart_id = ci.cart_id
JOIN products p ON ci.product_id = p.product_id
WHERE c.user_id = ?;


-- ---------------------------------------------------------
-- 6. CART ITEM COUNT
-- ---------------------------------------------------------
-- Used to detect empty cart
-- ---------------------------------------------------------

SELECT COUNT(*) AS item_count
FROM cart_items
WHERE cart_id = ?;


-- ---------------------------------------------------------
-- 7. CART GRAND TOTAL
-- ---------------------------------------------------------

SELECT
    COALESCE(SUM(p.price * ci.quantity), 0) AS cart_total
FROM cart c
JOIN cart_items ci ON c.cart_id = ci.cart_id
JOIN products p ON ci.product_id = p.product_id
WHERE c.user_id = ?;

-- =========================================================
-- END OF DQL SCRIPT
-- =========================================================
