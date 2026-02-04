-- Database Schema Validation Queries - DQL
-- Generated for schema reconciliation with LLD requirements
-- Date: 2026-02-04
-- Validation queries to verify schema correctness

-- ========================================
-- SCHEMA STRUCTURE VALIDATION
-- ========================================

-- Verify all required tables exist
SELECT 
    'Table Existence Check' as validation_type,
    table_name,
    CASE WHEN table_name IS NOT NULL THEN 'EXISTS' ELSE 'MISSING' END as status
FROM (
    SELECT 'users' as table_name
    UNION SELECT 'user_roles'
    UNION SELECT 'user_role_assignments'
    UNION SELECT 'user_sessions'
    UNION SELECT 'categories'
    UNION SELECT 'products'
    UNION SELECT 'product_images'
    UNION SELECT 'orders'
    UNION SELECT 'order_items'
    UNION SELECT 'addresses'
    UNION SELECT 'payment_transactions'
) expected_tables
LEFT JOIN information_schema.tables t 
    ON expected_tables.table_name = t.table_name 
    AND t.table_schema = DATABASE();

-- ========================================
-- COLUMN VALIDATION
-- ========================================

-- Verify users table has all required columns
SELECT 
    'Users Table Columns' as validation_type,
    column_name,
    data_type,
    is_nullable,
    column_default
FROM information_schema.columns 
WHERE table_schema = DATABASE() 
    AND table_name = 'users'
ORDER BY ordinal_position;

-- Verify products table has all required columns including SKU
SELECT 
    'Products Table Columns' as validation_type,
    column_name,
    data_type,
    is_nullable,
    column_default
FROM information_schema.columns 
WHERE table_schema = DATABASE() 
    AND table_name = 'products'
ORDER BY ordinal_position;

-- ========================================
-- FOREIGN KEY CONSTRAINTS VALIDATION
-- ========================================

-- Check all foreign key constraints
SELECT 
    'Foreign Key Constraints' as validation_type,
    kcu.table_name,
    kcu.column_name,
    kcu.referenced_table_name,
    kcu.referenced_column_name,
    rc.constraint_name
FROM information_schema.key_column_usage kcu
JOIN information_schema.referential_constraints rc 
    ON kcu.constraint_name = rc.constraint_name
WHERE kcu.table_schema = DATABASE()
ORDER BY kcu.table_name, kcu.column_name;

-- ========================================
-- INDEX VALIDATION
-- ========================================

-- Verify important indexes exist
SELECT 
    'Index Validation' as validation_type,
    table_name,
    index_name,
    column_name,
    non_unique
FROM information_schema.statistics 
WHERE table_schema = DATABASE()
    AND table_name IN ('users', 'products', 'orders', 'order_items', 'user_sessions')
ORDER BY table_name, index_name, seq_in_index;

-- ========================================
-- DATA INTEGRITY VALIDATION
-- ========================================

-- Check if default roles exist
SELECT 
    'Default Roles Check' as validation_type,
    role_name,
    description,
    created_at
FROM user_roles
WHERE role_name IN ('ADMIN', 'CUSTOMER', 'MANAGER', 'SUPPORT')
ORDER BY role_name;

-- Check if admin user exists and has proper role
SELECT 
    'Admin User Check' as validation_type,
    u.email,
    u.first_name,
    u.last_name,
    u.status,
    u.email_verified,
    r.role_name
FROM users u
JOIN user_role_assignments ura ON u.user_id = ura.user_id
JOIN user_roles r ON ura.role_id = r.role_id
WHERE u.email = 'admin@ecommerce.com';

-- Check categories hierarchy
SELECT 
    'Categories Hierarchy Check' as validation_type,
    c.name as category_name,
    p.name as parent_category_name,
    c.status
FROM categories c
LEFT JOIN categories p ON c.parent_category_id = p.category_id
ORDER BY p.name, c.name;

-- ========================================
-- CONSTRAINT VALIDATION
-- ========================================

-- Check table constraints
SELECT 
    'Table Constraints' as validation_type,
    table_name,
    constraint_name,
    constraint_type
FROM information_schema.table_constraints 
WHERE table_schema = DATABASE()
    AND constraint_type IN ('PRIMARY KEY', 'FOREIGN KEY', 'UNIQUE', 'CHECK')
ORDER BY table_name, constraint_type;

-- ========================================
-- BUSINESS RULE VALIDATION
-- ========================================

-- Verify products have positive prices
SELECT 
    'Product Price Validation' as validation_type,
    COUNT(*) as total_products,
    COUNT(CASE WHEN price > 0 THEN 1 END) as products_with_valid_price,
    COUNT(CASE WHEN price <= 0 THEN 1 END) as products_with_invalid_price
FROM products;

-- Verify products have non-negative stock
SELECT 
    'Product Stock Validation' as validation_type,
    COUNT(*) as total_products,
    COUNT(CASE WHEN stock_quantity >= 0 THEN 1 END) as products_with_valid_stock,
    COUNT(CASE WHEN stock_quantity < 0 THEN 1 END) as products_with_invalid_stock
FROM products;

-- Verify all products have unique SKUs
SELECT 
    'Product SKU Uniqueness' as validation_type,
    COUNT(*) as total_products,
    COUNT(DISTINCT sku) as unique_skus,
    CASE WHEN COUNT(*) = COUNT(DISTINCT sku) THEN 'VALID' ELSE 'INVALID' END as sku_uniqueness_status
FROM products
WHERE sku IS NOT NULL;

-- ========================================
-- SYSTEM CONFIGURATION VALIDATION
-- ========================================

-- Check system configuration
SELECT 
    'System Configuration' as validation_type,
    config_key,
    config_value,
    description
FROM system_config
WHERE config_key IN (
    'schema_version', 
    'user_session_timeout', 
    'default_user_role', 
    'enable_email_verification'
)
ORDER BY config_key;

-- ========================================
-- TRIGGER VALIDATION
-- ========================================

-- Check if update triggers exist
SELECT 
    'Trigger Validation' as validation_type,
    trigger_name,
    event_object_table,
    action_timing,
    event_manipulation
FROM information_schema.triggers 
WHERE trigger_schema = DATABASE()
    AND trigger_name LIKE '%updated_at%'
ORDER BY event_object_table;

-- ========================================
-- SUMMARY VALIDATION REPORT
-- ========================================

-- Generate summary report
SELECT 
    'Schema Validation Summary' as validation_type,
    'All validations completed' as message,
    NOW() as validation_timestamp,
    DATABASE() as database_name,
    VERSION() as mysql_version;

-- Count records in each main table
SELECT 'Record Counts' as validation_type, 'users' as table_name, COUNT(*) as record_count FROM users
UNION ALL
SELECT 'Record Counts', 'user_roles', COUNT(*) FROM user_roles
UNION ALL
SELECT 'Record Counts', 'categories', COUNT(*) FROM categories
UNION ALL
SELECT 'Record Counts', 'products', COUNT(*) FROM products
UNION ALL
SELECT 'Record Counts', 'orders', COUNT(*) FROM orders
UNION ALL
SELECT 'Record Counts', 'addresses', COUNT(*) FROM addresses
ORDER BY table_name;