-- Database Schema Updates - DML (Data Manipulation)
-- Generated for schema reconciliation with LLD requirements
-- Date: 2026-02-04
-- Seed data and initial records

-- Insert default user roles
INSERT INTO user_roles (role_name, description, permissions) VALUES
('ADMIN', 'System Administrator with full access', '{"users": ["create", "read", "update", "delete"], "products": ["create", "read", "update", "delete"], "orders": ["create", "read", "update", "delete"], "categories": ["create", "read", "update", "delete"]}'),
('CUSTOMER', 'Regular customer with basic access', '{"products": ["read"], "orders": ["create", "read"], "profile": ["read", "update"]}'),
('MANAGER', 'Store manager with product and order management access', '{"products": ["create", "read", "update"], "orders": ["read", "update"], "categories": ["create", "read", "update"]}'),
('SUPPORT', 'Customer support with read access and order management', '{"users": ["read"], "products": ["read"], "orders": ["read", "update"], "categories": ["read"]}')
ON DUPLICATE KEY UPDATE 
    description = VALUES(description),
    permissions = VALUES(permissions);

-- Update existing categories with additional information
UPDATE categories SET description = 'Consumer electronics, gadgets, and tech accessories' WHERE name = 'Electronics';
UPDATE categories SET description = 'Fashion, apparel, shoes, and accessories' WHERE name = 'Clothing';
UPDATE categories SET description = 'Books, e-books, educational materials, and literature' WHERE name = 'Books';

-- Insert additional categories for comprehensive coverage
INSERT INTO categories (name, description) VALUES
('Home & Garden', 'Home improvement, furniture, and garden supplies'),
('Sports & Outdoors', 'Sports equipment, outdoor gear, and fitness products'),
('Health & Beauty', 'Health products, cosmetics, and personal care items'),
('Toys & Games', 'Toys, games, and entertainment products for all ages'),
('Automotive', 'Car parts, accessories, and automotive tools')
ON DUPLICATE KEY UPDATE description = VALUES(description);

-- Create subcategories (hierarchical structure)
INSERT INTO categories (name, description, parent_category_id) VALUES
('Smartphones', 'Mobile phones and smartphones', (SELECT category_id FROM categories WHERE name = 'Electronics' LIMIT 1)),
('Laptops', 'Laptop computers and notebooks', (SELECT category_id FROM categories WHERE name = 'Electronics' LIMIT 1)),
('Men''s Clothing', 'Clothing items for men', (SELECT category_id FROM categories WHERE name = 'Clothing' LIMIT 1)),
('Women''s Clothing', 'Clothing items for women', (SELECT category_id FROM categories WHERE name = 'Clothing' LIMIT 1)),
('Fiction', 'Fiction books and novels', (SELECT category_id FROM categories WHERE name = 'Books' LIMIT 1)),
('Non-Fiction', 'Non-fiction books and educational materials', (SELECT category_id FROM categories WHERE name = 'Books' LIMIT 1))
ON DUPLICATE KEY UPDATE 
    description = VALUES(description),
    parent_category_id = VALUES(parent_category_id);

-- Update system configuration for the new schema
INSERT INTO system_config (config_key, config_value, description) VALUES
('schema_version', '2.0', 'Current database schema version'),
('user_session_timeout', '3600', 'User session timeout in seconds'),
('max_login_sessions', '5', 'Maximum concurrent sessions per user'),
('default_user_role', 'CUSTOMER', 'Default role assigned to new users'),
('order_number_prefix', 'ORD', 'Prefix for order numbers'),
('enable_email_verification', 'true', 'Enable email verification for new users'),
('max_addresses_per_user', '10', 'Maximum addresses allowed per user'),
('default_currency', 'USD', 'Default currency for transactions')
ON DUPLICATE KEY UPDATE config_value = VALUES(config_value);

-- Create a default admin user (password should be changed immediately)
-- Note: This is for initial setup only, password hash represents 'TempAdmin123!'
INSERT INTO users (email, password_hash, first_name, last_name, status, email_verified) VALUES
('admin@ecommerce.com', '$2y$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', 'System', 'Administrator', 'ACTIVE', TRUE)
ON DUPLICATE KEY UPDATE 
    first_name = VALUES(first_name),
    last_name = VALUES(last_name),
    status = VALUES(status),
    email_verified = VALUES(email_verified);

-- Assign admin role to the default admin user
INSERT INTO user_role_assignments (user_id, role_id, assigned_by)
SELECT 
    u.user_id,
    r.role_id,
    u.user_id
FROM users u, user_roles r
WHERE u.email = 'admin@ecommerce.com' AND r.role_name = 'ADMIN'
ON DUPLICATE KEY UPDATE assigned_at = CURRENT_TIMESTAMP;

-- Update existing products with SKU if missing
UPDATE products 
SET sku = CONCAT('SKU-', UPPER(SUBSTRING(MD5(CONCAT(product_id, name)), 1, 8)))
WHERE sku IS NULL;

-- Ensure all existing users have the CUSTOMER role if no role assigned
INSERT INTO user_role_assignments (user_id, role_id, assigned_by)
SELECT 
    u.user_id,
    r.role_id,
    (SELECT user_id FROM users WHERE email = 'admin@ecommerce.com' LIMIT 1)
FROM users u
CROSS JOIN user_roles r
WHERE r.role_name = 'CUSTOMER'
AND u.user_id NOT IN (
    SELECT DISTINCT user_id FROM user_role_assignments
)
ON DUPLICATE KEY UPDATE assigned_at = CURRENT_TIMESTAMP;