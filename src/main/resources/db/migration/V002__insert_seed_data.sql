-- V002__insert_seed_data.sql
-- Seed data for testing

-- Insert sample users (password is 'password123' hashed with BCrypt)
INSERT INTO users (username, email, password, first_name, last_name, is_active, is_deleted) VALUES
('john_doe', 'john.doe@example.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'John', 'Doe', TRUE, FALSE),
('jane_smith', 'jane.smith@example.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'Jane', 'Smith', TRUE, FALSE),
('bob_wilson', 'bob.wilson@example.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'Bob', 'Wilson', TRUE, FALSE);

-- Insert sample products
INSERT INTO products (sku, name, description, price, stock_quantity, category, image_url, is_active, is_deleted) VALUES
('LAPTOP-001', 'Dell XPS 15', 'High-performance laptop with 15-inch display', 1299.99, 50, 'Electronics', 'https://example.com/images/laptop1.jpg', TRUE, FALSE),
('LAPTOP-002', 'MacBook Pro 14', 'Apple MacBook Pro with M2 chip', 1999.99, 30, 'Electronics', 'https://example.com/images/laptop2.jpg', TRUE, FALSE),
('PHONE-001', 'iPhone 14 Pro', 'Latest iPhone with advanced camera system', 999.99, 100, 'Electronics', 'https://example.com/images/phone1.jpg', TRUE, FALSE),
('PHONE-002', 'Samsung Galaxy S23', 'Flagship Android smartphone', 899.99, 80, 'Electronics', 'https://example.com/images/phone2.jpg', TRUE, FALSE),
('TABLET-001', 'iPad Air', 'Lightweight and powerful tablet', 599.99, 60, 'Electronics', 'https://example.com/images/tablet1.jpg', TRUE, FALSE),
('WATCH-001', 'Apple Watch Series 8', 'Advanced health and fitness features', 399.99, 75, 'Wearables', 'https://example.com/images/watch1.jpg', TRUE, FALSE),
('HEADPHONE-001', 'Sony WH-1000XM5', 'Premium noise-canceling headphones', 349.99, 120, 'Audio', 'https://example.com/images/headphone1.jpg', TRUE, FALSE),
('HEADPHONE-002', 'AirPods Pro', 'Wireless earbuds with active noise cancellation', 249.99, 150, 'Audio', 'https://example.com/images/headphone2.jpg', TRUE, FALSE),
('KEYBOARD-001', 'Logitech MX Keys', 'Wireless illuminated keyboard', 99.99, 200, 'Accessories', 'https://example.com/images/keyboard1.jpg', TRUE, FALSE),
('MOUSE-001', 'Logitech MX Master 3', 'Advanced wireless mouse', 99.99, 180, 'Accessories', 'https://example.com/images/mouse1.jpg', TRUE, FALSE);