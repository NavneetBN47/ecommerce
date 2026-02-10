-- Shopping Cart System DML Script
-- Data Manipulation Language statements for initial data setup
-- Generated from Low-Level Design (LLD)

-- Insert default categories
INSERT INTO categories (name, description, is_active, sort_order) VALUES
('Electronics', 'Electronic devices and accessories', TRUE, 1),
('Clothing', 'Apparel and fashion items', TRUE, 2),
('Books', 'Books and educational materials', TRUE, 3),
('Home & Garden', 'Home improvement and gardening supplies', TRUE, 4),
('Sports & Outdoors', 'Sports equipment and outdoor gear', TRUE, 5),
('Health & Beauty', 'Health and beauty products', TRUE, 6),
('Toys & Games', 'Toys and gaming products', TRUE, 7),
('Automotive', 'Car parts and automotive accessories', TRUE, 8)
ON CONFLICT (name) DO NOTHING;

-- Insert subcategories for Electronics
INSERT INTO categories (name, description, parent_category_id, is_active, sort_order)
SELECT 
    'Smartphones', 'Mobile phones and accessories', category_id, TRUE, 1
FROM categories WHERE name = 'Electronics'
ON CONFLICT (name) DO NOTHING;

INSERT INTO categories (name, description, parent_category_id, is_active, sort_order)
SELECT 
    'Laptops', 'Laptop computers and accessories', category_id, TRUE, 2
FROM categories WHERE name = 'Electronics'
ON CONFLICT (name) DO NOTHING;

INSERT INTO categories (name, description, parent_category_id, is_active, sort_order)
SELECT 
    'Tablets', 'Tablet computers and accessories', category_id, TRUE, 3
FROM categories WHERE name = 'Electronics'
ON CONFLICT (name) DO NOTHING;

-- Insert subcategories for Clothing
INSERT INTO categories (name, description, parent_category_id, is_active, sort_order)
SELECT 
    'Men''s Clothing', 'Clothing for men', category_id, TRUE, 1
FROM categories WHERE name = 'Clothing'
ON CONFLICT (name) DO NOTHING;

INSERT INTO categories (name, description, parent_category_id, is_active, sort_order)
SELECT 
    'Women''s Clothing', 'Clothing for women', category_id, TRUE, 2
FROM categories WHERE name = 'Clothing'
ON CONFLICT (name) DO NOTHING;

INSERT INTO categories (name, description, parent_category_id, is_active, sort_order)
SELECT 
    'Children''s Clothing', 'Clothing for children', category_id, TRUE, 3
FROM categories WHERE name = 'Clothing'
ON CONFLICT (name) DO NOTHING;

-- Insert sample products for Electronics > Smartphones
INSERT INTO products (name, description, sku, category_id, price, cost_price, weight, is_active, is_featured)
SELECT 
    'iPhone 15 Pro', 'Latest Apple iPhone with advanced features', 'IPHONE15PRO-001', category_id, 999.99, 750.00, 0.221, TRUE, TRUE
FROM categories WHERE name = 'Smartphones'
ON CONFLICT (sku) DO NOTHING;

INSERT INTO products (name, description, sku, category_id, price, cost_price, weight, is_active, is_featured)
SELECT 
    'Samsung Galaxy S24', 'Premium Android smartphone', 'SAMSUNG-S24-001', category_id, 899.99, 650.00, 0.196, TRUE, TRUE
FROM categories WHERE name = 'Smartphones'
ON CONFLICT (sku) DO NOTHING;

INSERT INTO products (name, description, sku, category_id, price, cost_price, weight, is_active, is_featured)
SELECT 
    'Google Pixel 8', 'Google''s flagship smartphone with AI features', 'PIXEL8-001', category_id, 699.99, 500.00, 0.187, TRUE, FALSE
FROM categories WHERE name = 'Smartphones'
ON CONFLICT (sku) DO NOTHING;

-- Insert sample products for Electronics > Laptops
INSERT INTO products (name, description, sku, category_id, price, cost_price, weight, is_active, is_featured)
SELECT 
    'MacBook Pro 16"', 'Professional laptop for creative work', 'MBP16-2024-001', category_id, 2499.99, 1800.00, 2.140, TRUE, TRUE
