-- V002: Insert seed data for E-Commerce application
-- Author: Backend Automation Agent
-- Date: 2024

-- Insert sample categories
INSERT INTO categories (name, description, active) VALUES
('Electronics', 'Electronic devices and accessories', TRUE),
('Clothing', 'Apparel and fashion items', TRUE),
('Books', 'Books and publications', TRUE),
('Home & Garden', 'Home improvement and garden supplies', TRUE),
('Sports & Outdoors', 'Sports equipment and outdoor gear', TRUE);

-- Insert sample products
INSERT INTO products (sku, name, description, price, stock_quantity, category_id, active) VALUES
('ELEC-001', 'Wireless Bluetooth Headphones', 'High-quality wireless headphones with noise cancellation', 79.99, 50, 1, TRUE),
('ELEC-002', 'Smart Watch', 'Fitness tracking smart watch with heart rate monitor', 199.99, 30, 1, TRUE),
('ELEC-003', 'Laptop Stand', 'Ergonomic aluminum laptop stand', 39.99, 100, 1, TRUE),
('CLOTH-001', 'Cotton T-Shirt', 'Comfortable 100% cotton t-shirt', 19.99, 200, 2, TRUE),
('CLOTH-002', 'Denim Jeans', 'Classic fit denim jeans', 49.99, 150, 2, TRUE),
('CLOTH-003', 'Running Shoes', 'Lightweight running shoes with cushioned sole', 89.99, 75, 2, TRUE),
('BOOK-001', 'Programming Guide', 'Comprehensive guide to modern programming', 44.99, 60, 3, TRUE),
('BOOK-002', 'Cookbook', 'Collection of healthy recipes', 29.99, 80, 3, TRUE),
('HOME-001', 'LED Desk Lamp', 'Adjustable LED desk lamp with USB charging', 34.99, 120, 4, TRUE),
('SPORT-001', 'Yoga Mat', 'Non-slip yoga mat with carrying strap', 24.99, 90, 5, TRUE);

-- Insert sample user (password: Test@1234)
INSERT INTO users (username, email, password, first_name, last_name, phone_number, active, email_verified) VALUES
('testuser', 'test@example.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'Test', 'User', '+1234567890', TRUE, TRUE);

-- Insert sample address for test user
INSERT INTO addresses (user_id, address_line1, address_line2, city, state, postal_code, country, is_default, type) VALUES
(1, '123 Main Street', 'Apt 4B', 'New York', 'NY', '10001', 'USA', TRUE, 'BOTH');