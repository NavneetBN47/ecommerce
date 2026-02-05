-- V002__add_sample_data.sql
-- Sample data for testing and development
-- Version: 1.0.0
-- Description: Inserts sample categories, products, and test user

-- Insert sample categories
INSERT INTO categories (category_name, description, is_active) VALUES
('Electronics', 'Electronic devices and accessories', TRUE),
('Clothing', 'Apparel and fashion items', TRUE),
('Books', 'Books and educational materials', TRUE),
('Home & Garden', 'Home improvement and garden supplies', TRUE),
('Sports', 'Sports equipment and accessories', TRUE)
ON CONFLICT (category_name) DO NOTHING;

-- Insert sample products
INSERT INTO products (product_name, description, sku, price, stock_quantity, category_id, image_url, is_active)
SELECT 
    'Laptop Pro 15', 
    'High-performance laptop with 16GB RAM and 512GB SSD', 
    'ELEC-LAP-001', 
    1299.99, 
    50,
    (SELECT category_id FROM categories WHERE category_name = 'Electronics'),
    'https://example.com/images/laptop.jpg',
    TRUE
WHERE NOT EXISTS (SELECT 1 FROM products WHERE sku = 'ELEC-LAP-001');

INSERT INTO products (product_name, description, sku, price, stock_quantity, category_id, image_url, is_active)
SELECT 
    'Wireless Mouse', 
    'Ergonomic wireless mouse with USB receiver', 
    'ELEC-MOU-001', 
    29.99, 
    200,
    (SELECT category_id FROM categories WHERE category_name = 'Electronics'),
    'https://example.com/images/mouse.jpg',
    TRUE
WHERE NOT EXISTS (SELECT 1 FROM products WHERE sku = 'ELEC-MOU-001');

INSERT INTO products (product_name, description, sku, price, stock_quantity, category_id, image_url, is_active)
SELECT 
    'Cotton T-Shirt', 
    'Comfortable 100% cotton t-shirt', 
    'CLTH-TSH-001', 
    19.99, 
    500,
    (SELECT category_id FROM categories WHERE category_name = 'Clothing'),
    'https://example.com/images/tshirt.jpg',
    TRUE
WHERE NOT EXISTS (SELECT 1 FROM products WHERE sku = 'CLTH-TSH-001');

INSERT INTO products (product_name, description, sku, price, stock_quantity, category_id, image_url, is_active)
SELECT 
    'Programming Guide', 
    'Comprehensive guide to modern programming', 
    'BOOK-PRG-001', 
    49.99, 
    100,
    (SELECT category_id FROM categories WHERE category_name = 'Books'),
    'https://example.com/images/book.jpg',
    TRUE
WHERE NOT EXISTS (SELECT 1 FROM products WHERE sku = 'BOOK-PRG-001');

INSERT INTO products (product_name, description, sku, price, stock_quantity, category_id, image_url, is_active)
SELECT 
    'Yoga Mat', 
    'Non-slip yoga mat with carrying strap', 
    'SPRT-YOG-001', 
    39.99, 
    150,
    (SELECT category_id FROM categories WHERE category_name = 'Sports'),
    'https://example.com/images/yogamat.jpg',
    TRUE
WHERE NOT EXISTS (SELECT 1 FROM products WHERE sku = 'SPRT-YOG-001');

-- Insert test user (password: Test@123)
INSERT INTO users (username, email, password_hash, full_name, phone, address, is_active)
SELECT 
    'testuser',
    'test@example.com',
    '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy',
    'Test User',
    '+1234567890',
    '123 Test Street, Test City, TC 12345',
    TRUE
WHERE NOT EXISTS (SELECT 1 FROM users WHERE username = 'testuser');

COMMENT ON TABLE categories IS 'Sample categories added for testing';
COMMENT ON TABLE products IS 'Sample products added for testing';