FROM categories WHERE name = 'Laptops'
ON CONFLICT (sku) DO NOTHING;

INSERT INTO products (name, description, sku, category_id, price, cost_price, weight, is_active, is_featured)
SELECT 
    'Dell XPS 13', 'Ultra-portable business laptop', 'DELL-XPS13-001', category_id, 1299.99, 950.00, 1.270, TRUE, FALSE
FROM categories WHERE name = 'Laptops'
ON CONFLICT (sku) DO NOTHING;

-- Insert sample products for Clothing > Men's Clothing
INSERT INTO products (name, description, sku, category_id, price, cost_price, weight, is_active, is_featured)
SELECT 
    'Men''s Cotton T-Shirt', 'Comfortable cotton t-shirt in various colors', 'MENS-TSHIRT-001', category_id, 24.99, 12.00, 0.200, TRUE, FALSE
FROM categories WHERE name = 'Men''s Clothing'
ON CONFLICT (sku) DO NOTHING;

INSERT INTO products (name, description, sku, category_id, price, cost_price, weight, is_active, is_featured)
SELECT 
    'Men''s Denim Jeans', 'Classic fit denim jeans', 'MENS-JEANS-001', category_id, 79.99, 40.00, 0.600, TRUE, TRUE
FROM categories WHERE name = 'Men''s Clothing'
ON CONFLICT (sku) DO NOTHING;

-- Insert sample products for Books
INSERT INTO products (name, description, sku, category_id, price, cost_price, weight, is_active, is_featured)
SELECT 
    'The Art of Programming', 'Comprehensive guide to software development', 'BOOK-PROG-001', category_id, 49.99, 25.00, 0.800, TRUE, TRUE
FROM categories WHERE name = 'Books'
ON CONFLICT (sku) DO NOTHING;

INSERT INTO products (name, description, sku, category_id, price, cost_price, weight, is_active, is_featured)
SELECT 
    'Database Design Fundamentals', 'Essential guide to database design', 'BOOK-DB-001', category_id, 39.99, 20.00, 0.650, TRUE, FALSE
FROM categories WHERE name = 'Books'
ON CONFLICT (sku) DO NOTHING;

-- Insert inventory records for all products
INSERT INTO product_inventory (product_id, quantity_available, reorder_level, max_stock_level)
SELECT 
    product_id, 
    CASE 
        WHEN price > 1000 THEN 25  -- High-value items: lower stock
        WHEN price > 100 THEN 100  -- Medium-value items: moderate stock
        ELSE 500                   -- Low-value items: higher stock
    END as quantity_available,
    CASE 
        WHEN price > 1000 THEN 5
        WHEN price > 100 THEN 20
        ELSE 50
    END as reorder_level,
    CASE 
        WHEN price > 1000 THEN 50
        WHEN price > 100 THEN 200
        ELSE 1000
    END as max_stock_level
FROM products
WHERE NOT EXISTS (
    SELECT 1 FROM product_inventory WHERE product_inventory.product_id = products.product_id
);

-- Insert sample product images
INSERT INTO product_images (product_id, image_url, alt_text, is_primary, sort_order)
SELECT 
    p.product_id,
    '/images/products/' || LOWER(REPLACE(p.sku, '-', '_')) || '_main.jpg',
    p.name || ' - Main Image',
    TRUE,
    1
FROM products p
WHERE NOT EXISTS (
    SELECT 1 FROM product_images pi WHERE pi.product_id = p.product_id AND pi.is_primary = TRUE
);

INSERT INTO product_images (product_id, image_url, alt_text, is_primary, sort_order)
SELECT 
    p.product_id,
    '/images/products/' || LOWER(REPLACE(p.sku, '-', '_')) || '_gallery_' || generate_series(1, 3) || '.jpg',
    p.name || ' - Gallery Image ' || generate_series(1, 3),
    FALSE,
    generate_series(2, 4)
FROM products p
WHERE p.price > 100  -- Only add gallery images for higher-priced items
AND NOT EXISTS (
    SELECT 1 FROM product_images pi 
    WHERE pi.product_id = p.product_id 
    AND pi.is_primary = FALSE
);

