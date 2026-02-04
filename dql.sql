-- Database Schema Reconciliation - DQL Queries
-- Date: 2026-02-04
-- Purpose: Verification queries for schema reconciliation

-- Verify products table constraints
SELECT 
    'Products with valid prices' AS check_name,
    COUNT(*) AS count
FROM products
WHERE price >= 0;

SELECT 
    'Products with valid available quantities' AS check_name,
    COUNT(*) AS count
FROM products
WHERE available_qty >= 0;

-- Verify cart_items table constraints
SELECT 
    'Cart items with valid quantities' AS check_name,
    COUNT(*) AS count
FROM cart_items
WHERE quantity > 0;

-- Verify unique constraint on cart_id and product_id
SELECT 
    'Duplicate cart_id and product_id combinations' AS check_name,
    COUNT(*) AS count
FROM (
    SELECT cart_id, product_id, COUNT(*) as cnt
    FROM cart_items
    GROUP BY cart_id, product_id
    HAVING COUNT(*) > 1
) AS duplicates;

-- Verify foreign key constraints
SELECT 
    'Orphaned cart_items (invalid cart_id)' AS check_name,
    COUNT(*) AS count
FROM cart_items ci
LEFT JOIN carts c ON ci.cart_id = c.cart_id
WHERE c.cart_id IS NULL;

SELECT 
    'Orphaned cart_items (invalid product_id)' AS check_name,
    COUNT(*) AS count
FROM cart_items ci
LEFT JOIN products p ON ci.product_id = p.product_id
WHERE p.product_id IS NULL;

-- List all constraints on tables
SELECT 
    table_name,
    constraint_name,
    constraint_type
FROM information_schema.table_constraints
WHERE table_name IN ('products', 'cart_items', 'carts')
ORDER BY table_name, constraint_type;

-- Schema reconciliation verification complete