-- Insert sample admin user (password should be hashed in real implementation)
INSERT INTO users (email, password_hash, first_name, last_name, is_active, email_verified)
VALUES (
    'admin@ecommerce.com',
    '$2b$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/LewdBPj/RK.PmvlW.',  -- hashed 'admin123'
    'System',
    'Administrator',
    TRUE,
    TRUE
)
ON CONFLICT (email) DO NOTHING;

-- Insert sample customer users
INSERT INTO users (email, password_hash, first_name, last_name, phone, is_active, email_verified)
VALUES 
(
    'john.doe@example.com',
    '$2b$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/LewdBPj/RK.PmvlW.',  -- hashed 'password123'
    'John',
    'Doe',
    '+1-555-0123',
    TRUE,
    TRUE
),
(
    'jane.smith@example.com',
    '$2b$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/LewdBPj/RK.PmvlW.',  -- hashed 'password123'
    'Jane',
    'Smith',
    '+1-555-0456',
    TRUE,
    TRUE
),
(
    'mike.johnson@example.com',
    '$2b$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/LewdBPj/RK.PmvlW.',  -- hashed 'password123'
    'Mike',
    'Johnson',
    '+1-555-0789',
    TRUE,
    FALSE
)
ON CONFLICT (email) DO NOTHING;

-- Insert sample addresses for users
INSERT INTO user_addresses (user_id, address_type, street_address, city, state, postal_code, country, is_default)
SELECT 
    u.user_id,
    'both',
    '123 Main Street',
    'Anytown',
    'CA',
    '12345',
    'United States',
    TRUE
FROM users u
WHERE u.email = 'john.doe@example.com'
AND NOT EXISTS (
    SELECT 1 FROM user_addresses ua WHERE ua.user_id = u.user_id
);

INSERT INTO user_addresses (user_id, address_type, street_address, city, state, postal_code, country, is_default)
SELECT 
    u.user_id,
    'shipping',
    '456 Oak Avenue',
    'Springfield',
    'NY',
    '67890',
    'United States',
    TRUE
FROM users u
WHERE u.email = 'jane.smith@example.com'
AND NOT EXISTS (
    SELECT 1 FROM user_addresses ua WHERE ua.user_id = u.user_id
);

INSERT INTO user_addresses (user_id, address_type, street_address, city, state, postal_code, country, is_default)
SELECT 
    u.user_id,
    'billing',
    '789 Pine Street',
    'Riverside',
    'TX',
    '54321',
    'United States',
    TRUE
FROM users u
WHERE u.email = 'jane.smith@example.com'
AND NOT EXISTS (
    SELECT 1 FROM user_addresses ua WHERE ua.user_id = u.user_id AND ua.address_type = 'billing'
);

-- Update product inventory last_restocked dates
UPDATE product_inventory 
SET last_restocked = CURRENT_TIMESTAMP - INTERVAL '7 days'
WHERE last_restocked IS NULL;

-- Update some users' last_login timestamps
UPDATE users 
SET last_login = CURRENT_TIMESTAMP - INTERVAL '2 days'
WHERE email IN ('john.doe@example.com', 'jane.smith@example.com');

-- Create some sample shopping carts (active sessions)
INSERT INTO shopping_carts (user_id, expires_at)
SELECT 
    u.user_id,
    CURRENT_TIMESTAMP + INTERVAL '30 days'
FROM users u
WHERE u.email = 'john.doe@example.com'
AND NOT EXISTS (
    SELECT 1 FROM shopping_carts sc WHERE sc.user_id = u.user_id
);

-- Add items to John Doe's cart
INSERT INTO cart_items (cart_id, product_id, quantity, unit_price)
SELECT 
    sc.cart_id,
    p.product_id,
    2,
    p.price
FROM shopping_carts sc
JOIN users u ON sc.user_id = u.user_id
JOIN products p ON p.sku = 'IPHONE15PRO-001'
WHERE u.email = 'john.doe@example.com'
AND NOT EXISTS (
    SELECT 1 FROM cart_items ci WHERE ci.cart_id = sc.cart_id AND ci.product_id = p.product_id
);

INSERT INTO cart_items (cart_id, product_id, quantity, unit_price)
SELECT 
    sc.cart_id,
    p.product_id,
    1,
    p.price
FROM shopping_carts sc
JOIN users u ON sc.user_id = u.user_id
JOIN products p ON p.sku = 'MBP16-2024-001'
WHERE u.email = 'john.doe@example.com'
AND NOT EXISTS (
    SELECT 1 FROM cart_items ci WHERE ci.cart_id = sc.cart_id AND ci.product_id = p.product_id
);

-- Insert a sample completed order
INSERT INTO orders (
    user_id, 
    status, 
    subtotal, 
    tax_amount, 
    shipping_amount, 
    total_amount,
    billing_address_id,
    shipping_address_id,
    payment_method,
    payment_status
)
SELECT 
    u.user_id,
    'delivered',
    149.98,
    12.00,
    9.99,
    171.97,
    ua1.address_id,
    ua1.address_id,
    'credit_card',
    'captured'
FROM users u
JOIN user_addresses ua1 ON ua1.user_id = u.user_id AND ua1.address_type IN ('both', 'billing')
WHERE u.email = 'jane.smith@example.com'
AND NOT EXISTS (
    SELECT 1 FROM orders o WHERE o.user_id = u.user_id
)
LIMIT 1;

-- Insert order items for the sample order
INSERT INTO order_items (order_id, product_id, quantity, unit_price, total_price)
SELECT 
    o.order_id,
    p.product_id,
    2,
    p.price,
    2 * p.price
FROM orders o
JOIN users u ON o.user_id = u.user_id
JOIN products p ON p.sku = 'MENS-TSHIRT-001'
WHERE u.email = 'jane.smith@example.com'
AND o.status = 'delivered'
AND NOT EXISTS (
    SELECT 1 FROM order_items oi WHERE oi.order_id = o.order_id
);

INSERT INTO order_items (order_id, product_id, quantity, unit_price, total_price)
SELECT 
    o.order_id,
    p.product_id,
    1,
    p.price,
    p.price
FROM orders o
JOIN users u ON o.user_id = u.user_id
JOIN products p ON p.sku = 'BOOK-PROG-001'
WHERE u.email = 'jane.smith@example.com'
AND o.status = 'delivered'
AND NOT EXISTS (
    SELECT 1 FROM order_items oi WHERE oi.order_id = o.order_id AND oi.product_id = p.product_id
);

-- Insert payment transaction for the sample order
INSERT INTO payment_transactions (
    order_id,
    transaction_type,
    amount,
    payment_method,
    payment_gateway,
    gateway_transaction_id,
    status,
    processed_at
)
SELECT 
    o.order_id,
    'capture',
    o.total_amount,
    'credit_card',
    'stripe',
    'txn_' || SUBSTR(MD5(RANDOM()::TEXT), 1, 16),
    'success',
    o.created_at + INTERVAL '5 minutes'
FROM orders o
JOIN users u ON o.user_id = u.user_id
WHERE u.email = 'jane.smith@example.com'
AND o.status = 'delivered'
AND NOT EXISTS (
    SELECT 1 FROM payment_transactions pt WHERE pt.order_id = o.order_id
);

-- Update order timestamps for the delivered order
UPDATE orders 
SET 
    shipped_at = created_at + INTERVAL '1 day',
    delivered_at = created_at + INTERVAL '3 days'
WHERE status = 'delivered' 
AND shipped_at IS NULL;

-- Reduce inventory for delivered order items
UPDATE product_inventory 
SET quantity_available = quantity_available - (
    SELECT COALESCE(SUM(oi.quantity), 0)
    FROM order_items oi
    JOIN orders o ON oi.order_id = o.order_id
    WHERE oi.product_id = product_inventory.product_id
    AND o.status IN ('delivered', 'shipped')
)
WHERE EXISTS (
    SELECT 1 
    FROM order_items oi
    JOIN orders o ON oi.order_id = o.order_id
    WHERE oi.product_id = product_inventory.product_id
    AND o.status IN ('delivered', 'shipped')
);